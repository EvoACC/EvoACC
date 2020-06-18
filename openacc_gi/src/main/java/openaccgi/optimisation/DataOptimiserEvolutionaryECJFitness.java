package openaccgi.optimisation;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.vector.BitVectorIndividual;
import openaccgi.patch_model.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("serial")
 public class DataOptimiserEvolutionaryECJFitness extends Problem implements SimpleProblemForm {
	
	private static Patch patch = null;
	private static IFitnessFunction fitnessFunction = null;
	private static Optional<Set<ADirective>> recentlyAdded = Optional.empty();
	private static IToolUtils toolUtils = null;
	private static SortedSet<DataDirectiveInsertionPoint> insertionPoints = null;
	
	/*package*/ static void setPatch(Patch p){
		patch = p;
	}
	
	/*package*/ static void setFitnessFunction(IFitnessFunction fitFunc){
		fitnessFunction = fitFunc;
	}
	
	/*package*/ static void setToolUtils(IToolUtils tUtils){
		toolUtils = tUtils;
	}
	
	/*package*/ static void setInsertionPoints(SortedSet<DataDirectiveInsertionPoint> s){
		insertionPoints = s;
	}
	
	/*package*/ static void applyToPatch(Individual ind){
		assert(ind instanceof BitVectorIndividual);
		assert(patch != null && toolUtils != null && insertionPoints != null && !recentlyAdded.isPresent());
		
		BitVectorIndividual vectorInd = (BitVectorIndividual)ind;
		Map<File,Set<DataDirectiveInsertionPoint>> currentInsertionPoints = new HashMap<File, Set<DataDirectiveInsertionPoint>>();
		for(DataDirectiveInsertionPoint i : insertionPoints){
			if(!currentInsertionPoints.containsKey(i.getFile())){
				currentInsertionPoints.put(i.getFile(), new HashSet<DataDirectiveInsertionPoint>());
			}
			currentInsertionPoints.get(i.getFile()).add(i);
		}
		
		int index = 0;
		Iterator<DataDirectiveInsertionPoint> it = insertionPoints.iterator();
		while(it.hasNext()){
			DataDirectiveInsertionPoint dir = it.next();
			if(vectorInd.genome[index] && currentInsertionPoints.get(dir.getFile()).contains(dir)){

				Optional<OpenACCDataDirective> temp = Optional.empty();
				try {
					temp = toolUtils.getDataDirective(dir.getFile(), dir.getStartLineNumber(),
						dir.getEndLineNumber(), patch.getDirectives());
				} catch (IOException e){
					System.err.println("Error in getting data directive info for File '"
						+ dir.getFile() + "', start line '" + dir.getStartLineNumber()
						+ "', end line '" + dir.getEndLineNumber());
					System.err.println(e.getMessage());
					System.exit(1);
				}
				assert(temp.isPresent());
				patch.addADirective(temp.get());
				if(!recentlyAdded.isPresent()){
					recentlyAdded = Optional.of(new HashSet<ADirective>());
				}
				recentlyAdded.get().add(temp.get());
				try {
					currentInsertionPoints.put(dir.getFile(),
						toolUtils.getDataDirectiveInsertionPoints(dir.getFile(), patch.getDirectives()));
				} catch(IOException e){
					System.err.println("Error in getting Data directive insertion points for file '"+dir.getFile());
					System.err.println(e.getMessage());
					System.exit(1);
				}
			}
			index++;
		}
	}
	
	/*package*/ static Patch getPatch(){
		return patch;
	}
	
	/*package*/ static void removeRecentlyAddedDirectives(){
		if(recentlyAdded.isPresent()){
			for(ADirective dir : recentlyAdded.get()){
                patch.removeDirective(dir);
			}
			recentlyAdded = Optional.empty();
		}
	}

	/*package*/ static void close(){
		patch = null;
		fitnessFunction = null;
		recentlyAdded = Optional.empty();
		toolUtils = null;
		insertionPoints = null;
	}

	@Override
	public void evaluate(EvolutionState arg0, Individual arg1, int arg2, int arg3) {
		assert(patch != null && fitnessFunction != null && toolUtils != null && insertionPoints != null);

		boolean assertsEnabled = false;
		assert((assertsEnabled = true));
		ADirective[] dirsBefore = null;
		String patchBefore = null;

		if(assertsEnabled) {
			dirsBefore = patch.getDirectives();
			patchBefore = patch.getPatch();
		}


		applyToPatch(arg1);
		BitVectorIndividual vectorInd = (BitVectorIndividual)arg1;
		double fitness = fitnessFunction.getFitness(patch);
		removeRecentlyAddedDirectives();

		if(assertsEnabled) {
			assert(!recentlyAdded.isPresent());
			assert(patchBefore.equals(patch.getPatch()));
			assert(patch.getDirectives().length == dirsBefore.length);
			for(int i=0; i<patch.getDirectives().length; i++){
				assert(patch.getDirectives()[i].isActive() == dirsBefore[i].isActive());
				assert(patch.getDirectives()[i].getFile().equals(dirsBefore[i].getFile()));
				assert(patch.getDirectives()[i].hashCode() == dirsBefore[i].hashCode());
			}
		}


		((SimpleFitness)arg1.fitness).setFitness(arg0, fitness, false);
		arg1.evaluated = true;
	}
}
