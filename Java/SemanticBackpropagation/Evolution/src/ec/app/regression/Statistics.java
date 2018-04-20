package ec.app.regression;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.Subpopulation;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.koza.KozaFitness;
import ec.gp.semantic.utils.Pair;
import ec.simple.SimpleStatistics;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

public class Statistics extends SimpleStatistics {
	private static final long serialVersionUID = 1L;

	private static final String OUT_DIR = "outDir";

	private boolean printIndividual = false;
	private FileWriter outputWriter = null;

	protected long timeStarted;

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		Locale.setDefault(Locale.ENGLISH);

		try {
			String outputDirectory = state.parameters.getString(base.push(OUT_DIR), null);
			File output = null;
			for (int i = 0; output == null; ++i) {
				output = new File(String.format("%s%coutput_canonic_%s_j%d.%d.csv", outputDirectory,
						File.separatorChar, state.evaluator.p_problem.getClass().getSimpleName(), state.job[0], i));
				if (!output.createNewFile())
					output = null;
			}
			outputWriter = new FileWriter(output);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		this.timeStarted = System.currentTimeMillis();
	}

	@Override
	public void postInitializationStatistics(final EvolutionState state) {
		super.postInitializationStatistics(state);

		ParameterDatabase params = state.parameters;

		try {
			// write configuration information
			outputWriter.write("ParamFilename;Problem;PopSize\n");

			Class<?> problem = (Class<?>) params.getClassForParameter(new Parameter("eval.problem"), null,
					Problem.class);
			int popSize = params.getInt(new Parameter("pop.subpop.0.size"), null);

			outputWriter.write(String.format("%s;%s;%d\n", params.getFilename(),
					problem.getSimpleName().replaceFirst("Factory", ""), popSize));

			// write header
			outputWriter
					.write("Generation;BestFitness;BestHits;AvgFitness;FitnessStddev;TestSetFitness;TestSetHits;AvgTreeDepth;TreeDepthStddev;AvgNodeCount;NodeCountStddev;TimeElapsed");
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

		Pair<Double, Integer> testSet = this.getTestSetResults((GPIndividual) this.getBestSoFar()[0], state);

		try {

			outputWriter.write(String.format("%d;%f;%d;%.2f;%.2f;%f;%d;%.2f;%.2f;%.2f;%.2f;%.2f", state.generation,
					((KozaFitness) this.best_of_run[0].fitness).standardizedFitness(),
					((KozaFitness) this.best_of_run[0].fitness).hits, fitnessAvg, fitnessStddev, testSet.value1,
					testSet.value2, treeDepthAvg, treeDepthStddev, nodeCountAvg, nodeCountStddev,
					(double) (System.currentTimeMillis() - this.timeStarted) * 0.001));

			if (printIndividual)
				outputWriter.write(String.format(";%s",
						((GPIndividual) this.best_of_run[0]).trees[0].child.toStringForHumans()));
			outputWriter.write("\n");

		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	protected Pair<Double, Integer> getTestSetResults(final GPIndividual ind, final EvolutionState state) {
		ind.evaluated = false;

		Regression problem = (Regression) state.evaluator.p_problem;
		problem.computeTestSet(state, true);
		problem.evaluate(state, ind, 0, 0);

		KozaFitness fitness = (KozaFitness) ind.fitness;
		return new Pair<Double, Integer>((double) fitness.standardizedFitness(), fitness.hits);
	}

	@Override
	public void finalStatistics(final EvolutionState state, final int result) {
		super.finalStatistics(state, result);

		try {
			// take best individual and evaluate it on test set
			/*
			 * GPIndividual ind = (GPIndividual) this.getBestSoFar()[0]; ind.evaluated = false;
			 * 
			 * Regression problem = (Regression) state.evaluator.p_problem; problem.computeTestSet(state, true);
			 * problem.evaluate(state, ind, 0, 0);
			 * 
			 * KozaFitness fitness = (KozaFitness) ind.fitness;
			 * 
			 * // write statistics as last generation
			 * outputWriter.write(String.format("%d;%f;%d;0;0;%d;0;0;0;0;0;%.2f\n", state.generation + 1,
			 * fitness.standardizedFitness(), fitness.hits, ind.trees[0].child.numNodes(GPNode.NODESEARCH_ALL), (double)
			 * (System.currentTimeMillis() - this.timeStarted) * 0.001));
			 */

			outputWriter.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
