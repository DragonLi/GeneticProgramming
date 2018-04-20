package ec.app.regression.testcases;

public class Keijzer09Factory extends FunctionFactory {

	@Override
	protected double function(double x) {
		// asinh(x)
		return Math.log(x + Math.sqrt(x * x + 1));
	}

}
