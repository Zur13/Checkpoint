package zur13.checkpoint;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import zur13.checkpoint.resource.AResourceData;
import zur13.checkpoint.resource.storage.AResourceDataStorage;

/**
 * Supports all features of Simple checkpoint and also supports reentrancy.
 * Reentrancy allows same thread to enter multiple restricted sections with the same resourceId without using
 * additional passes and without using global limit.
 * 
 * 
 * WARN: Reentrant checkpoint does not allow reentrant upgrades from RO pass to RW pass for the same thread for the same
 * resource id.
 * WARN: Reentrant checkpoint does not allow to close the pass from the different thread. Pass should be closed by the
 * thread which requested it.
 *
 * @author
 *         <ul>
 *         <li>Yurii Polianytsia (coolio-iglesias@yandex.ru)</li>
 *         </ul>
 *
 */
public class ReentrantCheckpoint extends SimpleCheckpoint {
	protected static final String UPGRADING_RO_PASS_IS_NOT_SUPPORTED =
			"Upgrading RO pass to the RW pass is not supported";
	protected ConcurrentHashMap<TidResourceKey, ReentrantPass> rPassStor =
			new ConcurrentHashMap<ReentrantCheckpoint.TidResourceKey, ReentrantPass>();

	/**
	 * 
	 * @param ads
	 * @param globalPassesLimit
	 *            upper limit for global number of passes for all resources or use
	 *            org.checkpoint.SimpleCheckpoint.UNLIMITED
	 */
	public ReentrantCheckpoint(AResourceDataStorage ads, int globalPassesLimit) {
		super(ads, globalPassesLimit);

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
		ReentrantPass rPass = checkReenter(resourceId);
		Pass pass = rPass.getPass();

		if ( pass == null ) {
			try {
				pass = super.getPass(resourceId);
				if ( pass == null ) {
					checkReenterExit(resourceId);
				} else {
					rPass.setPass(pass);
				}
			} catch (Exception e) {
				checkReenterExit(resourceId);
				throw e;
			}
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
		ReentrantPass rPass = checkReenter(resourceId);
		Pass pass = rPass.getPass();

		if ( pass == null ) {
			try {
				pass = super.getPassUninterruptibly(resourceId);
				if ( pass == null ) {
					checkReenterExit(resourceId);
				} else {
					rPass.setPass(pass);
				}
			} catch (Exception e) {
				checkReenterExit(resourceId);
				throw e;
			}
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
		ReentrantPass rPass = checkReenter(resourceId);
		Pass pass = rPass.getPass();

		if ( pass == null ) {
			try {
				pass = super.tryGetPass(resourceId);
				if ( pass == null ) {
					checkReenterExit(resourceId);
				} else {
					rPass.setPass(pass);
				}
			} catch (Exception e) {
				checkReenterExit(resourceId);
				throw e;
			}
		}
		return pass;
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
		ReentrantPass rPass = checkReenter(resourceId);
		Pass pass = rPass.getPass();

		if ( pass == null ) {
			try {
				pass = super.tryGetPass(resourceId, timeout, unit);
				if ( pass == null ) {
					checkReenterExit(resourceId);
				} else {
					rPass.setPass(pass);
				}
			} catch (Exception e) {
				checkReenterExit(resourceId);
				throw e;
			}
		}
		return pass;
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
		ReentrantPass rPass = checkReenter(resourceId);
		Pass pass = rPass.getPass();

		if ( pass == null ) {
			try {
				pass = super.getPassRW(resourceId);
				if ( pass == null ) {
					checkReenterExit(resourceId);
				} else {
					rPass.setPass(pass);
				}
			} catch (Exception e) {
				checkReenterExit(resourceId);
				throw e;
			}
		} else if ( pass.isReadOnly ) {
			throw new UnsupportedOperationException(UPGRADING_RO_PASS_IS_NOT_SUPPORTED);
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
		ReentrantPass rPass = checkReenter(resourceId);
		Pass pass = rPass.getPass();

		if ( pass == null ) {
			try {
				pass = super.getPassRWUninterruptibly(resourceId);
				if ( pass == null ) {
					checkReenterExit(resourceId);
				} else {
					rPass.setPass(pass);
				}
			} catch (Exception e) {
				checkReenterExit(resourceId);
				throw e;
			}
		} else if ( pass.isReadOnly ) {
			throw new UnsupportedOperationException(UPGRADING_RO_PASS_IS_NOT_SUPPORTED);
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
		ReentrantPass rPass = checkReenter(resourceId);
		Pass pass = rPass.getPass();

		if ( pass == null ) {
			try {
				pass = super.tryGetPassRW(resourceId);
				if ( pass == null ) {
					checkReenterExit(resourceId);
				} else {
					rPass.setPass(pass);
				}
			} catch (Exception e) {
				checkReenterExit(resourceId);
				throw e;
			}
		} else if ( pass.isReadOnly ) {
			throw new UnsupportedOperationException(UPGRADING_RO_PASS_IS_NOT_SUPPORTED);
		}
		return pass;
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
		ReentrantPass rPass = checkReenter(resourceId);
		Pass pass = rPass.getPass();

		if ( pass == null ) {
			try {
				pass = super.tryGetPassRW(resourceId, timeout, unit);
				if ( pass == null ) {
					checkReenterExit(resourceId);
				} else {
					rPass.setPass(pass);
				}
			} catch (Exception e) {
				checkReenterExit(resourceId);
				throw e;
			}
		} else if ( pass.isReadOnly ) {
			throw new UnsupportedOperationException(UPGRADING_RO_PASS_IS_NOT_SUPPORTED);
		}
		return pass;
	}

	@Override
	protected void returnPass(Pass pass) {
		if ( checkReenterExit(pass.getResourceId()) ) {
			if ( globalPassesSemaphore != null ) {
				globalPassesSemaphore.release();
			}

			AResourceData ad = ads.get(pass.getResourceId());
			ad.returnPass(pass);

			ads.release(pass.getResourceId());
		}
	}

	/**
	 * Increments reenter counter for the specified thread for the specified resourceId.
	 * 
	 * @param resourceId
	 * @return ReenterPass object (ReenterPass.getPass()==null if entered for the first time)
	 */
	protected ReentrantPass checkReenter(Object resourceId) {
		TidResourceKey key = buildKey(resourceId);
		ReentrantPass reenterPass = rPassStor.get(key);
		if ( reenterPass != null ) {
			reenterPass.getCounter().incrementAndGet();
			return reenterPass;
		} else {
			reenterPass = new ReentrantPass(null);
			rPassStor.put(key, reenterPass);
			return reenterPass;
		}
	}

	/**
	 * Decrements reenter counter and returns true if that was last reentry for that thread for specified resourceId
	 * and pass release required.
	 * 
	 * @param resourceId
	 * @return true if that was last reenter record and pass should be returned
	 */
	protected boolean checkReenterExit(Object resourceId) {
		TidResourceKey key = buildKey(resourceId);
		ReentrantPass rpass = rPassStor.get(key);
		if ( rpass != null ) {
			AtomicLong reenterCounter = rpass.getCounter();
			if ( reenterCounter.decrementAndGet() == 0 ) {
				rPassStor.remove(key);
				return true;
			}
		}
		return false;
	}

	protected static TidResourceKey buildKey(Object resourceId) {
		return new TidResourceKey(resourceId, Thread.currentThread().getId());
	}

	/**
	 * Contains Pass and reenter counter for reentrant applicant thread.
	 *
	 * @author
	 *         <ul>
	 *         <li>Yurii Polianytsia (coolio-iglesias@yandex.ru)</li>
	 *         </ul>
	 *
	 */
	protected static class ReentrantPass {
		AtomicLong counter = new AtomicLong(1);
		Pass pass;

		public ReentrantPass(Pass pass) {
			super();
			this.pass = pass;
		}

		public AtomicLong getCounter() {
			return this.counter;
		}

		public Pass getPass() {
			return this.pass;
		}

		public void setPass(Pass pass) {
			this.pass = pass;
		}
	}

	/**
	 * Reentrant thread and resource id combination.
	 *
	 * @author
	 *         <ul>
	 *         <li>Yurii Polianytsia (coolio-iglesias@yandex.ru)</li>
	 *         </ul>
	 *
	 */
	protected static class TidResourceKey {
		Object resourceId;
		long tid;

		public TidResourceKey(Object resourceId, long tid) {
			super();
			this.resourceId = resourceId;
			this.tid = tid;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.resourceId == null) ? 0 : this.resourceId.hashCode());
			result = prime * result + (int) (this.tid ^ (this.tid >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj )
				return true;
			if ( obj == null )
				return false;
			if ( !(obj instanceof TidResourceKey) )
				return false;
			TidResourceKey other = (TidResourceKey) obj;
			if ( this.resourceId == null ) {
				if ( other.resourceId != null )
					return false;
			} else if ( !this.resourceId.equals(other.resourceId) )
				return false;
			if ( this.tid != other.tid )
				return false;
			return true;
		}

	}
}
