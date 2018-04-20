package ec.app.semanticGP.func;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import library.generator.TreeNode;
import library.instructions.InstructionBase;
import ec.EvolutionState;
import ec.app.semanticGP.func.logic.And;
import ec.app.semanticGP.func.logic.False;
import ec.app.semanticGP.func.logic.Input0;
import ec.app.semanticGP.func.logic.Input1;
import ec.app.semanticGP.func.logic.Input10;
import ec.app.semanticGP.func.logic.Input11;
import ec.app.semanticGP.func.logic.Input12;
import ec.app.semanticGP.func.logic.Input13;
import ec.app.semanticGP.func.logic.Input14;
import ec.app.semanticGP.func.logic.Input15;
import ec.app.semanticGP.func.logic.Input2;
import ec.app.semanticGP.func.logic.Input3;
import ec.app.semanticGP.func.logic.Input4;
import ec.app.semanticGP.func.logic.Input5;
import ec.app.semanticGP.func.logic.Input6;
import ec.app.semanticGP.func.logic.Input7;
import ec.app.semanticGP.func.logic.Input8;
import ec.app.semanticGP.func.logic.Input9;
import ec.app.semanticGP.func.logic.Nand;
import ec.app.semanticGP.func.logic.Nor;
import ec.app.semanticGP.func.logic.Or;
import ec.app.semanticGP.func.logic.True;
import ec.app.semanticGP.func.logic.Xor;
import ec.app.semanticGP.func.numeric.Cos;
import ec.app.semanticGP.func.numeric.Div;
import ec.app.semanticGP.func.numeric.ERC;
import ec.app.semanticGP.func.numeric.Exp;
import ec.app.semanticGP.func.numeric.Inv;
import ec.app.semanticGP.func.numeric.Log;
import ec.app.semanticGP.func.numeric.Mul;
import ec.app.semanticGP.func.numeric.Neg;
import ec.app.semanticGP.func.numeric.One;
import ec.app.semanticGP.func.numeric.Sin;
import ec.app.semanticGP.func.numeric.Sqrt;
import ec.app.semanticGP.func.numeric.Sub;
import ec.app.semanticGP.func.numeric.Sum;
import ec.app.semanticGP.func.numeric.X0;
import ec.app.semanticGP.func.numeric.X1;
import ec.app.semanticGP.func.numeric.X2;
import ec.app.semanticGP.func.numeric.X3;
import ec.app.semanticGP.func.numeric.X4;
import ec.app.semanticGP.func.numeric.X5;
import ec.app.semanticGP.func.numeric.X6;
import ec.app.semanticGP.func.numeric.X7;
import ec.app.semanticGP.func.numeric.Zero;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.semantic.func.SimpleNodeBase;

/**
 * Converter from Generator's TreeNode-based program to ECJ's GPNode-based program.
 * 
 * @author Tomasz Pawlak
 */
public class TreeConverter implements Serializable {

	private Map<Class<?>, SimpleNodeBase<?>> mapping = new HashMap<Class<?>, SimpleNodeBase<?>>();

	public TreeConverter(final EvolutionState state) {

		/*
		 * GPFunctionSet set = new GPFunctionSet(); set.setup(state, new Parameter("gp.fs.0"));
		 */
		Object[] sets = ((GPInitializer) state.initializer).functionSetRepository.values().toArray();
		GPFunctionSet set = (GPFunctionSet) sets[0];

		for (GPNode[] array : set.nodes) {
			for (GPNode gpNode : array) {

				// caution! this ugly if makes mapping between GPNode types and TreeNode types
				if (gpNode instanceof Sum) {
					this.mapping.put(library.instructions.numeric.Sum.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Sub) {
					this.mapping.put(library.instructions.numeric.Sub.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Mul) {
					this.mapping.put(library.instructions.numeric.Mul.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Div) {
					this.mapping.put(library.instructions.numeric.Div.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Sin) {
					this.mapping.put(library.instructions.numeric.Sin.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Cos) {
					this.mapping.put(library.instructions.numeric.Cos.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Exp) {
					this.mapping.put(library.instructions.numeric.Exp.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Log) {
					this.mapping.put(library.instructions.numeric.Log.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Inv) {
					this.mapping.put(library.instructions.numeric.Inv.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Neg) {
					this.mapping.put(library.instructions.numeric.Neg.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Sqrt) {
					this.mapping.put(library.instructions.numeric.Sqrt.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Zero) {
					this.mapping.put(library.instructions.numeric.Zero.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof One) {
					this.mapping.put(library.instructions.numeric.One.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof ERC) {
					//just ignore, there is no ERC in library
				} else if (gpNode instanceof X0) {
					this.mapping.put(library.instructions.numeric.X0.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof X1) {
					this.mapping.put(library.instructions.numeric.X1.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof X2) {
					this.mapping.put(library.instructions.numeric.X2.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof X3) {
					this.mapping.put(library.instructions.numeric.X3.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof X4) {
					this.mapping.put(library.instructions.numeric.X4.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof X5) {
					this.mapping.put(library.instructions.numeric.X5.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof X6) {
					this.mapping.put(library.instructions.numeric.X6.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof X7) {
					this.mapping.put(library.instructions.numeric.X7.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof And) {
					this.mapping.put(library.instructions.logic.And.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Or) {
					this.mapping.put(library.instructions.logic.Or.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Nand) {
					this.mapping.put(library.instructions.logic.Nand.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Nor) {
					this.mapping.put(library.instructions.logic.Nor.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Xor) {
					this.mapping.put(library.instructions.logic.Xor.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Inv) {
					this.mapping.put(library.instructions.logic.Inv.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof True) {
					this.mapping.put(library.instructions.logic.True.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof False) {
					this.mapping.put(library.instructions.logic.False.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Input1) {
					this.mapping.put(library.instructions.logic.Input1.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Input2) {
					this.mapping.put(library.instructions.logic.Input2.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Input3) {
					this.mapping.put(library.instructions.logic.Input3.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Input4) {
					this.mapping.put(library.instructions.logic.Input4.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Input5) {
					this.mapping.put(library.instructions.logic.Input5.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Input6) {
					this.mapping.put(library.instructions.logic.Input6.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Input7) {
					this.mapping.put(library.instructions.logic.Input7.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Input8) {
					this.mapping.put(library.instructions.logic.Input8.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Input9) {
					this.mapping.put(library.instructions.logic.Input9.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Input10) {
					this.mapping.put(library.instructions.logic.Input10.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Input11) {
					this.mapping.put(library.instructions.logic.Input11.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Input12) {
					this.mapping.put(library.instructions.logic.Input12.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Input13) {
					this.mapping.put(library.instructions.logic.Input13.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Input14) {
					this.mapping.put(library.instructions.logic.Input14.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Input15) {
					this.mapping.put(library.instructions.logic.Input15.class, (SimpleNodeBase<?>) gpNode);
				} else if (gpNode instanceof Input0) {
					// Input0 must be last, because other inputs inherit from it
					this.mapping.put(library.instructions.logic.Input0.class, (SimpleNodeBase<?>) gpNode);
				} else {
					throw new RuntimeException("Unknown instruction type");
				}
			}
		}
	}

	public SimpleNodeBase<?> convert(final TreeNode source) {
		// map current TreeNode to GPNode
		InstructionBase<?> instruction = source.getInstruction();
		SimpleNodeBase<?> copy = this.mapping.get(instruction.getClass());
		copy = (SimpleNodeBase<?>) copy.lightClone();
		copy.resetSemantics();

		TreeNode[] children = source.getChildren();

		assert copy.children.length == instruction.getNumberOfArguments();
		assert children.length == copy.children.length;

		for (int i = 0; i < children.length; ++i) {
			copy.children[i] = this.convert(children[i]);
			copy.children[i].argposition = (byte) i;
			copy.children[i].parent = copy;
		}

		return copy;
	}

}
