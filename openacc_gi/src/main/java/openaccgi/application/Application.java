package openaccgi.application;

import openaccgi.optimisation.*;
import openaccgi.patch_model.Patch;
import openaccgi.patch_model.PatchFactory;
import openaccgi.patch_model.ToolUtils;
import org.apache.commons.cli.ParseException;
import openaccgi.Utils;
import openaccgi.optimisation.*;
import openaccgi.program_data.CSVDataContainer;

import java.io.File;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.Optional;
import java.util.Random;

public class Application {

	public static void main(String[] args) throws IOException, AccessControlException, ParseException {

	    // Read in the parameters
		ApplicationCommandLineParser parser = new ApplicationCommandLineParser(args);
		ToolUtils toolUtils = new ToolUtils(parser.getIncludes(), parser.getLanguage(), parser.getLoopAnalyser(),
				parser.getDataInsertionFinder(), parser.getDataDirectiveAnalyser(), parser.isIncludeDataRanges());

		CSVDataContainer dataContainer = new CSVDataContainer(parser.getCsvFile(), parser.isIncludeDataRanges());
		Patch p = PatchFactory.createPatchWithAllLoopsParallelised(dataContainer, parser.getLanguage(), toolUtils);
		BasicScriptFitnessFunction fitnessFunc = new BasicScriptFitnessFunction(parser.getScriptFile());

		Random random = new Random();
		if(parser.getSeed().isPresent()){
			random = new Random(parser.getSeed().get());
		}

		Optional<File> log;
		if(parser.getLogFile().isPresent()){
			log = Optional.of
				(new File(parser.getLogFile().get().getAbsolutePath() + File.separator + "log.dat"));
		} else {
			log = Optional.empty();
		}

		if(parser.getLogFile().isPresent()){
			Utils.appendToFile(new File(parser.getLogFile().get().getAbsolutePath() + File.separator
				+ "original_fitness.dat"),
				Double.toString(fitnessFunc.getFitness(PatchFactory.createEmptyPatch(parser.getLanguage()))));
		}

		Optional<File> loopSelectionLogDir;
		if(parser.getLogFile().isPresent()){
			loopSelectionLogDir = Optional.of(new File(parser.getLogFile().get().getAbsolutePath()
				+ File.separator + "loop_selection"));
			loopSelectionLogDir.get().mkdir();
		} else {
			loopSelectionLogDir = Optional.empty();
		}

		if(log.isPresent()){
			Utils.appendToFile(log.get(), "Running loop selection [Logged to \""
				+ loopSelectionLogDir.get().getAbsolutePath() + "\"]...");
		}

		AOptimiser loopSelection = new GreedyDirectiveSelection(fitnessFunc, loopSelectionLogDir);
		loopSelection.optimise(p);

		String currentPatch = p.getPatch();
		double currentFitness = fitnessFunc.getFitness(p);

		if(parser.getLogFile().isPresent()){
			Utils.appendToFile(new File(parser.getLogFile().get() + File.separator
				+ "loop_selection_fitness.dat"), Double.toString(currentFitness));
			Utils.appendToFile(new File(parser.getLogFile().get() + File.separator
				+ "loop_selection.patch"), currentPatch);
			Utils.appendToFile(log.get(),"Done running loop selection.");
		}


		//Determine the number of evaluations required for the final
		int directivePruningEvaluationsRequired = 0;
		int loopSelectionEvaluated = 0;

		boolean loopsSelected = false;
		for(int i=0; i<p.getDirectives().length; i++){
			if(p.getDirectives()[i].isActive()){
				loopsSelected = true;
				//This is a rough estimate of how much directive pruning evaluations we require.
				directivePruningEvaluationsRequired++;
			}
		}

		if(!loopsSelected){
			if(log.isPresent()) {
				Utils.appendToFile(log.get(), "No loops selected for parallelisation. " +
					"Cannot proceed to the next steps.");
			}
			System.exit(0);
		}

		loopSelectionEvaluated = p.getDirectives().length;

		//Set The Data Optimiser and Parameter Tuning algorithm
		int numDataEvaluationsMax =
			parser.getMaxEvaluations() - loopSelectionEvaluated - directivePruningEvaluationsRequired;

		int popSize = (int) Math.max(5, numDataEvaluationsMax / 10.0);

		Optional<File> dataOptimiserLog;
		if(parser.getLogFile().isPresent()){
			dataOptimiserLog = Optional.of(new File(parser.getLogFile().get().getAbsolutePath()
				+ File.separator + "data_optimiser"));
			dataOptimiserLog.get().mkdir();
		} else {
			dataOptimiserLog = Optional.empty();
		}

		if(log.isPresent()){
			Utils.appendToFile(log.get(), "Running data directive optimization [Logged to \"" +
				dataOptimiserLog.get().getAbsolutePath() + "\"]...");
		}
		AOptimiser dataOptimiser =
			new DataOptimiserEvolutionary(fitnessFunc, numDataEvaluationsMax,popSize, random, dataOptimiserLog, toolUtils);
		dataOptimiser.optimise(p);

		currentPatch = p.getPatch();
		currentFitness = fitnessFunc.getFitness(p);

		if(parser.getLogFile().isPresent()){
			Utils.appendToFile(new File(parser.getLogFile().get() + File.separator
				+ "data_optimiser_fitness.dat"), Double.toString(currentFitness));
			Utils.appendToFile(new File(parser.getLogFile().get() + File.separator
				+ "data_optimiser.patch"), currentPatch);
			Utils.appendToFile(log.get(),"Done running data optimiser.");
		}



                Optional<File> paramOptimiserLog;
		if(parser.getLogFile().isPresent()){
			paramOptimiserLog = Optional.of(new File(parser.getLogFile().get().getAbsolutePath()
					+ File.separator + "param_optimiser"));
			paramOptimiserLog.get().mkdir();
		} else {
			paramOptimiserLog = Optional.empty();
		}

		if(log.isPresent()){
			Utils.appendToFile(log.get(), "Running param directive optimization [Logged to \"" +
					paramOptimiserLog.get().getAbsolutePath() + "\"]...");
		}
                int numParamEvaluationsMax =(int) (0.5 * numDataEvaluationsMax);
		AOptimiser parameterOptimiser =
				new ParameterTuningEvolutionary(fitnessFunc, numParamEvaluationsMax, random, paramOptimiserLog);
		parameterOptimiser.optimise(p);
		currentPatch = p.getPatch();
		currentFitness = fitnessFunc.getFitness(p);

		if(parser.getLogFile().isPresent()){
			Utils.appendToFile(new File(parser.getLogFile().get() + File.separator
					+ "parameter_optimiser_fitness.dat"), Double.toString(currentFitness));
			Utils.appendToFile(new File(parser.getLogFile().get() + File.separator
					+ "parameter_optimiser.patch"), currentPatch);
			Utils.appendToFile(log.get(),"Done running parameter optimiser.");
		}


		Optional<File> directivePruningLog;
		if(parser.getLogFile().isPresent()){
			directivePruningLog = Optional.of(new File(parser.getLogFile().get().getAbsolutePath()
				+ File.separator + "directive_pruning"));
			directivePruningLog.get().mkdir();
		} else {
			directivePruningLog = Optional.empty();
		}

		if(log.isPresent()){
			Utils.appendToFile(log.get(), "Running directive pruning [Logged to \"" +
				directivePruningLog.get().getAbsolutePath() + "\"]...");
		}
		DirectivePrune deltaDebug =
			new DirectivePrune(fitnessFunc, toolUtils, directivePruningLog);
		deltaDebug.optimise(p);

		currentPatch = p.getPatch();
		currentFitness = fitnessFunc.getFitness(p);

		if(parser.getLogFile().isPresent()){
			Utils.appendToFile(new File(parser.getLogFile().get() + File.separator
				+ "directive_pruning_fitness.dat"), Double.toString(currentFitness));
			Utils.appendToFile(new File(parser.getLogFile().get() + File.separator
				+ "directive_pruning.patch"), currentPatch);
			Utils.appendToFile(log.get(),"Done running directive pruning.");

			Utils.appendToFile(new File(parser.getLogFile().get() + File.separator
				+ "final.patch"), currentPatch);
			Utils.appendToFile(new File(parser.getLogFile().get() + File.separator
				+ "final_fitness.dat"), Double.toString(currentFitness));
			Utils.appendToFile(new File(parser.getLogFile().get() + File.separator
				+ "total_evaluations.dat"),
				Integer.toString((deltaDebug.getNumberEvaluations() + numDataEvaluationsMax + loopSelectionEvaluated)));

			Utils.appendToFile(log.get(),"Done optimising.");
		}

		if(!parser.isSilent()){
			System.out.println(currentPatch);
		}
	}


}
