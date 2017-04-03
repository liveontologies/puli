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
 * A skeleton implementation of enumerator factories that use inference sets.
 * Enumerated sets are over axioms with which the inferences are justified and
 * the query for enumerator is one of the conclusions.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 *            the type of conclusion and premises used by the inferences
 * @param <A>
 *            the type of axioms used by the inferences
 */
public abstract class MinimalSubsetsFromInferences<C, A>
		implements MinimalSubsetEnumerator.Factory<C, A> {

	private final InferenceSet<C> inferenceSet_;

	private final InferenceJustifier<C, ? extends Set<? extends A>> justifier_;

	private final InterruptMonitor monitor_;

	public MinimalSubsetsFromInferences(final InferenceSet<C> inferenceSet,
			final InferenceJustifier<C, ? extends Set<? extends A>> justifier,
			final InterruptMonitor monitor) {
		Preconditions.checkNotNull(inferenceSet);
		Preconditions.checkNotNull(justifier);
		Preconditions.checkNotNull(monitor);
		this.inferenceSet_ = inferenceSet;
		this.justifier_ = justifier;
		this.monitor_ = monitor;
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

	/**
	 * Factory for creating enumerator factories.
	 * 
	 * @author Yevgeny Kazakov
	 * @author Peter Skocovsky
	 * 
	 * @param <C>
	 *            the type of conclusion and premises used by the inferences
	 * @param <A>
	 *            the type of axioms used by the inferences
	 */
	public static interface Factory<C, A> {

		/**
		 * @param inferenceSet
		 * @param justifier
		 * @param monitor
		 * @return a new {@link MinimalSubsetEnumerator.Factory} which uses the
		 *         given inference set and inference justifier
		 */
		MinimalSubsetEnumerator.Factory<C, A> create(
				InferenceSet<C> inferenceSet,
				InferenceJustifier<C, ? extends Set<? extends A>> justifier,
				InterruptMonitor monitor);

	}

}
