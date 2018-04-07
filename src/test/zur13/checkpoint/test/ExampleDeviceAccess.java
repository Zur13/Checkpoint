package test.zur13.checkpoint.test;

import java.net.InetAddress;

import zur13.checkpoint.ACheckpoint;
import zur13.checkpoint.CheckpointBuilder;
import zur13.checkpoint.Pass;

public class ExampleDeviceAccess {
	/*
	 * Maximum number or RO requests that can be executed simultaneously to each device. Other threads should wait
	 * their turn to request device data.
	 */
	private final static int MAX_SIMULTANEOUS_RO_REQUESTS_PER_DEVICE = 30;

	/* Maximum number or requests that can be executed simultaneously by server. */
	private final static int MAX_SIMULTANEOUS_FOR_ALL_DEVICES = 600;

	ACheckpoint cp = CheckpointBuilder.newInst()
			.setName("Device access")
			.setMaxPassesPerResource(MAX_SIMULTANEOUS_RO_REQUESTS_PER_DEVICE)
			.setReentrant(false)
			.setFair(true)
			.setGlobalPassesLimit(MAX_SIMULTANEOUS_FOR_ALL_DEVICES)
			.build();

	public String readDeviceData(InetAddress device) {
		String deviceData;
		
		try (Pass p = cp.getPassUninterruptibly(device)) {
			// read device data here 
			// there are no writing threads for current device and no more then
			// MAX_SIMULTANEOUS_RO_REQUESTS_PER_DEVICE threads are reading current device
			
			deviceData = "";
		}
		
		return deviceData;
	}

	public boolean writeDeviceData(InetAddress device, String deviceData) {
		
		try (Pass p = cp.getPassRWUninterruptibly(device)) {
			// write device data here 
			// there is no other reading or writing threads for current device 
			// when current thread received access here 
			// but there might be other RW threads in this section which access other devices
			
		}
		
		return true;
	}
}
