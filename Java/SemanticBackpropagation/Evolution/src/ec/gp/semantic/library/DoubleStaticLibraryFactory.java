package ec.gp.semantic.library;

import library.distance.IDistanceTo;
import ec.EvolutionState;
import ec.gp.semantic.DesiredSemanticsBase;
import ec.util.Parameter;

public class DoubleStaticLibraryFactory implements ILibraryFactory<Double>, IDistanceToFactory<double[]> {

	@Override
	public ILibrary<Double> getLibrary(EvolutionState state, Parameter base) {
		return new StaticLibrary<Double, double[]>(state, base, this, new DoubleConstantGenerator(state));
	}

	@Override
	public IDistanceTo<double[]> getDistanceToSet(EvolutionState state, DesiredSemanticsBase<?> desiredSemantics) {
		return new DoubleDistanceToSet(state, (DesiredSemanticsBase<Double>)desiredSemantics);
	}
	
}
