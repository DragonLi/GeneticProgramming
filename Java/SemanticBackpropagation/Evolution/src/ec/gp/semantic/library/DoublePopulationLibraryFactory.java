package ec.gp.semantic.library;

import library.distance.IDistanceTo;
import ec.EvolutionState;
import ec.gp.semantic.DesiredSemanticsBase;
import ec.util.Parameter;

public class DoublePopulationLibraryFactory implements IDistanceToFactory<double[]>, ILibraryFactory<Double> {

	@Override
	public ILibrary<Double> getLibrary(EvolutionState state, Parameter base) {
		return new PopulationLibrary<Double, double[]>(state, this, new DoubleConstantGenerator(state));
	}

	@Override
	public IDistanceTo<double[]> getDistanceToSet(EvolutionState state, DesiredSemanticsBase<?> desiredSemantics) {
		return new DoubleDistanceToSet(state, (DesiredSemanticsBase<Double>) desiredSemantics);
	}

}
