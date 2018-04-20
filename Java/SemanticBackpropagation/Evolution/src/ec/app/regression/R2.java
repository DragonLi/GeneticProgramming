package ec.app.regression;

public class R2 extends Regression {

	@Override
	public double func(double x) {
		return (x * x * (x * (x * x - 3)) + 1) / (x * x + 1);
	}

}
