package openaccgi.optimisation;

import openaccgi.patch_model.ADirective;
import openaccgi.patch_model.Patch;

import java.util.Set;

//A mock fitness function created to test DataOptimiser
public class CustomDataFitnessFunction implements IFitnessFunction {
	
	private final Set<ADirective> toContain;
	
	public CustomDataFitnessFunction(Set<ADirective> ideal){
		this.toContain = ideal;
	}

	@Override
	public double getFitness(Patch p) {
		double toReturn = 0.0;
		for(ADirective d : p.getDirectives()){
			if(toContain.contains(d)){
				toReturn++;
			} else {
				toReturn--;
			}
		}
		
		return toReturn;
	}
}
