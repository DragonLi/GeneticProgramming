package ec.gp.semantic;

/**
 * 
 * @author Tomasz Pawlak
 */
public interface ISemantics extends Cloneable, Comparable<ISemantics>, Iterable {

	public Object getValue();

	ISemantics clone();

	ISemantics getMidpointBetweenMeAnd(ISemantics other);

	Object getValue(int fitnessCase);

	boolean isBetween(ISemantics semantics1, ISemantics semantics2);

	/**
	 * Calculates Minkowski distance Lp, for a given p.
	 * 
	 * @param other
	 * @param p
	 * @return
	 */
	double distanceTo(ISemantics other, double p);

	/**
	 * Calculates Minkowski distance Lp, for a given p, without calculating the root. I.e. the monotonicity would be
	 * maintained, however the absolute value not.
	 * 
	 * @param other
	 * @param p
	 * @return
	 */
	double fastDistanceTo(ISemantics other, double p);

	/**
	 * Calculates distance according to the 'default' metric for a given domain.
	 * 
	 * @param other
	 * @return
	 */
	double distanceTo(ISemantics other);

	/**
	 * Calculates distance according to the 'default' metric for a given domain, without calculating the root. I.e. the
	 * monotonicity would be maintained, however the absolute value not.
	 * 
	 * @param other
	 * @return
	 */
	double fastDistanceTo(ISemantics other);

	/**
	 * Calculates semantics that is counterpoint of given semantics w.r.t. this semantics. Assuming a metric d, the
	 * counterpoint fulfills the equation d(other, this) + d(this, calculated) = d(other, calculated).
	 * 
	 * @param other
	 * @return
	 */
	ISemantics counterpointTo(ISemantics other);
	
	int size();
}
