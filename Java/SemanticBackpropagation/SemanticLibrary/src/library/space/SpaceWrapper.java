package library.space;

import java.util.ArrayList;
import java.util.List;

import library.distance.IDistanceTo;
import library.generator.TreeNode;

public class SpaceWrapper<TSemStore> {

	private final List<TreeNode> programs;
	private final ISpace<TSemStore> space;

	public SpaceWrapper(List<TreeNode> programs, ISpace<TSemStore> space) {
		assert programs != null;
		assert space != null;

		this.programs = programs;
		this.space = space;
	}

	public List<SearchResult<TreeNode>> getNearestPrograms(final TSemStore semantics, final int count,
			final int maxHeight) {
		List<SearchResult<Integer>> ids = this.space.getNearestPrograms(semantics, count, maxHeight);
		List<SearchResult<TreeNode>> programs = new ArrayList<SearchResult<TreeNode>>(ids.size());
		for (int i = 0; i < ids.size(); ++i) {
			SearchResult<Integer> id = ids.get(i);
			programs.add(new SearchResult<TreeNode>(this.programs.get(id.getProgram()), id.getError()));
		}
		return programs;
	}

	public List<SearchResult<TreeNode>> getNearestPrograms(final IDistanceTo<TSemStore> distanceMetter, final int count,
			final int maxHeight) {
		List<SearchResult<Integer>> ids = this.space.getNearestPrograms(distanceMetter, count, maxHeight);
		List<SearchResult<TreeNode>> programs = new ArrayList<SearchResult<TreeNode>>(ids.size());
		for (int i = 0; i < ids.size(); ++i) {
			SearchResult<Integer> id = ids.get(i);
			programs.add(new SearchResult<TreeNode>(this.programs.get(id.getProgram()), id.getError()));
		}
		return programs;
	}

	public SearchResult<TreeNode> getNearestProgram(final TSemStore semantics, final int maxHeight) {
		SearchResult<Integer> id = this.space.getNearestProgram(semantics, maxHeight);
		return new SearchResult<TreeNode>(this.programs.get(id.getProgram()), id.getError());
	}

	public SearchResult<TreeNode> getNearestProgram(final IDistanceTo<TSemStore> distanceMeter, final int maxHeight) {
		SearchResult<Integer> id = this.space.getNearestProgram(distanceMeter, maxHeight);
		return new SearchResult<TreeNode>(this.programs.get(id.getProgram()), id.getError());
	}
}
