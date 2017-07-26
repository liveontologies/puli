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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

class TransformedProof<F, T> extends Delegator<Proof<F>> implements Proof<T> {

	final Function<? super F, ? extends T> function_;
	final Function<? super T, ? extends F> inverse_;

	public TransformedProof(final Proof<F> proof,
			final Function<? super F, ? extends T> function,
			final Function<? super T, ? extends F> inverse) {
		super(proof);
		this.function_ = function;
		this.inverse_ = inverse;
	}

	@Override
	public Collection<? extends Inference<T>> getInferences(
			final T conclusion) {
		return Collections2.transform(
				getDelegate().getInferences(inverse_.apply(conclusion)),
				new Function<Inference<F>, Inference<T>>() {
					@Override
					public Inference<T> apply(Inference<F> inference) {
						return Inferences.transform(inference, function_);
					}
				});
	}

}
