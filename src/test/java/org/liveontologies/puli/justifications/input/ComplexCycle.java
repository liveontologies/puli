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
package org.liveontologies.puli.justifications.input;

import java.util.Set;

import org.liveontologies.puli.InferenceJustifier;
import org.liveontologies.puli.InferenceSet;
import org.liveontologies.puli.InferenceSetAndJustifierBuilder;
import org.liveontologies.puli.justifications.EnumeratorTestInput;

public abstract class ComplexCycle
		implements EnumeratorTestInput<String, Integer> {

	private final InferenceSetAndJustifierBuilder<String, Integer> builder_;

	public ComplexCycle() {

		this.builder_ = new InferenceSetAndJustifierBuilder<String, Integer>();

		builder_.conclusion("A").premise("B").axiom(1).add();
		builder_.conclusion("A").premise("C").axiom(2).add();
		builder_.conclusion("B").premise("C").axiom(3).add();
		builder_.conclusion("C").premise("D").axiom(4).add();
		builder_.conclusion("D").premise("B").axiom(5).add();
		builder_.conclusion("D").axiom(6).axiom(7).add();
		builder_.conclusion("A").premise("E").axiom(8).add();
		builder_.conclusion("E").axiom(1).axiom(9).add();
		builder_.conclusion("B").axiom(8).axiom(9).add();

	}

	@Override
	public String getQuery() {
		return "A";
	}

	@Override
	public InferenceSet<String> getInferenceSet() {
		return builder_.build();
	}

	@Override
	public InferenceJustifier<String, ? extends Set<? extends Integer>> getJustifier() {
		return builder_.buildJustifier();
	}

}
