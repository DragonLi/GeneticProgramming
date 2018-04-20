package library.instructions;

public final class ArtificialVariable<TData> extends InstructionBase<TData> {
	private static final long serialVersionUID = 1L;
	protected int argumentNumber = 0;

	public ArtificialVariable(final int argumentNumber) {
		this.argumentNumber = argumentNumber;
	}

	@Override
	public String getName() {
		return String.format("a_{%d}", argumentNumber + 1);
	}

	@Override
	public String getSymbol() {
		return getName();
	}

	@Override
	public boolean isSymmetric() {
		return false;
	}

	@Override
	public int getNumberOfArguments() {
		return 0;
	}

	public int getArgumentNumber() {
		return argumentNumber;
	}
	
	@Override
	public InstructionType getType() {
		return InstructionType.Unknown;
	}

	@Override
	public final TData execute(final InputData<TData> input, final TData... args) {
		if (input instanceof ExtendedInputData<?>)
			return ((ExtendedInputData<TData>) input).getArtificialVariables()[argumentNumber];
		throw new RuntimeException("input is not an ExtendedInputData, cannot evaluate!");
	}

	@Override
	public TData[] invert(TData output, int forChild, TData... otherArgs) {
		return null; // the instruction is non-invertible
	}

}
