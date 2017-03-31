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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Combination of multiple inference sets. Inferences from this inference set
 * deriving some conclusion are union of inferences from the supplied inference
 * sets deriving that conclusion.
 * 
 * @author Peter Skocovsky
 *
 * @param <C>
 *            The type of conclusion and premises used by the inferences.
 * @param <I>
 *            The type of the inferences.
 */
public class CombinedInferenceSet<C, I extends Inference<C>>
		implements GenericInferenceSet<C, I> {

	private final Iterable<? extends GenericInferenceSet<C, I>> inferenceSets_;

	public CombinedInferenceSet(
			final Iterable<? extends GenericInferenceSet<C, I>> inferenceSets) {
		Preconditions.checkNotNull(inferenceSets);
		this.inferenceSets_ = inferenceSets;
	}

	public CombinedInferenceSet(
			final GenericInferenceSet<C, I>... inferenceSets) {
		this(Arrays.asList(inferenceSets));
	}

	@Override
	public Collection<? extends I> getInferences(final C conclusion) {

		final List<I> result = new ArrayList<I>();

		for (final GenericInferenceSet<C, I> inferenceSet : inferenceSets_) {
			final Collection<? extends I> infs = inferenceSet
					.getInferences(conclusion);
			if (infs != null) {
				result.addAll(infs);
			}
		}

		return result;
	}

}
