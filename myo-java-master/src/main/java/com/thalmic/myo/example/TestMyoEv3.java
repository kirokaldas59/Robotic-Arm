package com.thalmic.myo.example;

import java.rmi.RemoteException;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.lcd.LCD;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;
import lejos.utility.Delay;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.FirmwareVersion;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.enums.PoseType;

public class TestMyoEv3 {

	public static void main(String[] args) {
		
		try 
		{
			Hub hub = new Hub("net.havlena.myo");
			Myo myo = hub.waitForMyo(10000);
			final RemoteEV3 ev3 = new RemoteEV3("10.0.1.1");
			ev3.setDefault();
			Sound.beep();
			Button.LEDPattern(4);
			final GraphicsLCD g = ev3.getGraphicsLCD();
			g.clear();
			final RMIRegulatedMotor horizontal = ev3.createRegulatedMotor("C", 'L');
		    final RMIRegulatedMotor vertical = ev3.createRegulatedMotor("B", 'L');
		    final RMIRegulatedMotor close = ev3.createRegulatedMotor("A", 'L');
			hub.addListener(new AbstractDeviceListener() {
				@Override
				public void onConnect(Myo myo, long timestamp, FirmwareVersion firmwareVersion)
				{
					g.drawString("Myo is Connected", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
					//g.drawString("Closing...", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2+30, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
					Delay.msDelay(3000);
					g.clear();
					g.refresh();
					Button.LEDPattern(0);
					/*g.drawString("Myo is Connected", 0, 0, GraphicsLCD.VCENTER |
							GraphicsLCD.LEFT);
					
					Delay.msDelay(1000);*/
				}
				/*@Override
				public void onLock(Myo myo, long timestamp)
				{
					System.out.println(String.format("Myo switched to pose %s.", "Locked"));
				}*/
				@Override
				public void onPose(Myo myo, long timestamp, Pose pose) 
				{
					if (pose.getType() == PoseType.DOUBLE_TAP)
					{
						//g.drawString("Double Tap Pose", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
						try {
							horizontal.close();
							vertical.close();
							close.close();
				            System.exit(0);
			            } catch (RemoteException e) {
			               e.printStackTrace();
			            }
					}
					else if (pose.getType() == PoseType.FINGERS_SPREAD)
					{
						//g.drawString("Fingers Spread Pose", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
						try {
								close.rotate(-100);
				            } catch (RemoteException e) {
				               e.printStackTrace();
				            }
					}
					else if (pose.getType() == PoseType.FIST)
					{
						//g.drawString("Fist Pose", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
						try {
							   close.rotate(100, true);
				            } catch (RemoteException e) {
				               e.printStackTrace();
				            }
					}
					else if (pose.getType() == PoseType.WAVE_IN)
					{
						//g.drawString("Wave In Pose", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
						try {
								horizontal.rotate(-100);
				            } catch (RemoteException e) {
				               e.printStackTrace();
				            }
					}
					else if (pose.getType() == PoseType.WAVE_OUT)
					{
						//g.drawString("Wave Out Pose", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
						try {
								horizontal.rotate(100,true);
				            } catch (RemoteException e) {
				               e.printStackTrace();
				            }
					}
					Delay.msDelay(3000);
					g.clear();
					g.refresh();
					Button.LEDPattern(0);
					/*g.drawString(String.format("Myo switched to pose %s.", pose.toString()), 0, 0, GraphicsLCD.VCENTER |
							GraphicsLCD.LEFT);
					
					Delay.msDelay(1000);*/
				}
				/*@Override
				public void onDisconnect(Myo myo, long timestamp)
				{
					System.out.println("Myo has been disconnected from the device. Please Re-connect.");
				}
				@Override
				public void onArmUnsync(Myo myo, long timestamp)
				{
					System.out.println("Myo has been unsynced from arm. Please Re-sync.");
				}
				@Override
				public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection, float rotation, WarmupState warmupState)
				{
					System.out.println("Myo has been synced to the arm.");
				}*/
			});
			while (true) {
				hub.run(1000 / 20);
			}
		} 
		catch (Exception e) 
		{
			System.err.println("Error: ");
			e.printStackTrace();
			System.exit(1);
		}
	}

}
