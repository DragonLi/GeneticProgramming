package ec.app.regression.testcases;

public class PSepticFactory extends FunctionFactory {

	@Override
	protected double function(final double x) {
		/*
		 * return x * x * x * x * x * x * x - 2 * x * x * x * x * x * x + x * x * x * x * x - x * x * x * x + x * x * x
		 * - 2 * x * x + x;
		 */
		return x * (x * (x * (x * (x * (x * (x - 2) + 1) - 1) + 1) - 2) + 1);
	}

}
