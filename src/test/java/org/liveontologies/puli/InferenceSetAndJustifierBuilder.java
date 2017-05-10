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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InferenceSetAndJustifierBuilder<C, A>
		extends InferenceSetBuilder<C> {

	private final Map<Inference<C>, Set<? extends A>> inferenceJustifications_ = new HashMap<Inference<C>, Set<? extends A>>();

	public InferenceSetAndJustifierBuilder() {
		// Empty.
	}

	public InferenceJustifier<C, ? extends Set<? extends A>> buildJustifier() {
		return new BaseInferenceJustifier<C, Set<? extends A>>(
				inferenceJustifications_, Collections.<A> emptySet());
	}

	public ThisInferenceBuilder conclusion(C conclusion) {
		ThisInferenceBuilder result = new ThisInferenceBuilder(INF_NAME);
		result.conclusion(conclusion);
		return result;
	}

	public class ThisInferenceBuilder
			extends InferenceSetBuilder<C>.ThisInferenceBuilder {

		private final Set<A> axioms_ = new HashSet<A>();

		protected ThisInferenceBuilder(String name) {
			super(name);
		}

		@Override
		ThisInferenceBuilder conclusion(C conclusion) {
			super.conclusion(conclusion);
			return this;
		}

		@Override
		public ThisInferenceBuilder premise(C premise) {
			super.premise(premise);
			return this;
		}

		public ThisInferenceBuilder axiom(final A axiom) {
			axioms_.add(axiom);
			return this;
		}

		public Inference<C> add() {
			final Inference<C> inference = super.add();
			inferenceJustifications_.put(inference, axioms_);
			return inference;
		}

	}

}
