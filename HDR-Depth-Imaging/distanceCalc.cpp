#include <opencv2\opencv.hpp>
#include <Windows.h>

using namespace cv;
using namespace std;

void drawCircle(Mat in, int x, int y, int fac, Mat& out) {
	Mat in_new = in*fac;
	Mat cM1, cM2, cM3;
	Mat temp1 = in_new.clone();
	Mat temp2 = in_new.clone();
	Mat temp3 = in_new.clone();
	applyColorMap(temp1, cM1, COLORMAP_HSV);
	applyColorMap(temp2, cM2, COLORMAP_JET);
	applyColorMap(temp3, cM3, COLORMAP_COOL);
	circle(cM1, Point(x, y), 2, (0, 255, 0), 2, 8);
	circle(cM1, Point(x, y), 6, (255, 0, 255), 2, 8);
	circle(cM2, Point(x, y), 2, (0, 255, 0), 2, 8);
	circle(cM2, Point(x, y), 6, (255, 0, 255), 2, 8);
	circle(cM3, Point(x, y), 2, (0, 255, 0), 2, 8);
	circle(cM3, Point(x, y), 6, (255, 0, 255), 2, 8);
	imshow("HSV", cM1);
	imshow("JET", cM2);
	//imshow("COOL", cM3);
	out = cM2;
}

void printIntensity(Mat in, int x, int y) {
	Scalar intensity = in.at<uchar>(Point(x, y));
	//in.at<uchar>(y, 3*x) = 255;
	//cout << "Intensity at (" << x << "," << y << "): " << intensity << endl;
}

void printDistance(Mat in, int x, int y) {
	Point3f p = in.at<Point3f>(y, x);
	cout << "Distance at (" << x << "," << y << "): " << -2*(p.z) <<" meter"<< endl;
}

int main(int argc, char** argv) {

	Mat dispMap, dM;
	Mat dM1, dM2, dM3;
	Mat cM1, cM2, cM3;
	Mat Q;
	int fac = 2;
	string dir = "";
	string name = "00092.jpg";

	int p1 = 0;
	int p2 = 0;
	//Scalar intensity;
	cv::FileStorage fs2("out_file_28_01", cv::FileStorage::READ);
	fs2["Q"] >> Q;	
	fs2.release();

	string inStr = dir + name;
	dispMap = imread("dist22_disp.jpg");
	dM = imread(inStr, IMREAD_GRAYSCALE);
	dM1 = (dM * fac);
	applyColorMap(dM1, cM1, COLORMAP_HSV);
	applyColorMap(dM1, cM2, COLORMAP_JET);
	applyColorMap(dM1, cM3, COLORMAP_COOL);

	//cout << dM.type() << endl;
	//dM.convertTo(dM, CV_32SC1);
	//cout << dM.type() << endl << Q << endl;
	
	//resize(dispMap, dispMap, Size(640, 480));
	//resize(dM, dM, Size(640, 480));
	Mat img3D(dM.size(), CV_32FC3);
	//reprojectImageTo3D(disp8_SGBM, XYZ, Q, false, CV_32F);
	reprojectImageTo3D(dM, img3D, Q, true, CV_32F);
	

	//imwrite("img3d.png", img3D);

	namedWindow("HSV", CV_WINDOW_FREERATIO);
	namedWindow("JET", CV_WINDOW_FREERATIO);
	//namedWindow("COOL", CV_WINDOW_FREERATIO);

	

	imshow("HSV", cM1);
	imshow("JET", cM2);
	//imshow("COOL", cM3);
	

	Size size = dispMap.size();
	Size s = dM.size();
	int w = size.width;
	int h = size.height;
	int w1 = s.width;
	int h1 = s.height;
	//cv::FileStorage fs1("dispMap.txt", cv::FileStorage::WRITE);
	//fs1 << "dispMap" << dM;

	cout << size << ", " << s << endl;

	while (1) {

		char c = waitKey(50);

		if (c == 27) {
			destroyAllWindows();
			return 0;
			break;
		}

		if (c == 'j') {
			if (p1 > 5) {
				p1-=6;
				drawCircle(dM, p1, p2, fac, cM2);
				printIntensity(dM, p1, p2);
				printDistance(img3D, p1, p2);
			}
		}
		if (c == 'k') {
			if (p2 < h-5) {
				p2+=6;
				drawCircle(dM, p1, p2, fac, cM2);
				printIntensity(dM, p1, p2);
				printDistance(img3D, p1, p2);
			}
		}
		if (c == 'l') {
			if (p1 < w-5) {
				p1+=6;
				drawCircle(dM, p1, p2, fac, cM2);
				printIntensity(dM, p1, p2);
				printDistance(img3D, p1, p2);
			}
		}
		if (c == 'i') {
			if (p2 > 5) {
				p2-=6;
				drawCircle(dM, p1, p2, fac, cM2);
				printIntensity(dM, p1, p2);
				printDistance(img3D, p1, p2);
			}
		}
	
		if (c == '+') {
			fac++;
			cout << "Factor: " << fac << endl;
		}
		else if (c == '-') {
			if (fac > 1) {
				fac--;
				cout << "Factor: " << fac << endl;
			}
		}
		if (c == ' ') {
			imwrite(dir+"fac"+to_string(fac)+"_"+name, cM2);
			cout << "saved" << endl;
		}
	
	
	}

	return 0;
}