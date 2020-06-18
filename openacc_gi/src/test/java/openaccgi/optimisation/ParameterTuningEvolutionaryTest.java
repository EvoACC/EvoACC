package openaccgi.optimisation;

import openaccgi.patch_model.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParameterTuningEvolutionaryTest {

	@Test
	public void optimiseTest() throws IOException {
		int lineNo1 = 5;
		File f1 = File.createTempFile("test", ".tmp");
		Map<String, VARIABLE_STATUS> var1 = new HashMap<String, VARIABLE_STATUS>();
		var1.put("varA", VARIABLE_STATUS.NONE);
		var1.put("varB", VARIABLE_STATUS.NONE);
		var1.put("varC", VARIABLE_STATUS.NONE);
		var1.put("varD", VARIABLE_STATUS.NONE);
		Map<String, Map.Entry<String, String>> var1ranges = new HashMap<String, Map.Entry<String, String>>();
		OpenACCParallelLoopDirective dir1 = new OpenACCParallelLoopDirective(lineNo1, f1, var1, var1ranges,
			Optional.empty(), Optional.empty(), Optional.empty());
		
		int lineNo2 = 10;
		File f2 = File.createTempFile("test", ".tmp");
		Map<String, VARIABLE_STATUS> var2 = new HashMap<String, VARIABLE_STATUS>();
		var2.put("varA", VARIABLE_STATUS.NONE);
		var2.put("varB", VARIABLE_STATUS.NONE);
		var2.put("varC", VARIABLE_STATUS.NONE);
		var2.put("varD", VARIABLE_STATUS.NONE);
		Map<String, Map.Entry<String, String>> var2ranges = new HashMap<String, Map.Entry<String, String>>();
		OpenACCParallelLoopDirective dir2 = new OpenACCParallelLoopDirective(lineNo2, f2, var2, var2ranges,
			Optional.empty(), Optional.empty(), Optional.empty());
		
		List<ADirective> directives = new ArrayList<ADirective>();
		directives.add(dir1);
		directives.add(dir2);
		
		Patch p = new Patch(directives, LANGUAGE.CPP);
		
		@SuppressWarnings("unchecked")
		Optional<Integer>[] numGangs = new Optional[2];
		numGangs[0] = Optional.empty();
		numGangs[1] = Optional.of(10);
		
		@SuppressWarnings("unchecked")
		Optional<Integer>[] numWorkers = new Optional[2];
		numWorkers[0] = Optional.of(4);
		numWorkers[1] = Optional.of(0);
		
		@SuppressWarnings("unchecked")
		Optional<Integer>[] vectorLength = new Optional[2];
		vectorLength[0] = Optional.of(5);
		vectorLength[1] = Optional.empty();
		
		CustomParameterFitnessFunction fitnessFunction = new CustomParameterFitnessFunction(
			numGangs, numWorkers, vectorLength);

		ParameterTuningEvolutionary optimiser = new ParameterTuningEvolutionary(
			fitnessFunction, 100, new Random(2), Optional.empty());
		optimiser.optimise(p);
		
		for(int i=0; i<p.getDirectives().length; i++){
			assertTrue(p.getDirectives()[i] instanceof OpenACCParallelLoopDirective);
			assertEquals(numGangs[i], ((OpenACCParallelLoopDirective)p.getDirectives()[i]).getNumGangs());
			assertEquals(numWorkers[i], ((OpenACCParallelLoopDirective)p.getDirectives()[i]).getNumWorkers());
			assertEquals(vectorLength[i], ((OpenACCParallelLoopDirective)p.getDirectives()[i]).getVectorLength());
		}
		
	}

}
