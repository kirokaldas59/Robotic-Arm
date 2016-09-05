package com.thalmic.myo.example;

import com.thalmic.myo.*;
import com.thalmic.myo.enums.Arm;
import com.thalmic.myo.enums.PoseType;
import com.thalmic.myo.enums.WarmupState;
import com.thalmic.myo.enums.XDirection;

public class TestMyo {

	public static void main(String[] args) {
		
		try 
		{
			Hub hub = new Hub("net.havlena.myo");
			Myo myo = hub.waitForMyo(10000);
			hub.addListener(new AbstractDeviceListener() {
				@Override
				public void onConnect(Myo myo, long timestamp, FirmwareVersion firmwareVersion)
				{
					System.out.println("Myo has been connected to the device.");
				}
				@Override
				public void onLock(Myo myo, long timestamp)
				{
					System.out.println(String.format("Myo switched to pose %s.", "Locked"));
				}
				@Override
				public void onPose(Myo myo, long timestamp, Pose pose) 
				{
					if (pose.getType() != PoseType.REST)
						System.out.println(String.format("Myo switched to pose %s.", pose.toString()));
				}
				@Override
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
				}
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
