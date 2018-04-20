package ec.app.regression.testcases;

public class F06Factory extends FunctionFactory {

	@Override
	protected double function(final double x) {
		return Math.sin(x) + Math.sin(x + x * x);
	}

}
