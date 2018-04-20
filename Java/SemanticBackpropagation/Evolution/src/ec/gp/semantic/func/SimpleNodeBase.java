package ec.gp.semantic.func;

import java.lang.ref.SoftReference;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.semantic.ISemantics;
import ec.util.Parameter;

public abstract class SimpleNodeBase<DataType> extends GPNode {

	private SoftReference<ISemantics> semantics = new SoftReference<ISemantics>(null);
	protected EvolutionState state;

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		this.state = state;
	}

	public ISemantics getSemantics() {
		ISemantics semantics = this.semantics.get();
		if (semantics == null) {
			// compute semantics now
			//ISemanticsProvider problem = (ISemanticsProvider) this.state.evaluator.p_problem;
			//this.semantics = problem.computeSemantics(this);
			ISemantics[] childSemantics = new ISemantics[this.children.length];
			for (int ch = 0; ch < this.children.length; ++ch) {
				assert this.children[ch] != null;

				childSemantics[ch] = ((SimpleNodeBase<DataType>) this.children[ch]).getSemantics();
			}

			semantics = this.execute(childSemantics);
			this.semantics = new SoftReference<ISemantics>(semantics);
		}

		return semantics;
	}

	public void resetSemantics() {
		this.semantics.clear();
	}

	public void resetSemanticsRecursive() {
		resetSemantics();
		for (int i = 0; i < children.length; ++i) {
			if (children[i] != null)
				((SimpleNodeBase<?>) children[i]).resetSemanticsRecursive();
		}
	}

	@Override
	public void resetNode(EvolutionState state, int thread) {
		super.resetNode(state, thread);
		this.resetSemantics();
	}

	@Override
	public Object clone() {
		SimpleNodeBase<DataType> cloned = (SimpleNodeBase<DataType>) super.clone();
		cloned.resetSemanticsRecursive();
		return cloned;
	}

	@Override
	public GPNode lightClone() {
		SimpleNodeBase<DataType> copy = (SimpleNodeBase<DataType>) super.lightClone();
		copy.resetSemanticsRecursive();
		return copy;
	}

	/**
	 * 
	 * @param output
	 * @param missingArgIdx
	 *            Index of argument to calculate
	 * @param restOfArguments
	 *            Array of remaining arguments, without the missing one (so, the indexes after the missing one are moved
	 *            by one).
	 * @return Array of possible values.
	 */
	public abstract DataType[] invert(DataType output, int missingArgIdx, DataType... restOfArguments);

	protected abstract ISemantics execute(ISemantics... arguments);

	@Override
	public final void eval(final EvolutionState state, final int thread, final GPData input, final ADFStack stack,
			final GPIndividual individual, final Problem problem) {
		throw new UnsupportedOperationException("Use getSemantics instead.");
	}
}
