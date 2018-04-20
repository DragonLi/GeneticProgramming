package ec.app.regression.testcases;

public class F07Factory extends FunctionFactory {

	@Override
	protected double function(final double x) {
		// return Math.log(x + 1) + Math.log(x * x + 1);
		assert Math.abs((Math.log(x + 1) + Math.log(x * x + 1)) - Math.log(1 + x * (1 + x * (1 + x)))) < 1E-10;
		return Math.log(1 + x * (1 + x * (1 + x))); // faster version of above formula assuming x > -1
	}

}
