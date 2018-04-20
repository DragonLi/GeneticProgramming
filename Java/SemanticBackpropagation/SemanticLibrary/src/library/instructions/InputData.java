package library.instructions;

public class InputData<TData> implements Cloneable {

	protected TData[] x;

	public TData[] getX() {
		return x;
	}

	public void setX(final TData... x) {
		this.x = x;
	}

	@Override
	public InputData<TData> clone() {
		InputData<TData> cloned = new InputData<TData>();
		cloned.x = this.x.clone();
		return cloned;
	}
}
