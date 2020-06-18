package openaccgi.optimisation;

import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.simple.SimpleStatistics;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import ec.vector.BitVectorIndividual;
import openaccgi.Utils;
import openaccgi.patch_model.ADirective;
import openaccgi.patch_model.DataDirectiveInsertionPoint;
import openaccgi.patch_model.IToolUtils;
import openaccgi.patch_model.Patch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class DataOptimiserEvolutionary extends AOptimiser{
	
	private final int numEvaluations;
	private final int populationSize;
	private final Random rng;
	private final IToolUtils toolUtils;

	public DataOptimiserEvolutionary(IFitnessFunction fitnessFunc, int numEvals, int popSize,
	                                 Random random, Optional<File> logDir, IToolUtils t) {
		super(fitnessFunc, logDir);
		this.numEvaluations = numEvals;
		this.populationSize = popSize;
		this.rng = random;
		this.toolUtils = t;
	}
	
	private boolean unique(boolean[][] set, boolean[] toTest) {
		for(boolean[] t : set) {
			boolean differ=false;
			for(int i=0; i < t.length; i++) {
				if(t[i] != toTest[i]) {
					differ = true;
					break;
				}
			}
			if(!differ){
				return false;
			}
		}
		return true;
	}

	@Override
	public void optimise(Patch p) {

		Optional<File> insertionPointLog;
		Optional<File> evolutionLog;
		Optional<File> evolutionLogDir;
		Optional<File> bestIndLog;
		if(this.logDirectory.isPresent()){
			insertionPointLog = Optional.of(new File(this.logDirectory.get().getAbsolutePath()
				+ File.separator + "insertion_point.dat"));
			evolutionLog = Optional.of(new File(this.logDirectory.get().getAbsolutePath()
				+ File.separator + "evolution_log.dat"));
			bestIndLog = Optional.of(new File(this.logDirectory.get().getAbsolutePath()
				+ File.separator + "best_individual.dat"));
			evolutionLogDir = Optional.of(new File(this.logDirectory.get().getAbsolutePath()
				+ File.separator + "evolution_patches"));
			evolutionLogDir.get().mkdir();
		} else {
			insertionPointLog = Optional.empty();
			evolutionLog = Optional.empty();
			bestIndLog = Optional.empty();
			evolutionLogDir = Optional.empty();
		}
		
		//Get the valid insertion points
		SortedSet<DataDirectiveInsertionPoint> validInsertionPoints = new TreeSet<DataDirectiveInsertionPoint>();
		
		Map<File, List<ADirective>> presentDirectives = new HashMap<File, List<ADirective>>();
		for(ADirective dir : p.getDirectives()){
			if(!presentDirectives.containsKey(dir.getFile())){
				presentDirectives.put(dir.getFile(), new ArrayList<ADirective>());
			}
			presentDirectives.get(dir.getFile()).add(dir);
		}
		
		for(Map.Entry<File, List<ADirective>> e : presentDirectives.entrySet()){
			try{
			validInsertionPoints.addAll(this.toolUtils.getDataDirectiveInsertionPoints(e.getKey(),
				((ADirective[])e.getValue().toArray(new ADirective[e.getValue().size()]))));
			} catch (IOException ex){
				System.err.println("Error when getting Data directive insertion points during optimisation.");
				System.err.println(ex.getMessage());
				System.exit(1);
			}
		}
		
		//Determine genotype length
		int genotypeLength =  validInsertionPoints.size();

		if(insertionPointLog.isPresent()){
			for(DataDirectiveInsertionPoint dataDirectiveInsertionPoint : validInsertionPoints){
				String toAppend = dataDirectiveInsertionPoint.getFile().getAbsolutePath() + ","
					+ dataDirectiveInsertionPoint.getStartLineNumber() + ","
					+ dataDirectiveInsertionPoint.getEndLineNumber();
				try {
					Utils.appendToFile(new File(insertionPointLog.get().getAbsolutePath()), toAppend);
				}catch(IOException e){
					System.err.println("Failed to write to the data directive optimisation log.");
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		if(genotypeLength > 0){
			//Generate initial population
			boolean[][] initialPopulation = new boolean[this.populationSize][genotypeLength];
			for(int i=0; i<initialPopulation[0].length; i++){ //One member shows no change
				initialPopulation[0][i]=false;
			}
			
			for(int i=1; i<initialPopulation.length;i++){ 
				
				boolean[] candidate = new boolean[initialPopulation[i].length];

				int limit = 100;
				int curVal = 0;
				do {
					if(curVal == limit){
						break;
					}
					curVal++;
					for(int j=0; j< candidate.length;j++) {
						candidate[j] = (1.0 / ((double)genotypeLength)) > this.rng.nextDouble();
					}
				} while (!this.unique(initialPopulation, candidate));
				
				initialPopulation[i] = candidate;
			}
			
			File populationFile = null;
			try {
				populationFile = File.createTempFile("population", ".tmp");
				PrintWriter writer = new PrintWriter(populationFile);
				writer.println("Number of Subpopulations: i1|");
				writer.println("Subpopulation Number: i0|");
				writer.println("Number of Individuals: i" + this.populationSize + "|");
				for(int i=0; i<initialPopulation.length;i++){
					writer.println("Individual Number: i"+i+"|");
					writer.println("Evaluated: F");
					writer.println("Fitness: d0|0.0|");
					writer.print("i" + initialPopulation[i].length);
					for(int j=0; j<initialPopulation[i].length;j++){
						writer.print("|b" + (initialPopulation[i][j] ? 1: 0));
					}
					writer.print("|");
					writer.println();
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			assert(populationFile != null);
			
			// Setup the ECJ parameters
			ParameterDatabase dbase = new ParameterDatabase();
			dbase.set(new Parameter("breedthreads"), "1");
			dbase.set(new Parameter("evalthreads"), "1");
			dbase.set(new Parameter("seed.0"), Integer.toString(this.rng.nextInt()));
			
			dbase.set(new Parameter("state"),"ec.simple.SimpleEvolutionState");
			
			dbase.set(new Parameter("pop"), "ec.Population");
			dbase.set(new Parameter("init"), "ec.simple.SimpleInitializer");
			dbase.set(new Parameter("finish"), "ec.simple.SimpleFinisher");
			dbase.set(new Parameter("breed"), "ec.simple.SimpleBreeder");
			dbase.set(new Parameter("eval"), "ec.simple.SimpleEvaluator");
			dbase.set(new Parameter("stat"), "ec.simple.SimpleStatistics");
			dbase.set(new Parameter("exch"), "ec.simple.SimpleExchanger");
			
			dbase.set(new Parameter("pop.file"), populationFile.getAbsolutePath());
			
			dbase.set(new Parameter("evaluations"), Integer.toString(this.numEvaluations));
			dbase.set(new Parameter("checkpoint"), "false");
			dbase.set(new Parameter("checkpoint-prefix"), "ec");
			dbase.set(new Parameter("checkpoint-modulo"), "1");
			
			dbase.set(new Parameter("pop.subpops"), "1");
			dbase.set(new Parameter("pop.subpop.0"), "ec.Subpopulation");

			dbase.set(new Parameter("pop.default-subpop"), "0");
			dbase.set(new Parameter("breed.elite.0"), "1");
			dbase.set(new Parameter("breed.reevaluate-elites.0"), "false");
			
			dbase.set(new Parameter("pop.subpop.0.size"), Integer.toString(this.populationSize));
			dbase.set(new Parameter("pop.subpop.0.species"), "ec.vector.BitVectorSpecies");
			dbase.set(new Parameter("pop.subpop.0.duplicate-retries"), "0");
			
			dbase.set(new Parameter("pop.subpop.0.species.fitness"), "ec.simple.SimpleFitness");
			dbase.set(new Parameter("pop.subpop.0.species.ind"), "ec.vector.BitVectorIndividual");
			
			dbase.set(new Parameter("pop.subpop.0.species.genome-size"), Integer.toString(genotypeLength));
			//Used to be 'uniform' but it wouldn't work for come reason. One-point appears to work
			dbase.set(new Parameter("pop.subpop.0.species.crossover-type"), "one");
			dbase.set(new Parameter("pop.subpop.0.species.mutation-type"), "flip");
			dbase.set(new Parameter("pop.subpop.0.species.mutation-prob"), Double.toString(1.0 / genotypeLength));
			
			dbase.set(new Parameter("pop.subpop.0.species.pipe"), "ec.vector.breed.VectorMutationPipeline");
			dbase.set(new Parameter("pop.subpop.0.species.pipe.source.0"), "ec.vector.breed.VectorCrossoverPipeline");
			dbase.set(new Parameter("pop.subpop.0.species.pipe.source.0.source.0"), "ec.select.TournamentSelection");
			dbase.set(new Parameter("pop.subpop.0.species.pipe.source.0.source.1"), "ec.select.TournamentSelection");
			
			dbase.set(new Parameter("select.tournament.size"), Integer.toString(Math.max(this.populationSize / 10, 2)));
			
			dbase.set(new Parameter("eval.problem"), DataOptimiserEvolutionaryECJFitness.class.getName());
			
			/*
			Annoying hack here: DataOptimiserEvolutionaryECJFitness has FitnessFunction and setPatch as static so I
			can set them before the method is initialized by ECJ
			 */
			DataOptimiserEvolutionaryECJFitness.setFitnessFunction(super.getFitnessFunction());
			DataOptimiserEvolutionaryECJFitness.setPatch(p);
			DataOptimiserEvolutionaryECJFitness.setInsertionPoints(validInsertionPoints);
			DataOptimiserEvolutionaryECJFitness.setToolUtils(toolUtils);

			Output output = Evolve.buildOutput();
			output.getLog(0).silent = true;
			output.getLog(1).silent = true;
			
			//Run Evolution
			EvolutionState evaluatedState = Evolve.initialize(dbase, 0, output);
			evaluatedState.startFresh();

			int genLog = 0;
			logGeneration(genLog, evaluatedState, evolutionLog, evolutionLogDir);

			int result = EvolutionState.R_NOTDONE;
			while(result == EvolutionState.R_NOTDONE){
				result = evaluatedState.evolve();
				genLog++;
				logGeneration(genLog, evaluatedState, evolutionLog, evolutionLogDir);
			}
			
			//Get the best individual and set it to the patch
			Individual[] inds = ((SimpleStatistics)(evaluatedState.statistics)).best_of_run;


			
			assert(inds[0] instanceof BitVectorIndividual);
			
			BitVectorIndividual fittest = (BitVectorIndividual)inds[0];
			DataOptimiserEvolutionaryECJFitness.applyToPatch(fittest);
			DataOptimiserEvolutionaryECJFitness.close();

			if(bestIndLog.isPresent()){
				double fitness =inds[0].fitness.fitness();
				String genotype = inds[0].genotypeToString();
				try{
					Utils.appendToFile(bestIndLog.get(), "Genotype: " + genotype);
					Utils.appendToFile(bestIndLog.get(), "Fitness: " + Double.toString(fitness));
					Utils.appendToFile(bestIndLog.get(), "Patch:" + System.lineSeparator() + p.getPatch());
				} catch (IOException e) {
					System.out.println("Error when trying to log during the data optimisation stage.");
					e.printStackTrace();
					System.exit(1);
				}
			}
				
			Evolve.cleanup(evaluatedState);
		}
	}

	private static void logGeneration(int genNo, EvolutionState evaluatedState, Optional<File> evolutionLog,
	                                  Optional<File> evolutionLogDir){
		if(evolutionLog.isPresent()){
			for(int i=0; i<evaluatedState.population.subpops[0].individuals.length; i++) {
				Individual individual = evaluatedState.population.subpops[0].individuals[i];
				double fitness = individual.fitness.fitness();
				String genotype = individual.genotypeToString();
				DataOptimiserEvolutionaryECJFitness.applyToPatch(individual);
				String patch = DataOptimiserEvolutionaryECJFitness.getPatch().getPatch();
				DataOptimiserEvolutionaryECJFitness.removeRecentlyAddedDirectives();
				try {
					Utils.appendToFile(evolutionLog.get(), genNo + "," + i + ","
						+ genotype + "," + Double.toString(fitness));
					if(evolutionLogDir.isPresent()){
						Utils.appendToFile(new File(evolutionLogDir.get() + File.separator + "gen_" + genNo
							+ "_ind_" + i +".patch"), patch);
					}
				} catch (IOException e) {
					System.out.println("Error when trying to log during the data optimisation stage.");
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
	}
}