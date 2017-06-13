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

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class InferenceJustifiers {

	private InferenceJustifiers() {
		// Forbid instantiation of an utility class.
	}

	/**
	 * @return An {@link InferenceJustifier} that justifies inferences by a set
	 *         containing the conclusion if the inference is an
	 *         {@link AssertedConclusionInference}, and by an empty set
	 *         otherwise.
	 * @see Proofs#addAssertedInferences(Proof, Set)
	 */
	public static <C> InferenceJustifier<C, ? extends Set<? extends C>> justifyAssertedInferences() {
		return AssertedConclusionInferenceJustifier.getInstance();
	}

	public static <C, F, T> InferenceJustifier<C, ? extends Set<? extends T>> transform(
			final InferenceJustifier<C, ? extends Set<? extends F>> justifier,
			final Function<F, T> function) {
		return new InferenceJustifier<C, Set<? extends T>>() {

			@Override
			public Set<? extends T> getJustification(
					final Inference<C> inference) {
				final Set<? extends F> justification = justifier
						.getJustification(inference);
				return ImmutableSet
						.copyOf(Iterables.transform(justification, function));
			}

		};
	}

}
