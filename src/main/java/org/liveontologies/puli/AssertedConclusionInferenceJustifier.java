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

import java.util.Collections;
import java.util.Set;

/**
 * An {@link InferenceJustifier} that justifies inferences by a set containing
 * the conclusion if the inference is {@link AssertedConclusionInference}, and
 * by an empty set otherwise.
 * 
 * @author Peter Skocovsky
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            The type of conclusion and premises used by the inferences.
 */
class AssertedConclusionInferenceJustifier<C>
		implements InferenceJustifier<C, Set<? extends C>> {

	private static final InferenceJustifier<?, ?> INSTANCE_ = new AssertedConclusionInferenceJustifier<Object>();

	@SuppressWarnings("unchecked")
	static <C> InferenceJustifier<C, Set<? extends C>> getInstance() {
		return (InferenceJustifier<C, Set<? extends C>>) INSTANCE_;
	}

	@Override
	public Set<? extends C> getJustification(final Inference<C> inference) {
		if (Inferences.isAsserted(inference)) {
			return Collections.singleton(inference.getConclusion());
		}
		// else
		return Collections.emptySet();
	}

}
