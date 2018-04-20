package ec.gp.canonic.breed;

import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.gp.koza.CrossoverPipeline;
import ec.gp.semantic.utils.Pair;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;

/**
 * One-point crossover by Langdon and Poli (aka. Homologous canonic crossover).
 * 
 * @author Tomasz Pawlak
 * 
 */
public class HGP extends CrossoverPipeline {

	private List<GPNode>[] validFromT1;
	private List<GPNode>[] validLeafsFromT1;
	private List<GPNode>[] validFromT2;
	private List<GPNode>[] validLeafsFromT2;

	protected double leafProbability = 0.1;
	protected double intermediateProbability = 0.9;

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		this.validFromT1 = new List[state.breedthreads];
		this.validLeafsFromT1 = new List[state.breedthreads];
		this.validFromT2 = new List[state.breedthreads];
		this.validLeafsFromT2 = new List[state.breedthreads];

		for (int i = 0; i < state.breedthreads; ++i) {
			this.validFromT1[i] = new ArrayList<GPNode>();
			this.validLeafsFromT1[i] = new ArrayList<GPNode>();
			this.validFromT2[i] = new ArrayList<GPNode>();
			this.validLeafsFromT2[i] = new ArrayList<GPNode>();
		}
	}

	@Override
	public int produce(final int min, final int max, final int start, final int subpopulation, final Individual[] inds,
			final EvolutionState state, final int thread)

	{
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
			boolean res1 = false;
			boolean res2 = false;

			// prepare the nodeselectors
			//nodeselect1.reset();
			//nodeselect2.reset();

			// pick some nodes

			GPNode p1 = null;
			GPNode p2 = null;

			for (int x = 0; x < numTries; x++) {
				// pick nodes from both parents
				Pair<GPNode, GPNode> pair = this.getHomologousCrossoverPoints_varProb(parents[0].trees[t1].child,
						parents[1].trees[t2].child, state.random[thread], thread);
				p1 = pair.value1;
				p2 = pair.value2;
				
				// pick a node in individual 1
				// p1 = nodeselect1.pickNode(state, subpopulation, thread, parents[0], parents[0].trees[t1]);

				// pick a node in individual 2
				// p2 = nodeselect2.pickNode(state, subpopulation, thread, parents[1], parents[1].trees[t2]);

				// check for depth and swap-compatibility limits
				res1 = verifyPoints(initializer, p2, p1); // p2 can fill p1's spot -- order is important!
				if (n - (q - start) < 2 || tossSecondParent)
					res2 = true;
				else
					res2 = verifyPoints(initializer, p1, p2); // p1 can fill p2's spot -- order is important!

				// did we get something that had both nodes verified?
				// we reject if EITHER of them is invalid. This is what lil-gp does.
				// Koza only has numTries set to 1, so it's compatible as well.
				if (res1 && res2)
					break;
			}

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
				if (x == t1 && res1) // we've got a tree with a kicking cross position!
				{
					j1.trees[x] = (GPTree) (parents[0].trees[x].lightClone());
					j1.trees[x].owner = j1;
					j1.trees[x].child = parents[0].trees[x].child.cloneReplacing(p2, p1);
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
					if (x == t2 && res2) // we've got a tree with a kicking cross position!
					{
						j2.trees[x] = (GPTree) (parents[1].trees[x].lightClone());
						j2.trees[x].owner = j2;
						j2.trees[x].child = parents[1].trees[x].child.cloneReplacing(p1, p2);
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

	protected Pair<GPNode, GPNode> getHomologousCrossoverPoints(final GPNode root1, final GPNode root2,
			final MersenneTwisterFast random, final int thread) {

		final List<GPNode> validFromT1 = this.validFromT1[thread];
		final List<GPNode> validFromT2 = this.validFromT2[thread];

		this.buildHomologousLists(validFromT1, validFromT2, root1, root2);

		int index = 0;
		int size = validFromT1.size();

		// choose whether to select from leafs or intermediate nodes
		if (size > 1) {
			// do not select root
			index = 1 + random.nextInt(size - 1);
		}// else -> select 0

		Pair<GPNode, GPNode> result = new Pair<GPNode, GPNode>(validFromT1.get(index), validFromT2.get(index));

		validFromT1.clear();
		validFromT2.clear();

		return result;
	}

	private void buildHomologousLists(final List<GPNode> nodes1, final List<GPNode> nodes2, final GPNode node1,
			final GPNode node2) {

		nodes1.add(node1);
		nodes2.add(node2);

		if (node1.children.length == node2.children.length) {
			for (int i = 0; i < node1.children.length; ++i) {
				this.buildHomologousLists(nodes1, nodes2, node1.children[i], node2.children[i]);
			}
		}
	}

	protected Pair<GPNode, GPNode> getHomologousCrossoverPoints_varProb(final GPNode root1, final GPNode root2,
			final MersenneTwisterFast random, final int thread) {

		final List<GPNode> validFromT1 = this.validFromT1[thread];
		final List<GPNode> validLeafsFromT1 = this.validLeafsFromT1[thread];
		final List<GPNode> validFromT2 = this.validFromT2[thread];
		final List<GPNode> validLeafsFromT2 = this.validLeafsFromT2[thread];

		this.buildHomologousLists_varProb(validFromT1, validLeafsFromT1, validFromT2, validLeafsFromT2, root1, root2);

		Pair<GPNode, GPNode> result;
		int index = 0;
		int intermediateSize = validFromT1.size();
		int leafSize = validLeafsFromT1.size();

		// choose whether to select from leafs or intermediate nodes
		if ((intermediateSize > 0 && random.nextBoolean(this.intermediateProbability)) || leafSize == 0) {
			if (intermediateSize > 1) {
				// do not select root
				index = 1 + random.nextInt(intermediateSize - 1);
			} else {
				// select 0
				assert intermediateSize == 1;
			}

			result = new Pair<GPNode, GPNode>(validFromT1.get(index), validFromT2.get(index));
		} else {
			// if validFromT1 is empty, then validLeafsFromT1 must contain only one node -> root
			// if validFromT1 is not empty, then validLeafsFromT1 does not contain root
			if (intermediateSize != 0) {
				// select any node
				index = random.nextInt(leafSize);
			} else {
				// select 0
				assert leafSize == 1;
			}

			result = new Pair<GPNode, GPNode>(validLeafsFromT1.get(index), validLeafsFromT2.get(index));
		}

		validFromT1.clear();
		validLeafsFromT1.clear();
		validFromT2.clear();
		validLeafsFromT2.clear();

		return result;
	}

	private void buildHomologousLists_varProb(final List<GPNode> nodes1, final List<GPNode> leafs1,
			final List<GPNode> nodes2, final List<GPNode> leafs2, final GPNode node1, final GPNode node2) {

		if (node1.children.length == 0 && node2.children.length == 0) {
			leafs1.add(node1);
			leafs2.add(node2);
		} else {
			nodes1.add(node1);
			nodes2.add(node2);

			if (node1.children.length == node2.children.length) {
				for (int i = 0; i < node1.children.length; ++i) {
					this.buildHomologousLists_varProb(nodes1, leafs1, nodes2, leafs2, node1.children[i],
							node2.children[i]);
				}
			}
		}
	}

}
