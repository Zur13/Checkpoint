# Checkpoint
Checkpoint library provides the way to organize restricted sections. Restricted sections are somewhat similar to the critical sections (synchronize blocks) but allows multiple threads to access same section under some conditions.

Quick example: lets say we have class that implements read and write data from/to device and we want to limit maximum number of device read requests processed simultaneously and maximum number of connections used by our server:  

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
    			// there are no writing threads for current device when thread received access 
    			// here and no more then MAX_SIMULTANEOUS_RO_REQUESTS_PER_DEVICE threads are 
    			// reading current device
                
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
