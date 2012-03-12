/**
 * TestActivity.java
 * @date Jan 7, 2012
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

import android.app.Activity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickView;
import com.TwentyCodes.android.IOIOTruck.IOIOTruckConnectionManager.IOIOTruckThreadListener;
import com.TwentyCodes.android.exception.ExceptionHandler;

/**
 * This is a simple testing activity to test to main functions of the truck.
 * These functions are:
 *  + drive forward /reverse
 *  + steer left / right
 *  + shift 1st, 2nd, 3rd
 *  + status LED on / off
 * @author ricky barrette
 */
public class TestActivity extends Activity implements JoystickMovedListener, OnSeekBarChangeListener, OnCheckedChangeListener, IOIOTruckThreadListener {
	private static final String TAG = "TestActivity";
	private TextView mStatusTextView;
	private TextView mDriveTextView;
	private TextView mSteerTextView;
	private TextView mShifterTextView;
	private IOIOTruckConnectionManager mIOIOManager;

	/**
	 * Called when the led swich is toggled
	 * (non-Javadoc)
	 * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton, boolean)
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			mIOIOManager.setStatLedEnabled(isChecked);
	}

	/**
	 * Called when the activity is first created
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		setContentView(R.layout.test_activity);
		
		mStatusTextView = (TextView) findViewById(R.id.title);
		
		Switch ledSwitch = (Switch) findViewById(R.id.led_switch);
		ledSwitch.setOnCheckedChangeListener(this);
		
		SeekBar seekBar = (SeekBar) findViewById(R.id.skeekbar1);
		seekBar.setOnSeekBarChangeListener(this);
		
		JoystickView joyStick = (JoystickView) findViewById(R.id.joystick);
		joyStick.setOnJostickMovedListener(this);
		joyStick.setMovementRange(500);
		
		mDriveTextView = (TextView) findViewById(R.id.tv_drive);
		mSteerTextView = (TextView) findViewById(R.id.tv_steer);
		mShifterTextView = (TextView) findViewById(R.id.tv_shifter);
		
		mDriveTextView.setText(getString(R.string.drive)+1500);
		mSteerTextView.setText(getString(R.string.steer)+1500);
		mShifterTextView.setText(getString(R.string.shifter)+1500);
		mIOIOManager = new IOIOTruckConnectionManager(this, this);
		mIOIOManager.getIOIOAndroidApplicationHelper().create();
	}

	/**
	 * (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		mIOIOManager.getIOIOAndroidApplicationHelper().destroy();
		super.onDestroy();
	}

	/**
	 * Called when the IOIOTruckThread has a log to publish
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.IOIOTruck.IOIOTruckConnectionManager.IOIOTruckThreadListener#onLogUpdate(java.lang.String)
	 */
	@Override
	public void onLogUpdate(String log) {
		mStatusTextView.setText(log);
	}

	/**
	 * called when the joystick has been moved
	 * (non-Javadoc)
	 * @see com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener#OnMoved(int, int)
	 */
	@Override
	public void OnMoved(int pan, int tilt) {
		int drive = tilt + 1500;
		int steer = pan + 1500;
		mIOIOManager.setDriveValue(drive);
		mIOIOManager.setSteerValue(steer);
		mDriveTextView.setText(getString(R.string.drive)+mIOIOManager.getDriveValue());//drive);
		mSteerTextView.setText(getString(R.string.steer)+mIOIOManager.getSteerValue());//steer);
	}

	/**
	 * Called when the shifter seekbar is adjusted
	 * (non-Javadoc)
	 * @see android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android.widget.SeekBar, int, boolean)
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		float shifter = progress + 1000;
		mIOIOManager.setShifterValue(progress + 1000);
		
		mShifterTextView.setText(getString(R.string.shifter)+ shifter);
	}

	/**
	 * Called when the joystick is released
	 * (non-Javadoc)
	 * @see com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener#OnReleased()
	 */
	@Override
	public void OnReleased() {
		//NOT USED
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
	 * Called when the joystick ir returned to center
	 * (non-Javadoc)
	 * @see com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener#OnReturnedToCenter()
	 */
	@Override
	public void OnReturnedToCenter() {
		//All Stop
		mIOIOManager.setDriveValue(IOIOTruckValues.DRIVE_STOP);
		mIOIOManager.setSteerValue(IOIOTruckValues.STEER_STRAIGHT);
	}

	/**
	 * (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		mIOIOManager.getIOIOAndroidApplicationHelper().start();
		super.onStart();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		//NOT USED
	}

	/**
	 * (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		mIOIOManager.getIOIOAndroidApplicationHelper().stop();
		super.onStop();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		//NOT USED
	}
}