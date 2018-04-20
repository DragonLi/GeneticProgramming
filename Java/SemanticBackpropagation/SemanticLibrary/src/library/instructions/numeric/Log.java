package library.instructions.numeric;

import library.instructions.InputData;
import library.instructions.InstructionBase;
import library.instructions.InstructionType;

public final class Log extends InstructionBase<Double> {

	@Override
	public String getName() {
		return "log";
	}

	@Override
	public String getSymbol() {
		return "log";
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
		/*if (args[0] == 0.0) {
			return 0.0;
		}*/
		return Math.log(Math.abs(args[0]));
	}

	@Override
	public Double[] invert(final Double output, final int forChild, final Double... otherArgs) {
		assert forChild == 0;

		double exp = Math.exp(output);
		//if (Double.isInfinite(exp))
		//	exp = 1E300; // prevent infinity
		return new Double[] { exp, -exp };
	}

}
