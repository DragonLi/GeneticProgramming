package ec.app.regression.testcases;

public class Keijzer04Factory extends FunctionFactory {

	@Override
	protected double function(double x) {
		double sinx = Math.sin(x);
		double sincosx = sinx * Math.cos(x);
		return x * x * x * Math.exp(-x) * sincosx * (sinx * sincosx - 1);
	}

}
