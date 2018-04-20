package ec.app.semanticGP;

import java.util.ArrayList;

import library.semantics.BitSet;
import library.semantics.TestCase;
import ec.EvolutionState;
import ec.gp.semantic.BooleanSemantics;
import ec.util.Parameter;

public class Multiplexer extends BooleanProblem {

	private int addressBits = 3;

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);

		this.addressBits = state.parameters.getIntWithDefault(base.push("addressBits"),
				defaultBase().push("addressBits"), this.addressBits);
		this.calculateTestCases();
	}

	private void calculateTestCases() {
		final int totalBits = (1 << this.addressBits) + this.addressBits; // (data_bits + address_bits);
		final int upperBound = 1 << totalBits;
		TestCase<Boolean> tc;
		boolean v;

		this.trainingSet = new ArrayList<TestCase<Boolean>>(upperBound);
		BitSet target = new BitSet(upperBound);

		for (int i = 0; i < upperBound; ++i) {
			// i = [data_bits... addr_bits...]
			v = (i >>> ((/*addr_val*/i & ((1 << this.addressBits) - 1)) + /*addr_len*/this.addressBits)) == 1;
			tc = new TestCase<Boolean>(v, asArray(i, totalBits));
			this.trainingSet.add(tc);
			target.set(i, v);
		}

		this.targetSemantics = new BooleanSemantics(target);
	}

	@Override
	public String getName() {
		return "Multiplexer" + ((1 << this.addressBits) + this.addressBits);
	}

}
