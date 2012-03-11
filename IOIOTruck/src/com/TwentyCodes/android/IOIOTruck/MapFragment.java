/**
 * MapFragment.java
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

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.util.Log;

import com.TwentyCodes.android.fragments.UserOverlayMapFragment;
import com.TwentyCodes.android.location.MapView;
import com.TwentyCodes.android.location.OnLocationSelectedListener;
import com.TwentyCodes.android.overlays.DirectionsOverlay;
import com.TwentyCodes.android.overlays.DirectionsOverlay.OnDirectionsCompleteListener;
import com.TwentyCodes.android.overlays.RadiusOverlay;
import com.google.android.maps.GeoPoint;

/**
 * This map fragment will maintain a map view and all its functions
 * 
 * Specifically this map view will allow user to select a point on the map via RadiusOverlay
 * @author ricky barrette
 */
public class MapFragment extends UserOverlayMapFragment implements OnLocationSelectedListener, OnDirectionsCompleteListener {
	
	private final String TAG = "MapFragment";
	private RadiusOverlay mRadiusOverlay;
	private OnLocationSelectedListener mLocationSelectedListener;
	private OnDirectionsCompleteListener mDirectionsCompleteListener;
	private DirectionsOverlay mDirectionsOverlay;

	/**
	 * Creates a new MapFragment
	 * @author ricky barrette
	 */
	public MapFragment() {
		super();
	}

	/**
	 * Called whrn the directions overlay is finished getting the directions.
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.overlays.DirectionsOverlay.OnDirectionsCompleteListener#onDirectionsComplete(com.TwentyCodes.android.overlays.DirectionsOverlay)
	 */
	@Override
	public void onDirectionsComplete(DirectionsOverlay directionsOverlay) {
		mDirectionsOverlay = directionsOverlay;
		if(mDirectionsCompleteListener != null)
			mDirectionsCompleteListener.onDirectionsComplete(directionsOverlay);
	}

	/**
	 * Called when a point is selected on the map 
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.location.LocationSelectedListener#onLocationSelected(com.google.android.maps.GeoPoint)
	 */
	@Override
	public void onLocationSelected(final GeoPoint point) {
		
		if(mDirectionsCompleteListener != null)
			new Thread( new Runnable(){
				@Override
				public void run(){
					try {
						new DirectionsOverlay(getMap(), getUserLocation(), point, MapFragment.this);
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
		
		removePath();
		
		setDestination(point);
		
		if(mLocationSelectedListener != null)
			mLocationSelectedListener.onLocationSelected(point);
		
		if(point != null){
			if(Debug.DEBUG)
				Log.d(TAG, "onLocationSelected() "+ point.toString());

			if(this.mRadiusOverlay != null)
				this.mRadiusOverlay.setLocation(point);
			
		} else if(Debug.DEBUG)
			Log.d(TAG, "onLocationSelected() Location was null");
	}
	
	/**
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.location.UserOverlayMapFragment#onMapViewCreate(com.TwentyCodes.android.location.MapView)
	 */
	@Override
	public void onMapViewCreate(MapView map) {
		mRadiusOverlay = new RadiusOverlay();
		mRadiusOverlay.setLocationSelectedListener(this);
		
		map.getOverlays().add(mRadiusOverlay);
		super.onMapViewCreate(map);
	}
	
	/**
	 * Removes the path if displayed 
	 * @author ricky barrette
	 */
	public void removePath(){
		if(mDirectionsOverlay != null)
			mDirectionsOverlay.removePath();
	}
	
	/**
	 * sets the distener for the directions overlay
	 * @param listener
	 * @author ricky barrette
	 */
	public void setDirectionsCompleteListener(OnDirectionsCompleteListener listener){
		mDirectionsCompleteListener = listener;
	}
	
	/**
	 * @param listener
	 * @author ricky barrette
	 */
	public void setLocationSelectedListener(OnLocationSelectedListener listener){
		mLocationSelectedListener = listener;
	}

	/**
	 * @param radius meters
	 * @author ricky barrette
	 */
	public void setRadius(int radius){
		mRadiusOverlay.setRadius(radius);
	}
	
	/**
	 * @param color
	 * @author ricky barrette
	 */
	public void setRadiusColor(int color){
		mRadiusOverlay.setColor(color);
	}
}
