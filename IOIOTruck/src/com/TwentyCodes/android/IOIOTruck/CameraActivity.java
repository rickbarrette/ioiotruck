/**
 * CameraActivity.java
 * @date Jan 29, 2012
 * @author ricky barrette
 * @author Twenty Codes, LLC
 * 
 * Copyright 2012 Richard Barrette 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License
 */
package com.TwentyCodes.android.IOIOTruck;

import com.TwentyCodes.android.IOIOTruck.IOIOTruckConnectionManager.IOIOTruckThreadListener;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

/**
 * This activity will be used to test the object avoidance algorithms
 * 
 * TODO
 *  + drive the robot forward
 *  + steer the robot to avoid object in front of it
 * @author ricky barrette
 */
public class CameraActivity extends FragmentActivity implements IOIOTruckThreadListener {

	private static final String TAG = "CameraActivity";
	private IOIOTruckConnectionManager mIOIOManager;
	private WakeLock mWakeLock;
	private TextView mLogTextView;

	/**
	 * Called when the activity is being created
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.camera_activity);
		mLogTextView = (TextView) findViewById(R.id.log_textView);
		mIOIOManager = new IOIOTruckConnectionManager(this, this);
		mIOIOManager.getIOIOAndroidApplicationHelper().create();
	}

	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		mIOIOManager.getIOIOAndroidApplicationHelper().destroy();
		super.onDestroy();
	}

	/**
	 * Called when the IOIO Manager wants to log something
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.IOIOTruck.IOIOTruckConnectionManager.IOIOTruckThreadListener#onLogUpdate(java.lang.String)
	 */
	@Override
	public void onLogUpdate(String log) {
		mLogTextView.setText(log);
	}

	/**
	 * Called when the activity is pausing
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if(mWakeLock.isHeld())
			mWakeLock.release();
	}

	/**
	 * (non-Javadoc)
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		mIOIOManager.getIOIOAndroidApplicationHelper().restart();
		super.onRestart();
	}

	/**
	 * Called when the activity is resuming
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
		mWakeLock.acquire();
		super.onResume();
	}

	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onStart()
	 */
	@Override
	protected void onStart() {
		mIOIOManager.getIOIOAndroidApplicationHelper().start();
		super.onStart();
	}

	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onStop()
	 */
	@Override
	protected void onStop() {
		mIOIOManager.getIOIOAndroidApplicationHelper().stop();
		super.onStop();
	}

}
