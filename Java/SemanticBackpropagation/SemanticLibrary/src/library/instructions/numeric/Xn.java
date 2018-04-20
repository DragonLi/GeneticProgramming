package library.instructions.numeric;

import library.instructions.InputData;
import library.instructions.InstructionBase;
import library.instructions.InstructionType;

/**
 * Represents n-th independent variable X_n.
 * 
 * @author Tomasz Pawlak
 */
public class Xn extends InstructionBase<Double> {

	private final int n;

	public Xn(int n) {
		if (n < 0)
			throw new RuntimeException("n cannot be negative!");

		this.n = n;
	}

	@Override
	public String getName() {
		return "X" + this.n;
	}

	@Override
	public String getSymbol() {
		return "X" + this.n;
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
		assert args == null || args.length == 0;
		return input.getX()[this.n];
	}

	@Override
	public Double[] invert(final Double output, int forChild, final Double... otherArgs) {
		return null; // cannot invert
	}

}
