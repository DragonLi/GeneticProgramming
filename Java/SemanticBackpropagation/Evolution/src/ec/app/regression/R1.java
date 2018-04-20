package ec.app.regression;

public class R1 extends Regression {

	@Override
	public double func(double x) {
		return (x + 1) * (x + 1) * (x + 1) / (x * x - x + 1);
	}

}
