package ec.gp.semantic.library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import library.distance.IDistanceTo;
import library.space.SearchResult;
import ec.EvolutionState;
import ec.gp.GPNode;
import ec.gp.semantic.DesiredSemanticsBase;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.SimpleNodeBase;
import ec.util.MersenneTwisterFast;

public abstract class LibraryBase<SemType, TSemStore> implements ILibrary<SemType> {

	protected final EvolutionState state;
	protected final IDistanceToFactory distanceFactory;
	protected final IConstantGenerator constantGenerator;

	LibraryBase(EvolutionState state, IDistanceToFactory distanceFactory, IConstantGenerator<SemType> constantGenerator) {
		this.state = state;
		this.distanceFactory = distanceFactory;
		this.constantGenerator = constantGenerator;
	}

	/**
	 * 
	 * @return Sorted map of HashMaps. Each element of the sorted map refers to set of programs having particular depth.
	 *         HashMap's key is program semantics, value is particular program.
	 */
	protected abstract TreeMap<Integer, HashMap<ISemantics, SimpleNodeBase<SemType>>> getLibrary();

	@Override
	public SearchResult<GPNode> getProgram(DesiredSemanticsBase<SemType> desiredSemantics, int maxHeight) {
		return this.getPrograms(desiredSemantics, 1, maxHeight).get(0);
	}

	protected ArrayList<SearchResult<GPNode>> getProgramsInternal(final DesiredSemanticsBase<SemType> desiredSemantics,
			final int k, final int maxHeight) {
		assert maxHeight > 0;

		int insertPos;
		double distance;
		TSemStore currentSemantics;
		SearchResult<GPNode> currentResult;
		HashMap<ISemantics, SimpleNodeBase<SemType>> bucket;

		TreeMap<Integer, HashMap<ISemantics, SimpleNodeBase<SemType>>> library = this.getLibrary();
		IDistanceTo distanceCalculator = this.distanceFactory.getDistanceToSet(this.state, desiredSemantics);

		ArrayList<SearchResult<GPNode>> results = new ArrayList<SearchResult<GPNode>>(k);
		results.add(this.constantGenerator.getPerfectConstant(desiredSemantics));

		for (Entry<Integer, HashMap<ISemantics, SimpleNodeBase<SemType>>> _bucket : library.entrySet()) {
			if (_bucket.getKey() > maxHeight)
				break; // NavigableMap is sorted!

			bucket = _bucket.getValue();

			for (Entry<ISemantics, SimpleNodeBase<SemType>> entry : bucket.entrySet()) {
				currentSemantics = (TSemStore) entry.getKey().getValue();
				distance = distanceCalculator.getDistanceTo(currentSemantics);
				currentResult = new SearchResult<GPNode>(entry.getValue(), distance);

				insertPos = Collections.binarySearch(results, currentResult);
				if (insertPos < 0)
					insertPos = -insertPos - 1;

				if (insertPos >= k)
					continue;

				if (k > results.size()) {
					results.add(null); // just allocate space, we will override this value below
				}

				for (int i = results.size() - 1; i > insertPos; --i) {
					results.set(i, results.get(i - 1));
				}

				results.set(insertPos, currentResult);

				assert results.get(results.size() - 1) != null;
			}
		}

		return results;
	}

	@Override
	public List<SearchResult<GPNode>> getPrograms(final DesiredSemanticsBase<SemType> desiredSemantics, final int k,
			final int maxHeight) {
		SearchResult<GPNode> currentResult;
		ArrayList<SearchResult<GPNode>> results = this.getProgramsInternal(desiredSemantics, k, maxHeight);

		//make a copy of returned programs (to not refer the same objects in different programs)
		for (int i = 0; i < results.size(); ++i) {
			currentResult = results.get(i);
			SimpleNodeBase<TSemStore> copy = (SimpleNodeBase<TSemStore>) currentResult.getProgram().clone();
			copy.resetSemantics();
			currentResult = new SearchResult(copy, currentResult.getError());
			results.set(i, currentResult);
		}

		return results;
	}

	@Override
	public SearchResult<GPNode> getKthProgram(final DesiredSemanticsBase<SemType> desiredSemantics, int k,
			final int maxHeight) {
		SearchResult<GPNode> currentResult;
		ArrayList<SearchResult<GPNode>> results = this.getProgramsInternal(desiredSemantics, k + 1, maxHeight);

		//make a copy of returned program (to not refer the same objects in different programs)
		currentResult = results.get(results.size() - 1);
		SimpleNodeBase<TSemStore> copy = (SimpleNodeBase<TSemStore>) currentResult.getProgram().clone();
		copy.resetSemantics();
		return new SearchResult(copy, currentResult.getError());
	}

	@Override
	public GPNode getRandom(final MersenneTwisterFast random, final int maxHeight) {
		TreeMap<Integer, HashMap<ISemantics, SimpleNodeBase<SemType>>> library = this.getLibrary();
		int r = random.nextInt(this.size(maxHeight));
		for (HashMap<ISemantics, SimpleNodeBase<SemType>> bucket : library.values()) {
			for (SimpleNodeBase<SemType> program : bucket.values()) {
				if (r-- == 0) {
					return program;
				}
			}
		}

		assert false;
		return null;
	}
}
