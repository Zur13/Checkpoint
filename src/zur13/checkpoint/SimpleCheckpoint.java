package zur13.checkpoint;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import zur13.checkpoint.resource.AResourceData;
import zur13.checkpoint.resource.storage.AResourceDataStorage;

/**
 * Simple checkpoint allows to limit number of passes per resource and to limit global number passes.
 *
 * @author
 *         <ul>
 *         <li>Yurii Polianytsia (coolio-iglesias@yandex.ru)</li>
 *         </ul>
 *
 */
public class SimpleCheckpoint extends ACheckpoint {
	AResourceDataStorage ads;
	int globalPassesLimit;
	Semaphore globalPassesSemaphore;
	public static final int UNLIMITED = -1;

	/**
	 * 
	 * @param ads
	 * @param globalPassesLimit
	 *            upper limit for global number of passes for all resources or use
	 *            org.checkpoint.SimpleCheckpoint.UNLIMITED
	 */
	public SimpleCheckpoint(AResourceDataStorage ads, int globalPassesLimit) {
		super();
		this.ads = ads;
		this.globalPassesLimit = globalPassesLimit;
		if ( UNLIMITED == globalPassesLimit ) {
			globalPassesSemaphore = null;
		} else {
			globalPassesSemaphore = new Semaphore(globalPassesLimit);
		}
	}

	/**
	 * Requests RO pass for specified resource blocking until one is available, or the thread is interrupted. <br/>
	 * <br/>
	 * Multiple RO passes available for each resource but none can't be received while there is active RW pass. <br/>
	 * Awaits for RW pass return if there is active for specified resource and awaits for any RO pass return if no RO
	 * passes available for specified resource.
	 * 
	 * @param resourceId
	 *            unique resource identifier
	 * @return resource pass
	 * @throws InterruptedException
	 */
	@Override
	public Pass getPass(Object resourceId) throws InterruptedException {
		AResourceData ad = ads.get(resourceId);
		Pass pass = null;
		try {
			pass = ad.getPass(this); // TODO:
		} finally {
			if ( pass == null ) {
				ads.release(resourceId);
				return null;
			}
		}

		try {
			if ( globalPassesSemaphore != null ) {
				globalPassesSemaphore.acquire();
			}
		} catch (Throwable e) {
			pass.close();
			throw e;
		}
		return pass;
	}

	/**
	 * Requests RO pass for specified resource blocking until one is available. <br/>
	 * <br/>
	 * Multiple RO passes available for each resource but none can't be received while there is active RW pass. <br/>
	 * Awaits for RW pass return if there is active for specified resource and awaits for any RO pass return if no RO
	 * passes available for specified resource.
	 * 
	 * @param resourceId
	 *            unique resource identifier
	 * @return resource pass
	 */
	@Override
	public Pass getPassUninterruptibly(Object resourceId) {
		AResourceData ad = ads.get(resourceId);
		Pass pass = ad.getPassUninterruptibly(this);// TODO:

		if ( globalPassesSemaphore != null ) {
			globalPassesSemaphore.acquireUninterruptibly();
		}

		return pass;
	}

	/**
	 * Requests RO pass for specified resource, returns pass only if one is available at the time of invocation. <br/>
	 * <br/>
	 * Multiple RO passes available for each resource but none can't be received while there is active RW pass. <br/>
	 * Returns pass if there is at least one available RO pass and there is no active RW pass for specified resource at
	 * the time of invocation.
	 * 
	 * @param resourceId
	 *            unique resource identifier
	 * @return resource pass or null if no RO passes available or RW pass active
	 */
	@Override
	public Pass tryGetPass(Object resourceId) {
		AResourceData ad = ads.get(resourceId);
		Pass pass = null;
		try {
			pass = ad.tryGetPass(this); // TODO:
		} finally {
			if ( pass == null ) {
				ads.release(resourceId);
				return null;
			}
		}

		if ( globalPassesSemaphore != null ) {
			if ( globalPassesSemaphore.tryAcquire() ) {
				return pass;
			} else {
				pass.close();
				return null;
			}
		} else {
			return pass;
		}
	}

	/**
	 * Requests RO pass for specified resource, returns pass if one becomes available within the given waiting time and
	 * the current thread has not been interrupted. <br/>
	 * <br/>
	 * Multiple RO passes available for each resource but none can't be received while there is active RW pass. <br/>
	 * Returns pass if there is at least one available RO pass and there is no active RW pass for specified resource at
	 * the time of invocation.
	 * <br/>
	 * <br/>
	 * WARN: timeout is not precise and may take double time until it happens (internally there are 2 consecutive places
	 * where it is used) <br/>
	 * 
	 * @param resourceId
	 *            unique resource identifier
	 * @return resource pass or null if no RO passes available or RW pass active
	 * @throws InterruptedException
	 */
	@Override
	public Pass tryGetPass(Object resourceId, long timeout, TimeUnit unit) throws InterruptedException {
		AResourceData ad = ads.get(resourceId);
		Pass pass = null;
		try {
			pass = ad.tryGetPass(this, timeout, unit); // TODO:
		} finally {
			if ( pass == null ) {
				ads.release(resourceId);
				return null;
			}
		}

		if ( globalPassesSemaphore != null ) {
			if ( globalPassesSemaphore.tryAcquire(timeout, unit) ) {
				return pass;
			} else {
				pass.close();
				return null;
			}
		} else {
			return pass;
		}
	}

	/**
	 * Requests RW pass for specified resource blocking until one is available, or the thread is interrupted. <br/>
	 * <br/>
	 * Single RW pass available for each resource and it can't be received while there are active RO passes. <br/>
	 * Awaits for all RO and RW pass return if there are active for specified resource.
	 * 
	 * @param resourceId
	 *            unique resource identifier
	 * @return resource pass
	 * @throws InterruptedException
	 */
	@Override
	public Pass getPassRW(Object resourceId) throws InterruptedException {
		AResourceData ad = ads.get(resourceId);
		Pass pass = null;
		try {
			pass = ad.getPassRW(this); // TODO:
		} finally {
			if ( pass == null ) {
				ads.release(resourceId);
				return null;
			}
		}

		try {
			if ( globalPassesSemaphore != null ) {
				globalPassesSemaphore.acquire();
			}
		} catch (Throwable e) {
			pass.close();
			throw e;
		}
		return pass;
	}

	/**
	 * Requests RW pass for specified resource blocking until one is available. <br/>
	 * <br/>
	 * Single RW pass available for each resource and it can't be received while there are active RO passes. <br/>
	 * Awaits for all RO and RW pass return if there are active for specified resource.
	 * 
	 * @param resourceId
	 *            unique resource identifier
	 * @return resource pass
	 */
	@Override
	public Pass getPassRWUninterruptibly(Object resourceId) {
		AResourceData ad = ads.get(resourceId);
		Pass pass = ad.getPassRWUninterruptibly(this); // TODO:

		if ( globalPassesSemaphore != null ) {
			globalPassesSemaphore.acquireUninterruptibly();
		}
		return pass;
	}

	/**
	 * Requests RW pass for specified resource, returns pass only if one is available at the time of invocation. <br/>
	 * <br/>
	 * Single RW pass available for each resource and it can't be received while there are active RO passes. <br/>
	 * Returns pass if no active RO and RW passes for specified resource at the time of invocation.
	 * 
	 * @param resourceId
	 *            unique resource identifier
	 * @return resource pass or null if no passes available
	 */
	@Override
	public Pass tryGetPassRW(Object resourceId) {
		AResourceData ad = ads.get(resourceId);
		Pass pass = null;
		try {
			pass = ad.tryGetPassRW(this); // TODO:
		} finally {
			if ( pass == null ) {
				ads.release(resourceId);
				return null;
			}
		}

		if ( globalPassesSemaphore != null ) {
			if ( globalPassesSemaphore.tryAcquire() ) {
				return pass;
			} else {
				pass.close();
				return null;
			}
		} else {
			return pass;
		}
	}

	/**
	 * Requests RW pass for specified resource, returns pass if one becomes available within the given waiting time and
	 * the current thread has not been interrupted. <br/>
	 * <br/>
	 * Single RW pass available for each resource and it can't be received while there are active RO passes. <br/>
	 * Returns pass if no active RO and RW passes for specified resource at the time of invocation.
	 * <br/>
	 * <br/>
	 * WARN: timeout is not precise and may take double time until it happens (internally there are 2 consecutive places
	 * where it is used) <br/>
	 * 
	 * @param resourceId
	 *            unique resource identifier
	 * @param timeout
	 *            the maximum time to wait for a permit
	 * @param unit
	 *            the time unit of the timeout argument
	 * @return resource pass or null if no passes available
	 * @throws InterruptedException
	 */
	@Override
	public Pass tryGetPassRW(Object resourceId, long timeout, TimeUnit unit) throws InterruptedException {
		AResourceData ad = ads.get(resourceId);
		Pass pass = null;
		try {
			pass = ad.tryGetPassRW(this, timeout, unit); // TODO:
		} finally {
			if ( pass == null ) {
				ads.release(resourceId);
				return null;
			}
		}
		if ( globalPassesSemaphore != null ) {
			if ( !globalPassesSemaphore.tryAcquire(timeout, unit) ) {
				pass.close();
				return null;
			}
		}
		return pass;
	}

	@Override
	protected void returnPass(Pass pass) {
		if ( globalPassesSemaphore != null ) {
			globalPassesSemaphore.release();
		}

		AResourceData ad = ads.get(pass.getResourceId());
		ad.returnPass(pass);

		ads.release(pass.getResourceId());

	}
}
