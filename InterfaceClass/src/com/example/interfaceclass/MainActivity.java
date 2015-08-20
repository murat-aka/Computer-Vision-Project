package com.example.interfaceclass;

import java.security.PublicKey;



import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		enableBT();
			
	}
	
	public void onclick(View view){
		
		Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("org.opencv.samples.facedetect");
		startActivity(LaunchIntent);
	}
	
	public void color(View view){
		
		Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("org.opencv.samples.colorblobdetect");
		startActivity(LaunchIntent);
	}
	
	public void enableBT(){
	    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	    if (!mBluetoothAdapter.isEnabled()){
	        Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); 
	        // The REQUEST_ENABLE_BT constant passed to startActivityForResult() is a locally defined integer (which must be greater than 0), that the system passes back to you in your onActivityResult() 
	        // implementation as the requestCode parameter. 
	        int REQUEST_ENABLE_BT = 1;
	        startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BT);
	        }
	  }
	
	  @Override
	    public void onPause()
	    {
	        super.onPause();
	     
	    }

	    @Override
	    public void onResume()
	    {
	        super.onResume();
	    }

	    public void onDestroy() {
	        super.onDestroy();
	          
	      BluetoothAdapter mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
		  mBlueAdapter.disable();
	        
	    }
	  
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
