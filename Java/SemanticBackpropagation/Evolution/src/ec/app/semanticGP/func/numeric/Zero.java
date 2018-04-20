package ec.app.semanticGP.func.numeric;

import java.util.Arrays;
import java.util.List;

import library.semantics.TestCase;

import ec.EvolutionState;
import ec.gp.semantic.DoubleSemantics;
import ec.gp.semantic.ISemanticProblem;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.NullaryNode;
import ec.util.Parameter;

public final class Zero extends NullaryNode<Double> {

	private static final String ZERO = "0";

	private ISemantics semantics;

	@Override
	public String toString() {
		return ZERO;
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);

		ISemanticProblem<Double> problem = (ISemanticProblem<Double>) this.state.evaluator.p_problem;
		List<TestCase<Double>> cases = problem.getFitnessCases();

		double[] semantics = new double[cases.size()];
		Arrays.fill(semantics, 0.0);

		this.semantics = new DoubleSemantics(semantics);
	}

	@Override
	protected ISemantics execute(ISemantics... arguments) {
		return this.semantics;
	}

}
