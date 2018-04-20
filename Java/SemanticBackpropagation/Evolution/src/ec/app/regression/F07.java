package ec.app.regression;

public class F07 extends Regression {

	@Override
	public double func(double x) {
		return Math.log(1 + x * (1 + x * (1 + x)));
	}

}
