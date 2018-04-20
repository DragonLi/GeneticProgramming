package ec.gp.semantic.utils;

public class Pair<V1, V2> {
	public V1 value1;
	public V2 value2;

	public Pair() {

	}

	public Pair(final V1 v1, final V2 v2) {
		this();

		this.value1 = v1;
		this.value2 = v2;
	}
}
