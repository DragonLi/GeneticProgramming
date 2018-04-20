package ec.gp.semantic.utils;

import ec.EvolutionState;
import ec.Prototype;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;

public class PoissonDistribution implements Prototype {

	private final static Parameter DEFAULT_BASE = new Parameter("PoissonDistribution");

	protected static final String LAMBDA = "lambda";

	private EvolutionState state;

	protected double lambda = 2.5;
	private double expMinusLambda = 0.0820849986;

	@Override
	public Parameter defaultBase() {
		return DEFAULT_BASE;
	}

	@Override
	public Object clone() {
		PoissonDistribution copy = new PoissonDistribution();
		copy.state = this.state;
		copy.lambda = this.lambda;
		copy.expMinusLambda = this.expMinusLambda;
		
		return copy;
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		this.state = state;
		this.lambda = state.parameters.getDoubleWithDefault(base.push(LAMBDA), defaultBase().push(LAMBDA), this.lambda);
		this.expMinusLambda = Math.exp(-this.lambda);
	}

	/**
	 * Returns random number from Poisson distribution, from 0 to max inclusive.
	 * @param thread
	 * @param max Maximum [inclusive]
	 * @return
	 */
	public int next(final int thread, final int max) {
		final double p = state.random[thread].nextDouble();
		double cumulativeP = expMinusLambda;
		int k;
		int kFact = 1;
		double lambdaPow = 1.0;
		for (k = 1; k < max; ++k) {
			if (p <= cumulativeP)
				break;

			kFact *= k;
			lambdaPow *= this.lambda;
			cumulativeP += lambdaPow * expMinusLambda / kFact;
		}

		return k;
	}

}
