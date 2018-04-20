package ec.app.regression.testcases;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import library.semantics.TestCase;

import ec.EvolutionState;
import ec.util.Parameter;

public class CSVFactory extends TestCaseFactory {

	private static final Parameter trainingCSV = new Parameter("testCases.trainingCsv");
	private static final Parameter testingCSV = new Parameter("testCases.testingCsv");

	@Override
	public List<TestCase<Double>> compute(EvolutionState state) {
		return null;
	}
	
	protected List<TestCase<Double>> compute(EvolutionState state, Parameter param) {
		
		File csvFile = state.parameters.getFile(param, null);
		List<TestCase<Double>> list = new ArrayList<TestCase<Double>>();
		BufferedReader reader = null;
		String line;
		String[] fields;
		Double[] x;

		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile)));

			line = reader.readLine(); // first line contains headers
			x = new Double[line.split(";").length - 1];

			while ((line = reader.readLine()) != null) {
				fields = line.split(";");
				assert fields.length == x.length + 1;

				Double y = Double.parseDouble(fields[0]);
				for (int i = 1; i < fields.length; ++i) {
					x[i - 1] = Double.parseDouble(fields[i]);
				}

				list.add(new TestCase<Double>(y, x.clone()));
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return list;
	}

	public List<TestCase<Double>> generateTraining(final EvolutionState state) {
		return this.compute(state, trainingCSV);
	}

	public List<TestCase<Double>> generateTest(final EvolutionState state) {
		return this.compute(state, testingCSV);
	}
}
