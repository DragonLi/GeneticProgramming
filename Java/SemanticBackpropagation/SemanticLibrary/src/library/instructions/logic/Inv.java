package library.instructions.logic;

import library.instructions.InputData;
import library.instructions.InstructionBase;
import library.instructions.InstructionType;

public final class Inv extends InstructionBase<Boolean> {

	@Override
	public String getName() {
		return "inv";
	}

	@Override
	public String getSymbol() {
		return "!";
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
		return InstructionType.Boolean;
	}
	
	@Override
	public Boolean execute(InputData<Boolean> input, Boolean... args) {
		assert args.length == 1;
		return !args[0];
	}

	@Override
	public Boolean[] invert(Boolean output, int forChild, Boolean... otherArgs) {
		assert forChild == 0;
		return new Boolean[] { !output };
	}

}
