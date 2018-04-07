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

public class ReentrancyTest {
	ACheckpoint cp = CheckpointBuilder.newInst().setName("CheckpointNoGlobalLimit").setMaxPassesPerResource(1)
			.setReentrant(true).build();
	ACheckpoint cp2 = CheckpointBuilder.newInst().setName("CheckpointUseGlobalLimit").setGlobalPassesLimit(2)
			.setMaxPassesPerResource(1).setReentrant(true).build();

	@Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 seconds max per method tested
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testTryGetPassObjectLongTimeUnit() {
		boolean successSimple = false;
		boolean successMultiResource = false;
		boolean reentrancyGlobalLimitBypassed = false;
		boolean globalLimitIgnored = false;

		try (Pass p = cp.tryGetPass("green", 1, TimeUnit.SECONDS)) {
			try (Pass p1 = cp.tryGetPass("green", 1, TimeUnit.SECONDS)) {
				if ( p != null && p1 != null ) {
					successSimple = true;
				}
			}
			try (Pass p1 = cp.tryGetPass("red", 1, TimeUnit.SECONDS)) {
				try (Pass p2 = cp.tryGetPass("green", 1, TimeUnit.SECONDS)) {
					try (Pass p3 = cp.tryGetPass("red", 1, TimeUnit.SECONDS)) {
						if ( p != null && p1 != null && p2 != null && p3 != null ) {
							successMultiResource = true;
						}
					}
				}
			}
		} catch (InterruptedException e) {
			fail("Test interrupted");
		}
		try (Pass p = cp2.tryGetPass("cyan", 1, TimeUnit.SECONDS)) {
			try (Pass p1 = cp2.tryGetPass("cyan", 1, TimeUnit.SECONDS)) {
				try (Pass p2 = cp2.tryGetPass("cyan", 1, TimeUnit.SECONDS)) {
					if ( p != null && p1 != null && p2 != null ) {
						reentrancyGlobalLimitBypassed = true;
					}
				}
			}
			try (Pass p1 = cp2.tryGetPass("purple", 1, TimeUnit.SECONDS)) {
				try (Pass p2 = cp2.tryGetPass("violet", 1, TimeUnit.SECONDS)) {
					if ( p != null && p1 != null && p2 != null ) {
						globalLimitIgnored = true;
					}
				}
			}
		} catch (InterruptedException e) {
			fail("Test interrupted");
		}
		assertTrue(successSimple);
		assertTrue(successMultiResource);

		assertTrue(reentrancyGlobalLimitBypassed);
		assertFalse(globalLimitIgnored);
	}

	@Test
	public void testTryGetPassRWObjectLongTimeUnit() {
		boolean successSimple = false;
		boolean successMultiResource = false;
		boolean successDowngrade = false;
		boolean successUpgrade = false;
		boolean reentrancyGlobalLimitBypassed = false;
		boolean globalLimitIgnored = false;

		try (Pass p = cp.tryGetPassRW("blue", 1, TimeUnit.SECONDS)) {
			try (Pass p1 = cp.tryGetPassRW("blue", 1, TimeUnit.SECONDS)) {
				if ( p != null && p1 != null ) {
					successSimple = true;
				}
			}
			try (Pass p1 = cp.tryGetPassRW("white", 1, TimeUnit.SECONDS)) {
				try (Pass p2 = cp.tryGetPassRW("blue", 1, TimeUnit.SECONDS)) {
					try (Pass p3 = cp.tryGetPassRW("white", 1, TimeUnit.SECONDS)) {
						if ( p != null && p1 != null && p2 != null && p3 != null ) {
							successMultiResource = true;
						}
					}
				}
			}
			try (Pass p2 = cp.tryGetPass("blue", 1, TimeUnit.SECONDS)) {
				if ( p != null && p2 != null ) {
					successDowngrade = true;
				}
			}
		} catch (InterruptedException e) {
			fail("Test interrupted");
		}
		
		try (Pass p = cp.tryGetPass("black", 1, TimeUnit.SECONDS)) {
			try (Pass p1 = cp.tryGetPassRW("black", 1, TimeUnit.SECONDS)) {
				if ( p != null && p1 != null ) {
					successUpgrade = true;
				}
			}
		} catch (InterruptedException e) {
			fail("Test interrupted");
		} catch (UnsupportedOperationException e) {
			// Upgrade RO pass is not supported
		}

		try (Pass p = cp2.tryGetPassRW("cyan", 1, TimeUnit.SECONDS)) {
			try (Pass p1 = cp2.tryGetPassRW("cyan", 1, TimeUnit.SECONDS)) {
				try (Pass p2 = cp2.tryGetPassRW("cyan", 1, TimeUnit.SECONDS)) {
					if ( p != null && p1 != null && p2 != null ) {
						reentrancyGlobalLimitBypassed = true;	
					}
				}
			}
			try (Pass p1 = cp2.tryGetPassRW("purple", 1, TimeUnit.SECONDS)) {
				try (Pass p2 = cp2.tryGetPassRW("violet", 1, TimeUnit.SECONDS)) {
					if ( p != null && p1 != null && p2 != null ) {
						globalLimitIgnored = true;
					}
				}
			}
		} catch (InterruptedException e) {
			fail("Test interrupted");
		}
		assertTrue(successSimple);
		assertTrue(successMultiResource);
		assertTrue(successDowngrade);
		assertFalse(successUpgrade);
		
		assertTrue(reentrancyGlobalLimitBypassed);
		assertFalse(globalLimitIgnored);
	}

}
