package ec.gp.semantic.utils;

import ec.util.MersenneTwisterFast;

public class MultivariateGaussianDistribution {

	protected final int dimension;
	protected final MersenneTwisterFast randomGenerator;
	protected final double[] mean;
	protected final double[][] covariance;
	private double[][] covarianceL;

	public MultivariateGaussianDistribution(MersenneTwisterFast random, double[] mean) {
		this(random, mean, SimpleMatrix.matrixIdentity(mean.length));
	}

	public MultivariateGaussianDistribution(MersenneTwisterFast random, Double[] mean) {
		this(random, mean, SimpleMatrix.matrixIdentity(mean.length));
	}

	public MultivariateGaussianDistribution(MersenneTwisterFast random, double[] mean, double[][] covariance) {
		this.dimension = mean.length;
		this.mean = mean;
		this.covariance = covariance;
		this.randomGenerator = random;

		this.covarianceL = SimpleMatrix.decomposeCholesky(covariance);
	}

	public MultivariateGaussianDistribution(MersenneTwisterFast random, Double[] mean, double[][] covariance) {
		this(random, cast(mean), covariance);
	}

	private static double[] cast(Double[] array) {
		double[] casted = new double[array.length];
		System.arraycopy(array, 0, casted, 0, array.length);
		return casted;
	}

	public double[] next() {
		double[] d = new double[dimension];

		for (int i = 0; i < dimension; i++)
			d[i] = randomGenerator.nextGaussian();

		return SimpleMatrix.plus(SimpleMatrix.times(covarianceL, d), mean);
	}
}
