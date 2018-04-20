package ec.app.regression.testcases;

import java.util.Collections;
import java.util.List;

import library.semantics.TestCase;

import ec.EvolutionState;
import ec.util.Parameter;

public abstract class TestCaseFactory {

	private static final String FACTORY = "testCases.factory";

	/**
	 * Computes list of test cases depending on given configuration
	 * 
	 * @param state
	 * @return
	 */
	public abstract <T> List<TestCase<T>> compute(final EvolutionState state);

	public <T> List<TestCase<T>> generateTraining(final EvolutionState state) {
		return this.compute(state);
	}

	public <T> List<TestCase<T>> generateTest(final EvolutionState state) {
		return Collections.emptyList();
	}

	public static TestCaseFactory getFactory(final EvolutionState state) {
		try {
			Class<?> cl = (Class<?>) state.parameters.getClassForParameter(new Parameter(FACTORY), null,
					TestCaseFactory.class);
			return (TestCaseFactory) cl.newInstance();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
