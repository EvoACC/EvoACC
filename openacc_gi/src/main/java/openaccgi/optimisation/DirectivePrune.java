package openaccgi.optimisation;

import openaccgi.Utils;
import openaccgi.patch_model.*;
import openaccgi.patch_model.directive_tree.DirectiveTreeFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class DirectivePrune extends AOptimiser {
	
	private int numberEvaluations=0;
	private final IToolUtils toolUtils;

	public DirectivePrune(IFitnessFunction fitnessFunc, IToolUtils tUtils, Optional<File> logDir) {
		super(fitnessFunc, logDir);
		this.toolUtils = tUtils;
	}

	private void setPatch(Patch p, int map[], boolean[] activateStatuses){
		assert(activateStatuses.length == map.length);
		for(int i=0; i<activateStatuses.length; i++){
			p.getDirectives()[map[i]].setActive(activateStatuses[i]);
		}

		DirectiveTreeFactory.getTree(p.getDirectives()).removeUselessElements(this.toolUtils);
	}

	@Override
	public void optimise(Patch p) {

		Optional<File> logFile;
		if(this.logDirectory.isPresent()){
			logFile = Optional.of(new File(this.logDirectory.get().getAbsolutePath() + File.separator
				+ "log.dat"));
		} else {
			logFile = Optional.empty();
		}

		ADirective[] directives = p.getDirectives();

		int numActive = 0;
		for(ADirective dir : directives){
			if(dir.isActive()){
				numActive++;
			}
		}

		int[] map = new int[numActive];
		int mapPos=0;
		for(int i=0; i<directives.length;i++){
			if(directives[i].isActive()){
				map[mapPos] = i;
				mapPos++;
			}
		}

		boolean[] directivesGenotype = new boolean[map.length];
		for(int i=0; i<directivesGenotype.length;i++){
			directivesGenotype[i]=true;
		}
		
		double currentBest = super.getFitnessFunction().getFitness(p);
		
		//Stage 1: Remove all the parallelisation directives that are not effective
		for(int i=0; i<directivesGenotype.length; i++){
			if(directives[map[i]] instanceof OpenACCParallelLoopDirective){
				if(directivesGenotype[i]){
					directivesGenotype[i] = false;
					setPatch(p,map,directivesGenotype);
					double fitness = super.getFitnessFunction().getFitness(p);
					numberEvaluations++;
					try {
						Utils.appendToFile(logFile, "Testing:\n" + p.getPatch());
						Utils.appendToFile(logFile, "Fitness: " + fitness + " (Best fitness: " + currentBest + ")");
						;
						if (fitness >= currentBest) {
							currentBest = fitness;
							Utils.appendToFile(logFile, "Replacing current best.");
						} else {
							directivesGenotype[i] = true;
							Utils.appendToFile(logFile, "Reverting.");
						}
						Utils.appendToFile(logFile, System.lineSeparator());
					}catch(IOException e){
						System.err.println("Error when trying to create Directive Pruning log.");
						e.printStackTrace();
						System.exit(1);
					}
				}
			}
		}
		
		//Stage 2: Remove all the data directives that are not effective
		for(int i=0; i<directivesGenotype.length; i++){
			if(directives[map[i]].getClass().equals(OpenACCDataDirective.class)) {
				directivesGenotype[i] = false;
				setPatch(p, map, directivesGenotype);
				double fitness = super.getFitnessFunction().getFitness(p);
				numberEvaluations++;
				try {
					Utils.appendToFile(logFile, "Testing:\n" + p.getPatch());
					Utils.appendToFile(logFile, "Fitness: " + fitness + " (Best fitness: " + currentBest + ")");
					;
					if (fitness >= currentBest) {
						currentBest = fitness;
						Utils.appendToFile(logFile, "Replacing current best.");
					} else {
						directivesGenotype[i] = true;
						Utils.appendToFile(logFile, "Reverting.");
					}
					Utils.appendToFile(logFile, System.lineSeparator());
				} catch (IOException e) {
					System.err.println("Error when trying to create Directive Pruning log.");
					e.printStackTrace();
					System.exit(1);
				}
			}
		}

		setPatch(p,map,directivesGenotype);
	}
	
	public int getNumberEvaluations(){
		return this.numberEvaluations;
	}
	

}
