package library.semantics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import library.generator.TreeNode;
import library.instructions.InputData;

public class IntervalSemantics extends VectorSemantics<Double, double[]> {

	public IntervalSemantics(final TreeNode program, List<TestCase<Double>> testCases) {
		super(program, testCases);
	}

	@Override
	protected double[] computeSemanticsInternal(final TreeNode program, List<TestCase<Double>> testCases) {
		double[] semantics = new double[testCases.size()];
		InputData<Double> data = new InputData<Double>();

		for (int i = 0; i < semantics.length; ++i) {
			data.setX(testCases.get(i).arguments);
			semantics[i] = (Double) program.execute(data);
		}

		return semantics;
	}

	@Override
	public IntervalSemantics clone() {
		IntervalSemantics cloned = (IntervalSemantics) super.clone();
		return cloned;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode((double[]) this.vector);
	}
	
	@Override
	public boolean equals(final Object second) {
		if (!(second instanceof VectorSemantics<?, ?>))
			return false;
		return Arrays.equals((double[]) this.vector, (double[]) ((VectorSemantics<?, ?>) second).vector);
	}

	public boolean equals(final VectorSemantics<?, ?> second, final double maxError) {
		double[] vector = (double[]) this.vector;
		double[] secondVector = (double[]) second.vector;
		
		if (vector.length != secondVector.length)
			return false;

		for (int i = 0; i < vector.length; ++i) {
			if (Math.abs(vector[i] - secondVector[i]) > maxError)
				return false;
		}

		return true;
	}
	

	private int toRange(final int x, final int min, final int max) {
		if (x < min)
			return min;
		else if (x > max)
			return max;
		return x;
	}

	private double toRange(final double x, final double min, final double max) {
		if (x < min)
			return min;
		else if (x > max)
			return max;
		return x;
	}

	public void drawSimpleSemantics(BufferedImage image, int offsetX, int offsetY, int width, int height) {
		double from = Double.MAX_VALUE;
		double to = -Double.MAX_VALUE;

		for (TestCase<Double> tc : this.testCases) {
			double arg0 = tc.getArguments()[0];
			if (arg0 < from)
				from = arg0;
			if (arg0 > to)
				to = arg0;
		}

		Graphics graphics = image.getGraphics();
		graphics.setColor(Color.RED);
		int yIdx = 0;
		double scaleX = (double) width / (to - from);
		double scaleY = Math.sqrt(scaleX);// (double) height / (to - from);
		int[] xP = new int[4];
		int[] yP = new int[4];
		//for (double x = from; x < to && yIdx < vector.length - 1; x += step) {
		for (TestCase<Double> tc : this.testCases) {
			double x = tc.getArguments()[0];

			xP[0] = xP[3] = (int) (offsetX + (x - from) * scaleX);
			xP[1] = xP[2] = (int) (offsetX + (x - from + 1) * scaleX);
			yP[0] = (int) offsetY + toRange((int) (-vector[yIdx] * scaleY + height / 2), 0, height);
			yP[1] = offsetY + toRange((int) (-vector[yIdx + 1] * scaleY + height / 2), 0, height);
			yP[2] = offsetY + height / 2;
			yP[3] = offsetY + height / 2;
			// graphics.drawLine(x0, y0, x1, y1);

			graphics.fillPolygon(xP, yP, xP.length);

			++yIdx;
		}
	}

	@Override
	public void drawSemantics(BufferedImage image, int offsetX, int offsetY, int width, int height) {
		drawSimpleSemantics(image, offsetX, offsetY, width, height);
	}

}
