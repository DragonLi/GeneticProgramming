package ec.gp.semantic.func;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public abstract class BinaryNode<DataType> extends SimpleNodeBase<DataType> {

	@Override
	public void checkConstraints(final EvolutionState state, final int tree, final GPIndividual typicalIndividual,
			final Parameter individualBase) {
		super.checkConstraints(state, tree, typicalIndividual, individualBase);

		if (this.children.length != 2) {
			state.output.error(String.format("This is binary node, %d children found", this.children.length));
		}
	}

}
