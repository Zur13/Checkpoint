package test.zur13.checkpoint.junit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import zur13.checkpoint.ACheckpoint;
import zur13.checkpoint.CheckpointBuilder;
import zur13.checkpoint.Pass;

public class MultithreadTest {
	protected final static int MAX_THREADS_PER_RESOURCE = 6;
	protected final static int MAX_RESOURCES = 3;
	protected final static int MAX_THREADS = MAX_THREADS_PER_RESOURCE * MAX_RESOURCES + MAX_THREADS_PER_RESOURCE * 2;

	ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);

	ACheckpoint cp = CheckpointBuilder.newInst().setName("CheckpointMaxPasses" + MAX_THREADS_PER_RESOURCE)
			.setMaxPassesPerResource(MAX_THREADS_PER_RESOURCE).setReentrant(false).build();

	protected AtomicInteger threadCounter1 = new AtomicInteger(0);
	protected AtomicInteger threadCounter2 = new AtomicInteger(0);

	@Rule
	public Timeout globalTimeout = Timeout.seconds(30); // 30 seconds max per method tested

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		runSingleTest();
	}

	/**
	 * This method should be entered only by limited number of threads simultaneously.
	 */
	protected void restrictedAccessMethod() {
		assertTrue("Max threads limit ignored", threadCounter1.incrementAndGet() <= MAX_THREADS_PER_RESOURCE);

		synchronized (threadCounter1) {
			if ( threadCounter1.get() < MAX_THREADS_PER_RESOURCE ) {
				try {
					threadCounter1.wait();
				} catch (InterruptedException e) {
					fail("Test interrupted");
				}
			} else {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				threadCounter1.notifyAll();
			}
		}
		threadCounter1.decrementAndGet();
	}

	protected void processResource(String resId) {
		try (Pass p = cp.getPassUninterruptibly(resId)) {
			threadCounter2.incrementAndGet();
			restrictedAccessMethod();
		}
		synchronized (threadCounter2) {
			if ( threadCounter2.get() == MAX_THREADS ) {
				threadCounter2.notifyAll();
			}
		}
	}

	protected void runSingleTest() {
		for (int i = 0; i < MAX_THREADS; i++) {
			pool.execute(new Runnable() {
				@Override
				public void run() {
					processResource("SingleResource");

				}
			});
		}
		synchronized (threadCounter2) {
			try {
				if ( threadCounter2.get() != MAX_THREADS ) {
					threadCounter2.wait();
				}
			} catch (InterruptedException e) {
			}
		}
		assertTrue("Some threads didn't got access to the restricted section", threadCounter2.get() == MAX_THREADS);
	}

}
