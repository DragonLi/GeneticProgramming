package library.instructions.logic;

import library.instructions.InputData;
import library.instructions.InstructionBase;
import library.instructions.InstructionType;

public final class False extends InstructionBase<Boolean> {

	@Override
	public String getName() {
		return "false";
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
		return InstructionType.Boolean;
	}

	@Override
	public Boolean execute(InputData<Boolean> input, Boolean... args) {
		assert args.length == 0;
		return false;
	}

	@Override
	public Boolean[] invert(Boolean output, int forChild, Boolean... otherArgs) {
		return null;
	}

}
