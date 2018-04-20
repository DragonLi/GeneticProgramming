package ec.app.regression.testcases;

public class R2Factory extends FunctionFactory {

	@Override
	protected double function(final double x) {
		// return (x * x * x * x * x - 3 * x * x * x + 1) / (x * x + 1);
		return (x * x * (x * (x * x - 3)) + 1) / (x * x + 1);
	}

}
