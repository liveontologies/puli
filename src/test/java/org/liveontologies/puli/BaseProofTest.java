/*-
 * #%L
 * Proof Utility Library
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2017 Live Ontologies Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.liveontologies.puli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BaseProofTest {

	@Test
	public void testAddingAndRemovingInferences() {

		BaseProof<Inference<Integer>> p = new BaseProof<Inference<Integer>>();
		assertEquals(0, p.getInferences(1).size());
		assertEquals(0, p.getInferences(2).size());

		p.produce(InferenceBuilder.<Integer> create().conclusion(1).premise(2)
				.build());
		p.produce(InferenceBuilder.<Integer> create().conclusion(2).premise(3)
				.premise(4).build());
		p.produce(InferenceBuilder.<Integer> create().conclusion(2).premise(3)
				.premise(1).build());
		assertEquals(1, p.getInferences(1).size());
		assertEquals(2, p.getInferences(2).size());

		p.clear();
		assertEquals(0, p.getInferences(1).size());
		assertEquals(0, p.getInferences(2).size());

	}

	@Test
	public void testProofListenerAfterAppearence() {

		BaseProof<Inference<Integer>> p = new BaseProof<Inference<Integer>>();
		p.produce(InferenceBuilder.<Integer> create().conclusion(1).premise(2)
				.build());

		ProofListener listener = new ProofListener();
		assertFalse(listener.wasNotified());
		p.addListener(listener);

		// If no conclusion was queried, nothing is guaranteed.

		// Add inference for a queried conclusion.
		p.getInferences(1);
		p.produce(InferenceBuilder.<Integer> create().conclusion(1).premise(3)
				.build());
		assertTrue(listener.wasNotified());

		// After one notification no more notifications are guaranteed.
	}

	@Test
	public void testProofListenerAfterDeletion() {

		BaseProof<Inference<Integer>> p = new BaseProof<Inference<Integer>>();
		p.produce(InferenceBuilder.<Integer> create().conclusion(2).premise(3)
				.premise(4).build());
		p.produce(InferenceBuilder.<Integer> create().conclusion(2).premise(3)
				.premise(1).build());

		ProofListener listener = new ProofListener();
		assertFalse(listener.wasNotified());
		p.addListener(listener);

		// If no conclusion was queried, nothing is guaranteed.

		// Remove all inferences after querying some conclusion.
		p.getInferences(2);
		p.clear();
		assertTrue(listener.wasNotified());

		// After one notification no more notifications are guaranteed.
	}

	private static class ProofListener implements DynamicProof.ChangeListener {

		private boolean notificationReceived_ = false;

		@Override
		public void inferencesChanged() {
			notificationReceived_ = true;
		}

		public boolean wasNotified() {
			return notificationReceived_;
		}

	}

}
