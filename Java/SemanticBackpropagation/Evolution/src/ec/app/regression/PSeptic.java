package ec.app.regression;

public class PSeptic extends Regression {

	@Override
	public double func(double x) {
		return x * (x * (x * (x * (x * (x * (x - 2) + 1) - 1) + 1) - 2) + 1);
	}

}
