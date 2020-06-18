package openaccgi.optimisation;

import openaccgi.patch_model.Patch;

public interface IFitnessFunction {
	//Contract Constraint #1: Higher is better, < 0 implies the breaking of hard constraints
	//Contract Constraint #2: if p.getPatch().isEmpty() then the script must get the fitness of the target without any
	//modifications
	public double getFitness(Patch p);
}
