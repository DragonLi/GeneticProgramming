package ec.app.semanticGP.func.logic;

import java.util.List;

import library.semantics.BitSet;
import library.semantics.TestCase;
import ec.EvolutionState;
import ec.gp.semantic.BooleanSemantics;
import ec.gp.semantic.ISemanticProblem;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.NullaryNode;
import ec.util.Parameter;

public class ERC extends NullaryNode<Boolean> {

	private ISemantics semantics;

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
	}

	@Override
	protected ISemantics execute(ISemantics... arguments) {
		return this.semantics;
	}

	public void resetNode(EvolutionState state, int thread) {
		super.resetNode(state, thread);
		ISemanticProblem<Boolean> problem = (ISemanticProblem<Boolean>) this.state.evaluator.p_problem;
		List<TestCase<Boolean>> cases = problem.getFitnessCases();

		BitSet semantics = new BitSet(cases.size());
		if (state.random[thread].nextBoolean()) {
			semantics.set(0, semantics.length());
		} else {
			semantics.clear(); //TODO: is it necessary?
		}

		this.semantics = new BooleanSemantics(semantics);
	}

	@Override
	public String toString() {
		if (this.semantics != null) {
			return this.semantics.getValue(0).toString();
		}
		return "ERC";
	}

}
