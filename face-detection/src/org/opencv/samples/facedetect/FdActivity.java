package org.opencv.samples.facedetect;


import org.opencv.samples.facedetect.Bluethooth;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.util.concurrent.Delayed;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.FpsMeter;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;

import android.R.integer;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.StaticLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class FdActivity extends Activity implements CvCameraViewListener2 {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;
    

    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    private MenuItem               mItemType;

    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;
    private DetectionBasedTracker  mNativeDetector;

    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
    public int mati =0;
    public int first=0;
    public int second=0;
    
    public int mati2 =0;
    public int first2=0;
    public int second2=0;
    
    public int error = 55;
    public int error2 = 35;
    
    public int count=0;
    
  
    
    
    int midFaceY=0;
    int midFaceX=0;
    
    int midScreenY; 
    int midScreenX;
    
    int midScreenWindow = 20;
    
    int servoTiltPosition = 70;
    int servoPanPosition = 90;
    
    
    String tiltChannel = "0";
    String panChannel = "1";
    
    
    int stepSize=2;

    TextView myLabel;
    TextView myLabel2;
    TextView myLabel3;
    TextView myLabel4;

    

    private CameraBridgeViewBase   mOpenCvCameraView;

    private final static String DATA_RECEIVED_INTENT = "primavera.arduino.intent.action.DATA_RECEIVED";
    private final static String SEND_DATA_INTENT = "primavera.arduino.intent.action.SEND_DATA";
    private final static String DATA_EXTRA = "primavera.arduino.intent.extra.DATA";
    private final static byte[] DATA = {0x49};
    private final static byte[] DATA1 = {48};

    public Bluethooth thing ;


    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
        	
                     
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                 //   System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                      //  mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.enableFpsMeter();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	
    	
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        
    
            
        
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCameraIndex(1);
        mOpenCvCameraView.setCvCameraViewListener(this);

        thing = new Bluethooth();

        Context cccc = getApplicationContext();
        thing.onBlue(cccc);
        
        
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
        
	//  mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
       // BluetoothAdapter mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
	   // mBlueAdapter.disable();
        
    }
  

    public void onCameraViewStarted(int width, int height) {
    	
    
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }
    
    public static void textToast(String textToDisplay, Context context)  { 
    	//Toast toast2 = Toast.makeText(getApplicationContext(),"Turned on", Toast.LENGTH_LONG);
      //  toast2.show();
    }
    
    
    public void Toast(){
    	
    	     	
    	// Toast toast2 = Toast.makeText(getApplicationContext(),"Turned on", Toast.LENGTH_LONG);
       // toast2.show();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

  	
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
           // mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
    /*    else if (mDetectorType == NATIVE_DETECTOR) {
            if (mNativeDetector != null)
                mNativeDetector.detect(mGray, faces);
        }*/
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();
        if (facesArray.length > 0){
            Core.rectangle(mRgba, facesArray[0].tl(), facesArray[0].br(), FACE_RECT_COLOR, 3);

            if(count < 1)
            	{
            	takeScreenShot();
            	count++;
            	}
            
             midScreenY = 120; //(mOpenCvCameraView.getHeight()/2);
             midScreenX = 650; //(mOpenCvCameraView.getWidth()/2);
             // midScreenY = 240;
             // midScreenX = 440;
        
        midFaceY = facesArray[0].y + (facesArray[0].height/2);
        midFaceX = facesArray[0].x + (facesArray[0].width/2);
        
        
     //   midFaceY = facesArray[0].y ;
      //  midFaceX = facesArray[0].x ;
        

        first = midFaceX ;
        second= midScreenX;// mOpenCvCameraView.getHeight()/2;
        
        mati = first - second;
        
        
        
        first2 = midFaceY;
        second2 = midScreenY;//mOpenCvCameraView.getWidth()/2;
        mati2 = first2 - second2;
        
        
        
        if(midFaceY < (midScreenY - midScreenWindow)){
            if(servoTiltPosition >= 5){
            	servoTiltPosition -= stepSize; //If it is below the middle of the screen, update the tilt position variable to lower the tilt servo.
            	}
            }
          //Find out if the Y component of the face is above the middle of the screen.
          else if(midFaceY > (midScreenY + midScreenWindow)){
            if(servoTiltPosition <= 175){
            	servoTiltPosition +=stepSize; //Update the tilt position variable to raise the tilt servo.
            	}
            }
          //Find out if the X component of the face is to the left of the middle of the screen.
          if(midFaceX > (midScreenX + midScreenWindow)){
            if(servoPanPosition >= 5){
            	servoPanPosition -= stepSize; //Update the pan position variable to move the servo to the left.
            	}
            }
          //Find out if the X component of the face is to the right of the middle of the screen.
          else if(midFaceX < (midScreenX - midScreenWindow)){
           if(servoPanPosition <= 175){
        	   servoPanPosition +=stepSize; //Update the pan position variable to move the servo to the right.
           		}
           }

          
       
            	
          	 
                
      /*    
          
          port.write(tiltChannel);      //Send the tilt servo ID
          port.write(servoTiltPosition); //Send the updated tilt position.
          port.write(panChannel);        //Send the Pan servo ID
          port.write(servoPanPosition);  //Send the updated pan position.
          */
          
       

         /*   IntentFilter filter = new IntentFilter();
            filter.addAction(DATA_RECEIVED_INTENT);
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    if (DATA_RECEIVED_INTENT.equals(action)) {
                        final byte[] data = intent.getByteArrayExtra(DATA_EXTRA);
                        Toast.makeText(context, new String(data), Toast.LENGTH_SHORT).show();
                    }
                }
            }, filter); */
          
          
          /*
if(mati>40 ){
            Intent intent = new Intent(SEND_DATA_INTENT);
            intent.putExtra(DATA_EXTRA, DATA);
         //   sendBroadcast(intent);


           

            try {
            	
                
                thing.sendData("2");
                

            }catch (IOException ex ){}

        
     



    runOnUiThread(new Runnable() {

        public void run() {
            //HERE I UPDATE TEXT VIEW
          //  TextView    myLabel = (TextView) findViewById(R.id.text_id);
         //   myLabel.setText(String.valueOf(mati));
        //	 Bluethooth thing1 = new Bluethooth();

       
    });
    
    
} 



            if(mati<-40 )
            {

               /* Intent intent = new Intent(SEND_DATA_INTENT);
                intent.putExtra(DATA_EXTRA, DATA1);
                sendBroadcast(intent); */
            	
          
          /*
            	try {
                	
            		
                    thing.sendData("1");

                }catch (IOException ex ){}

            }

            
            
            if(mati>-90 & mati<90 )
            {
              /*  Intent intent = new Intent(SEND_DATA_INTENT);
                intent.putExtra(DATA_EXTRA, DATA2);
                sendBroadcast(intent);
                */
          
          /*
            	try {
                	
            		thing.sendData("3");

                }catch (IOException ex ){}

            	
            }
            
            
            */
          
      
          
          
         //  FpsMeter.setTextForTesting("TiltPosition :" + String.valueOf(servoTiltPosition));
           
           FpsMeter.setTextForTesting("MATI :" + String.valueOf(mati));
           
           
           
           
       runOnUiThread(new Runnable() {
    	   
    	   
    	   

            public void run() {
            	
            	
            	 if(mati<-error )
                 {

                    /* Intent intent = new Intent(SEND_DATA_INTENT);
                     intent.putExtra(DATA_EXTRA, DATA1);
                     sendBroadcast(intent); */
                 	
               
               
                 	try {
                     	
                 		
                         thing.sendData("5");

                     }catch (IOException ex ){}

                 }

                 
                 
                 if(mati>-error & mati<error )
                 {
                     /*Intent intent = new Intent(SEND_DATA_INTENT);
                     intent.putExtra(DATA_EXTRA, DATA2);
                     sendBroadcast(intent);
                     
               
               */
                 	try {
                     	
                 		thing.sendData("0");

                     }catch (IOException ex ){}
                 }
                 	
                 	 if(mati>error )
                     {

                        /* Intent intent = new Intent(SEND_DATA_INTENT);
                         intent.putExtra(DATA_EXTRA, DATA1);
                         sendBroadcast(intent); */
                     	
                   
                   
                     	try {
                         	
                     		
                             thing.sendData("4");

                         }catch (IOException ex ){}

                     }
                 	 
                 	 
                     
                     if(mati2>error2 ){
                          Intent intent = new Intent(SEND_DATA_INTENT);
                          intent.putExtra(DATA_EXTRA, DATA);
                          sendBroadcast(intent);
                          
                          try {
          					thing.sendData("6");
          				} catch (IOException e) {
          					// TODO Auto-generated catch block
          					e.printStackTrace();
          				}
                       
                          
              
                          
                      }
                      if(mati2<-error2 )
                      {
                          Intent intent = new Intent(SEND_DATA_INTENT);
                          intent.putExtra(DATA_EXTRA, DATA1);
                          sendBroadcast(intent);
                          try {
          					thing.sendData("7");
          				} catch (IOException e) {
          					// TODO Auto-generated catch block
          					e.printStackTrace();
          				}
                      }
                      

                 	
                 
                 
                 
                 
               
           
            	
            	
            	
                //HERE I UPDATE TEXT VIEW
                myLabel = (TextView) findViewById(R.id.text_id);
                myLabel2 = (TextView) findViewById(R.id.textView1);
                myLabel3 = (TextView) findViewById(R.id.text_id2);
                myLabel4 = (TextView) findViewById(R.id.textView2);
                myLabel.setText("TiltPosition :" + String.valueOf(servoTiltPosition));
                myLabel2.setText("PanPosition" +Integer.toString(servoPanPosition));
                myLabel.setText("midFaceY :" + Integer.toString(midFaceY));
                myLabel2.setText("midScreenY :" + Integer.toString(midScreenY));
                myLabel3.setText("midFaceX :" + Integer.toString(midFaceX));
                myLabel4.setText("midScreenX :" + Integer.toString(midScreenX));
                
                
                
                
                try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
                
                
                
           /*     
                try {
                  	
              	  
               	 
              	  String s_t_position = Integer.toString(servoTiltPosition);
              	 
              	  String s_p_position = Integer.toString(servoPanPosition);
                    
              /*	  if (s_t_position.length()<3)
              	  {
              		  s_t_position = "0" + s_t_position;
              		  Log.i(TAG, s_t_position);
              	  }
              	  
              	  
              	  if (s_p_position.length()<3)
              	  {	
              		   s_p_position = "0"+ s_p_position;
              	  }
              	  
              	  
                    
              	  	thing.sendData(tiltChannel+"*");
                    thing.sendData(s_t_position+"*");
                    
                    thing.sendData(panChannel+"*");
                    thing.sendData(s_p_position+"*");
                    try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    

                }catch (IOException ex ){}*/
            }
        });


}

        return mRgba;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        mItemType   = menu.add(mDetectorName[mDetectorType]);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);
        else if (item == mItemType) {
          /*  int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
            item.setTitle(mDetectorName[tmpDetectorType]);
            setDetectorType(tmpDetectorType);
           /* Toast toast = Toast.makeText(this,"Turned on", Toast.LENGTH_LONG);
            toast.show();*/
        }
        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                Log.i(TAG, "Detection Based Tracker enabled");
                mNativeDetector.start();
            } else {
                Log.i(TAG, "Cascade detector enabled");
                mNativeDetector.stop();
            }
        }
    }
    
    
    public void takeScreenShot(){
    	
    	org.opencv.android.JavaCameraView fd_activity_surface_view;
       
       
        
     
            fd_activity_surface_view = (org.opencv.android.JavaCameraView) findViewById(R.id.fd_activity_surface_view);
           
               
                    View v1 = fd_activity_surface_view.getRootView();
                    v1.setDrawingCacheEnabled(true);
                    Bitmap bm = v1.getDrawingCache();
                  
                
          
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
  //  bm.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

    saveToInternalSorage(bm);
   
    
    }
    
    public String saveToInternalSorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
         // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {           

            fos = new FileOutputStream(mypath);

       // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }
  
}
