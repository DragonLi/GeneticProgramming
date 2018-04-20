package ec.gp.semantic.breed;

import java.util.List;

import library.space.SearchResult;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.gp.breed.GPBreedDefaults;
import ec.gp.koza.CrossoverPipeline;
import ec.gp.semantic.DesiredSemanticsBase;
import ec.gp.semantic.EvoState;
import ec.gp.semantic.ISemanticProblem;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.SimpleNodeBase;
import ec.gp.semantic.library.ILibrary;
import ec.gp.semantic.statistics.Statistics;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

public class NonRandomizedNHX extends CrossoverPipeline {

	private static final Parameter DEFAULT_BASE = GPBreedDefaults.base().push("NonRandomizedNHX");
	private static final String RANDOM = "random";
	private static final String MACROMUTATE_THRESHOLD = "macromutateThreshold";

	protected ILibrary<?> library = null;
	protected boolean random = false;

	protected Statistics stat;
	/**
	 * The square of distance threshold
	 */
	protected double macromutateThreshold = 1;

	protected double[][] averageSemantics;

	@Override
	public Parameter defaultBase() {
		return DEFAULT_BASE;
	}

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		try {

			this.averageSemantics = new double[state.breedthreads][];
			int semanticsLength = ((ISemanticProblem) state.evaluator.p_problem).getFitnessCases().size();
			for (int i = 0; i < this.averageSemantics.length; ++i) {
				this.averageSemantics[i] = new double[semanticsLength];
			}

			ParameterDatabase db = state.parameters;
			Parameter defaultBase = this.defaultBase();

			this.random = db.getBoolean(base.push(RANDOM), defaultBase.push(RANDOM), this.random);
			this.macromutateThreshold = db.getDouble(base.push(MACROMUTATE_THRESHOLD),
					defaultBase.push(MACROMUTATE_THRESHOLD), this.macromutateThreshold);
			this.macromutateThreshold *= this.macromutateThreshold;

			this.stat = (Statistics) state.statistics;

			this.library = ((EvoState) state).getLibrary();

		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public int produce(final int min, final int max, final int start, final int subpopulation, final Individual[] inds,
			final EvolutionState state, final int thread) {
		// how many individuals should we make?
		int n = typicalIndsProduced();
		if (n < min)
			n = min;
		if (n > max)
			n = max;

		// should we bother?
		if (!state.random[thread].nextBoolean(likelihood))
			return reproduce(n, start, subpopulation, inds, state, thread, true); // DO produce children from source --
																					// we've not done so already

		this.stat.crossoverOccurred();

		GPInitializer initializer = ((GPInitializer) state.initializer);

		for (int q = start; q < n + start; /* no increment */) // keep on going until we're filled up
		{
			// grab two individuals from our sources
			if (sources[0] == sources[1]) // grab from the same source
				sources[0].produce(2, 2, 0, subpopulation, parents, state, thread);
			else // grab from different sources
			{
				sources[0].produce(1, 1, 0, subpopulation, parents, state, thread);
				sources[1].produce(1, 1, 1, subpopulation, parents, state, thread);
			}

			// at this point, parents[] contains our two selected individuals

			// are our tree values valid?
			if (tree1 != TREE_UNFIXED && (tree1 < 0 || tree1 >= parents[0].trees.length))
				// uh oh
				state.output
						.fatal("GP Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual");
			if (tree2 != TREE_UNFIXED && (tree2 < 0 || tree2 >= parents[1].trees.length))
				// uh oh
				state.output
						.fatal("GP Crossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual");

			int t1 = 0;
			int t2 = 0;
			if (tree1 == TREE_UNFIXED || tree2 == TREE_UNFIXED) {
				do
				// pick random trees -- their GPTreeConstraints must be the same
				{
					if (tree1 == TREE_UNFIXED)
						if (parents[0].trees.length > 1)
							t1 = state.random[thread].nextInt(parents[0].trees.length);
						else
							t1 = 0;
					else
						t1 = tree1;

					if (tree2 == TREE_UNFIXED)
						if (parents[1].trees.length > 1)
							t2 = state.random[thread].nextInt(parents[1].trees.length);
						else
							t2 = 0;
					else
						t2 = tree2;
				} while (parents[0].trees[t1].constraints(initializer) != parents[1].trees[t2].constraints(initializer));
			} else {
				t1 = tree1;
				t2 = tree2;
				// make sure the constraints are okay
				if (parents[0].trees[t1].constraints(initializer) != parents[1].trees[t2].constraints(initializer)) // uh
																													// oh
					state.output
							.fatal("GP Crossover Pipeline's two tree choices are both specified by the user -- but their GPTreeConstraints are not the same");
			}

			// validity results...
			//boolean res1 = false;
			//boolean res2 = false;

			// prepare the nodeselectors
			nodeselect1.reset();
			nodeselect2.reset();

			// pick some nodes

			GPNode p1 = null;
			GPNode p2 = null;
			int maxHeight = 0;
			SimpleNodeBase<?> newSubtree = null;

			// pick a node in individual 1
			p1 = nodeselect1.pickNode(state, subpopulation, thread, parents[0], parents[0].trees[t1]);

			// pick a node in individual 2
			p2 = nodeselect2.pickNode(state, subpopulation, thread, parents[1], parents[1].trees[t2]);

			int maxHeight1 = this.maxDepth - p1.atDepth();
			int maxHeight2 = this.maxDepth - p2.atDepth();
			if (maxHeight1 < maxHeight2)
				maxHeight = maxHeight1;
			else
				maxHeight = maxHeight2;

			newSubtree = getAverageProgram(thread, p1, p2, state.random[thread], maxHeight);

			assert newSubtree.depth() <= maxHeight;
			assert verifyPoints(initializer, newSubtree, p1);
			assert verifyPoints(initializer, newSubtree, p2);

			// at this point, res1 AND res2 are valid, OR
			// either res1 OR res2 is valid and we ran out of tries, OR
			// neither res1 nor res2 is valid and we rand out of tries.
			// So now we will transfer to a tree which has res1 or res2
			// valid, otherwise it'll just get replicated. This is
			// compatible with both Koza and lil-gp.

			// at this point I could check to see if my sources were breeding
			// pipelines -- but I'm too lazy to write that code (it's a little
			// complicated) to just swap one individual over or both over,
			// -- it might still entail some copying. Perhaps in the future.
			// It would make things faster perhaps, not requiring all that
			// cloning.

			// Create some new individuals based on the old ones -- since
			// GPTree doesn't deep-clone, this should be just fine. Perhaps we
			// should change this to proto off of the main species prototype, but
			// we have to then copy so much stuff over; it's not worth it.

			GPIndividual j1 = (GPIndividual) (parents[0].lightClone());
			GPIndividual j2 = null;
			if (n - (q - start) >= 2 && !tossSecondParent)
				j2 = (GPIndividual) (parents[1].lightClone());

			// Fill in various tree information that didn't get filled in there
			j1.trees = new GPTree[parents[0].trees.length];
			if (n - (q - start) >= 2 && !tossSecondParent)
				j2.trees = new GPTree[parents[1].trees.length];

			// at this point, p1 or p2, or both, may be null.
			// If not, swap one in. Else just copy the parent.

			for (int x = 0; x < j1.trees.length; x++) {
				if (x == t1) // we've got a tree with a kicking cross position!
				{
					j1.trees[x] = (GPTree) (parents[0].trees[x].lightClone());
					j1.trees[x].owner = j1;

					j1.trees[x].child = parents[0].trees[x].child.cloneReplacing(newSubtree, p1);
					j1.trees[x].child.parent = j1.trees[x];
					j1.trees[x].child.argposition = 0;
					j1.evaluated = false;
				} // it's changed
				else {
					j1.trees[x] = (GPTree) (parents[0].trees[x].lightClone());
					j1.trees[x].owner = j1;
					j1.trees[x].child = (GPNode) (parents[0].trees[x].child.clone());
					j1.trees[x].child.parent = j1.trees[x];
					j1.trees[x].child.argposition = 0;
				}
			}

			if (n - (q - start) >= 2 && !tossSecondParent)
				for (int x = 0; x < j2.trees.length; x++) {
					if (x == t2) // we've got a tree with a kicking cross position!
					{
						j2.trees[x] = (GPTree) (parents[1].trees[x].lightClone());
						j2.trees[x].owner = j2;

						if (this.random) {
							newSubtree = this.getAverageProgram(thread, p1, p2, state.random[thread], maxHeight);
						} else {
							newSubtree = (SimpleNodeBase<?>) newSubtree.clone();
						}

						j2.trees[x].child = parents[1].trees[x].child.cloneReplacing(newSubtree, p2);
						j2.trees[x].child.parent = j2.trees[x];
						j2.trees[x].child.argposition = 0;
						j2.evaluated = false;
					} // it's changed
					else {
						j2.trees[x] = (GPTree) (parents[1].trees[x].lightClone());
						j2.trees[x].owner = j2;
						j2.trees[x].child = (GPNode) (parents[1].trees[x].child.clone());
						j2.trees[x].child.parent = j2.trees[x];
						j2.trees[x].child.argposition = 0;
					}
				}

			// add the individuals to the population
			inds[q] = j1;
			q++;
			if (q < n + start && !tossSecondParent) {
				inds[q] = j2;
				q++;
			}
		}
		return n;
	}

	protected SimpleNodeBase<?> getAverageProgram(final int thread, final GPNode p1, final GPNode p2,
			final MersenneTwisterFast random, int maxHeight) {

		GPNode newSubtree;

		if (!this.random) {
			// get semantics
			final SimpleNodeBase<?> p1Subtree = (SimpleNodeBase<?>) p1;
			final SimpleNodeBase<?> p2Subtree = (SimpleNodeBase<?>) p2;
			final ISemantics p1Semantics = p1Subtree.getSemantics();
			final ISemantics p2Semantics = p2Subtree.getSemantics();

			ISemantics averageSemantics = p1Semantics.getMidpointBetweenMeAnd(p2Semantics);
			double distance = p1Semantics.distanceTo(p2Semantics);

			if (distance > this.macromutateThreshold) {
				// get nearest program
				SearchResult<GPNode> kthProgram = this.library.getProgram(new DesiredSemanticsBase(averageSemantics), maxHeight);
				newSubtree = kthProgram.getProgram();
			} else {
				// do a random mutate
				newSubtree = this.library.getRandom(random, maxHeight);
				this.stat.macromutateOccurred();
			}
		} else {
			// pick random program
			newSubtree = this.library.getRandom(random, maxHeight);
		}

		// obtain statistics
		//this.stat.reportChosenProcedureNodeCount(newSubtree.numNodes(GPNode.NODESEARCH_ALL));

		// convert program to GPNode-based tree
		return (SimpleNodeBase<?>) newSubtree;
	}

	protected SimpleNodeBase<?>[] getAveragePrograms(final int thread, final GPNode p1, final GPNode p2,
			final MersenneTwisterFast random, int maxHeight) {

		GPNode[] newSubtree;

		if (!this.random) {
			// get semantics
			final SimpleNodeBase<?> p1Subtree = (SimpleNodeBase<?>) p1;
			final SimpleNodeBase<?> p2Subtree = (SimpleNodeBase<?>) p2;
			final ISemantics p1Semantics = p1Subtree.getSemantics();
			final ISemantics p2Semantics = p2Subtree.getSemantics();

			ISemantics averageSemantics = p1Semantics.getMidpointBetweenMeAnd(p2Semantics);
			double distance = p1Semantics.distanceTo(p2Semantics);

			if (distance > this.macromutateThreshold) {
				// get nearest program
				List<SearchResult<GPNode>> result = this.library.getPrograms(
						new DesiredSemanticsBase(averageSemantics), 2, maxHeight);

				newSubtree = new GPNode[result.size()];
				for (int i = 0; i < result.size(); ++i) {
					newSubtree[i] = result.get(i).getProgram();
				}
			} else {
				// do a random mutate
				newSubtree = new GPNode[2];
				newSubtree[0] = this.library.getRandom(random, maxHeight);
				newSubtree[1] = this.library.getRandom(random, maxHeight);
				this.stat.macromutateOccurred();
			}
		} else {
			// pick random program
			newSubtree = new GPNode[2];
			newSubtree[0] = this.library.getRandom(random, maxHeight);
			newSubtree[1] = this.library.getRandom(random, maxHeight);
		}

		// convert program to GPNode-based tree
		return new SimpleNodeBase[] { (SimpleNodeBase<?>) newSubtree[0], (SimpleNodeBase<?>) newSubtree[1] };
	}

}
