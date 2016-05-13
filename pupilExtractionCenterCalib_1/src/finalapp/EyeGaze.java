package finalapp;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

public class EyeGaze {
  

    private Mat                   mRgba;
    private Mat                   mGray;
    private Mat 		mZoomCorner;
    private Mat                 mZoomWindow;
    private Mat                 mZoomWindow2;
    private Mat                 mResult;
    private Mat 		teplateR;
    private Mat 		teplateL;
   
    private CascadeClassifier   mJavaDetector;
    private CascadeClassifier   mCascadeER;
    private CascadeClassifier   mCascadeEL;

    private static final Scalar   FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private float               mRelativeFaceSize = 0.2f;
    private float               mAbsoluteFaceSize = 0;
 
    private Rect                eyearea = new Rect();
    private MatOfByte mem=new MatOfByte();
     static double avgCOIx=0,avgCOIy=0;
     static double COEx,COEy;
  
    
     static int framecount=0,calibCount=0;
     static Point center=new Point();
    private static boolean count=false;
  
     
     
public EyeGaze() {
  // Initialise all required matrices and cascades
   
    mRgba=new Mat();
    mGray=new Mat();
    mZoomCorner=null;
    mZoomWindow=null;
    mZoomWindow2=new Mat();
    mResult=new Mat();
    teplateR=new Mat();
    teplateL=new Mat();
    mJavaDetector = new CascadeClassifier("lbpcascade_frontalface.xml");
    mCascadeER = new CascadeClassifier("haarcascade_righteye_2splits.xml");
    mCascadeEL = new CascadeClassifier( "haarcascade_lefteye_2splits.xml");
   
}

    public Image processFrame(VideoCapture capture,boolean calibration,int method,OptFrame op) {
        capture.grab(); //grabs individual frame from camera
        capture.retrieve(mRgba); // retives the grabbed frame into Mat obj
        Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_BGR2GRAY);//gray scale conversion
        Imgproc.equalizeHist(mGray, mGray );
        if (mAbsoluteFaceSize == 0) // gets height of frame detected
        {
        	int height = mGray.rows();
        	if (Math.round(height * mRelativeFaceSize) > 0);
        	{
        		mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
        	}
        	
        }        
        MatOfRect faces = new MatOfRect();//to crop and get and store all faces 
        //detects faces from mGray frame to faces object
       //Syntax -->  public  void detectMultiScale(Mat image, MatOfRect objects, double scaleFactor, int minNeighbors, int flags, Size minSize, Size maxSize)
        mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2 ,new Size(mAbsoluteFaceSize, mAbsoluteFaceSize)/*to get square*/, new Size());
        

        //To create zooming at corner
        if (mZoomCorner == null || mZoomWindow == null)
               CreateAuxiliaryMats();
        	        	 
        Rect[] facesArray = faces.toArray();
         //process each face detected	
        for (int i = 0; i < facesArray.length; i++){
            //Convert each face as rectangle
            Rect r = facesArray[i];
            
            //Draws rectangle around detected faces in mgray and mRgba frame 
            //r.tl returns point(X,Y) r.br returns point(X+width,Y+height)
            Imgproc.rectangle(mGray, r.tl(), r.br(), new Scalar(0, 255, 0, 255), 3);
            Imgproc.rectangle(mRgba, r.tl(), r.br(), new Scalar(0, 255, 0, 255), 3);
             //refer image in explanation folder
            eyearea = new Rect(r.x +r.width/8,(int)(r.y + (r.height/4.5)),r.width - 2*r.width/8,(int)( r.height/3.0));
            //Imgproc.rectangle(mRgba,eyearea.tl(),eyearea.br() , new Scalar(255,0, 0, 255), 2); 
            Rect eyearea_right = new Rect(r.x +r.width/16,(int)(r.y + (r.height/4.5)),(r.width - 2*r.width/16)/2,(int)( r.height/3.0));
            Rect eyearea_left = new Rect(r.x +r.width/16 +(r.width - 2*r.width/16)/2,(int)(r.y + (r.height/4.5)),(r.width - 2*r.width/16)/2,(int)( r.height/3.0));
            
            //find_pupil(mCascadeER,eyearea_right,24,calibration,method,op);        
            find_pupil(mCascadeEL,eyearea_left,24,calibration,method,op);
           
            
           Imgproc.resize(mRgba.submat(eyearea_left), mZoomWindow2, mZoomWindow2.size());
           Imgproc.resize(mRgba.submat(eyearea_right), mZoomWindow, mZoomWindow.size());
                 
    }
         Image im = null;
        facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
             
        //draw rec around face
        Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        if(!calibration){
        //Convert frame to img .bmp format to display in panel
        Imgcodecs.imencode(".bmp", mRgba, mem);
        try {
            im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
           
        } catch (IOException ex) {
           ex.printStackTrace();
        }
        return im;

    }
     return null;
    }
    
   private void CreateAuxiliaryMats() {
	    if (mGray.empty())
	        return;

	    int rows = mGray.rows();
	    int cols = mGray.cols();
	    
	    if (mZoomWindow == null){
          
	        mZoomWindow = mRgba.submat(rows / 2 + rows / 10 ,rows , cols / 2 + cols / 10, cols );
	        mZoomWindow2 = mRgba.submat(0, rows / 2 - rows / 10 , cols / 2 + cols / 10, cols );
	    }	    
 }
   
    private void  find_pupil(CascadeClassifier clasificator, Rect area,int size,boolean calibration,int method,OptFrame op){
            Mat template = new Mat();
            //fetch the eye area from original gray scale frame
	    Mat mROI = mGray.submat(area);
	    MatOfRect eyes = new MatOfRect();
	    Point iris = new Point();
            
	    Rect eye_template = new Rect();
            //detects eyes from region of interest ROI
	    clasificator.detectMultiScale(mROI, eyes, 1.15, 2,Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_SCALE_IMAGE, new Size(30,30),new Size());
	    Rect[] eyesArray = eyes.toArray();
            //process each eye detected
	    for (int i = 0; i < eyesArray.length; i++){
	    	Rect e = eyesArray[i];
	    	e.x = area.x + e.x;
	    	e.y = area.y + e.y;
	    	Rect eye_only_rectangle = new Rect((int)e.tl().x-3,(int)( e.tl().y + e.height*0.4)-3,(int)e.width-3,(int)(e.height*0.6)-3);
	    	mROI = mGray.submat(eye_only_rectangle);
	    	Mat vyrez = mRgba.submat(eye_only_rectangle);
                Imgproc.rectangle(mRgba, eye_only_rectangle.tl(), eye_only_rectangle.br(), FACE_RECT_COLOR, 1);
                //seperates dark and light region
	    	Core.MinMaxLocResult mmG = Core.minMaxLoc(mROI);
	    	//draw circle on iris
	    	Imgproc.circle(vyrez, mmG.minLoc,2, new Scalar(255, 255, 255, 255),2);
               
	    	iris.x = mmG.minLoc.x + eye_only_rectangle.width;
	    	iris.y = mmG.minLoc.y + eye_only_rectangle.height;
         
                if(calibration){
                    
                    calibrate(count,iris.x,iris.y);
                    count=true;
                }
                else{
                   
                 if(method!=0)
                     if(framecount<33){
                         framecount++;
                        // System.out.println(framecount+"   "+iris.x+"    "+iris.y+"    "+avgCOIx+"    "+avgCOIy);
                          avgCOIx+=iris.x;
                          avgCOIy+=iris.y;
                          
                     }
                     else{
                         System.out.println(avgCOIx/framecount+"     "+avgCOIy/framecount);
                        gazePointer(e,avgCOIx/framecount,avgCOIy/framecount,op);
                        framecount=0;avgCOIx=0;avgCOIy=0;
                }
                }
	    }
		
	  }


private void calibrate(boolean count,double eyeX,double eyeY) {
            center.x+=eyeX;
            center.y+=eyeY;
            
            if(count){
            center.x/=2;
            center.y/=2;
            }
          //  System.out.println("Center x   "+center.x+"       Center y   "+center.y);
}
private void gazePointer(Rect rect,double COIx, double COIy,OptFrame op) {
   Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
//   
  
       Robot robot;
 
         if(COIx<=center.x && COIy<=center.y){
            System.out.println("cell 1");
           try {
               robot = new Robot();
               robot.mouseMove((int)d.width/2-500, (int)d.height/2-250);
               op.foodBtn.doClick();
                } catch (AWTException ex) {
                    Logger.getLogger(EyeGaze.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if(COIx<=center.x && COIy>center.y){
                System.out.println("cell 3");
                 
           try {
               robot = new Robot();
               robot.mouseMove((int)d.width/2+500, (int)d.height/2-250);
                op.restBtn.doClick();
                } catch (AWTException ex) {
                    Logger.getLogger(EyeGaze.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if(COIx>center.x && COIy<=center.y){
                System.out.println("cell 2");
                 try {
               robot = new Robot();
               robot.mouseMove((int)d.width/2-500, (int)d.height/2+250);
                 op.waterBtn.doClick();
                } catch (AWTException ex) {
                    Logger.getLogger(EyeGaze.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
              else if(COIx>center.x && COIy>center.y){
                   try {
               robot = new Robot();
               robot.mouseMove((int)d.width/2+500, (int)d.height/2+250);
               op.sleepBtn.doClick();
               
                } catch (AWTException ex) {
                    Logger.getLogger(EyeGaze.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("cell 4");
            }
       

             
    }  

    public static int getMostOccoringElement(ArrayList<Integer> list) {
    int size = list.size();
    if(size == 0)
        return -1;
     
    int count = 0;
    int maxCount = 0;
    int element = list.get(0);
    int mostOccuringElement = element;
     
    for(int index = 0; index<size; index++) {
        if(list.get(index).equals(element)) {
            count++;
            if(count > maxCount) {
                maxCount = count;
                mostOccuringElement = element;
            }
        } else {
            count = 1;
        }
        element = list.get(index);
    }
    return mostOccuringElement;
}


}

    

