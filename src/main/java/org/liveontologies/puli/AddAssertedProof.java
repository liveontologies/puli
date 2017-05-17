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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Iterators;

class AddAssertedProof<C> extends DelegatingProof<C, Proof<C>> {

	private final Set<? extends C> assertedConclusions_;

	AddAssertedProof(final Proof<C> delegate,
			final Set<? extends C> assertedConclusions) {
		super(delegate);
		this.assertedConclusions_ = assertedConclusions;
	}

	@Override
	public Collection<? extends Inference<C>> getInferences(
			final C conclusion) {
		final Collection<? extends Inference<C>> result = getDelegate()
				.getInferences(conclusion);
		if (!assertedConclusions_.contains(conclusion)) {
			return result;
		}
		// else, add asserted conclusion inference
		return new AbstractCollection<Inference<C>>() {

			@Override
			public Iterator<Inference<C>> iterator() {
				return Iterators.concat(result.iterator(),
						Iterators.singletonIterator(
								new AssertedConclusionInference<C>(
										conclusion)));
			}

			@Override
			public int size() {
				return result.size() + 1;
			}

		};
	}

}
