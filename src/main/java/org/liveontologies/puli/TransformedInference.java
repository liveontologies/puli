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

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

class TransformedInference<F, T> extends Delegator<Inference<F>>
		implements Inference<T> {

	private final Function<? super F, ? extends T> function_;

	public TransformedInference(final Inference<F> inference,
			final Function<? super F, ? extends T> function) {
		super(inference);
		this.function_ = function;
	}

	@Override
	public String getName() {
		return getDelegate().getName();
	}

	@Override
	public T getConclusion() {
		return function_.apply(getDelegate().getConclusion());
	}

	@Override
	public List<? extends T> getPremises() {
		return Lists.transform(getDelegate().getPremises(), function_);
	}

}
