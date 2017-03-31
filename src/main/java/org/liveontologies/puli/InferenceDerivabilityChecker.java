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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * A utility to check derivability of conclusions by inferences. A conclusion is
 * derivable if it is a conclusion of an inference whose all premises are
 * (recursively) derivable.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of conclusions supported by this checker
 */
public class InferenceDerivabilityChecker<C> implements DerivabilityChecker<C> {

	// logger for this class
	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(InferenceDerivabilityChecker.class);

	/**
	 * the inferences that can be used for deriving conclusions
	 */
	private final InferenceSet<C> inferences_;

	/**
	 * conclusions for which a derivability test was initiated or finished
	 */
	private final Set<C> goals_ = new HashSet<C>();

	/**
	 * {@link #goals_} that are not yet checked for derivability
	 */
	private final Queue<C> toCheck_ = new ArrayDeque<C>(128);

	/**
	 * {@link #goals_} that that were found derivable
	 */
	private final Set<C> derivable_ = new HashSet<C>();

	/**
	 * queue of iterators over yet unexpanded inferences for conclusions; when
	 * inference is expanded, derivibility of its premises is recursively
	 * checked; all iterators must have the next elements
	 */
	private final Deque<Iterator<? extends Inference<C>>> toExpand_ = new ArrayDeque<Iterator<? extends Inference<C>>>();

	/**
	 * {@link #derivable_} goals not yet used to derived other {@link #goals_}
	 */
	private final Queue<C> toPropagate_ = new LinkedList<C>();

	/**
	 * a map from {@link #toCheck_} goals to the list of inferences in which
	 * this goal can be used as a premise; these inferences are "waiting" for
	 * this conclusion to be derived
	 */
	private final ListMultimap<C, Inference<C>> watchedInferences_ = ArrayListMultimap
			.create();

	/**
	 * a map from {@link #toCheck_} goals to the iterator over the premises of
	 * the corresponding inference in {@link #watchedInferences_} that currently
	 * points to this goal (as it is one of the premises)
	 */
	private final ListMultimap<C, Iterator<? extends C>> premiseIteratorsMap_ = ArrayListMultimap
			.create();

	public InferenceDerivabilityChecker(InferenceSet<C> inferences) {
		Preconditions.checkNotNull(inferences);
		this.inferences_ = inferences;
	}

	@Override
	public boolean isDerivable(C conclusion) {
		LOGGER_.trace("{}: checking derivability", conclusion);
		toCheck(conclusion);
		process();
		boolean derivable = derivable_.contains(conclusion);
		LOGGER_.trace("{}: derivable: {}", conclusion, derivable);
		return derivable;
	}

	/**
	 * @return all conclusions that could not be derived in tests for
	 *         derivability. It guarantees to contain all conclusions for which
	 *         {@link #isDerivable(Object)} returns {@code false} and also at
	 *         least one premise for each inference producing an element in this
	 *         set. But this set may also grow if {@link #isDerivable(Object)}
	 *         returns {@code true} (e.g., if the conclusion is derivable by one
	 *         inference but has another inference in which some premise is not
	 *         derivable). This set is mostly useful for debugging issues with
	 *         derivability.
	 */
	public Set<? extends C> getNonDerivableConclusions() {
		return watchedInferences_.keySet();
	}

	private void process() {
		for (;;) {
			C next = toCheck_.poll();

			if (next != null) {
				Iterator<? extends Inference<C>> inferences = inferences_
						.getInferences(next).iterator();
				if (inferences.hasNext()) {
					toExpand_.addFirst(inferences);
				}
				continue;
			}

			next = toPropagate_.poll();

			if (next != null) {
				List<Inference<C>> watched = watchedInferences_.removeAll(next);
				if (watched == null) {
					continue;
				}
				List<Iterator<? extends C>> premiseIterators = premiseIteratorsMap_
						.removeAll(next);
				for (int i = 0; i < watched.size(); i++) {
					Inference<C> inf = watched.get(i);
					Iterator<? extends C> iterator = premiseIterators.get(i);
					check(iterator, inf);
				}
				continue;
			}

			Iterator<? extends Inference<C>> inferences = toExpand_.peekFirst();
			if (inferences != null) {
				Inference<C> inf = inferences.next();
				if (derivable_.contains(inf.getConclusion())) {
					toExpand_.remove();
					continue;
				}
				LOGGER_.trace("{}: expanding", inf);
				check(inf.getPremises().iterator(), inf);
				if (!inferences.hasNext()) {
					toExpand_.remove();
				}
				continue;
			}

			// all done
			return;

		}

	}

	private void toCheck(C conclusion) {
		if (goals_.add(conclusion)) {
			LOGGER_.trace("{}: new goal", conclusion);
			toCheck_.add(conclusion);
		}
	}

	private void addWatch(C premise, Iterator<? extends C> premiseIterator,
			Inference<C> inf) {
		List<Inference<C>> inferences = watchedInferences_.get(premise);
		List<Iterator<? extends C>> premiseIterators = premiseIteratorsMap_
				.get(premise);
		inferences.add(inf);
		premiseIterators.add(premiseIterator);
		toCheck(premise);
	}

	private void proved(C conclusion) {
		if (derivable_.add(conclusion)) {
			LOGGER_.trace("{}: derived", conclusion);
			toPropagate_.add(conclusion);
		}
	}

	private void check(Iterator<? extends C> premiseIterator,
			Inference<C> inf) {
		while (premiseIterator.hasNext()) {
			C next = premiseIterator.next();
			if (!derivable_.contains(next)) {
				addWatch(next, premiseIterator, inf);
				return;
			}
		}
		// all premises are derived
		LOGGER_.trace("{}: fire", inf);
		proved(inf.getConclusion());
	}

}
