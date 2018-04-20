package ec.app.semanticGP.func.numeric;

import java.util.Arrays;
import java.util.List;

import library.semantics.TestCase;

import ec.EvolutionState;
import ec.gp.GPNode;
import ec.gp.semantic.DoubleSemantics;
import ec.gp.semantic.ISemanticProblem;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.NullaryNode;

public final class Constant extends NullaryNode<Double> {

	private final ISemantics semantics;

	public Constant(EvolutionState state, Double value) {
		this.state = state;
		this.children = new GPNode[0];
		
		assert value != null;
		
		// calculate semantics
		ISemanticProblem<Double> problem = (ISemanticProblem<Double>) this.state.evaluator.p_problem;
		List<TestCase<Double>> cases = problem.getFitnessCases();

		double[] semantics = new double[cases.size()];
		Arrays.fill(semantics, value);
		this.semantics = new DoubleSemantics(semantics);
	}
	
	@Override
	protected ISemantics execute(ISemantics... arguments) {
		return this.semantics;
	}

	@Override
	public String toString() {
		return this.semantics.getValue(0).toString();
	}

	@Override
	public int nodeHashCode() {
		return this.hashCode() ^ this.semantics.getValue(0).hashCode();
	}
}
