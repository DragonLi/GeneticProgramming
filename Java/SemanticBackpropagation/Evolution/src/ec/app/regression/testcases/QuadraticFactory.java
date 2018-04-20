package ec.app.regression.testcases;

public class QuadraticFactory extends FunctionFactory {

	@Override
	protected double function(double x) {
		return x * x - x;
	}

}
