package library.generator;

import library.instructions.InstructionBase;

/**
 * Factory for tree nodes. It creates new instances of TreeNode or derived class.
 * 
 * @author Tomasz Pawlak
 * 
 */
public interface ITreeNodeFactory {

	TreeNode newNode(InstructionBase<?> instruction);

}
