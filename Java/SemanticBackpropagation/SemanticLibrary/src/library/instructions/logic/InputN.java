package library.instructions.logic;

import library.instructions.InputData;
import library.instructions.InstructionBase;
import library.instructions.InstructionType;

public class InputN extends InstructionBase<Boolean> {

	private final int n;

	public InputN(int n) {
		if (n < 0)
			throw new RuntimeException("n cannot be negative!");

		this.n = n;
	}

	@Override
	public String getName() {
		return "input " + this.n;
	}

	@Override
	public String getSymbol() {
		return "in" + this.n;
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

	/**
	 * Returns number of input represented by this object.
	 * 
	 * @return
	 */
	public int getN() {
		return this.n;
	}

	@Override
	public final Boolean execute(InputData<Boolean> input, Boolean... args) {
		assert args == null || args.length == 0;
		return input.getX()[this.n];
	}

	@Override
	public Boolean[] invert(Boolean output, int forChild, Boolean... otherArgs) {
		return null; //cannot invert
	}

}
