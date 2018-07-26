/*
 * Copyright 2018 Yurii Polianytsia (coolio-iglesias@yandex.ru)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zur13.checkpoint.resource.storage;

import java.util.concurrent.ConcurrentHashMap;

import zur13.checkpoint.resource.AResourceData;
import zur13.checkpoint.resource.ResourceDataFactory;

/**
 * Provides thread safe operations to store, create and release Resource Data objects.
 * <br/><br/>
 * This is super fast version that DOES NOT clear internal data!
 * <br/><br/>
 * There is only one critical section at get() method and this section only used during new Resource Record creation
 * (when requested ResourceAccess for new resource).
 * <br/><br/>
 * You can populate ResourceAccessProviderUnsafe instance with known resource ids during initialization using
 * get(Object resourceId) method.
 * <br/><br/>
 * Use it only in cases when there is a limited number of resources possible and your app has enough memory to store
 * record for all resources simultaneously.
 *
 * @author
 *         <ul>
 *         <li>Yurii Polianytsia (coolio-iglesias@yandex.ru)</li>
 *         </ul>
 *
 */
public class ResourceDataStorageUnsafe extends AResourceDataStorage {
	ConcurrentHashMap<Object, AResourceData>[] dataBuckets;
	ResourceDataFactory adf;

	@SuppressWarnings("unchecked")
	public ResourceDataStorageUnsafe(ResourceDataFactory adf, int concurrencyLevel) {
		super();
		this.adf = adf;
		dataBuckets = new ConcurrentHashMap[concurrencyLevel];
		for (int i = 0; i < dataBuckets.length; i++) {
			dataBuckets[i] = new ConcurrentHashMap<Object, AResourceData>();
		}
	}

	/**
	 * Retrieves ResourceAccessController instance for the given resource.
	 * Creates new instance of the ResourceAccessController if no instance stored for the given resource.
	 * <br/><br/>
	 * ResourceAccess should be released after all passes it supplied are closed.
	 * 
	 * @return
	 */
	@Override
	public AResourceData get(Object resourceId) {
		AResourceData ad = null;

		int bucketIdx = spread(resourceId.hashCode()) % dataBuckets.length;
		ConcurrentHashMap<Object, AResourceData> resourcesDataBucket = dataBuckets[bucketIdx];

		ad = resourcesDataBucket.get(resourceId);

		if ( ad == null ) {
			synchronized (resourcesDataBucket) {
				ad = resourcesDataBucket.get(resourceId);
				if ( ad == null ) {
					ad = adf.getResourceData(resourceId); // default refCounter == 1
					resourcesDataBucket.put(resourceId, ad);
				}
			}
		}

		return ad;
	}

	/**
	 * Release ResourceAccessController instance.
	 * Do nothing in this implementation.
	 *
	 * @param resourceId
	 */
	@Override
	public void release(Object resourceId) {
	}

	/**
	 * Spread hash to minimize collisions inside ConcurrentHashMaps.
	 * 
	 * @param h
	 * @return
	 */
	static final int spread(int h) {
		return (h ^ (h >> 8));
	}
}
