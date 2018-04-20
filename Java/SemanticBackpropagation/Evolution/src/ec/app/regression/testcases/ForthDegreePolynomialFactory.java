package ec.app.regression.testcases;

public class ForthDegreePolynomialFactory extends FunctionFactory {

	@Override
	protected double function(final double x) {
		return x * x * x * x + 2 * x * x * x - x + 1;
	}

}
