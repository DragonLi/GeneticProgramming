package ec.app.semanticGP;

import java.util.ArrayList;

import library.semantics.BitSet;
import library.semantics.TestCase;
import ec.EvolutionState;
import ec.gp.semantic.BooleanSemantics;
import ec.util.Parameter;

public class Parity extends BooleanProblem {

	private boolean evenParity = true; // false for odd-parity
	private int bits = 6;
	
	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);

		this.bits = state.parameters.getIntWithDefault(base.push("bits"), defaultBase().push("bits"), this.bits);
		this.calculateTestCases();
	}

	private void calculateTestCases() {
		final int upperBound = 1 << this.bits;
		TestCase<Boolean> tc;
		boolean v;

		this.trainingSet = new ArrayList<TestCase<Boolean>>(upperBound);
		BitSet target = new BitSet(upperBound);

		for (int i = 0; i < upperBound; ++i) {
			v = (Integer.bitCount(i) & 1) == 0 ^ !evenParity;
			tc = new TestCase<Boolean>(v, asArray(i, this.bits));
			this.trainingSet.add(tc);
			target.set(i, v);
		}

		this.targetSemantics = new BooleanSemantics(target);
	}

	@Override
	public String getName() {
		return "Parity" + this.bits;
	}
}
