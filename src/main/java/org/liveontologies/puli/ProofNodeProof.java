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

import org.liveontologies.puli.Proof;
import org.liveontologies.puli.ProofNode;
import org.liveontologies.puli.ProofNodeProof;
import org.liveontologies.puli.ProofStep;

public class ProofNodeProof<C> implements Proof<ProofNode<C>> {

	@SuppressWarnings("rawtypes")
	private final static ProofNodeProof INSTANCE_ = new ProofNodeProof();

	@SuppressWarnings("unchecked")
	public static <C> ProofNodeProof<C> get() {
		return INSTANCE_;
	}

	@Override
	public Collection<? extends ProofStep<C>> getInferences(
			ProofNode<C> conclusion) {
		return conclusion.getInferences();
	}

}
