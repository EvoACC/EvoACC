package openaccgi.optimisation;

import openaccgi.patch_model.OpenACCParallelLoopDirective;
import openaccgi.patch_model.Patch;

import java.util.Optional;

public class CustomParameterFitnessFunction implements IFitnessFunction {
	
	private final Optional<Integer>[] idealNumGangs;
	private final Optional<Integer>[] idealNumWorkers;
	private final Optional<Integer>[] idealVectorLength;
	
	public CustomParameterFitnessFunction(Optional<Integer>[] numGangs, Optional<Integer>[] numWorkers,
	                                      Optional<Integer>[] vectorLength){
		assert(numGangs.length == numWorkers.length && numWorkers.length == vectorLength.length);
		this.idealNumGangs = numGangs;
		this.idealNumWorkers = numWorkers;
		this.idealVectorLength = vectorLength;
	}

	@Override
	public double getFitness(Patch p) {
		double toReturn = 0.0;
		
		assert(idealNumGangs.length == p.getDirectives().length);
		
		for(int i=0; i<p.getDirectives().length;i++){
			if(p.getDirectives()[i].isActive() && p.getDirectives()[i] instanceof OpenACCParallelLoopDirective){
				
			}
			if(((OpenACCParallelLoopDirective)p.getDirectives()[i]).getNumGangs().equals(idealNumGangs[i])){
				toReturn++;
			}
			
			if(((OpenACCParallelLoopDirective)p.getDirectives()[i]).getNumWorkers().equals(idealNumWorkers[i])){
				toReturn++;
			}
			
			if(((OpenACCParallelLoopDirective)p.getDirectives()[i]).getVectorLength().equals(idealVectorLength[i])){
				toReturn++;
			}
		}
		
		return toReturn;
	}

}
