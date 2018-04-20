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

public class False extends NullaryNode<Boolean> {

	private static final String FALSE = "0";

	private ISemantics semantics;

	@Override
	public String toString() {
		return FALSE;
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);

		ISemanticProblem<Boolean> problem = (ISemanticProblem<Boolean>) this.state.evaluator.p_problem;
		List<TestCase<Boolean>> cases = problem.getFitnessCases();

		BitSet semantics = new BitSet(cases.size());
		semantics.clear(); //TODO: is it necessary?

		this.semantics = new BooleanSemantics(semantics);
	}

	@Override
	protected ISemantics execute(ISemantics... arguments) {
		return this.semantics;
	}
}
