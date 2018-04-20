package ec.gp.semantic.library;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.gp.semantic.DesiredSemanticsBase;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.SimpleNodeBase;

public final class PopulationLibrary<SemType, TSemStore> extends LibraryBase<SemType, TSemStore> {

	private int libraryForGeneration = -1;
	private int size;
	private TreeMap<Integer, HashMap<ISemantics, SimpleNodeBase<SemType>>> library = new TreeMap<Integer, HashMap<ISemantics, SimpleNodeBase<SemType>>>();
	private int[] sizes;

	public PopulationLibrary(EvolutionState state, IDistanceToFactory distanceFactory,
			IConstantGenerator<SemType> constantGenerator) {
		super(state, distanceFactory, constantGenerator);
	}

	@Override
	protected TreeMap<Integer, HashMap<ISemantics, SimpleNodeBase<SemType>>> getLibrary() {
		if (this.libraryForGeneration == this.state.generation)
			return this.library; // the library is already built

		// otherwise build a library
		long sTime = System.currentTimeMillis();
		this.size = 0;
		this.library.clear();
		this.libraryForGeneration = this.state.generation;

		// access individuals
		Subpopulation[] subpops = this.state.population.subpops;
		for (int p = 0; p < subpops.length; ++p) {
			Individual[] individuals = subpops[p].individuals;
			for (int i = 0; i < individuals.length; ++i) {
				GPIndividual ind = (GPIndividual) individuals[i];
				for (int t = 0; t < ind.trees.length; ++t) {
					GPTree tree = ind.trees[t];
					this.addProgram((SimpleNodeBase<SemType>) tree.child);
				}
			}
		}

		int maxHeight = this.library.lastKey();
		int sum = 0;
		this.sizes = new int[maxHeight + 1];
		for (int h = 1; h <= maxHeight; ++h) {
			HashMap<ISemantics, SimpleNodeBase<SemType>> bucket = this.library.get(h);
			if (bucket != null)
				sum += bucket.size();
			this.sizes[h] = sum;
		}

		String message = String.format("Library for new generation built: %d distinct programs, %dms",
				this.size, System.currentTimeMillis() - sTime);
		this.state.output.message(message);

		return this.library;
	}

	private int addProgram(final SimpleNodeBase<SemType> node) {
		int myHeight = 0;

		if (node.children.length == 0) {
			myHeight = 1;
		} else {

			for (int i = 0; i < node.children.length; ++i) {
				int childHeight = this.addProgram((SimpleNodeBase<SemType>) node.children[i]);
				if (myHeight < childHeight)
					myHeight = childHeight;
			}

			// my height is height of may tallest child plus one
			myHeight += 1;
		}

		ISemantics semantics = node.getSemantics();
		SimpleNodeBase<SemType> progInLib = null;
		HashMap<ISemantics, SimpleNodeBase<SemType>> progInLibBucket = null;
		// find other program in library that have the same semantics
		for (HashMap<ISemantics, SimpleNodeBase<SemType>> bucket : this.library.values()) {
			progInLib = bucket.get(semantics);
			if (progInLib != null) {
				progInLibBucket = bucket;
				break; // we guarantee that library contains at most one program for particular semantics
			}
		}

		if (progInLib == null || progInLib.numNodes(GPNode.NODESEARCH_ALL) > node.numNodes(GPNode.NODESEARCH_ALL)) {
			// add program to the library only if:
			// * there is no other program with the same semantics, or
			// * the other program is bigger (we prefer small programs)

			HashMap<ISemantics, SimpleNodeBase<SemType>> bucket = this.library.get(myHeight);
			if (bucket == null) {
				bucket = new HashMap<ISemantics, SimpleNodeBase<SemType>>();
				this.library.put(myHeight, bucket);
			}

			if (progInLibBucket != null) {
				// remove old program
				progInLibBucket.remove(semantics);
			} else {
				// we add new program (not replaces the existing one), so the cardinality of library increases
				this.size += 1;
			}

			// add new program
			bucket.put(semantics, node);
		}

		return myHeight;
	}

	@Override
	public double calculateError(DesiredSemanticsBase<SemType> desiredSemantics, ISemantics semantics) {
		return this.distanceFactory.getDistanceToSet(this.state, desiredSemantics).getDistanceTo(
				(TSemStore) semantics.getValue());
	}

	@Override
	public int size() {
		this.getLibrary(); // it builds new library and calculates size if necessary
		return this.size;
	}

	@Override
	public int size(int maxHeight) {
		assert maxHeight > 0;

		this.getLibrary(); // it builds new library and calculates size if necessary

		if (maxHeight >= this.sizes.length)
			return this.size;

		return this.sizes[maxHeight];
	}
}
