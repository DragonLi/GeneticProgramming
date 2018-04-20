package ec.app.semanticGP;

import java.util.ArrayList;

import library.semantics.BitSet;
import library.semantics.TestCase;
import ec.EvolutionState;
import ec.gp.semantic.BooleanSemantics;
import ec.util.Parameter;

public class Majority extends BooleanProblem {

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
		Boolean v;
		int ones;

		this.trainingSet = new ArrayList<TestCase<Boolean>>(upperBound);
		BitSet target = new BitSet(upperBound);

		for (int i = 0; i < upperBound; ++i) {
			ones = Integer.bitCount(i);
			if (ones + ones < this.bits) //no. of ones is less then no. of zeros
				v = false;
			else if (ones + ones > this.bits) //no. of ones is greater then no. of zeros
				v = true;
			else
				v = null; // we don't care (no. of ones is equal to no. of zeros)

			tc = new TestCase<Boolean>(v, asArray(i, this.bits));
			this.trainingSet.add(tc);
			if (v != null) //else target[i] <- false
				target.set(i, v);
		}

		this.targetSemantics = new BooleanSemantics(target);
	}

	@Override
	public String getName() {
		return "Majority" + this.bits;
	}

}
