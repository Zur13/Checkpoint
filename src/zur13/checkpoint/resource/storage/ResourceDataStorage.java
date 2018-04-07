package zur13.checkpoint.resource.storage;

import java.util.concurrent.ConcurrentHashMap;

import zur13.checkpoint.resource.AResourceData;
import zur13.checkpoint.resource.ResourceDataFactory;

/**
 * Provides thread safe operations to store, create and release Resource Data objects.
 * Has critical sections at get() and release() operations.
 * Synchronously clears internal records for resource if no references left on release().
 *
 * @author
 *         <ul>
 *         <li>Yurii Polianytsia (coolio-iglesias@yandex.ru)</li>
 *         </ul>
 *
 */
public class ResourceDataStorage extends AResourceDataStorage {
	ConcurrentHashMap<Object, AResourceData> dataBuckets[];
	ResourceDataFactory adf;

	@SuppressWarnings("unchecked")
	public ResourceDataStorage(ResourceDataFactory adf, int concurrencyLevel) {
		super();
		this.adf = adf;
		dataBuckets = new ConcurrentHashMap[concurrencyLevel];
		for (int i = 0; i < dataBuckets.length; i++) {
			dataBuckets[i] = new ConcurrentHashMap<Object, AResourceData>();
		}
	}

	/**
	 * Retrieve ResourceData instance for the given resource.
	 * Create new instance of the ResourceData if no instance stored for the given resource.
	 * 
	 * Release ResourceData after the passes it supplied is returned or you have done working with it.
	 * 
	 * @return
	 */
	@Override
	public AResourceData get(Object resourceId) {
		AResourceData ad = null;
		AResourceData adPrev = null;

		int bucketIdx = spread(resourceId.hashCode()) % dataBuckets.length;
		ConcurrentHashMap<Object, AResourceData> resourcesDataBucket = dataBuckets[bucketIdx];

		ad = resourcesDataBucket.get(resourceId);

		if ( ad == null || ad.getRefCounter().getAndIncrement() <= 0 ) {
			adPrev = ad;
			synchronized (resourcesDataBucket) {
				ad = resourcesDataBucket.get(resourceId);
				if ( ad == null ) {
					ad = adf.getResourceData(resourceId); // default refCounter == 1
					resourcesDataBucket.put(resourceId, ad);
				} else if ( ad != adPrev ) {
					// ResourceData was recreated and put to Hash Map after refCounter.getAndIncrement()
					// but before synchronized() block
					ad.getRefCounter().getAndIncrement();
				}
			}
		}

		return ad;
	}

	/**
	 * Release ResourceAccessController instance and clear it from the storage if no references left.
	 *
	 * @param resourceId
	 */
	@Override
	public void release(Object resourceId) {
		int bucketIdx = spread(resourceId.hashCode()) % dataBuckets.length;
		ConcurrentHashMap<Object, AResourceData> resourcesDataBucket = dataBuckets[bucketIdx];

		AResourceData ad = resourcesDataBucket.get(resourceId);

		if ( ad.getRefCounter().decrementAndGet() <= 0 ) {
			synchronized (resourcesDataBucket) {
				if ( ad.getRefCounter().get() <= 0 ) {
					resourcesDataBucket.remove(ad.getResourceId());
				}
			}
		}
	}

	/**
	 * Spread hash to minimize collisions inside ConcurrentHashMaps
	 * 
	 * @param h
	 * @return
	 */
	static final int spread(int h) {
		return (h ^ (h >> 8));
	}
}
