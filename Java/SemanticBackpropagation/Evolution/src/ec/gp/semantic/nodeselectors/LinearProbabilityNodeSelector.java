package ec.gp.semantic.nodeselectors;

import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPNodeSelector;
import ec.gp.GPTree;
import ec.util.Parameter;

public class LinearProbabilityNodeSelector implements GPNodeSelector {

	private final static Parameter DEFAULT_BASE = new Parameter(
			"ec.gp.semantic.nodeselectors.LinearProbabilityNodeSelector");

	@Override
	public Parameter defaultBase() {
		return DEFAULT_BASE;
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public GPNode pickNode(EvolutionState state, int subpopulation, int thread, GPIndividual ind, GPTree tree) {

		int depth = tree.child.depth(); // >= 1
		int level = state.random[thread].nextInt(depth); // 0..depth-1
		List<GPNode> nodesAtLevel = getNodesAtLevel(tree.child, level);
		return nodesAtLevel.get(state.random[thread].nextInt(nodesAtLevel.size()));
	}

	@Override
	public void reset() {
		// Nothing to do here
	}

	/**
	 * 
	 * @param node
	 * @param level
	 *            Zero-based
	 * @return
	 */
	private List<GPNode> getNodesAtLevel(GPNode node, int level) {
		List<GPNode> list = new ArrayList<GPNode>(1 << level); // assuming full binary tree, we will get 2^level nodes
		getNodesAtLevel(list, node, level);
		return list;
	}

	private void getNodesAtLevel(List<GPNode> nodes, GPNode node, int level) {
		if (level == 0) {
			nodes.add(node);
		} else {
			for (GPNode child : node.children) {
				getNodesAtLevel(nodes, child, level - 1);
			}
		}
	}
}
