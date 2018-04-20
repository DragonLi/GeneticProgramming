package ec.app.semanticGP.func.logic;

import library.semantics.BitSet;
import ec.gp.semantic.BooleanSemantics;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.BinaryNode;

public final class Or extends BinaryNode<Boolean> {

	@Override
	public String toString() {
		return "or";
	}

	@Override
	protected ISemantics execute(ISemantics... arguments) {
		/*boolean[] semantics = new boolean[arguments[0].size()];

		for (int i = 0; i < semantics.length; ++i) {
			semantics[i] = (Boolean) arguments[0].getValue(i) || (Boolean) arguments[1].getValue(i);
		}*/
		
		BitSet semantics = (BitSet)((BitSet)arguments[0].getValue()).clone();
		semantics.or((BitSet)arguments[1].getValue());

		return new BooleanSemantics(semantics);
	}

	@Override
	public Boolean[] invert(Boolean output, int missingArgIdx, Boolean... restOfArguments) {
		if (restOfArguments[0]) {
			if (output)
				return new Boolean[0];
			else
				return new Boolean[] { null };
		}

		assert !restOfArguments[0];
		return new Boolean[] { output /* && !restOfArguments[0] */};
	}

}
