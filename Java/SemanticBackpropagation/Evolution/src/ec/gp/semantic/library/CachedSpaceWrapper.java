package ec.gp.semantic.library;

import java.util.ArrayList;
import java.util.List;

import library.distance.IDistanceTo;
import library.generator.TreeNode;
import library.space.ISpace;
import library.space.SearchResult;
import library.space.SpaceWrapper;

public final class CachedSpaceWrapper<TSemStore> extends SpaceWrapper<TSemStore> {

	private final Cache cache = new Cache();

	public CachedSpaceWrapper(List<TreeNode> programs, ISpace<TSemStore> space) {
		super(programs, space);
	}

	@Override
	public List<SearchResult<TreeNode>> getNearestPrograms(final TSemStore semantics, final int count,
			final int maxHeight) {
		// ask cache
		List<SearchResult<TreeNode>> programs = this.cache.get(semantics, maxHeight);

		if (programs == null || programs.size() < count) {
			programs = super.getNearestPrograms(semantics, count, maxHeight);
			this.cache.set(semantics, programs, maxHeight);
		}

		return programs;
	}

	@Override
	public List<SearchResult<TreeNode>> getNearestPrograms(final IDistanceTo<TSemStore> distanceMetter, final int count,
			final int maxHeight) {
		// ask cache
		List<SearchResult<TreeNode>> programs = this.cache.get(distanceMetter, maxHeight);

		if (programs == null || programs.size() < count) {
			programs = super.getNearestPrograms(distanceMetter, count, maxHeight);
			this.cache.set(distanceMetter, programs, maxHeight);
		}

		return programs;
	}

	@Override
	public SearchResult<TreeNode> getNearestProgram(TSemStore semantics, int maxHeight) {
		// ask cache
		List<SearchResult<TreeNode>> programs = this.cache.get(semantics, maxHeight);

		if (programs == null) {
			programs = new ArrayList<SearchResult<TreeNode>>(1);
			programs.add(super.getNearestProgram(semantics, maxHeight));
			this.cache.set(semantics, programs, maxHeight);
		}

		assert programs.size() > 0;

		return programs.get(0);
	}

	@Override
	public SearchResult<TreeNode> getNearestProgram(IDistanceTo<TSemStore> distanceMeter, int maxHeight) {
		// ask cache
		List<SearchResult<TreeNode>> programs = this.cache.get(distanceMeter, maxHeight);

		if (programs == null) {
			programs = new ArrayList<SearchResult<TreeNode>>(1);
			programs.add(super.getNearestProgram(distanceMeter, maxHeight));
			this.cache.set(distanceMeter, programs, maxHeight);
		}

		assert programs.size() > 0;

		return programs.get(0);
	}

}
