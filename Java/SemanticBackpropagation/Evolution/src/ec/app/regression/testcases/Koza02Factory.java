package ec.app.regression.testcases;

/**
 * Quintic or Koza-2 test case factory.
 * 
 * @author Tomasz Pawlak
 */
public class Koza02Factory extends FunctionFactory {

	@Override
	protected double function(double x) {
		// x * x * x * x * x - 2.0 * x * x * x + x
		assert Math.abs((x * x * x * x * x - 2.0 * x * x * x + x) - (x * (x * x * (x * x - 2.0) + 1))) < 1E-10;
		return x * (x * x * (x * x - 2.0) + 1);
	}

}
