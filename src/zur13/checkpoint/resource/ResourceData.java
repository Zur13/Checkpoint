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

package zur13.checkpoint.resource;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import zur13.checkpoint.ACheckpoint;
import zur13.checkpoint.Pass;

/**
 * Controls access for single resource. Allows to limit max number of active passes per resource.
 *
 * @author
 *         <ul>
 *         <li>Yurii Polianytsia (coolio-iglesias@yandex.ru)</li>
 *         </ul>
 *
 */
public class ResourceData extends AResourceData {
	protected int maxActivePasses = 1; // max number of resource passes might be given simultaneously (max number of
										// threads with current resourceId allowed to access restricted section
										// simultaneously)
	protected Semaphore semaphore;

	private ResourceData(Object resourceId) {
		super();
		this.resourceId = resourceId;
	}

	/**
	 * 
	 * @param resourceId
	 * @param maxActivePassesPerResource
	 *            max number of threads with current resourceId allowed to access restricted section simultaneously
	 * @param fair
	 * @see java.util.concurrent.Semaphore
	 */
	public ResourceData(Object resourceId, int maxActivePasses, boolean fair) {
		this(resourceId);
		this.maxActivePasses = maxActivePasses;
		semaphore = new Semaphore(maxActivePasses, fair);
	}

	@Override
	public Pass getPass(ACheckpoint checkpoint) throws InterruptedException {
		semaphore.acquire();
		try {
			return new Pass(resourceId, checkpoint, true);
		} catch (Exception e) {
			semaphore.release();
			throw e;
		}
	}

	@Override
	public Pass getPassUninterruptibly(ACheckpoint checkpoint) {
		semaphore.acquireUninterruptibly();
		try {
			return new Pass(resourceId, checkpoint, true);
		} catch (Exception e) {
			semaphore.release();
			throw e;
		}
	}

	@Override
	public Pass tryGetPass(ACheckpoint checkpoint) {
		semaphore.tryAcquire();
		try {
			return new Pass(resourceId, checkpoint, true);
		} catch (Exception e) {
			semaphore.release();
			throw e;
		}
	}

	@Override
	public Pass tryGetPass(ACheckpoint checkpoint, long timeout, TimeUnit unit) throws InterruptedException {
		if ( semaphore.tryAcquire(timeout, unit) ) {
			try {
				return new Pass(resourceId, checkpoint, true);
			} catch (Exception e) {
				semaphore.release();
				throw e;
			}
		}
		return null;
	}

	@Override
	public Pass getPassRW(ACheckpoint checkpoint) throws InterruptedException {
		semaphore.acquire(maxActivePasses);
		try {
			return new Pass(resourceId, checkpoint, false);
		} catch (Exception e) {
			semaphore.release();
			throw e;
		}
	}

	@Override
	public Pass getPassRWUninterruptibly(ACheckpoint checkpoint) {
		semaphore.acquireUninterruptibly(maxActivePasses);
		try {
			return new Pass(resourceId, checkpoint, false);
		} catch (Exception e) {
			semaphore.release();
			throw e;
		}
	}

	@Override
	public Pass tryGetPassRW(ACheckpoint checkpoint) {
		semaphore.tryAcquire(maxActivePasses);
		try {
			return new Pass(resourceId, checkpoint, false);
		} catch (Exception e) {
			semaphore.release();
			throw e;
		}
	}

	@Override
	public Pass tryGetPassRW(ACheckpoint checkpoint, long timeout, TimeUnit unit) throws InterruptedException {
		if ( semaphore.tryAcquire(maxActivePasses, timeout, unit) ) {
			try {
				return new Pass(resourceId, checkpoint, false);
			} catch (Exception e) {
				semaphore.release();
				throw e;
			}
		}
		return null;
	}

	@Override
	public void returnPass(Pass pass) {
		if ( pass.isReadOnly() ) {
			semaphore.release();
		} else {
			semaphore.release(maxActivePasses);
		}
	}

}
