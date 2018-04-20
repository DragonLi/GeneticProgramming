package ec.gp.semantic.library;

import library.space.SearchResult;
import ec.gp.GPNode;
import ec.gp.semantic.DesiredSemanticsBase;

public interface IConstantGenerator<SemType> {

	SearchResult<GPNode> getPerfectConstant(DesiredSemanticsBase<SemType> semantics);
	
}
