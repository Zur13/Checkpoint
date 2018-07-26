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

package zur13.checkpoint;

import zur13.checkpoint.resource.ResourceDataFactory;
import zur13.checkpoint.resource.storage.AResourceDataStorage;
import zur13.checkpoint.resource.storage.ResourceDataStorage;
import zur13.checkpoint.resource.storage.ResourceDataStorageUnsafe;

/**
 * Helper class to provide easy way to configure checkpoint.
 * 
 * @author
 *         <ul>
 *         <li>Yurii Polianytsia (coolio-iglesias@yandex.ru)</li>
 *         </ul>
 * @see zur13.checkpoint.ACheckpoint
 */
public class CheckpointBuilder {
	private int globalPassesLimit = SimpleCheckpoint.UNLIMITED;
	private boolean useUnsafeApplicationDataStorage = false;
	private int maxActivePassesPerResource = 1;
	private boolean fair = false;
	private int concurrencyLevel = 16;
	private boolean reentrant = false;
	private String name = null;

	/**
	 * Creates new instance of checkpoint builder.
	 * 
	 * @return
	 */
	public static CheckpointBuilder newInst() {
		return new CheckpointBuilder();
	}

	/**
	 * Sets global number of simultaneously active passes for checkpoint instance.
	 * <p/>
	 * Default is Unlimited.
	 * 
	 * @param globalPassesLimit
	 */
	public CheckpointBuilder setGlobalPassesLimit(int globalPassesLimit) {
		this.globalPassesLimit = globalPassesLimit;
		return this;
	}

	/**
	 * Sets Unlimited global number of simultaneously active passes for checkpoint instance.
	 * 
	 */
	public CheckpointBuilder setUnlimitedGlobalPasses() {
		this.globalPassesLimit = SimpleCheckpoint.UNLIMITED;
		return this;
	}

	/**
	 * Configure checkpoint instance with the unsafe data storage which provides better performance but does not clear
	 * itself.
	 * Warning! Unsafe data storage does not clear internal data and should be used only if the possible number of
	 * resource ids is limited and there are enough memory to store data for all of them.
	 * 
	 * @return
	 */
	public CheckpointBuilder useUnsafeDataStorage() {
		this.useUnsafeApplicationDataStorage = true;
		return this;
	}

	/**
	 * Sets number of RO passes simultaneously available for each unique resource id.
	 * 
	 * @param maxPasses
	 * @return
	 */
	public CheckpointBuilder setMaxPassesPerResource(int maxPasses) {
		this.maxActivePassesPerResource = maxPasses;
		return this;
	}

	/**
	 * Configures checkpoint to guarantee first-in first-out granting of passes under contention.
	 * 
	 * @param fair
	 * @return
	 */
	public CheckpointBuilder setFair(boolean fair) {
		this.fair = fair;
		return this;
	}

	/**
	 * Sets estimated number of concurrently updating threads.
	 * Recommended default is 16.
	 * 
	 * @param concurrencyLevel
	 * @return
	 */
	public CheckpointBuilder setConcurrencyLevel(int concurrencyLevel) {
		this.concurrencyLevel = concurrencyLevel;
		return this;
	}

	/**
	 * Configures checkpoint to ignore reentrant requests.
	 * <p/>
	 * Default is false.
	 * <p/>
	 * Reentrant request means that same thread may enter restricted section using same resource id unlimited number of
	 * times (ignores resource max passes limit and global passes limit).
	 * <p/>
	 * WARN: Reentrant checkpoint does not allow reentrant upgrades from RO pass to RW pass for the same thread for the
	 * same resource id.
	 * WARN: Reentrant checkpoint does not allow to close the pass from the different thread. Pass should be closed by
	 * the thread which requested it.
	 * 
	 * @param reentrant
	 * @return
	 */
	public CheckpointBuilder setReentrant(boolean reentrant) {
		this.reentrant = reentrant;
		return this;
	}

	/**
	 * Sets checkpoint name.
	 * 
	 * @param name
	 */
	public CheckpointBuilder setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Creates checkpoint instance with the given settings.
	 * 
	 * @return checkpoint instance
	 */
	public ACheckpoint build() {
		ResourceDataFactory adf = new ResourceDataFactory(maxActivePassesPerResource, fair);
		AResourceDataStorage ads;
		if ( useUnsafeApplicationDataStorage ) {
			ads = new ResourceDataStorageUnsafe(adf, concurrencyLevel);
		} else {
			ads = new ResourceDataStorage(adf, concurrencyLevel);
		}
		ACheckpoint cp;
		if ( reentrant ) {
			cp = new ReentrantCheckpoint(ads, globalPassesLimit);
		} else {
			cp = new SimpleCheckpoint(ads, globalPassesLimit);
		}
		cp.setName(name);
		return cp;
	}
}
