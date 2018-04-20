package library.instructions.numeric;

import library.instructions.InputData;
import library.instructions.InstructionBase;
import library.instructions.InstructionType;

public final class Sin extends InstructionBase<Double> {
	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return "sin";
	}

	@Override
	public String getSymbol() {
		return "sin";
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
		return Math.sin(args[0]);
	}

	@Override
	public Double[] invert(final Double output, final int forChild, final Double... otherArgs) {
		if(output > 1.0 || output < -1.0)
			return null; // cannot invert in this context
		
		double asin = Math.asin(output);
		// we return one positive and one negative value
		return new Double[] { asin, asin - Math.PI - Math.PI };
	}

}
