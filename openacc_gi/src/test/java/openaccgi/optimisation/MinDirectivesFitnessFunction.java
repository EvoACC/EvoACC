package openaccgi.optimisation;

import openaccgi.patch_model.ADirective;
import openaccgi.patch_model.Patch;

public class MinDirectivesFitnessFunction implements IFitnessFunction {

	@Override
	public double getFitness(Patch p) {
		double toReturn = 100.0;
		ADirective[] directives =  p.getDirectives();
		for(ADirective dirs : directives){
			if(dirs.isActive()){
				toReturn--;
			}
		}
		return toReturn;
	}

}
