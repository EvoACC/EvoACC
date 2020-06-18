package openaccgi.optimisation;

import openaccgi.Utils;
import openaccgi.patch_model.ADirective;
import openaccgi.patch_model.Patch;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class GreedyDirectiveSelection extends AOptimiser {
	
	public GreedyDirectiveSelection(IFitnessFunction fitFunc, Optional<File> logDir){
		super(fitFunc, logDir);
	}

	@Override
	public void optimise(Patch p){

		Optional<File> logFile;
		if(this.logDirectory.isPresent()){
			logFile = Optional.of(new File(this.logDirectory.get() + File.separator + "log.dat"));
		} else {
			logFile = Optional.empty();
		}

		ADirective[] directives = p.getDirectives();
		
		for(ADirective dir: directives){
			dir.setActive(false);
		}
		
		for(ADirective dir: directives){
			try {
				dir.setActive(true);
				Utils.appendToFile(logFile, "Testing:\n" + p.getPatch());
				double fitness = super.getFitnessFunction().getFitness(p);
				Utils.appendToFile(logFile, "Fitness: " + fitness);
				Utils.appendToFile(logFile, "Success: " + (fitness >= 0 ? "true" : "false"));
				Utils.appendToFile(logFile, System.lineSeparator());
				dir.setActive(fitness >= 0);
			}catch(IOException e){
				System.err.println("Error when trying to create Directive Selection log.");
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

}
