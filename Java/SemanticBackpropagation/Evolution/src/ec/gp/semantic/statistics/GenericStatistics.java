package ec.gp.semantic.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import ec.EvolutionState;
import ec.gp.semantic.EvoState;
import ec.gp.semantic.utils.IFinishListener;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

public class GenericStatistics implements IFinishListener {

	private static final Parameter DEFAULT_BASE = new Parameter("GenericStatistics");
	private static final String OUT_DIR = "outDir";
	private static final String OUT_FILENAME = "baseFilename";

	private EvoState state;

	private File outputFile;
	private BufferedWriter writer;
	private HashMap<String, Integer> columnMapping = new HashMap<String, Integer>();

	public void setup(EvolutionState state, Parameter base) {
		this.state = (EvoState)state;
		this.state.addFinishListener(this);

		ParameterDatabase db = state.parameters;
		String outputDirectory = db.getString(base.push(OUT_DIR), DEFAULT_BASE.push(OUT_DIR));
		String baseFilename = db.getString(base.push(OUT_FILENAME), DEFAULT_BASE.push(OUT_FILENAME));

		try {
			int i = 0;
			do {
				this.outputFile = new File(String.format("%s\\%s.%d.csv", outputDirectory, baseFilename, i++));
			} while (!this.outputFile.createNewFile());

			this.writer = new BufferedWriter(new FileWriter(this.outputFile));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public void write(NavigableMap<String, Object> values) {
		Locale.setDefault(Locale.ENGLISH);

		TreeMap<Integer, Object> sortedValues = new TreeMap<Integer, Object>();

		for (Entry<String, Object> value : values.entrySet()) {
			Integer column = this.columnMapping.get(value.getKey());
			if (column == null) {
				column = this.columnMapping.size();
				this.columnMapping.put(value.getKey(), column);
			}

			sortedValues.put(column, value.getValue());
		}

		//write
		try {
			int lastKey = 0;
			for (Entry<Integer, Object> value : sortedValues.entrySet()) {
				while (lastKey + 1 < value.getKey()) {
					writer.write(";");
					lastKey += 1;
				}

				writer.write(value.getValue().toString());
				writer.write(";");
				lastKey = value.getKey();
			}

			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		if (this.writer == null)
			return;

		try {
			TreeMap<Integer, String> revMapping = new TreeMap<Integer, String>();

			for (Entry<String, Integer> mapping : this.columnMapping.entrySet()) {
				revMapping.put(mapping.getValue(), mapping.getKey());
			}

			for (Entry<Integer, String> value : revMapping.entrySet()) {
				writer.write('"');
				writer.write(value.getValue());
				writer.write("\";");
			}

			this.writer.newLine();
			this.writer.close();
			this.writer = null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void finalize() {
		this.close();
	}

	@Override
	public void finish(int code) {
		this.close();
	}
}
