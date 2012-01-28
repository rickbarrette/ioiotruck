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
import com.TwentyCodes.android.IOIOTruck.IOIOTruckManager.IOIOTruckThreadListener;
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
	private IOIOTruckManager mIOIOManager;

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
	}

	/**
	 * Called when the application is resumed (also when first started). Here is
	 * where we'll create our IOIO thread.
	 * @author ricky barrette
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mIOIOManager = new IOIOTruckManager(this, this);
		mIOIOManager.start();
	}

	/**
	 * Called when the application is paused. We want to disconnect with the
	 * IOIO at this point, as the user is no longer interacting with our
	 * application.
	 * @author ricky barrette
	 */
	@Override
	protected void onPause() {
		super.onPause();
		try {
			mIOIOManager.abort();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
	 * Called when the joystick is released
	 * (non-Javadoc)
	 * @see com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener#OnReleased()
	 */
	@Override
	public void OnReleased() {
		//NOT USED
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

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		//NOT USED
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		//NOT USED
	}

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
	 * Called when the IOIOTruckThread has a log to publish
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.IOIOTruck.IOIOTruckManager.IOIOTruckThreadListener#onLogUpdate(java.lang.String)
	 */
	@Override
	public void onLogUpdate(String log) {
		mStatusTextView.setText(log);
	}
}