/**
 * IOIOTruckThread.java
 * @date Jan 11, 2012
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.IOIOTruck;

import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import android.app.Activity;
import android.util.Log;

import com.TwentyCodes.android.IOIOTruck.R;
import com.TwentyCodes.android.ioio.IOIOManager;

/**
 * This IOIO thread will be used to drive a rc truck
 * @author ricky barrette
 */
public class IOIOTruckManager extends IOIOManager {

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
	private PwmOutput mDrive;
	private PwmOutput mSteer;
	private PwmOutput mShifter;
	private int mDriveValue;
	private int mSteerValue;
	private int mShifterValue;
	private boolean mStatLedValue;
	private boolean isTank = false;
	
	/**
	 * Creates a new IOIOTruckThread
	 * @param activity
	 * @param listener
	 * @author ricky barrette
	 */
	public IOIOTruckManager(Activity activity, IOIOTruckThreadListener listener){
		super();
		mActivity = activity;
		mListener = listener;
		updateLog(R.string.wait_ioio);
	}
	
	private void arcadeDrive() {	
		int left, right;	
		left = (mDriveValue + mSteerValue) /2;
		right = (mDriveValue - mSteerValue) / 2;
		
		mDriveValue = left;
		mSteerValue = right + 1500;
	    Log.d(TAG, "left: "+ mDriveValue + " right: "+ mSteerValue);
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
	 * @return the mStatLedValue
	 */
	public synchronized boolean isStatLedValue() {
		return mStatLedValue;
	}

	/**
	 * Here we register and initialize each port
	 * (non-Javadoc)
	 * @throws ConnectionLostException 
	 * @see com.TwentyCodes.android.ioio.IOIOThread#onConnected()
	 */
	@Override
	public void onConnected(IOIO ioio) throws ConnectionLostException {
		updateLog(R.string.ioio_connected);
		
		mDrive = ioio.openPwmOutput(IOIOTruckValues.DRIVE_PORT, IOIOTruckValues.RC_PWM_FRQ);
		mSteer = ioio.openPwmOutput(IOIOTruckValues.STEER_PORT, IOIOTruckValues.RC_PWM_FRQ);
		mShifter = ioio.openPwmOutput(IOIOTruckValues.SHIFTER_PORT, IOIOTruckValues.RC_PWM_FRQ);
	}

	/**
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.ioio.IOIOThread#onDisconnect()
	 */
	@Override
	public void onDisconnected() {
		updateLog(R.string.wait_ioio);
	}

	/**
	 * (non-Javadoc)
	 * @throws ConnectionLostException 
	 * @see com.TwentyCodes.android.ioio.IOIOThread#onUpdate()
	 */
	@Override
	public void loop() throws ConnectionLostException {
		
		this.setStatLedEnabled(mStatLedValue);
		
		if(isTank){
			arcadeDrive();
			mSteer.setPulseWidth(mSteerValue);
			mDrive.setPulseWidth(mDriveValue);
		}
		else {
			/*
			 * if the autonomous routine is running
			 * then drive the truck
			 * else stop the truck
			 */
			if(mStatLedValue)
				mDrive.setPulseWidth(mDriveValue);
			else
				mDrive.setPulseWidth(IOIOTruckValues.DRIVE_STOP);
		
		}
		mSteer.setPulseWidth(mSteerValue);
		mShifter.setPulseWidth(mShifterValue);

	}

	/**
	 * @param mDriveValue the mDriveValue to set
	 */
	public synchronized void setDriveValue(int mDriveValue) {
		this.mDriveValue = mDriveValue;
	}

	/**
	 * @param mShifterValue the mShifterValue to set
	 */
	public synchronized void setShifterValue(int mShifterValue) {
		this.mShifterValue = mShifterValue;
	}

	/**
	 * @param mStatLedValue the mStatLedValue to set
	 */
	public synchronized void setStatLedValue(boolean mStatLedValue) {
		this.mStatLedValue = mStatLedValue;
	}

	/**
	 * @param mSteerValue the mSteerValue to set
	 */
	public synchronized void setSteerValue(int mSteerValue) {
		this.mSteerValue = mSteerValue;
	}

	/**
	 * @param isTankDrive
	 * @author ricky barrette
	 */
	public synchronized void setTankDrive(boolean isTankDrive){
		isTank = isTankDrive;
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

}
