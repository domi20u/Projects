#include "opencv2\opencv.hpp"
#include <windows.h>

using namespace cv;
using namespace std;

void getLeft(Mat in, Mat& out) {
	Size size = in.size();
	int w = size.width;
	int h = size.height;
	Rect ROI(0, 0, w / 2, h);
	out = in(ROI);
}
void rawToDisp(Mat Input, Mat& DispMap, Mat& DispCol) {
	Mat frame;//, disp, dispCol
	Mat K1, K2, D1, D2, R1, R2, P1, P2;//, F, E;
	
	cv::FileStorage fs2("out_file_28_01", cv::FileStorage::READ);
	fs2["K1"] >> K1;
	fs2["D1"] >> D1;
	fs2["K2"] >> K2;
	fs2["D2"] >> D2;
	fs2["R1"] >> R1;
	fs2["R2"] >> R2;
	fs2["P1"] >> P1;
	fs2["P2"] >> P2;
	fs2.release();
	
	printf("splitting...\n");
	Size size = Input.size();
	int w = size.width;
	int h = size.height;
	
	Rect ROI_l(0, 0, w/2, h);
	Rect ROI_r(w/2, 0, w/2, h);
	Mat left = Input(ROI_l);
	Mat right = Input(ROI_r);

	Mat map1x, map1y, map2x, map2y;
	printf("start undistorting frames...\n");
	initUndistortRectifyMap(K1, D1, R1, P1, Size(w/2, h), CV_16SC2, map1x, map1y);
	initUndistortRectifyMap(K2, D2, R2, P2, Size(w/2, h), CV_16SC2, map2x, map2y);

	Mat left_rec, right_rec;
	printf("start remapping...\n");
	remap(left, left_rec, map1x, map1y, INTER_LINEAR);
	printf("right..\n");
	remap(right, right_rec, map2x, map2y, INTER_LINEAR);

	Rect ROI_new(1, 21, 1598, 1179);
	Mat left_new = left_rec(ROI_new);
	Mat right_new = right_rec(ROI_new);

	Mat temp1, temp2;
	printf("resizing...\n");
	left_new.convertTo(left_new, CV_8UC1);
	right_new.convertTo(right_new, CV_8UC1);
	resize(left_new, temp1, Size(), 0.4, 0.4);
	resize(right_new, temp2, Size(), 0.4, 0.4);
	//temp1.convertTo(left_new, CV_8UC1);
	//temp2.convertTo(right_new, CV_8UC1);
	left_new = temp1;
	right_new = temp2;
	printf("start desparity Map creation:\n");
	Ptr<StereoSGBM> sgbm1 = StereoSGBM::create(0, 16, 3);	
	sgbm1->setNumDisparities(48);
	sgbm1->setMinDisparity(-9);
	sgbm1->setPreFilterCap(31);
	sgbm1->setBlockSize(13);
	sgbm1->setUniquenessRatio(9);
	sgbm1->setSpeckleWindowSize(1000);
	sgbm1->setSpeckleRange(1);
	sgbm1->setDisp12MaxDiff(-1);
	sgbm1->setP1(8*13*13); // 13 = blocksize!
	sgbm1->setP2(32*13*13);

	sgbm1->setMode(StereoSGBM::MODE_SGBM_3WAY);
	Mat disp, dispCol, disp8, disp82;
	//printf("computing...\n");
	sgbm1->compute(right_new, left_new, disp);
	//printf("converting...\n");
	double min, max;
	minMaxIdx(disp, &min, &max);
	double diff = max - min;
//check diff! shouldn't change a lot, otherwise disp-values will change from frame to frame -> intensities showing different distances!
	disp = (disp + 160) / (diff / 255);	

	disp.convertTo(disp8, CV_8U);
	//printf("resize...\n");
	applyColorMap(disp8, dispCol, COLORMAP_JET);
	//resize(disp8, Disp, Size(960, 720));
	resize(dispCol, DispCol, Size(960, 720));
	resize(disp8, DispMap, Size(960, 720));
	//printf("finished!\n");
}

void mergeM(vector<Mat> images, Mat &out) {
	Mat temp;
	Ptr<AlignMTB> alignMTB = createAlignMTB();
	alignMTB->process(images, images);
	Ptr<MergeMertens> mergeMertens = createMergeMertens(1, 1, 0);
	mergeMertens->process(images, out);
	out = out * 255;	
	out.convertTo(out, CV_8UC1);
	resize(out, out, Size(3200, 1200));
}
void hdrR(vector<Mat> images, int diff, Mat &out) {
	vector<float> times;
	static const float timesArray[] = { 1.0, diff };
	times.assign(timesArray, timesArray + 2);

	//vector<Mat> images;
	//images.push_back(in1);
	//images.push_back(in2);
	Ptr<AlignMTB> alignMTB = createAlignMTB();
	alignMTB->process(images, images);

	Mat responseDebevec, hdrDebevec;
	Ptr<CalibrateDebevec> calibrateDebevec = createCalibrateDebevec();
	calibrateDebevec->process(images, responseDebevec, times);

	Ptr<MergeDebevec> mergeDebevec = createMergeDebevec();
	mergeDebevec->process(images, hdrDebevec, times);

	Ptr<TonemapReinhard> tonemapReinhard = createTonemapReinhard(1.5, 2.0, 0, 0);
	tonemapReinhard->process(hdrDebevec, out);
	out = out * 255;
	out.convertTo(out, CV_8U);
}

void mergeNdisp(cv::VideoCapture vid, string dir_out, int startFrame, int endFrame, bool makeVid, int fps, int fourcc) {
	cout << "started MERGE..." << endl;
	Mat frame, left, dispCol, dispMap;
	Mat result;
	int counter = 0;
	int count = 0, countFractions = 0, startingFrame = -1;
	vector<Mat> frames;
	int bright_dark_diff = 10;
	bool ready = false;
	bool save = false;
	Scalar a, b, c;
	Mat img1, img2;
	if (makeVid) {
		string s_out0 = dir_out + to_string(startFrame) + "_" + to_string(endFrame) + "merge_d.avi";
		string s_out1 = dir_out + to_string(startFrame) + "_" + to_string(endFrame) + "merge.avi";
		string s_out2 = dir_out + to_string(startFrame) + "_" + to_string(endFrame) + "merge_disp.avi";
		VideoWriter writerMergeD = VideoWriter(s_out0, fourcc, fps / 2, Size(960, 720), 0);
		VideoWriter writerMerge = VideoWriter(s_out1, fourcc, fps / 2, Size(1600, 1200), 1);
		VideoWriter writerMergeDisp = VideoWriter(s_out2, fourcc, fps / 2, Size(960, 720), 1);
		cout << "Start & End: " << startFrame << " & " << endFrame << endl;
		while (vid.isOpened()) {
			vid >> frame;
			if (counter % 22 == 0) {
				cout << counter / 22 << endl;
			}
			if (counter >= startFrame && counter <= endFrame) {
				if (count == startingFrame) {
					ready = true;
					std::cout << "starting frame: " << count << endl;
				}
				
				if (ready) {
					if (counter == ((endFrame + startFrame) / 2)) {
						save = true;
					}
					else {
						save = false;
					}
					if (countFractions == 0) {
						a = mean(frame);
						cout << "a: " << a[0] << endl;
						if ((a[0] + bright_dark_diff) < b[0]) {
							frames.push_back(frame.clone());
							countFractions++;
							if (save) {
								imwrite("dark.jpg", frame);
							}
						}
						else {
							cout << "found two BRIGHT frames in a row" << endl;
						}
					}
					else if (countFractions == 1) {
						b = mean(frame);
						cout << "b: " << b[0] << endl;
						if ((a[0] + bright_dark_diff) < b[0]) {
							frames.push_back(frame);
							countFractions++;
							if (save) {
								imwrite("bright.jpg", frame);
							}
						}
						else {
							cout << "found two dark frames in a row" << endl;
						}
					}
					if (countFractions == 2) {
						//if (count >= startFrame && count <= endFrame) {
						cout << "processing: " << (endFrame - counter) << endl;
						mergeM(frames, result);
						cout << "finish merging" << endl;
						writerMerge.write(result);
						if (save) {
							getLeft(result, left);
							imwrite("merge.jpg", left);
						}
						cout << "dispMap..." << endl;
						//result2 = imread("merge.jpg");
						rawToDisp(result, dispMap, dispCol);
						writerMergeDisp.write(dispCol);
						writerMergeD.write(dispMap);
						if (save) {
							imwrite("dispCol.jpg", dispCol);
							imwrite("dispMap.jpg", dispMap);
						}
						

						//}
						countFractions = 0;
						frames = {};
					}
				}
				else if (count == 0) {
					a = mean(frame);
				}
				else if (count == 1) {
					b = mean(frame);
					if ((a[0] + bright_dark_diff) < b[0]) {
						startingFrame = 2;
					}
					else {
						b = a;
						if ((b[0] + bright_dark_diff) < a[0]) {
							startingFrame = 3;
						}
					}
				}
				else if (count == 2) {
					c = mean(frame);
					if ((c[0] + bright_dark_diff) < b[0]) {
						a = c;
						startingFrame = 4;
					}
					else {
						b = c;
						startingFrame = 3;
					}
				
				}
				count++;
				
			}
			counter++;
			if (counter > endFrame) {
				vid.release();
				writerMerge.release();
				writerMergeDisp.release();
				writerMergeD.release();
				break;
			}
		}
	}
	else {
		string s_out0 = dir_out + to_string(startFrame/22) + "merg_d.jpg";
		string s_out1 = dir_out + to_string(startFrame/22) + "merge.jpg";
		string s_out2 = dir_out + to_string(startFrame/22) + "merge_disp.jpg";
		while (vid.isOpened()) {
			vid >> frame;
			if (counter % 22 == 0) {
				cout << counter / 22 << endl;
			}
			if (counter >= startFrame) {
				if (count == startingFrame) {
					ready = true;
					std::cout << "starting frame: " << count << endl;
				}
				
				if (ready) {
					if (countFractions == 0) {
						a = mean(frame);
						//imwrite("dark1.jpg", frame);
						//cout << "a: " << a[0] << endl;
						if ((a[0] + bright_dark_diff) < b[0]) {
							frames.push_back(frame.clone());
							countFractions++;
							imwrite("dark.jpg", frame);
						}
						else {
							cout << "found two BRIGHT frames in a row" << endl;
						}
					}
					else if (countFractions == 1) {
						b = mean(frame);
						//imwrite("bright1.jpg", frame);
						//cout << "b: " << b[0] << endl;
						if ((a[0] + bright_dark_diff) < b[0]) {
							frames.push_back(frame);
							countFractions++;
							imwrite("bright.jpg", frame);
							}
						else {
							cout << "found two dark frames in a row" << endl;
						}
					}
					if (countFractions == 2) {
						cout << "processing: " << (endFrame/22 - counter/22) << endl;
						mergeM(frames, result);
						getLeft(result, left);
						cout << "finish merging" << endl;
						imwrite(s_out1, left);
						//cout << "Image Size: " << result.size() << endl;					
						cout << "dispMap..." << endl;
						rawToDisp(result, dispMap, dispCol);
						imwrite(s_out2, dispCol);
						imwrite(s_out0, dispMap);
						cout << "written image successfully" << endl;
						break;
					}
				}
				else if (count == 0) {
					a = mean(frame);
				}
				else if (count == 1) {
					b = mean(frame);
					if ((a[0] + bright_dark_diff) < b[0]) {
						startingFrame = 2;
					}
					else {
						b = a;
						if ((b[0] + bright_dark_diff) < a[0]) {
							startingFrame = 3;
						}
					}
				}
				else if (count == 2) {
					c = mean(frame);
					if ((c[0] + bright_dark_diff) < b[0]) {
						a = c;
						startingFrame = 4;
					}
					else {
						b = c;
						startingFrame = 3;
					}

				}
				count++;

			}
			counter++;
			if (counter > (endFrame+count)) {
				vid.release();
				break;
			}
		}
	}
}
void hdrNdisp(cv::VideoCapture vid, string dir_out, int diff, int startFrame, int endFrame, bool makeVid, int fps, int fourcc) {
	cout << "started HDR..." << endl;
	Mat frame, left, dispCol, dispMap;
	Mat result;
	int counter = 0;
	int count = 0, countFractions = 0, startingFrame = -1;
	vector<Mat> frames;
	int bright_dark_diff = 10;
	bool ready = false;
	bool save = false;
	Scalar a, b, c;
	Mat img1, img2;
	if (makeVid) {
		string s_out0 = dir_out + to_string(startFrame) + "_" + to_string(endFrame) + "hdrD.avi";
		string s_out3 = dir_out + to_string(startFrame) + "_" + to_string(endFrame) + "hdr.avi";
		string s_out4 = dir_out + "_" + to_string(startFrame) + "_" + to_string(endFrame) + "hdr_disp.avi";
		VideoWriter writerHdrD = VideoWriter(s_out0, fourcc, fps / 2, Size(960, 720), 0);
		VideoWriter writerHdr = VideoWriter(s_out3, fourcc, fps / 2, Size(1600, 1200), 1);
		VideoWriter writerHdrDisp = VideoWriter(s_out4, fourcc, fps / 2, Size(960, 720), 1);
		cout << "Start & End: " << startFrame << " & " << endFrame << endl;
		while (vid.isOpened()) {
			vid >> frame;
			if (counter % 22 == 0) {
				cout << counter / 22 << endl;
			}
			if (counter >= startFrame && counter <= endFrame) {
				if (count == startingFrame) {
					ready = true;
					std::cout << "starting frame: " << count << endl;
				}

				if (ready) {
					if (counter == ((endFrame + startFrame) / 2)) {
						save = true;
					}
					else {
						save = false;
					}
					if (countFractions == 0) {
						a = mean(frame);
						cout << "a: " << a[0] << endl;
						if ((a[0] + bright_dark_diff) < b[0]) {
							frames.push_back(frame.clone());
							countFractions++;
							if (save) {
								imwrite("dark.jpg", frame);
							}
						}
						else {
							cout << "found two BRIGHT frames in a row" << endl;
						}
					}
					else if (countFractions == 1) {
						b = mean(frame);
						cout << "b: " << b[0] << endl;
						if ((a[0] + bright_dark_diff) < b[0]) {
							frames.push_back(frame);
							countFractions++;
							if (save) {
								imwrite("bright.jpg", frame);
							}
						}
						else {
							cout << "found two dark frames in a row" << endl;
						}
					}
					if (countFractions == 2) {
						//if (count >= startFrame && count <= endFrame) {
						cout << "processing: " << (endFrame - counter) << endl;
						hdrR(frames, diff, result);
						cout << "finish merging" << endl;
						writerHdr.write(result);
						if (save) {
							getLeft(result, left);
							imwrite("hdr.jpg", left);
						}
						cout << "dispMap..." << endl;
						//result2 = imread("merge.jpg");
						rawToDisp(result, dispMap, dispCol);
						writerHdrDisp.write(dispCol);
						writerHdrD.write(dispMap);
						if (save) {
							cout << "save!" << endl << endl << endl << endl;
							imwrite("dispCol.jpg", dispCol);
							imwrite("dispMap.jpg", dispMap);
						}


						//}
						countFractions = 0;
						frames = {};
					}
				}
				else if (count == 0) {
					a = mean(frame);
				}
				else if (count == 1) {
					b = mean(frame);
					if ((a[0] + bright_dark_diff) < b[0]) {
						startingFrame = 2;
					}
					else {
						b = a;
						if ((b[0] + bright_dark_diff) < a[0]) {
							startingFrame = 3;
						}
					}
				}
				else if (count == 2) {
					c = mean(frame);
					if ((c[0] + bright_dark_diff) < b[0]) {
						a = c;
						startingFrame = 4;
					}
					else {
						b = c;
						startingFrame = 3;
					}

				}
				count++;

			}
			counter++;
			if (counter > endFrame) {
				vid.release();
				writerHdr.release();
				writerHdrDisp.release();
				writerHdrD.release();
				break;
			}
		}
	}
	else {
		string s_out0 = dir_out + to_string(startFrame/22) + "hdr_d.jpg";
		string s_out1 = dir_out + to_string(startFrame/22) + "hdr.jpg";
		string s_out2 = dir_out + to_string(startFrame/22) + "hdr_disp.jpg";
		while (vid.isOpened()) {
			vid >> frame;
			if (counter % 22 == 0) {
				cout << counter / 22 << endl;
			}
			if (counter >= startFrame) {
				if (count == startingFrame) {
					ready = true;
					std::cout << "starting frame: " << count << endl;
				}

				if (ready) {
					if (countFractions == 0) {
						a = mean(frame);
						//imwrite("dark1.jpg", frame);
						//cout << "a: " << a[0] << endl;
						if ((a[0] + bright_dark_diff) < b[0]) {
							frames.push_back(frame.clone());
							countFractions++;
							imwrite("dark.jpg", frame);
						}
						else {
							cout << "found two BRIGHT frames in a row" << endl;
						}
					}
					else if (countFractions == 1) {
						b = mean(frame);
						//imwrite("bright1.jpg", frame);
						//cout << "b: " << b[0] << endl;
						if ((a[0] + bright_dark_diff) < b[0]) {
							frames.push_back(frame);
							countFractions++;
							imwrite("bright.jpg", frame);
						}
						else {
							cout << "found two dark frames in a row" << endl;
						}
					}
					if (countFractions == 2) {
						cout << "processing: " << (endFrame/22 - counter/22) << endl;
						hdrR(frames, diff, result);
						cout << "finish merging" << endl;
						getLeft(result, left);
						imwrite(s_out1, left);
						//cout << "Image Size: " << result.size() << endl;
						cout << "dispMap..." << endl;
						rawToDisp(result, dispMap, dispCol);
						imwrite(s_out2, dispCol);
						imwrite(s_out0, dispMap);
						cout << "written image successfully" << endl;
						break;
					}
				}
				else if (count == 0) {
					a = mean(frame);
				}
				else if (count == 1) {
					b = mean(frame);
					if ((a[0] + bright_dark_diff) < b[0]) {
						startingFrame = 2;
					}
					else {
						b = a;
						if ((b[0] + bright_dark_diff) < a[0]) {
							startingFrame = 3;
						}
					}
				}
				else if (count == 2) {
					c = mean(frame);
					if ((c[0] + bright_dark_diff) < b[0]) {
						a = c;
						startingFrame = 4;
					}
					else {
						b = c;
						startingFrame = 3;
					}

				}
				count++;

			}
			counter++;
			if (counter > (endFrame + count)) {
				vid.release();
				break;
			}
		}
	}
}
void mergeNhdrNdisps(cv::VideoCapture vid, string dir_out, int diff, int startFrame, int endFrame, bool makeVid, int fps, int fourcc) {
	cout << "started MERGE and HDR..." << endl;

	Mat merge, mDispCol, mDispMap;
	Mat hdr, hDispCol, hDispMap;
	Mat frame, left1, left2;
	Mat result_merge, result_hdr;
	int counter = 0;
	int count = 0, countFractions = 0, startingFrame = -1;
	vector<Mat> frames;
	int bright_dark_diff = 10;
	bool ready = false;
	bool save = false;
	Scalar a, b, c;
	int co = 0;
	
	Mat img1, img2;
	if (makeVid) {
		string s_out0 = dir_out + "_" + to_string(startFrame/22) + "_" + to_string(endFrame / 22) + "mergeD.avi";
		string s_out1 = dir_out + "_" + to_string(startFrame / 22) + "_" + to_string(endFrame / 22) + "merge.avi";
		string s_out2 = dir_out + "_" + to_string(startFrame / 22) + "_" + to_string(endFrame / 22) + "merge_disp.avi";
		VideoWriter writerMergeD = VideoWriter(s_out0, fourcc, fps / 2, Size(960, 720), 0);
		VideoWriter writerMerge = VideoWriter(s_out1, fourcc, fps / 2, Size(1600, 1200), 1);
		VideoWriter writerMergeDisp = VideoWriter(s_out2, fourcc, fps / 2, Size(960, 720), 1);

		string s_out5 = dir_out + "_" + to_string(startFrame / 22) + "_" + to_string(endFrame / 22) + "hdrD.avi";
		string s_out3 = dir_out + "_" + to_string(startFrame / 22) + "_" + to_string(endFrame / 22) + "hdr.avi";
		string s_out4 = dir_out + "_" + to_string(startFrame / 22) + "_" + to_string(endFrame / 22) + "hdr_disp.avi";
		VideoWriter writerHdrD = VideoWriter(s_out5, fourcc, fps / 2, Size(960, 720), 0);
		VideoWriter writerHdr = VideoWriter(s_out3, fourcc, fps / 2, Size(1600, 1200), 1);
		VideoWriter writerHdrDisp = VideoWriter(s_out4, fourcc, fps / 2, Size(960, 720), 1);
		cout << "Start & End: " << startFrame/22 << " & " << endFrame/22 << endl;
		while (vid.isOpened()) {
			vid >> frame;
			
			cout   << counter / 22 << ", " << counter << endl;
			
			if (counter >= startFrame && counter <= endFrame) {
				if (count == startingFrame) {
					ready = true;
					std::cout << "starting frame: " << count << endl;
				}

				if (ready) {
					
					if (counter == ((endFrame + startFrame) / 2)) {
						save = true;
					}
					else {
						save = false;
					}
					if (countFractions == 0) {
						a = mean(frame);
						cout << "dark: " << a[0] << endl;
						if ((a[0] + bright_dark_diff) < b[0]) {
							frames.push_back(frame.clone());
							countFractions++;
							co++;
							if (save) {
								imwrite("dark.jpg", frame);
							}
						}
						else {
							cout << "found two BRIGHT frames in a row" << endl;
						}
					}
					else if (countFractions == 1) {
						b = mean(frame);
						cout << "bright: " << b[0] << endl;
						if ((a[0] + bright_dark_diff) < b[0]) {
							frames.push_back(frame);
							countFractions++;
							co++;
							if (save) {
								imwrite("bright.jpg", frame);
							}
						}
						else {
							cout << "found two dark frames in a row" << endl;
						}
					}
					if (countFractions == 2) {					
						//if (co%7 == 0) {
							cout << "processing: " << (endFrame - counter) << endl;
							mergeM(frames, result_merge);
							cout << "finish merging" << endl;
							getLeft(result_merge, left1);
							writerMerge.write(left1);
							hdrR(frames, diff, result_hdr);
							getLeft(result_hdr, left2);
							writerHdr.write(left2);
							cout << "finish hdr" << endl;
							if (save) {
								getLeft(result_merge, left1);
								getLeft(result_hdr, left2);
								imwrite("merge.jpg", left1);
								imwrite("hdr.jpg", left2);
							}
							cout << "dispMap..." << endl;
							//result2 = imread("merge.jpg");
							rawToDisp(result_merge, mDispMap, mDispCol);
							rawToDisp(result_hdr, hDispMap, hDispCol);
							writerMergeDisp.write(mDispCol);
							writerMergeD.write(mDispMap);
							writerHdrDisp.write(hDispCol);
							writerHdrD.write(hDispMap);
							if (save) {
								imwrite("hdispCol.jpg", hDispCol);
								imwrite("mDispCol.jpg", mDispCol);
								imwrite("hDispMap.jpg", hDispMap);
								imwrite("mDispMap.jpg", mDispMap);
							}


							//}
							countFractions = 0;
							frames = {};
						//}
						co++;
					}
				}
				else if (count == 0) {
					a = mean(frame);
				}
				else if (count == 1) {
					b = mean(frame);
					if ((a[0] + bright_dark_diff) < b[0]) {
						startingFrame = 2;
					}
					else {
						b = a;
						if ((b[0] + bright_dark_diff) < a[0]) {
							startingFrame = 3;
						}
					}
				}
				else if (count == 2) {
					c = mean(frame);
					if ((c[0] + bright_dark_diff) < b[0]) {
						a = c;
						startingFrame = 4;
					}
					else {
						b = c;
						startingFrame = 3;
					}

				}
				count++;

			}
			counter++;
			if (counter > endFrame) {
				vid.release();
				writerMerge.release();
				writerHdr.release();
				writerMergeDisp.release();
				writerHdrDisp.release();
				writerMergeD.release();
				writerHdrD.release();
				break;
			}
		}
	}
	else {
		string s_out0 = dir_out + to_string(startFrame/22) + "merge_d.jpg";
		string s_out1 = dir_out + to_string(startFrame/22) + "merge.jpg";
		string s_out2 = dir_out + to_string(startFrame/22) + "merge_disp.jpg";
		string s_out3 = dir_out + to_string(startFrame/22) + "hdr.jpg";
		string s_out4 = dir_out + to_string(startFrame/22) + "hdr_disp.jpg";
		string s_out5 = dir_out + to_string(startFrame/22) + "hdr_d.jpg";
		while (vid.isOpened()) {
			vid >> frame;
			if (counter % 22 == 0) {
				cout << counter / 22 << endl;
			}
			if (counter >= startFrame) {
				if (count == startingFrame) {
					ready = true;
					std::cout << "starting frame: " << count << endl;
				}

				if (ready) {
					if (countFractions == 0) {
						a = mean(frame);
						//imwrite("dark1.jpg", frame);
						//cout << "a: " << a[0] << endl;
						if ((a[0] + bright_dark_diff) < b[0]) {
							frames.push_back(frame.clone());
							countFractions++;
							imwrite("dark.jpg", frame);
						}
						else {
							cout << "found two BRIGHT frames in a row" << endl;
						}
					}
					else if (countFractions == 1) {
						b = mean(frame);
						//imwrite("bright1.jpg", frame);
						//cout << "b: " << b[0] << endl;
						if ((a[0] + bright_dark_diff) < b[0]) {
							frames.push_back(frame);
							countFractions++;
							imwrite("bright.jpg", frame);
						}
						else {
							cout << "found two dark frames in a row" << endl;
						}
					}
					if (countFractions == 2) {
						cout << "processing: " << (endFrame/22 - counter/22) << endl;
						mergeM(frames, result_merge);
						cout << "finish merging" << endl;
						getLeft(result_merge, left1);
						imwrite(s_out1, left1);
						hdrR(frames, diff, result_hdr);
						getLeft(result_hdr, left2);
						imwrite(s_out3, left2);
						cout << "finish hdr" << endl;
						//cout << "Image Size: " << result.size() << endl;
						cout << "dispMap..." << endl;
						rawToDisp(result_merge, mDispMap, mDispCol);
						imwrite(s_out2, mDispCol);
						imwrite(s_out0, mDispMap);
						rawToDisp(result_hdr, hDispMap, hDispCol);
						imwrite(s_out4, hDispCol);
						imwrite(s_out5, hDispMap);
						cout << "written images successfully" << endl;
						break;
					}
				}
				else if (count == 0) {
					a = mean(frame);
				}
				else if (count == 1) {
					b = mean(frame);
					if ((a[0] + bright_dark_diff) < b[0]) {
						startingFrame = 2;
					}
					else {
						b = a;
						if ((b[0] + bright_dark_diff) < a[0]) {
							startingFrame = 3;
						}
					}
				}
				else if (count == 2) {
					c = mean(frame);
					if ((c[0] + bright_dark_diff) < b[0]) {
						a = c;
						startingFrame = 4;
					}
					else {
						b = c;
						startingFrame = 3;
					}

				}
				count++;

			}
			counter++;
			if (counter > (endFrame + count)) {
				vid.release();
				break;
			}
		}
	}
}
void normToDisp(cv::VideoCapture vid, string dir_out, int startFrame, int endFrame, bool makeVid, int fps, int fourcc) {
	Mat frame, left, dispCol, dispMap;	
	int counter = 0;
	int count = 0;
	bool save = false;
	int fac = 2;
	if (makeVid) {
		string s_out0 = dir_out + to_string(startFrame/22) + "_" + to_string(endFrame/22) + "_d.avi";
		string s_out1 = dir_out + to_string(startFrame/22) + "_" + to_string(endFrame/22) + ".avi";
		string s_out2 = dir_out + to_string(startFrame/22) + "_" + to_string(endFrame/22) + "_disp.avi";
		VideoWriter writerD = VideoWriter(s_out0, fourcc, fps/fac, Size(960, 720), 0);
		VideoWriter writer = VideoWriter(s_out1, fourcc, fps/fac, Size(1600, 1200), 1);
		VideoWriter writerDisp = VideoWriter(s_out2, fourcc, fps/fac, Size(960, 720), 1);
		cout << "Start & End: " << startFrame/22 << " & " << endFrame/22 << endl;
		while (vid.isOpened()) {
			vid >> frame;
			if (counter % fac == 0) {
				//if (counter % 22 == 0) {
				cout << counter / 22 << ", " << counter << endl;
				//}
				if (counter == ((endFrame + startFrame) / 2)) {
					save = true;
				}
				else {
					save = false;
				}
				if (counter >= startFrame && counter <= endFrame) {
					cout << "processing: " << (endFrame - counter) << endl;
					rawToDisp(frame, dispMap, dispCol);
					writerDisp.write(dispCol);
					writerD.write(dispMap);
					getLeft(frame, left);
					writer.write(left);
					if (save) {
						imwrite("dispCol.jpg", dispCol);
						imwrite("dispMap.jpg", dispMap);
					}
				}
				
			}
			counter++;
			if (counter > endFrame) {
				vid.release();
				writer.release();
				writerDisp.release();
				writerD.release();
				break;
			}
		}
	}
	else{
		while (vid.isOpened()) {
			vid >> frame;
			if (counter % 22 == 0) {
				cout << counter / 22 << endl;
			}
			string s_out0 = dir_out + to_string(startFrame/22) + "_d.jpg";
			string s_out1 = dir_out + to_string(startFrame/22) + ".jpg";
			string s_out2 = dir_out + to_string(startFrame/22)+ "_disp.jpg";
			if (counter >= startFrame && counter <= endFrame) {
				cout << "processing: " << (endFrame/22 - counter/22) << endl;
				rawToDisp(frame, dispMap, dispCol);
				imwrite(s_out2, dispCol);
				getLeft(frame, left);
				imwrite(s_out1, left);
				imwrite(s_out0, dispMap);
			}
			counter++;
			if (counter > endFrame) {
				vid.release();
				break;
			}
		}
	}
}

int VidToHdrDisp(string name, bool merging, bool hdrImaging, int vidStart, int vidEnd) {
	//string dir_out = "C:\\Users\Dominik\Documents\Visual Studio 2015\Projects\DispMap_Processing\DispMap_Processing\result";
	string dir_out = "result/"+name;
	int fps = 22;
	int diff = 4;  //diff for hdr images
/*
	if (CreateDirectory((LPCWSTR)string(dir_o).c_str(), NULL) ||		//(LPCWSTR)string(dir_out+"\\"+name).c_str()
		ERROR_ALREADY_EXISTS == GetLastError())
	{
		std::cout << "created folder" << endl;
	}
	else
	{
		std::cout << "folder already exists" << endl;
	}
	*/
	string s_in = name + ".mp4";
	//string dir_out = dir_o + "/";
	Mat frame, dispCol;
	Mat merge, mDispCol;
	Mat hdr, hDispCol;
		
	VideoCapture vid(s_in);
	int numFrames = (int)vid.get(CAP_PROP_FRAME_COUNT);
	printf("Number of Frames: %d\n", numFrames);

	int fourcc = CV_FOURCC('D', 'I', 'V', '3');
	int startFrame = vidStart * fps;
	int endFrame = vidEnd * fps;
	int mode;
	bool makeVid;
		
	if (endFrame > numFrames) {
		endFrame = numFrames-5;  // cause of first test, whether start frame is a darker one 
	}
	if (startFrame >= 0 && endFrame >= 0 && startFrame <= endFrame) {
		if (startFrame < endFrame) {
			makeVid = true;
		}
		else {
			makeVid = false;
		}
	}
	else {
		makeVid = false;
		startFrame = numFrames / 2;
		endFrame = startFrame;
	}
	if (merging && hdrImaging) {
		mergeNhdrNdisps(vid, dir_out, diff, startFrame, endFrame, makeVid, fps, fourcc);
	}
	else if (hdrImaging) {
		hdrNdisp(vid, dir_out, diff, startFrame, endFrame, makeVid, fps, fourcc);
	}
	else if (merging) {	
		mergeNdisp(vid, dir_out, startFrame, endFrame, makeVid, fps, fourcc);
	}
	else {
		normToDisp(vid, dir_out, startFrame, endFrame, makeVid, fps, fourcc);
	}
	/*
	if (merging || hdrImaging) {
		if (merging) {
			string s_out1 = dir_out + "/" + to_string(startFrame) + "_" + to_string(endFrame) + "merge.avi";
			string s_out2 = dir_out + "/" + to_string(startFrame) + "_" + to_string(endFrame) + "merge_disp.avi";
			VideoWriter writerMerge = VideoWriter(s_out1, fourcc, fps/2, Size(1600, 1200), 1);
			VideoWriter writerMergeDisp = VideoWriter(s_out2, fourcc, fps/2, Size(960, 720), 1);
		}
		if (hdrImaging) {
			string s_out3 = dir_out + "/" + to_string(startFrame) + "_" + to_string(endFrame) + "hdr.avi";
			string s_out4 = dir_out + "/" + to_string(startFrame) + "_" + to_string(endFrame) + "hdr_disp.avi";
			VideoWriter writerHdr = VideoWriter(s_out3, fourcc, fps/2, Size(1600, 1200), 1);
			VideoWriter writerHdrDisp = VideoWriter(s_out4, fourcc, fps/2, Size(960, 720), 1);
		}


		int counter = 1;
		int count = 0, counterFractions = 0, startingFrame = -1;
		int frames[2];
		int bright_dark_diff = 5;
		bool ready = false;

		while (vid.isOpened()) {
			vid >> frame;
			if (count == startingFrame) {
				ready = true;
				std::cout << "starting frame: " << count << endl;
			}
			printf("%d\n", counter);
			if (mode == 1) {

				if (merging) {
					mergeM(img1, img2, merge);
					writerMerge.write(merge);
					rawToDisp(merge, mDispCol);
					writerMergeDisp.write(mDispCol);

				}
				if (hdrImaging) {
					hdrR(img1, img2, diff, hdr);
					writerHdr.write(hdr);
					rawToDisp(hdr, hDispCol);
					writerHdrDisp.write(hDispCol);

				}
				counter++;
			}
			else if (vidStart >= 0 && vidStart*fps < numFrames) {
				int startFrame = vidStart*fps;

			}
			else {
				int startFrame = numFrames / 2;
			}
		}
	}
	
	else {
		string s_out = dir_out + "/" + to_string(startFrame) + "_" + to_string(endFrame) + "_disp.avi";
		VideoWriter writerDisp = VideoWriter(s_out, fourcc, fps, Size(1600, 1200), 1);
	
		while (vid.isOpened()) {
			vid >> frame;
			rawToDisp(frame, dispCol);
			writerDisp.write(dispCol);
		}
	//}*/
	return 0;
}

void mergeNhdrNdispsArray(cv::VideoCapture vid, string dir_out, int diff, vector<int> secs) {
	cout << "started MERGE and HDR..." << endl;

	Mat merge, mDispCol, mDispMap;
	Mat hdr, hDispCol, hDispMap;
	Mat frame, left1, left2;
	Mat result_merge, result_hdr;
	int counter = 0;
	int count = 0, countFractions = 0, startingFrame = -1;
	vector<Mat> frames;
	int bright_dark_diff = 10;
	bool ready = false;
	bool save = false;
	Scalar a, b, c;
	Mat img1, img2;
	int i = 0;
	int s = secs.size();
	int startFrame = secs[0] * 22;
	int endFrame = secs[s - 1] * 22;
	std::cout << "Begin: " << secs[0] << ", End: " << secs[s - 1] << endl;
	while (vid.isOpened()) {
		vid >> frame;
		if (counter % 22 == 0) {
			cout << counter / 22 << endl;
		}
		if (counter >= secs[i] * 22) {
			string s_out0 = dir_out + "_" + to_string(secs[i]) + "merge_d.jpg";
			string s_out1 = dir_out + "_" + to_string(secs[i]) + "merge.jpg";
			string s_out2 = dir_out + "_" + to_string(secs[i]) + "merge_disp.jpg";
			string s_out3 = dir_out + "_" + to_string(secs[i]) + "hdr.jpg";
			string s_out4 = dir_out + "_" + to_string(secs[i]) + "hdr_disp.jpg";
			string s_out5 = dir_out + "_" + to_string(secs[i]) + "hdr_d.jpg";


			
			if (count == startingFrame) {
				ready = true;
				std::cout << "starting frame: " << count << endl;
			}

			if (ready) {
				if (countFractions == 0) {
					a = mean(frame);
					//imwrite("dark1.jpg", frame);
					//cout << "a: " << a[0] << endl;
					if ((a[0] + bright_dark_diff) < b[0]) {
						frames.push_back(frame.clone());
						countFractions++;
						imwrite("dark.jpg", frame);
					}
					else {
						cout << "found two BRIGHT frames in a row" << endl;
					}
				}
				else if (countFractions == 1) {
					b = mean(frame);
					//imwrite("bright1.jpg", frame);
					//cout << "b: " << b[0] << endl;
					if ((a[0] + bright_dark_diff) < b[0]) {
						frames.push_back(frame);
						countFractions++;
						imwrite("bright.jpg", frame);
					}
					else {
						cout << "found two dark frames in a row" << endl;
					}
				}
				if (countFractions == 2) {
					cout << "processing: " << secs[i] << endl;
					mergeM(frames, result_merge);
					cout << "finish merging" << endl;
					getLeft(result_merge, left1);
					imwrite(s_out1, left1);
					hdrR(frames, diff, result_hdr);
					getLeft(result_hdr, left2);
					imwrite(s_out3, left2);
					cout << "finish hdr" << endl;
					//cout << "Image Size: " << result.size() << endl;
					cout << "dispMap..." << endl;
					rawToDisp(result_merge, mDispMap, mDispCol);
					imwrite(s_out2, mDispCol);
					imwrite(s_out0, mDispMap);
					rawToDisp(result_hdr, hDispMap, hDispCol);
					imwrite(s_out4, hDispCol);
					imwrite(s_out5, hDispMap);
					cout << "written images successfully" << endl;
					i++;
					countFractions = 0;
					frames = {};
				}
			}
			else if (count == 0) {
				a = mean(frame);
			}
			else if (count == 1) {
				b = mean(frame);
				if ((a[0] + bright_dark_diff) < b[0]) {
					startingFrame = 2;
				}
				else {
					b = a;
					if ((b[0] + bright_dark_diff) < a[0]) {
						startingFrame = 3;
					}
				}
			}
			else if (count == 2) {
				c = mean(frame);
				if ((c[0] + bright_dark_diff) < b[0]) {
					a = c;
					startingFrame = 4;
				}
				else {
					b = c;
					startingFrame = 3;
				}
								}
				count++;

			}
			counter++;
			if (i > (s - 1)) {
				vid.release();
				break;
			
		}
	}
}
void normToDispArray(cv::VideoCapture vid, string dir_out, vector<int> secs) {
	Mat frame, left, dispCol, dispMap;
	int counter = 0;
	int i = 0;
	int s = secs.size();
	int startFrame = secs[0] * 22;
	int endFrame = secs[(s - 1)] * 22;
	std::cout << "Begin: " << secs[0] << ", End: " << secs[(s-1)] << endl;
	while (vid.isOpened()) {
		vid >> frame;
		if (counter % 22 == 0) {
			std::cout << counter / 22 << endl;
		}
		if (counter == (secs[i] * 22)) {
			string s_out0 = dir_out + "_" + to_string(secs[i]) + "_d.jpg";
			string s_out1 = dir_out + "_" + to_string(secs[i]) + ".jpg";
			string s_out2 = dir_out + "_" + to_string(secs[i]) + "_disp.jpg";

			std::cout << "processing: " << secs[i] << endl;
			rawToDisp(frame, dispMap, dispCol);
			imwrite(s_out2, dispCol);
			getLeft(frame, left);
			imwrite(s_out1, left);
			imwrite(s_out0, dispMap);
			i++;
		}
		counter++;
		if (counter > endFrame) {
			vid.release();
			break;
		}
	}

}
int VidToHdrDispArray(string name, bool merging, bool hdrImaging, vector<int> secs) {
	//string dir_out = "C:\\Users\Dominik\Documents\Visual Studio 2015\Projects\DispMap_Processing\DispMap_Processing\result";
	string dir_out = "result/" + name;
	int fps = 22;
	int diff = 4;  //diff for hdr images
	//int arrayLength = secs.size;

	string s_in = name + ".mp4";
	//string dir_out = dir_o + "/";
	Mat frame, dispCol;
	Mat merge, mDispCol;
	Mat hdr, hDispCol;

	VideoCapture vid(s_in);
	int numFrames = (int)vid.get(CAP_PROP_FRAME_COUNT);
	printf("Number of Frames: %d\n", numFrames);

	int fourcc = CV_FOURCC('D', 'I', 'V', '3');
	

	int mode;
	bool makeVid = false;

	
	
	if (merging && hdrImaging) {
		mergeNhdrNdispsArray(vid, dir_out, diff, secs);
	}
	
	else if (!merging && !hdrImaging) {
		normToDispArray(vid, dir_out, secs);
	}
	else {
		cout << "only merge and hdr or nothing" << endl;
	}
	
	return 0;
}


int ImToDisp(string name) {
	Mat dispMap, dispCol;
	string s = name + ".jpg";
	Mat in = imread(s);

	rawToDisp(in, dispMap, dispCol);
	string s1 = name + "_disp.jpg";
	string s2 = name + "_dispCol.jpg";

	imwrite(s1, dispMap);
	imwrite(s2, dispCol);
	return 0;
}

int ImToHDRDisp(string nameDark, string nameBright, int diff) {
	Mat dispMap, dispCol, in;
	string s1 = nameDark + ".jpg";
	string s2 = nameBright + ".jpg";

	Mat inD = imread(s1);
	Mat inB = imread(s2);

	vector<Mat> frames;
	frames.push_back(inD);
	frames.push_back(inB);
	hdrR(frames, diff, in);

	rawToDisp(in, dispMap, dispCol);
	string s3 = nameDark + "_disp.jpg";
	string s4 = nameDark + "_dispCol.jpg";

	imwrite(s1, dispMap);
	imwrite(s2, dispCol);
	return 0;
}

vector<int> everySec(int from, int to) {
	vector<int> sec;
	for (int i = from; i <= to; i++) {
		sec.push_back(i);
	}
	return sec;
}


int main(int argc, char** argv) {

	Mat frame, disp, dispCol;
	//vector<int> secs = { 8, 23, 37, 48, 57, 69, 81, 95, 106, 114, 118 };
	//ImToDisp("gain_0");
	//ImToDisp("gain_a");
	//ImToDisp("gain_d");
	
	//vector<int> in02_16x_sec = everySec(28, 68);
	VidToHdrDisp("out03_4x", 1, 1, 19, 24);
	/*
	VidToHdrDispArray("out02_1x_2", 0, 0, out02_1x_2_sec);
	VidToHdrDispArray("out02_4x", 1, 1, out02_4x_sec);  // again without _2_sec!
	VidToHdrDispArray("out02_4x_2", 1, 1, out02_4x_2_sec);
	VidToHdrDispArray("out02_8x", 1, 1, out02_8x_sec);
	VidToHdrDispArray("out02_8x_2", 1, 1, out02_8x_2_sec);
	VidToHdrDispArray("out02_16x", 1, 1, out02_16x_sec);
	VidToHdrDispArray("out02_16x_2", 1, 1, out02_16x_2_sec);

	VidToHdrDispArray("in02_1x", 0, 0, in02_1x_sec);
	VidToHdrDispArray("in02_4x", 1, 1, in02_4x_sec);
	VidToHdrDispArray("in02_8x", 1, 1, in02_8x_sec);
	VidToHdrDispArray("in02_16x", 1, 1, in02_16x_sec);
	*/
	
	//VidToHdrDisp("gain01", 1, 1, 3, 6);
	//VidToHdrDisp("out03_4x", 1, 1, 20, 63);
	//VidToHdrDisp("8xTest", 1, 1, 6, 6);
	//VidToHdrDisp("out03_16x", 1, 1, 19, 77);

	//VidToHdrDisp("in03_1x", 0, 0, 10, 54);
	//VidToHdrDisp("in03_4x", 1, 1, 10, 56);
	//VidToHdrDisp("in03_8x", 1, 1, 19, 61);
	//VidToHdrDisp("in03_16x", 1, 1, 29, 61);


	//VidToHdrDisp("out02_1x", 0, 0, 2, 19);
	//VidToHdrDisp("out02_4x", 1, 1, 21, 38);
	//VidToHdrDisp("out02_8x", 1, 1, 27, 44);
	//VidToHdrDisp("out02_16x", 1, 1, 30, 47);
	
	//VidToHdrDisp("distanceMeasure", 0, 0, 0, 1);
	//ImToDisp("distanceLast");
	
	/*
	Mat image = imread("m04.jpg");
	
	rawToDisp(image, K1, K2, D1, D2, R1, R2, P1, P2, disp, dispCol);
	imwrite("Disp_m04.png", disp);
	imwrite("DispCol_m04.png", dispCol);
	*/

	/*
	
	VideoCapture vid("default_A4.mp4");
	int numFrames = (int)vid.get(CAP_PROP_FRAME_COUNT);
	printf("Number of Frames: %d\n", numFrames);
	//Size imSize(vid.get(CV_CAP_PROP_FRAME_WIDTH), vid.get(CV_CAP_PROP_FRAME_HEIGHT));
	//printf("imsize: %d x %d\n", vid.get(CV_CAP_PROP_FRAME_WIDTH), vid.get(CV_CAP_PROP_FRAME_HEIGHT));

	int fourcc = CV_FOURCC('D', 'I', 'V', '3');
	VideoWriter writerCol = VideoWriter("default_A4d_a4.avi", fourcc, 10, Size(960, 720), 1);
	//VideoWriter writerBack = VideoWriter("back2.avi", fourcc, 10, Size(960, 720), 0);
	//VideoWriter writerSum = VideoWriter("sum2.avi", fourcc, 22, Size(960, 720), 0);
	//VideoWriter writerSumCol = VideoWriter("sumCol2.avi", fourcc, 10, Size(960, 720), 1);
	//VideoWriter writerSumInv = VideoWriter("sumInv2.avi", fourcc, 22, Size(960, 720), 0);
	//VideoWriter writerSumInvCol = VideoWriter("sumInvCol2.avi", fourcc, 10, Size(960, 720), 1);

	int counter = 1;

	while (vid.isOpened()) {
		vid >> frame;
		
		if (frame.empty()) {
			cout << "end" << endl;
			vid.release();
			//writerBack.release();
			writerCol.release();
			//writerSum.release();
			//writerSumCol.release();
			//writerSumInv.release();
			//writerSumInvCol.release();
			return 0;
			break;
		}
		printf("%d\n", counter);
		rawToDisp(frame, dispCol);
		imwrite("dispCol.jpg", dispCol);
		writerCol.write(dispCol);
		
		//imwrite("front1.jpg", dispFront);
		//imwrite("back1.jpg", dispBack);
		//resize(dispFront, dispFront, Size(sw, sh * 3));
		//resize(dispBack, dispBack, Size(sw, sh * 3));
		//printf("rows/columns: %d/%d\nwidth/height: %d/%d\n", r, c, sw, sh);
		
		//printf("Back\n");
		//writerBack.write(dispBack);
		//printf("written front and back\n");

		//Mat sum, sumInv, sumCol, sumInvCol, front, back;
		//sum = dispFront + dispBack;
		
		//int f = sum.channels();
		//printf("writing sum and colored sum...\n");
		//imwrite("sum1.jpg", sum);
		//imwrite("somCol1.jpg", sumCol);
		//writerSum.write(sum);
		//writerSumCol.write(sumCol);

		//front = dispFront / 2;
		//bitwise_not(dispBack, back);
		//back = back / 2;
		//printf("writing the rest...\n");
		//sumInv = front + back;
		//applyColorMap(sumInv, sumInvCol, COLORMAP_JET);
		//imwrite("sumInv1.jpg", sumInv);
		//imwrite("sumInvCol1.jpg", sumInvCol);
		//writerSumInv.write(sumInv);
		//writerSumInvCol.write(sumInvCol);
		
		counter++;
		

	}

	vid.release();
	//writerBack.release();
	writerCol.release();
	//writerSum.release();
	//writerSumCol.release();
	//writerSumInv.release();
	//writerSumInvCol.release();
	*/
	/*
	Mat vorne, vornee, hinten, hintene, gesamt;

	vorne = imread("vorne3.jpg");
	hinten = imread("hinten3.jpg");

	vornee = (vorne / 2);
	bitwise_not(hinten, hintene);

	hintene = hintene / 2;

	imwrite("vornee3.jpg", vornee);
	imwrite("hintene3.jpg", hintene);

	gesamt = vornee + hintene;

	imwrite("gesamt4.jpg", gesamt);

	Mat scaledDisparityMap, cm_disp, gesamtColored;
	//gesamtColored = hinten - vorne;
	//convertScaleAbs(gesamtColored, scaledDisparityMap, 1);
	applyColorMap(gesamt, cm_disp, COLORMAP_JET);
	imwrite("gesamt4_colored.jpg", cm_disp);
	*/

	

	cout << "finish" << endl;
	return 0;
}