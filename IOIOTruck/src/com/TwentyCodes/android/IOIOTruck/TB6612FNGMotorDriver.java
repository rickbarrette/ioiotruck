/**
 * IOIOMotorDriver.java
 * @date Jan 25, 2012
 * @author Tamlyn Rhodes
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.IOIOTruck;

import android.util.Log;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * This class is used to drive one of the (2) motors via the TB6612FNG motor driver from sparkfun.
 * https://github.com/tamlyn/Billy-Robot/blob/master/src/org/tamlyn/billy/IOIOMotor.java 
 * @author Tamlyn Rhodes
 * @author ricky barrette
 */
public class TB6612FNGMotorDriver {
	
	private static final String TAG = "TB6612FNGMotorDriver";
	
	private PwmOutput mPWM;
	private DigitalOutput mIn1;
	private DigitalOutput mIn2;

	private final int PWM_FREQUENCY = 100000;

	/**
	 * Creates a new IOIOMotorDiver.
	 * @param ioio that is connected
	 * @param pwmPin IOIO port number for the motor driver pwm pin
	 * @param in1Pin IOIO port number for the motor driver in1 pin
	 * @param in2Pin IOIO port number for the motor driver in2 pin
	 * @throws ConnectionLostException
	 * @author Tamlyn Rhodes
	 */
	public TB6612FNGMotorDriver(IOIO ioio, int pwmPin, int in1Pin, int in2Pin) throws ConnectionLostException {
		Log.v(TAG, "Initializing TB6612FNG Motor Driver on ports: (pwm) "+ pwmPin +", (in1) "+ in1Pin +", (in2) "+in2Pin);
		mPWM = ioio.openPwmOutput(pwmPin, PWM_FREQUENCY);
		mPWM.setDutyCycle(0);
		mIn1 = ioio.openDigitalOutput(in1Pin, false);
		mIn2 = ioio.openDigitalOutput(in2Pin, false);
	}

	/**
	 * Sets the speed of this motor
	 * @param speed +/- 0.0 - 1.0
	 * @throws ConnectionLostException
	 * @author Tamlyn Rhodes
	 */
	public void setSpeed(float speed) throws ConnectionLostException {
		if(speed == 0) {
			//stop
			mIn1.write(false);
			mIn2.write(false);
		} else if (speed < 0) {
			//reverse
			mIn1.write(true);
			mIn2.write(false);
		} else if (speed > 0) {
			//forward
			mIn1.write(false);
			mIn2.write(true);
		}
		mPWM.setDutyCycle(Math.abs(speed));
	}
	
	/**
	 * Sets the speed of this motor
	 * @param speed 1000 - 2000
	 * @throws ConnectionLostException
	 * @author ricky barrette
	 */
	public void setSpeed(int speed) throws ConnectionLostException {
		if(speed == 1500)
			setSpeed(0f);
		else if(speed < 1500)
			setSpeed((((float) speed - 1000) / 1000) - 1f);
		else
			setSpeed(((float) speed - 1000) / 1000);
	}
}