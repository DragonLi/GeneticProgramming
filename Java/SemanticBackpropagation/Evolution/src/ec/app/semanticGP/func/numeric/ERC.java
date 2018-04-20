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

public final class ERC extends NullaryNode<Double> {

	private ISemantics semantics;

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
	}

	@Override
	public void resetNode(EvolutionState state, int thread) {
		super.resetNode(state, thread);
		ISemanticProblem<Double> problem = (ISemanticProblem<Double>) this.state.evaluator.p_problem;
		List<TestCase<Double>> cases = problem.getFitnessCases();

		double[] semantics = new double[cases.size()];
		Arrays.fill(semantics, this.state.random[0].nextDouble() * 2.0 - 1.0);

		this.semantics = new DoubleSemantics(semantics);
	}

	@Override
	public String toString() {
		if (this.semantics != null)
			return this.semantics.getValue(0).toString();
		return "ERC";
	}

	@Override
	protected ISemantics execute(ISemantics... arguments) {
		return this.semantics;
	}

}
