package zur13.checkpoint;

import java.util.concurrent.TimeUnit;

/**
 * Abstract checkpoint structure.
 * Checkpoint is the way to organize restricted sections. Restricted sections are somewhat similar to the critical sections (synchronize blocks) but allows multiple threads to access same section. 
 * <br/><br/>
 * Checkpoint gives instance of the Pass object to one of the waiting threads (applicants) after previous thread exits restricted access section and close its pass. Pass instance should be closed upon exiting restricted section. 
 * <br/><br/>
 * Checkpoint works with the resource ids to restrict simultaneous access to the restricted section. Each resource id allows multiple simultaneously active RO passes and a single active RW pass (RW access granted when there are no active RO passes). 
 * <br/><br/>
 * The preferable way to organize restricted section is to use try-with-resource block:
 * <pre> {@code
 * try (Pass p = checkpoint.getPassUninterruptibly(resourceId)) {
 * // restricted access section here<br/>
 * }
 * }</pre>
 * <br/><br/>
 * @author
 *         <ul>
 *         <li>Yurii Polianytsia (coolio-iglesias@yandex.ru)</li>
 *         </ul>
 */
public abstract class ACheckpoint implements ICheckpoint {
	protected String name = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.checkpoint.ICheckpoint#getPass(java.lang.Object)
	 */
	@Override
	public abstract Pass getPass(Object resourceId) throws InterruptedException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.checkpoint.ICheckpoint#getPassUninterruptibly(java.lang.Object)
	 */
	@Override
	public abstract Pass getPassUninterruptibly(Object resourceId);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.checkpoint.ICheckpoint#tryGetPass(java.lang.Object)
	 */
	@Override
	public abstract Pass tryGetPass(Object resourceId);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.checkpoint.ICheckpoint#tryGetPass(java.lang.Object, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public abstract Pass tryGetPass(Object resourceId, long timeout, TimeUnit unit) throws InterruptedException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.checkpoint.ICheckpoint#getPassRW(java.lang.Object)
	 */
	@Override
	public abstract Pass getPassRW(Object resourceId) throws InterruptedException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.checkpoint.ICheckpoint#getPassRWUninterruptibly(java.lang.Object)
	 */
	@Override
	public abstract Pass getPassRWUninterruptibly(Object resourceId);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.checkpoint.ICheckpoint#tryGetPassRW(java.lang.Object)
	 */
	@Override
	public abstract Pass tryGetPassRW(Object resourceId);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.checkpoint.ICheckpoint#tryGetPassRW(java.lang.Object, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public abstract Pass tryGetPassRW(Object resourceId, long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * Returns specified pass to this checkpoint.
	 * 
	 * @see Pass.close()
	 * @param pass
	 */
	protected abstract void returnPass(Pass pass);

	/**
	 * Gets checkpoint name.
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets checkpoint name.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		if ( name != null ) {
			return name;
		} else {
			return super.toString();
		}
	}

}
