package openaccgi.application;

import openaccgi.patch_model.*;
import org.junit.Test;
import openaccgi.utils.EvaluationExecutables;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertTrue;

public class BasicScriptFitnessFunctionTest {

	@Test //simple tests that no exceptions are thrown
	public void getFitnessTest() throws IOException {
		File script = EvaluationExecutables.getBashScript();
		int lineNo = 1;
		File f = File.createTempFile("test", ".tmp");
		Map<String, VARIABLE_STATUS> vars = new HashMap<String, VARIABLE_STATUS>();
		OpenACCParallelLoopDirective directive = new OpenACCParallelLoopDirective(lineNo, f, vars,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		List<ADirective> directives = new ArrayList<ADirective>();
		directives.add(directive);
		Patch p = new Patch(directives, LANGUAGE.CPP);
		
		BasicScriptFitnessFunction fitnessFunction = new BasicScriptFitnessFunction(script);
		
		double fitnessValue = fitnessFunction.getFitness(p);
		
		assertTrue(fitnessValue > 0);
	}

}
