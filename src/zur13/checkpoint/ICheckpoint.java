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

import java.util.concurrent.TimeUnit;

/**
 * Checkpoint interface.
 *
 * @author
 *         <ul>
 *         <li>Yurii Polianytsia (coolio-iglesias@yandex.ru)</li>
 *         </ul>
 *
 */
public interface ICheckpoint {

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
	public Pass getPass(Object resourceId) throws InterruptedException;

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
	public Pass getPassUninterruptibly(Object resourceId);

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
	public Pass tryGetPass(Object resourceId);

	/**
	 * Requests RO pass for specified resource, returns pass if one becomes available within the given waiting time and
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
	 * @param resourceId
	 *            unique resource identifier
	 * @return resource pass or null if no RO passes available or RW pass active
	 * @throws InterruptedException
	 */
	public Pass tryGetPass(Object resourceId, long timeout, TimeUnit unit) throws InterruptedException;

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
	public Pass getPassRW(Object resourceId) throws InterruptedException;

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
	public Pass getPassRWUninterruptibly(Object resourceId);

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
	public Pass tryGetPassRW(Object resourceId);

	/**
	 * Requests RW pass for specified resource, returns pass if one becomes available within the given waiting time and
	 * the current thread has not been interrupted. <br/>
	 * <br/>
	 * Single RW pass available for each resource and it can't be received while there are active RO passes. <br/>
	 * Returns pass if no active RO and RW passes for specified resource at the time of invocation.
	 * <br/>
	 * <br/>
	 * WARN: timeout is not precise and may take double time until timeout happens in case of global pass limit used
	 * (internally there are 2 consecutive places where it is used) <br/>
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
	public Pass tryGetPassRW(Object resourceId, long timeout, TimeUnit unit) throws InterruptedException;

}