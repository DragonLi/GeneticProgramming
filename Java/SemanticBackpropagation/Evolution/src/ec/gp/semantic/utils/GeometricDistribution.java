package ec.gp.semantic.utils;

import ec.EvolutionState;
import ec.Prototype;
import ec.util.Parameter;

public class GeometricDistribution implements Prototype {

	private final static Parameter DEFAULT_BASE = new Parameter("GeometricDistribution");

	protected static final String PROB = "prob";

	private EvolutionState state;
	protected double prob = 0.01;
	private double invLog1MinusProb = -99.49916247;

	@Override
	public Parameter defaultBase() {
		return DEFAULT_BASE;
	}

	@Override
	public Object clone() {
		GeometricDistribution copy = new GeometricDistribution();
		copy.state = this.state;
		copy.prob = this.prob;

		return copy;
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		this.state = state;
		this.prob = state.parameters.getDoubleWithDefault(base.push(PROB), defaultBase().push(PROB), this.prob);
		this.invLog1MinusProb = 1.0 / Math.log(1 - this.prob);
	}

	/**
	 * Returns random number from geometric distribution from 0 to max inclusive.
	 * 
	 * @param thread
	 * @param max
	 *            Maximum [inclusive]
	 * @return
	 */
	public int next(final int thread, final int max) {
		final double x = state.random[thread].nextDouble();
		final int k = (int) Math.min(Math.ceil(Math.log(1 - x) * invLog1MinusProb), max);

		return k;
	}

}
