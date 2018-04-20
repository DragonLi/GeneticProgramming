package library.instructions.numeric;

import library.instructions.InputData;
import library.instructions.InstructionBase;
import library.instructions.InstructionType;

public final class Pow extends InstructionBase<Double> {
	private static final long serialVersionUID = 1L;

	@Override
	public String getSymbol() {
		return "^";
	}

	@Override
	public String getName() {
		return "pow";
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
		return Math.pow(args[0], args[1]);
	}

	@Override
	public Double[] invert(final Double output, final int forChild, final Double... otherArgs) {
		assert 0 <= forChild && forChild < this.getNumberOfArguments();
		
		double val;
		switch (forChild) {
			case 0:
				val = Math.pow(output, 1.0/otherArgs[1]);
				break;
			case 1:
			default:
				val = Math.log(output)/Math.log(otherArgs[0]);
				break;
		}
		
		if(val == Double.NaN || Double.isInfinite(val))
			return null; // we cannot invert in this case
		return new Double[] { val };
	}

}
