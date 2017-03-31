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
import java.util.Collection;

import com.google.common.base.Preconditions;

class BaseProofNode<C> extends AbstractProofNode<C> {

	private final InferenceSet<C> inferenceSet_;

	private Collection<ProofStep<C>> steps_ = null;

	BaseProofNode(InferenceSet<C> inferences, C member) {
		super(member);
		Preconditions.checkNotNull(inferences);
		this.inferenceSet_ = inferences;
	}

	public InferenceSet<C> getInferenceSet() {
		return inferenceSet_;
	}

	@Override
	public Collection<? extends ProofStep<C>> getInferences() {
		if (steps_ == null) {
			Collection<? extends Inference<C>> original = inferenceSet_
					.getInferences(getMember());
			steps_ = new ArrayList<ProofStep<C>>(original.size());
			for (Inference<C> inf : original) {
				convert(inf);
			}
		}
		return steps_;
	}

	void convert(Inference<C> inf) {
		steps_.add(new BaseProofStep<C>(inferenceSet_, inf));
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof BaseProofNode) {
			BaseProofNode<?> other = (BaseProofNode<?>) o;
			return getMember().equals(other.getMember())
					&& inferenceSet_.equals(other.inferenceSet_);
		}
		// else
		return false;
	}

	@Override
	public int hashCode() {
		return BaseProofNode.class.hashCode() + getMember().hashCode()
				+ inferenceSet_.hashCode();
	}

}
