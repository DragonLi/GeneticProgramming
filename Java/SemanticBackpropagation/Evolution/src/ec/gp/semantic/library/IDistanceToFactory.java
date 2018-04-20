package ec.gp.semantic.library;

import library.distance.IDistanceTo;
import ec.EvolutionState;
import ec.gp.semantic.DesiredSemanticsBase;

public interface IDistanceToFactory<SemType> {

	IDistanceTo<SemType> getDistanceToSet(EvolutionState state, DesiredSemanticsBase<?> desiredSemantics);
	
}
