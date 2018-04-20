package ec.app.regression.testcases;

public class PolynomialDivisionFactory extends FunctionFactory {

	@Override
	protected double function(double x) {
		return 4.0 * (x * x * x * x * x - x * x * x) / (x * x * x * x + 1);
	}

}
