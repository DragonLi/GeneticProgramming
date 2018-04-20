package ec.gp.semantic.utils;

import java.util.NavigableSet;

import ec.EvolutionState;
import ec.gp.semantic.DesiredSemanticsBase;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.SimpleNodeBase;

public final class BooleanSemanticInverter extends SemanticInverter<Boolean> {

	public BooleanSemanticInverter(EvolutionState state) {
		super(state);
	}

	@Override
	protected DesiredSemanticsBase<Boolean> invertInstruction(final SimpleNodeBase<?> node, final int forChild,
			final DesiredSemanticsBase<Boolean> desiredSemantics, final ISemantics[] childrenSemantics) {

		final DesiredSemanticsBase<Boolean> desiredSemanticsForChild = new DesiredSemanticsBase<Boolean>();

		Boolean[] args = null;
		if (childrenSemantics.length != 0)
			args = new Boolean[childrenSemantics.length];
		
		
		for (int fc = 0; fc < desiredSemantics.size(); ++fc) {

			NavigableSet<Boolean> desiredValues = desiredSemantics.getValuesFor(fc);

			if (desiredValues.isEmpty()) {
				// don't care value
				continue;
			} else if (desiredValues.first() == null) {
				// inconsistent value
				// rewrite to child semantics
				desiredSemanticsForChild.markInconsistent(fc);
				continue;
			}

			// prepare arguments
			for (int j = 0; j < childrenSemantics.length; ++j) {
				args[j] = (Boolean) childrenSemantics[j].getValue(fc);
			}

			// execute inversion
			for (final Boolean value : desiredValues) {
				Boolean[] inversions = ((SimpleNodeBase<Boolean>) node).invert(value, forChild, args);
				desiredSemanticsForChild.addValues(fc, inversions);
			}

			NavigableSet<Boolean> childValues = desiredSemanticsForChild.getValuesFor(fc);
			//if it is 0 then `don't care', if 1, then it is either inconsistent, true or false,
			// otherwise we have to remove nulls to leave only consistent values
			if (childValues.size() > 1) {
				childValues.remove(null);
			}
		}

		return desiredSemanticsForChild;
	}

}
