package ec.gp.semantic.library;

import java.util.List;

import library.space.SearchResult;
import ec.gp.GPNode;
import ec.gp.semantic.DesiredSemanticsBase;
import ec.gp.semantic.ISemantics;
import ec.util.MersenneTwisterFast;

public interface ILibrary<SemType> {

	/**
	 * Returns a program that produces as close as possible semantics to the given one.
	 * 
	 * @param desiredSemantics
	 * @param maxHeight
	 *            Maximum height of returned program.
	 * @return
	 */
	SearchResult<GPNode> getProgram(DesiredSemanticsBase<SemType> desiredSemantics, int maxHeight);

	/**
	 * Returns a collection of at most k programs that are the closest to the given desired semantics.
	 * 
	 * @param desiredSemantics
	 * @param k
	 * @param maxHeight
	 *            Maximum height of returned program.
	 * @return
	 */
	List<SearchResult<GPNode>> getPrograms(DesiredSemanticsBase<SemType> desiredSemantics, int k, int maxHeight);

	/**
	 * Returns k-closest program in library to the given desired semantics.
	 * 
	 * @param semantics
	 * @param k
	 * @return
	 */
	SearchResult<GPNode> getKthProgram(DesiredSemanticsBase<SemType> semantics, int k, int maxHeight);

	/**
	 * 
	 * @param random
	 * @param maxHeight
	 *            Maximum height of returned program.
	 * @return
	 */
	GPNode getRandom(MersenneTwisterFast random, int maxHeight);

	double calculateError(DesiredSemanticsBase<SemType> desiredSemantics, ISemantics semantics);

	int size();

	int size(int maxDepth);
}
