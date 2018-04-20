package ec.gp.semantic.library;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import library.generator.TreeNode;
import library.space.SearchResult;

final class Cache
{

	private final Map<Object, SoftReference<HashMap<Integer, List<SearchResult<TreeNode>>>>> cache = new HashMap<Object, SoftReference<HashMap<Integer, List<SearchResult<TreeNode>>>>>();

	// private long hits = 0;
	// private long misses = 0;

	public List<SearchResult<TreeNode>> get(final Object query, final Integer maxHeight)
	{
		SoftReference<HashMap<Integer, List<SearchResult<TreeNode>>>> ref = this.cache.get(query);
		HashMap<Integer, List<SearchResult<TreeNode>>> value = null;

		if (ref != null)
		{
			value = ref.get();
			if (value == null)
				this.cache.remove(query);
			else
				return value.get(maxHeight);
		}

		return null;

		/*
		 * List<SearchResult<TreeNode>> entry = this.cache.get(query);
		 * 
		 * if (entry != null) { ++hits; System.out.println(String.format(
		 * "Cache hit! Hit ratio: %.3f Cache size: %d", (double) hits / (hits +
		 * misses), cache.size())); } else { ++misses; }
		 * 
		 * return entry;
		 */
	}

	public void set(final Object query, final List<SearchResult<TreeNode>> result, final Integer maxHeight)
	{
		assert query != null;

		SoftReference<HashMap<Integer, List<SearchResult<TreeNode>>>> ref = this.cache.get(query);
		HashMap<Integer, List<SearchResult<TreeNode>>> map = null;

		if (ref != null) {
			map = ref.get();
		}

		if (map == null) {
			map = new HashMap<Integer, List<SearchResult<TreeNode>>>();
			this.cache.put(query, new SoftReference<HashMap<Integer, List<SearchResult<TreeNode>>>>(map));
		}

		map.put(maxHeight, result);
	}
}
