package library.generator;

import library.instructions.InstructionBase;

public final class TreeNodeFactory implements ITreeNodeFactory {

	@Override
	public TreeNode newNode(final InstructionBase<?> instruction) {
		return new TreeNode(instruction);
	}

}
