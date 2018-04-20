package ec.gp.semantic;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Base class for semantics. The values of semantics are immutable!
 * 
 * @author Tomasz Pawlak
 * @param <TSemStore>
 */
public abstract class SemanticsBase<TSemStore> implements ISemantics {

	protected TSemStore value;

	public SemanticsBase(TSemStore values) {
		this.value = values;
	}

	@Override
	public SemanticsBase<TSemStore> clone() {
		try {
			return (SemanticsBase<TSemStore>) super.clone();
		} catch (CloneNotSupportedException e) {
			assert false : "this should never happen";
			return null;
		}
	}

	@Override
	public boolean isBetween(ISemantics semantics1, ISemantics semantics2) {
		SemanticsBase<?> sem1 = (SemanticsBase<?>) semantics1;
		SemanticsBase<?> sem2 = (SemanticsBase<?>) semantics2;

		return (sem1.compareTo(this) <= 0 && this.compareTo(sem2) <= 0)
				|| (sem2.compareTo(this) <= 0 && this.compareTo(sem1) <= 0);

		/*for (int i = 0; i < sem1.values.length; ++i) {
			// (sem1[i] < center[i] && center[i] < sem2[i]) || (sem2[i] < center[i] && center[i] < sem1[i])
			if ((sem1.values[i] >= this.values[i] || this.values[i] >= sem2.values[i])
					&& (sem2.values[i] >= this.values[i] || this.values[i] >= sem1.values[i]))
				return false;
		}*/
	}

	@Override
	public double distanceTo(ISemantics other, double p) {
		return Math.pow(this.fastDistanceTo(other, p), 1.0 / p);
	}

	public Object getValue(){
		return this.value;
	}
}
