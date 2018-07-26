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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import zur13.checkpoint.ACheckpoint;
import zur13.checkpoint.Pass;

/**
 * Stores active passes data for a single resource.
 *
 * @author
 *         <ul>
 *         <li>Yurii Polianytsia (coolio-iglesias@yandex.ru)</li>
 *         </ul>
 *
 */
public abstract class AResourceData {

	protected final Object resourceId;
	protected final AtomicLong refCounter = new AtomicLong(1);

	public AResourceData(Object resourceId) {
		super();
		this.resourceId = resourceId;
	}

	public Object getResourceId() {
		return this.resourceId;
	}

	public AtomicLong getRefCounter() {
		return this.refCounter;
	}

	/**
	 * Requests RO pass for this resource blocking until one is available, or the thread is interrupted. <br/>
	 * <br/>
	 * Multiple RO passes available for each resource but none can't be received while there is active RW pass. <br/>
	 * Awaits for RW pass return if there is active for specified resource and awaits for any RO pass return if no RO
	 * passes available for specified resource.
	 * 
	 * @param checkpoint
	 * @return resource pass
	 * @throws InterruptedException
	 */
	public abstract Pass getPass(ACheckpoint checkpoint) throws InterruptedException;

	/**
	 * Requests RO pass for this resource blocking until one is available. <br/>
	 * <br/>
	 * Multiple RO passes available for each resource but none can't be received while there is active RW pass. <br/>
	 * Awaits for RW pass return if there is active for specified resource and awaits for any RO pass return if no RO
	 * passes available for specified resource.
	 * 
	 * @param checkpoint
	 * @return resource pass
	 */
	public abstract Pass getPassUninterruptibly(ACheckpoint checkpoint);

	/**
	 * Requests RO pass for this resource, returns pass only if one is available at the time of invocation. <br/>
	 * <br/>
	 * Multiple RO passes available for each resource but none can't be received while there is active RW pass. <br/>
	 * Returns pass if there is at least one available RO pass and there is no active RW pass for specified resource at
	 * the time of invocation.
	 * 
	 * @param checkpoint
	 * @return resource pass or null if no RO passes available or RW pass active
	 */
	public abstract Pass tryGetPass(ACheckpoint checkpoint);

	/**
	 * Requests RO pass for this resource, returns pass if one becomes available within the given waiting time and
	 * the current thread has not been interrupted. <br/>
	 * <br/>
	 * Multiple RO passes available for each resource but none can't be received while there is active RW pass. <br/>
	 * Returns pass if there is at least one available RO pass and there is no active RW pass for specified resource at
	 * the time of invocation.
	 * <br/>
	 * <br/>
	 * WARN: timeout is not precise and may take double time until timeout happens in case of global pass limit used
	 * (internally there are 2 consecutive places where it is used) <br/>
	 * 
	 * @param checkpoint
	 * @return resource pass or null if no RO passes available or RW pass active
	 * @throws InterruptedException
	 */
	public abstract Pass tryGetPass(ACheckpoint checkpoint, long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * Requests RW pass for this resource blocking until one is available, or the thread is interrupted. <br/>
	 * <br/>
	 * Single RW pass available for each resource and it can't be received while there are active RO passes. <br/>
	 * Awaits for all RO and RW pass return if there are active for specified resource.
	 * 
	 * @param checkpoint
	 * @return resource pass
	 * @throws InterruptedException
	 */
	public abstract Pass getPassRW(ACheckpoint checkpoint) throws InterruptedException;

	/**
	 * Requests RW pass for this resource blocking until one is available. <br/>
	 * <br/>
	 * Single RW pass available for each resource and it can't be received while there are active RO passes. <br/>
	 * Awaits for all RO and RW pass return if there are active for specified resource.
	 * 
	 * @param checkpoint
	 * @return resource pass
	 */
	public abstract Pass getPassRWUninterruptibly(ACheckpoint checkpoint);

	/**
	 * Requests RW pass for this resource, returns pass only if one is available at the time of invocation. <br/>
	 * <br/>
	 * Single RW pass available for each resource and it can't be received while there are active RO passes. <br/>
	 * Returns pass if no active RO and RW passes for specified resource at the time of invocation.
	 * 
	 * @param checkpoint
	 * @return resource pass or null if no passes available
	 */
	public abstract Pass tryGetPassRW(ACheckpoint checkpoint);

	/**
	 * Requests RW pass for this resource, returns pass if one becomes available within the given waiting time and
	 * the current thread has not been interrupted. <br/>
	 * <br/>
	 * Single RW pass available for each resource and it can't be received while there are active RO passes. <br/>
	 * Returns pass if no active RO and RW passes for specified resource at the time of invocation.
	 * <br/>
	 * <br/>
	 * WARN: timeout is not precise and may take double time until timeout happens in case of global pass limit used
	 * (internally there are 2 consecutive places where it is used) <br/>
	 * 
	 * @param checkpoint
	 * @param timeout
	 *            the maximum time to wait for a permit
	 * @param unit
	 *            the time unit of the timeout argument
	 * @return resource pass or null if no passes available
	 * @throws InterruptedException
	 */
	public abstract Pass tryGetPassRW(ACheckpoint checkpoint, long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * Return Pass instance allowing new Pass for the resourceId.
	 * 
	 * @param pass
	 */
	public abstract void returnPass(Pass pass);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.resourceId == null) ? 0 : this.resourceId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( !(obj instanceof AResourceData) ) {
			return false;
		}
		AResourceData other = (AResourceData) obj;
		if ( this.resourceId == null ) {
			if ( other.resourceId != null ) {
				return false;
			}
		} else if ( !this.resourceId.equals(other.resourceId) ) {
			return false;
		}
		return true;
	}

}
