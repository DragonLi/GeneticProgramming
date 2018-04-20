package library.instructions;

public class ExtendedInputData<TData> extends InputData<TData> {

	protected TData[] artificialVariables;

	public TData[] getArtificialVariables() {
		return artificialVariables;
	}

	public void setArtificialVariables(final TData[] args) {
		artificialVariables = args;
	}
	
	@Override
	public ExtendedInputData<TData> clone(){
		ExtendedInputData<TData> cloned = (ExtendedInputData<TData>) super.clone();
		cloned.artificialVariables = this.artificialVariables.clone();
		return cloned;
	}

}
