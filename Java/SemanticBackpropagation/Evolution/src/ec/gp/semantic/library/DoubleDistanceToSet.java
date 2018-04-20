package ec.gp.semantic.library;

import java.util.Arrays;
import java.util.NavigableSet;

import library.distance.IDistanceTo;
import ec.EvolutionState;
import ec.gp.semantic.DesiredSemanticsBase;
import ec.gp.semantic.ISemanticProblem;

public final class DoubleDistanceToSet implements IDistanceTo<double[]> {

	private static final CalculateDoubleFast calculateFast = new CalculateDoubleFast();
	private static final CalculateDoubleExactly calculateExactly = new CalculateDoubleExactly();

	private final NavigableSet[] buckets;
	private final Double[] desiredSemantics;
	private final ICalculateDouble calculator;

	public DoubleDistanceToSet(final EvolutionState state, final DesiredSemanticsBase<Double> desiredSemantics) {
		ISemanticProblem<Double> problem = (ISemanticProblem<Double>) state.evaluator.p_problem;
		boolean isSimpleSemantics = problem.getTargetSemantics().size() == desiredSemantics.size();

		// DesiredSemanticsBase dynamically expands itself, so it checks its underlying 
		// structures for existence each time, we call getValuesFor. Because of that 
		// we decided to store (cache) desired values for each fitness case.
		this.buckets = new NavigableSet[desiredSemantics.size()];
		for (int i = 0; i < desiredSemantics.size(); ++i) {
			this.buckets[i] = desiredSemantics.getValuesFor(i);

			if (this.buckets[i].size() != 1 || this.buckets[i].first() == null)
				isSimpleSemantics = false;
		}

		if (isSimpleSemantics) {
			this.calculator = DoubleDistanceToSet.calculateFast;
			this.desiredSemantics = new Double[this.buckets.length];
			for (int i = 0; i < this.buckets.length; ++i) {
				assert this.buckets[i].size() == 1 && this.buckets[i].first() != null;
				this.desiredSemantics[i] = (Double) this.buckets[i].first();
			}
		} else {
			this.calculator = DoubleDistanceToSet.calculateExactly;
			this.desiredSemantics = null;
		}
	}

	@Override
	public double getDistanceTo(final double[] semantics) {
		return this.calculator.calculate(this.buckets, this.desiredSemantics, semantics);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.buckets);
	}

	@Override
	public boolean equals(Object obj) {
		assert obj == null || obj instanceof DoubleDistanceToSet;
		return obj != null && Arrays.deepEquals(this.buckets, ((DoubleDistanceToSet) obj).buckets);
	}
}

interface ICalculateDouble {
	double calculate(final NavigableSet[] buckets, final Double[] desiredSemantics, final double[] semantics);
}

/**
 * The class implements standard Manhattan distance. Use only if desired semantics has exactly one value for each
 * fitness case.
 */
final class CalculateDoubleFast implements ICalculateDouble {

	@Override
	public double calculate(final NavigableSet[] buckets, final Double[] desiredSemantics, final double[] semantics) {
		assert desiredSemantics.length == semantics.length;

		double diff;
		double distance = 0.0;
		for (int i = 0; i < desiredSemantics.length; ++i) {
			diff = desiredSemantics[i] - semantics[i];
			distance += Math.abs(diff);// * diff;
		}

		return distance;//Math.sqrt(distance);
	}
}

/**
 * This class calculates minimal Manhattan distance over all possible combinations of desired values.
 * 
 */
final class CalculateDoubleExactly implements ICalculateDouble {

	@Override
	public double calculate(final NavigableSet[] buckets, final Double[] desiredSemantics, final double[] semantics) {
		Double notLower, notHigher;
		double distance = 0;
		int inconsistencies = 0;
		NavigableSet<Double> desiredValues;

		for (int fc = 0; fc < semantics.length && fc < buckets.length; ++fc) {
			desiredValues = buckets[fc];

			// O(1)
			if (desiredValues.isEmpty()) {
				// don't care
				continue;
			} else if (desiredValues.first() == null) { // O(1)
				++inconsistencies;
				continue;
			}

			// 2 * O(log(n))
			notLower = desiredValues.ceiling(semantics[fc]);
			notHigher = desiredValues.floor(semantics[fc]);

			if (notLower == null)
				notLower = Double.POSITIVE_INFINITY;

			if (notHigher == null)
				notHigher = Double.NEGATIVE_INFINITY;

			assert !Double.isNaN(notLower) && !Double.isNaN(notHigher);

			// calculate diffs
			notLower = notLower - semantics[fc];
			notHigher = semantics[fc] - notHigher;

			// add a smaller of two above
			if (notLower < notHigher) {
				assert notLower >= 0 || Double.isNaN(notLower) || Double.isInfinite(notLower);
				distance += notLower;// * notLower;
			} else {
				assert notHigher >= 0 || Double.isNaN(notHigher) || Double.isInfinite(notHigher);
				distance += notHigher;// * notHigher;
			}
		}

		// Euclidean distance
		//distance = Math.sqrt(distance);

		// penalize inconsistencies
		distance += inconsistencies;//Math.scalb(distance, inconsistencies);

		if (distance == Double.POSITIVE_INFINITY || distance == Double.NaN)
			distance = Double.MAX_VALUE;

		return distance;
	}
}
