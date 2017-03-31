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
import java.util.Set;

import org.liveontologies.puli.Inference;
import org.liveontologies.puli.InferenceJustifier;
import org.liveontologies.puli.InferenceSet;

import com.google.common.base.Preconditions;

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

	private final InferenceSet<C> inferenceSet_;

	private final InferenceJustifier<C, ? extends Set<? extends A>> justifier_;

	private final InterruptMonitor monitor_;

	public AbstractJustificationComputation(final InferenceSet<C> inferenceSet,
			final InferenceJustifier<C, ? extends Set<? extends A>> justifier,
			final InterruptMonitor monitor) {
		Preconditions.checkNotNull(inferenceSet);
		Preconditions.checkNotNull(justifier);
		Preconditions.checkNotNull(monitor);
		this.inferenceSet_ = inferenceSet;
		this.justifier_ = justifier;
		this.monitor_ = monitor;
	}

	@Override
	public void enumerateJustifications(final C conclusion,
			final JustificationComputation.Listener<A> listener) {
		enumerateJustifications(conclusion,
				JustificationComputation.DEFAULT_ORDER, listener);
	}

	public InferenceSet<C> getInferenceSet() {
		return inferenceSet_;
	}

	public InferenceJustifier<C, ? extends Set<? extends A>> getInferenceJustifier() {
		return justifier_;
	}

	public Collection<? extends Inference<C>> getInferences(
			final C conclusion) {
		return inferenceSet_.getInferences(conclusion);
	}

	public Set<? extends A> getJustification(final Inference<C> inference) {
		return justifier_.getJustification(inference);
	}

	protected boolean isInterrupted() {
		return monitor_.isInterrupted();
	}

}
