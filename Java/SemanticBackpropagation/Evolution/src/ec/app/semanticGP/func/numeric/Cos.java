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

public final class Cos extends UnaryNode<Double> {

	 private static final String COS = "cos";
	
	@Override
	public String toString() {
		return COS;
	}

	@Override
	protected ISemantics execute(ISemantics... arguments) {
		double[] semantics = new double[arguments[0].size()];

		for (int i = 0; i < semantics.length; ++i) {
			semantics[i] = Math.cos((Double) arguments[0].getValue(i));
		}

		return new DoubleSemantics(semantics);
	}

	@Override
	public Double[] invert(Double output, int missingArgIdx, Double... restOfArguments) {
		if (output > 1.0 || output < -1.0)
			return new Double[] { null };

		double acos = Math.acos(output);
		// we return one positive and one negative value
		return new Double[] { acos, acos - Math.PI - Math.PI };
	}

}
