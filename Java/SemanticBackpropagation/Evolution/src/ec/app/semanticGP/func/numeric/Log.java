package ec.app.semanticGP.func.numeric;

import ec.gp.semantic.DoubleSemantics;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.UnaryNode;

public final class Log extends UnaryNode<Double> {

	private static final String LOG = "log";
	
	@Override
	public String toString() {
		return LOG;
	}

	@Override
	protected ISemantics execute(ISemantics... arguments) {
		double[] semantics = new double[arguments[0].size()];

		assert new Double(-1.0 / 0.0) == Double.NEGATIVE_INFINITY;

		for (int i = 0; i < semantics.length; ++i) {
			semantics[i] = Math.log(Math.abs((Double) arguments[0].getValue(i)));
			//if (semantics[i] == Double.NEGATIVE_INFINITY) {
			//	semantics[i] = -1E300;
			//}
		}

		return new DoubleSemantics(semantics);
	}

	@Override
	public Double[] invert(Double output, int missingArgIdx, Double... restOfArguments) {
		double exp = Math.exp(output);
		//if (Double.isInfinite(exp))
		//	exp = 1E300;
		return new Double[] { exp, -exp };
	}
}
