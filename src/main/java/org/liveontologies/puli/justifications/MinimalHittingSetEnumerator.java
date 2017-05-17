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
package org.liveontologies.puli.justifications;

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

	private final MinimalSubsetsFromProofs.Factory<Object, E> repairComputationFactory_;

	private final InterruptMonitor monitor_;

	public MinimalHittingSetEnumerator(
			final MinimalSubsetsFromProofs.Factory<Object, E> repairComputationFactory,
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

			final Proof<Object> proof = new SetWrapperProof(
					originalSets_);

			final MinimalSubsetEnumerator.Factory<Object, E> computation = repairComputationFactory_
					.create(proof, setWrapperJustifier_, monitor_);
			final MinimalSubsetEnumerator<E> enumerator = computation
					.newEnumerator(CONCLUSION_);

			enumerator.enumerate(listener, priorityComparator);
		}

	}

	private class SetWrapperProof implements Proof<Object> {

		private final Collection<? extends Set<? extends E>> originalSets_;

		private SetWrapperProof(
				final Collection<? extends Set<? extends E>> originalSets) {
			this.originalSets_ = originalSets;
		}

		@Override
		public Collection<? extends Inference<Object>> getInferences(
				final Object conclusion) {
			if (conclusion == CONCLUSION_) {
				return Collections2.transform(originalSets_,
						new Function<Set<? extends E>, Inference<Object>>() {

							@Override
							public Inference<Object> apply(
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

	private final InferenceJustifier<Object, Set<? extends E>> setWrapperJustifier_ = new InferenceJustifier<Object, Set<? extends E>>() {

		@Override
		public Set<? extends E> getJustification(
				final Inference<Object> inference) {
			if (inference instanceof MinimalHittingSetEnumerator.SetWrapperInference) {
				return ((SetWrapperInference) inference).originalSet_;
			}
			// else
			return Collections.emptySet();
		}

	};

	public static <E> Collection<? extends Set<? extends E>> compute(
			final Collection<? extends Set<? extends E>> sets) {

		final Collection<Set<? extends E>> result = new ArrayList<Set<? extends E>>();

		final MinimalHittingSetEnumerator<E> computation = new MinimalHittingSetEnumerator<E>(
				TopDownRepairComputation.<Object, E> getFactory(),
				InterruptMonitor.DUMMY);
		computation.newEnumerator(sets)
				.enumerate(new MinimalSubsetCollector<E>(result));

		return result;
	}

}
