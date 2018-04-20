package ec.gp.semantic.utils;

import java.util.IdentityHashMap;

import library.space.SearchResult;
import ec.EvolutionState;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.gp.semantic.DesiredSemanticsBase;
import ec.gp.semantic.EvoState;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.SemanticsBase;
import ec.gp.semantic.func.SimpleNodeBase;
import ec.gp.semantic.library.ILibrary;

public abstract class SemanticInverter<SemType> {

	private IdentityHashMap<GPNode, DesiredSemanticsBase<SemType>> desiredSemantics = new IdentityHashMap<GPNode, DesiredSemanticsBase<SemType>>();
	private ILibrary<SemType> library;

	//protected boolean allowOnlyOneInversion = false;

	public SemanticInverter(EvolutionState state) {
		this.library = (ILibrary<SemType>) ((EvoState) state).getLibrary();
		//this.allowOnlyOneInversion = state.parameters.getBoolean(
		//		new Parameter("SemanticInverter.allowOnlyOneInversion"), null, false);
	}

	public void clearCache() {
		desiredSemantics.clear();
	}

	protected abstract DesiredSemanticsBase<SemType> invertInstruction(final SimpleNodeBase<?> node,
			final int forChild, final DesiredSemanticsBase<SemType> desiredSemantics,
			final ISemantics[] childrenSemantics);

	private DesiredSemanticsBase<SemType> invert(final SimpleNodeBase<?> node,
			final DesiredSemanticsBase<SemType> desiredSemantics, final int forChild) {

		ISemantics[] childrenSemantics = null;
		if (node.children.length > 1) {
			childrenSemantics = new ISemantics[node.children.length - 1];
			for (int i = 0, j = 0; i < node.children.length; ++i) {
				if (i == forChild)
					continue;

				childrenSemantics[j++] = ((SimpleNodeBase<?>) node.children[i]).getSemantics();
			}
		}

		// calculate inversion
		return this.invertInstruction(node, forChild, desiredSemantics, childrenSemantics);

	}

	public void invertTree(final GPTree tree, ISemantics _desiredSemantics) {
		DesiredSemanticsBase<SemType> desiredSemantics = new DesiredSemanticsBase<SemType>(
				(SemanticsBase<SemType>) _desiredSemantics);
		this.desiredSemantics.put(tree.child, desiredSemantics);
		this.invertRecursive((SimpleNodeBase<?>) tree.child, desiredSemantics);
	}

	private void invertRecursive(final SimpleNodeBase<?> node, final DesiredSemanticsBase<SemType> desiredSemantics) {
		for (int i = 0; i < node.children.length; ++i) {
			DesiredSemanticsBase<SemType> inversions = invert(node, desiredSemantics, i);

			this.desiredSemantics.put(node.children[i], inversions);

			this.invertRecursive((SimpleNodeBase<?>) node.children[i], inversions);
		}
	}

	public class ErrorNodePair {
		public double currentNodeError = Double.MAX_VALUE;
		public double replacingNodeError = Double.MAX_VALUE;
		public SimpleNodeBase<?> currentNode;
		public SimpleNodeBase<?> replacingNode;

		@Override
		public String toString() {
			return String.format("Error: %.2f, current: %s, replacing: %s", replacingNodeError,
					currentNode.makeLispTree(),
					replacingNode.makeLispTree());
		}
	}

	public ErrorNodePair chooseNodeWithSmallestError(final SimpleNodeBase<?> node, final int maxHeight) {
		ErrorNodePair bestSoFar = new ErrorNodePair();
		chooseNodeWithSmallestError(node, bestSoFar, maxHeight);
		return bestSoFar;
	}

	protected void chooseNodeWithSmallestError(final SimpleNodeBase<?> node, final ErrorNodePair bestSoFar,
			final int maxHeight) {
		SearchResult<GPNode> closestProcedure;

		DesiredSemanticsBase<SemType> desiredSemantices = this.desiredSemantics.get(node);
		if (desiredSemantices != null) {
			closestProcedure = library.getProgram(desiredSemantices, maxHeight);

			double error = closestProcedure.getError();

			if (error < bestSoFar.replacingNodeError) {
				bestSoFar.currentNode = node;
				bestSoFar.currentNodeError = this.library.calculateError(desiredSemantices, node.getSemantics());
				bestSoFar.replacingNode = (SimpleNodeBase<?>) closestProcedure.getProgram();
				bestSoFar.replacingNodeError = error;
			}
		}

		if (maxHeight > 1) {
			for (final GPNode child : node.children) {
				chooseNodeWithSmallestError((SimpleNodeBase<?>) child, bestSoFar, maxHeight - 1);
			}
		}
	}

	public ErrorNodePair getBestReplacementFor(final SimpleNodeBase<?> node, final int maxHeight) {
		SearchResult<GPNode> closestProcedure;
		ErrorNodePair pair = new ErrorNodePair();

		DesiredSemanticsBase<SemType> desiredSemantices = this.desiredSemantics.get(node);
		if (desiredSemantices != null) {
			closestProcedure = library.getProgram(desiredSemantices, maxHeight);

			pair.currentNode = node;
			pair.currentNodeError = library.calculateError(desiredSemantices, node.getSemantics());
			pair.replacingNode = (SimpleNodeBase<?>) closestProcedure.getProgram();
			pair.replacingNodeError = closestProcedure.getError();
		}

		if (pair.currentNode == null)
			return null;
		return pair;
	}

	/**
	 * 
	 * @param toNode
	 *            Node to which the path will be inverted
	 * @param desiredRootSemantics
	 */
	/*public void invertPath(final SimpleNodeBase toNode, final ISemantics desiredRootSemantics) {
		this.invertPath(toNode, -1, desiredRootSemantics);
	}

	private void invertPath(final SimpleNodeBase node, final int childIndex, final ISemantics desiredRootSemantics) {
		// if we have cached desired semantics for node 'node', we must have cached all 
		// semantics on the path to the root (assuming they were inverted by invertPath method),
		// so stop inverting ancestors
		if (desiredSemantics.containsKey(node)) {
			return;
		}

		if (node.parent instanceof GPNode) {
			// go bottom-up
			invertPath((SimpleNodeBase) node.parent, node.argposition, desiredRootSemantics);
		} else {
			// we are the root, add desired semantics to cache
			desiredSemantics.put(node, new double[][] { desiredRootSemantics });
		}

		// go top-down
		// we are the root or the desired semantics is calculated for us

		if (childIndex < 0)
			return; // do not go deeper

		double[][] myInversions = this.desiredSemantics.get(node);
		if (myInversions == null)
			return; // cannot invert path, return

		List<double[]> childInversions = new ArrayList<double[]>();
		for (double[] inversion : myInversions) {
			childInversions.addAll(invert(node, inversion, childIndex));
		}
		desiredSemantics.put(node.children[childIndex], childInversions.toArray(new double[0][]));
	}*/

}
