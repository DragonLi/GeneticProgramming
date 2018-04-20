package library.semantics;

import java.util.Arrays;

public final class TestCase<TData> {

	protected TData[] arguments;
	protected TData value;

	public TestCase() {

	}

	public TestCase(TData value, TData... arguments) {
		this();
		setValue(value);
		setArguments(arguments);
	}

	public TData[] getArguments() {
		return arguments;
	}

	public void setArguments(TData... args) {
		arguments = args;
	}

	public TData getValue() {
		return value;
	}

	public void setValue(TData val) {
		value = val;
	}
	
	@Override
	public String toString(){
		return String.format("%s -> %s", Arrays.toString(arguments), value.toString());
	}
}
