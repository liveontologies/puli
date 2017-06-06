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
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class Proofs {

	@SuppressWarnings("rawtypes")
	public static GenericDynamicProof EMPTY_PROOF = new EmptyProof();

	@SuppressWarnings("unchecked")
	public static <C, I extends Inference<C>> GenericDynamicProof<C, I> emptyProof() {
		return (GenericDynamicProof<C, I>) EMPTY_PROOF;
	}

	public static <C> boolean isDerivable(Proof<C> proof, C conclusion) {
		return ProofNodes.isDerivable(ProofNodes.create(proof, conclusion));
	}

	public static <C> boolean isDerivable(Proof<C> proof, C conclusion,
			Set<C> statedAxioms) {
		return ProofNodes.isDerivable(ProofNodes.create(proof, conclusion),
				statedAxioms);
	}

	public static <C, I extends Inference<C>> GenericProof<C, I> combine(
			final Iterable<? extends GenericProof<C, I>> proofs) {
		return new CombinedProof<C, I>(proofs);
	}

	public static <C, I extends Inference<C>> GenericProof<C, I> combine(
			final GenericProof<C, I>... proofs) {
		return new CombinedProof<C, I>(proofs);
	}

	public static <C> Proof<C> addAssertedInferences(final Proof<C> inferences,
			final Set<? extends C> asserted) {
		return new AddAssertedProof<C>(inferences, asserted);
	}

	public static <C> DynamicProof<C> cache(DynamicProof<C> inferences) {
		return new CachingProof<C>(inferences);
	}

	public static <C> InferenceJustifier<C, ? extends Set<? extends C>> justifyAssertedInferences() {
		return AssertedConclusionInferenceJustifier.getInstance();
	}

	public static <C> Set<C> unfoldRecursively(Proof<C> inferences, C goal,
			Producer<Inference<C>> producer) {
		Set<C> result = new HashSet<C>();
		Queue<C> toExpand = new ArrayDeque<C>();
		result.add(goal);
		toExpand.add(goal);
		for (;;) {
			C next = toExpand.poll();
			if (next == null) {
				break;
			}
			for (Inference<C> inf : inferences.getInferences(next)) {
				producer.produce(inf);
				for (C premise : inf.getPremises()) {
					if (result.add(premise)) {
						toExpand.add(premise);
					}
				}
			}
		}
		return result;
	}

	/**
	 * @param inferences
	 * @param goal
	 * @return the set of conclusions without which the goal would not be
	 *         derivable using the given inferences; i.e., every derivation
	 *         using the inferences must use at least one essential conclusion
	 */
	public static <C> Set<C> getEssentialConclusions(Proof<C> inferences,
			C goal) {
		Set<C> result = new HashSet<C>();
		DerivabilityCheckerWithBlocking<C> checker = new InferenceDerivabilityChecker<C>(
				inferences);
		for (C candidate : unfoldRecursively(inferences, goal,
				Producer.Dummy.<Inference<C>> get())) {
			checker.block(candidate);
			if (!checker.isDerivable(goal)) {
				result.add(candidate);
			}
			checker.unblock(candidate);
		}
		return result;
	}

	/**
	 * Adds to the set of conclusions all conclusions that are derived under the
	 * inferences used for deriving the given goal; produces the applied
	 * inferences using the given producer
	 * 
	 * @param derivable
	 * @param inferences
	 * @param goal
	 * @param producer
	 */
	public static <C> void expand(Set<C> derivable, Proof<C> inferences, C goal,
			Producer<Inference<C>> producer) {
		InferenceExpander.expand(derivable, inferences, goal, producer);
	}

	/**
	 * @param inferences
	 * @param goal
	 * @param asserted
	 * @return a proof obtained from the given proofs by removing some
	 *         inferences that do not have effect on the derivation relation
	 *         between subsets of the given asserted conclusion and the goal
	 *         conclusion; i.e., if the goal conclusion was derivable from some
	 *         subset of asserted conclusions using original inferences, then it
	 *         is also derivable using the returned inferences
	 */
	public static <C> Proof<C> prune(Proof<C> inferences, C goal,
			Set<C> asserted) {
		return new PrunedProof<C>(inferences, goal, asserted);
	}

}
