package library.distance;


public final class ManhattanDistance implements IDistanceTo<double[]> {

	private final double[] curSemantics;

	public ManhattanDistance(final double[] curSemantics) {
		this.curSemantics = curSemantics;
	}

	@Override
	public double getDistanceTo(final double[] semantics) {
		double diff;
		double sum = 0.0;
		for (int k = 0; k < semantics.length; ++k) {
			diff = semantics[k] - curSemantics[k];
			sum += Math.abs(diff);
		}
		return sum;
	}

}