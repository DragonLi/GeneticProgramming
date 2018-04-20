package library.space;

import java.util.List;

import library.distance.IDistanceTo;

public interface ISpace<TSemStore> {

	/**
	 * Returns list of ids of the closest procedures to the given semantics.
	 * 
	 * @param semantics
	 *            The semantics to find the closest neighbors for.
	 * @param count
	 *            Number of neighbors to find.
	 * @return List of ids of neighbors.
	 */
	List<SearchResult<Integer>> getNearestPrograms(final TSemStore semantics, final int count, final int maxHeight);

	/**
	 * Returns list of ids of the closest procedures to the given semantics, using given comparator.
	 * 
	 * @param distanceMeter
	 *            Distance meter, that compares the distance of given semantics to the target (implicitly stated,
	 *            stitched in the meter).
	 * @param count
	 *            Number of neighbors to find.
	 * @return
	 */
	List<SearchResult<Integer>> getNearestPrograms(final IDistanceTo<TSemStore> distanceMeter, final int count, final int maxHeight);

	/**
	 * 
	 * @param semantics
	 * @return id of the closest procedure to the given semantics.
	 */
	SearchResult<Integer> getNearestProgram(final TSemStore semantics, final int maxHeight);

	/**
	 * Returns an id of the closest procedure to the given semantics, using given comparator.
	 * 
	 * @param distanceMeter
	 *            Distance meter, that compares the distance of given semantics to the target (implicitly stated,
	 *            stitched in the meter).
	 * @return
	 */
	SearchResult<Integer> getNearestProgram(final IDistanceTo<TSemStore> distanceMeter, final int maxHeight);

}