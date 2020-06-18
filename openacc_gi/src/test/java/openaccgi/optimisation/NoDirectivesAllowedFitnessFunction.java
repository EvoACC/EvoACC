package openaccgi.optimisation;

import openaccgi.patch_model.ADirective;
import openaccgi.patch_model.Patch;

//Any Active Parallel loop directives will result in -1 being returned (i.e. hard constraints are broken)
public class NoDirectivesAllowedFitnessFunction implements IFitnessFunction {

	@Override
	public double getFitness(Patch p) {
		ADirective[] directives =  p.getDirectives();
		for(ADirective dirs : directives){
			if(dirs.isActive()){
				return -1;
			}
		}
		return Double.MAX_VALUE;
	}

}
