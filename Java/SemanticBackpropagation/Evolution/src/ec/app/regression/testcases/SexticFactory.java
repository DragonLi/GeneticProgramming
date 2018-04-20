package ec.app.regression.testcases;

public class SexticFactory extends FunctionFactory {

	@Override
	protected double function(double x) {
		return x * x * x * x * x * x - 2.0 * x * x * x * x + x * x;
	}

}
