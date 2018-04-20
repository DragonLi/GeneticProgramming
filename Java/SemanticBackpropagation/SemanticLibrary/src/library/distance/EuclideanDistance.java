package library.distance;


public final class EuclideanDistance implements IDistanceTo<double[]> {

	private final double[] curSemantics;

	public EuclideanDistance(final double[] curSemantics) {
		this.curSemantics = curSemantics;
	}

	@Override
	public double getDistanceTo(final double[] semantics) {
		double diff;
		double sum = 0.0;
		for (int k = 0; k < semantics.length; ++k) {
			diff = semantics[k] - curSemantics[k];
			sum += diff * diff;
		}
		return Math.sqrt(sum);
	}

}