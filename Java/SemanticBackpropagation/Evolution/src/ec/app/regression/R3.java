package ec.app.regression;

public class R3 extends Regression {

	@Override
	public double func(double x) {
		return (x * x * x * x * x * x + x) / (x * (x * (x * (x + 1) + 1) + 1) + 1);
	}

}
