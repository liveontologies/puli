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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;

public class ProofAndJustifierBuilder<C, A> extends ProofBuilder<C> {

	public ProofAndJustifierBuilder() {
		// Empty.
	}

	private final InferenceJustifier<C, Set<? extends A>> justifier_ = new InferenceJustifier<C, Set<? extends A>>() {

		@Override
		public Set<? extends A> getJustification(final Inference<C> inference) {
			if (inference instanceof ProofAndJustifierBuilder.ThisInference) {
				return ((ProofAndJustifierBuilder<C, A>.ThisInference) inference).axioms_;
			}
			// else
			return Collections.emptySet();
		}

	};

	public InferenceJustifier<C, ? extends Set<? extends A>> buildJustifier() {
		return justifier_;
	}

	public ThisInferenceBuilder conclusion(C conclusion) {
		ThisInferenceBuilder result = new ThisInferenceBuilder(INF_NAME);
		result.conclusion(conclusion);
		return result;
	}

	public class ThisInferenceBuilder
			extends ProofBuilder<C>.ThisInferenceBuilder {

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
			Preconditions.checkNotNull(axiom);
			axioms_.add(axiom);
			return this;
		}

		@Override
		Inference<C> build() {
			return new ThisInference(getName(), getConclusion(), getPremises(),
					axioms_);
		}

	}

	public class ThisInference extends BaseInference<C> {

		private final Set<A> axioms_;

		public ThisInference(final String name, final C conclusion,
				final List<? extends C> premises, final Set<A> axioms) {
			super(name, conclusion, premises);
			this.axioms_ = axioms;
		}

		@Override
		public boolean equals(final Object o) {
			if (o instanceof ProofAndJustifierBuilder.ThisInference) {
				return super.equals(o) && axioms_.equals(
						((ProofAndJustifierBuilder<?, ?>.ThisInference) o).axioms_);
			}
			// else
			return false;
		}

		@Override
		public synchronized int hashCode() {
			if (hash == 0) {
				hash = Inferences.hashCode(this) + axioms_.hashCode();
			}
			return hash;
		}

	}

}
