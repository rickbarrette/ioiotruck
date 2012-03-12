/**
 * IOIOTruckThread.java
 * @date Jan 11, 2012
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

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.IOIOLooperProvider;
import ioio.lib.util.android.IOIOAndroidApplicationHelper;
import android.app.Activity;
import android.util.Log;

/**
 * This IOIO thread will be used to drive a rc truck
 * @author ricky barrette
 */
public class IOIOTruckConnectionManager implements IOIOLooper, IOIOLooperProvider {

	/**
	 * This listener will be used to notify the owner of this thread to update the onscreen log 
	 * @author ricky barrette
	 */
	public interface IOIOTruckThreadListener{
		public void onLogUpdate(String log);
	}

	private static final String TAG = "IOIOTruckThread";
	private Activity mActivity;
	private IOIOTruckThreadListener mListener;
	private int mDriveValue;
	private int mSteerValue;
	private int mShifterValue;
	private TB6612FNGMotorDriver mLeftMotor;
	private TB6612FNGMotorDriver mRightMotor;
	private PwmOutput mShifter;
	private DigitalOutput mMotorDriverStandBy;
	private DigitalInput mLeftFrontBumber;
	private DigitalInput mRightFrontBumber;
	private boolean isStatLedEnabled;
	private IOIOAndroidApplicationHelper mIOIOAndroidApplicationHelper;
//	private IOIO mIOIO;
	private DigitalOutput mStatLed;
	
	/**
	 * Creates a new IOIOTruckThread
	 * @param activity
	 * @param listener
	 * @author ricky barrette
	 */
	public IOIOTruckConnectionManager(Activity activity, IOIOTruckThreadListener listener){
		super();
		mIOIOAndroidApplicationHelper = new IOIOAndroidApplicationHelper(activity, this);
		mActivity = activity;
		mListener = listener;
		updateLog(R.string.wait_ioio);
	}
	
	/**
	 * Drives the robot based on the drive and steering values
	 * @throws ConnectionLostException
	 * @author ricky barrette
	 */
	private void arcadeDrive() throws ConnectionLostException {	
		int left, right;	
		left = (mDriveValue + mSteerValue) /2;
		right = (mDriveValue - mSteerValue) / 2;
		
		mLeftMotor.setSpeed(left);
		mRightMotor.setSpeed(right + 1500);
	}

	/**
	 * @return the mDriveValue
	 */
	public synchronized int getDriveValue() {
		return mDriveValue;
	}

	/**
	 * @return the mShifterValue
	 */
	public synchronized int getShifterValue() {
		return mShifterValue;
	}

	/**
	 * @return the mSteerValue
	 */
	public synchronized int getSteerValue() {
		return mSteerValue;
	}

	/**
	 * Here we register and initialize each port
	 * (non-Javadoc)
	 * @throws ConnectionLostException 
	 * @see com.TwentyCodes.android.ioio.IOIOThread#onConnected()
	 */
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException,InterruptedException {
		updateLog(R.string.ioio_connected);
//		mIOIO = ioio;
		
		mStatLed = ioio.openDigitalOutput(0, true);
		mShifter = ioio.openPwmOutput(IOIOTruckValues.SHIFTER_PORT, IOIOTruckValues.RC_PWM_FRQ);
		mLeftMotor = new TB6612FNGMotorDriver(ioio, IOIOTruckValues.MOTOR_DRIVER_PWMA, IOIOTruckValues.MOTOR_DRIVER_A1, IOIOTruckValues.MOTOR_DRIVER_A2);
		mRightMotor = new TB6612FNGMotorDriver(ioio, IOIOTruckValues.MOTOR_DRIVER_PWMB, IOIOTruckValues.MOTOR_DRIVER_B1, IOIOTruckValues.MOTOR_DRIVER_B2);
		
		//enable the motor driver
		mMotorDriverStandBy = ioio.openDigitalOutput(IOIOTruckValues.MOTOR_DRIVER_STANDBY, true);
		
		/*
		 * bumper switches in the front
		 */
		mLeftFrontBumber = ioio.openDigitalInput(IOIOTruckValues.LEFT_FRONT_BUMPER_PORT, DigitalInput.Spec.Mode.PULL_DOWN);
		mRightFrontBumber = ioio.openDigitalInput(IOIOTruckValues.RIGHT_FRONT_BUMBER_PORT, DigitalInput.Spec.Mode.PULL_DOWN);
	}

	/**
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.ioio.IOIOThread#onDisconnect()
	 */
	@Override
	public void disconnected() {
		updateLog(R.string.wait_ioio);
	}

	/**
	 * (non-Javadoc)
	 * @throws ConnectionLostException 
	 * @see com.TwentyCodes.android.ioio.IOIOThread#onUpdate()
	 */
	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		
		/*
		 * we need to check our sensors before we can make a move. 
		 */
		if(mLeftFrontBumber.read()){
			//TODO backup, spin right, drive forward
			isStatLedEnabled = false;
		} else if(mRightFrontBumber.read()){
			//TODO backup, spin left, drive forward
			isStatLedEnabled = false;
		}
		
//		mIOIO.beginBatch();
		
		mStatLed.write(!isStatLedEnabled);
		
		mShifter.setPulseWidth(mShifterValue);
		
		
		mMotorDriverStandBy.write(isStatLedEnabled);
		
		/*
		 * if the autonomous routine is running
		 * then drive the truck
		 * else stop the truck
		 */
		if(isStatLedEnabled){
			arcadeDrive();
		}
		else{				
			mLeftMotor.setSpeed(0);
			mRightMotor.setSpeed(0);
		}
		
//		mIOIO.endBatch();
	}

	/**
	 * @param mDriveValue the mDriveValue to set
	 */
	public synchronized void setDriveValue(int driveValue) {
		mDriveValue = driveValue;
	}

	/**
	 * @param mShifterValue the mShifterValue to set
	 */
	public synchronized void setShifterValue(int shifterValue) {
		mShifterValue = shifterValue;
	}

	/**
	 * @param mSteerValue the mSteerValue to set
	 */
	public synchronized void setSteerValue(int steerValue) {
		mSteerValue = steerValue;
	}

	/**
	 * updates the log listener in the UI thread 
	 * @param resId
	 * @author ricky barrette
	 */
	private void updateLog(final int resId) {
		if(mListener != null)
			mActivity.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					mListener.onLogUpdate(mActivity.getString(resId));
				}
			});
	}

	@Override
	public void incompatible() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IOIOLooper createIOIOLooper(String connectionType, Object extra) {
		Log.v(TAG, connectionType);
		return this;
	}

	/**
	 * @return the mIOIOAndroidApplicationHelper
	 */
	public IOIOAndroidApplicationHelper getIOIOAndroidApplicationHelper() {
		return mIOIOAndroidApplicationHelper;
	}

	/**
	 * set the status of the led
	 * @param b
	 * @author ricky barrette
	 */
	public void setStatLedEnabled(boolean b) {
		this.isStatLedEnabled = b;
	}
	
	/**
	 * @return state of status led
	 * @author ricky barrette
	 */
	public boolean isStatLedEnabled(){
		return this.isStatLedEnabled;
	}
}