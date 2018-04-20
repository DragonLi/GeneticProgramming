package ec.app.regression.testcases;

public class Keijzer06Factory extends FunctionFactory {

	@Override
	protected double function(double x) {
		int to = (int) Math.rint(x);
		assert to >= 0;

		double sum = 0.0;
		
		for (int i = 0; i <= to; ++i) {
			sum += 1.0 / (double) i;
		}

		return sum;
	}

}
