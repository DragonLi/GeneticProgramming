package library.generator;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import library.instructions.ArtificialVariable;
import library.instructions.InputData;
import library.instructions.InstructionBase;
import library.instructions.InstructionType;

public class TreeNode implements Serializable, Comparable<TreeNode> {
	private static final long serialVersionUID = 1L;
	private static final TreeNode[] EMPTY_CHILDREN = new TreeNode[0];

	protected InstructionBase<?> instruction;
	protected TreeNode[] children;

	/**
	 * Creates new instance of TreeNode. The constructor is protected, use ITreeNodeFactory to create new instances of
	 * TreeNode in external code.
	 * 
	 * @param instruction
	 *            The instruction of the node.
	 */
	protected TreeNode(final InstructionBase<?> instruction) {
		setInstruction(instruction);
	}

	public InstructionBase<?> getInstruction() {
		return instruction;
	}

	public void setInstruction(final InstructionBase<?> instruction) {
		this.instruction = instruction;
		int args = instruction.getNumberOfArguments();
		if (args > 0)
			this.children = new TreeNode[args];
		else
			this.children = EMPTY_CHILDREN;

		childrenValues = null;
	}

	public TreeNode[] getChildren() {
		return children;
	}

	public void setChild(final int index, TreeNode node) {
		children[index] = node;
	}

	public void setChild(final int index, InstructionBase<?> instruction) {
		setChild(index, new TreeNode(instruction));
	}

	public int getHeight() {
		int childMaxHeight = 0;
		int childHeight;

		for (final TreeNode child : getChildren()) {
			childHeight = child.getHeight();
			if (childHeight > childMaxHeight)
				childMaxHeight = childHeight;
		}

		return childMaxHeight + 1;
	}

	public int getNodeCount() {
		int count = 1;
		for (final TreeNode child : getChildren()) {
			count += child.getNodeCount();
		}
		return count;
	}

	/**
	 * The method counts distinct terminals in the tree in order to compute number of program arguments.
	 * 
	 * @return
	 */
	public int getNumberOfArguments() {
		final Set<Class<?>> set = new HashSet<Class<?>>();
		collectTerminals(set);
		return set.size();
	}

	private void collectTerminals(final Set<Class<?>> classes) {
		if (instruction.getNumberOfArguments() == 0)
			classes.add(instruction.getClass());
		else {
			for (final TreeNode child : getChildren()) {
				child.collectTerminals(classes);
			}
		}
	}

	public SortedSet<Integer> getArtificialVariables() {
		final SortedSet<Integer> set = new TreeSet<Integer>();
		collectArtificialVariables(set);
		return set;
	}

	private void collectArtificialVariables(final SortedSet<Integer> variables) {
		if (instruction instanceof ArtificialVariable)
			variables.add(((ArtificialVariable) instruction).getArgumentNumber());
		else {
			for (final TreeNode child : getChildren()) {
				child.collectArtificialVariables(variables);
			}
		}
	}

	private transient Object[] childrenValues = null;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object execute(final InputData<?> input) {

		if (children.length != 0) {
			
			if (childrenValues == null) {
				switch (this.instruction.getType()) {
					case Double:
						childrenValues = new Double[children.length];
						break;
					case Boolean:
						childrenValues = new Boolean[children.length];
						break;
					default:
						childrenValues = new Object[children.length];
						break;
				}
			}
			
			for (int i = 0; i < children.length; ++i) {
				childrenValues[i] = children[i].execute(input);
			}
		}

		return ((InstructionBase) instruction).execute(input, childrenValues);
	}

	@Override
	public String toString() {
		final TreeNode[] children = getChildren();
		final String[] childStrings = new String[children.length];
		for (int i = 0; i < children.length; ++i) {
			childStrings[i] = children[i].toString();
		}
		return instruction.toString(childStrings);
	}

	public TreeNode deepClone(final boolean cloneLeafs) {
		TreeNode cloned = this;

		if (this.children != null) {
			cloned = new TreeNode(this.instruction);
			cloned.childrenValues = null;

			// construction already made this:
			// cloned.children = new TreeNode[children.length];
			for (int i = 0; i < children.length; ++i) {
				cloned.children[i] = this.children[i].deepClone(cloneLeafs);
			}
		} else if (cloneLeafs) {
			cloned = new TreeNode(this.instruction);
		}

		return cloned;
	}

	public boolean containsInstruction(final Class<?> instructionType) {
		if (instructionType.isAssignableFrom(instruction.getClass()))
			return true;

		for (final TreeNode child : getChildren()) {
			if (child.containsInstruction(instructionType))
				return true;
		}

		return false;
	}

	@Override
	public int compareTo(final TreeNode other) {
		return this.getNodeCount() - other.getNodeCount();
	}

	/**
	 * Deeply compares two programs if they are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TreeNode))
			return false;

		TreeNode other = (TreeNode) obj;
		if (!this.instruction.equals(other.instruction) || this.children.length != other.children.length)
			return false;

		for (int i = 0; i < this.children.length; ++i) {
			if (!this.children[i].equals(other.children[i]))
				return false;
		}

		return true;
	}

	/**
	 * Deeply calculates hashCode for entire program.
	 */
	@Override
	public int hashCode() {
		int hash = this.instruction.hashCode();
		for (int i = 0; i < children.length; ++i) {
			hash ^= ~(children[i].hashCode() << (i + 1));
		}
		return hash;
	}

}
