package ec.gp.semantic.library;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import library.distance.IDistanceTo;
import library.generator.TreeGenerator;
import library.generator.TreeNode;
import library.instructions.InstructionBase;
import library.semantics.BitwiseSemantics;
import library.semantics.IntervalSemantics;
import library.semantics.ProgramSemanticsPair;
import library.semantics.TestCase;
import library.semantics.UniquenessFilter;
import library.semantics.VectorSemantics;
import library.space.MetricSpaceParallel;
import library.space.SearchResult;
import library.space.SpaceWrapper;
import ec.EvolutionState;
import ec.app.semanticGP.func.TreeConverter;
import ec.gp.GPNode;
import ec.gp.semantic.DesiredSemanticsBase;
import ec.gp.semantic.ISemanticProblem;
import ec.gp.semantic.ISemantics;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

public class StaticLibrary<SemType, TSemStore> implements ILibrary<SemType>, Serializable {

	private static transient final String DEFAULT_BASE = "library";
	private static transient final String INSTRUCTIONS = "instructions";
	private static transient final String SIZE = "size";
	private static transient final String MAX_CHILD_DEPTH = "maxChildDepth";

	private static transient final String UNIQUENESS_FILTERING = "uniquenessFiltering";

	protected SpaceWrapper<TSemStore> space;
	protected transient TreeConverter converter;
	protected ArrayList<TreeNode> programs;

	/**
	 * Key - tree height, Value - program
	 */
	protected final TreeMap<Integer, ArrayList<TreeNode>> programsHierarchical = new TreeMap<Integer, ArrayList<TreeNode>>();
	/**
	 * Number of programs, whose height is less or equal to the given key.
	 */
	protected int[] leqProgramCount;

	protected final IDistanceToFactory<TSemStore> distanceToFactory;
	protected final IConstantGenerator<SemType> constantGenerator;
	protected final EvolutionState state;

	protected Parameter defaultBase() {
		return new Parameter(DEFAULT_BASE);
	}

	StaticLibrary(final EvolutionState state, final Parameter base, IDistanceToFactory<TSemStore> distanceToFactory,
			IConstantGenerator<SemType> constantGenerator) {
		try {
			this.distanceToFactory = distanceToFactory;
			this.constantGenerator = constantGenerator;
			this.converter = new TreeConverter(state);
			this.state = state;

			long sTime;

			state.output.message("Generating programs...");
			Iterable<TreeNode> programs = this.generatePrograms(base, state.parameters);
			this.programs = new ArrayList<TreeNode>();

			state.output.message("Computing semantics...");
			Iterable<ProgramSemanticsPair> semantics = this.computeSemantics(base, state, programs);
			List<VectorSemantics<?, ?>> semCollection = new ArrayList<VectorSemantics<?, ?>>();

			if (state.parameters.getBoolean(defaultBase().push(UNIQUENESS_FILTERING), null, true)) {
				state.output.message("Filtering non-unique programs...");
				sTime = System.currentTimeMillis();
				long originalCount = UniquenessFilter.filter(semantics, this.programs, semCollection);
				state.output.message(String.format("Orignal program number: %d, new program number: %d, time=%.2fs",
						originalCount, this.programs.size(), (double) (System.currentTimeMillis() - sTime) * 0.001));
			} else {
				state.output.message("Skipping uniqueness filtering...");

				for (ProgramSemanticsPair pair : semantics) {
					this.programs.add(pair.program);
					semCollection.add(pair.semantics);
				}
			}

			for (TreeNode program : this.programs) {
				int height = program.getHeight();
				ArrayList<TreeNode> programsOfHeight = this.programsHierarchical.get(height);
				if (programsOfHeight == null) {
					programsOfHeight = new ArrayList<TreeNode>();
					this.programsHierarchical.put(height, programsOfHeight);
				}
				programsOfHeight.add(program);
			}

			int maxHeight = this.programsHierarchical.lastKey();
			int sum = 0;

			this.leqProgramCount = new int[maxHeight + 1];

			for (int h = 1; h <= maxHeight; ++h) {
				ArrayList<TreeNode> list = this.programsHierarchical.get(h);
				if (list != null)
					sum += list.size();

				this.leqProgramCount[h] = sum;
			}

			state.output.message("Building metric space...");
			sTime = System.currentTimeMillis();
			this.space = new CachedSpaceWrapper(this.programs, new MetricSpaceParallel(this.programs, semCollection));
			state.output.message(String.format("Metric space built, time=%.2fs",
					(double) (System.currentTimeMillis() - sTime) * 0.001));

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private Iterable<TreeNode> generatePrograms(final Parameter base, final ParameterDatabase db)
			throws InstantiationException, IllegalAccessException {
		TreeGenerator generator = new TreeGenerator();

		int maxDepth = db.getIntWithDefault(base.push(MAX_CHILD_DEPTH), defaultBase().push(MAX_CHILD_DEPTH), 3);
		generator.setDepth(maxDepth);

		state.output.message("Max tree height: " + maxDepth);

		// read classes
		Parameter instructions = base.push(INSTRUCTIONS);
		Parameter defaultInstructions = defaultBase().push(INSTRUCTIONS);
		int count = db.getInt(instructions.push(SIZE), defaultInstructions.push(SIZE));
		for (int i = 0; i < count; ++i) {
			Class<?> cInstruction = (Class<?>) db.getClassForParameter(instructions.push(new Integer(i).toString()),
					defaultInstructions.push(new Integer(i).toString()), InstructionBase.class);

			System.out.println("Adding instruction: " + cInstruction);

			InstructionBase<?> instruction = (InstructionBase<?>) cInstruction.newInstance();

			if (instruction.getNumberOfArguments() == 0) {
				generator.addTerminals(instruction);
			} else {
				generator.addNonterminals(instruction);
			}
		}

		return generator;
	}

	private Iterable<ProgramSemanticsPair> computeSemantics(final Parameter base, final EvolutionState state,
			final Iterable<TreeNode> programs) {

		Exception firstException = null;
		Exception secondException = null;

		try {
			final List<TestCase<Double>> testCases = ((ISemanticProblem<Double>) state.evaluator.p_problem)
					.getFitnessCases();

			// This line is extremely important, it will cause exception if type of value is other then Double.
			// The really strange thing here is the line above. Believe me or not, but it would not throw an exception if test cases actually contain Booleans.
			// Java is weird...
			final Double v = testCases.get(0).getValue();

			final Iterator<TreeNode> progIterator = programs.iterator();

			return new Iterable<ProgramSemanticsPair>() {
				@Override
				public Iterator<ProgramSemanticsPair> iterator() {
					return new Iterator<ProgramSemanticsPair>() {

						@Override
						public boolean hasNext() {
							return progIterator.hasNext();
						}

						@Override
						public ProgramSemanticsPair next() {
							TreeNode program = progIterator.next();
							VectorSemantics<?, ?> semantics = new IntervalSemantics(program, testCases);
							return new ProgramSemanticsPair(program, semantics);
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}

					};
				}
			};
		} catch (Exception ex) {
			firstException = ex;
		}

		try {
			final List<TestCase<Boolean>> testCases = ((ISemanticProblem<Boolean>) state.evaluator.p_problem)
					.getFitnessCases();
			final Iterator<TreeNode> progIterator = programs.iterator();

			return new Iterable<ProgramSemanticsPair>() {
				@Override
				public Iterator<ProgramSemanticsPair> iterator() {
					return new Iterator<ProgramSemanticsPair>() {

						@Override
						public boolean hasNext() {
							return progIterator.hasNext();
						}

						@Override
						public ProgramSemanticsPair next() {
							TreeNode program = progIterator.next();
							VectorSemantics<?, ?> semantics = new BitwiseSemantics(program, testCases);
							return new ProgramSemanticsPair(program, semantics);
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}

					};
				}
			};
		} catch (Exception ex) {
			secondException = ex;
		}

		state.output.error(firstException.toString());
		state.output.error(secondException.toString());
		throw new RuntimeException(firstException);
	}

	public SearchResult<GPNode> getProgram(final DesiredSemanticsBase<SemType> semantics, final int maxHeight) {
		return this.getPrograms(semantics, 1, maxHeight).get(0);
	}

	public List<SearchResult<GPNode>> getPrograms(final DesiredSemanticsBase<SemType> semantics, final int count,
			final int maxHeight) {

		IDistanceTo<TSemStore> distanceMeter = this.distanceToFactory.getDistanceToSet(this.state, semantics);
		List<SearchResult<TreeNode>> ids = this.space.getNearestPrograms(distanceMeter, count, maxHeight);
		SearchResult<GPNode>[] gpTrees = (SearchResult<GPNode>[]) new SearchResult<?>[ids.size()];

		int i = 0;
		for (final SearchResult<TreeNode> id : ids) {
			GPNode gpNode = this.converter.convert(id.getProgram());
			gpTrees[i++] = new SearchResult(gpNode, id.getError());
		}

		SearchResult<GPNode> bestConstant = this.constantGenerator.getPerfectConstant(semantics);

		int insertPos = Arrays.binarySearch(gpTrees, bestConstant);

		if (insertPos < 0)
			insertPos = -insertPos - 1;

		if (insertPos < gpTrees.length) {
			for (int p = gpTrees.length - 1; p > insertPos; ++p)
				gpTrees[p] = gpTrees[p - 1];

			gpTrees[insertPos] = bestConstant;
		}

		return Arrays.asList(gpTrees);
	}

	public SearchResult<GPNode> getKthProgram(final DesiredSemanticsBase<SemType> semantics, final int k,
			final int maxHeight) {

		IDistanceTo<TSemStore> distanceMeter = this.distanceToFactory.getDistanceToSet(this.state, semantics);
		final List<SearchResult<TreeNode>> ids = this.space.getNearestPrograms(distanceMeter, k + 1, maxHeight);

		SearchResult<GPNode> bestConstant = this.constantGenerator.getPerfectConstant(semantics);

		int insertPos = Collections.binarySearch(ids, bestConstant);

		if (insertPos < 0)
			insertPos = -insertPos - 1;

		// we'are exactly at k position, or number of returned programs from library is lower than k and constant is the last value
		if (insertPos == k || (ids.size() <= k && insertPos == ids.size())) {
			return bestConstant;
		}

		final SearchResult<TreeNode> id = ids.get(ids.size() > k ? k : ids.size() - 1);
		final GPNode gpNode = this.converter.convert(id.getProgram());
		return new SearchResult<GPNode>(gpNode, id.getError());
	}

	public GPNode getRandom(final MersenneTwisterFast random, final int maxHeight) {
		int index = random.nextInt(this.size(maxHeight));
		TreeNode program = null;

		for (ArrayList<TreeNode> bucket : this.programsHierarchical.values()) {
			if (index >= bucket.size()) {
				index -= bucket.size();
				continue;
			}

			program = bucket.get(index);
		}

		return this.converter.convert(program);
	}

	@Override
	public double calculateError(DesiredSemanticsBase<SemType> desiredSemantics, ISemantics semantics) {
		return this.distanceToFactory.getDistanceToSet(this.state, desiredSemantics).getDistanceTo(
				(TSemStore) semantics.getValue());
	}

	@Override
	public int size() {
		return this.programs.size();
	}

	@Override
	public int size(int maxDepth) {
		assert maxDepth > 0;

		if (maxDepth >= this.leqProgramCount.length)
			return this.size();

		return this.leqProgramCount[maxDepth];
	}
}
