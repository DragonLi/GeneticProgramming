package ec.gp.semantic.library;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;

import library.space.SearchResult;
import ec.EvolutionState;
import ec.app.semanticGP.func.numeric.Constant;
import ec.gp.GPNode;
import ec.gp.semantic.DesiredSemanticsBase;

public final class DoubleConstantGenerator implements IConstantGenerator<Double> {

	private final EvolutionState state;

	public DoubleConstantGenerator(EvolutionState state) {
		this.state = state;
	}

	@Override
	public SearchResult<GPNode> getPerfectConstant(final DesiredSemanticsBase<Double> semantics) {
		Double bestSoFarValue = 0.0;
		double bestSoFarError = Double.POSITIVE_INFINITY;

		Double diff1, diff2;
		int inconsistencies = 0;
		final HashSet<Double> pointsOfInterest = new HashSet<Double>();
		final HashMap<Integer, NavigableSet<Double>> buckets = new HashMap<Integer, NavigableSet<Double>>();

		// O(n)
		for (int bucket = 0; bucket < semantics.size(); ++bucket) {
			NavigableSet<Double> bucketVals = semantics.getValuesFor(bucket);

			if (bucketVals.isEmpty()) {
				continue; // don't care
			} else if (bucketVals.first() == null) {
				++inconsistencies;
				continue;
			}

			buckets.put(bucket, bucketVals);

			for (final Double pt : bucketVals) {
				assert pt != null;
				if (!Double.isInfinite(pt) && !Double.isNaN(pt)) {
					pointsOfInterest.add(pt);
				}
			}
		}

		// O(n)
		for (final Double pt : pointsOfInterest) {
			double error = 0;

			for (int bucket = 0; bucket < semantics.size(); ++bucket) {
				NavigableSet<Double> bucketVals = buckets.get(bucket);

				if (bucketVals == null) {
					continue;
				}

				// O(log(n))
				Double notHigher = bucketVals.floor(pt);
				Double notLower = bucketVals.ceiling(pt);

				if (notLower == null || Double.isNaN(notLower))
					notLower = Double.MAX_VALUE;
				if (notHigher == null || Double.isNaN(notHigher))
					notHigher = -Double.MAX_VALUE;

				diff1 = pt - notHigher;
				diff2 = notLower - pt;

				assert !Double.isNaN(diff1) && !Double.isNaN(diff2);

				if (diff1 < diff2)
					error += diff1;
				else
					error += diff2;
			}

			if (error < bestSoFarError) {
				bestSoFarError = error;
				bestSoFarValue = pt;
			}
		}
		
		bestSoFarError += inconsistencies;

		/*boolean isInBucket = false;
		as: for (int b = 0; b < semantics.size(); ++b) {
			NavigableSet<Double> bucketVals = buckets.get(b);
			if (bucketVals == null)
				continue;

			for (Double bv : bucketVals) { 
				if (Math.abs(bv - bestSoFarValue) < 1E-20) {
					isInBucket = true;
					break as;
				}
			}
		}

		assert isInBucket || buckets.size() == 0;*/

		return new SearchResult<GPNode>(new Constant(this.state, bestSoFarValue), bestSoFarError);
	}

	/*private Iterable<Double> getPointsOfInterest(final NavigableSet<Double> values) {
		return new Iterable<Double>() {
			@Override
			public Iterator<Double> iterator() {
				return new Iterator<Double>() {

					private Iterator<Double> valIterator = values.iterator();
					private Double last;
					private Double next;

					@Override
					public boolean hasNext() {
						// (this.last != null && valIterator.hasNext()) || this.next != null || (this.last == null && this.next == null && valIterator.hasNext());
						assert ((this.last != null && valIterator.hasNext()) || this.next != null || (this.last == null
								&& this.next == null && valIterator.hasNext())) == (valIterator.hasNext() || next != null);

						return valIterator.hasNext() || next != null;
					}

					@Override
					public Double next() {
						if (this.last != null) {
							this.next = valIterator.next();
							Double output = (last + this.next) / 2;
							this.last = null;
							return output;
						} else if (this.next != null) {
							this.last = this.next;
							this.next = null;
							return this.last;
						} else {
							this.last = valIterator.next();
							return this.last;
						}
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}

				};
			}

		};
	}*/
}
