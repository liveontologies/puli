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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class Proofs {

	@SuppressWarnings("rawtypes")
	public static GenericDynamicProof EMPTY_PROOF = new EmptyProof();

	/**
	 * @return a {@link GenericDynamicProof} that has no inference, i.e.,
	 *         {@link GenericDynamicProof#getInferences(Object)} is always the
	 *         empty set. This proof never changes so if a
	 *         {@link DynamicProof.ChangeListener} is added, it does not receive
	 *         any notifications.
	 */
	@SuppressWarnings("unchecked")
	public static <C, I extends Inference<C>> GenericDynamicProof<C, I> emptyProof() {
		return (GenericDynamicProof<C, I>) EMPTY_PROOF;
	}

	/**
	 * @param proof
	 * @param conclusion
	 * @return {@code true} if the given conclusion is derivable in the given
	 *         {@link Proof}, i.e., there exists an sequence of conclusions
	 *         ending with the given conclusion, such that for each conclusion
	 *         there exists an inference in {@link Proof#getInferences} that has
	 *         as premises only conclusions that appear before in this sequence.
	 */
	public static <C> boolean isDerivable(Proof<C> proof, C conclusion) {
		return ProofNodes.isDerivable(ProofNodes.create(proof, conclusion));
	}

	/**
	 * @param proof
	 * @param conclusion
	 * @param assertedConclusions
	 * @return {@code true} if the given conclusion is derivable in the given
	 *         {@link Proof} starting from the given 'asserted' conclusions,
	 *         i.e., there exists an sequence of conclusions ending with the
	 *         given conclusion, such that for each conclusion there exists an
	 *         inference in {@link Proof#getInferences} that has as premises
	 *         only conclusions that appear before in this sequence or in the
	 *         stated conclusions.
	 */
	public static <C> boolean isDerivable(Proof<C> proof, C conclusion,
			Set<C> assertedConclusions) {
		return ProofNodes.isDerivable(ProofNodes.create(proof, conclusion),
				assertedConclusions);
	}

	/**
	 * @param proofs
	 * @return the union of the given the {@link GenericProof}s, i.e., a
	 *         {@link GenericProof} that for each conclusion returns the union
	 *         of inferences returned by the proofs in the argument
	 */
	public static <C, I extends Inference<C>> GenericProof<C, I> union(
			final Iterable<? extends GenericProof<C, I>> proofs) {
		return new ProofUnion<C, I>(proofs);
	}

	/**
	 * @param proofs
	 * @return the union of the given the {@link GenericProof}s, i.e., a
	 *         {@link GenericProof} that for each conclusion returns the union
	 *         of inferences returned by the proofs in the argument
	 */
	public static <C, I extends Inference<C>> GenericProof<C, I> union(
			final GenericProof<C, I>... proofs) {
		return new ProofUnion<C, I>(proofs);
	}

	/**
	 * @param proof
	 * @param asserted
	 * @return the {@link Proof} that has all inferences of the given
	 *         {@link Proof} plus the {@link AssertedConclusionInference}s for
	 *         each of the given asserted conclusions
	 */
	public static <C> Proof<C> addAssertedInferences(final Proof<C> proof,
			final Set<? extends C> asserted) {
		return new AddAssertedProof<C>(proof, asserted);
	}

	/**
	 * @param proof
	 * @return {@link DynamicProof} that caches all
	 *         {@link DynamicProof#getInferences(Object)} requests of the input
	 *         {@link DynamicProof}, until the input proof changes
	 */
	public static <C> DynamicProof<C> cache(DynamicProof<C> proof) {
		return new CachingProof<C>(proof);
	}

	/**
	 * @return An {@link InferenceJustifier} that justifies inferences by a set
	 *         containing the conclusion if the inference is an
	 *         {@link AssertedConclusionInference}, and by an empty set
	 *         otherwise.
	 * @see Proofs#addAssertedInferences(Proof, Set)
	 * @deprecated Use {@link InferenceJustifiers#justifyAssertedInferences()}
	 */
	@Deprecated
	public static <C> InferenceJustifier<C, ? extends Set<? extends C>> justifyAssertedInferences() {
		return AssertedConclusionInferenceJustifier.getInstance();
	}

	/**
	 * Recursively enumerates all inferences of the given {@link Proof} starting
	 * from the inferences for the given goal conclusion and then proceeding to
	 * the inferences of their premises. The encountered inferences are reported
	 * using the provided {@link Producer} by calling {@link Producer#produce}.
	 * The inferences for each conclusion are enumerated only once even if the
	 * conclusion appears as premise in several inferences.
	 * 
	 * @param proof
	 * @param goal
	 * @param producer
	 * @return the set of all conclusions for which the inferences were
	 *         enumerated
	 */
	public static <C> Set<C> unfoldRecursively(Proof<C> proof, C goal,
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
			for (Inference<C> inf : proof.getInferences(next)) {
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
	 * @param proof
	 * @param goal
	 * @return the set of conclusions without which the goal would not be
	 *         derivable using the given inferences; i.e., every derivation
	 *         using the inferences must use at least one essential conclusion
	 */
	public static <C> Set<C> getEssentialConclusions(Proof<C> proof, C goal) {
		Set<C> result = new HashSet<C>();
		DerivabilityCheckerWithBlocking<C> checker = new InferenceDerivabilityChecker<C>(
				proof);
		for (C candidate : unfoldRecursively(proof, goal,
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
	 * @param proof
	 * @param goal
	 * @param producer
	 */
	public static <C> void expand(Set<C> derivable, Proof<C> proof, C goal,
			Producer<Inference<C>> producer) {
		InferenceExpander.expand(derivable, proof, goal, producer);
	}

	/**
	 * @param proof
	 * @param goal
	 * @param asserted
	 * @return a proof obtained from the given proofs by removing some
	 *         inferences that do not have effect on the derivation relation
	 *         between subsets of the given asserted conclusion and the goal
	 *         conclusion; i.e., if the goal conclusion was derivable from some
	 *         subset of asserted conclusions using original inferences, then it
	 *         is also derivable using the returned proof
	 */
	public static <C> Proof<C> prune(Proof<C> proof, C goal, Set<C> asserted) {
		return new PrunedProof<C>(proof, goal, asserted);
	}

	/**
	 * Recursively prints all inferences for the derived goal and the premises
	 * of such inferences to the standard output using ASCII characters. Due to
	 * potential cycles, inferences for every conclusion are printed only once
	 * upon their first occurrence in the proof. Every following occurrence of
	 * the same conclusion is labeled by {@code *}.
	 * 
	 * @param proof
	 *            the {@link Proof} from which to take the inferences
	 * @param goal
	 *            the conclusion starting from which the inferences are printed
	 */
	public static <C> void print(Proof<C> proof, C goal) {
		try {
			ProofPrinter.print(proof, goal);
		} catch (IOException e) {
			throw new RuntimeException("Exception while printing the proof", e);
		}

	}

}
