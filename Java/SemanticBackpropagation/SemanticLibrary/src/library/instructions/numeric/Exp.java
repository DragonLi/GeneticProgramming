package library.instructions.numeric;

import library.instructions.InputData;
import library.instructions.InstructionBase;
import library.instructions.InstructionType;

public final class Exp extends InstructionBase<Double> {

	@Override
	public String getName() {
		return "exp";
	}

	@Override
	public String getSymbol() {
		return "e^";
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
		return Math.exp(args[0]);
	}

	@Override
	public Double[] invert(final Double output, final int forChild, final Double... otherArgs) {
		if (output < 0)
			return null;
		return new Double[] { Math.log(output) };
	}

}
