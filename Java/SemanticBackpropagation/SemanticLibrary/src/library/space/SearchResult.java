package library.space;

public class SearchResult<ProgramType> implements Comparable<SearchResult<?>> {

	private final ProgramType program;
	private final double error;

	public SearchResult(ProgramType program, double error) {
		this.program = program;
		this.error = error;
	}

	public ProgramType getProgram() {
		return this.program;
	}

	public double getError() {
		return this.error;
	}

	@Override
	public int compareTo(SearchResult<?> o) {
		return Double.compare(this.error, o.error);
	}

}
