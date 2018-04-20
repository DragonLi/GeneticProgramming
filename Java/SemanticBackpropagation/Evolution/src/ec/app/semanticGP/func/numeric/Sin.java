package ec.app.semanticGP.func.numeric;

import ec.EvolutionState;
import ec.Problem;
import ec.app.semanticGP.RegressionData;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.semantic.DesiredSemanticsBase;
import ec.gp.semantic.DoubleSemantics;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.UnaryNode;

public final class Sin extends UnaryNode<Double> {

	private static final String SIN = "sin";

	@Override
	public String toString() {
		return SIN;
	}

	@Override
	protected ISemantics execute(ISemantics... arguments) {
		double[] semantics = new double[arguments[0].size()];

		for (int i = 0; i < semantics.length; ++i) {
			semantics[i] = Math.sin((Double) arguments[0].getValue(i));
		}

		return new DoubleSemantics(semantics);
	}

	@Override
	public Double[] invert(Double output, int missingArgIdx, Double... restOfArguments) {
		if (output > 1.0 || output < -1.0)
			return new Double[] { null };

		double asin = Math.asin(output);
		// we return one positive and one negative value
		return new Double[] { asin, asin - Math.PI - Math.PI };
	}
}
