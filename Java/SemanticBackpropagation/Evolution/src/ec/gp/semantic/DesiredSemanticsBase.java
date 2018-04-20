package ec.gp.semantic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Base class for desired semantics with multiple permitted values for each fitness case. The values for each fitness
 * case are guaranteed to be sorted ascending.
 * 
 * @author Tomasz Pawlak
 * 
 * @param <SemType>
 */
public class DesiredSemanticsBase<SemType> {

	private final List<TreeSet<SemType>> values = new ArrayList<TreeSet<SemType>>();
	private final Cmp cmp = new Cmp();

	public DesiredSemanticsBase() {

	}

	public DesiredSemanticsBase(final ISemantics other) {
		this.prepareCollection(other.size() - 1);

		int i = 0;
		for (final Object v : other) {
			this.addValuesFast(i++, (SemType) v);
		}
	}

	public DesiredSemanticsBase(final SemType... other) {
		this.prepareCollection(other.length - 1);

		int i = 0;
		for (final SemType v : other) {
			this.addValuesFast(i++, v);
		}
	}

	public DesiredSemanticsBase(final double... other) {
		this.prepareCollection(other.length - 1);

		int i = 0;
		for (final double v : other) {
			this.addValuesFast(i++, (SemType) (Double) v);
		}
	}

	private void prepareCollection(final int fitnessCase) {
		while (this.values.size() <= fitnessCase)
			this.values.add(new TreeSet<SemType>(this.cmp));
	}

	public void addValues(final int fitnessCase, final SemType... values) {
		this.prepareCollection(fitnessCase);
		this.addValuesFast(fitnessCase, values);
	}

	private void addValuesFast(final int fitnessCase, final SemType... values) {
		assert values != null;

		final Set<SemType> set = this.values.get(fitnessCase);
		for (final SemType value : values) {
			set.add(value);
		}
	}

	public void markInconsistent(int fitnessCase) {
		this.prepareCollection(fitnessCase);

		Set<SemType> set = this.values.get(fitnessCase);
		set.clear();
		set.add(null);
	}

	public void markDontCare(int fitnessCase) {
		this.prepareCollection(fitnessCase);

		Set<SemType> set = this.values.get(fitnessCase);
		set.clear();
	}

	/**
	 * If set is empty, then we `don't care' what value is at this position, if set contains only null, then the
	 * semantics is `inconsistent' at this position, otherwise set contains all possible values for this position.
	 * 
	 * @param fitnessCase
	 * @return
	 */
	public NavigableSet<SemType> getValuesFor(int fitnessCase) {
		this.prepareCollection(fitnessCase);

		NavigableSet<SemType> set = this.values.get(fitnessCase);

		assert set != null;
		return set;
	}

	public int size() {
		return this.values.size();
	}

	@Override
	public int hashCode() {
		int hash = 0;
		int i = 0;
		for (NavigableSet<SemType> value : values) {
			hash ^= value.hashCode() << i;
		}
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DesiredSemanticsBase<?>))
			return false;

		DesiredSemanticsBase<SemType> other = (DesiredSemanticsBase<SemType>) obj;
		if (this.values.size() != other.values.size())
			return false;

		for (int i = 0; i < this.values.size(); ++i) {
			if (!this.values.get(i).equals(other.values.get(i))) {
				return false;
			}
		}

		assert this.hashCode() == obj.hashCode();
		return true;
	}

	private class Cmp implements Comparator<SemType> {

		@Override
		public final int compare(final SemType o1, final SemType o2) {
			if (o1 == o2) //null == null, otherwise references equals
				return 0;
			else if (o1 == null)
				return -1;
			else if (o2 == null)
				return 1;

			return ((Comparable) o1).compareTo(o2);
		}

	}
}
