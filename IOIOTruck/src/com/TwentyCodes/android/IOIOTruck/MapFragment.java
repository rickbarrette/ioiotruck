/**
 * MapFragment.java
 * @date Jan 7, 2012
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.IOIOTruck;

import android.util.Log;

import com.TwentyCodes.android.location.LocationSelectedListener;
import com.TwentyCodes.android.location.MapView;
import com.TwentyCodes.android.location.RadiusOverlay;
import com.TwentyCodes.android.location.UserOverlayMapFragment;
import com.google.android.maps.GeoPoint;

/**
 * This map fragment will maintain a map view and all its functions
 * 
 * Specifically this map view will allow user to select a point on the map via RadiusOverlay
 * @author ricky barrette
 */
public class MapFragment extends UserOverlayMapFragment implements LocationSelectedListener {
	
	private final String TAG = "MapFragment";

	private RadiusOverlay mRadiusOverlay;
	private LocationSelectedListener mLocationSelectedListener;

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
	public void setLocationSelectedListener(LocationSelectedListener listener){
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