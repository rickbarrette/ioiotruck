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
	 * IOIO port for the motor driver pwma port
	 */
	public static final int MOTOR_DRIVER_PWMA = 4;
	
	/**
	 * IOIO port for the motor driver a2 port
	 */
	public static final int MOTOR_DRIVER_A2 = 5;
	
	/**
	 * IOIO port for the motor driver a1 port
	 */
	public static final int MOTOR_DRIVER_A1 = 6;
	
	/**
	 * IOIO port for the motor driver stby port
	 */
	public static final int MOTOR_DRIVER_STANDBY = 7;
	
	/**
	 * IOIO port for the motor driver b1 port
	 */
	public static final int MOTOR_DRIVER_B1 = 8;
	
	/**
	 * IOIO port for the motor driver b2 port
	 */
	public static final int MOTOR_DRIVER_B2 = 9;
	
	/**
	 * IOIO port for the motor driver pwmb port
	 */
	public final static int MOTOR_DRIVER_PWMB = 10;
	
	/**
	 * stop the truck
	 * PWM value
	 */
	public static final int DRIVE_STOP = RC_PWM_PULSE_WIDTH_NEUTRAL;
	
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
	 * IOIO port for the "shifter"
	 */
	public static final int SHIFTER_PORT = 3;

	/**
	 * Steering value to drive the robot straight
	 */
	public static final int STEER_STRAIGHT = RC_PWM_PULSE_WIDTH_NEUTRAL;

	/**
	 * Steering value to drive the robot right
	 */
	public static final int STEER_RIGHT = 1700;

	/**
	 * Steering value to drive the robot left
	 */
	public static final int STEER_LEFT = 1300;


}
