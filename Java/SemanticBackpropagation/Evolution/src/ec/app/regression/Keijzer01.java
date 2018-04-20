package ec.app.regression;

public class Keijzer01 extends Regression {

	@Override
	public double func(double x) {
		return 0.3 * x * Math.sin(2 * Math.PI * x);
	}
	
}
