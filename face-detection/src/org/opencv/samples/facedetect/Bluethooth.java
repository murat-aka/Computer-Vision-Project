package org.opencv.samples.facedetect;

/**
 * Created by murat on 02/03/14.
 */


        import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.content.Context;




        import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

        import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
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

        import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class Bluethooth extends FdActivity
{


    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    
    FdActivity tstActivity;

    volatile boolean stopWorker;

    
    
    private static final String    TAG                 = "BLUETOOTH";


    public void onBlue(Context cccc)
    {





            findBT(cccc);
            try
            {
                openBT();
               
            }catch (IOException ex) { }


     /*   try
        {
            openBT();
            sendData();
        }
        catch (IOException ex) { }


*/




    }

    void findBT(Context cccc)
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      /*  if(mBluetoothAdapter == null)
        {
            myLabel.setText("No bluetooth adapter available");
        }
*/
       

      //  tstActivity = new FdActivity();
      //  tstActivity.Toast();
        
     /*   Toast toast = Toast.makeText(this,"Turned on", Toast.LENGTH_LONG);
        toast.show();
           */
        
    
       
        if(!mBluetoothAdapter.isEnabled())
        {
        	
        	  
        	  
        	Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	//startActivityForResult(turnOn, 0);
        	
        	
       	
         mBluetoothAdapter.enable();
        	
        	
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("HC-06"))
                {
                    mmDevice = device;
                    break;
                }
            }
        }

        
     Toast.makeText(cccc,
               "Your Message", Toast.LENGTH_LONG).show();

        
/*
        runOnUiThread(new Runnable() {

           public void run() {
                //HERE I UPDATE TEXT VIEW
                    myLabel2 = (TextView) findViewById(R.id.textView1);
       myLabel2.setText("Bluetooth Device Found");

           //    Toast.makeText(getApplicationContext(),
               //         "Your Message", Toast.LENGTH_LONG).show();


            }


        });
*/
        Log.i(TAG, "Bluetooth Device Found");

        
      //  myLabel2 = (TextView) findViewById(R.id.textView1);
      // myLabel2.setText("Bluetooth Device Found");

    }

    private Activity getActivity() {
		// TODO Auto-generated method stub
		return null;
	}

	void openBT() throws IOException
    {

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();


        
     //   myLabel.setText("Bluetooth Opened");
        
      
    }



    void sendData(String msg) throws IOException
    {
    	
    	try {
    		mmOutputStream.write(msg.getBytes());
            Log.i(TAG, "Sending bytes");
    		} catch (Exception e) { 
    			
    		        e.printStackTrace();
    		        
    		}
   
     // mmOutputStream.write(murat.getBytes());
     //   myLabel.setText("Data Sent");


    }

    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
      //  myLabel.setText("Bluetooth Closed");
    }
}
