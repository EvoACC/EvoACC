package openaccgi.optimisation;

import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.simple.SimpleStatistics;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import ec.vector.BitVectorIndividual;
import openaccgi.patch_model.ADirective;
import openaccgi.patch_model.DataDirectiveInsertionPoint;
import openaccgi.patch_model.IToolUtils;
import openaccgi.patch_model.Patch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class DataOptimiserEvolutionaryStrategy extends AOptimiser{
	
	private final int numEvaluations;
	private final Random rng;
	private final IToolUtils toolUtils;

	public DataOptimiserEvolutionaryStrategy(IFitnessFunction fitnessFunc, int numEvals,
	                                         Random random, Optional<File> logDir, IToolUtils t) {
		super(fitnessFunc, logDir);
		this.rng = random;
		this.toolUtils = t;
		this.numEvaluations = numEvals;

		if(logDir.isPresent()){
			System.err.println("Warning: DataOptimiserEvolutionaryStrategy has yet to implement logging functionality");
		}
	}

	@Override
	public void optimise(Patch p) {
		
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
			try {
				validInsertionPoints.addAll(this.toolUtils.getDataDirectiveInsertionPoints(e.getKey(),
					((ADirective[]) e.getValue().toArray(new ADirective[e.getValue().size()]))));
			} catch (IOException ex){
				System.err.println("Error when getting Data directive insertion points during optimisation: ");
				System.err.println(ex.getMessage());
				System.exit(1);
			}
		}
		
		//Determine genotype length
		int genotypeLength =  validInsertionPoints.size();
		
		if(genotypeLength > 0){
			//Generate initial population
			boolean[][] initialPopulation = new boolean[1][genotypeLength];
			for(int i=0; i<initialPopulation[0].length; i++){ //One member shows no change
				initialPopulation[0][i]=false;
			}
			
			for(int i=1; i<initialPopulation.length;i++){ 
				for(int j=0; j<initialPopulation[i].length;j++){
					//The remainder are just random --- TODO: is this the best initialisation strategy?
					initialPopulation[i][j]=this.rng.nextBoolean();
				}
			}
			
			File populationFile = null;
			try {
				populationFile = File.createTempFile("population", ".tmp");
				PrintWriter writer = new PrintWriter(populationFile);
				writer.println("Number of Subpopulations: i1|");
				writer.println("Subpopulation Number: i0|");
				writer.println("Number of Individuals: i1|");
				for(int i=0; i<initialPopulation.length;i++){
					writer.println("Individual Number: i"+i+"|");
					writer.println("Evaluated: F");
					writer.println("Fitness: d0|0.0|");
					writer.print("i" + initialPopulation[i].length + "|");
					for(int j=0; j<initialPopulation[i].length;j++){
						writer.print("b" + (initialPopulation[i][j] ? 1: 0) + "|");
					}
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
			dbase.set(new Parameter("breed"), "ec.es.MuPlusLambdaBreeder");
			dbase.set(new Parameter("eval"), "ec.simple.SimpleEvaluator");
			dbase.set(new Parameter("stat"), "ec.simple.SimpleStatistics");
			dbase.set(new Parameter("exch"), "ec.simple.SimpleExchanger");
			dbase.set(new Parameter("pop.file"), populationFile.getAbsolutePath());
			dbase.set(new Parameter("evaluations"), Integer.toString(this.numEvaluations));
			dbase.set(new Parameter("checkpoint"), "false");
			dbase.set(new Parameter("checkpoint-prefix"), "ec");
			dbase.set(new Parameter("checkpoint-modulo"), "1");
			dbase.set(new Parameter("es.mu.0"), "1");
			dbase.set(new Parameter("es.lambda.0"), "1");
			dbase.set(new Parameter("pop.subpops"), "1");
			dbase.set(new Parameter("pop.subpop.0"), "ec.Subpopulation");
			dbase.set(new Parameter("pop.subpop.0.size"), "1");
			dbase.set(new Parameter("pop.subpop.0.duplicate-retries"), "100");
			dbase.set(new Parameter("pop.subpop.0.species"), "ec.vector.IntegerVectorSpecies");
			dbase.set(new Parameter("pop.subpop.0.species.fitness"), "ec.simple.SimpleFitness");
			dbase.set(new Parameter("pop.subpop.0.species.ind"), "ec.vector.IntegerVectorIndividual");
			dbase.set(new Parameter("pop.subpop.0.species.genome-size"), Integer.toString(genotypeLength));
			dbase.set(new Parameter("vector.species.max-gene"), Integer.toString(5));
			dbase.set(new Parameter("vector.species.min-gene"), Integer.toString(0));
			dbase.set(new Parameter("pop.subpop.0.species.mutation-type"), "reset");
			dbase.set(new Parameter("pop.subpop.0.species.mutation-prob"), Double.toString(1.0 / genotypeLength));
			dbase.set(new Parameter("pop.subpop.0.species.pipe"), "ec.vector.breed.VectorMutationPipeline");
			dbase.set(new Parameter("pop.subpop.0.species.pipe.source.0"), "ec.es.ESSelection");
			dbase.set(new Parameter("eval.problem"), DataOptimiserEvolutionaryECJFitness.class.getName());
			
			/*
			Annoying hack here: DataOptimiserEvolutionaryECJFitness has FitnessFunction and setPatch as static so I can
			set them before the method is initialized by ECJ.
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
			evaluatedState.population.printPopulation(evaluatedState, 1);
			int result = EvolutionState.R_NOTDONE;
			while(result == EvolutionState.R_NOTDONE){
				result = evaluatedState.evolve();
				evaluatedState.population.printPopulation(evaluatedState, 1);
			}
			
			//Get the best individual and set it to the patch
			Individual[] inds = ((SimpleStatistics)(evaluatedState.statistics)).best_of_run;
			
			assert(inds[0] instanceof BitVectorIndividual);
			
			BitVectorIndividual fittest = (BitVectorIndividual)inds[0];
			
			DataOptimiserEvolutionaryECJFitness.applyToPatch(fittest);
				
			Evolve.cleanup(evaluatedState);
		}
	}
}