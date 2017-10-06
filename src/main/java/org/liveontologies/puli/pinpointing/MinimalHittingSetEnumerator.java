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
package org.liveontologies.puli.pinpointing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.liveontologies.puli.Inference;
import org.liveontologies.puli.InferenceJustifier;
import org.liveontologies.puli.Proof;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class MinimalHittingSetEnumerator<E> implements
		MinimalSubsetEnumerator.Factory<Collection<? extends Set<? extends E>>, E> {

	private static final Object CONCLUSION_ = new Object();

	private final MinimalSubsetsFromProofs.Factory<SetWrapperInference, E> repairComputationFactory_;

	private final InterruptMonitor monitor_;

	public MinimalHittingSetEnumerator(
			final MinimalSubsetsFromProofs.Factory<SetWrapperInference, E> repairComputationFactory,
			final InterruptMonitor monitor) {
		this.repairComputationFactory_ = repairComputationFactory;
		this.monitor_ = monitor;
	}

	@Override
	public MinimalSubsetEnumerator<E> newEnumerator(
			final Collection<? extends Set<? extends E>> query) {
		return new Enumerator(query);
	}

	private class Enumerator extends AbstractMinimalSubsetEnumerator<E> {

		private final Collection<? extends Set<? extends E>> originalSets_;

		private Enumerator(
				final Collection<? extends Set<? extends E>> originalSets) {
			this.originalSets_ = originalSets;
		}

		@Override
		public void enumerate(final Listener<E> listener,
				final PriorityComparator<? super Set<E>, ?> priorityComparator) {

			final Proof<SetWrapperInference> proof = new SetWrapperProof(
					originalSets_);

			final MinimalSubsetEnumerator.Factory<Object, E> computation = repairComputationFactory_
					.create(proof, setWrapperJustifier_, monitor_);
			final MinimalSubsetEnumerator<E> enumerator = computation
					.newEnumerator(CONCLUSION_);

			enumerator.enumerate(listener, priorityComparator);
		}

	}

	private class SetWrapperProof implements Proof<SetWrapperInference> {

		private final Collection<? extends Set<? extends E>> originalSets_;

		private SetWrapperProof(
				final Collection<? extends Set<? extends E>> originalSets) {
			this.originalSets_ = originalSets;
		}

		@Override
		public Collection<? extends SetWrapperInference> getInferences(
				final Object conclusion) {
			if (conclusion == CONCLUSION_) {
				return Collections2.transform(originalSets_,
						new Function<Set<? extends E>, SetWrapperInference>() {

							@Override
							public SetWrapperInference apply(
									final Set<? extends E> originalSet) {
								return new SetWrapperInference(originalSet);
							}

						});
			}
			// else
			return Collections.emptySet();
		}

	}

	private class SetWrapperInference implements Inference<Object> {

		private final Set<? extends E> originalSet_;

		private SetWrapperInference(final Set<? extends E> originalSet) {
			this.originalSet_ = originalSet;
		}

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public Object getConclusion() {
			return CONCLUSION_;
		}

		@Override
		public List<? extends Object> getPremises() {
			return Collections.emptyList();
		}

	}

	private final InferenceJustifier<SetWrapperInference, Set<? extends E>> setWrapperJustifier_ = new InferenceJustifier<SetWrapperInference, Set<? extends E>>() {

		@Override
		public Set<? extends E> getJustification(
				final SetWrapperInference inference) {
			return inference.originalSet_;
		}

	};

	public static <E> Collection<? extends Set<? extends E>> compute(
			final Collection<? extends Set<? extends E>> sets) {

		final Collection<Set<? extends E>> result = new ArrayList<Set<? extends E>>();

		final MinimalHittingSetEnumerator<E> computation = new MinimalHittingSetEnumerator<E>(
				TopDownRepairComputation.<MinimalHittingSetEnumerator<E>
						.SetWrapperInference, E> getFactory(),
				InterruptMonitor.DUMMY);
		computation.newEnumerator(sets)
				.enumerate(new MinimalSubsetCollector<E>(result));

		return result;
	}

}
