package library.instructions.numeric;

import library.instructions.InputData;
import library.instructions.InstructionBase;
import library.instructions.InstructionType;

public final class Div extends InstructionBase<Double> {
	private static final long serialVersionUID = 1L;

	@Override
	public String getSymbol() {
		return "/";
	}

	@Override
	public String getName() {
		return "div";
	}

	@Override
	public boolean isSymmetric() {
		return false;
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
		if (args[1] == 0.0)
			return 0.0;
		return args[0] / args[1];
	}

	@Override
	public Double[] invert(final Double output, final int forChild, final Double... otherArgs) {
		assert 0 <= forChild && forChild < this.getNumberOfArguments();

		switch (forChild) {
			case 0:
				if (!Double.isInfinite(otherArgs[1]))
					return new Double[] { output * otherArgs[1] };
				else if (output == 0.0)
					return new Double[0]; // don't care
				else
					return null; // inconsistent
			case 1:
			default:
				if (otherArgs[0] != 0.0)
					return new Double[] { otherArgs[0] / output };
				else if (output == 0.0)
					return new Double[0]; //prevent infinity
				else
					return null; // insonsistent

		}
	}

}
