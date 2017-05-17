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

public class InferenceSetBuilder<C> {

	static final String INF_NAME = "inf";

	private final BaseProof.Projection<C> inferenceSet_ = new BaseProof.Projection<C>();

	/**
	 * use {@link #create()}
	 */
	InferenceSetBuilder() {
	}

	public static <C> InferenceSetBuilder<C> create() {
		return new InferenceSetBuilder<C>();
	}

	public BaseProof.Projection<C> build() {
		return inferenceSet_;
	}

	public ThisInferenceBuilder conclusion(C conclusion) {
		ThisInferenceBuilder result = new ThisInferenceBuilder(INF_NAME);
		result.conclusion(conclusion);
		return result;
	}

	public class ThisInferenceBuilder extends InferenceBuilder<C> {

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

		public Inference<C> add() {
			Inference<C> inference = build();
			inferenceSet_.produce(inference);
			return inference;
		}

	}

}
