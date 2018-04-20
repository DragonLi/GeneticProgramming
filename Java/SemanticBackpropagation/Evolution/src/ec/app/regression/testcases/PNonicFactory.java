package ec.app.regression.testcases;

public class PNonicFactory extends FunctionFactory {

	@Override
	protected double function(final double x) {
		return x * (1 + x * (1 + x * (1 + x * (1 + x * (1 + x * (1 + x * (1 + x * (1 + x))))))));
	}

}
