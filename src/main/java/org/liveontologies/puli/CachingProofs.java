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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link DynamicProof} that caches the inferences returned by the
 * input {@link DynamicProof} by {@link #getInferences(Object)}. When
 * this method is called for the second time with the same input, the cached
 * version is used.
 * 
 * @author Yevgeny Kazakov
 *
 * @param <C>
 */
public class CachingProofs<C>
		extends DelegatingDynamicProof<C, DynamicProof<C>>
		implements DynamicProof.ChangeListener {

	private final Map<C, Collection<? extends Inference<C>>> inferenceCache_ = new HashMap<C, Collection<? extends Inference<C>>>();

	public CachingProofs(DynamicProof<C> delegate) {
		super(delegate);
		addListener(this);
	}

	@Override
	public Collection<? extends Inference<C>> getInferences(C conclusion) {
		Collection<? extends Inference<C>> result = inferenceCache_
				.get(conclusion);
		if (result == null) {
			result = super.getInferences(conclusion);
			inferenceCache_.put(conclusion, result);
		}
		return result;
	}

	@Override
	public void inferencesChanged() {
		inferenceCache_.clear();
	}

	@Override
	public void dispose() {
		removeListener(this);
		super.dispose();
	}

}
