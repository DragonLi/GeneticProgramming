package ec.app.semanticGP.func.logic;

import java.util.List;

import library.semantics.BitSet;
import library.semantics.TestCase;
import ec.gp.semantic.BooleanSemantics;
import ec.gp.semantic.ISemanticProblem;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.NullaryNode;

public class Input0 extends NullaryNode<Boolean> {

	protected int getInputNumber() {
		return 0;
	}

	@Override
	protected ISemantics execute(ISemantics... arguments) {
		ISemanticProblem<Boolean> problem = (ISemanticProblem<Boolean>) this.state.evaluator.p_problem;
		List<TestCase<Boolean>> cases = problem.getFitnessCases();

		BitSet semantics = new BitSet(cases.size());
		for (int i = 0; i < cases.size(); ++i) {
			semantics.set(i, cases.get(i).getArguments()[this.getInputNumber()]);
		}

		return new BooleanSemantics(semantics);
	}

	@Override
	public String toString() {
		return "in" + this.getInputNumber();
	}

}
