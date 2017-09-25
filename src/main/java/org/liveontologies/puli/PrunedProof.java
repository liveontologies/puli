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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PrunedProof<C> extends DelegatingProof<C, Proof<C>>
		implements Proof<C>, Producer<Inference<C>> {

	private final Map<C, Inference<C>> expanded_ = new HashMap<C, Inference<C>>();

	public PrunedProof(Proof<C> delegate, C goal) {
		super(delegate);
		Set<C> essential = Proofs.getEssentialConclusions(delegate, goal);
		Proofs.expand(essential, Proofs.removeAssertedInferences(delegate),
				goal, this);
	}

	@Override
	public void produce(Inference<C> inf) {
		expanded_.put(inf.getConclusion(), inf);
	}

	@Override
	public Collection<? extends Inference<C>> getInferences(C conclusion) {
		Inference<C> inf = expanded_.get(conclusion);
		if (inf == null) {
			return super.getInferences(conclusion);
		}
		// else
		return Collections.singleton(inf);
	}

}
