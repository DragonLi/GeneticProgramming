package ec.app.semanticGP.func.numeric;

import ec.EvolutionState;
import ec.Problem;
import ec.app.semanticGP.RegressionData;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.semantic.DoubleSemantics;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.UnaryNode;

public final class Inv extends UnaryNode<Double> {

	private static final String INV = "1/";

	@Override
	public String toString() {
		return INV;
	}

	@Override
	protected ISemantics execute(ISemantics... arguments) {
		double[] semantics = new double[arguments[0].size()];

		assert new Double(0.0) == new Double(0.0);

		for (int i = 0; i < semantics.length; ++i) {
			Double arg = (Double) arguments[0].getValue(i);
			if (arg != 0.0) {
				semantics[i] = 1.0 / arg;
			}
			semantics[i] = 0.0;
		}

		return new DoubleSemantics(semantics);
	}

	@Override
	public Double[] invert(Double output, int missingArgIdx, Double... restOfArguments) {
		return new Double[] { 1.0 / output };
	}

}
