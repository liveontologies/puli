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
package org.liveontologies.puli.justifications;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class JustificationCollector<A>
		implements JustificationComputation.Listener<A> {

	private final Collection<Set<? extends A>> justifications_;

	public JustificationCollector(
			final Collection<Set<? extends A>> justifications) {
		this.justifications_ = justifications;
	}

	public JustificationCollector() {
		this(new ArrayList<Set<? extends A>>());
	}

	@Override
	public void newJustification(final Set<A> justification) {
		justifications_.add(justification);
	}

	public Collection<Set<? extends A>> getJustifications() {
		return justifications_;
	}

}
