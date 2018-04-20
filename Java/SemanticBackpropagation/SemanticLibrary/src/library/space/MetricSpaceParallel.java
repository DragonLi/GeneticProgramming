package library.space;

import java.util.List;

import library.distance.EuclideanDistance;
import library.generator.TreeNode;
import library.semantics.VectorSemantics;

public final class MetricSpaceParallel extends GenericSpaceParallel<double[]> {

	public MetricSpaceParallel(final List<TreeNode> programs, final List<VectorSemantics<?, ?>> semantics) {
		super(programs, semantics);
	}

	@Override
	public List<SearchResult<Integer>> getNearestPrograms(final double[] curSemantics, final int count,
			final int maxHeight) {
		return this.getNearestPrograms(new EuclideanDistance(curSemantics), count, maxHeight);
	}

	@Override
	public SearchResult<Integer> getNearestProgram(double[] semantics, final int maxHeight) {
		return getNearestPrograms(semantics, 1, maxHeight).get(0);
	}

}
