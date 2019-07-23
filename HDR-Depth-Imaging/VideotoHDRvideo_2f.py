import cv2
import numpy as np
import time
import os
import errno
from nt import strerror


def createHDR(images, timeDifference):
    times = np.array([ 1.0, timeDifference ], dtype=np.float32)
    #try:     
    alignMTB = cv2.createAlignMTB()
    alignMTB.process(images, images)
      
    mergeMertens = cv2.createMergeMertens(10, 4, 8)
    hdrs = mergeMertens.process(images)  

    #calibrateDebevec = cv2.createCalibrateDebevec()
    #responseDebevec = calibrateDebevec.process(images, times)
       
    #mergeDebevec = cv2.createMergeDebevec()
    #hdrDebevec = mergeDebevec.process(images, times, responseDebevec)
       
    #tonemapReinhard = cv2.createTonemapReinhard(1.5, 2.0,0,0)
    #ldrReinhard = tonemapReinhard.process(hdrDebevec)
        
    #return ldrReinhard
    return hdrs

#    except IOError as (errno, strerror):
 #       print "I/O error({0}): {1}".format(errno, strerror)
  #      return -2
   # except ValueError:
    #    print "Could not convert data to an integer."
     #   return -3
    #except:
     #   print "Unexpected error"
      #  raise


def video_to_HDRvideo(inputVid_2f, outputName, fps, exposureFactor):
    #try:
     #   os.mkdir(output_loc)
    #except OSError:
     #   pass
    # Log the time
    time_start = time.time()
    # Start capturing the feed
    cap = cv2.VideoCapture(inputVid_2f)
    fourcc = cv2.VideoWriter_fourcc(*'MPEG')
    out = cv2.VideoWriter(outputName+".avi",fourcc, fps, (3200,1200))
    startFrame = -1
    ready = False
    
    # Find the number of frames
    video_length = int(cap.get(cv2.CAP_PROP_FRAME_COUNT)) - 1
    print ("Number of frames: ", video_length)
    count = 0
    countFractions = 0
    frames = []
    brigth_dark_difference = 5
   
    # Start converting the video
    while cap.isOpened():
        # Extract the frame
        ret, frame = cap.read()
        #ready, when frame with shortest exposure time is found!      
        if(count == startFrame):
            ready = True
            #cv2.imwrite("StartFrame.jpg", frame)
            print "startFrame:", count

        if(ready):
                   
            if countFractions == 0:
#** to-do: exception catch if frames cannot be mixed to hdr?! -> take instead one of the three                            
                a = cv2.mean(frame)
                #print "a: ", a[0]
                if (a[0]+brigth_dark_difference) < b[0]:
                    
                    frames.append(frame)
                    countFractions+=1 
                else:
                    print "found two BRIGHT frames in a row, try next..."        
                #create array with 2 frames to be mixed
            elif countFractions == 1:
                b = cv2.mean(frame)
                #print "b: ", b[0]
                #print(a[0], b[0]-10)
                if (a[0]+brigth_dark_difference)<b[0]:
                    frames.append(frame)
                    countFractions+=1
                else:
                    print "found two DARK frames in a row, try next..."
                
                
            if countFractions == 2:
                result = createHDR(frames, exposureFactor)*255
                grey = cv2.cvtColor(result, cv2.COLOR_BGR2GRAY)
                cv2.imwrite("mergeMertens.jpg", result)
                #if result == -2 or result == -3:
                 #   out.write(frame1)
                #else:
                result1 = cv2.imread("mergeMertens.jpg")
                
                #cv2.imwrite("grey-test.jpg",grey)
                #write hdr-image to video
                
                out.write(result1)
                
                #reset counter and array
                countFractions = 0
                frames = []
                print count
                
                            
            # If there are no more frames left
            if (count > (video_length-1)):
                # Log the time again
                time_end = time.time()
                # Release the feed
                cap.release()
                out.release()
                # Print stats
                print ("Done extracting frames.\n%d frames extracted" % count)
                print ("It took %d seconds forconversion." % (time_end-time_start))
                break
        #analyse first frames to find the one with the shortest exposure time to start the video with (for 3 frames to be mixed)
        elif (count == 0):
            a = cv2.mean(frame)
            print(a)
        elif (count == 1):
            b = cv2.mean(frame)
            if (a[0]+brigth_dark_difference) < b[0]:               
                startFrame = 2
            else:
                b = a
                if (b[0]+brigth_dark_difference) < a[0]:                   
                    startFrame = 3
        #in case video starts with two bright or dark frames in a row
        elif count == 2:
            c = cv2.mean(frame)
            if (c[0]+brigth_dark_difference) < b[0]:
                a = c
                startFrame = 4
            else:   
                b = c 
                startFrame = 3
             
               
        else:
            if(count > 3):
                print("didn't work :(")
                return -1
        #counter for all frames in video
   
        count = count + 1
        #print count



if __name__ == '__main__':
    print("start")
    #execute 
    
    video_to_HDRvideo("h2.avi","h2_h", 11, 4)