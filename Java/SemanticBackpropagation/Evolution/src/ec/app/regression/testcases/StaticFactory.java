package ec.app.regression.testcases;

import java.util.ArrayList;
import java.util.List;

import library.semantics.TestCase;

import ec.EvolutionState;
import ec.util.Parameter;

public class StaticFactory extends TestCaseFactory {

	private static final String COUNT = "testCases.count";
	private static final String ARGUMENT_COUNT = "testCases.argumentCount";

	@Override
	public List<TestCase<Double>> compute(EvolutionState state) {
		int count = state.parameters.getInt(new Parameter(COUNT), null);
		int argumentCount = state.parameters.getInt(new Parameter(ARGUMENT_COUNT), null);
		List<TestCase<Double>> cases = new ArrayList<TestCase<Double>>(count);

		for (int i = 0; i < count; ++i) {
			TestCase<Double> _case = new TestCase<Double>();
			Double[] args = new Double[argumentCount];

			for (int arg = 0; arg < argumentCount; ++arg) {
				args[arg] = state.parameters.getDouble(new Parameter(String.format("testCases.%d.arg.%d", i, arg)),
						null);
			}
			_case.setArguments(args);
			_case.setValue(state.parameters.getDouble(new Parameter(String.format("testCase.%d.val", i)), null));

			cases.add(_case);
		}

		return cases;
	}

}
