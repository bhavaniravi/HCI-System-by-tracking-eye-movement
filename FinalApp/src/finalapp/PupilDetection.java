package finalapp;


import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;

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
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

public class PupilDetection {

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
     private static final int   TM_SQDIFF = 0;
   
    private static final Scalar   FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private float               mRelativeFaceSize = 0.2f;
    private float               mAbsoluteFaceSize = 0;
    private int 		learn_frames = 0;
    private double 		match_value;
    private Rect                eyearea = new Rect();
    private MatOfByte mem=new MatOfByte();
     
    
public PupilDetection() {
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
   public void resetLearFramesCount()
    {
    	learn_frames = 0;
    }
    public Image processFrame(VideoCapture capture) {
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
            Imgproc.rectangle(mRgba,eyearea.tl(),eyearea.br() , new Scalar(255,0, 0, 255), 2); 
            Rect eyearea_right = new Rect(r.x +r.width/16,(int)(r.y + (r.height/4.5)),(r.width - 2*r.width/16)/2,(int)( r.height/3.0));
            Rect eyearea_left = new Rect(r.x +r.width/16 +(r.width - 2*r.width/16)/2,(int)(r.y + (r.height/4.5)),(r.width - 2*r.width/16)/2,(int)( r.height/3.0));
            Imgproc.rectangle(mRgba,eyearea_left.tl(),eyearea_left.br() , new Scalar(255,0, 0, 255), 2); 
            Imgproc.rectangle(mRgba,eyearea_right.tl(),eyearea_right.br() , new Scalar(255, 0, 0, 255), 2);
            
            //Every eye differs from one another hence we create template for 1st five frames alone
            if(learn_frames<5){
                   teplateR = get_template(mCascadeER,eyearea_right,24);
                   teplateL = get_template(mCascadeEL,eyearea_left,24);
                   learn_frames++;
            }
            else{
            //pass eye area and template designed.....    
            match_value = match_eye(eyearea_right,teplateR,1); 
            match_value = match_eye(eyearea_left,teplateL,1); 
            }
            
    
           Imgproc.resize(mRgba.submat(eyearea_left), mZoomWindow2, mZoomWindow2.size());
           Imgproc.resize(mRgba.submat(eyearea_right), mZoomWindow, mZoomWindow.size());
                 
    }

        facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            //draw rec around face
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        //Convert frame to img .bmp format to display in panel
        Imgcodecs.imencode(".bmp", mRgba, mem);
        Image im = null;
        try {
            im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
           
        } catch (IOException ex) {
           ex.printStackTrace();
        }
        return im;
    }
    
    //
   private void CreateAuxiliaryMats() {
	    if (mGray.empty())
	        return;

	    int rows = mGray.rows();
	    int cols = mGray.cols();
	    
	    if (mZoomWindow == null){
                
               /*
                Frame matrix(10 X 10)           Converted mZoomWindow() Right eye         Converted mZoomWindow2 left eye
               1 - - - - - - - - - -              6- - - - -                                 1- - - - -   
               2 - - - - - - - - - -              7- - - - -                                 2 - - - - -   
               3 - - - - - - - - - -              8- - - - -                                 3 - - - - -     
               4 - - - - - - - - - -              9- - - - -                                 4 - - - - -     
               5 - - - - - - - - - -     =>       10- - - - -                                5 - - - - -     
               6 - - - - - - - - - -                                            - - - - -     
               7 - - - - - - - - - - 
               8 - - - - - - - - - - 
               9- - - - - - - - - - 
               
                */
	        mZoomWindow = mRgba.submat(rows / 2 + rows / 10 ,rows , cols / 2 + cols / 10, cols );
	        mZoomWindow2 = mRgba.submat(0, rows / 2 - rows / 10 , cols / 2 + cols / 10, cols );
	    }
	    
	}
   //compares frame with templates
   private double  match_eye(Rect area, Mat mTemplate,int type){
		  Point matchLoc; 
		  Mat mROI = mGray.submat(area);
                    int result_cols =  mGray.cols() - mTemplate.cols() + 1;
		  int result_rows = mGray.rows() - mTemplate.rows() + 1;
                  //if template exactly matches frame return 0
		  if(mTemplate.cols()==0 ||mTemplate.rows()==0){
			  return 0.0;
		  }
		  mResult = new Mat(result_cols,result_rows, CvType.CV_32FC1);
                  //http://docs.opencv.org/2.4/doc/tutorials/imgproc/histograms/template_matching/template_matching.html
                  //else match the ROI with template and store in mResult
		  Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_SQDIFF) ; 
		  //Continue as did in get_template
		  Core.MinMaxLocResult mmres =  Core.minMaxLoc(mResult);
                    matchLoc = mmres.minLoc; 
		  Point  matchLoc_tx = new Point(matchLoc.x+area.x,matchLoc.y+area.y);
		  Point  matchLoc_ty = new Point(matchLoc.x + mTemplate.cols() + area.x , matchLoc.y + mTemplate.rows()+area.y );
		 
		  Imgproc.rectangle(mRgba, matchLoc_tx,matchLoc_ty, new Scalar(255, 255, 0, 255));
                  return mmres.maxVal; 

	    }
    private Mat  get_template(CascadeClassifier clasificator, Rect area,int size){
            Mat template = new Mat();
            //fetech the eye area from original gray scale frame
	    Mat mROI = mGray.submat(area);
	    MatOfRect eyes = new MatOfRect();
	    Point iris = new Point();
	    Rect eye_template = new Rect();
            //detects eyes from region of interest ROI
	    clasificator.detectMultiScale(mROI, eyes, 1.15, 2,Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_SCALE_IMAGE, new Size(30,30),new Size());
	    Rect[] eyesArray = eyes.toArray();
            //process each eye
	    for (int i = 0; i < eyesArray.length; i++){
	    	Rect e = eyesArray[i];
	    	e.x = area.x + e.x;
	    	e.y = area.y + e.y;
	    	Rect eye_only_rectangle = new Rect((int)e.tl().x,(int)( e.tl().y + e.height*0.4),(int)e.width,(int)(e.height*0.6));
	    	mROI = mGray.submat(eye_only_rectangle);
	    	Mat vyrez = mRgba.submat(eye_only_rectangle);
                //seperates dark and light region
	    	Core.MinMaxLocResult mmG = Core.minMaxLoc(mROI);
	    	//draw circle on iris
	    	Imgproc.circle(vyrez, mmG.minLoc,2, new Scalar(255, 255, 255, 255),2);
	    	iris.x = mmG.minLoc.x + eye_only_rectangle.x;
	    	iris.y = mmG.minLoc.y + eye_only_rectangle.y;
                //map iris back to eye template  
	    	eye_template = new Rect((int)iris.x-size/2,(int)iris.y-size/2 ,size,size);
                //draw eye template to frame
	    	Imgproc.rectangle(mRgba,eye_template.tl(),eye_template.br(),new Scalar(255, 0, 0, 255), 2);
                //fetch the template from Grayscale image and return it
	    	template = (mGray.submat(eye_template)).clone();
	    	return template;
	    }
		 return template;
	  }
}