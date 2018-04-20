package library.semantics;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import library.generator.TreeNode;

public abstract class VectorSemantics<TSemantics, TStore> {

	protected TStore vector;
	protected List<TestCase<TSemantics>> testCases;

	public TStore getSemantics() {
		return vector;
	}

	@Override
	public abstract boolean equals(final Object second);

	public abstract boolean equals(final VectorSemantics<?, ?> second, final double maxError);

	@Override
	public abstract int hashCode();

	@SuppressWarnings("unchecked")
	@Override
	public VectorSemantics<TSemantics, TStore> clone() {
		try {
			VectorSemantics<TSemantics, TStore> cloned = this.getClass().newInstance();
			if (vector != null)
				cloned.vector = (TStore) Arrays.copyOf((Object[])vector, ((Object[])vector).length);

			return cloned;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public VectorSemantics(TreeNode program, List<TestCase<TSemantics>> testCases) {
		this.testCases = testCases;
		this.computeSemantics(program, testCases);
	}

	public void computeSemantics(final TreeNode program, List<TestCase<TSemantics>> testCases) {
		this.vector = computeSemanticsInternal(program, testCases);
	}

	protected abstract TStore computeSemanticsInternal(TreeNode program, List<TestCase<TSemantics>> testCases);

	public abstract void drawSemantics(BufferedImage image, int offsetX, int offsetY, int width, int height);

	@Deprecated
	public static List<VectorSemantics<?,?>> getSemantics(final List<TreeNode> programs, final Class<?> semanticsClass,
			final double from, final double to, final double step) {

		if (semanticsClass != IntervalSemantics.class)
			throw new UnsupportedOperationException(
					"The method getSemantics is deprecated and supports only IntervalSemantics.");

		// calculate test cases
		List<TestCase<Double>> testCases = new ArrayList<TestCase<Double>>();
		for (double x = from; x <= to; x += step) {
			testCases.add(new TestCase<Double>(0.0, x));
		}

		// calculate semantics
		List<VectorSemantics<?,?>> semantics = new ArrayList<VectorSemantics<?,?>>(programs.size());

		for (int i = 0; i < programs.size(); ++i) {
			semantics.add(new IntervalSemantics(programs.get(i), testCases));
		}

		return semantics;
	}

	@Override
	public String toString() {
		return Arrays.toString((Object[])vector);
	}
}
