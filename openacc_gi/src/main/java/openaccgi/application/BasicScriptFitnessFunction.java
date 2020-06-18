package openaccgi.application;

import openaccgi.patch_model.Patch;
import openaccgi.optimisation.IFitnessFunction;

import java.io.*;

public class BasicScriptFitnessFunction implements IFitnessFunction {
	
	private final File script;
	
	public BasicScriptFitnessFunction(File script){
		this.script = script;
	}

	@Override
	public double getFitness(Patch p)  { //TODO: Hellish handling of Exceptions here. Could do with some refactoring.
		boolean patchPopulated = !p.getPatch().isEmpty();

		File f = null;

		if(patchPopulated) {
			try {
				f = File.createTempFile("patch", ".tmp");
			} catch (IOException e) {
				System.err.println("Error when attempting to create patch file. Stack trace: ");
				e.printStackTrace();
				System.exit(1);
			}


			PrintWriter writer = null;
			try {
				writer = new PrintWriter(f);
			} catch (FileNotFoundException e) {
				System.err.println("Error when attempting to write to patch file. Stack trace: ");
				e.printStackTrace();
				System.exit(1);
			}
			writer.print(p.getPatch());
			writer.close();
		}

		Double toReturn = null;

		assert((patchPopulated && f != null) || (!patchPopulated));

		try {
			toReturn = runScript(patchPopulated ? f.getAbsolutePath() : "");
		} catch (NumberFormatException | NullPointerException | IOException e) {
			System.err.println("Error when running the fitness script. Stack trace: ");
			e.printStackTrace();
			System.exit(1);
		}
		
		assert(toReturn != null);
		
		return toReturn;
	}
	
	private double runScript(String argument) throws IOException, NullPointerException, NumberFormatException{
		Process process = Runtime.getRuntime().exec(this.script.getAbsolutePath()+" "+argument);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		String output = null;
		while((line=reader.readLine()) != null){
			if(output != null){
				reader.close();
				throw new IOException("Script "+this.script.getAbsolutePath()+" has output more than one line.\n" +
					"This breaks the contract. The evaluation script must return a single" +
					" double value. Value Returned:\n" +
					output + "\n" + line);
			}
			output = line;
		}
		reader.close();
		
		Double toReturn = null;
		try{
			toReturn = Double.parseDouble(output);
		} catch(NullPointerException e){
			throw new NullPointerException("Script '" + this.script.getAbsolutePath()
				+ "' produced no output\nException Information:\n"+e.getMessage());
		} catch(NumberFormatException e){
			throw new NumberFormatException("Script " + this.script.getAbsolutePath()
				+ " produced output '" +output+"'. Not parsable\nException Information:\n" + e.getMessage());
		}
		
		assert(toReturn != null);
		
		return toReturn;
		
	}

}
