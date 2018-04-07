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

/**
 * Checkpoint instance gives passes to the applicant when it allowed to access restricted section for the required
 * resource. <br>
 * Every applicant which was granted the pass should close it as soon as it leaves restricted section.
 *
 * @author
 *         <ul>
 *         <li>Yurii Polianytsia</li>
 *         </ul>
 *
 */
public class Pass implements AutoCloseable {
	protected Object resourceId;
	protected ACheckpoint checkpoint;
	protected boolean isReadOnly = false;

	public Pass(Object id, ACheckpoint checkpoint) {
		super();
		this.resourceId = id;
		this.checkpoint = checkpoint;
	}

	public Pass(Object id, ACheckpoint checkpoint, boolean isRO) {
		super();
		this.resourceId = id;
		this.checkpoint = checkpoint;
		this.isReadOnly = isRO;
	}

	/**
	 * Gets id of resource.
	 * 
	 * @return
	 */
	public Object getResourceId() {
		return this.resourceId;
	}

	/**
	 * Gets checkpoint which granted this pass.
	 * 
	 * @return
	 */
	public ACheckpoint getCheckpoint() {
		return this.checkpoint;
	}

	/**
	 * Notifies checkpoint that applicant leaves restricted section.
	 */
	@Override
	public void close() {
		checkpoint.returnPass(this);
	}

	/**
	 * Checks if the pass has RO access.
	 * 
	 * @return true if RO; false if RW
	 */
	public boolean isReadOnly() {
		return this.isReadOnly;
	}

	@Override
	public String toString() {
		return "Pass [resourceId=" + this.resourceId + ", checkpoint=" + this.checkpoint + ", isReadOnly="
				+ this.isReadOnly + "]";
	}	
}
