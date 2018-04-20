package ec.gp.semantic;

import java.util.Iterator;

import library.semantics.BitSet;
import ec.util.MersenneTwisterFast;

public class BooleanSemantics extends SemanticsBase<BitSet> {

	private static MersenneTwisterFast random = new MersenneTwisterFast(0);
	private final int hashCode;

	public BooleanSemantics(BitSet values) {
		super(values);
		this.hashCode = values.hashCode();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
		SemanticsBase<BitSet> other;
		try {
			other = (SemanticsBase<BitSet>) obj;
		} catch (ClassCastException e) {
			return false;
		}

		boolean result = this.value.equals(other.value);
		assert !result || this.hashCode() == other.hashCode();
		return result;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public SemanticsBase<BitSet> clone() {
		SemanticsBase<BitSet> copy = (SemanticsBase<BitSet>) super.clone();
		copy.value = (BitSet)this.value.clone();
		return copy;
	}

	@Override
	public Boolean getValue(int fitnessCase) {
		assert fitnessCase < this.value.length();
		return this.value.get(fitnessCase);
	}

	@Override
	public ISemantics getMidpointBetweenMeAnd(ISemantics _other) {
		BooleanSemantics other = (BooleanSemantics) _other;
		Boolean lastFromThis = random.nextBoolean();

		assert this.value.length() == other.value.length();

		BitSet midpoint = new BitSet(this.value.length());
		for (int i = 0; i < this.value.length(); ++i) {
			// this procedure tries to equalize number of bits rewritten from both parents
			// by rewriting value once from one parent and once from the other

			// the commented code and the code below are equivalent, however the latter
			// one does not involve code branching

			// @formatter:off
			/*if (this.values[i] == other.values[i]) {
				midpoint[i] = this.values[i];
			} else {	
				midpoint[i] = lastFromThis ? other.values[i] : this.values[i];
				lastFromThis = !lastFromThis;
			}*/
			// @formatter:on

			midpoint.set(i, (lastFromThis && this.value.get(i)) || (!lastFromThis && other.value.get(i)));
			lastFromThis = lastFromThis ^ this.value.get(i) ^ other.value.get(i);
		}

		return new BooleanSemantics(midpoint);
	}

	@Override
	public int compareTo(ISemantics o) {
		BooleanSemantics other = (BooleanSemantics) o;

		assert this.value.length() == other.value.length();

		for (int i = 0; i < this.value.length(); ++i) {
			int cmp = Boolean.compare(this.value.get(i), other.value.get(i));
			if (cmp != 0)
				return cmp;
		}

		return 0;
	}

	/**
	 * Calculates the distance according to the Hamming metric.
	 */
	@Override
	public double distanceTo(ISemantics other) {
		return this.distanceTo(other, 1);
	}

	/**
	 * Calculates the distance according to the Hamming metric.
	 */
	@Override
	public double fastDistanceTo(ISemantics other) {
		return this.fastDistanceTo(other, 1);
	}

	@Override
	public double fastDistanceTo(ISemantics _other, double p) {
		SemanticsBase<BitSet> other = (SemanticsBase<BitSet>) _other;

		assert this.value.length() == other.value.length();

		double distance = 0.0;
		for (int i = 0; i < this.value.length(); ++i) {
			if (this.value.get(i) != other.value.get(i))
				distance += 1.0;
		}

		return distance;
	}

	@Override
	public ISemantics counterpointTo(ISemantics o) {
		// it cannot be other point than `this', consider all possible cases:
		// this = 0, o = 0 => counterpoint = 0
		// this = 0, o = 1 => counterpoint = -1, but we cannot set it to -1, so 0
		// this = 1, o = 0 => counterpoint = 2, but we cannot set it to 2, so 1
		// this = 1, o = 1 => counterpoint = 1
		return this.clone();
	}

	public Iterator<Boolean> iterator() {
		return new Iterator<Boolean>() {

			private int index = 0;

			@Override
			public boolean hasNext() {
				return index < size();
			}

			@Override
			public Boolean next() {
				return value.get(index++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	@Override
	public int size() {
		return this.value.length();
	}

	@Override
	public String toString() {
		return this.value.toString();//Arrays.toString(this.value);
	}
}
