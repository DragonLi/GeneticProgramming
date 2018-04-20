package ec.app.regression.testcases;

public class F02Factory extends FunctionFactory {

	@Override
	protected double function(final double x) {
		// return x * x * x * x + x * x * x + x * x + x;
		return x * (1 + x * (1 + x * (1 + x)));
	}

}
