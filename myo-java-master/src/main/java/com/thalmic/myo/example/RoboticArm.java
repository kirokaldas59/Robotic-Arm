/*
 * The startup code has been written by Matous Havlena
 * Original Copy can be found on: https://github.com/matoushavlena/myo-truck
 * And edited/completed by Kirolos Kaldas to suit the Robotic Arm
 * project purposes.
 * */

package com.thalmic.myo.example;

import java.rmi.RemoteException;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;
import lejos.utility.Delay;

import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.enums.UnlockType;

public class RoboticArm {

	// This motor controls the horizontal movement
	private RMIRegulatedMotor motorH;
	// This motor controls the vertical movement
	private RMIRegulatedMotor motorV;
	// This motor controls the hand
	private RMIRegulatedMotor motorHand;
	// This is the color sensor
	EV3ColorSensor colorSensor;
	// This is the touch sensor
	EV3TouchSensor touchSensor;
	private RemoteEV3 ev3;
	private GraphicsLCD lcd;
	private boolean running = true;
	private boolean sleeping = true;
	// We don't want to allow the horizontal motor to exceed maximum values
	private static int HORIZONTAL_MAX = 20;
	// The higher number, the more sensitive(faster), 1 is the lowest
	private static int HORIZONTAL_SENSITIVITY = 10;
	// The higher number, the more sensitive(faster), 1 is the lowest
	private static int VERTICAL_SENSITIVITY = 2;
	private int horizontalScale;  
	
	public RoboticArm() throws RemoteException 
	{
		try 
		{
			// Looks for the ev3 using its IP address & makes an object of it
			ev3 = new RemoteEV3("10.0.1.1");
			ev3.setDefault();
			Sound.beep();
			Button.LEDPattern(4);
			lcd = ev3.getGraphicsLCD();
			lcd.clear();
			lcd.drawString("Myo Truck", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
			lcd.drawString("Running...", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2+30, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
			// Creates objects of the motors and sensors
			motorHand = ev3.createRegulatedMotor("A", 'M');
			motorV = ev3.createRegulatedMotor("B", 'L');
			motorH = ev3.createRegulatedMotor("C", 'L');
			colorSensor = new EV3ColorSensor(SensorPort.S3);
			touchSensor = new EV3TouchSensor(SensorPort.S1);
			
			// Initialize the maximum movement of the horizontal motor
			horizontalScale = 0;
			horizontalScale -= HORIZONTAL_MAX;
			
			// Initialize speed and acceleration to the motors
			motorH.setSpeed(0);
			motorH.setAcceleration(200);
			motorH.stop(true);
			
			motorV.setAcceleration(200);
			motorV.setSpeed(0);
			motorV.stop(true);
		} 
		catch (Exception e) 
		{
			this.close();
			e.printStackTrace();
		}
	}
	
	// Closes all the ports to prevent errors and closes the program
	private void close() 
	{
		Button.LEDPattern(5);
		try 
		{
			if (motorH != null) motorH.close();
			if (motorV != null) motorV.close();
			if (motorHand != null) motorHand.close();
			if (colorSensor != null) colorSensor.close();
			if (touchSensor != null) touchSensor.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		lcd.clear();
		lcd.drawString("Myo Truck", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
		lcd.drawString("Closing...", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2+30, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
		Delay.msDelay(3000);
		lcd.clear();
		lcd.refresh();
		Button.LEDPattern(0);
	}
	
	// This methods gets the data from the myo armband and sends instructions to the EV3
	private void run(Hub hub, DataCollector dataCollector) throws RemoteException 
	{
		while (running) 
		{
			hub.run(1000/20);
			System.out.print(dataCollector);
			if (sleeping == true)
			{
				try 
				{
	    			Thread.sleep(200);
	    			continue;
	    		} 
				catch (InterruptedException e) 
				{
	    			e.printStackTrace();
	    		}
			}
	    	System.out.print(dataCollector);
	    	// Pitch is the data from rising and lowering your hand
	    	this.setVerticalSpeed(dataCollector.getPitch());
	    	// Roll is the data from twisting your wrist
	    	this.setHorizontalSpeed(dataCollector.getRoll(), dataCollector.isLeftArm());
		}
		motorV.stop(true);
	}
	
	// Moves the vertical motor
	private void setVerticalSpeed(double pitch) 
	{
		try 
		{
			// Calculations of the height of the vertical movement of the arm
			int speed = 0;
			int scalePitch = DataCollector.SCALE / 2 / VERTICAL_SENSITIVITY;
			int scaleMotor = (int) (motorV.getMaxSpeed() / 5);
			if (pitch >= 0) speed = (int)((-scalePitch * VERTICAL_SENSITIVITY + pitch) * (scaleMotor / scalePitch));
			// Sets new speed to the motor according to the speed of the arm
			motorV.setSpeed(speed);
			
			// Gets the value of the ambient light from the colour sensor
			SensorMode ambient = colorSensor.getAmbientMode();
			float[] sample = new float[ambient.sampleSize()];
			ambient.fetchSample(sample, 0);
			
			if (speed >= 0) 
			{
				/* If the ambient value is less than or equal to 0.05 then the motor will stop because
				 * it has reached its desired highest point */
				if (sample[0] > 0.05)
				{
					motorV.backward();
				}
				else
				{
					motorV.stop(true);
				}
			} 
			else 
			{
				motorV.forward();
			}
		} 
		catch (RemoteException e) 
		{
			e.printStackTrace();
		}
	}
	
	// Moves the horizontal motor
	private void setHorizontalSpeed(double roll, boolean isLeftArm) 
	{
		try 
		{
			// Calculations of the strength of the wrist twist
			int steering = 0;
			int scale = DataCollector.SCALE / 2 / HORIZONTAL_SENSITIVITY;
			if (roll >= 0)
			{
				steering = (int)((-scale * HORIZONTAL_SENSITIVITY + roll) * (horizontalScale / scale));
			}
			// If the myo is on the left arm then directions of the horizontal movement will be different to when its on the right arm
			if (isLeftArm) steering = steering * -1;
			// Sets new speed to the motor according to the strength of the wrist twist
			motorH.setSpeed(steering);
			
			// Gets the value of the button on the touch sensor
			SensorMode touch = touchSensor.getTouchMode();
			float[] sample = new float[touch.sampleSize()];
			touch.fetchSample(sample, 0);
			if (steering >= 0) 
			{
				motorH.backward();
			} 
			else
			{
				/* If the button value is 1 then the button is pressed which means that 
				 * the horizontal motor is at its desired maximum point and that will stop the motor
				 * from rotating more */
				if (sample[0] != 1)
				{
					motorH.forward();
				}
				else
				{
					motorH.stop(true);
				}
			}
		} 
		catch (RemoteException e) 
		{
			e.printStackTrace();
		}
	}
	
	// This method simply changes running to false to close the program
	public void stop() 
	{
		this.running = false;
	}
	
	
	/*public void sleep() 
	{
		lcd.clear();
		lcd.drawString("Myo Truck", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
		lcd.drawString("Sleeping...", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2+30, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
		Button.LEDPattern(6);
		try 
		{
			motorV.setSpeed(0);
		} 
		catch (RemoteException e) 
		{
			e.printStackTrace();
		}		
		this.running = true;
		this.sleeping = true;
	}*/
	
	// This starts the program
	public void start() 
	{
		Button.LEDPattern(4);
		this.running = true;
		this.sleeping = false;
	}
	
	// Open the gripper
	public void openGripper() 
	{
		try 
		{
			motorHand.rotateTo(90);
		} 
		catch (RemoteException e) 
		{
			e.printStackTrace();
		}
	}
	
	// Closes the gripper
	public void closeGripper() 
	{
		try 
		{
			motorHand.rotateTo(-90);
		} 
		catch (RemoteException e) 
		{
			e.printStackTrace();
		}
	}
	
	protected void finalize() throws Throwable 
	{
		try 
		{
			this.close();
		} 
		finally 
		{
			super.finalize();
		}
	}
	
	public static void main(String[] args) 
	{
		try 
		{
			RoboticArm myoArm = new RoboticArm();
			Hub hub = new Hub("net.havlena.myo");
		    Myo myo = hub.waitForMyo(10000);
		    if (myo == null) 
		    {
				throw new RuntimeException("Unable to find a Myo!");
			}
		    
		    // Keeps the myo armband unlocked for the duration of the program's runtime
		    myo.unlock(UnlockType.UNLOCK_HOLD);
		    DataCollector dataCollector = new DataCollector(myoArm);
		    hub.addListener(dataCollector);
		    myoArm.run(hub, dataCollector);
		    myoArm.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
}
