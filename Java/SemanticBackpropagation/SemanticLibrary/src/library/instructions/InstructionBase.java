package library.instructions;

import java.io.Serializable;

import library.INamedElement;

public abstract class InstructionBase<DType> implements INamedElement, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Gets the symbolic representation of the instruction.
	 * 
	 * @return Symbol representing the instruction.
	 */
	public abstract String getSymbol();

	/**
	 * Gets the value indicating if the instructions is symmetric? The instruction is considered symmetric iff the
	 * output of the instruction does not depend on order of arguments.
	 * 
	 * @return True for symmetric instructions, otherwise false.
	 */
	public abstract boolean isSymmetric();

	/**
	 * Gets the number of arguments of this instruction.
	 * 
	 * @return Number of instruction arguments.
	 */
	public abstract int getNumberOfArguments();

	/**
	 * Executes instruction with given arguments.
	 * 
	 * @param input
	 *            Input data of entire program. The object is supposed to be given by reference to lower level nodes.
	 * @param args
	 *            Arguments of the instruction.
	 * @return
	 */
	public abstract DType execute(InputData<DType> input, DType... args);

	/**
	 * Executes inversion of the instruction for the given child and the desired output of the instruction in context of
	 * other arguments.
	 * 
	 * @param output
	 *            Desired output of the instruction.
	 * @param forChild
	 *            Zero-based index of child, for which the instruction is inverted.
	 * @param otherArgs
	 *            Remaining arguments of the instruction. The value of the `forChild` argument is ignored.
	 * @return The array of possible inversions. The array may contain one or more values, or can be null if inversion
	 *         is not possible.
	 */
	public abstract DType[] invert(DType output, int forChild, DType... otherArgs);

	public abstract InstructionType getType();

	/**
	 * Gets textual representation of entire program rooted at this instruction/node.
	 * 
	 * @param args
	 *            Arguments of the instruction.
	 * @return Textual representation.
	 */
	public String toString(final String... args) {
		String symbol = getSymbol();
		int argNum = getNumberOfArguments();

		assert argNum == args.length;

		switch (argNum) {
			case 0:
				return symbol;
			case 1:
				return symbol + '(' + args[0] + ')';
			default:
				StringBuilder builder = new StringBuilder();
				for (String arg : args) {
					builder.append(symbol);
					builder.append('(');
					builder.append(arg);
					builder.append(')');
				}
				if (argNum > 1)
					builder.replace(0, symbol.length(), "");

				return builder.toString();
		}
	}
}
