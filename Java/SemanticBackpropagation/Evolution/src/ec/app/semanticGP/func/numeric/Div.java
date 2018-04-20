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

public final class Div extends BinaryNode<Double> {

	private static final String DIV = "/";

	@Override
	public String toString() {
		return DIV;
	}

	@Override
	protected ISemantics execute(ISemantics... arguments) {
		double[] semantics = new double[arguments[0].size()];

		for (int i = 0; i < semantics.length; ++i) {
			Double arg1 = (Double) arguments[1].getValue(i);
			if (arg1 != 0.0)
				semantics[i] = (Double) arguments[0].getValue(i) / arg1;
			//else
			//	semantics[i] = 0.0; // semantics is of type Double[], not double[], so it contains nulls by default
		}

		return new DoubleSemantics(semantics);
	}

	@Override
	public Double[] invert(Double output, int missingArgIdx, Double... restOfArguments) {
		switch (missingArgIdx) {
			case 0:
				if (!Double.isInfinite(restOfArguments[0]))
					return new Double[] { output * restOfArguments[0] };
				else if (output == 0.0)
					return new Double[0]; // don't care
				else
					return new Double[] { null }; // inconsistent
			case 1:
			default:
				if (restOfArguments[0] != 0.0)
					return new Double[] { restOfArguments[0] / output };
				else if (output == 0.0)
					return new Double[0]; // don't care
				else
					return new Double[] { null }; // inconsistent
		}
	}
}
