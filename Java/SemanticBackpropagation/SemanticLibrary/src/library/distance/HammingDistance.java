package library.distance;

public final class HammingDistance implements IDistanceTo<boolean[]> {

	private final boolean[] curSemantics;

	public HammingDistance(final boolean[] curSemantics) {
		this.curSemantics = curSemantics;
	}

	@Override
	public double getDistanceTo(final boolean[] semantics) {
		int distance = 0;
		for (int i = 0; i < semantics.length; ++i) {
			distance += this.curSemantics[i] == semantics[i] ? 0 : 1;
		}
		return (double) distance;
	}

}
