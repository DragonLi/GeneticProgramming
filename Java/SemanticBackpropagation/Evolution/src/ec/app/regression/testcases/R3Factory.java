package ec.app.regression.testcases;

public class R3Factory extends FunctionFactory {

	@Override
	protected double function(final double x) {
		// return (x * x * x * x * x * x + x) / (x * x * x * x + x * x * x + x * x + x + 1);
		return (x * x * x * x * x * x + x) / (x * (x * (x * (x + 1) + 1) + 1) + 1);
	}

}
