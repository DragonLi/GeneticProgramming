package ec.gp.semantic.library;

import java.util.Arrays;
import java.util.BitSet;
import java.util.NavigableSet;

import library.distance.IDistanceTo;
import ec.EvolutionState;
import ec.gp.semantic.DesiredSemanticsBase;
import ec.gp.semantic.ISemanticProblem;

public final class BooleanDistanceToSet implements IDistanceTo<BitSet> {

	private static final CalculateBooleanFast calculateFast = new CalculateBooleanFast();
	private static final CalculateBooleanExactly calculateExactly = new CalculateBooleanExactly();

	private final NavigableSet[] buckets;
	private final Boolean[] desiredSemantics;
	private final ICalculateBoolean calculator;

	public BooleanDistanceToSet(EvolutionState state, DesiredSemanticsBase<Boolean> desiredSemantics) {
		ISemanticProblem<Boolean> problem = (ISemanticProblem<Boolean>) state.evaluator.p_problem;
		boolean isSimpleSemantics = problem.getTargetSemantics().size() == desiredSemantics.size();

		// DesiredSemanticsBase dynamically expands itself, so it checks its underlying 
		// structures for existence each time, we call getValuesFor. Because of that 
		// we decided to store (cache) desired values for each fitness case.
		this.buckets = new NavigableSet[desiredSemantics.size()];
		for (int i = 0; i < desiredSemantics.size(); ++i) {
			buckets[i] = desiredSemantics.getValuesFor(i);

			if (buckets[i].size() != 1 || buckets[i].first() == null)
				isSimpleSemantics = false;
		}

		if (isSimpleSemantics) {
			this.calculator = BooleanDistanceToSet.calculateFast;
			this.desiredSemantics = new Boolean[this.buckets.length];
			for (int i = 0; i < this.buckets.length; ++i) {
				assert this.buckets[i].size() == 1 && this.buckets[i].first() != null;
				this.desiredSemantics[i] = (Boolean) this.buckets[i].first();
			}
		} else {
			this.calculator = BooleanDistanceToSet.calculateExactly;
			this.desiredSemantics = null;
		}

	}

	@Override
	public double getDistanceTo(final BitSet semantics) {
		return this.calculator.calculate(this.buckets, this.desiredSemantics, semantics);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.buckets);
	}

	@Override
	public boolean equals(Object obj) {
		assert obj == null || obj instanceof BooleanDistanceToSet;
		return obj != null && Arrays.deepEquals(this.buckets, ((BooleanDistanceToSet) obj).buckets);
	}
}

interface ICalculateBoolean {
	double calculate(final NavigableSet[] buckets, final Boolean[] desiredSemantics, final BitSet semantics);
}

/**
 * The class implements standard Hamming distance. Use only if desired semantics has exactly one value for each fitness
 * case.
 */
final class CalculateBooleanFast implements ICalculateBoolean {

	@Override
	public double calculate(final NavigableSet[] buckets, final Boolean[] desiredSemantics, final BitSet semantics) {
		assert desiredSemantics.length == semantics.length();

		int distance = 0;
		for (int i = 0; i < desiredSemantics.length; ++i) {
			if (desiredSemantics[i] != semantics.get(i))
				distance += 1;
		}

		assert distance >= 0;
		
		return distance;
	}
}

/**
 * This class calculates minimal Hamming distance over all possible combinations of desired values.
 * 
 */
final class CalculateBooleanExactly implements ICalculateBoolean {

	@Override
	public double calculate(final NavigableSet[] buckets, final Boolean[] desiredSemantics, final BitSet semantics) {
		long distance = 0;
		int inconsistencies = 0;
		NavigableSet<Boolean> desiredValues;
		Boolean v;

		for (int fc = 0; fc < semantics.length() && fc < buckets.length; ++fc) {
			desiredValues = buckets[fc];

			// desiredValues.size():
			// 0 = don't care
			// 1 = inconsistent or a single value
			// 2 = add 0 and continue the loop, since bool can only have two values, and one of them must be equal to
			// the given semantics

			if (desiredValues.size() == 1) {
				v = desiredValues.first();
				if (v == null) {
					++inconsistencies;
					continue;
				} else if (semantics.get(fc) != v.booleanValue()) {
					distance += 1;
				}
			}
		}

		// penalize inconsistencies
		distance = distance * (1 + inconsistencies);

		assert distance >= 0;
		
		return distance;
	}
}
