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

import java.util.Set;

import org.liveontologies.puli.Inference;
import org.liveontologies.puli.InferenceJustifier;
import org.liveontologies.puli.Proof;

public class MinimalSubsetEnumerators {

	private MinimalSubsetEnumerators() {
		// Forbid instantiation of an utility class.
	}

	/**
	 * Uses the provided {@link MinimalSubsetEnumerator.Factory} to enumerate
	 * minimal subsets for the provided query and notifies the provided listener
	 * about the enumerated subsets.
	 * 
	 * @param query
	 * @param computation
	 * @param listener
	 */
	public static <Q, A> void enumerate(final Q query,
			final MinimalSubsetEnumerator.Factory<Q, A> computation,
			final MinimalSubsetEnumerator.Listener<A> listener) {
		final MinimalSubsetEnumerator<A> enumerator = computation
				.newEnumerator(query);
		enumerator.enumerate(listener);
	}

	/**
	 * Uses the provided {@link MinimalSubsetEnumerator.Factory} to enumerate
	 * minimal subsets for the provided query in the order defined by the
	 * provided comparator and notifies the provided listener about the
	 * enumerated subsets.
	 * <p>
	 * <strong>There is an additional constraint on the provided
	 * comparator!</strong> See
	 * {@link MinimalSubsetEnumerator#enumerate(MinimalSubsetEnumerator.Listener, PriorityComparator)}
	 * 
	 * @param query
	 * @param priorityComparator
	 * @param computation
	 * @param listener
	 */
	public static <Q, A> void enumerate(final Q query,
			final PriorityComparator<? super Set<A>, ?> priorityComparator,
			final MinimalSubsetEnumerator.Factory<Q, A> computation,
			final MinimalSubsetEnumerator.Listener<A> listener) {
		final MinimalSubsetEnumerator<A> enumerator = computation
				.newEnumerator(query);
		enumerator.enumerate(listener, priorityComparator);
	}

	/**
	 * Enumerates minimal subsets for the provided query from the provided proof
	 * and justifier by a computation created by the provided factory and
	 * notifies the provided listener about the enumerated subsets.
	 * 
	 * @param query
	 * @param proof
	 * @param justifier
	 * @param computationFactory
	 * @param monitor
	 * @param listener
	 */
	public static <I extends Inference<?>, A> void enumerate(final Object query,
			final Proof<? extends I> proof,
			final InferenceJustifier<? super I, ? extends Set<? extends A>> justifier,
			final MinimalSubsetsFromProofs.Factory<I, A> computationFactory,
			final InterruptMonitor monitor,
			final MinimalSubsetEnumerator.Listener<A> listener) {
		enumerate(query, computationFactory.create(proof, justifier, monitor),
				listener);
	}

	/**
	 * Enumerates minimal subsets for the provided query from the provided proof
	 * and justifier in the order defined by the provided comparator by a
	 * computation created by the provided factory and notifies the provided
	 * listener about the enumerated subsets.
	 * <p>
	 * <strong>There is an additional constraint on the provided
	 * comparator!</strong> See
	 * {@link MinimalSubsetEnumerator#enumerate(MinimalSubsetEnumerator.Listener, PriorityComparator)}
	 * 
	 * @param query
	 * @param proof
	 * @param justifier
	 * @param priorityComparator
	 * @param computationFactory
	 * @param monitor
	 * @param listener
	 */
	public static <I extends Inference<?>, A> void enumerate(final Object query,
			final Proof<? extends I> proof,
			final InferenceJustifier<? super I, ? extends Set<? extends A>> justifier,
			final PriorityComparator<? super Set<A>, ?> priorityComparator,
			final MinimalSubsetsFromProofs.Factory<I, A> computationFactory,
			final InterruptMonitor monitor,
			final MinimalSubsetEnumerator.Listener<A> listener) {
		enumerate(query, priorityComparator,
				computationFactory.create(proof, justifier, monitor), listener);
	}

	/**
	 * Enumerates justifications for the provided query from the provided proof
	 * and justifier using a {@link ResolutionJustificationComputation} and
	 * notifies the provided listener about the enumerated justifications.
	 * 
	 * @param query
	 * @param proof
	 * @param justifier
	 * @param monitor
	 * @param listener
	 */
	public static <I extends Inference<?>, A> void enumerateJustifications(
			final Object query, final Proof<? extends I> proof,
			final InferenceJustifier<? super I, ? extends Set<? extends A>> justifier,
			final InterruptMonitor monitor,
			final MinimalSubsetEnumerator.Listener<A> listener) {
		enumerate(query, proof, justifier,
				ResolutionJustificationComputation.<I, A> getFactory(), monitor,
				listener);
	}

	/**
	 * Enumerates justifications for the provided query from the provided proof
	 * and justifier in the order defined by the provided comparator using a
	 * {@link ResolutionJustificationComputation} and notifies the provided
	 * listener about the enumerated justifications.
	 * <p>
	 * <strong>There is an additional constraint on the provided
	 * comparator!</strong> See
	 * {@link MinimalSubsetEnumerator#enumerate(MinimalSubsetEnumerator.Listener, PriorityComparator)}
	 * 
	 * @param query
	 * @param proof
	 * @param justifier
	 * @param priorityComparator
	 * @param monitor
	 * @param listener
	 */
	public static <I extends Inference<?>, A> void enumerateJustifications(
			final Object query, final Proof<? extends I> proof,
			final InferenceJustifier<? super I, ? extends Set<? extends A>> justifier,
			final PriorityComparator<? super Set<A>, ?> priorityComparator,
			final InterruptMonitor monitor,
			final MinimalSubsetEnumerator.Listener<A> listener) {
		enumerate(query, proof, justifier, priorityComparator,
				ResolutionJustificationComputation.<I, A> getFactory(), monitor,
				listener);
	}

	/**
	 * Enumerates repairs for the provided query from the provided proof and
	 * justifier using a {@link TopDownRepairComputation} and notifies the
	 * provided listener about the enumerated repairs.
	 * 
	 * @param query
	 * @param proof
	 * @param justifier
	 * @param monitor
	 * @param listener
	 */
	public static <I extends Inference<?>, A> void enumerateRepairs(
			final Object query, final Proof<? extends I> proof,
			final InferenceJustifier<? super I, ? extends Set<? extends A>> justifier,
			final InterruptMonitor monitor,
			final MinimalSubsetEnumerator.Listener<A> listener) {
		enumerate(query, proof, justifier,
				TopDownRepairComputation.<I, A> getFactory(), monitor,
				listener);
	}

	/**
	 * Enumerates repairs for the provided query from the provided proof and
	 * justifier in the order defined by the provided comparator using a
	 * {@link TopDownRepairComputation} and notifies the provided listener about
	 * the enumerated repairs.
	 * <p>
	 * <strong>There is an additional constraint on the provided
	 * comparator!</strong> See
	 * {@link MinimalSubsetEnumerator#enumerate(MinimalSubsetEnumerator.Listener, PriorityComparator)}
	 * 
	 * @param query
	 * @param proof
	 * @param justifier
	 * @param priorityComparator
	 * @param monitor
	 * @param listener
	 */
	public static <I extends Inference<?>, A> void enumerateRepairs(
			final Object query, final Proof<? extends I> proof,
			final InferenceJustifier<? super I, ? extends Set<? extends A>> justifier,
			final PriorityComparator<? super Set<A>, ?> priorityComparator,
			final InterruptMonitor monitor,
			final MinimalSubsetEnumerator.Listener<A> listener) {
		enumerate(query, proof, justifier, priorityComparator,
				TopDownRepairComputation.<I, A> getFactory(), monitor,
				listener);
	}

}
