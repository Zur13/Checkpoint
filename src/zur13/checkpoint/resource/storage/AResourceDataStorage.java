package zur13.checkpoint.resource.storage;

import zur13.checkpoint.resource.AResourceData;

/**
 * Provides fast thread safe operations to manage resources data.
 *
 * @author
 *         <ul>
 *         <li>Yurii Polianytsia (coolio-iglesias@yandex.ru)</li>
 *         </ul>
 *
 */
public abstract class AResourceDataStorage {

	public AResourceDataStorage() {
		super();
	}

	/**
	 * Retrieves ResourceAccessController instance for the given resource id.
	 * Creates new instance of the ResourceAccessController if no instances stored for the given resource. 
	 * 
	 * @return
	 */
	public abstract AResourceData get(Object resourceId);

	/**
	 * Release ResourceAccessController instance.
	 *
	 * @param resourceId
	 */
	public abstract void release(Object resourceId);

}