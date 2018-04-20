package ec.app.regression;

public class Keijzer04 extends Regression {

	@Override
	public double func(double x) {
		double sinx = Math.sin(x);
		double sincosx = sinx * Math.cos(x);
		return x * x * x * Math.exp(-x) * sincosx * (sinx * sincosx - 1);
	}

}
