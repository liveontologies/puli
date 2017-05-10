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

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

@RunWith(Parameterized.class)
public abstract class BaseDualEnumeratorTest<C, A> {

	@Parameter(0)
	public String name;

	@Parameter(1)
	public EnumeratorTestInput<C, A> input;

	@Parameter(2)
	public MinimalSubsetsFromInferences.Factory<C, A> factory;

	@Test
	public void testJustifications() {

		final MinimalSubsetEnumerator.Factory<C, A> computation = factory
				.create(input.getInferenceSet(), input.getJustifier(),
						InterruptMonitor.DUMMY);

		final Set<Set<? extends A>> actualResult = new HashSet<Set<? extends A>>();
		computation.newEnumerator(input.getQuery())
				.enumerate(new MinimalSubsetCollector<A>(actualResult));

		final Set<Set<? extends A>> expectedResult = new HashSet<Set<? extends A>>(
				MinimalHittingSetEnumerator.compute(input.getExpectedResult()));

		Assert.assertEquals(expectedResult, actualResult);

	}

}
