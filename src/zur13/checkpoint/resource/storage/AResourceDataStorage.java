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
