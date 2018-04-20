package library.generator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import library.instructions.InstructionBase;

public class TreeGenerator implements Iterable<TreeNode> {

	protected List<InstructionBase<?>> nonterminals;
	protected List<InstructionBase<?>> terminals;
	/**
	 * Cached terminal nodes
	 */
	protected List<TreeNode> terminalNodeCache;
	protected boolean useTerminalNodeCache = true;

	protected int maxDepth = 3;

	private ITreeNodeFactory treeNodeFactory = new TreeNodeFactory();

	// state variables
	private int terminalCount;
	private int nonterminalCount;
	private int instructionCount;
	private int maxArgs;
	private int leafDelimiter;

	public TreeGenerator() {
		nonterminals = new ArrayList<InstructionBase<?>>();
		terminals = new ArrayList<InstructionBase<?>>();
		terminalNodeCache = new ArrayList<TreeNode>();
	}

	public InstructionBase<?>[] getNonterminals() {
		return nonterminals.toArray(new InstructionBase<?>[0]);
	}

	public void addNonterminals(InstructionBase<?>... instructions) {
		for (InstructionBase<?> ins : instructions)
			nonterminals.add(ins);
	}

	public InstructionBase<?>[] getTerminals() {
		return terminals.toArray(new InstructionBase<?>[0]);
	}

	public void addTerminals(InstructionBase<?>... instructions) {
		for (InstructionBase<?> ins : instructions) {
			terminals.add(ins);
			terminalNodeCache.add(this.treeNodeFactory.newNode(ins));
		}
	}

	public int getDepth() {
		return maxDepth;
	}

	public void setDepth(int depth) {
		if (depth < 1)
			throw new IllegalArgumentException("Depth must be greater than 0.");
		maxDepth = depth;
	}

	public ITreeNodeFactory getTreeNodeFactory() {
		return this.treeNodeFactory;
	}

	public void setTreeNodeFactory(ITreeNodeFactory factory) {
		if (factory == null)
			throw new IllegalArgumentException("Argument cannot be null");
		this.treeNodeFactory = factory;
	}

	public boolean getUseTerminalNodeCache() {
		return this.useTerminalNodeCache;
	}

	/**
	 * Enables or disables terminal node caching. If useCache is true, then all terminal nodes in programs returned by
	 * generator are a the sample instance of TreeNode, otherwise each terminal node is distance instance of TreeNode.
	 * 
	 * @param useCache
	 */
	public void setUseTerminalNodeCache(boolean useCache) {
		this.useTerminalNodeCache = useCache;
	}

	@Override
	public Iterator<TreeNode> iterator() {
		// compute some constants in order to improve performance
		terminalCount = terminals.size();
		nonterminalCount = nonterminals.size();
		instructionCount = terminalCount + nonterminalCount;

		// get maximum number of instruction arguments
		maxArgs = 0;
		for (InstructionBase<?> ins : nonterminals) {
			int args = ins.getNumberOfArguments();
			if (args > maxArgs)
				maxArgs = args;
		}

		// generate state vector and its mask
		final int[] state = new int[((int) Math.pow(maxArgs, maxDepth) - 1) / (maxArgs - 1)];
		final boolean[] mask = new boolean[state.length];
		mask[0] = true;

		// compute leaf delimiter
		leafDelimiter = ((int) Math.pow(maxArgs, maxDepth - 1) - 1) / (maxArgs - 1);

		return new Iterator<TreeNode>() {
			private boolean hasNext = true;

			@Override
			public boolean hasNext() {
				return hasNext;
			}

			@Override
			public TreeNode next() {
				try {
					int childStart;
					boolean constraintsSatisfied;
					TreeNode progToReturn = null;

					outerMostLoop: do {
						// validate symmetry constraints
						constraintsSatisfied = true;
						for (int i = 0; i < state.length; ++i) {
							if (!mask[i])
								continue;
							InstructionBase<?> ins = getInstructionById(state[i], i);
							if (ins.isSymmetric()) {
								childStart = maxArgs * i + 1;
								for (int ch = 0; ch < ins.getNumberOfArguments() - 1; ++ch) {
									if (state[childStart + ch] > state[childStart + ch + 1]) {
										constraintsSatisfied = false;
										break;
									}
								}
							}
							if (!constraintsSatisfied)
								break;
						}

						// build program
						if (constraintsSatisfied)
							progToReturn = generateProgram(state, mask, 0);
						else
							updateMask(state, mask, 0);

						// increment
						for (int m = mask.length - 1; m >= 0; --m) {
							if (mask[m]) {

								// increment state at last position
								++state[m];
								// check for overflow and if necessary increment previous
								// element
								boolean overflow = false;
								for (int o = m; o >= leafDelimiter; --o) {
									if (!mask[o])
										continue;

									if (state[o] >= terminalCount) {
										state[o] = 0;
										overflow = true;
										for (int prev = o - 1; prev >= 0; --prev) {
											if (mask[prev]) {
												++state[prev];
												overflow = false;
												break;
											}
										}
									} else {
										break;
									}
								}
								for (int o = leafDelimiter - 1; o >= 0; --o) {
									if (!mask[o])
										continue;

									if (state[o] >= instructionCount) {
										state[o] = 0;
										overflow = true;
										for (int prev = o - 1; prev >= 0; --prev) {
											if (mask[prev]) {
												++state[prev];
												overflow = false;
												break;
											}
										}
									} else {
										break;
									}
								}

								// state vector got overflow, we've checked all combinations
								if (overflow) {
									this.hasNext = false;
									break outerMostLoop;
								}

								break;
							}
						}
					} while (progToReturn == null);

					return progToReturn;

				} catch (OutOfMemoryError ex) {
					System.out.println("state vector: ");
					for (int s : state) {
						System.out.print(s + " ");
					}
					System.out.println();
					throw ex;
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	public List<TreeNode> generate() {
		List<TreeNode> programs = new ArrayList<TreeNode>();

		for (final TreeNode prog : this) {
			programs.add(prog);
		}

		return programs;
	}

	/**
	 * 
	 * @param id
	 *            Instruction id
	 * @param index
	 *            Index in state vector
	 * @return Instruction with given id. The id may vary depending of instruction position in the state vector.
	 */
	private InstructionBase<?> getInstructionById(final int id, final int index) {
		/*if (index < leafDelimiter) {
			if (id < nonterminalCount)
				return nonterminals.get(id);
			return terminals.get(id - nonterminalCount);
		} else
			return terminals.get(id);*/
		
		if (index < leafDelimiter) {
			if (id < terminalCount)
				return terminals.get(id);
			return nonterminals.get(id - terminalCount);
		} else
			return terminals.get(id);
	}

	private TreeNode getTreeNodeByInstructionId(final int id, final int index) {
		/*if (index < leafDelimiter) {
			// nonterminals
			if (id < nonterminalCount)
				return this.treeNodeFactory.newNode(nonterminals.get(id));

			// terminals
			if (this.useTerminalNodeCache)
				return this.terminalNodeCache.get(id - nonterminalCount);
			return this.treeNodeFactory.newNode(terminals.get(id - nonterminalCount));
		} else {
			if (this.useTerminalNodeCache)
				return this.terminalNodeCache.get(id);
			return this.treeNodeFactory.newNode(terminals.get(id));
		}*/
		
		if (index < leafDelimiter) {
			// terminals
			if (id < terminalCount) {
				if (this.useTerminalNodeCache)
					return this.terminalNodeCache.get(id);
				return this.treeNodeFactory.newNode(terminals.get(id));
			}

			// nonterminals
			return this.treeNodeFactory.newNode(nonterminals.get(id - terminalCount));
		} else {
			if (this.useTerminalNodeCache)
				return this.terminalNodeCache.get(id);
			return this.treeNodeFactory.newNode(terminals.get(id));
		}
	}

	private TreeNode generateProgram(final int[] state, final boolean[] mask, final int index) {
		TreeNode parent = getTreeNodeByInstructionId(state[index], index);
		int args = parent.getInstruction().getNumberOfArguments();
		int childStart = maxArgs * index + 1;

		for (int ch = 0; ch < args && mask.length > childStart + ch; ++ch) {
			mask[childStart + ch] = true;
			parent.setChild(ch, generateProgram(state, mask, childStart + ch));
		}

		if (index < leafDelimiter) {
			for (int ch = args; ch < maxArgs; ++ch) {
				mask[childStart + ch] = false;
				if (childStart + ch < leafDelimiter)
					clearMask(mask, childStart + ch);
			}
		}

		return parent;
	}

	private void updateMask(final int[] state, final boolean[] mask, final int index) {
		InstructionBase<?> instruction = getInstructionById(state[index], index);
		int args = instruction.getNumberOfArguments();
		int childStart = maxArgs * index + 1;

		for (int ch = 0; ch < args /*&& mask.length > childStart + ch*/; ++ch) {
			mask[childStart + ch] = true;
			updateMask(state, mask, childStart + ch);
		}

		if (index < leafDelimiter) {
			for (int ch = args; ch < maxArgs; ++ch) {
				mask[childStart + ch] = false;
				if (childStart + ch < leafDelimiter)
					clearMask(mask, childStart + ch);
			}
		}
	}

	private void clearMask(final boolean[] mask, final int index) {
		int childStart = maxArgs * index + 1;
		for (int ch = 0; ch < maxArgs; ++ch) {
			mask[childStart + ch] = false;
			if (childStart + ch < leafDelimiter)
				clearMask(mask, childStart + ch);
		}
	}

}
