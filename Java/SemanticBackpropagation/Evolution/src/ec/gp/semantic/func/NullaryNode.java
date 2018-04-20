package ec.gp.semantic.func;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public abstract class NullaryNode<DataType> extends SimpleNodeBase<DataType> {

	@Override
	public void checkConstraints(final EvolutionState state, final int tree, final GPIndividual typicalIndividual,
			final Parameter individualBase) {
		super.checkConstraints(state, tree, typicalIndividual, individualBase);

		if (this.children.length != 0) {
			state.output.error(String.format("This is nullary node, %d children found", this.children.length));
		}
	}
	
	@Override
	public DataType[] invert(DataType output, int missingArgIdx, DataType... restOfArguments) {
		throw new UnsupportedOperationException("Cannot invert nullary operation");
	}
	
}
