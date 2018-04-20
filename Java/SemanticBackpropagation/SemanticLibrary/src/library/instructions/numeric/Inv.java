package library.instructions.numeric;

import library.instructions.InputData;
import library.instructions.InstructionBase;
import library.instructions.InstructionType;

public final class Inv extends InstructionBase<Double> {

	@Override
	public String getName() {
		return "inv";
	}

	@Override
	public String getSymbol() {
		return "1/";
	}

	@Override
	public boolean isSymmetric() {
		return true;
	}

	@Override
	public int getNumberOfArguments() {
		return 1;
	}
	
	@Override
	public InstructionType getType() {
		return InstructionType.Double;
	}

	@Override
	public Double execute(final InputData<Double> input, final Double... args) {
		assert args.length == getNumberOfArguments();
		if (args[0] == 0) {
			return 0.0;
		}
		return 1.0 / args[0];
	}

	@Override
	public Double[] invert(final Double output, final int forChild, final Double... otherArgs) {
		assert forChild == 1;

		return new Double[] { 1.0 / output };
	}

}
