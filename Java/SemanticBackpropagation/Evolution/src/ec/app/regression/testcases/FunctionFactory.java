package ec.app.regression.testcases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import library.semantics.TestCase;

import ec.EvolutionState;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;

public abstract class FunctionFactory extends TestCaseFactory {

	protected static final String FROM = "testCases.from";
	protected static final String TO = "testCases.to";
	protected static final String STEP = "testCases.step";

	protected boolean randomizedPoints = false;

	public boolean isRandomizedPoints() {
		return randomizedPoints;
	}

	protected void setRandomizedPoints(final boolean randomizedPoints) {
		this.randomizedPoints = randomizedPoints;
	}

	/**
	 * Computes function, from which test cases are generated.
	 * 
	 * @param x
	 * @return y
	 */
	protected abstract double function(double x);

	@Override
	public List<TestCase<Double>> generateTraining(final EvolutionState state) {
		this.setRandomizedPoints(false);
		return this.compute(state);
	}

	@Override
	public List<TestCase<Double>> generateTest(final EvolutionState state) {
		this.setRandomizedPoints(true);
		return this.compute(state);
	}

	@Override
	public List<TestCase<Double>> compute(final EvolutionState state) {
		double from = state.parameters.getDouble(new Parameter(FROM), null);
		double to = state.parameters.getDouble(new Parameter(TO), null);
		double step = state.parameters.getDouble(new Parameter(STEP), null);

		List<TestCase<Double>> cases = new ArrayList<TestCase<Double>>();

		if (!this.randomizedPoints) {
			for (double x = from; x <= to; x += step) {
				TestCase<Double> _case = new TestCase<Double>();

				_case.setArguments(x);
				_case.setValue(this.function(x));

				cases.add(_case);
			}
		} else {
			//make pseudo-random generator deterministic
			MersenneTwisterFast random = new MersenneTwisterFast(0);
			int count = (int) Math.round((to - from) / step) + 1;
			for (int i = 0; i < count; ++i) {
				double x = random.nextDouble() * (to - from) + from;
				TestCase<Double> _case = new TestCase<Double>();

				_case.setArguments(x);
				_case.setValue(this.function(x));

				cases.add(_case);
			}
		}

		return cases;
	}
}
