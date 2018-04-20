package ec.gp.semantic;

import java.util.Arrays;
import java.util.Iterator;

public class DoubleSemantics extends SemanticsBase<double[]> {

	private final int hashCode;

	public DoubleSemantics(double... values) {
		super(values);
		this.hashCode = Arrays.hashCode(values);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
		SemanticsBase<double[]> other;
		try {
			other = (SemanticsBase<double[]>) obj;
		} catch (ClassCastException e) {
			return false;
		}

		boolean result = Arrays.equals(this.value, other.value);
		assert !result || this.hashCode() == other.hashCode();
		return result;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public SemanticsBase<double[]> clone() {
		SemanticsBase<double[]> copy = (SemanticsBase<double[]>) super.clone();
		copy.value = this.value.clone();
		return copy;
	}

	@Override
	public Double getValue(int fitnessCase) {
		assert fitnessCase < this.value.length;
		return this.value[fitnessCase];
	}

	@Override
	public ISemantics getMidpointBetweenMeAnd(ISemantics _other) {
		DoubleSemantics other = (DoubleSemantics) _other;

		assert this.value.length == other.value.length;

		double[] midpoint = new double[this.value.length];
		for (int i = 0; i < this.value.length; ++i) {
			midpoint[i] = 0.5 * (this.value[i] + other.value[i]);
		}

		return new DoubleSemantics(midpoint);
	}

	@Override
	public int compareTo(ISemantics o) {
		DoubleSemantics other = (DoubleSemantics) o;

		assert this.value.length == other.value.length;

		for (int i = 0; i < this.value.length; ++i) {
			int cmp = Double.compare(this.value[i], other.value[i]);
			if (cmp != 0)
				return cmp;
		}

		return 0;
	}

	/**
	 * Calculates distance to other semantics according to the Euclidean distance.
	 * 
	 * @param other
	 * @return
	 */
	@Override
	public double distanceTo(ISemantics other) {
		return this.distanceTo(other, 2);
	}

	/**
	 * Calculates distance to other semantics according to the Euclidean distance, without calculating the root.
	 * 
	 * @param other
	 * @return
	 */
	@Override
	public double fastDistanceTo(ISemantics other) {
		return this.fastDistanceTo(other, 2);
	}

	@Override
	public double fastDistanceTo(ISemantics _other, double p) {
		SemanticsBase<double[]> other = (SemanticsBase<double[]>) _other;

		assert this.value.length == other.value.length;

		double distance = 0.0;
		for (int i = 0; i < this.value.length; ++i) {
			distance += Math.pow(Math.abs(this.value[i] - other.value[i]), p);
		}

		return distance;
	}

	@Override
	public ISemantics counterpointTo(ISemantics o) {
		DoubleSemantics other = (DoubleSemantics) o;
		double[] counterpoint = new double[this.value.length];

		assert this.value.length == other.value.length;

		for (int i = 0; i < this.value.length; ++i) {
			counterpoint[i] = this.value[i] + this.value[i] - other.value[i];
		}

		return new DoubleSemantics(counterpoint);
	}

	@Override
	public Iterator<Double> iterator() {
		return new Iterator<Double>() {

			private int index = 0;
			
			@Override
			public boolean hasNext() {
				return index < size();
			}

			@Override
			public Double next() {
				return value[index++];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
	@Override
	public int size() {
		return this.value.length;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.value);
	}
}
