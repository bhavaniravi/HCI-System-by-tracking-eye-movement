package imagecomparison;


import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.RowFilter.Entry;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
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
    Mat area[]=new Mat[4];
    private CascadeClassifier   mJavaDetector;
    private CascadeClassifier   mCascadeER;
    private CascadeClassifier   mCascadeEL;
     private static final int   TM_SQDIFF = 0;
   
    private static final Scalar   FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private float               mRelativeFaceSize = 0.2f;
    private float               mAbsoluteFaceSize = 0;
 
    private Rect                eyearea = new Rect();
    private MatOfByte mem=new MatOfByte();
    boolean left=true;  
    static double COEx,COEy;
    boolean calibration=true;
    static int framecount=0;
    static Mat[] histogram=new Mat[5];
    static BufferedImage[] template=new BufferedImage[5];
    static double avgPOGX=0,avgPOGY=0,centerX,centerY,Rx,Ry,wEye,hEye,topRightX,topLeftX,bottomLeftX,bottomLeftY,topRightY,topLeftY,bottomRightX,bottomRightY;

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

    public Image processFrame(VideoCapture capture,boolean calibration,int method) {
        capture.grab(); //grabs individual frame from camera
        capture.retrieve(mRgba); // retives the grabbed frame into Mat obj
        Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_BGR2GRAY);//gray scale conversion
      
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
            
            //find_pupil(mCascadeER,eyearea_right,24,calibration,method);        
            find_pupil(mCascadeEL,eyearea_left,24,calibration,method);
           
            
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
   
    private void  find_pupil(CascadeClassifier clasificator, Rect area,int size,boolean calibration,int method){
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
               
	    	iris.x = mmG.minLoc.x ;
	    	iris.y = mmG.minLoc.y ;
         
                if(calibration){
                    calibrate(method,iris.x,iris.y,vyrez);
                    
                }
                else{
                    
                    int max=compareImage1(getImage(mROI));
                    moveMouse(max);
                }
	    }
		
	  }
void moveMouse(int max){
        Robot robot;
          Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
       if(max==1){
            System.out.println("cell 1");
           // System.out.println("center x     "+p.center.x+"   center y    "+p.center.y+"   CoIx     "+p.COIx+"   CoIy        "+p.COIy);
           try {
               robot = new Robot();
               robot.mouseMove((int)d.width/2-500, (int)d.height/2-350);
                } catch (AWTException ex) {
                    Logger.getLogger(EyeGaze.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            else if(max==2){
                System.out.println("cell 2");
             //   System.out.println("center x     "+p.center.x+"   center y    "+p.center.y+"   CoIx     "+p.COIx+"   CoIy        "+p.COIy);
                 try {
               robot = new Robot();
               robot.mouseMove((int)d.width/2+500, (int)d.height/2-350);
                } catch (AWTException ex) {
                    Logger.getLogger(EyeGaze.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if(max==3){
                System.out.println("cell 3");
             //   System.out.println("center x     "+p.center.x+"   center y    "+p.center.y+"   CoIx     "+p.COIx+"   CoIy        "+p.COIy);
                 
           try {
                robot = new Robot();
               robot.mouseMove((int)d.width/2-500, (int)d.height/2+350);
                } catch (AWTException ex) {
                    Logger.getLogger(EyeGaze.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
              else if(max==4){
                   try {
               robot = new Robot();
               robot.mouseMove((int)d.width/2+500, (int)d.height/2+350);
                } catch (AWTException ex) {
                    Logger.getLogger(EyeGaze.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("cell 4");
               // System.out.println("center x     "+p.center.x+"   center y    "+p.center.y+"   CoIx     "+p.COIx+"   CoIy        "+p.COIy);
            }   

    }
BufferedImage getImage(Mat mROI){
    Imgproc.threshold(mROI, mROI,0, 255, Imgproc.THRESH_BINARY);
    Image im = null;
     MatOfByte mem1=new MatOfByte();
  Imgcodecs.imencode(".bmp", mRgba, mem1);
        try {
            im = ImageIO.read(new ByteArrayInputStream(mem1.toArray()));
           
        } catch (IOException ex) {
           ex.printStackTrace();
        }
        return (BufferedImage)im;

}

private void calibrate(int method,double eyeX,double eyeY,Mat mROI) {
    switch(method){       
        case 1:
            centerX=eyeX;
            centerY=eyeY;
            histogram[0]=histogram(mROI); 
            template[0]=getImage(mROI);
            break;
        case 2:
            topLeftX=eyeX;
            topLeftY=eyeY;
             histogram[1]=histogram(mROI);
             template[1]=getImage(mROI);
            break;
        case 3:
            topRightX=eyeX;
            topRightY=eyeY;        
             histogram[2]=histogram(mROI);
               template[2]=getImage(mROI);
            break;
        case 4:
            bottomLeftX=eyeX;
            bottomLeftY=eyeY;
             histogram[3]=histogram(mROI);
               template[3]=getImage(mROI);
            break;
        case 5:
            bottomRightX=eyeX;
            bottomRightY=eyeY;     
            histogram[4]=histogram(mROI);
            template[4]=getImage(mROI);
            break;
    }
    
   // System.out.println("centerX:     "+centerX+"\ncenterY:       "+centerY+"\ntopRightX       "+topRightX+"\ntopLeftX    "+topLeftX+"\nbottomLeftX        "+bottomLeftX+"\nbottomLeftY        "+bottomLeftY+"\ntopRightY    "+topRightY+"\ntopLeftY    "+topLeftY+"\nbottomRightX     "+bottomRightX+"\nbottomRightY      "+bottomRightY+" ");
}


public void compareImage(Mat mROI){
    Image im2=getImage(mROI);
    for(int i=0;i<template.length;i++){
        ImageCompare ic = new ImageCompare(template[i],im2);		
        ic.setParameters(8, 6, 5, 10);       
        ic.setDebugMode(2);      
        ic.compare();       
        if(ic.match){
            System.out.println(i);
            break;
        }
        else{
            System.out.println("no match...");
        }
    }

}
static ArrayList<Integer> text=new ArrayList<>();
  public int compare(Mat mROI)
{

    Mat H1 = histogram(mROI);
    int i=0,high=-1;
    double highData=-1;
   
        for (i=0;i< histogram.length;i++) {
             //text.put(i,Imgproc.compareHist(H1, histogram[i], Imgproc.CV_COMP_CORREL));
            double d=Imgproc.compareHist(H1, histogram[i], Imgproc.CV_COMP_CORREL);
            if(d>highData){
                highData=d;
                high=i;
            }
          
        }
        text.add(high);
         //System.out.println(high+"    "+highData);
return high;
}
public Mat histogram(Mat src) // zwraca histogram obrazu
{
  
//    Mat src = new Mat(img.height(), img.width(), CvType.CV_8UC2);
//    Imgproc.cvtColor(img, src, Imgproc.COLOR_RGB2GRAY);
    Vector<Mat> bgr_planes = new Vector<>();                                                                                                                                                                                 
    Core.split(src, bgr_planes);
    MatOfInt histSize = new MatOfInt(256);
    final MatOfFloat histRange = new MatOfFloat(0f, 256f);
    boolean accumulate = false;
    Mat b_hist = new  Mat();
    Imgproc.calcHist(bgr_planes, new MatOfInt(0),new Mat(), b_hist, histSize, histRange, accumulate);
    return b_hist;
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
    HashMap<Integer,Integer> imagePercent=new HashMap<Integer,Integer>();
public int compareImage1(BufferedImage biA) {

    int percentage = 0;
    try {
        // take buffer data from both image files //
     
        DataBuffer dbA = biA.getData().getDataBuffer();
        int sizeA = dbA.getSize();
        for(int j=0;j<4;j++){
        DataBuffer dbB = template[j].getData().getDataBuffer();
        int sizeB = dbB.getSize();
        int count = 0;
        // compare data-buffer objects //
        if (sizeA == sizeB) {

            for (int i = 0; i < sizeA; i++) {
                if (dbA.getElem(i) == dbB.getElem(i)) {
                    count = count + 1;
                }

            }
           
            imagePercent.put(j,(count * 100) / sizeA);
        } else {
            System.out.println("Both the images are not of same size");
        }
        }

    } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Failed to compare image files ...");
    }
  
int maxValueInMap=(Collections.max(imagePercent.values()));  // This will return max value in the Hashmap
        for (Map.Entry<Integer, Integer> entry : imagePercent.entrySet()) {  // Itrate through hashmap
            if (entry.getValue()==maxValueInMap) {
                System.out.println(entry.getKey());
                return entry.getKey();// Print the key with max value
            }
        }
     
return -1;

}

}
