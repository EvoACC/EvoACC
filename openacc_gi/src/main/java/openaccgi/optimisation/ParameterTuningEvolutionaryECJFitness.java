package openaccgi.optimisation;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.vector.IntegerVectorIndividual;
import openaccgi.patch_model.OpenACCParallelLoopDirective;
import openaccgi.patch_model.Patch;

import java.util.Optional;

@SuppressWarnings("serial")
public class ParameterTuningEvolutionaryECJFitness extends Problem implements SimpleProblemForm {
	
	private static Patch patch = null;
	private static  IFitnessFunction fitnessFunction = null;
	
	/*package*/ static void setPatch(Patch p){
		patch = p;
	}
	
	/*package*/ static void setFitnessFunction(IFitnessFunction fitFunc){
		fitnessFunction = fitFunc;
	}
	
	/*package*/ static void applyToPatch(Individual ind){
		assert(ind instanceof IntegerVectorIndividual);
		assert(patch !=null);
		
		IntegerVectorIndividual vectorInd = (IntegerVectorIndividual)ind;
		int pos = 0;
		for(int i=0; i<patch.getDirectives().length;i++){
			if(patch.getDirectives()[i] instanceof OpenACCParallelLoopDirective) {
				if(vectorInd.genome[pos*3]!=-1){
					((OpenACCParallelLoopDirective)patch.getDirectives()[i])
						.setNumGangs(Optional.of(vectorInd.genome[pos*3]));
				} else {
					((OpenACCParallelLoopDirective)patch.getDirectives()[i]).setNumGangs(Optional.empty());
				}
				
				if(vectorInd.genome[pos*3 + 1]!=-1){
					((OpenACCParallelLoopDirective)patch.getDirectives()[i])
						.setNumWorkers(Optional.of(vectorInd.genome[pos*3 + 1]));
				} else {
					((OpenACCParallelLoopDirective)patch.getDirectives()[i]).setNumWorkers(Optional.empty());
				}
				
				if(vectorInd.genome[pos*3 + 2]!=-1){
					((OpenACCParallelLoopDirective)patch.getDirectives()[i])
						.setVectorLength(Optional.of(vectorInd.genome[pos*3 + 2]));
				} else {
					((OpenACCParallelLoopDirective)patch.getDirectives()[i]).setVectorLength(Optional.empty());
				}
				pos++;
			}
		}
	}

	@Override
	public void evaluate(EvolutionState arg0, Individual arg1, int arg2, int arg3) {
		assert(patch != null);
		assert(fitnessFunction != null);
		assert(arg1 instanceof IntegerVectorIndividual);
		applyToPatch(arg1);
		double fitness = fitnessFunction.getFitness(patch);
		((SimpleFitness)arg1.fitness).setFitness(arg0, fitness, false);
		arg1.evaluated = true;
	}

}
