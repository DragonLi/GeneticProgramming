package library.instructions.numeric;

import library.instructions.InputData;
import library.instructions.InstructionBase;
import library.instructions.InstructionType;

public final class Cos extends InstructionBase<Double> {

	@Override
	public String getName() {
		return "cos";
	}

	@Override
	public String getSymbol() {
		return "cos";
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
		return Math.cos(args[0]);
	}

	@Override
	public Double[] invert(final Double output, final int forChild, final Double... otherArgs) {
		assert forChild == 0;

		if (output > 1.0 || output < -1.0)
			return null;

		double acos = Math.acos(output);
		// we return one positive and one negative value
		return new Double[] { acos, acos - Math.PI - Math.PI };
	}

}
