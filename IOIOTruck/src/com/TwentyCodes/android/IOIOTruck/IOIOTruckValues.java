/**
 * IOIOTruckValues.java
 * @date Jan 21, 2012
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.IOIOTruck;

import com.TwentyCodes.android.ioio.IOIOValues;

/**
 * This class is used to hold values for driving an RC truck 
 * @author ricky barrette
 */
public class IOIOTruckValues extends IOIOValues {
	
	/**
	 * stop the truck
	 * PWM value
	 */
	public static final int DRIVE_STOP = 1500;
	
	/**
	 * drive the truck forward
	 * PWM value
	 */
	public static final int DRIVE_FORWARD = 1300;
	
	/**
	 * drive the truck in reverse
	 * PWM value
	 */
	public static final int DRIVE_REVERSE = 1700;

	/**
	 * IOIO port to drive the speed controller
	 */
	public static final int DRIVE_PORT = 3;
	
	/**
	 * IOIO port to drive the shifter servo
	 */
	public static final int SHIFTER_PORT = 4;
	
	/**
	 * IOIO port to drive the steering servo
	 */
	public static final int STEER_PORT = 5;

	/**
	 * steers the truck straight
	 * PWM value
	 */
	public static final int STEER_STRAIGHT = 1500; 
	
	/**
	 * steers the truck left
	 * PWM value
	 */
	public static final int STEER_LEFT = 1400;
	
	/**
	 * steers the truck right
	 * PWM value
	 */
	public static final int STEER_RIGHT = 1600;
	
	/**
	 * 
	 * shifts truck into first
	 * PWM value
	 */
	public static final int SHIFT_FIRST = 1500; 
	
	/**
	 * shifts the truck into second
	 * TODO verify value
	 * PWM value
	 */
	public static final int SHIFT_SECOND = 1000;
	
	/**
	 * shifts the truck into 3rd
	 * PWM value
	 */
	public static final int SHIFT_THRID = 2000;

}
