package ec.gp.semantic.breed;

import java.util.TreeMap;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPProblem;
import ec.gp.GPTree;
import ec.gp.koza.KozaFitness;
import ec.gp.semantic.ISemanticProblem;
import ec.gp.semantic.func.SimpleNodeBase;
import ec.gp.semantic.statistics.GenericStatistics;
import ec.gp.semantic.utils.SemanticInverter.ErrorNodePair;
import ec.util.Parameter;

/**
 * Globally Geometric Semantic Crossover. The variant, that chooses the replacement locus randomly and calculates
 * additional statistics.
 * 
 * @author Tomasz Pawlak
 * 
 */
public class RDOStats extends RDO {

	private final GenericStatistics statistics = new GenericStatistics();

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
		this.statistics.setup(state, base.push("stats"));
	}

	@Override
	public int produce(int min, int max, int start, int subpopulation, Individual[] inds, EvolutionState state,
			int thread) {
		// grab individuals from our source and stick 'em right into inds.
		// we'll modify them from there
		int n = sources[0].produce(min, max, start, subpopulation, inds, state, thread);

		// should we bother?
		if (!state.random[thread].nextBoolean(likelihood))
			return reproduce(n, start, subpopulation, inds, state, thread, false); // DON'T produce children from source -- we already did

		// now let's mutate 'em
		for (int q = start; q < n + start; q++) {
			GPIndividual i = (GPIndividual) inds[q];

			if (tree != TREE_UNFIXED && (tree < 0 || tree >= i.trees.length))
				// uh oh
				state.output
						.fatal("GP Mutation Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual");

			int t;
			// pick random tree
			if (tree == TREE_UNFIXED)
				if (i.trees.length > 1)
					t = state.random[thread].nextInt(i.trees.length);
				else
					t = 0;
			else
				t = tree;

			// validity result...
			boolean res = false;

			// prepare the nodeselector
			nodeselect.reset();

			ISemanticProblem<?> provider = (ISemanticProblem<?>) state.evaluator.p_problem;
			inverter.invertTree(i.trees[t], provider.getTargetSemantics());

			// pick a node

			GPNode p1 = null; // the node we pick
			GPNode p2 = null;
			ErrorNodePair replacement = null;

			for (int x = 0; x < numTries; x++) {
				// pick a node in individual 1
				p1 = nodeselect.pickNode(state, subpopulation, thread, i, i.trees[t]);

				replacement = inverter.getBestReplacementFor((SimpleNodeBase<?>) p1, this.maxDepth - p1.atDepth());

				if (replacement == null) {
					continue;
				} else {
					res = true;
					p2 = replacement.replacingNode;

					// check for depth and swap-compatibility limits
					assert verifyPoints(p2, p1); // p2 can fit in p1's spot  -- the order is important!;

					break;
				}
			}

			inverter.clearCache();

			GPIndividual j;

			if (sources[0] instanceof BreedingPipeline)
			// it's already a copy, so just smash the tree in
			{
				j = i;
				if (res) // we're in business
				{
					p2.parent = p1.parent;
					p2.argposition = p1.argposition;
					if (p2.parent instanceof GPNode)
						((GPNode) (p2.parent)).children[p2.argposition] = p2;
					else
						((GPTree) (p2.parent)).child = p2;
					j.evaluated = false; // we've modified it
				}
			} else // need to clone the individual
			{
				j = (GPIndividual) (i.lightClone());

				// Fill in various tree information that didn't get filled in there
				j.trees = new GPTree[i.trees.length];

				// at this point, p1 or p2, or both, may be null.
				// If not, swap one in.  Else just copy the parent.
				for (int x = 0; x < j.trees.length; x++) {
					if (x == t && res) // we've got a tree with a kicking cross position!
					{
						j.trees[x] = (GPTree) (i.trees[x].lightClone());
						j.trees[x].owner = j;
						j.trees[x].child = i.trees[x].child.cloneReplacingNoSubclone(p2, p1);
						j.trees[x].child.parent = j.trees[x];
						j.trees[x].child.argposition = 0;
						j.evaluated = false;
						((SimpleNodeBase<?>) j.trees[x].child).resetSemanticsRecursive();
					} // it's changed
					else {
						j.trees[x] = (GPTree) (i.trees[x].lightClone());
						j.trees[x].owner = j;
						j.trees[x].child = (GPNode) (i.trees[x].child.clone());
						j.trees[x].child.parent = j.trees[x];
						j.trees[x].child.argposition = 0;
					}
				}
			}

			// add the new individual, replacing its previous source
			inds[q] = j;

			// STATISTICS
			{
				if (Double.isInfinite(replacement.currentNodeError) || Double.isNaN(replacement.currentNodeError))
					replacement.currentNodeError = Double.MAX_VALUE;

				if (Double.isInfinite(replacement.replacingNodeError) || Double.isNaN(replacement.replacingNodeError))
					replacement.replacingNodeError = Double.MAX_VALUE;

				TreeMap<String, Object> stats = new TreeMap<String, Object>();
				stats.put("currentNodeError", replacement.currentNodeError);
				stats.put("replacingNodeError", replacement.replacingNodeError);
				stats.put("parentFitness", ((KozaFitness) i.fitness).standardizedFitness());

				GPProblem problem = (GPProblem) state.evaluator.p_problem;
				problem.evaluate(state, j, subpopulation, thread);

				stats.put("offspringFitness", ((KozaFitness) j.fitness).standardizedFitness());

				boolean isConstant = replacement.replacingNode instanceof ec.app.semanticGP.func.numeric.Constant
						|| replacement.replacingNode instanceof ec.app.semanticGP.func.logic.Constant;
				stats.put("zConstant", isConstant ? 1 : 0);

				this.statistics.write(stats);
			}
			// END STATISTICS

		}
		return n;
	}
}
