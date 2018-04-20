package ec.gp.semantic.utils;

import java.util.NavigableSet;

import ec.EvolutionState;
import ec.gp.semantic.DesiredSemanticsBase;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.SimpleNodeBase;

public final class DoubleSemanticInverter extends SemanticInverter<Double> {

	public DoubleSemanticInverter(EvolutionState state) {
		super(state);
	}

	@Override
	protected DesiredSemanticsBase<Double> invertInstruction(final SimpleNodeBase<?> node, final int forChild,
			final DesiredSemanticsBase<Double> desiredSemantics, final ISemantics[] childrenSemantics) {

		final DesiredSemanticsBase<Double> desiredSemanticsForChild = new DesiredSemanticsBase<Double>();

		Double[] args = null;

		if (childrenSemantics != null)
			args = new Double[childrenSemantics.length];

		for (int fc = 0; fc < desiredSemantics.size(); ++fc) {
			NavigableSet<Double> desiredValues = desiredSemantics.getValuesFor(fc);

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
			if (childrenSemantics != null) {
				for (int j = 0; j < childrenSemantics.length; ++j) {
					args[j] = (Double) childrenSemantics[j].getValue(fc);
				}
			}

			// execute inversion
			for (final Double value : desiredValues) {
				Double[] inversions = ((SimpleNodeBase<Double>) node).invert(value, forChild, args);
				for (int i = 0; i < inversions.length; ++i) {
					if (inversions[i] == null || (!Double.isNaN(inversions[i]) && !Double.isInfinite(inversions[i])))
						desiredSemanticsForChild.addValues(fc, inversions[i]);
				}
			}

			NavigableSet<Double> childValues = desiredSemanticsForChild.getValuesFor(fc);
			// if it is 0 then `don't care', if 1, then it is either inconsistent, double,
			// otherwise we have to remove nulls to leave only consistent values
			if (childValues.size() > 1) {
				childValues.remove(null); //clean up inconsistencies if there is at least one consistent value
			}
		}

		return desiredSemanticsForChild;
	}

}
