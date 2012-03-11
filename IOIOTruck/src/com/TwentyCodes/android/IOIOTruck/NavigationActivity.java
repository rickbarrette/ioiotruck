/**
 * NavigationActivity.java
 * @date Jan 7, 2012
 * @author ricky barrette
 * @author Twenty Codes, LLC
 * 
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

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
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
import com.TwentyCodes.android.location.OnLocationSelectedListener;
import com.TwentyCodes.android.overlays.DirectionsOverlay;
import com.TwentyCodes.android.overlays.DirectionsOverlay.OnDirectionsCompleteListener;
import com.TwentyCodes.android.overlays.PathOverlay;
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
public class NavigationActivity extends FragmentActivity implements CompassListener, GeoPointLocationListener, OnLocationSelectedListener, OnClickListener, OnCheckedChangeListener, IOIOTruckThreadListener, OnDirectionsCompleteListener {
	
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
		public synchronized void abort() {
			isAborted = true;
		}
		
		@Override
		public void run(){
			while (!isAborted) {
					updateLog("\nDistance: "+ mDistance +getString(R.string.m)
							+"\nDrive: "+mIOIOManager.getDriveValue()
							+"\nSteering: "+mIOIOManager.getSteerValue()
							+"\nBearing: "+mBearing
							+"\nisRunning: "+isRunning);
							if(mPoints != null)
								updateLog("Point = "+mIndex +" of "+ mPoints.size());
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
	private static final String TAG = "NavigationActivity";
	private IOIOTruckManager mIOIOManager;
	private MapFragment mMap;
	private TextView mLog;
	private ProgressBar mProgress;
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
	private int mCount = 0;
	private ArrayList<GeoPoint> mPoints;
	private int mIndex = 0;
	private GeoPoint mDestPoint;
	private ArrayList<PathOverlay> mWayPoints;

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

			case R.id.map_button:
				mMap.changeMapMode();
				break;
				
			case R.id.mark_my_lcoation_button:
				final GeoPoint point = mMap.getUserLocation();
				
				if(point != null){
					mMap.onLocationSelected(point);
				} else
					Toast.makeText(this, R.string.no_gps_signal, Toast.LENGTH_SHORT).show();
				break;
				
			case R.id.my_location_button:
				final GeoPoint user = mMap.getUserLocation();
				
				if(user != null){
					mMap.setMapCenter(user);
				} else
					
				break;
				
		}
	}

	/**
	 * Called when there is an update from the compass
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.location.CompassListener#onCompassUpdate(float)
	 */
	@Override
	public void onCompassUpdate(float bearing) {
		bearing = GeoUtils.calculateBearing(mMap.getUserLocation(), mMap.getDestination(), bearing);

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
		findViewById(R.id.map_button).setOnClickListener(this);
	}

	/**
	 * called when the directions overlay is generated
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.overlays.DirectionsOverlay.OnDirectionsCompleteListener#onDirectionsComplete(com.TwentyCodes.android.overlays.DirectionsOverlay)
	 */
	@Override
	public void onDirectionsComplete(DirectionsOverlay directionsOverlay) {
		ArrayList<PathOverlay> path = directionsOverlay.getPath();
		
		if(path.size() > 0){
			mWayPoints = new ArrayList<PathOverlay>();
			ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();
			points.add(path.get(0).getStartPoint());
			for(PathOverlay item : path)
				if(item.getEndPoint() != null) {
					points.add(item.getEndPoint());
					mWayPoints.add(new PathOverlay(item.getEndPoint(), 5, Color.GRAY));
				}
		
			mPoints = points;
			mMap.setDestination(points.get(0));
			mWayPoints.add(new PathOverlay(points.get(0), 5, Color.MAGENTA));
			mMap.getMap().getOverlays().addAll(mWayPoints);
			mWayPoints.addAll(path);
		}
	}

	@Override
	public void onFirstFix(boolean isFirstFix) {
		mMap.disableGPSProgess();
	}
	
	/**
	 * Called when android's location services have an update
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.location.GeoPointLocationListener#onLocationChanged(com.google.android.maps.GeoPoint, int)
	 */
	@Override
	public synchronized void onLocationChanged(final GeoPoint point, final int accuracy) {
		mLast = System.currentTimeMillis();
		mAccuracyTextView.setText(accuracy+getString(R.string.m));
		mDistance = updateProgress(point);
		final GeoPoint currentDest = mMap.getDestination();
		
		/*
		 * if we have a destination, check to see if we are there yet
		 * if we are then increment mCount
		 */
		if(point != null)
			if(currentDest != null)
				
				
				/*
				 * are we closer than 15 feet?
				 */
				if (GeoUtils.distanceKm(point, currentDest) < 0.009144) {
//				if(GeoUtils.isIntersecting(point, (float) (accuracy / 1E3), currentDest, Debug.RADIUS, Debug.FUDGE_FACTOR)) {
					updateLog("Count = "+ (++mCount));
					/*
					 * if we get 6 positives, we are problay at our waypoint/dest
					 */
					if(mCount == 6){
						
						mCount = 0;
						
						/*
						 * if the points list is null, or there are no more waypoints
						 */
						if(mPoints == null || mIndex == mPoints.size()){
							mIOIOManager.setDriveValue(IOIOTruckValues.DRIVE_STOP);
							updateGoButton(true);
							updateLog(R.string.dest_reached);
							mMap.setDestination(null);
						} else {
								updateLog("Index = " + (++mIndex));
							
							/*
							 * if there are more waypoints, then move on to the next
							 * otherwise move on to the dest
							 */
							if(mIndex < mPoints.size()) {
								updateLog("Waypoint reached, moving to next");
								mMap.setDestination(mPoints.get(mIndex));
							} else {
								updateLog("last Waypoint reached, moving to dest");
								mMap.setDestination(mDestPoint);
							}
							
							updateLog("New dest = "+ mMap.getDestination().toString());
						}
					}
					
				} else {
					Log.v(TAG, "Driving Forward");
					mCount = 0;
					mIOIOManager.setDriveValue(IOIOTruckValues.DRIVE_FORWARD);
				}
		else {
			updateLog("Lost GPS signal (point was null), stopping");
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
		if(mWayPoints != null)
			mMap.getMap().getOverlays().removeAll(mWayPoints);
		mDestPoint = point;
		mDistance = updateProgress(mMap.getUserLocation());
		updateLog(getString(R.string.point_selected)+point.toString());
		mIndex = 0;
		mCount = 0;
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
		mMap.setDirectionsCompleteListener(this);
		mMap.setRadius((int) (Debug.RADIUS * 1E3));
		mMap.enableGPSProgess();
		mIOIOManager = new IOIOTruckManager(this, this);
		mIOIOManager.start();
		
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
		mWakeLock.acquire();
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
		mIOIOManager.setStatLedEnabled(!isRun);
		runOnUiThread(new Runnable() {
			@Override
			public void run(){
				if(isRun){
					mCount = 0;
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
		int distance = (int) (GeoUtils.distanceKm(point, mMap.getDestination()) * 1000);
		if (distance > mProgress.getMax())
			mProgress.setMax(distance);
		mProgress.setProgress(distance);
		return distance;
	}
}