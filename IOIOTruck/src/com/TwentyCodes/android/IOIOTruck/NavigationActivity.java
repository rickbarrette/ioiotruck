/**
 * NavigationActivity.java
 * @date Jan 7, 2012
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.IOIOTruck;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.TwentyCodes.android.IOIOTruck.IOIOTruckManager.IOIOTruckThreadListener;
import com.TwentyCodes.android.location.CompassListener;
import com.TwentyCodes.android.location.GeoPointLocationListener;
import com.TwentyCodes.android.location.GeoUtils;
import com.TwentyCodes.android.location.LocationSelectedListener;
import com.google.android.maps.GeoPoint;


/**
 * This activity will be used to interact with the IOIO and setup autonomous routines
 * 
 * The end goal of this activity is to:
 *  + have the user select a point on the map
 *  + have the IOIO drive the rc truck to the point on the map when the user starts the autonomous routine
 *  
 * TODO
 *  + drive the truck forward or reverse to best navigate to the selected point
 * @author ricky barrette
 */
public class NavigationActivity extends FragmentActivity implements CompassListener, GeoPointLocationListener, LocationSelectedListener, OnClickListener, OnCheckedChangeListener, IOIOTruckThreadListener {
	
	private static final String TAG = "NavigationActivity";
	private IOIOTruckManager mIOIOManager;
	private MapFragment mMap;
	private TextView mLog;
	private GeoPoint mPoint;
	private ProgressBar mProgress;
	private int mMaxDistance = 0; //meters
	private boolean isRunning = false;
	private Button mGoButton;
	private float mBearing;
	private ScrollView mScrollView;

	private boolean isScrollingEnabled = true;
	private int mDistance;
	private LogUpdater mLoggerThread;
	private TextView mAccuracyTextView;
	private TextView mLastUpdateTextView;
	private long mLast;
	private WakeLock mWakeLock;
	
	/**
	 * This thread will be used to update all the informational displays
	 * @author ricky barrette
	 */
	class LogUpdater extends Thread {
		
		private boolean isAborted;

		/**
		 * aborts the thread 
		 * @author ricky barrette
		 */
		public void abort() {
			isAborted = true;
		}
		
		@Override
		public void run(){
			while (true) {
					if (isAborted) 
						break;
					updateLog("\nDistance: "+ mDistance +getString(R.string.m)
							+"\nDrive: "+mIOIOManager.getDriveValue()
							+"\nSteering: "+mIOIOManager.getSteerValue()
							+"\nBearing: "+mBearing
							+"\nisRunning: "+isRunning);
					
					updateLastUpdateTextView();
					
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		/**
		 * updates the last update textview 
		 * thread safe
		 * @author ricky barrette
		 */
		private void updateLastUpdateTextView() {
			runOnUiThread( new Runnable(){
				@Override
				public void run(){
					mLastUpdateTextView.setText((System.currentTimeMillis() - mLast) +getString(R.string.ms));
				}
			});
			
		}
			
	}

	/**
	 * Called when the scrolling switch is checked
	 * (non-Javadoc)
	 * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton, boolean)
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		isScrollingEnabled = isChecked;
	}

	/**
	 * Called when a button is clicked
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.go_button:
				updateGoButton();
				break;
				
			case R.id.mark_my_lcoation_button:
				GeoPoint point = mMap.getUserLocation();
				
				if(point != null){
					mMap.onLocationSelected(point);
				} else
					Toast.makeText(this, R.string.no_gps_signal, Toast.LENGTH_SHORT).show();
				break;
				
			case R.id.my_location_button:
				GeoPoint user = mMap.getUserLocation();
				
				if(user != null){
					mMap.setMapCenter(user);
				} else
					
				break;
		}
	}

	/**
	 * updates the go/stop button based on isRunning
	 * thread safe 
	 * @author ricky barrette
	 */
	private void updateGoButton() {
		updateGoButton(isRunning);
	}
	
	/**
	 * Sets the go/stop button to the provided value
	 * thread safe
	 * @param isRunnuing true = stop, false = go
	 * @author ricky barrette
	 */
	private void updateGoButton(final boolean isRun) {
		mIOIOManager.setStatLedValue(!isRun);
		runOnUiThread(new Runnable() {
			@Override
			public void run(){
				if(isRun){
					mGoButton.setText(R.string.go);
					isRunning = false;
					updateLog(R.string.stop);
					if(mLoggerThread != null)
						mLoggerThread.abort();
				} else {
					mGoButton.setText(R.string.stop);
					isRunning = true;
					updateLog(R.string.go);
					mLoggerThread = new LogUpdater();
					mLoggerThread.start();
				}
			}
		});
	}

	/**
	 * Called when there is an update from the compass
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.location.CompassListener#onCompassUpdate(float)
	 */
	@Override
	public void onCompassUpdate(float bearing) {
		bearing = GeoUtils.calculateBearing(mMap.getUserLocation(), mPoint, bearing);

		if(bearing > 355 || bearing < 5)
			mIOIOManager.setSteerValue(IOIOTruckValues.STEER_STRAIGHT);
		
		if(bearing < 355 && bearing > 180)
			mIOIOManager.setSteerValue(IOIOTruckValues.STEER_RIGHT);
		
		if(bearing < 180 && bearing > 5)
			mIOIOManager.setSteerValue(IOIOTruckValues.STEER_LEFT);
		
		mBearing = bearing;
	}

	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.nav_activity);
		/*
		 * init UI
		 */
		mLog = (TextView) findViewById(R.id.log_textView);
		mGoButton = (Button) findViewById(R.id.go_button);
		mProgress = (ProgressBar) findViewById(R.id.progressBar);
		mScrollView = (ScrollView) findViewById(R.id.scrollview);
		Switch scrollSwitch = (Switch) findViewById(R.id.scrolling_switch);
		mAccuracyTextView = (TextView) findViewById(R.id.accuracy_textview);
		mLastUpdateTextView = (TextView) findViewById(R.id.time_textview);

		/*
		 * init listeners
		 */
		scrollSwitch.setOnCheckedChangeListener(this);
		mGoButton.setOnClickListener(this);
		findViewById(R.id.mark_my_lcoation_button).setOnClickListener(this);
		findViewById(R.id.my_location_button).setOnClickListener(this);
	}
	
	/**
	 * Called when android's location services have an update
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.location.GeoPointLocationListener#onLocationChanged(com.google.android.maps.GeoPoint, int)
	 */
	@Override
	public void onLocationChanged(GeoPoint point, int accuracy) {
		mLast = System.currentTimeMillis();
		mAccuracyTextView.setText(accuracy+getString(R.string.m));
		
		mDistance = updateProgress(point);
		
		/*
		 * here we will update the progress bar
		 * 
		 */
		if(mPoint != null)
			if(GeoUtils.isIntersecting(point, (float) (accuracy / 1E3), mPoint, Debug.RADIUS, Debug.FUDGE_FACTOR)) {
				Log.v(TAG, "Dest Reached, Stopping");
				mIOIOManager.setDriveValue(IOIOTruckValues.DRIVE_STOP);
				updateGoButton(true);
				updateLog(R.string.dest_reached);
			} else {
				Log.v(TAG, "Driving Forward");
				mIOIOManager.setDriveValue(IOIOTruckValues.DRIVE_FORWARD);
			}
		else{
			Log.v(TAG, "Lost GPS signal, stopping");
			mIOIOManager.setDriveValue(IOIOTruckValues.DRIVE_STOP);
		}

	}
	
	/**
	 * Called when the user selects a point for the truck to drive to
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.location.LocationSelectedListener#onLocationSelected(com.google.android.maps.GeoPoint)
	 */
	@Override
	public void onLocationSelected(GeoPoint point) {
		mPoint = point;
		mDistance = updateProgress(mMap.getUserLocation());
		updateLog(getString(R.string.point_selected)+point.toString());
	}

	/**
	 * Called when the application is paused. We want to disconnect with the
	 * IOIO at this point, as the user is no longer interacting with our
	 * application.
	 * @author ricky barrette
	 */
	@Override
	protected void onPause() {
		try {
			mIOIOManager.abort();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(mLoggerThread != null)
			mLoggerThread.abort();
		
		if(mWakeLock.isHeld())
			mWakeLock.release();
		super.onPause();
	}

	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mMap = (MapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map_fragment);
		mMap.setCompassListener(this);
		mMap.setGeoPointLocationListener(this);
		mMap.setLocationSelectedListener(this);
		mMap.setRadius((int) (Debug.RADIUS * 1E3));
		mIOIOManager = new IOIOTruckManager(this, this);
		mIOIOManager.start();
		
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
		mWakeLock.acquire();
	}
	
	/**
	 * updates the log with the provided string res
	 * thread safe
	 * @param resId
	 */
	private void updateLog(final int resId) {
		updateLog("\n"+getString(resId));
	}

	/**
	 * updates the log with the provided string
	 * thread safe 
	 * @param id The string ID of the message to present.
	 */
	private void updateLog(final String log) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mLog.append("\n"+log);
				
				/*
				 * Scroll the scroll view down
				 */
				if(isScrollingEnabled)
					mScrollView.scrollTo(0, mLog.getHeight());
			}
		});
		
	}

	/**
	 * updates the progress bar
	 * it will roughly show the progress of the truck
	 * 
	 * less = closer
	 * more = farther 
	 * @param point dest
	 * @return distance meters
	 * @author ricky barrette
	 */
	private int updateProgress(GeoPoint point) {
		int distance = (int) (GeoUtils.distanceKm(point, mPoint) * 1000);
		if (distance > mMaxDistance) {
			mMaxDistance = distance;
			mProgress.setMax(distance);
		}		
		mProgress.setProgress(distance);
		return distance;
	}

	/**
	 * Called when the IOIOTruckThread has a log it wants to display
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.IOIOTruck.IOIOTruckManager.IOIOTruckThreadListener#onLogUpdate(java.lang.String)
	 */
	@Override
	public void onLogUpdate(String log) {
		updateLog(log);
	}
	
}