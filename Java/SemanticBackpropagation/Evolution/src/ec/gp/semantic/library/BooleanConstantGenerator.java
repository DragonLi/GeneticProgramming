package ec.gp.semantic.library;

import java.util.NavigableSet;

import library.space.SearchResult;
import ec.EvolutionState;
import ec.app.semanticGP.func.logic.Constant;
import ec.gp.GPNode;
import ec.gp.semantic.DesiredSemanticsBase;

public class BooleanConstantGenerator implements IConstantGenerator<Boolean> {

	private final EvolutionState state;

	public BooleanConstantGenerator(EvolutionState state) {
		this.state = state;
	}

	@Override
	public SearchResult<GPNode> getPerfectConstant(DesiredSemanticsBase<Boolean> semantics) {
		int countFalse = 0;
		int countTrue = 0;
		int inconsistencies = 0;

		for (int bucket = 0; bucket < semantics.size(); ++bucket) {
			NavigableSet<Boolean> bucketVals = semantics.getValuesFor(bucket);
			for (Boolean v : bucketVals) {
				if (v == null)
					++inconsistencies;
				else if (v)
					++countTrue;
				else
					++countFalse;
			}
		}

		boolean output;
		int error;

		if (countTrue >= countFalse) {
			output = true;
			error = countFalse;
		} else {
			output = false;
			error = countTrue;
		}

		error *= (1 + inconsistencies);

		return new SearchResult<GPNode>(new Constant(this.state, output), error);
	}
}
