package ec.app.semanticGP.func.logic;

import library.semantics.BitSet;
import ec.gp.semantic.BooleanSemantics;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.UnaryNode;

public class Inv extends UnaryNode<Boolean> {

	@Override
	public String toString() {
		return "!";
	}

	@Override
	protected ISemantics execute(ISemantics... arguments) {
		/*boolean[] semantics = new boolean[arguments[0].size()];

		for (int i = 0; i < semantics.length; ++i) {
			semantics[i] = !(Boolean) arguments[0].getValue(i);
		}*/
		
		BitSet semantics = (BitSet)((BitSet)arguments[0].getValue()).clone();
		semantics.flip(0, semantics.length());

		return new BooleanSemantics(semantics);
	}

	@Override
	public Boolean[] invert(Boolean output, int missingArgIdx, Boolean... restOfArguments) {
		return new Boolean[] { !output };
	}
}
