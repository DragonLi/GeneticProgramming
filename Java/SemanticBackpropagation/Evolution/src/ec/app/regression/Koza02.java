package ec.app.regression;

public class Koza02 extends F02 {

	@Override
	public double func(double x) {
		// x * x * x * x * x - 2.0 * x * x * x + x
		assert Math.abs((x * x * x * x * x - 2.0 * x * x * x + x) - (x * (x * x * (x * x - 2.0) + 1))) < 1E-10;
		return x * (x * x * (x * x - 2.0) + 1);
	}
	
}
