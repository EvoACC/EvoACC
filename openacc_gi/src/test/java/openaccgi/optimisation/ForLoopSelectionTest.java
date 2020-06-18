package openaccgi.optimisation;

import openaccgi.patch_model.*;
import org.junit.Test;
import openaccgi.program_data.CSVDataContainer;
import openaccgi.utils.InputFileGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class ForLoopSelectionTest {
	
	//TODO: In both these tests the content of the MockToolUtils is tightly coupled with the InputFileGenerator.getRealF1() content. Fix this
	
	@Test
	public void basicOptimisationTest_MaxForFitnessFunction() throws AccessControlException, FileNotFoundException, IOException {
		GreedyDirectiveSelection loopSelection =
			new GreedyDirectiveSelection(new MaxDirectivesFitnessFunction(), Optional.empty());
		CSVDataContainer container = new CSVDataContainer(InputFileGenerator.getRealF1(), true);
		
		File f = null;
		assertEquals(1, container.getFiles().size());
		f = container.getFiles().iterator().next();
		
		MockToolUtils toolUtils = new MockToolUtils();
		
		Map<String, VARIABLE_STATUS> m1 = new HashMap<String, VARIABLE_STATUS>();
		m1.put("toReturn", VARIABLE_STATUS.COPY);
		toolUtils.addDataInfo(f, 16, m1);
		
		Map<String, VARIABLE_STATUS> m2 = new HashMap<String, VARIABLE_STATUS>();
		m2.put("toReturn", VARIABLE_STATUS.COPYIN);
		m2.put("input", VARIABLE_STATUS.COPY);
		toolUtils.addDataInfo(f, 18, m2);
		
		Map<String, VARIABLE_STATUS> m3 = new HashMap<String, VARIABLE_STATUS>();
		m3.put("toReturn", VARIABLE_STATUS.CREATE);
		toolUtils.addDataInfo(f, 23, m3);
		
		Map<String, VARIABLE_STATUS> m4 = new HashMap<String, VARIABLE_STATUS>();
		m4.put("input", VARIABLE_STATUS.NONE);
		toolUtils.addDataInfo(f, 25, m4);
		
		Patch patch = PatchFactory.createPatchWithAllLoopsParallelised(container, LANGUAGE.CPP, toolUtils);
		
		loopSelection.optimise(patch);
		
		for(ADirective dir: patch.getDirectives()){
			assertTrue(dir.isActive());
		}
	}

	@Test
	public void basicOptimisationTest_NoDirectivesAllowedFitnessFunction() throws AccessControlException, FileNotFoundException, IOException{
		GreedyDirectiveSelection loopSelection =
			new GreedyDirectiveSelection(new NoDirectivesAllowedFitnessFunction(), Optional.empty());
		CSVDataContainer container = new CSVDataContainer(InputFileGenerator.getRealF1(), true);
		
		assertEquals(1,container.getFiles().size());
		File f = container.getFiles().iterator().next();
		
		MockToolUtils toolUtils = new MockToolUtils();
		
		Map<String, VARIABLE_STATUS> m1 = new HashMap<String, VARIABLE_STATUS>();
		m1.put("toReturn", VARIABLE_STATUS.COPY);
		toolUtils.addDataInfo(f, 16, m1);
		
		Map<String, VARIABLE_STATUS> m2 = new HashMap<String, VARIABLE_STATUS>();
		m2.put("toReturn", VARIABLE_STATUS.COPYIN);
		m2.put("input", VARIABLE_STATUS.COPY);
		toolUtils.addDataInfo(f, 18, m2);
		
		Map<String, VARIABLE_STATUS> m3 = new HashMap<String, VARIABLE_STATUS>();
		m3.put("toReturn", VARIABLE_STATUS.CREATE);
		toolUtils.addDataInfo(f, 23, m3);
		
		Map<String, VARIABLE_STATUS> m4 = new HashMap<String, VARIABLE_STATUS>();
		m4.put("input", VARIABLE_STATUS.NONE);
		toolUtils.addDataInfo(f, 25, m4);
		
		Patch patch = PatchFactory.createPatchWithAllLoopsParallelised(container, LANGUAGE.CPP, toolUtils);
		
		loopSelection.optimise(patch);
		
		for(ADirective dir: patch.getDirectives()){
			assertFalse(dir.isActive());
		}
	}

}
