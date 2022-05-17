package org.liveontologies.puli.pinpointing;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.liveontologies.puli.AxiomPinpointingInference;
import org.liveontologies.puli.BaseTest;

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
			Stream<ProverAxiomPinpointingEnumerationFactory<?, ?>> computationFactories,
			String testInput) throws Exception {
		return data(computationFactories, Stream.of(testInput));
	}

	public static Iterable<Object[]> data(
			Stream<ProverAxiomPinpointingEnumerationFactory<?, ?>> computationFactories,
			Stream<String> testInputs) throws Exception {
		return data(
				computationFactories.map(
						computationFactory -> new AxiomPinpointingTestRunner<>(
								computationFactory))
						.collect(Collectors.toList()),
				testInputs.collect(Collectors.toList()));
	}	

}
