package ec.gp.semantic.statistics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import library.INamedElement;
import ec.EvolutionState;
import ec.util.Parameter;

public class CrossoverStatistics extends Statistics {

	private static Parameter DEFAULT_BASE = new Parameter("CrossoverStatistics");

	private EvolutionState state = null;

	private class Tuple {
		public int crossoverLevel = -1;
		/**
		 * Key - level in tree, Value - is geometric
		 */
		public final TreeMap<Integer, Boolean> isGeometric = new TreeMap<Integer, Boolean>();
	}

	private class Summary {
		public int crossoverLevel = -1;
		/**
		 * Key - level in tree, Value - no. of geometric crossovers.
		 */
		public final TreeMap<Integer, Integer> geometric = new TreeMap<Integer, Integer>();
		/**
		 * Key - level in tree, Value - total no. of crossovers
		 */
		public final TreeMap<Integer, Integer> totals = new TreeMap<Integer, Integer>();
	}

	/**
	 * Key - number of generation, value - collection of crossovers.
	 */
	private TreeMap<Integer, ArrayList<Tuple>> crossoverActs = new TreeMap<Integer, ArrayList<Tuple>>();

	/*
	/**
	 * Key - number of generation, Value.Key - level of crossover point in tree, Value.Value - number of crossovers,
	 * that are geometric.
	 * /
	private SortedMap<Integer, SortedMap<Integer, Integer>> geometricPropagated = new TreeMap<Integer, SortedMap<Integer, Integer>>();

	/**
	 * Key - number of generation, Value.Key - level of crossover point in tree, Value.Value - total number of
	 * crossovers.
	 * /
	private SortedMap<Integer, SortedMap<Integer, Integer>> totalCrossovers = new TreeMap<Integer, SortedMap<Integer, Integer>>();

	private void increment(SortedMap<Integer, SortedMap<Integer, Integer>> map, int generation, int level) {
		SortedMap<Integer, Integer> map2 = map.get(generation);
		if (map2 == null) {
			map2 = new TreeMap<Integer, Integer>(new Comparator<Integer>() {
				@Override
				public int compare(Integer arg0, Integer arg1) {
					return arg1 - arg0; //reverse order
				}
			});

			map.put(generation, map2);
		}

		Integer value = map2.get(level);
		if (value == null) {
			value = 1;
		} else {
			++value;
		}

		map2.put(level, value);
	}

	private int get(SortedMap<Integer, SortedMap<Integer, Integer>> map, int generation, int level) {
		SortedMap<Integer, Integer> map2 = map.get(generation);
		if (map2 == null)
			return 0;

		Integer value = map2.get(level);
		if (value == null)
			return 0;
		return value;
	}
	*/

	/**
	 * 
	 * @param crossoverLevel
	 *            Level of crossover point. One-based.
	 * @param level
	 *            Level on which we gather info about geometric changes. One-based.
	 * @param isGeometric
	 *            Key - level in tree, Value - is geometric change propagated?
	 */
	public void crossoverOccurred(int crossoverLevel, int level, boolean isGeometric) {
		Tuple tuple = new Tuple();
		tuple.crossoverLevel = crossoverLevel;
		tuple.isGeometric.put(level, isGeometric);
		ArrayList<Tuple> list = crossoverActs.get(state.generation);
		if (list == null) {
			list = new ArrayList<Tuple>();
			crossoverActs.put(state.generation, list);
		}
		list.add(tuple);
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);

		this.state = state;
	}

	@Override
	public void finalStatistics(EvolutionState state, int result) {
		super.finalStatistics(state, result);
		this.writePropagationHistogram();
	}

	private void writePropagationHistogram() {
		OutputStreamWriter writer = null;

		try {
			String problemName = ((INamedElement)state.evaluator.p_problem).getName();
			File output = null;
			for (int i = 0; output == null; ++i) {
				output = new File(String.format("%s%cCrossoverStatistics_%s.%d.csv", outputDirectory,
						File.separatorChar, problemName, i));

				if (!output.createNewFile()) {
					output = null;
				}
			}

			writer = new OutputStreamWriter(new FileOutputStream(output));
			writer.write("Propagation of geometic changes\n");
			writer.write("Problem:;" + problemName + "\n");

			// Key - crossover level, Value - average
			TreeMap<Integer, Summary> summaries = new TreeMap<Integer, Summary>();
			int totalCrossoverActs = 0;

			for (ArrayList<Tuple> tupleList : crossoverActs.values()) {
				totalCrossoverActs += tupleList.size();

				for (Tuple tuple : tupleList) {
					Summary summary = summaries.get(tuple.crossoverLevel);
					if (summary == null) {
						summary = new Summary();
						summary.crossoverLevel = tuple.crossoverLevel;
						summaries.put(tuple.crossoverLevel, summary);
					}

					for (Map.Entry<Integer, Boolean> pair : tuple.isGeometric.entrySet()) {
						Integer no;
						if (pair.getValue()) {
							no = summary.geometric.get(pair.getKey());
							if (no == null)
								no = 1;
							else
								++no;
							summary.geometric.put(pair.getKey(), no);
						}

						no = summary.totals.get(pair.getKey());
						if (no == null)
							no = 1;
						else
							++no;
						summary.totals.put(pair.getKey(), no);
					}

				}
			}

			printHistogramInternal(writer, summaries, false);
			printHistogramInternal(writer, summaries, true);

			writer.write(String.format("Total number of crossovers:;%d\n", totalCrossoverActs));

		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}

	private void printHistogramInternal(OutputStreamWriter writer, TreeMap<Integer, Summary> summaries, boolean totals)
			throws IOException {
		int expectedXoverLevel = 1;
		int expectedGeoLevel;
		int maxLevel = 1;

		if (totals)
			writer.write("Totals\n");
		else
			writer.write("Number of geometric crossovers\n");

		writer.write("Depth of crossover\n");

		for (Map.Entry<Integer, Summary> entry : summaries.entrySet()) {
			int xoverLevel = entry.getKey();
			for (int i = expectedXoverLevel; i < xoverLevel; ++i) {
				writer.write(String.format("%d\n", i)); // fill gaps
			}

			writer.write(String.format("%d;", xoverLevel));
			Summary summary = entry.getValue();

			Set<Entry<Integer, Integer>> lvlEntries = summary.geometric.entrySet();
			if(totals)
				lvlEntries = summary.totals.entrySet();
			
			expectedGeoLevel = 1;
			for (Map.Entry<Integer, Integer> lvlEntry : lvlEntries) {
				int level = lvlEntry.getKey();
				for (int i = expectedGeoLevel; i < level; ++i) {
					if(totals)
						writer.write(String.format("%d", summary.totals.get(level)));
					writer.write(';'); // fill gaps
				}

				if (totals)
					writer.write(String.format("%d;", summary.totals.get(level)));
				else
					writer.write(String.format("%d;", lvlEntry.getValue()));

				if (level > maxLevel)
					maxLevel = level;
				expectedGeoLevel = level + 1;
			}
			writer.write('\n');

			expectedXoverLevel = xoverLevel + 1;
		}

		writer.write("Depth of tree:;");
		for (int i = 1; i <= maxLevel; ++i) {
			writer.write(String.format("%d;", i));
		}
		writer.write('\n');
	}
}
