package openaccgi.optimisation;

import openaccgi.patch_model.Patch;

import java.io.File;
import java.util.Optional;

public abstract class  AOptimiser {
	
	final private IFitnessFunction fitnessFunction;
	final protected Optional<File> logDirectory;
	
	protected AOptimiser(IFitnessFunction fitnessFunc, Optional<File> logDir){
		this.fitnessFunction = fitnessFunc;
		this.logDirectory = logDir;
		assert(!this.logDirectory.isPresent() || this.logDirectory.get().isDirectory());
	}
	
	protected IFitnessFunction getFitnessFunction(){
		return this.fitnessFunction;
	}
	
	public abstract void optimise(Patch p);
}
