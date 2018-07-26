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

public class ResourceDataFactory {
	int maxActivePassesPerResource;
	boolean fair;

	/**
	 * Instantiate a resource data factory object.
	 * 
	 * @param maxActivePassesPerResource
	 *            max number of passes per resource
	 * @param fair
	 * @see java.util.concurrent.Semaphore
	 */
	public ResourceDataFactory(int maxActivePassesPerResource, boolean fair) {
		super();
		this.maxActivePassesPerResource = maxActivePassesPerResource;
		this.fair = fair;
	}

	public AResourceData getResourceData(Object resourceId) {
		return new ResourceData(resourceId, maxActivePassesPerResource, fair);
	}
}
