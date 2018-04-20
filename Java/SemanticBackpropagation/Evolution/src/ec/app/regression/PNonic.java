package ec.app.regression;

public class PNonic extends Regression {

	@Override
	public double func(double x) {
		return x * (1 + x * (1 + x * (1 + x * (1 + x * (1 + x * (1 + x * (1 + x * (1 + x))))))));
	}

}
