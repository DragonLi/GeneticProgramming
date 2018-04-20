package ec.app.regression.testcases;

public class R1Factory extends FunctionFactory {

	@Override
	protected double function(double x) {
		return (x + 1) * (x + 1) * (x + 1) / (x * x - x + 1);
		// return (1 + x * (3 + x * (3 + x))) / (x * (x - 1) + 1);
	}

}
