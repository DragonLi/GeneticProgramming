package ec.app.semanticGP.func.numeric;

import ec.EvolutionState;
import ec.Problem;
import ec.app.semanticGP.RegressionData;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.semantic.DoubleSemantics;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.BinaryNode;

public final class Sub extends BinaryNode<Double> {

	private static final String MINUS = "-";

	@Override
	public String toString() {
		return MINUS;
	}

	@Override
	protected ISemantics execute(ISemantics... arguments) {
		double[] semantics = new double[arguments[0].size()];

		for (int i = 0; i < semantics.length; ++i) {
			semantics[i] = (Double) arguments[0].getValue(i) - (Double) arguments[1].getValue(i);
		}

		return new DoubleSemantics(semantics);
	}

	@Override
	public Double[] invert(Double output, int missingArgIdx, Double... restOfArguments) {
		switch (missingArgIdx) {
			case 0:
				return new Double[] { output + restOfArguments[0] };
			case 1:
			default:
				return new Double[] { restOfArguments[0] - output };
		}
	}

}
