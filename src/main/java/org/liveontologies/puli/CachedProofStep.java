package org.liveontologies.puli;

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

import java.util.List;

public class CachedProofStep<C> extends ConvertedProofStep<C> {

	private final ProofNode<C> cachedConclusion_;

	private List<ProofNode<C>> cachedPremises_;

	protected CachedProofStep(ProofStep<C> delegate,
			CachedProofNode<C> conclusion) {
		super(delegate);
		this.cachedConclusion_ = conclusion;
	}

	@Override
	public ProofNode<C> getConclusion() {
		return cachedConclusion_;
	}

	@Override
	public List<ProofNode<C>> getPremises() {
		if (cachedPremises_ == null) {
			cachedPremises_ = super.getPremises();
		}
		return cachedPremises_;
	}

	@Override
	protected final ConvertedProofNode<C> convert(ProofNode<C> node) {
		return new CachedProofNode<C>(node);
	}

}
