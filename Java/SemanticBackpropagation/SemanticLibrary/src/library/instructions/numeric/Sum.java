package library.instructions.numeric;

import library.instructions.InputData;
import library.instructions.InstructionBase;
import library.instructions.InstructionType;

public final class Sum extends InstructionBase<Double> {
	private static final long serialVersionUID = 1L;

	@Override
	public String getSymbol() {
		return "+";
	}

	@Override
	public String getName() {
		return "sum";
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
		return InstructionType.Double;
	}
	
	@Override
	public final Double execute(final InputData<Double> input, final Double... args) {
		assert args.length == getNumberOfArguments();
		return args[0] + args[1];
	}

	@Override
	public Double[] invert(final Double output, final int forChild, final Double... otherArgs) {
		assert 0 <= forChild && forChild < this.getNumberOfArguments();
		return new Double[] { output - otherArgs[1 - forChild] };
	}

}
