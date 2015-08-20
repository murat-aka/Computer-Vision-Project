package org.opencv.samples.colorblobdetect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Point;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.Highgui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ColorBlobDetectionActivity extends Activity implements
		OnTouchListener, CvCameraViewListener2 {
	private static final String TAG = "OCVSample::Activity";

	private boolean mIsColorSelected = false;
	private Mat mRgba;
	private Scalar mBlobColorRgba;
	private Scalar mBlobColorHsv;
	private ColorBlobDetector mDetector;
	private Mat mSpectrum;
	private Size SPECTRUM_SIZE;
	private Scalar CONTOUR_COLOR;
	private Mat src;

	Scalar colorGreen = new Scalar(0, 255, 0, 255);
	private double d, dTextScaleFactor;
	public int mati = 0;
	public int first = 0;
	public int second = 0;

	public int mati2 = 0;
	public int first2 = 0;
	public int second2 = 0;

	public int L_area = 0;
	public int P_area = 0;

	int flashCounter = 0;
	int lostCounter;
	
	public boolean forward = true;
	
	int foundCounter;
	public boolean found = true;
	private Mat mIntermediateMat;

	public int error = 55;
	public int error2 = 35;

	BluetoothAdapter mBluetoothAdapter;

	private final static String SEND_DATA_INTENT = "primavera.arduino.intent.action.SEND_DATA";
	private final static String DATA_EXTRA = "primavera.arduino.intent.extra.DATA";
	private final static byte[] DATA = { 0x49 };
	private final static byte[] DATA1 = { 48 };
	private final static byte[] DATA2 = { 0x40 };
	private CameraBridgeViewBase mOpenCvCameraView;

	public Bluethooth thing;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
				mOpenCvCameraView
						.setOnTouchListener(ColorBlobDetectionActivity.this);
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public ColorBlobDetectionActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.color_blob_detection_surface_view);

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_surface_view);
		mOpenCvCameraView.setCameraIndex(1);
		mOpenCvCameraView.setCvCameraViewListener(this);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.enable();
		thing = new Bluethooth();

		thing.onBlue();
		try {
			thing.sendData("9");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mDetector = new ColorBlobDetector();
		mSpectrum = new Mat();
		mBlobColorRgba = new Scalar(255);
		mBlobColorHsv = new Scalar(255);
		SPECTRUM_SIZE = new Size(200, 64);
		CONTOUR_COLOR = new Scalar(255, 0, 0, 255);

		DisplayMetrics dm = this.getResources().getDisplayMetrics();
		int densityDpi = dm.densityDpi;
		dTextScaleFactor = ((double) densityDpi / 240.0) * 0.9;
	}

	public void onCameraViewStopped() {
		mRgba.release();
	}

	public boolean onTouch(View v, MotionEvent event) {
		int cols = mRgba.cols();
		int rows = mRgba.rows();

		int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
		int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

		int x = (int) event.getX() - xOffset;
		int y = (int) event.getY() - yOffset;

		Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

		if ((x < 0) || (y < 0) || (x > cols) || (y > rows))
			return false;

		Rect touchedRect = new Rect();

		touchedRect.x = (x > 4) ? x - 4 : 0;
		touchedRect.y = (y > 4) ? y - 4 : 0;

		touchedRect.width = (x + 4 < cols) ? x + 4 - touchedRect.x : cols
				- touchedRect.x;
		touchedRect.height = (y + 4 < rows) ? y + 4 - touchedRect.y : rows
				- touchedRect.y;

		Mat touchedRegionRgba = mRgba.submat(touchedRect);

		Mat touchedRegionHsv = new Mat();
		Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv,
				Imgproc.COLOR_RGB2HSV_FULL);

		// Calculate average color of touched region
		mBlobColorHsv = Core.sumElems(touchedRegionHsv);
		int pointCount = touchedRect.width * touchedRect.height;
		for (int i = 0; i < mBlobColorHsv.val.length; i++)
			mBlobColorHsv.val[i] /= pointCount;

		mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

		Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", "
				+ mBlobColorRgba.val[1] + ", " + mBlobColorRgba.val[2] + ", "
				+ mBlobColorRgba.val[3] + ")");

		mDetector.setHsvColor(mBlobColorHsv);

		Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

		mIsColorSelected = true;

		touchedRegionRgba.release();
		touchedRegionHsv.release();

		return false; // don't need subsequent touch events
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

		mRgba = inputFrame.rgba();

		/*
		 * Mat circles = new Mat();
		 * 
		 * 
		 * // Imgproc.GaussianBlur(inputFrame.gray(), mIntermediateMat, new
		 * Size(9, 9), 4,4 ); //Imgproc.cvtColor(mRgba, mRgba,
		 * Imgproc.COLOR_BGR2GRAY);
		 * 
		 * Imgproc.GaussianBlur( mRgba, mRgba, new Size(9, 9), 2, 2 );
		 * 
		 * Imgproc.HoughCircles( mRgba, circles, Imgproc.CV_HOUGH_GRADIENT, 1,
		 * mRgba.rows()/8, 200, 100, 0, 0 );
		 * 
		 * /// Apply the Hough Transform to find the circles
		 * //Imgproc.HoughCircles(mIntermediateMat, circles,
		 * Imgproc.CV_HOUGH_GRADIENT, 2, mIntermediateMat.rows() / 4, 100, 100,
		 * 50, 200);
		 * 
		 * //image->width/4, 100,100,0,50 /// Draw the circles detected for (int
		 * x = 0; x < circles.cols(); x++) { double vCircle[]=circles.get(0,x);
		 * 
		 * Point center=new Point(Math.round(vCircle[0]),
		 * Math.round(vCircle[1])); int radius = (int)Math.round(vCircle[2]); //
		 * circle center Core.circle( mIntermediateMat, center, 3, new
		 * Scalar(0,255,0), -1, 8, 0 ); // circle outline Core.circle(
		 * mIntermediateMat, center, radius, new Scalar(0,0,255), 3, 8, 0 );
		 * 
		 * 
		 * first = center.hashCode() ; second= mOpenCvCameraView.getHeight()/2;
		 * mati = first - second;
		 * 
		 * if(mati>90 ){ Intent intent = new Intent(SEND_DATA_INTENT);
		 * intent.putExtra(DATA_EXTRA, DATA); sendBroadcast(intent); }
		 * if(mati<-90 ) { Intent intent = new Intent(SEND_DATA_INTENT);
		 * intent.putExtra(DATA_EXTRA, DATA1); sendBroadcast(intent); } }
		 */

		if (mIsColorSelected) {

			mDetector.process(mRgba);
			List<MatOfPoint> contours = mDetector.getContours();
			Log.e(TAG, "Contours count: " + contours.size());
			Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

			int largestContour = -1;
			double area = 0;

			for (int i = 0; i < contours.size(); i++) {
				double cArea = Imgproc.contourArea(contours.get(i));
				if (cArea > area) {
					area = cArea;
					L_area = (int) area;
					largestContour = i;
				}
			}

			//
			Rect r = null;

			if (largestContour > -1) {
				r = Imgproc.boundingRect(contours.get(largestContour));
				flashCounter = 0;
				
				
				
				if (foundCounter == 2) {
					
					foundCounter = 0;
					
					

				}
				
				foundCounter++;
				
			}

			if (r != null) {
				Core.circle(mRgba, r.tl(), 50, CONTOUR_COLOR, 5);
				Core.rectangle(mRgba, r.tl(), r.br(), CONTOUR_COLOR, 5);
				
				
				
				first = r.x;
				second = 160;
				mati = first - second;

				first2 = r.y;
				second2 = 120;// mOpenCvCameraView.getWidth()/2;
				mati2 = first2 - second2;

				/*
				 * IntentFilter filter = new IntentFilter();
				 * filter.addAction(DATA_RECEIVED_INTENT); registerReceiver(new
				 * BroadcastReceiver() {
				 * 
				 * @Override public void onReceive(Context context, Intent
				 * intent) { final String action = intent.getAction(); if
				 * (DATA_RECEIVED_INTENT.equals(action)) { final byte[] data =
				 * intent.getByteArrayExtra(DATA_EXTRA); Toast.makeText(context,
				 * new String(data), Toast.LENGTH_SHORT).show(); } } }, filter);
				 */

				String s_p_position = Integer.toString(mati2);

			}

			if (largestContour == -1) {

				flashCounter++;
				lostCounter++;
				
			

			/*	if (flashCounter == 2  ) {
					
					flashCounter = 0;
					try {
						thing.sendData("9");
			   		} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					L_area = 0;
					P_area = 0;

				}
				*/
				
				
				if(lostCounter == 75 & found == true){
					
					lostCounter = 0;
					found = false;
					try {
						thing.sendData("2");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				
				if(lostCounter > 75)lostCounter = 0;
				
				
				
			
				}
			
			

			

			ShowTitle("P_Area: " + P_area, 3, colorGreen);
			ShowTitle("L_Area: " + L_area, 4, colorGreen);

			if (largestContour > -1 & L_area < 800 & L_area != 0) {
				found = true;
				// L_area = 500;
				new Thread(new Runnable() {
					public void run() {

						if (mati2 > error2) {
							/*Intent intent = new Intent(SEND_DATA_INTENT);
							intent.putExtra(DATA_EXTRA, DATA);
							sendBroadcast(intent);*/

							try {
								thing.sendData("6");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
						if (mati2 < -error2) {
						/*	Intent intent = new Intent(SEND_DATA_INTENT);
							intent.putExtra(DATA_EXTRA, DATA1);
							sendBroadcast(intent);*/
							try {
								thing.sendData("7");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						if (mati > error) {
						/*	Intent intent = new Intent(SEND_DATA_INTENT);
							intent.putExtra(DATA_EXTRA, DATA);
							sendBroadcast(intent);
*/
							try {
								thing.sendData("4");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
						if (mati < -error) {
						/*	Intent intent = new Intent(SEND_DATA_INTENT);
							intent.putExtra(DATA_EXTRA, DATA1);
							sendBroadcast(intent);
							*/
							try {
								thing.sendData("5");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					//	if (mati > -error & mati < error && forward == true) {
							if (mati > -error & mati < error ) {
						/*	Intent intent = new Intent(SEND_DATA_INTENT);
							intent.putExtra(DATA_EXTRA, DATA2);
							sendBroadcast(intent);
								
						*/	
							forward = false;
								try {
									thing.sendData("0");

								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}					

							
						}
						try {
							Thread.sleep(300);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}).start();
				
			}
				if (largestContour > -1 & L_area > 900 & L_area != 0) {
				// L_area = 500;
				new Thread(new Runnable() {
					public void run() {

					

							if (L_area > P_area + 200 & mati > -error & mati < error) {

							
								
								try {
									thing.sendData("1");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								P_area = L_area;
							}
							

							else if (L_area < P_area - 200) {

								/*
								 * try { thing.sendData("0"); } catch
								 * (IOException e) { // TODO Auto-generated
								 * catch block e.printStackTrace(); }
								 */
							/*	try {
									thing.sendData("0");

								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}*/
								P_area = L_area;
								forward = true;
							}
							
							
							
							if (mati > error) {
							/*	Intent intent = new Intent(SEND_DATA_INTENT);
								intent.putExtra(DATA_EXTRA, DATA);
								sendBroadcast(intent);*/

								try {
									thing.sendData("4");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
							else if (mati < -error) {
							/*	Intent intent = new Intent(SEND_DATA_INTENT);
								intent.putExtra(DATA_EXTRA, DATA1);
								sendBroadcast(intent);*/
								try {
									thing.sendData("5");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
							
							if (mati2 > error2) {
							/*	Intent intent = new Intent(SEND_DATA_INTENT);
								intent.putExtra(DATA_EXTRA, DATA);
								sendBroadcast(intent);
*/
								try {
									thing.sendData("6");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
							else if (mati2 < -error2) {
						/*		Intent intent = new Intent(SEND_DATA_INTENT);
								intent.putExtra(DATA_EXTRA, DATA1);
								sendBroadcast(intent);
								*/
								try {
									thing.sendData("7");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							

							
						
						try {
							Thread.sleep(400);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}).start();


			}
			// mRgba = mIntermediateMat;
			runOnUiThread(new Runnable() {

				public void run() {
					// HERE I UPDATE TEXT VIEW
					TextView txtView = (TextView) findViewById(R.id.text_id);
					txtView.setText(String.valueOf(mati));

				}
			});

			Mat colorLabel = mRgba.submat(4, 68, 4, 68);
			colorLabel.setTo(mBlobColorRgba);

			Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70,
					70 + mSpectrum.cols());
			mSpectrum.copyTo(spectrumLabel);
		}

		return mRgba;
	}

	private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
		Mat pointMatRgba = new Mat();
		Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
		Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL,
				4);

		return new Scalar(pointMatRgba.get(0, 0));
	}

	private void ShowTitle(String s, int iLineNum, Scalar color) {
		Core.putText(mRgba, s, new Point(10,
				(int) (dTextScaleFactor * 60 * iLineNum)),
				Core.FONT_HERSHEY_SIMPLEX, dTextScaleFactor, color, 2);
	}
}