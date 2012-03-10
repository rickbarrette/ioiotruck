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

import android.util.Log;

import com.TwentyCodes.android.location.MapView;
import com.TwentyCodes.android.location.OnLocationSelectedListener;
import com.TwentyCodes.android.overlays.RadiusOverlay;
import com.google.android.maps.GeoPoint;

/**
 * This map fragment will maintain a map view and all its functions
 * 
 * Specifically this map view will allow user to select a point on the map via RadiusOverlay
 * @author ricky barrette
 */
public class MapFragment extends com.TwentyCodes.android.fragments.UserOverlayMapFragment implements OnLocationSelectedListener {
	
	private final String TAG = "MapFragment";

	private RadiusOverlay mRadiusOverlay;
	private OnLocationSelectedListener mLocationSelectedListener;

	/**
	 * Creates a new MapFragment
	 * @author ricky barrette
	 */
	public MapFragment() {
		super();
	}

	/**
	 * Called when a point is selected on the map 
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.location.LocationSelectedListener#onLocationSelected(com.google.android.maps.GeoPoint)
	 */
	@Override
	public void onLocationSelected(GeoPoint point) {
		
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