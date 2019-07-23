
#From digit_recognition_tf.py
import tensorflow as tf
from tensorflow import keras
import numpy as np
import cv2
import imutils
#From handwriting_input_test.py
from kivy.app import App
from kivy.uix.widget import Widget
from kivy.graphics import Line, Color, Rectangle
from kivy.lang import Builder
import os.path
from kivy.uix.button import Button
from kivy.core.window import Window
#New
from kivy.uix.gridlayout import GridLayout
from kivy.uix.label import Label
from kivy.uix.screenmanager import ScreenManager, Screen, FadeTransition
from kivy.uix.floatlayout import FloatLayout
from kivy.uix.image import Image
from kivy.cache import Cache
from kivy.properties import StringProperty, NumericProperty



from sklearn.metrics import mean_squared_error

import time

def loadModel(model_string):
    new_model = keras.models.load_model(model_string)
    return new_model
def sizeNorm(im):

    active_px = np.argwhere(im!=0)
    active_px = active_px[:,[1,0]]
    x,y,w,h = cv2.boundingRect(active_px)
    im1 = im[y:y+h,x:x+w]
    
    im1_y, im1_x = im1.shape
    if(im1_x > im1_y):
        im1 = imutils.resize(im1, width=20)
    else:
        im1 = imutils.resize(im1, height=20)

    im_x = im.shape[0]
    im_y = im.shape[1]
    
    return im1
def editImage(image):
    #im = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        
    im = sizeNorm(image)
    im_new = np.zeros((28,28))
    ret,thresh = cv2.threshold(im,127,255,0)
 
    # calculate moments of binary imag
    M = cv2.moments(thresh)
 
    # calculate x,y coordinate of center
    cX = int(M["m10"] / M["m00"])
    cY = int(M["m01"] / M["m00"])
    r=im.shape[0]
    c=im.shape[1]
    print("cX:",cX, c-14, "cY:",cY, r-14)


    #Center the image in the frame 28x28
    #Considering the cases that the center is far off (expl. cX = 3, length of image = 20 -> center of 28x28 = 13 -> remaining pixels to image border: 13 vs 20-3 = 17 -> out of bound!!)
    #
    #Trying to center new image ( / / / /) to frame (# # # ) with matching both cendroits
    # # # # # # # # # # #
    #                   #
    #                   #
    #       / O   /    /   /
    #                   #
    #                   #
    # # # # # # # # # # #
    
    #Solution to just cut the new image to fit the frame
    
    if(cY>13):
        if(cX>13):
            im_new[0:(13-cY+r),0:(13-cX+c)] = im[cY-13:27,cX-13:27]
        elif(cX<c-14):
            im_new[0:(13-cY+r),(13-cX):27] = im[cY-13:27,0:27-13+cX]
        else:
            im_new[0:(13-cY+r),(13-cX):(13-cX+c)] = im[cY-13:27,:]
    elif(cY<r-14):
        if(cX>13):
            im_new[(13-cY):27,0:(13-cX+c)] = im[0:27-13+cY,cX-13:27]
        elif(cX<c-14):
            im_new[(13-cY):27,(13-cX):27] = im[0:27-13+cY,0:27-13+cX]
        else:
            im_new[(13-cY):27,(13-cX):(13-cX+c)] = im[0:27-13+cY,:]
    else:
        if(cX>13):
            im_new[(13-cY):(13-cY+r),0:(13-cX+c)] = im[:,cX-13:27]
        elif(cX<c-14):
            im_new[(13-cY):(13-cY+r),(13-cX):27] = im[:,0:27-13+cX]
        else:
            im_new[(13-cY):(13-cY+r),(13-cX):(13-cX+c)] = im
    
    return im_new

def testModel(im, model):   
    arr = []
    im = editImage(im)
    im = im/255
    arr.append(im)
    im_array = np.array(arr)
    result = model.predict(im_array)
    pred = np.argmax(result, axis=1)[0]
    
    conf = result[0,pred]  
    print("Predicted digit: ",pred, " with confidence: "+"{:.2%} ".format(conf))
    if(pred == 10):
        pred = "!"   
    return pred, conf

def testModel_c(im, model):   
    arr = []
    im = editImage(im)
    im = im/255
    im = im.reshape(28,28,1)
    arr.append(im)
    im_array = np.array(arr)
    result = model.predict(im_array)
    pred = np.argmax(result, axis=1)[0]
    conf = result[0,pred]  
    print("Predicted digit: ",pred, " with confidence: "+"{:.2%} ".format(conf))
    if(pred == 10):
        pred = "!"    
    return pred, conf

def testAnomaly(im, model):
    im = editImage(im)
    #im = im/255
    im1 = im.reshape(1,784)/255
    im_r = model.predict(im1)
    im_r[im_r>0.5] = 1
    im_r[im_r<0.5] = 0
    #err = np.mean(np.absolute(im1-im_r))#
    err = mean_squared_error(im1, im_r)
    #im_r = im_r.reshape(28,28)
    print(err)
    return im_r, err

class DrawInput(FloatLayout):
   # touched = 0
    def on_touch_down(self, touch): 
        #touched = 0
        touch.grab(self)
        with self.canvas:
            touch.ud["line"] = Line(points=(touch.x, touch.y), width=8)
            
    def on_touch_move(self, touch):
        if (touch.profile == []):
            pass
        elif touch.grab_current is self:
            touch.ud["line"].points += (touch.x, touch.y)            
        else:
            pass
            
    def on_touch_up(self, touch):
        #touched =1
        if touch.grab_current is self:
            touch.ungrab(self)
        else:
            pass
    def save(self):
        #print(touched)
        if(self.canvas.children==[]):
            print("Nothing drawn!")
        else:
            self.export_to_png("C:/Users/dominik.urbaniak/Documents/digit-demo-anaconda/digit.png")
        #on_press: painter.export_to_png("C:/Users/dominik.urbaniak/Documents/digit-demo-anaconda/digit.png")
    #def clearing(self):
        #painter.canvas.clear()
     #   if(self.canvas.children==[]):
      #      print("yeahi")
       # else: print("noo")
        #print(self.canvas.children)
        #self.export_to_png("C:/Users/dominik.urbaniak/Documents/digit-demo-anaconda/diii.png")
        
        #im = cv2.imread("C:/Users/dominik.urbaniak/Documents/digit-demo-anaconda/diii.png",0)
        #print(len(np.nonzero(im)[0]))
       # self.canvas.clear()
       
class ContentScreen(Screen):
    pass
class PMScreen(Screen):
    sli = NumericProperty(3)
    
class AnomalyScreen(Screen):
    an = NumericProperty(1)
class DigitGenScreen(Screen):
    gen_im = NumericProperty(50)
    thi = NumericProperty(4)
    til = NumericProperty(4)
class MainScreen(Screen):
    def __init__(self, **kwargs):
        super(MainScreen, self).__init__(**kwargs)
        
    #def on_enter(self):
        #image = cv2.imread("C:/Users/dominik.urbaniak/Documents/digit-demo-anaconda/digit.png",0)
        #if(len(np.nonzero(image)==0)):
            
        #model1 = loadModel('C:/Users/dominik.urbaniak/Documents/digit-demo-anaconda/mnist_AE1.h5')
        
        #p1, er = testAnomaly(image, model1)
        #im_r = p1.reshape(28,28)*255
        #im_r = cv2.resize(im_r, (312,312), interpolation = cv2.INTER_LINEAR)
        #cv2.imwrite('im_rec.png',im_r)
class AnotherScreen(Screen):
    pass
   # Window.fullscreen = True
    
class ResultScreen(Screen):
    pred1 = StringProperty()
    conf1 = StringProperty()
    pred2 = StringProperty()
    conf2 = StringProperty()
    im_s = StringProperty()
    def __init__(self, **kwargs):
        super(ResultScreen, self).__init__(**kwargs)
        self.pred1, self.conf1, self.pred2, self.conf2 = '...', '...', '...', '...'
    def on_enter(self):
        print('evaluate image, if existent')
        model1 = loadModel('C:/Users/dominik.urbaniak/Documents/digit-demo-anaconda/mnist_AE1.h5')
        model2 = loadModel('C:/Users/dominik.urbaniak/Documents/digit-demo-anaconda/3k_model_c1.h5')
        image = cv2.imread("C:/Users/dominik.urbaniak/Documents/digit-demo-anaconda/digit.png",0)
        #im = editImage(image)
        #print(image.shape)
        #p1, c1 = testModel_c(image, model1)
        p1, err = testAnomaly(image, model1)
        im_r = p1.reshape(28,28)*255
        im_r = cv2.resize(im_r, (312,312), interpolation = cv2.INTER_LINEAR)
        cv2.imwrite('im_rec.png',im_r)
        self.im_s = 'im_rec.png'
        #c1 = round(c1*100, 2)
        #print(err)
        if(err > 0.11):
            c1 = 'No digit'
        else:
            c1 = 'Real digit'
        self.pred1 = str(err*10)
        self.conf1 = str(c1)    
        p2, c2 = testModel_c(image, model2)
        c2 = round(c2*100, 2)
        self.pred2 = str(p2)
        self.conf2 = str(c2)
class ScreenManagement(ScreenManager):
    def new_screen(self):
        Cache.remove('kv.image')
        Cache.remove('kv.texture')
        im = cv2.imread("C:/Users/dominik.urbaniak/Documents/digit-demo-anaconda/digit.png",0)
        #im = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        active_px = np.argwhere(im!=0)
        active_px = active_px[:,[1,0]]
        x,y,w,h = cv2.boundingRect(active_px)
        image = im[y:y+h, x:x+w]
        cv2.rectangle(im,(x-2,y-2),(x+w+2,y+h+2),(255,0,0),1)
        im = im[y-5:y+h+5, x-5:x+w+5]
        #image = cv2.resize(image, (120,120))
        cv2.imwrite("C:/Users/dominik.urbaniak/Documents/digit-demo-anaconda/digit1.png", im)
        cv2.imwrite("C:/Users/dominik.urbaniak/Documents/digit-demo-anaconda/digit_show.png", image)
        #im28 = image.resize(28,28)
        #cv2.imwrite("C:/Users/dominik.urbaniak/Documents/digit-demo-anaconda/digit28.png", im28)
        name = str(time.time())
        s = MainScreen(name=name)
        self.add_widget(s)
        self.current = name
        #print("new screen: "+ name)
    def new_result_screen(self):
        Cache.remove('kv.image')
        Cache.remove('kv.texture')
        name = str(time.time())
        s = ResultScreen(name=name)
        self.add_widget(s)
        self.current = name
    def create_training_data(self):
        Cache.remove('kv.image')
        Cache.remove('kv.texture')
        image = cv2.imread("C:/Users/dominik.urbaniak/Documents/digit-demo-anaconda/training.png")
        print(image.shape)
        image = editImage(image)
        image = image*255
        s = str(time.time())+".png"
        cv2.imwrite("C:/Users/dominik.urbaniak/Documents/digit-demo-anaconda/test/"+s, image)
        #cv2.imwrite("C:/Users/dominik.urbaniak/Documents/digit-demo-anaconda/digit_1.png", image)
        #print('printed to test/')
        #cv2.imwrite("test/"+s, image)


presentation = Builder.load_file("demo_anomaly.kv")
        
class DemoApp(App):
   def build(self):
       return presentation
      
if __name__ == "__main__":
    DemoApp().run()