package ec.gp.semantic.func;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.semantic.SemanticsBase;
import ec.util.Parameter;

public abstract class UnaryNode<DataType> extends SimpleNodeBase<DataType> {

	@Override
	public void checkConstraints(final EvolutionState state, final int tree, final GPIndividual typicalIndividual,
			final Parameter individualBase) {
		super.checkConstraints(state, tree, typicalIndividual, individualBase);

		if (this.children.length != 1) {
			state.output.error(String.format("This is unary node, %d children found", this.children.length));
		}
	}
	
}
