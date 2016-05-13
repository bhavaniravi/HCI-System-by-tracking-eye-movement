/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package templatematching;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

/**
 *
 * @author BHAVANI
 */

public class ProcessFrame {

static int k=0;

        int Pupilx;
 int Pupily;
int scaled_deltax;
int scaled_deltay;
int ROIwidth;
int ROIheight;
int deltax;
int deltay;

Point Pupil;
double scaledPupilx;
 double scaledPupily;
 Mat frame = new Mat();
 String face_cascade_name = "haarcascade_frontalface_alt.xml";
String eyes_cascade_name = "haarcascade_eye.xml";
//Declaration of Haar Classifiers
CascadeClassifier faceHaar = new CascadeClassifier(face_cascade_name);
CascadeClassifier eyesHaar=new CascadeClassifier(eyes_cascade_name);

   public  Image processFrame(VideoCapture capture) {

 while(k<2){
      capture.grab(); 
  capture.retrieve(frame);
   capture.retrieve(frame);
 //treat each frame of the capture individually
      // retives the grabbed frame into Mat obj
   int frame_width = frame.rows();
 int frame_height = frame.cols();
MatOfRect faces1 = new MatOfRect();
 Mat frame_gray = new Mat();
 Mat ImageROI;
//change the frame to gray-scale
  Imgproc.cvtColor(frame, frame_gray, Imgproc.COLOR_BGR2GRAY);//gray scale conversion
 //use histogram equilization
 //Imgproc.equalizeHist(frame_gray, frame_gray );
 //use the face classifie
faceHaar.detectMultiScale( frame_gray, faces1, 1.1, 2, 2,new Size(30, 30) ,new Size());
 Rect[] faces = faces1.toArray();
   
for( int i = 0;i<faces.length; i++ ){
    //  System.out.println("Processing faces");
Point center =new Point( faces[i].x + faces[i].width*0.5, faces[i].y + faces[i].height*0.5 );
Imgproc.ellipse( frame, center, new Size( faces[i].width*0.5, faces[i].height*0.5), 0, 0, 360,
new Scalar( 0, 0, 255 ), 4, 8, 0 );
Mat faceROI =frame_gray.submat(faces[i]);
     
MatOfRect eyes1=new MatOfRect();
 eyesHaar.detectMultiScale(faceROI, eyes1, 1.15, 2,2, new Size(30,30),new Size());
//eyesHaar.detectMultiScale(faceROI, eyes1, 1.1, 2,  Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_SCALE_IMAGE, new Size(30,30),new Size());
Rect[] eyes = eyes1.toArray();
//  System.out.println("Processing eyes");
for( int j = 0; j < eyes.length; j++ )
{
  
Mat eyeROI = frame_gray.submat(eyes[i]);
Point center1=new Point ( faces[i].x + eyes[j].x + eyes[j].width*0.5, faces[i].y + eyes[j].y
+ eyes[j].height*0.5 );
int radius = (int)( (eyes[j].width + eyes[j].height)*0.005 );
Imgproc.circle(frame, center1, radius, new Scalar( 255, 0, 0 ), 4, 8, 0 );
Pupilx = (int) center1.x;
Pupily = (int) center1.y;
ROIwidth=eyes[j].width;
ROIheight=eyes[j].height;
Point centerX[]=new Point[2] ;
centerX[k]=center1;
//performing the scaling of the coordintaes to fir to the screen dimensions
if(k==0)
{
 scaledPupilx=Pupilx;
 scaledPupily=Pupily;
 k++;
}
else
{
    System.out.println("In else part");
deltax= (int) Math.abs((centerX[k].x-centerX[k-1].x));
deltay= (int) Math.abs((centerX[k].y-centerX[k-1].y));

scaled_deltax=(deltax*(65535/ROIwidth));

scaled_deltay=(deltay*(65535/ROIheight));
scaledPupilx=centerX[k-1].x+scaled_deltax;

scaledPupily=centerX[k-1].y+scaled_deltay;
}
if(k==2)
k=0;
//set the cursor position to the scaled coordinates
 try {
              Robot robot = new Robot();
           
               robot.mouseMove((int)(1366-scaledPupilx), (int)(768-scaledPupily));
           } catch (AWTException ex) {
             
           }
}
//define a window for displaying the frame

//release window if any key is hit

}

 MatOfByte mem=new MatOfByte();
 Imgcodecs.imencode(".bmp", frame, mem);
 Image im=null;
        try {
            im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
           
        } catch (IOException ex) {
           ex.printStackTrace();
        }
        return im;
}
    return null;

}
   
   
}
    

