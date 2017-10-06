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

import java.util.HashSet;
import java.util.Set;

/**
 * A collection of static utilities for manipulation with {@link ProofNode}
 * 
 * @author Pavel Klinov
 *
 *         pavel.klinov@uni-ulm.de
 * 
 * @author Yevgeny Kazakov
 *
 */
public class ProofNodes {

	public static <C> ProofNode<C> create(
			Proof<? extends Inference<? extends C>> proof, C member) {
		return new BaseProofNode<C>(proof, member);
	}

	public static <C> ProofNode<C> addAssertedInferences(ProofNode<C> node,
			Set<? extends C> assertedConclusions) {
		return new AddAssertedProofNode<C>(node, assertedConclusions);
	}

	public static <C> ProofNode<C> removeAssertedInferences(ProofNode<C> node) {
		return new RemoveAssertedProofNode<C>(node);
	}

	public static <C> ProofNode<C> eliminateNotDerivable(ProofNode<C> node) {
		if (isDerivable(node)) {
			return new DerivableProofNode<C>(node);
		}
		// else
		return null;
	}

	public static <C> ProofNode<C> eliminateNotDerivable(ProofNode<C> node,
			Set<C> assertedAxioms) {
		node = addAssertedInferences(node, assertedAxioms);
		if (!isDerivable(node)) {
			return null;
		}
		// else
		node = new DerivableProofNode<C>(node);
		node = removeAssertedInferences(node);
		return node;
	}

	public static <C> ProofNode<C> eliminateNotDerivableAndCycles(
			ProofNode<C> node) {
		if (isDerivable(node)) {
			return new AcyclicDerivableProofNode<C>(node);
		}
		// else
		return null;
	}

	public static <C> ProofNode<C> eliminateNotDerivableAndCycles(
			ProofNode<C> node, Set<C> assertedAxioms) {
		node = addAssertedInferences(node, assertedAxioms);
		node = eliminateNotDerivableAndCycles(node);
		if (node == null) {
			return null;
		}
		node = removeAssertedInferences(node);
		return node;
	}

	public static <C> ProofNode<C> limitInferencesPerNode(ProofNode<C> node,
			int limit) {
		return new LimitedProofNode<C>(node, limit);
	}

	public static <C> ProofNode<C> cache(ProofNode<C> node) {
		return new CachedProofNode<C>(node);
	}

	public static <C> boolean isDerivable(ProofNode<C> node) {
		return new ProofNodeDerivabilityChecker().isDerivable(node);
	}

	public static <C> boolean isDerivable(ProofNode<C> node,
			Set<? extends C> assertedConclusions) {
		node = addAssertedInferences(node, assertedConclusions);
		return isDerivable(node);
	}

	public static <C> String print(ProofNode<C> node) {
		StringBuilder builder = new StringBuilder();
		Set<ProofNode<C>> done = new HashSet<ProofNode<C>>();

		print(node, builder, done, 0);

		return builder.toString();
	}

	private static <C> void print(ProofNode<C> node, StringBuilder builder,
			Set<ProofNode<C>> done, int depth) {
		for (int i = 0; i < depth; i++) {
			builder.append("   ");
		}

		builder.append(node);

		if (done.add(node)) {
			builder.append('\n');
		} else {
			builder.append("*\n");
			return;
		}

		for (ProofStep<C> inf : node.getInferences()) {
			for (int i = 0; i < depth + 1; i++) {
				builder.append("   ");
			}

			builder.append(inf.getName()).append('\n');

			for (ProofNode<C> premise : inf.getPremises()) {
				print(premise, builder, done, depth + 2);
			}
		}
	}

}
