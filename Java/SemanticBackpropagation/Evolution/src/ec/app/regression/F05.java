package ec.app.regression;

public class F05 extends Regression {

	@Override
	public double func(double x) {
		return Math.sin(x * x) * Math.cos(x) - 1;
	}

}
