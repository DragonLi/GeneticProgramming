package library.instructions.numeric;

import library.instructions.InputData;
import library.instructions.InstructionBase;
import library.instructions.InstructionType;

public final class One extends InstructionBase<Double> {

	@Override
	public String getName() {
		return "1";
	}

	@Override
	public String getSymbol() {
		return "1";
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
	public Double execute(InputData<Double> input, Double... args) {
		return 1.0;
	}

	@Override
	public Double[] invert(Double output, int forChild, Double... otherArgs) {
		return null; // the instruction is non-invertible
	}

}
