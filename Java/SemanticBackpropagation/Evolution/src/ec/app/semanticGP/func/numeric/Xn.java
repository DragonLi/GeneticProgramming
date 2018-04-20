package ec.app.semanticGP.func.numeric;

import java.util.List;

import library.semantics.TestCase;
import ec.gp.semantic.DoubleSemantics;
import ec.gp.semantic.ISemanticProblem;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.NullaryNode;

public class Xn extends NullaryNode<Double> {

	private final static String X = "X";

	private final int n;

	public Xn(int n) {
		this.n = n;
	}

	@Override
	protected ISemantics execute(ISemantics... arguments) {
		ISemanticProblem<Double> problem = (ISemanticProblem<Double>) this.state.evaluator.p_problem;
		List<TestCase<Double>> cases = problem.getFitnessCases();

		double[] semantics = new double[cases.size()];
		for (int i = 0; i < cases.size(); ++i) {
			semantics[i] = cases.get(i).getArguments()[this.n];
		}

		return new DoubleSemantics(semantics);
	}

	@Override
	public String toString() {
		return X + this.n;
	}

}
