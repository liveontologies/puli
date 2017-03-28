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

import java.util.Comparator;
import java.util.Set;

import org.liveontologies.puli.InferenceJustifier;
import org.liveontologies.puli.InferenceSet;

/**
 * A common interface for procedures that compute justifications of conclusions
 * from sets of inferences. Justification is a smallest set of axioms such that
 * there is a proof of the conclusion using only inferences with justifications
 * in this set.
 * 
 * @author Yevgeny Kazakov
 * @author Peter Skocovsky
 *
 * @param <C>
 *            the type of conclusion and premises used by the inferences
 * @param <A>
 *            the type of axioms used by the inferences
 */
public interface JustificationComputation<C, A> {

	/**
	 * Starts computation of justifications and notifies the provided listener
	 * about each new justification as soon as it is computed. The listener is
	 * notified exactly once for every justification. When the method returns,
	 * the listener must be notified about all the justifications.
	 * 
	 * @param conclusion
	 *            the conclusion for which to compute the justification
	 * @param listener
	 *            The listener that is notified about new justifications.
	 */
	void enumerateJustifications(C conclusion, Listener<A> listener);

	/**
	 * Starts computation of justifications and notifies the provided listener
	 * about each new justification as soon as it is computed. The listener is
	 * notified about justifications in the order defined by the provided
	 * {@link Comparator}. The listener is notified exactly once for every
	 * justification. When the method returns, the listener must be notified
	 * about all the justifications.
	 * <p>
	 * <strong>There is an additional constraint on the provided
	 * comparator!</strong> It must be compatible with subset ordering,
	 * otherwise the results are not guaranteed to be correct. Formally:
	 * <blockquote>If {@link Set#containsAll(java.util.Collection)
	 * set2.containsAll(set1) == true} and
	 * {@link Set#containsAll(java.util.Collection) set1.containsAll(set2) ==
	 * false} then {@link Comparator#compare(Object, Object) order.compare(set1,
	 * set2) < 0}.</blockquote>
	 * 
	 * @param conclusion
	 *            the conclusion for which to compute the justification
	 * @param order
	 *            The comparator that defines the order of justifications. The
	 *            listener is notified about new justifications in this order.
	 * @param listener
	 *            The listener that is notified about new justifications.
	 */
	void enumerateJustifications(C conclusion, Comparator<? super Set<A>> order,
			Listener<A> listener);

	public static interface Listener<A> {

		void newJustification(Set<A> justification);

		public static Listener<?> DUMMY = new Listener<Object>() {

			@Override
			public void newJustification(final Set<Object> justification) {
				// Empty.
			}

		};

	}

	public static Comparator<? super Set<?>> DEFAULT_ORDER = new Comparator<Set<?>>() {
		@Override
		public int compare(final Set<?> just1, final Set<?> just2) {
			final int size1 = just1.size();
			final int size2 = just2.size();
			return (size1 < size2) ? -1 : ((size1 == size2) ? 0 : 1);
		}
	};

	/**
	 * Factory for creating computations
	 * 
	 * @author Yevgeny Kazakov
	 * 
	 * @param <C>
	 *            the type of conclusion and premises used by the inferences
	 * @param <A>
	 *            the type of axioms used by the inferences
	 */
	static interface Factory<C, A> {

		/**
		 * @param inferenceSet
		 * @param justifier
		 * @param monitor
		 * @return a new justification computation which uses the given
		 *         inference set and inference justifier
		 */
		JustificationComputation<C, A> create(InferenceSet<C> inferenceSet,
				InferenceJustifier<C, ? extends Set<? extends A>> justifier,
				InterruptMonitor monitor);

	}

}
