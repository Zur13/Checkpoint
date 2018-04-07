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
