package templatematching;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.equalizeHist;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

public class TemplateMatching{
static String face_cascade_name = "haarcascade_frontalface_alt.xml";
static String eyes_cascade_name = "haarcascade_eye_tree_eyeglasses.xml";
//Declaration of Haar Classifiers

//Get Screen dimensions
static void GetDesktopResolution(int horizontal, int vertical)
{
    Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
horizontal = d.width;
vertical = d.height;
}
public static Image ProcessFrame()
{
System.loadLibrary(Core.NATIVE_LIBRARY_NAME);  
int horizontal=0;
int vertical=0;
int Pupilx;
int Pupily;
int scaled_deltax;
int scaled_deltay;
int ROIwidth;
int ROIheight;
int deltax;
int deltay;
int k=0;
Point Pupil;
double scaledPupilx;
double scaledPupily;
Mat frame=new Mat();
//Define a white image to observe changes of coordinates on it
 CascadeClassifier faceHaar=new CascadeClassifier(face_cascade_name);
 CascadeClassifier eyesHaar=new CascadeClassifier(eyes_cascade_name);
GetDesktopResolution(horizontal, vertical);
    System.out.println(" The dimensions of the screen are  "+horizontal+"   "+vertical);
//Check if the face classifier is loaded
//Take the input from the camera
VideoCapture capture=new VideoCapture(0);
if( capture.grab() )
{
while( true )
{
while(k<2)
{
  //capture=new VideoCapture(0);
//treat each frame of the capture individually
capture.retrieve(frame);
       
if( !frame.empty() )
{
MatOfRect faces = new MatOfRect();// = new MatOfRect();
Mat frame_gray = new Mat();
Mat ImageROI = new Mat();
//change the frame to gray-scale
 Imgproc.cvtColor(frame, frame_gray, Imgproc.COLOR_BGR2GRAY);
//use histogram equilization
equalizeHist( frame_gray, frame_gray );
//use the face classifie

faceHaar.detectMultiScale(frame_gray,faces, 1.1, 2,2, new Size(frame.width(),frame.height()),new Size(30,30));

Rect[] faces_rect=faces.toArray();
System.out.println("Detecting face"+faces_rect.length);
for( int i = 0; i < faces_rect.length; i++ )
{
Point center=new Point( faces_rect[i].x + faces_rect[i].width*0.5, faces_rect[i].y + faces_rect[i].height*0.5 );
Imgproc.ellipse( frame, center, new Size( faces_rect[i].width*0.5, faces_rect[i].height*0.5), 0, 0, 360,new Scalar( 0, 0, 255 ), 4, 8, 0 );

Mat faceROI = frame_gray.submat(faces_rect[i] );
MatOfRect eyes = new MatOfRect();
// for the detected face use the eye classifier
 eyesHaar.detectMultiScale(faceROI, eyes, 1.15, 2,Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_SCALE_IMAGE, new Size(30,30),new Size());
Rect[] eyes_rect=eyes.toArray();
    System.out.println("Detecting eyes"+eyes_rect.length);
for( int j = 0; j < eyes_rect.length; j++ )
{
Mat eyeROI = frame_gray.submat(eyes_rect[j]);
Point center1=new Point( faces_rect[i].x + eyes_rect[j].x + eyes_rect[j].width*0.5, faces_rect[i].y + eyes_rect[j].y+ eyes_rect[j].height*0.5 );
int radius = (int)Math.round((eyes_rect[j].width + eyes_rect[j].height)*0.005 );
Imgproc.circle(frame, center1, radius,new Scalar( 255, 0, 0 ), 4, 8, 0 );
Pupilx = (int) center1.x;
Pupily = (int) center1.y;
ROIwidth=eyes_rect[j].width;
ROIheight=eyes_rect[j].height;
Point centerPt[];
    centerPt = new Point[2];
centerPt[k]=center1;
//performing the scaling of the coordintaes to fir to the screen dimensions
if(k==0)
{
    System.out.println("k==0");
    k++;
scaledPupilx=Pupilx;
scaledPupily=Pupily;
}
else
{
deltax= (int) Math.abs((centerPt[k].x-centerPt[k-1].x));
deltay= (int) Math.abs((centerPt[k].y-centerPt[k-1].y));
scaled_deltax=(deltax*(65535/ROIwidth));
scaled_deltay=(deltay*(65535/ROIheight));
scaledPupilx=centerPt[k-1].x+scaled_deltax;
scaledPupily=centerPt[k-1].y+scaled_deltay;
    System.out.println( Pupilx+"    "+Pupilx);
}
if(k==2)
k=0;
//set the cursor position to the scaled coordinates
 Robot robot;
           try {
               robot = new Robot();
             
               robot.mouseMove((int)(1366-scaledPupilx),(int)(768-scaledPupily));
           } catch (Exception ex) {
             
           }

}
////define a window for displaying the frame
//namedWindow("window_name");
////show the frame with the detected face and eyes on the defined window above
//Imgcodecs.imshow("window_name",frame);
//waitKey(10);
}
}
}
}
}
else
{
    System.out.println("No captured frame!!");
}
 MatOfByte mem=new MatOfByte();
Image im = null;
Imgcodecs.imencode(".bmp", frame, mem);
        try {
            im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
           
        } catch (IOException ex) {
           ex.printStackTrace();
        }
        return im;
}
}