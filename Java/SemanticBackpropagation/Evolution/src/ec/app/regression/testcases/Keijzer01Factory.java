package ec.app.regression.testcases;

public class Keijzer01Factory extends FunctionFactory {

	@Override
	protected double function(double x) {
		return 0.3 * x * Math.sin(2 * Math.PI * x);
	}

}
