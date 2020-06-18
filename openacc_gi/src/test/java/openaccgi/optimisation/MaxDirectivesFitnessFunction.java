package openaccgi.optimisation;

import openaccgi.patch_model.ADirective;
import openaccgi.patch_model.Patch;


//Kinda like Max-Ones but maximise the number of directives. Nothing Clever here
public class MaxDirectivesFitnessFunction implements IFitnessFunction {

	@Override
	public double getFitness(Patch p) {
		double toReturn = 0.0;
		ADirective[] directives =  p.getDirectives();
		for(ADirective dirs : directives){
			if(dirs.isActive()){
				toReturn++;
			}
		}
		return toReturn;
	}

}
