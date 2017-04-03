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

import java.util.Collection;

public class CachedProofNode<C> extends ConvertedProofNode<C> {

	private C cachedMember_;

	private Collection<ProofStep<C>> cachedInferences_;

	protected CachedProofNode(ProofNode<C> delegate) {
		super(delegate);
	}

	@Override
	public synchronized C getMember() {
		if (cachedMember_ == null) {
			cachedMember_ = super.getMember();
		}
		return cachedMember_;
	}

	@Override
	public Collection<ProofStep<C>> getInferences() {
		if (cachedInferences_ == null) {
			cachedInferences_ = super.getInferences();
		}
		return cachedInferences_;
	}

	@Override
	protected final void convert(ConvertedProofStep<C> step) {
		convert(new CachedProofStep<C>(step, this));
	}

	protected void convert(CachedProofStep<C> step) {
		super.convert(step);
	}

}
