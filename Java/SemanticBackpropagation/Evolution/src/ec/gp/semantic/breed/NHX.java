package ec.gp.semantic.breed;

import ec.EvolutionState;
import ec.gp.GPNode;
import ec.gp.breed.GPBreedDefaults;
import ec.gp.semantic.DesiredSemanticsBase;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.SimpleNodeBase;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import library.space.SearchResult;

public class NHX extends NonRandomizedNHX {

	private static final Parameter DEFAULT_BASE = GPBreedDefaults.base().push("NHX");
	protected static final String NEIGHBOR_COUNT = "neighborCount";
	protected int neighborCount = 8;

	@Override
	public Parameter defaultBase() {
		return DEFAULT_BASE;
	}

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		this.neighborCount = state.parameters.getInt(base.push(NEIGHBOR_COUNT), defaultBase().push(NEIGHBOR_COUNT),
				this.neighborCount);
	}

	@Override
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

			if (distance >= this.macromutateThreshold) {
				// get random program from neighborhood
				SearchResult<GPNode> kthProgram = this.library.getKthProgram(new DesiredSemanticsBase(averageSemantics),
						random.nextInt(this.neighborCount), maxHeight);
				newSubtree = kthProgram.getProgram();
			} else {
				// do a random mutate
				newSubtree = this.library.getRandom(random, maxHeight);
				// this.stat.macromutateOccurred();
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

}
