package ec.gp.semantic.utils;

import ec.gp.GPNode;
import ec.gp.semantic.ISemantics;
import ec.gp.semantic.func.SimpleNodeBase;
import ec.gp.semantic.statistics.CrossoverStatistics;

public class GeometricHelpers {
	
	/**
	 * 
	 * @param xoverPt1parent
	 *            Crossover point in parent 1
	 * @param xoverPt2parent
	 *            Crossover point in parent 2
	 * @param xoverPtchild
	 *            Crossover point in child
	 * @param stat
	 *            Statistics
	 */
	public static void checkGeometricity(final SimpleNodeBase xoverPt1parent, final SimpleNodeBase xoverPt2parent,
			final SimpleNodeBase xoverPtchild, final CrossoverStatistics stat) {
		checkGeometricity(xoverPt1parent.atDepth(), xoverPt1parent, xoverPt2parent, xoverPtchild, stat);
	}

	public static void checkGeometricity(final int xoverLevel, final SimpleNodeBase xoverPt1parent,
			final SimpleNodeBase xoverPt2parent, final SimpleNodeBase xoverPtchild, final CrossoverStatistics stat) {

		int depth = xoverPt1parent.atDepth();
		assert depth == xoverPt2parent.atDepth();
		assert depth == xoverPtchild.atDepth();

		/*double distance;

		distance = getDistanceFromGeometricity(xoverPt1parent.getSemantics(), xoverPt2parent.getSemantics(),
				xoverPtchild.getSemantics());*/

		ISemantics p1Sem = xoverPt1parent.getSemantics();
		ISemantics p2Sem = xoverPt2parent.getSemantics();
		ISemantics childSem = xoverPtchild.getSemantics();

		boolean isGeometric = childSem.isBetween(p1Sem, p2Sem);

		// we add 1 to the levels to start scale on 1
		stat.crossoverOccurred(xoverLevel + 1, depth + 1, isGeometric /*distance <= geometricThreshold*/);

		if (xoverPt1parent.parent instanceof SimpleNodeBase) {
			checkGeometricity(xoverLevel, (SimpleNodeBase) xoverPt1parent.parent,
					(SimpleNodeBase) xoverPt2parent.parent, (SimpleNodeBase) xoverPtchild.parent, stat);
		}
	}

	public static boolean isBetween(final double[] sem1, final double[] sem2, final double[] center) {
		assert sem1.length == sem2.length;
		assert sem1.length == center.length;

		for (int i = 0; i < sem1.length; ++i) {
			// (sem1[i] < center[i] && center[i] < sem2[i]) || (sem2[i] < center[i] && center[i] < sem1[i])
			if ((sem1[i] >= center[i] || center[i] >= sem2[i]) && (sem2[i] >= center[i] || center[i] >= sem1[i]))
				return false;
		}

		return true;
	}

	public static double getDistanceFromGeometricity(double[] sem1, double[] sem2, double[] center) {
		double distance = 0;
		double distanceBetweenParents = 0;
		double diff;

		for (int i = 0; i < sem1.length; ++i) {
			diff = 0.5 * (sem1[i] + sem2[i]) - center[i];
			distance += diff * diff;
			diff = sem1[i] - sem2[i];
			distanceBetweenParents += diff * diff;
		}

		distance = Math.sqrt(/*normalize*/distance / distanceBetweenParents);

		return distance;
	}

	public static ReplaceResult cloneReplacing(GPNode tree, GPNode newSubtree, GPNode oldSubtree) {
		ReplaceResult res = null;
		if (tree == oldSubtree) {
			res = new ReplaceResult();
			res.insertedNode = newSubtree.cloneReplacing();
			res.root = res.insertedNode;
			return res;
		} else {
			GPNode newnode = (GPNode) (tree.lightClone());
			for (int x = 0; x < tree.children.length; x++) {
				ReplaceResult resTmp = cloneReplacing(tree.children[x], newSubtree, oldSubtree);
				if (resTmp.insertedNode != null) {
					res = resTmp;
				}
				newnode.children[x] = (GPNode) (resTmp.root);
				// if you think about it, the following CAN'T be implemented by
				// the children's clone method.  So it's set here.
				newnode.children[x].parent = newnode;
				newnode.children[x].argposition = (byte) x;
			}
			if (res == null) {
				res = new ReplaceResult();
			}
			res.root = newnode;
			return res;
		}
	}
}
