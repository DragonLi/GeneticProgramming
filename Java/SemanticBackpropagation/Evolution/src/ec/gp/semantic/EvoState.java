package ec.gp.semantic;

import java.util.HashSet;

import ec.EvolutionState;
import ec.gp.semantic.library.ILibrary;
import ec.gp.semantic.library.ILibraryFactory;
import ec.gp.semantic.utils.IFinishListener;
import ec.simple.SimpleEvolutionState;
import ec.util.Checkpoint;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

public class EvoState extends SimpleEvolutionState {

	private final static Parameter TIME_LIMIT = new Parameter("timeLimit");
	private final static Parameter LIBRARY_FACTORY = new Parameter("libraryFactory");
	private final static Parameter LOAD_LIBRARY = new Parameter("loadLibrary");

	protected long timeLimit = -1;
	protected long timeStarted = -1;
	protected ILibrary<?> library;
	protected HashSet<IFinishListener> finishListeners = new HashSet<IFinishListener>();

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);

		try {

			this.timeLimit = state.parameters.getLongWithDefault(TIME_LIMIT, null, -1);
			this.timeStarted = System.currentTimeMillis();

			ParameterDatabase params = state.parameters;

			Class<?> libFactoryClass = (Class<?>) params.getClassForParameter(LIBRARY_FACTORY, null,
					ILibraryFactory.class);

			if (params.getBoolean(LOAD_LIBRARY, null, true)) {
				ILibraryFactory<?> libFactory = (ILibraryFactory<?>) libFactoryClass.newInstance();
				this.library = libFactory.getLibrary(state, new Parameter("state"));
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public ILibrary<?> getLibrary() {
		return this.library;
	}

	@Override
	public int evolve() {
		if (generation > 0)
			output.message("Generation " + generation);

		// EVALUATION
		statistics.preEvaluationStatistics(this);
		evaluator.evaluatePopulation(this);
		statistics.postEvaluationStatistics(this);

		// SHOULD WE QUIT?
		if (evaluator.runComplete(this) && quitOnRunComplete) {
			output.message("Found Ideal Individual");
			return R_SUCCESS;
		}

		// SHOULD WE QUIT?
		if (generation >= numGenerations - 1 && System.currentTimeMillis() > timeStarted + timeLimit) {
			return R_FAILURE;
		}

		// PRE-BREEDING EXCHANGING
		statistics.prePreBreedingExchangeStatistics(this);
		population = exchanger.preBreedingExchangePopulation(this);
		statistics.postPreBreedingExchangeStatistics(this);

		String exchangerWantsToShutdown = exchanger.runComplete(this);
		if (exchangerWantsToShutdown != null) {
			output.message(exchangerWantsToShutdown);
			/*
			 * Don't really know what to return here. The only place I could find where runComplete ever returns
			 * non-null is IslandExchange. However, that can return non-null whether or not the ideal individual was
			 * found (for example, if there was a communication error with the server).
			 * 
			 * Since the original version of this code didn't care, and the result was initialized to R_SUCCESS before
			 * the while loop, I'm just going to return R_SUCCESS here.
			 */

			return R_SUCCESS;
		}

		// BREEDING
		statistics.preBreedingStatistics(this);

		population = breeder.breedPopulation(this);

		// POST-BREEDING EXCHANGING
		statistics.postBreedingStatistics(this);

		// POST-BREEDING EXCHANGING
		statistics.prePostBreedingExchangeStatistics(this);
		population = exchanger.postBreedingExchangePopulation(this);
		statistics.postPostBreedingExchangeStatistics(this);

		// INCREMENT GENERATION AND CHECKPOINT
		generation++;
		if (checkpoint && generation % checkpointModulo == 0) {
			output.message("Checkpointing");
			statistics.preCheckpointStatistics(this);
			Checkpoint.setCheckpoint(this);
			statistics.postCheckpointStatistics(this);
		}

		return R_NOTDONE;
	}

	@Override
	public void finish(int result) {
		super.finish(result);
		
		for(IFinishListener listener : this.finishListeners) {
			listener.finish(result);
		}
	}
	
	public void addFinishListener(IFinishListener listener) {
		this.finishListeners.add(listener);
	}
	
	public void removeFinishListener(IFinishListener listener) {
		this.finishListeners.remove(listener);
	}

}
