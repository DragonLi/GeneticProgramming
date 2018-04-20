package ec.app.semanticGP;

import java.util.ArrayList;

import library.semantics.BitSet;
import library.semantics.TestCase;
import ec.EvolutionState;
import ec.gp.semantic.BooleanSemantics;
import ec.util.Parameter;

public class Comparator extends BooleanProblem {
	private int bits = 3;

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);

		this.bits = state.parameters.getIntWithDefault(base.push("bits"), defaultBase().push("bits"),
				this.bits);
		this.calculateTestCases();
	}

	private void calculateTestCases() {
		final int upperBound = 1 << (this.bits << 1); // we have two numbers
		final int lowMask = (1 << this.bits) - 1;
		TestCase<Boolean> tc;
		boolean v;

		this.trainingSet = new ArrayList<TestCase<Boolean>>(upperBound);
		BitSet target = new BitSet(upperBound);

		for (int i = 0; i < upperBound; ++i) {
			v = (i >>> this.bits) < (i & lowMask);
			tc = new TestCase<Boolean>(v, asArray(i, this.bits << 1));
			this.trainingSet.add(tc);
			target.set(i, v);
		}

		this.targetSemantics = new BooleanSemantics(target);
	}

	@Override
	public String getName() {
		return "Comparator" + (this.bits << 1);
	}
}
