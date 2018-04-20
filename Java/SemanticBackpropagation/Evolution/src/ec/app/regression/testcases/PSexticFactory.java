package ec.app.regression.testcases;

public class PSexticFactory extends FunctionFactory {

	@Override
	protected double function(final double x) {
		// return x * x * x * x * x * x - 2 * x * x * x * x + x * x;
		return x * x * (1 + x * x * (x * x - 2));
	}

}
