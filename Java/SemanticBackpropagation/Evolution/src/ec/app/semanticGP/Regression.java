package ec.app.semanticGP;

import java.util.List;

import library.INamedElement;
import library.semantics.TestCase;
import ec.EvolutionState;
import ec.Individual;
import ec.app.regression.testcases.TestCaseFactory;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.gp.koza.KozaFitness;
import ec.gp.semantic.DoubleSemantics;
import ec.gp.semantic.ISemanticProblem;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.evaluation.EvaluationMode;
import ec.gp.semantic.evaluation.IChangeableEvaluationMode;
import ec.gp.semantic.func.SimpleNodeBase;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;

public class Regression extends GPProblem implements SimpleProblemForm, ISemanticProblem<Double>,
		IChangeableEvaluationMode, INamedElement {

	private static final long serialVersionUID = 1L;

	public final static double HIT_LEVEL = 0.01;
	public final static double PROBABLY_ZERO = 1.11E-15;
	public final static double BIG_NUMBER = 1.0e15; // the same as lilgp uses

	protected EvolutionState state;

	private EvaluationMode evaluationMode = EvaluationMode.TrainingSet;
	protected List<TestCase<Double>> trainingCases;
	protected List<TestCase<Double>> testCases;

	protected double[] targetSemantics;
	protected ISemantics targetSemanticsObject;
	protected ISemantics targetSemanticsArguments;

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		try {
			this.state = state;

			TestCaseFactory factory = TestCaseFactory.getFactory(state);
			this.trainingCases = factory.generateTraining(state);
			this.testCases = factory.generateTest(state);

			this.targetSemantics = new double[this.trainingCases.size()];
			for (int i = 0; i < this.targetSemantics.length; ++i) {
				this.targetSemantics[i] = this.trainingCases.get(i).getValue();
			}
			this.targetSemanticsObject = new DoubleSemantics(this.targetSemantics);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<TestCase<Double>> getFitnessCases() {
		if (this.evaluationMode == EvaluationMode.TrainingSet)
			return this.trainingCases;
		return this.testCases;
	}

	@Override
	public ISemantics getTargetSemantics() {
		return this.targetSemanticsObject;
	}

	@Override
	public Regression clone() {
		Regression cloned = (Regression) super.clone();
		cloned.data = new RegressionData();
		return cloned;
	}

	@Override
	public void evaluate(final EvolutionState state, final Individual individual, final int subpopulation,
			final int threadnum) {
		if (individual.evaluated)
			return;

		final GPIndividual ind = (GPIndividual) individual;
		double fitness = 0.0;
		double error;
		int hits = 0;

		ISemantics semantics = ((SimpleNodeBase<?>) ind.trees[0].child).getSemantics();

		List<TestCase<Double>> fitnessCases = this.getFitnessCases();
		for (int i = 0; i < fitnessCases.size(); ++i) {

			error = Math.abs((Double) fitnessCases.get(i).getValue() - (Double) semantics.getValue(i));

			if (!(error < BIG_NUMBER)) // *NOT* (input.x >= BIG_NUMBER)
				error = BIG_NUMBER;
			else if (error < PROBABLY_ZERO)
				error = 0.0;

			if (error <= HIT_LEVEL)
				hits++;

			fitness += error;
		}

		fitness /= fitnessCases.size();
		
		final KozaFitness f = (KozaFitness) ind.fitness;
		f.setStandardizedFitness(state, (float) fitness);
		f.hits = hits;
	}

	@Override
	public EvaluationMode getEvaluationMode() {
		return this.evaluationMode;
	}

	@Override
	public void setEvaluationMode(EvaluationMode mode) {
		this.evaluationMode = mode;
	}

	@Override
	public String getName() {
		return TestCaseFactory.getFactory(state).getClass().getSimpleName();
	}

}
