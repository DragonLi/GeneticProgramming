package ec.app.semanticGP.func.logic;

import library.semantics.BitSet;
import ec.gp.semantic.BooleanSemantics;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.BinaryNode;

public final class And extends BinaryNode<Boolean> {

	@Override
	public String toString() {
		return "and";
	}

	@Override
	protected ISemantics execute(ISemantics... arguments) {
		/*BitSet semantics = new BitSet(arguments[0].size());

		for (int i = 0; i < semantics.length(); ++i) {
			semantics.set(i, (Boolean) arguments[0].getValue(i) && (Boolean) arguments[1].getValue(i));
		}*/
		
		BitSet semantics = (BitSet)((BitSet)arguments[0].getValue()).clone();
		semantics.and((BitSet)arguments[1].getValue());

		return new BooleanSemantics(semantics);
	}

	@Override
	public Boolean[] invert(Boolean output, int missingArgIdx, Boolean... restOfArguments) {
		if (restOfArguments[0]) {
			return new Boolean[] { output /*&& restOfArguments[0] == TRUE*/};
		}

		assert !restOfArguments[0];

		if (output)
			return new Boolean[] { null }; // inconsistent, we cannot obtain true
		else
			return new Boolean[0]; // don't care
	}
}
