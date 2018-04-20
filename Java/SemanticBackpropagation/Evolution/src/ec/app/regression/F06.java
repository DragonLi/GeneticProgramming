package ec.app.regression;

public class F06 extends Regression {

	@Override
	public double func(double x) {
		return Math.sin(x) + Math.sin(x + x * x);
	}

}
