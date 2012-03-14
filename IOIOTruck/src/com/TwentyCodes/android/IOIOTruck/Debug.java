/**
 * Debug.java
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

/**
 * This class will hold constants used for debuging
 * @author ricky barrette
 */
public class Debug {
	
	/**
	 * Allows the application to print to the system log
	 */
	public static final boolean DEBUG = true;
	
	/**
	 * Sets the size of the radius of a user selected point in km
	 */
	public static final float RADIUS = 0.009144f;

	/**
	 * Sets the amount of kilometers that a radius needs to be penetrated by an accuracy circle in km
	 */
	public static final float FUDGE_FACTOR = .005f;
		
}