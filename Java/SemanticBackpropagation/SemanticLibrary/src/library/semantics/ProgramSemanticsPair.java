package library.semantics;

import library.generator.TreeNode;

public class ProgramSemanticsPair {
	public final TreeNode program;
	public final VectorSemantics<?,?> semantics;

	public ProgramSemanticsPair(final TreeNode program, final VectorSemantics<?,?> semantics) {
		this.program = program;
		this.semantics = semantics;
	}
}
