package library.space;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import library.distance.IDistanceTo;
import library.generator.TreeNode;
import library.semantics.VectorSemantics;

public abstract class GenericSpaceParallel<TStore> implements ISpace<TStore> {

	private final static int CHUNK_SIZE = 10000;

	/**
	 * Key - program height, Value.Key - id of program, Value.Value - list of semantics
	 */
	private final NavigableMap<Integer, TreeMap<Integer, TStore>> semantics;
	private final TreeMap<Integer, HashMap<Integer, Double>> distances;

	private final int threadNum;
	private final ExecutorService pool;
	private final TreeMap<Integer, List<Future<?>>> futures;

	public GenericSpaceParallel(final List<TreeNode> programs, final List<VectorSemantics<?, ?>> semantics) {
		assert programs.size() == semantics.size();

		TreeMap<Integer, TStore> listAtHeight;
		VectorSemantics<?, ?> sem;
		TreeNode prog;
		Integer height;

		this.semantics = new TreeMap<Integer, TreeMap<Integer, TStore>>().descendingMap();
		this.distances = new TreeMap<Integer, HashMap<Integer, Double>>();

		for (Integer i = 0; i < semantics.size(); ++i) {
			sem = semantics.get(i);
			prog = programs.get(i);
			height = prog.getHeight();

			listAtHeight = this.semantics.get(height);
			if (listAtHeight == null) {
				listAtHeight = new TreeMap<Integer, TStore>();
				this.semantics.put(height, listAtHeight);
				this.distances.put(height, new HashMap<Integer, Double>());
			}

			listAtHeight.put(i, (TStore) sem.getSemantics());
		}

		for (Entry<Integer, TreeMap<Integer, TStore>> entry : this.semantics.entrySet()) {
			System.out.println(String.format("Bucket %2d size: %3d", entry.getKey(), entry.getValue().size()));
		}

		this.threadNum = Math.min(2, Runtime.getRuntime().availableProcessors());
		this.pool = Executors.newFixedThreadPool(threadNum);
		this.futures = new TreeMap<Integer, List<Future<?>>>();
	}

	@Override
	public List<SearchResult<Integer>> getNearestPrograms(final IDistanceTo<TStore> distanceMeter, final int count,
			final int maxHeight) {

		//long calculateTime;
		//long sTime = System.nanoTime();

		Future<?> future;
		this.futures.clear();

		for (Entry<Integer, TreeMap<Integer, TStore>> entry : this.semantics.entrySet()) {
			if (entry.getKey() > maxHeight)
				continue; // the this.semantics map is in descending order

			final Map<Integer, Double> distances = Collections.synchronizedMap(this.distances.get(entry.getKey()));
			TreeMap<Integer, TStore> semanticsAtHeight = entry.getValue();
			int maxIndex = semanticsAtHeight.lastKey();
			List<Future<?>> futureList = new ArrayList<Future<?>>();

			for (int chunkStart = 0; chunkStart <= maxIndex; chunkStart += CHUNK_SIZE) {

				final SortedMap<Integer, TStore> semantics = semanticsAtHeight.subMap(chunkStart, chunkStart
						+ CHUNK_SIZE);

				future = this.pool.submit(new Callable<Object>() {
					@Override
					public final Object call() {
						double distance;

						for (final Entry<Integer, TStore> sem : semantics.entrySet()) {
							distance = distanceMeter.getDistanceTo(sem.getValue());
							distances.put(sem.getKey(), distance);
						}

						return null;
					}
				});
				futureList.add(future);
			}

			this.futures.put(entry.getKey(), futureList);
		}

		ArrayList<SearchResult<Integer>> indexes = new ArrayList<SearchResult<Integer>>(count);
		// it will be replaced by other entries (including 0 if smaller)
		// if all distances are infinities or NaNs, then it probably remains in the list, however
		// the distance inserted here remains true
		indexes.add(new SearchResult<Integer>(0, Double.POSITIVE_INFINITY));

		try {

			for (Entry<Integer, HashMap<Integer, Double>> entry : this.distances.entrySet()) {
				if (entry.getKey() > maxHeight)
					break;

				// wait for computation to complete
				List<Future<?>> futureList = this.futures.get(entry.getKey());
				for (Future<?> _future : futureList)
					_future.get();

				for (Entry<Integer, Double> distanceEntry : entry.getValue().entrySet()) {
					double distance = distanceEntry.getValue();

					if ((indexes.size() >= count && distance > indexes.get(count - 1).getError())
							|| Double.isNaN(distance)
							|| Double.isInfinite(distance))
						continue;

					int pos = binSearch(indexes, distance);

					if (pos <= indexes.size() && indexes.size() < count) {
						indexes.add(null); //add dummy element, it will be overwritten by the below loop
					}

					for (int j = indexes.size() - 1; j > pos; --j) {
						indexes.set(j, indexes.get(j - 1));
					}

					indexes.set(pos, new SearchResult<Integer>(distanceEntry.getKey(), distance));
				}

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		//System.out.println("Search time = " + (System.nanoTime() - sTime) * 1E-6 + "ms, calculate time = " + (calculateTime * 1E-6) + "ms");
		return indexes;
	}

	private static int binSearch(final ArrayList<SearchResult<Integer>> list, final double key) {
		int l = 0;
		int r = list.size() - 1;
		int s = 0;
		do {
			s = (l + r) >> 1;
			if (key < list.get(s).getError()) {
				r = s - 1;
			} else if (key > list.get(s).getError()) {
				l = ++s; //this sets s to right value in the last loop
			} else {
				return s;
			}

		} while (l <= r);

		return s;
	}

	@Override
	public SearchResult<Integer> getNearestProgram(final IDistanceTo<TStore> distanceMeter, final int maxHeight) {
		return this.getNearestPrograms(distanceMeter, 1, maxHeight).get(0);
	}
}
