package ec.app.semanticGP.func.logic;

import java.util.List;

import library.semantics.BitSet;
import library.semantics.TestCase;
import ec.EvolutionState;
import ec.gp.GPNode;
import ec.gp.semantic.BooleanSemantics;
import ec.gp.semantic.ISemanticProblem;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.NullaryNode;

public final class Constant extends NullaryNode<Boolean> {

	private final ISemantics semantics;

	public Constant(EvolutionState state, boolean value) {
		this.state = state;
		this.children = new GPNode[0];

		//assert value != null;

		//calculate semantics
		ISemanticProblem<Boolean> problem = (ISemanticProblem<Boolean>) this.state.evaluator.p_problem;
		List<TestCase<Boolean>> cases = problem.getFitnessCases();

		BitSet semantics = new BitSet(cases.size());
		semantics.set(0, semantics.length(), value);
		this.semantics = new BooleanSemantics(semantics);
	}

	@Override
	protected ISemantics execute(ISemantics... arguments) {
		return this.semantics;
	}

	@Override
	public String toString() {
		return (Boolean) this.semantics.getValue(0) ? "1" : "0";
	}

	@Override
	public int nodeHashCode() {
		return this.hashCode() ^ this.semantics.getValue(0).hashCode();
	}
}
