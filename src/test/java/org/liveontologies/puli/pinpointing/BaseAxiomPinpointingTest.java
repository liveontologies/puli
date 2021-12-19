package org.liveontologies.puli.pinpointing;

/*-
 * #%L
 * Proof Utility Library
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2021 Live Ontologies Project
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.liveontologies.puli.AxiomPinpointingInference;
import org.liveontologies.puli.BaseTest;
import org.liveontologies.puli.TestManifest;
import org.liveontologies.puli.TestRunner;

public abstract class BaseAxiomPinpointingTest<C, A, I extends AxiomPinpointingInference<? extends C, ? extends A>>
		extends
		BaseTest<AxiomPinpointingTestManifest<C, A, I>, AxiomPinpointingTestRunner<C, A, I>> {

	public static final String TEST_INPUT_JUSTIFICATIONS_SUBPKG = "pinpointing.input.justifications";
	public static final String TEST_INPUT_REPAIRS_SUBPKG = "pinpointing.input.repairs";

	public static Iterable<Object[]> data(
			Stream<ProverAxiomPinpointingEnumerationFactory<?, ?>> computationFactories)
			throws Exception {
		return data(computationFactories, Stream.of(
				TEST_INPUT_JUSTIFICATIONS_SUBPKG, TEST_INPUT_REPAIRS_SUBPKG));
	}

	public static Iterable<Object[]> data(
			List<AxiomPinpointingTestRunner<?, ?, ?>> testRunners,
			List<String> testInputs) throws Exception {
		final List<Object[]> parameters = new ArrayList<Object[]>();
		for (String testInput : testInputs) {
			for (TestManifest manifest : BaseTest.getInstances(
					AxiomPinpointingTestManifest.class, testInput)) {
				for (final TestRunner<?> runner : testRunners) {
					parameters.add(new Object[] { manifest, runner });
				}
			}
		}
		return parameters;
	}

	public static Iterable<Object[]> data(
			Stream<ProverAxiomPinpointingEnumerationFactory<?, ?>> computationFactories,
			Stream<String> testInputs) throws Exception {
		return data(
				computationFactories
						.map(computationFactory -> createRunner(
								computationFactory,
								computationFactory.toString()))
						.collect(Collectors.toList()),
				testInputs.collect(Collectors.toList()));
	}

	public static Iterable<Object[]> data(
			Stream<ProverAxiomPinpointingEnumerationFactory<?, ?>> computationFactories,
			String testInput) throws Exception {
		return data(computationFactories, Stream.of(testInput));
	}

	static <C, A, I extends AxiomPinpointingInference<? extends C, ? extends A>> AxiomPinpointingTestRunner<C, A, I> createRunner(
			ProverAxiomPinpointingEnumerationFactory<C, A> computationFactory,
			String name) {
		return new AxiomPinpointingTestRunner<C, A, I>(computationFactory,
				name);
	}

}
