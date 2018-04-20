package library.semantics;

import java.awt.image.BufferedImage;
import java.util.List;

import library.generator.TreeNode;
import library.instructions.InputData;

public class BitwiseSemantics extends VectorSemantics<Boolean, BitSet> {

	public BitwiseSemantics(TreeNode program, List<TestCase<Boolean>> testCases) {
		super(program, testCases);
	}

	@Override
	public VectorSemantics<Boolean, BitSet> clone() {
		BitwiseSemantics cloned = (BitwiseSemantics) super.clone();
		return cloned;
	}

	@Override
	protected BitSet computeSemanticsInternal(final TreeNode program, List<TestCase<Boolean>> testCases) {
		InputData<Boolean> data = new InputData<Boolean>();
		int size = testCases.size();
		BitSet semantics = new BitSet(size);
		Boolean[] arguments = new Boolean[testCases.get(0).getArguments().length];

		for (int i = 0; i < size; ++i) {
			this.asArray(i, arguments);
			data.setX(arguments);
			semantics.set(i, (Boolean) program.execute(data));
		}

		return semantics;
	}

	private void asArray(final int i, final Boolean[] writeTo) {
		for (int b = 0; b < writeTo.length; ++b) {
			writeTo[writeTo.length - b - 1] = ((i >>> b) & 1) == 1;
		}
	}

	@Override
	public int hashCode() {
		return this.vector.hashCode();
	}
	
	@Override
	public boolean equals(final Object second) {
		if (!(second instanceof VectorSemantics<?, ?>))
			return false;
		return this.vector.equals(((VectorSemantics<?, ?>) second).vector);
	}

	@Override
	public boolean equals(final VectorSemantics<?, ?> second, final double maxError) {
		throw new UnsupportedOperationException("not implemented");
	}

	
	@Override
	public void drawSemantics(BufferedImage image, int offsetX, int offsetY, int width, int height) {
		throw new UnsupportedOperationException("not implemented");
	}

}
