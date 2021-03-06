package library.instructions.logic;

import library.instructions.InputData;
import library.instructions.InstructionBase;
import library.instructions.InstructionType;

public final class And extends InstructionBase<Boolean> {

	@Override
	public String getName() {
		return "and";
	}

	@Override
	public String getSymbol() {
		return "&";
	}

	@Override
	public boolean isSymmetric() {
		return true;
	}

	@Override
	public int getNumberOfArguments() {
		return 2;
	}
	
	@Override
	public InstructionType getType() {
		return InstructionType.Boolean;
	}

	@Override
	public Boolean execute(InputData<Boolean> input, Boolean... args) {
		assert args.length == 2;
		return args[0] && args[1];
	}

	@Override
	public Boolean[] invert(Boolean output, int forChild, Boolean... otherArgs) {
		assert 0 <= forChild && forChild <= 1;
		if (otherArgs[1 - forChild]) {
			return new Boolean[] { output };
		}

		if (output)
			return null;
		else
			return new Boolean[0];
	}

}
