package ec.gp.semantic;

import java.util.List;

import library.semantics.TestCase;

import ec.gp.semantic.func.SimpleNodeBase;

public interface ISemanticProblem<SemType> {

	/**
	 * Gets desired semantics (the target).
	 * 
	 * @return The target semantics.
	 */
	ISemantics getTargetSemantics();

	List<TestCase<SemType>> getFitnessCases();

	/**
	 * Calculates semantics of given program.
	 * 
	 * @param tree
	 *            Program to evaluate
	 */
	//ISemantics computeSemantics(SimpleNodeBase<SemType> tree);
}
