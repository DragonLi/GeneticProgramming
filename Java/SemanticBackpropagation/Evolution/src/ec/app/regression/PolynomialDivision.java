package ec.app.regression;

public class PolynomialDivision extends Regression {
	public double func(double x) {
		return 4.0 * (x * x * x * x * x - x * x * x) / (x * x * x * x + 1);
	}
}
