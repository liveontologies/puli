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
import java.util.List;

import com.google.common.base.Preconditions;

public class InferenceBuilder<C> {

	private final String name_;

	private C conclusion_;

	private final List<C> premises_ = new ArrayList<C>();

	protected InferenceBuilder(String name) {
		this.name_ = name;
	}

	public static <C> InferenceBuilder<C> create(String name) {
		return new InferenceBuilder<C>(name);
	}

	InferenceBuilder<C> conclusion(C conclusion) {
		Preconditions.checkNotNull(conclusion);
		if (conclusion_ != null) {
			throw new RuntimeException(
					"Conclusion already assigned: " + conclusion);
		}
		this.conclusion_ = conclusion;
		return this;
	}

	InferenceBuilder<C> premise(C premise) {
		Preconditions.checkNotNull(premise);
		premises_.add(premise);
		return this;
	}

	Inference<C> build() {
		return new BaseInference<C>(name_, conclusion_, premises_);
	}

}
