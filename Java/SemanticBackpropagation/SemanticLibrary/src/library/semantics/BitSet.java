package library.semantics;

public final class BitSet extends java.util.BitSet {

	private final int length;
	
	public BitSet() {
		super();
		this.length = 0;
	}
	
	public BitSet(int capacity) {
		super(capacity);
		this.length = capacity;
	}

	@Override
	public int length() {
		return this.length;
	}
}
