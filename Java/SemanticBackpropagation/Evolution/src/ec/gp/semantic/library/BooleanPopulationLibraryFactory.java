package ec.gp.semantic.library;

import java.util.BitSet;

import library.distance.IDistanceTo;
import ec.EvolutionState;
import ec.gp.semantic.DesiredSemanticsBase;
import ec.util.Parameter;

public class BooleanPopulationLibraryFactory implements ILibraryFactory<Boolean>, IDistanceToFactory<BitSet> {

	@Override
	public IDistanceTo<BitSet> getDistanceToSet(EvolutionState state, DesiredSemanticsBase<?> desiredSemantics) {
		return new BooleanDistanceToSet(state, (DesiredSemanticsBase<Boolean>) desiredSemantics);
	}

	@Override
	public ILibrary<Boolean> getLibrary(EvolutionState state, Parameter base) {
		return new PopulationLibrary<Boolean, boolean[]>(state, this, new BooleanConstantGenerator(state));
	}

}
