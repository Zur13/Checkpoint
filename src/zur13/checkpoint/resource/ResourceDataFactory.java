package zur13.checkpoint.resource;

public class ResourceDataFactory {
	int maxActivePassesPerResource;
	boolean fair;

	/**
	 * 
	 * 
	 * @param maxActivePassesPerResource
	 *            max number of passes per resource
	 * @param fair
	 * @see java.util.concurrent.Semaphore
	 */
	public ResourceDataFactory(int maxActivePassesPerResource, boolean fair) {
		super();
		this.maxActivePassesPerResource = maxActivePassesPerResource;
		this.fair = fair;
	}

	public AResourceData getResourceData(Object resourceId) {
		return new ResourceData(resourceId, maxActivePassesPerResource, fair);
	}
}
