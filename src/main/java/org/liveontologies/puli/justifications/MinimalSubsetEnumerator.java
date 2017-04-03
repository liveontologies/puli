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

/**
 * A common interface for procedures that enumerate subset-minimal sets.
 * 
 * @author Yevgeny Kazakov
 * @author Peter Skocovsky
 *
 * @param <E>
 *            the type of elements of the enumerated sets
 */
public interface MinimalSubsetEnumerator<E> {

	/**
	 * Starts enumeration of subset-minimal sets and notifies the provided
	 * listener about each new set as soon as it is computed. The listener is
	 * notified exactly once for every set. When the method returns, the
	 * listener must be notified about all the subset-minimal sets.
	 * 
	 * @param listener
	 *            The listener that is notified about new subset-minimal set.
	 */
	void enumerate(Listener<E> listener);

	/**
	 * Starts computation of subset-minimal sets and notifies the provided
	 * listener about each new set as soon as it is computed. The listener is
	 * notified about the sets in the order defined by the provided
	 * {@link Comparator}. The listener is notified exactly once for every set.
	 * When the method returns, the listener must be notified about all the
	 * subset-minimal sets.
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
	 * @param order
	 *            The comparator that defines the order of justifications. The
	 *            listener is notified about new justifications in this order.
	 * @param listener
	 *            The listener that is notified about new justifications.
	 */
	void enumerate(Comparator<? super Set<E>> order, Listener<E> listener);

	public static interface Listener<A> {

		void newMinimalSubset(Set<A> set);

		public static Listener<?> DUMMY = new Listener<Object>() {

			@Override
			public void newMinimalSubset(final Set<Object> set) {
				// Empty.
			}

		};

	}

	public static Comparator<? super Set<?>> DEFAULT_ORDER = new Comparator<Set<?>>() {
		@Override
		public int compare(final Set<?> set1, final Set<?> set2) {
			final int size1 = set1.size();
			final int size2 = set2.size();
			return (size1 < size2) ? -1 : ((size1 == size2) ? 0 : 1);
		}
	};

	/**
	 * Factory that creates {@link MinimalSubsetEnumerator}s for the provided
	 * query.
	 * 
	 * @author Yevgeny Kazakov
	 * @author Peter Skocovsky
	 *
	 * @param <Q>
	 *            the type of the query
	 * @param <E>
	 *            the type of elements of the enumerated sets
	 */
	static interface Factory<Q, E> {

		/**
		 * @param query
		 * @return new {@link MinimalSubsetEnumerator} for the provided query.
		 */
		MinimalSubsetEnumerator<E> newEnumerator(Q query);

	}

}
