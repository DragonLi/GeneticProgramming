package library.instructions.numeric;

import library.instructions.InputData;
import library.instructions.InstructionBase;
import library.instructions.InstructionType;

public final class Zero extends InstructionBase<Double> {

	@Override
	public String getName() {
		return "0";
	}

	@Override
	public String getSymbol() {
		return "0";
	}

	@Override
	public boolean isSymmetric() {
		return true;
	}

	@Override
	public int getNumberOfArguments() {
		return 0;
	}
	
	@Override
	public InstructionType getType() {
		return InstructionType.Double;
	}

	@Override
	public Double execute(final InputData<Double> input, final Double... args) {
		return 0.0;
	}

	@Override
	public Double[] invert(final Double output, final int forChild, final Double... otherArgs) {
		return null; // the instruction is non-invertible
	}
}
