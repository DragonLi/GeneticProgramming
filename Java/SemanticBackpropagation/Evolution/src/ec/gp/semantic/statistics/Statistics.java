package ec.gp.semantic.statistics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import library.INamedElement;
import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.gp.koza.KozaFitness;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.evaluation.EvaluationMode;
import ec.gp.semantic.evaluation.IChangeableEvaluationMode;
import ec.gp.semantic.func.SimpleNodeBase;
import ec.gp.semantic.utils.Pair;
import ec.simple.SimpleProblemForm;
import ec.simple.SimpleStatistics;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

public class Statistics extends SimpleStatistics {

	private static final String OUT_DIR = "outDir";
	private static final String PRINT_INDIVIDUAL = "printIndividual";

	private static final String MAX_TREE_DEPTH = "gp.koza.xover.maxdepth";
	private static final String MAX_SUBTREE_DEPTH = "library.maxChildDepth";
	// private static final String IS_RANDOM = "gp.breed.metricXOver.random";
	private static final String GEO_XOVER_LIKELIHOOD = "pop.subpop.0.species.pipe.source.0.prob";
	private static final String SUBTREE_XOVER_LIKELIHOOD = "pop.subpop.0.species.pipe.source.1.prob";

	private static final Pair<Double, Integer> WRONG_TEST_SET_RESULT = new Pair<Double, Integer>(Double.NaN, 0);

	private FileWriter outputWriter = null;

	protected String outputDirectory = "results";
	protected boolean printIndividual = false;
	protected AtomicInteger crossoverCount = new AtomicInteger(0);
	protected AtomicInteger macromutationCount = new AtomicInteger(0);
	protected int nodeCount = 0;
	protected int librarySearches = 0;

	protected long timeStarted;
	protected long timeLastPop;

	public void crossoverOccurred() {
		this.crossoverCount.incrementAndGet();
	}

	public void macromutateOccurred() {
		this.macromutationCount.incrementAndGet();
	}

	public synchronized void reportChosenProcedureNodeCount(final int count) {
		this.nodeCount += count;
		++this.librarySearches;
	}

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		Locale.setDefault(Locale.ENGLISH);

		ParameterDatabase db = state.parameters;
		INamedElement problem = (INamedElement) state.evaluator.p_problem;

		printIndividual = db.getBoolean(base.push(PRINT_INDIVIDUAL), null, printIndividual);

		// int maxSubtreeDepth = db.getInt(new Parameter(MAX_SUBTREE_DEPTH), null);
		// boolean isRandom = db.getBoolean(new Parameter(IS_RANDOM), null, false);

		try {
			outputDirectory = state.parameters.getString(base.push(OUT_DIR), null);
			File output = null;
			String filename = String.format("%s%coutput_%s_j%d.%%d.csv", outputDirectory, File.separatorChar,
					problem.getName(), state.job[0]);

			for (int i = 0; output == null; ++i) {
				output = new File(String.format(filename, i));
				if (!output.createNewFile())
					output = null;
			}
			outputWriter = new FileWriter(output);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		this.timeStarted = System.currentTimeMillis();
		this.timeLastPop = this.timeStarted;
	}

	@Override
	public void postInitializationStatistics(final EvolutionState state) {
		super.postInitializationStatistics(state);

		ParameterDatabase params = state.parameters;

		try {
			// write configuration information
			outputWriter
					.write("ParamFilename;Problem;PopSize;MaxTreeDepth;MaxSubtreeDepth;GeometricCrossoverProb;SubtreeCrossoverProb\n");

			String problem = ((INamedElement) state.evaluator.p_problem).getName();
			int popSize = params.getInt(new Parameter("pop.subpop.0.size"), null);
			int maxTreeDepth = params.getInt(new Parameter(MAX_TREE_DEPTH), null);
			int maxSubtreeDepth = params.getIntWithDefault(new Parameter(MAX_SUBTREE_DEPTH), null, 0);
			double geoXOverLikelihood = params.getDouble(new Parameter(GEO_XOVER_LIKELIHOOD), null);
			double subtreeXOverLikelihood = params.getDouble(new Parameter(SUBTREE_XOVER_LIKELIHOOD), null);

			outputWriter.write(String.format("%s;%s;%d;%d;%d;%.2f;%.2f\n", params.getFilename(), problem, popSize,
					maxTreeDepth, maxSubtreeDepth, geoXOverLikelihood, subtreeXOverLikelihood));

			// write header
			outputWriter
					.write("Generation;BestFitness;BestHits;AvgFitness;FitnessStddev;TestSetFitness;TestSetHits;AvgTreeDepth;TreeDepthStddev;AvgNodeCount;NodeCountStddev;UniqueSemantics;UniqueIndividuals;CrossoverCount;MacromutateCount;TimeElapsed");
			if (printIndividual)
				outputWriter.write(";BestIndividual");
			outputWriter.write("\n");

		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void postEvaluationStatistics(final EvolutionState state) {
		super.postEvaluationStatistics(state);

		final Subpopulation pop = state.population.subpops[0];
		final Individual[] individuals = pop.individuals;

		double fitnessAvg = 0.0;
		double fitnessStddev = 0.0;
		double treeDepthAvg = 0.0;
		double treeDepthStddev = 0.0;
		double nodeCountAvg = 0.0;
		double nodeCountStddev = 0.0;
		double bestFitness = Double.MAX_VALUE;
		int bestHits = 0;

		double tmp;

		GPIndividual ind;
		GPNode root;
		KozaFitness fitness;
		for (int i = 0; i < individuals.length; ++i) {
			ind = (GPIndividual) individuals[i];
			fitness = (KozaFitness) ind.fitness;
			root = ind.trees[0].child;

			// compute fitness statistics
			tmp = fitness.standardizedFitness();
			fitnessAvg += tmp;
			fitnessStddev += tmp * tmp;
			if (tmp < bestFitness)
				bestFitness = tmp;

			if (fitness.hits > bestHits)
				bestHits = fitness.hits;

			// compute tree depth statistics
			tmp = root.depth();
			treeDepthAvg += tmp;
			treeDepthStddev += tmp * tmp;

			// compute node count statistics
			tmp = root.numNodes(GPNode.NODESEARCH_ALL);
			nodeCountAvg += tmp;
			nodeCountStddev += tmp * tmp;
		}

		final double countF = (double) individuals.length;
		final double invCountF = 1.0 / countF;
		fitnessAvg *= invCountF;
		fitnessStddev = Math.sqrt(fitnessStddev * invCountF - fitnessAvg * fitnessAvg);
		treeDepthAvg *= invCountF;
		treeDepthStddev = Math.sqrt(treeDepthStddev * invCountF - treeDepthAvg * treeDepthAvg);
		nodeCountAvg *= invCountF;
		nodeCountStddev = Math.sqrt(nodeCountStddev * invCountF - nodeCountAvg * nodeCountAvg);

		final Pair<Double, Integer> testSetResults = this.getTestSetResults((GPIndividual) this.getBestSoFar()[0],
				state);

		final int uniqueSemanticsCount = this.getUniqueSemanticsCount(state);
		final int semanticallyUniqueIndCount = this.getSemanticallyUniqueIndividualCount(state);

		// timers
		final long now = System.currentTimeMillis();
		final double generationTime = (double) (now - this.timeLastPop) * 0.001;
		final double totalTime = (double) (now - this.timeStarted) * 0.001;
		this.timeLastPop = now;

		try {

			outputWriter.write(String.format("%d;%f;%d;%.2f;%.2f;%f;%d;%.2f;%.2f;%.2f;%.2f;%d;%d;%d;%d;%.2f",
					state.generation, bestFitness, bestHits, fitnessAvg, fitnessStddev, testSetResults.value1,
					testSetResults.value2, treeDepthAvg, treeDepthStddev, nodeCountAvg, nodeCountStddev,
					uniqueSemanticsCount, semanticallyUniqueIndCount, this.crossoverCount.getAndSet(0), this.macromutationCount.getAndSet(0) >> 1,
					totalTime));

			if (printIndividual)
				outputWriter.write(String.format(";%s",
						((GPIndividual) this.best_of_run[0]).trees[0].child.toStringForHumans()));
			outputWriter.write("\n");

			outputWriter.flush();

		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}

		state.output.message(String.format("Generation time: %10.3fs Total time: %10.3fs", generationTime, totalTime));
	}

	/**
	 * 
	 * @param ind
	 * @param state
	 * @return The method returns NaN as fitness if problem does not have test set.
	 */
	protected Pair<Double, Integer> getTestSetResults(final GPIndividual ind, final EvolutionState state) {
		if (!(state.evaluator.p_problem instanceof IChangeableEvaluationMode)) {
			return WRONG_TEST_SET_RESULT;
		}

		// get handles
		IChangeableEvaluationMode pMode = (IChangeableEvaluationMode) state.evaluator.p_problem;
		SimpleProblemForm problem = (SimpleProblemForm) state.evaluator.p_problem;

		// evaluate on test set
		pMode.setEvaluationMode(EvaluationMode.TestSet);
		((SimpleNodeBase<?>) ind.trees[0].child).resetSemanticsRecursive();
		ind.evaluated = false;
		problem.evaluate(state, ind, 0, 0);

		// revert mode to training set
		pMode.setEvaluationMode(EvaluationMode.TrainingSet);

		KozaFitness fitness = (KozaFitness) ind.fitness;

		return new Pair<Double, Integer>((double) fitness.standardizedFitness(), fitness.hits);
	}

	/**
	 * Calculates number of unique semantics in population, (including all subtrees).
	 * 
	 * @param state
	 * @return
	 */
	protected int getUniqueSemanticsCount(final EvolutionState state) {
		Population population = state.population;
		assert population.subpops.length > 0;

		HashSet<ISemantics> unique = new HashSet<ISemantics>(population.subpops[0].individuals.length << 2);

		for (int sp = 0; sp < population.subpops.length; ++sp) {
			Subpopulation subpop = population.subpops[sp];
			for (int i = 0; i < subpop.individuals.length; ++i) {
				GPIndividual ind = (GPIndividual) subpop.individuals[i];
				for (int t = 0; t < ind.trees.length; ++t) {
					GPTree tree = ind.trees[t];
					addSemanticsRecursive((SimpleNodeBase<?>) tree.child, unique);
				}
			}
		}

		return unique.size();
	}
	
	protected int getSemanticallyUniqueIndividualCount(final EvolutionState state) {
		Population population = state.population;
		assert population.subpops.length > 0;

		HashSet<ISemantics> unique = new HashSet<ISemantics>(population.subpops[0].individuals.length << 2);

		for (int sp = 0; sp < population.subpops.length; ++sp) {
			Subpopulation subpop = population.subpops[sp];
			for (int i = 0; i < subpop.individuals.length; ++i) {
				GPIndividual ind = (GPIndividual) subpop.individuals[i];
				for (int t = 0; t < ind.trees.length; ++t) {
					GPTree tree = ind.trees[t];
					unique.add(((SimpleNodeBase<?>) tree.child).getSemantics());
				}
			}
		}

		return unique.size();
	}

	private static void addSemanticsRecursive(final SimpleNodeBase<?> node, HashSet<ISemantics> set) {
		set.add(node.getSemantics());
		for (int i = 0; i < node.children.length; ++i) {
			addSemanticsRecursive((SimpleNodeBase<?>) node.children[i], set);
		}
	}

	@Override
	public void finalStatistics(final EvolutionState state, final int result) {
		super.finalStatistics(state, result);

		state.output.message(String.format("Avg node count of inserted procedure: %.3f", (double) this.nodeCount
				/ (double) this.librarySearches));

		try {
			// take best individual and evaluate it on test set
			/*
			 * GPIndividual ind = (GPIndividual) this.getBestSoFar()[0];
			 * 
			 * // build test set Class<?> cl = TestCaseFactory.getFactoryClass(state); FunctionFactory factory =
			 * (FunctionFactory) cl.newInstance(); factory.setRandomizedPoints(true); List<TestCase> cases =
			 * factory.compute(state);
			 * 
			 * // evaluate double fitness = 0.0; double error; int hits = 0; RegressionData data = new RegressionData();
			 * 
			 * for (final TestCase _case : cases) {
			 * 
			 * data.x = _case.getArguments()[0];
			 * 
			 * ind.trees[0].child.eval(state, 0, data, null, ind, null);
			 * 
			 * error = Math.abs(_case.getValue() - data.y);
			 * 
			 * if (!(error < Regression.BIG_NUMBER)) // *NOT* (input.x >= BIG_NUMBER) error = Regression.BIG_NUMBER;
			 * else if (error < Regression.PROBABLY_ZERO) error = 0.0;
			 * 
			 * if (error <= Regression.HIT_LEVEL) hits++;
			 * 
			 * fitness += error; }
			 * 
			 * // write statistics as last generation
			 * outputWriter.write(String.format("%d;%f;%d;0;0;%d;0;0;0;0;0;%.2f\n", state.generation + 1, fitness, hits,
			 * ind.trees[0].child.numNodes(GPNode.NODESEARCH_ALL), (double) (System.currentTimeMillis() -
			 * this.timeStarted) * 0.001));
			 */

			outputWriter.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
