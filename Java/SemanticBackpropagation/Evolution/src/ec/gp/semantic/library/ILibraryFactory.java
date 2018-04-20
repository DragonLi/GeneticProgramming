package ec.gp.semantic.library;

import ec.EvolutionState;
import ec.util.Parameter;

public interface ILibraryFactory<SemType> {

	ILibrary<SemType> getLibrary(EvolutionState state, Parameter base);
	
}
