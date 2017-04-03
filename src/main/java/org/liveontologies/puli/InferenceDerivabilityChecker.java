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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;

/**
 * A utility to check derivability of conclusions by inferences (in the presence
 * of blocked conclusions). A conclusion is derivable if it is not blocked and a
 * conclusion of an inference whose all premises are (recursively) derivable.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of conclusions supported by this checker
 */
public class InferenceDerivabilityChecker<C>
		implements DerivabilityCheckerWithBlocking<C> {

	// logger for this class
	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(InferenceDerivabilityChecker.class);

	/**
	 * the inferences that can be used for deriving conclusions
	 */
	private final InferenceSet<C> inferences_;

	/**
	 * conclusions that cannot be used in the derivations
	 */
	private final Set<C> blocked_ = new HashSet<C>();

	/**
	 * {@link #goals_} that that were found derivable
	 */
	private final Set<C> derivable_ = new HashSet<C>();

	/**
	 * a map from a conclusion not in {@link #derivable_} to
	 * {@link #inferences_} that have this conclusion as one of the premises;
	 * intuitively, these inferences are "waiting" for this conclusion to be
	 * derived
	 */
	private final ListMultimap<C, Inference<C>> watchedInferences_ = ArrayListMultimap
			.create();

	/**
	 * a mirror map corresponding to {@link #watchedInferences_} that points to
	 * the positions of premises in the respective inferences
	 */
	private final ListMultimap<C, Integer> watchPremisePositions_ = ArrayListMultimap
			.create();

	/**
	 * a map from a {@link #derivable_} conclusion to {@link #inferences_} whose
	 * all premises are also in {@link #derivable_}; intuitively, these
	 * inferences are used in the derivations
	 */
	private final SetMultimap<C, Inference<C>> firedInferencesByPremises_ = HashMultimap
			.create();

	/**
	 * a map containing inferences in {@link #firedInferencesByPremises_} with a
	 * key for every premise of such inference
	 */
	private final ListMultimap<C, Inference<C>> firedInferencesByConclusions_ = ArrayListMultimap
			.create();

	/**
	 * a map from conclusions to iterators over all inferences in
	 * {@link #inferences_} with these conclusions that are neither present in
	 * {@link #watchedInferences_} nor in {@link #firedInferencesByConclusions_}
	 */
	private final Map<C, Queue<Inference<C>>> remainingInferences_ = new HashMap<C, Queue<Inference<C>>>();

	/**
	 * conclusions for which a derivability test was initiated or finished
	 */
	private final Set<C> goals_ = new HashSet<C>();

	/**
	 * {@link #goals_} that needs to be checked for derivability; they should
	 * not be in {@link #blocked_}
	 */
	private final Deque<C> toCheck_ = new ArrayDeque<C>(128);

	private final Deque<C> toSetUnknown_ = new ArrayDeque<C>(128);

	/**
	 * {@link #derivable_} goals which may have some {@link #watchedInferences_}
	 */
	private final Queue<C> toPropagate_ = new LinkedList<C>();

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

	@Override
	public Set<C> getBlockedConclusions() {
		return blocked_;
	}

	@Override
	public boolean block(C conclusion) {
		if (blocked_.add(conclusion)) {
			LOGGER_.trace("{}: blocked", conclusion);
			setUnknown(conclusion);
			process();
			return true;
		}
		// else
		return false;
	}

	@Override
	public boolean unblock(C conclusion) {
		if (blocked_.remove(conclusion)) {
			LOGGER_.trace("{}: unblocked", conclusion);
			if (derivable_.contains(conclusion)) {
				toPropagate_.add(conclusion);
			} else if (goals_.contains(conclusion)) {
				toCheck_.addFirst(conclusion);
			}
			process();
			return true;
		}
		// else
		return false;
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

	private void toCheck(C conclusion) {
		if (blocked_.contains(conclusion)) {
			return;
		}
		if (goals_.add(conclusion)) {
			LOGGER_.trace("{}: new goal", conclusion);
			toCheck_.addFirst(conclusion);
		}
	}

	private void derivable(C conclusion) {
		if (derivable_.add(conclusion)) {
			LOGGER_.trace("{}: derived", conclusion);
			if (!blocked_.contains(conclusion)) {
				toPropagate_.add(conclusion);
			}
		}
	}

	private void process() {
		for (;;) {
			// propagating derivable inferences with the highest priority
			C derivable = toPropagate_.poll();

			if (derivable != null) {
				List<Inference<C>> watched = watchedInferences_
						.removeAll(derivable);
				List<Integer> positions = watchPremisePositions_
						.removeAll(derivable);
				for (int i = 0; i < watched.size(); i++) {
					Inference<C> inf = watched.get(i);
					int pos = positions.get(i);
					check(pos, inf);
				}
				continue;
			}

			// expanding inferences if there is nothing to propagate
			C unknown = toCheck_.peek();
			if (unknown != null) {
				if (derivable_.contains(unknown)) {
					toCheck_.poll();
					continue;
				}
				Queue<Inference<C>> inferences = getRemainingInferences(
						unknown);
				Inference<C> inf = inferences.poll();
				if (inf == null) {
					toCheck_.poll();
					continue;
				}
				LOGGER_.trace("{}: expanding", inf);
				check(0, inf);
				continue;
			}

			// all done
			return;
		}

	}

	private Queue<Inference<C>> getRemainingInferences(C conclusion) {
		Queue<Inference<C>> result = remainingInferences_.get(conclusion);
		if (result == null) {
			result = new ArrayDeque<Inference<C>>(
					inferences_.getInferences(conclusion));
			remainingInferences_.put(conclusion, result);
		}
		return result;
	}

	private void check(int pos, Inference<C> inf) {
		List<? extends C> premises = inf.getPremises();
		int premiseCount = premises.size();
		int premisesChecked = 0;
		for (;;) {
			if (premisesChecked == premiseCount) {
				// all premises are derived
				fire(inf);
				return;
			}
			C premise = premises.get(pos);
			if (!derivable_.contains(premise)) {
				addWatch(premise, pos, inf);
				return;
			}
			pos++;
			if (pos == premiseCount) {
				pos = 0;
			}
			premisesChecked++;
		}
	}

	private void fire(Inference<C> inf) {
		LOGGER_.trace("{}: fire", inf);
		C conclusion = inf.getConclusion();
		derivable(conclusion);
		firedInferencesByConclusions_.put(inf.getConclusion(), inf);
		List<? extends C> premises = inf.getPremises();
		for (int pos = 0; pos < premises.size(); pos++) {
			firedInferencesByPremises_.put(premises.get(pos), inf);
		}
	}

	private void addWatch(C premise, int pos, Inference<C> inf) {
		LOGGER_.trace("{}: watching position {}", inf, pos);
		List<Inference<C>> inferences = watchedInferences_.get(premise);
		List<Integer> positions = watchPremisePositions_.get(premise);
		inferences.add(inf);
		positions.add(pos);
		toCheck(premise);
	}

	void setUnknown(C conclusion) {
		toSetUnknown_.add(conclusion);
		for (;;) {
			conclusion = toSetUnknown_.poll();
			if (conclusion == null) {
				break;
			}
			if (!derivable_.remove(conclusion)) {
				continue;
			}
			// else was derivable
			LOGGER_.trace("{}: unknown goal", conclusion);
			if (!blocked_.contains(conclusion)) {
				toCheck_.addLast(conclusion);
			}
			List<Inference<C>> fired = firedInferencesByConclusions_
					.removeAll(conclusion);
			for (Inference<C> inf : fired) {
				for (C premise : inf.getPremises()) {
					firedInferencesByPremises_.remove(premise, inf);
				}
			}
			getRemainingInferences(conclusion).addAll(fired);
			for (Inference<C> inf : firedInferencesByPremises_
					.get(conclusion)) {
				toSetUnknown_.add(inf.getConclusion());
			}
		}
	}

}
