package openaccgi.optimisation;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.vector.BitVectorIndividual;
import openaccgi.patch_model.IToolUtils;
import openaccgi.patch_model.Patch;
import openaccgi.patch_model.directive_tree.DirectiveTreeFactory;

;

public class DirectivePruneEvolutionaryECJFitness extends Problem implements SimpleProblemForm {
	private static Patch patch = null;
	private static  IFitnessFunction fitnessFunction = null;
	private static int[] genotypeMap;
	private static IToolUtils toolUtils = null;

	/*package*/ static void setPatch(Patch p){
		patch = p;
	}

	/*package*/ static void setToolUtils(IToolUtils tUtils){
		toolUtils = tUtils;
	}

	/*package*/ static void setFitnessFunction(IFitnessFunction fitFunc){
		fitnessFunction = fitFunc;
	}

	/*package*/ static void setGenotypeMap(int[] genoMap){
		genotypeMap = genoMap;
	}

	/*package*/ static void applyToPatch(Individual ind){
		assert(ind instanceof BitVectorIndividual);
		assert(patch !=null);
		assert(genotypeMap != null);
		assert(toolUtils != null);

		BitVectorIndividual vectorInd = (BitVectorIndividual) ind;
		for(int i=0; i < vectorInd.genome.length; i++){
			patch.getDirectives()[genotypeMap[i]].setActive(vectorInd.genome[i]);
		}

		DirectiveTreeFactory.getTree(patch.getDirectives()).removeUselessElements(toolUtils);
	}

	@Override
	public void evaluate(EvolutionState arg0, Individual arg1, int arg2, int arg3) {
		assert(patch != null);
		assert(fitnessFunction != null);
		assert(toolUtils != null);
		assert(arg1 instanceof BitVectorIndividual);
		applyToPatch(arg1);
		double fitness = fitnessFunction.getFitness(patch);
		((SimpleFitness)arg1.fitness).setFitness(arg0, fitness, false);
		arg1.evaluated = true;
	}
}
