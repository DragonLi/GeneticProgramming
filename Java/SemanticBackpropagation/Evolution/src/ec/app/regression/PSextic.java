package ec.app.regression;

public class PSextic extends Regression {

	@Override
	public double func(double x) {
		return x * x * (1 + x * x * (x * x - 2));
	}

}
