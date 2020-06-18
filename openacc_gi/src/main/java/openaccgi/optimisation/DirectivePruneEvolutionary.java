package openaccgi.optimisation;

import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.simple.SimpleStatistics;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import ec.vector.BitVectorIndividual;
import openaccgi.patch_model.IToolUtils;
import openaccgi.patch_model.Patch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.Random;

/*
This uses a 1+1 ES to find an optimal number of directives to prune.
 */
public class DirectivePruneEvolutionary extends  AOptimiser {

	private final int numEvaluations;
	private final Random rng;
	private final IToolUtils toolUtils;

	public DirectivePruneEvolutionary(IFitnessFunction fitnessFunc,
									  int numEvals, Random random, IToolUtils tUtils, Optional<File> logDir) {
		super(fitnessFunc, logDir);

		this.numEvaluations = numEvals;
		this.rng = random;
		this.toolUtils = tUtils;
		if(logDir.isPresent()){
			System.err.println("Warning: DirectivePruneEvolutionary has yet to implement logging functionality");
		}
	}

	@Override
	public void optimise(Patch p) {
		//Determine genotype length from patch
		int genotypeLength = 0;

		for(int i=0; i < p.getDirectives().length; i++) {
			if(p.getDirectives()[i].isActive()) {
				genotypeLength ++;
			}
		}

		if(genotypeLength == 0 || this.numEvaluations <= 0){
			return;
		}

		//Maps the genotype to the currently active directories
		final int[] genotypeMap = new int[genotypeLength];
		int index=0;
		for(int i=0; i<p.getDirectives().length;i++){
			if(p.getDirectives()[i].isActive()) {
				genotypeMap[index] = i;
				index++;
			}
		}

		//Generate initial population
		boolean[][] initialPopulation = new boolean[1][genotypeLength];
		for(int i=0; i<initialPopulation[0].length; i++){ //One member shows no change
			initialPopulation[0][i]=true;
		}

		for(int i=1; i<initialPopulation.length;i++){ // This remainder have one bit flipped
			int randomPoint = this.rng.nextInt(initialPopulation[i].length);
			for(int j=0; j<initialPopulation[i].length; j++){
				initialPopulation[i][j] = (j != randomPoint);
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
					writer.print("b" + (initialPopulation[i][j] ? 1 : 0) + "|");
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
		dbase.set(new Parameter("pop.subpop.0.species"), "ec.vector.BitVectorSpecies");
		dbase.set(new Parameter("pop.subpop.0.species.fitness"), "ec.simple.SimpleFitness");
		dbase.set(new Parameter("pop.subpop.0.species.ind"), "ec.vector.BitVectorIndividual");
		dbase.set(new Parameter("pop.subpop.0.species.genome-size"), Integer.toString(genotypeLength));
		dbase.set(new Parameter("pop.subpop.0.species.mutation-type"), "flip");
		dbase.set(new Parameter("pop.subpop.0.species.mutation-prob"), Double.toString(1.0 / genotypeLength));
		dbase.set(new Parameter("pop.subpop.0.species.pipe"), "ec.vector.breed.VectorMutationPipeline");
		dbase.set(new Parameter("pop.subpop.0.species.pipe.source.0"), "ec.es.ESSelection");
		dbase.set(new Parameter("eval.problem"), DirectivePruneEvolutionaryECJFitness.class.getName());

		/*
		Annoying hack here: DirectivePruneEvolutionaryECJFitness has FitnessFunction and setPatch as static so I can
		set them before the method is initialized by ECJ
		 */
		DirectivePruneEvolutionaryECJFitness.setFitnessFunction(super.getFitnessFunction());
		DirectivePruneEvolutionaryECJFitness.setPatch(p);
		DirectivePruneEvolutionaryECJFitness.setGenotypeMap(genotypeMap);
		DirectivePruneEvolutionaryECJFitness.setToolUtils(this.toolUtils);

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

		DirectivePruneEvolutionaryECJFitness.applyToPatch(fittest);

		Evolve.cleanup(evaluatedState);
	}
}
