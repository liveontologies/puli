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

import java.util.Map;

/**
 * Justifies inferences by the justifications from the provided map, or by the
 * provided default justification if the map does not contain the justification.
 * 
 * @author Peter Skocovsky
 *
 * @param <C>
 *            The type of conclusion and premises used by the inferences.
 * @param <J>
 *            The type of justifications of the inferences.
 */
public class BaseInferenceJustifier<C, J> implements InferenceJustifier<C, J> {

	private final Map<Inference<C>, J> inferenceJustifications_;

	private final J defaultJustification_;

	public BaseInferenceJustifier(
			final Map<Inference<C>, J> inferenceJustifications,
			final J defaultJustification) {
		this.inferenceJustifications_ = inferenceJustifications;
		this.defaultJustification_ = defaultJustification;
	}

	@Override
	public J getJustification(final Inference<C> inference) {
		final J result = inferenceJustifications_.get(inference);
		if (result == null) {
			return defaultJustification_;
		}
		// else
		return result;
	}

}
