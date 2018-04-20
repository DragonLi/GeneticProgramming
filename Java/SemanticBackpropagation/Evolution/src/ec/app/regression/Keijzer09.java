package ec.app.regression;

public class Keijzer09 extends Regression {

	@Override
	public double func(double x) {
		// asinh(x)
		return Math.log(x + Math.sqrt(x * x + 1));
	}

}
