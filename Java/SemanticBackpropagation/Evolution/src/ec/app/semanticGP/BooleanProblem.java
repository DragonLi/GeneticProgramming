package ec.app.semanticGP;

import java.util.List;

import library.INamedElement;
import library.semantics.TestCase;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.gp.koza.KozaFitness;
import ec.gp.semantic.BooleanSemantics;
import ec.gp.semantic.ISemanticProblem;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.SimpleNodeBase;
import ec.simple.SimpleProblemForm;

public abstract class BooleanProblem extends GPProblem implements ISemanticProblem<Boolean>, SimpleProblemForm,
		INamedElement {

	protected List<TestCase<Boolean>> trainingSet;
	protected BooleanSemantics targetSemantics;

	@Override
	public void evaluate(EvolutionState state, Individual individual, int subpopulation, int threadnum) {
		if (individual.evaluated)
			return;

		GPIndividual ind = (GPIndividual) individual;
		BooleanSemantics semantics = (BooleanSemantics) ((SimpleNodeBase<Boolean>) ind.trees[0].child).getSemantics();
		List<TestCase<Boolean>> testCases = this.getFitnessCases();
		
		int hits = 0;
		
		assert semantics.size() == testCases.size();
		for (int i = 0; i < semantics.size(); ++i) {
			Boolean targetValue = testCases.get(i).getValue();
			if (targetValue == null /* don't care */ || targetValue == semantics.getValue(i))
				++hits;
		}

		KozaFitness fitness = (KozaFitness) ind.fitness;
		fitness.hits = hits;
		fitness.setStandardizedFitness(state, semantics.size() - hits);

		ind.evaluated = true;
	}

	protected Boolean[] asArray(int i, int bitCount) {
		Boolean[] array = new Boolean[bitCount];
		for (int b = 0; b < bitCount; ++b) {
			array[bitCount - b - 1] = ((i >>> b) & 1) == 1;
		}
		return array;
	}

	@Override
	public ISemantics getTargetSemantics() {
		return this.targetSemantics;
	}

	@Override
	public List<TestCase<Boolean>> getFitnessCases() {
		// we do not distinguish between training and test set, since training set contains all possible cases
		return this.trainingSet;
	}

}
