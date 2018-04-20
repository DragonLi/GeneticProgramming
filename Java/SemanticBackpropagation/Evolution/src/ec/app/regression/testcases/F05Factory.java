package ec.app.regression.testcases;

public class F05Factory extends FunctionFactory {

	@Override
	protected double function(final double x) {
		return Math.sin(x * x) * Math.cos(x) - 1;
	}

}
