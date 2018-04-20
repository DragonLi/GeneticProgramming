package library.semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import library.generator.TreeNode;

public class UniquenessFilter {

	/**
	 * Filters out the given collection of pairs of programs and semantics from redundant entries, according to the
	 * values of semantics.
	 * 
	 * @param semanticsGenerator
	 * @param outputPrograms
	 * @param outputSemantics
	 * @return The initial amount of programs in the collection
	 */
	public static long filter(final Iterable<ProgramSemanticsPair> semanticsGenerator,
			final List<TreeNode> outputPrograms, final List<VectorSemantics<?, ?>> outputSemantics) {

		final HashMap<VectorSemantics<?, ?>, TreeNode> unique = new HashMap<VectorSemantics<?, ?>, TreeNode>(0x2000000); //128MB/256MB of references (depending on architecture)
		long count = 0;

		//long sTime = System.currentTimeMillis();

		for (final ProgramSemanticsPair pair : semanticsGenerator) {
			++count;
			TreeNode prog = unique.get(pair.semantics);
			if (prog == null) {
				unique.put(pair.semantics, pair.program);
			} else {
				if (prog.getNodeCount() > pair.program.getNodeCount()) {
					// replace the existing program with new one
					unique.put(pair.semantics, pair.program);
				}
			}

			/*if (count % 1000000 == 0) {
				System.out.println(String.format("Speed: %d/s", count * 1000 / (System.currentTimeMillis() - sTime)));
			}*/
		}

		outputSemantics.addAll(unique.keySet());
		outputPrograms.addAll(unique.values());
		return count;
	}

	public static void filter(final List<TreeNode> programs, final List<VectorSemantics<?, ?>> semantics) {
		int count = programs.size();
		// It is expected that at least 1/2 of programs will be filtered out
		final HashMap<VectorSemantics<?, ?>, TreeNode> unique = new HashMap<VectorSemantics<?, ?>, TreeNode>(count >> 1);
		final HashMap<VectorSemantics<?, ?>, Integer> originalIndexes = new HashMap<VectorSemantics<?, ?>, Integer>(
				count >> 1);

		VectorSemantics<?, ?> newSem;
		TreeNode newProgram, existingProgram;
		int originalIndex;

		for (int i = 0; i < count; ++i) {
			newProgram = programs.get(i);
			newSem = semantics.get(i);

			existingProgram = unique.get(newSem);
			if (existingProgram != null) {
				if (existingProgram.getNodeCount() > newProgram.getNodeCount()) {
					// new program is smaller than existing one
					// replace existing program with new one
					// note that the above condition guarantees that source instructions
					// (one-node programs) will be preserved in the collection
					unique.put(newSem, newProgram);
					// mark old program to remove from programs and semantics collections
					// (newSem == oldSem)
					originalIndex = originalIndexes.get(newSem);
					programs.set(originalIndex, null);
					// update original index of given semantics
					originalIndexes.put(newSem, i);
				} else {
					// mark the new program to remove from programs and semantics collections
					programs.set(i, null);
				}
			} else {
				// the program of given semantics does not exist in the unique collection
				// add the program to the collection
				unique.put(newSem, newProgram);
				// remember index of inserted program
				originalIndexes.put(newSem, i);
			}
		}

		// remove programs marked for deletion
		for (int i = 0; i < count; ++i) {
			if (programs.get(i) == null) {
				programs.remove(i);
				semantics.remove(i);
				--i;
				--count;
			}
		}

		// compact collections
		if (programs instanceof ArrayList)
			((ArrayList<?>) programs).trimToSize();

		if (semantics instanceof ArrayList)
			((ArrayList<?>) semantics).trimToSize();
	}

	/**
	 * Removes non-unique programs by comparing its semantics using .equal() method.
	 * 
	 * @param programs
	 *            List of programs
	 * @param semantics
	 *            List of semantics
	 */
	@Deprecated
	public static void filter_old(final List<TreeNode> programs, final List<VectorSemantics<?, ?>> semantics) {
		int count = semantics.size();

		if (count != programs.size())
			throw new RuntimeException("Number of programs does not match number of semantics!");

		// TreeNode jProgram;
		VectorSemantics<Number, double[]> iSemantics, jSemantics;
		int iNodeCount, jNodeCount;

		for (int i = 1; i < count; ++i) {
			iSemantics = (VectorSemantics<Number, double[]>) semantics.get(i);
			iNodeCount = programs.get(i).getNodeCount();

			// check if semantics is equal to any other previously analyzed
			for (int j = 0; j < i; ++j) {
				jSemantics = (VectorSemantics<Number, double[]>) semantics.get(j);
				// first condition - heuristic check if two semantics may be equal, it speeds up execution by about 9%
				if (((double[]) iSemantics.vector)[0] == ((double[]) jSemantics.vector)[0]
						&& iSemantics.equals(jSemantics)) {
					// i is semantically equivalent to j
					// remove the larger instruction
					jNodeCount = programs.get(j).getNodeCount();
					if (jNodeCount <= iNodeCount && iNodeCount > 1) {
						// remove i
						programs.remove(i);
						semantics.remove(i);
					} else if (jNodeCount > 1) {
						programs.remove(j);
						semantics.remove(j);
					} else {
						// we cannot remove either i-th, nor j-th programs since both of them are original instructions
						// continue;
						break; // optimization, its is extremely unlikely to find another equal semantics
					}

					--i;
					--count;
					break;
				}
			}
		}
	}

}
