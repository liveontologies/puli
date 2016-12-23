package org.liveontologies.proof.util;

/*-
 * #%L
 * OWL API Proof Extension
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2016 Live Ontologies Project
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

public abstract class AbstractProofNode<C> implements ProofNode<C> {

	private final C member_;

	public AbstractProofNode(C member) {
		Util.checkNotNull(member);
		this.member_ = member;
	}

	@Override
	public C getMember() {
		return member_;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AbstractProofNode) {
			AbstractProofNode<?> other = (AbstractProofNode<?>) o;
			return member_.equals(other.member_);
		}
		// else
		return false;
	}

	@Override
	public int hashCode() {
		return AbstractProofNode.class.hashCode() + member_.hashCode();
	}

	@Override
	public String toString() {
		return member_.toString();
	}

}