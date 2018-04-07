package test.zur13.checkpoint.junit;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import zur13.checkpoint.ACheckpoint;
import zur13.checkpoint.CheckpointBuilder;
import zur13.checkpoint.Pass;

public class SimpleTests {
	ACheckpoint cp = CheckpointBuilder.newInst().setName("CheckpointNoGlobalLimit").setMaxPassesPerResource(1)
			.setReentrant(false).build();
	ACheckpoint cp2 = CheckpointBuilder.newInst().setName("CheckpointUseGlobalLimit").setGlobalPassesLimit(2)
			.setMaxPassesPerResource(1).setReentrant(false).build();

	ACheckpoint cp3 = CheckpointBuilder.newInst().setName("CheckpointNoGlobalLimit").setMaxPassesPerResource(2)
			.setReentrant(false).build();
	@Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 seconds max per method tested

	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testTryGetPassObjectLongTimeUnit() {
		try (Pass p = cp.tryGetPass("green", 1, TimeUnit.SECONDS)) {
			assertTrue("Thread was not given single access to resource", p != null);
			try (Pass p1 = cp.tryGetPass("green", 1, TimeUnit.SECONDS)) {
				assertTrue("Max resource passes limit ignored", p1 == null);
			}
		} catch (InterruptedException e) {
			fail("Test interrupted");
		}
		
		try (Pass p = cp.tryGetPass("red", 1, TimeUnit.SECONDS)) {
			try (Pass p1 = cp.tryGetPass("blue", 1, TimeUnit.SECONDS)) {
				assertTrue("Thread was not allowed to access 2 different resources", p != null && p1 != null);
			}
		} catch (InterruptedException e) {
			fail("Test interrupted");
		}

		try (Pass p1 = cp.tryGetPass("yellow", 1, TimeUnit.SECONDS)) {
			try (Pass p2 = cp.tryGetPass("cyan", 1, TimeUnit.SECONDS)) {
				try (Pass p3 = cp.tryGetPass("yellow", 1, TimeUnit.SECONDS)) {
					assertTrue("Max resource passes limit ignored when 2 resources used", p3 == null);
				}
			}
		} catch (InterruptedException e) {
			fail("Test interrupted");
		}

		try (Pass p1 = cp2.tryGetPass("yellow", 1, TimeUnit.SECONDS)) {
			try (Pass p2 = cp2.tryGetPass("cyan", 1, TimeUnit.SECONDS)) {
				try (Pass p3 = cp2.tryGetPass("green", 1, TimeUnit.SECONDS)) {
					assertTrue("Global passes limit ignored when 2 resources used", p3 == null);
				}
			}
		} catch (InterruptedException e) {
			fail("Test interrupted");
		}
	}

	@Test
	public void testTryGetPassRWObjectLongTimeUnit() {
		try (Pass p = cp3.tryGetPassRW("green", 1, TimeUnit.SECONDS)) {
			assertTrue("Thread was not given single access to resource RW", p != null);
			try (Pass p1 = cp3.tryGetPassRW("green", 1, TimeUnit.SECONDS)) {
				assertTrue("Thread was given second access to resource RW", p1 == null);
			}
		} catch (InterruptedException e) {
			fail("Test interrupted");
		}
		
		try (Pass p = cp3.tryGetPassRW("red", 1, TimeUnit.SECONDS)) {
			try (Pass p1 = cp3.tryGetPassRW("blue", 1, TimeUnit.SECONDS)) {
				assertTrue("Thread was not allowed to access 2 different resources RW", p != null && p1 != null);
			}
		} catch (InterruptedException e) {
			fail("Test interrupted");
		}

		try (Pass p1 = cp3.tryGetPassRW("yellow", 1, TimeUnit.SECONDS)) {
			try (Pass p2 = cp3.tryGetPassRW("cyan", 1, TimeUnit.SECONDS)) {
				try (Pass p3 = cp3.tryGetPassRW("yellow", 1, TimeUnit.SECONDS)) {
					assertTrue("Thread was given second access to resource when 2 resources used RW", p3 == null);
				}
			}
		} catch (InterruptedException e) {
			fail("Test interrupted");
		}

		try (Pass p1 = cp2.tryGetPassRW("yellow", 1, TimeUnit.SECONDS)) {
			try (Pass p2 = cp2.tryGetPassRW("cyan", 1, TimeUnit.SECONDS)) {
				try (Pass p3 = cp2.tryGetPassRW("green", 1, TimeUnit.SECONDS)) {
					assertTrue("Global passes limit ignored when 2 resources used RW", p3 == null);
				}
			}
		} catch (InterruptedException e) {
			fail("Test interrupted");
		}
	}

}
