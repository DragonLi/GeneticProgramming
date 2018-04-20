package library.space;

import java.util.List;

import library.distance.HammingDistance;
import library.generator.TreeNode;
import library.semantics.VectorSemantics;

public final class HammingSpaceParallel extends GenericSpaceParallel<boolean[]> {

	public HammingSpaceParallel(final List<TreeNode> programs, final List<VectorSemantics<?, ?>> semantics) {
		super(programs, semantics);
	}

	@Override
	public List<SearchResult<Integer>> getNearestPrograms(boolean[] semantics, int count, final int maxHeight) {
		return this.getNearestPrograms(new HammingDistance(semantics), count, maxHeight);
	}

	@Override
	public SearchResult<Integer> getNearestProgram(boolean[] semantics, final int maxHeight) {
		return this.getNearestPrograms(new HammingDistance(semantics), 1, maxHeight).get(0);
	}

}
