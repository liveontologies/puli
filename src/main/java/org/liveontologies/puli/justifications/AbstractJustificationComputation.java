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

import java.util.Collection;

import org.liveontologies.puli.GenericInferenceSet;
import org.liveontologies.puli.JustifiedInference;

/**
 * A skeleton implementation of {@link JustificationComputation}
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of conclusion and premises used by the inferences
 * @param <A>
 *            the type of axioms used by the inferences
 */
public abstract class AbstractJustificationComputation<C, A>
		implements JustificationComputation<C, A> {

	private final GenericInferenceSet<C, ? extends JustifiedInference<C, A>> inferenceSet_;

	private final InterruptMonitor monitor_;

	public AbstractJustificationComputation(
			final GenericInferenceSet<C, ? extends JustifiedInference<C, A>> inferenceSet,
			final InterruptMonitor monitor) {
		this.inferenceSet_ = inferenceSet;
		this.monitor_ = monitor;
	}

	@Override
	public void enumerateJustifications(final C conclusion,
			final JustificationComputation.Listener<A> listener) {
		enumerateJustifications(conclusion,
				JustificationComputation.DEFAULT_ORDER, listener);
	}

	public GenericInferenceSet<C, ? extends JustifiedInference<C, A>> getInferenceSet() {
		return inferenceSet_;
	}

	public Collection<? extends JustifiedInference<C, A>> getInferences(
			final C conclusion) {
		return inferenceSet_.getInferences(conclusion);
	}

	protected boolean isInterrupted() {
		return monitor_.isInterrupted();
	}

}
