package openaccgi.optimisation;

import openaccgi.patch_model.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DirectivePruneTest {

	@Test
	public void optimiseTest_allRequired() throws IOException {
		int lineNo1 = 5;
		File f1 = File.createTempFile("test", ".tmp");
		Map<String, VARIABLE_STATUS>  vars1 = new HashMap<String, VARIABLE_STATUS>();
		vars1.put("var1", VARIABLE_STATUS.PRESENT);
		vars1.put("var2", VARIABLE_STATUS.PRESENT);
		vars1.put("var3", VARIABLE_STATUS.PRESENT);
		OpenACCParallelLoopDirective dir1 = new OpenACCParallelLoopDirective(lineNo1, f1, vars1,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		
		int lineNo2 = 10;
		Map<String, VARIABLE_STATUS>  vars2 = new HashMap<String, VARIABLE_STATUS>();
		vars2.put("var1", VARIABLE_STATUS.PRESENT);
		OpenACCParallelLoopDirective dir2 = new OpenACCParallelLoopDirective(lineNo2, f1, vars2,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		
		Map<String, VARIABLE_STATUS> dataDirVars = new HashMap<String, VARIABLE_STATUS>();
		dataDirVars.put("var1", VARIABLE_STATUS.CREATE);
		dataDirVars.put("var2", VARIABLE_STATUS.CREATE);
		dataDirVars.put("var3", VARIABLE_STATUS.CREATE);
		dataDirVars.put("varA", VARIABLE_STATUS.CREATE);

		
		Map<String, OpenACCUpdateDirective.UPDATE_TYPE> updateVars = new HashMap<String, OpenACCUpdateDirective.UPDATE_TYPE>();
		updateVars.put("var1", OpenACCUpdateDirective.UPDATE_TYPE.HOST);
		OpenACCUpdateDirective up = new OpenACCUpdateDirective(6, f1, updateVars);

		Set<OpenACCUpdateDirective> updateDirectives = new HashSet<OpenACCUpdateDirective>();
		updateDirectives.add(up);
		OpenACCDataDirective dir3 = new OpenACCDataDirective(4, 11, f1, dataDirVars,
				new HashMap<String, Map.Entry<String, String>>(),updateDirectives);
		
		List<ADirective> directives = new ArrayList<ADirective>();
		directives.add(dir1);
		directives.add(dir2);
		directives.add(dir3);

		MockToolUtils toolUtils = new MockToolUtils();

		SortedSet<DataDirectiveInsertionPoint> insertionPoints = new TreeSet<DataDirectiveInsertionPoint>();
		insertionPoints.add(new DataDirectiveInsertionPoint(f1,4,11));
		toolUtils.addDataDirectiveInsertionPoint(f1, insertionPoints);

		toolUtils.addDataDirective(f1,4,11,Optional.of(dir3));

		toolUtils.addDataInfo(f1,5,dir1.getVariables());
		toolUtils.addDataInfo(f1,10,dir2.getVariables());
		toolUtils.addDataInfo(f1,4,dir3.getVariables());
		
		Patch p = new Patch(directives, LANGUAGE.CPP);
		
		MaxDirectivesFitnessFunction fitnessFunction = new MaxDirectivesFitnessFunction();
		DirectivePrune delta = new DirectivePrune(fitnessFunction, toolUtils,Optional.empty());
		
		delta.optimise(p);
		
		for(ADirective dir : p.getDirectives()){
			assertTrue(dir.isActive());
		}
	}

	@Test
	public void optimiseTest_removeDataDirective() throws IOException {
		//Similare to 'optimisetest_allRequired() but the loops do not share any data
		int lineNo1 = 5;
		File f1 = File.createTempFile("test", ".tmp");
		Map<String, VARIABLE_STATUS>  vars1 = new HashMap<String, VARIABLE_STATUS>();
		vars1.put("var1", VARIABLE_STATUS.PRESENT);
		vars1.put("var2", VARIABLE_STATUS.PRESENT);
		vars1.put("var3", VARIABLE_STATUS.PRESENT);
		OpenACCParallelLoopDirective dir1 = new OpenACCParallelLoopDirective(lineNo1, f1, vars1,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());

		int lineNo2 = 10;
		Map<String, VARIABLE_STATUS>  vars2 = new HashMap<String, VARIABLE_STATUS>();
		vars2.put("varA", VARIABLE_STATUS.PRESENT);
		OpenACCParallelLoopDirective dir2 = new OpenACCParallelLoopDirective(lineNo2, f1, vars2,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());

		Map<String, VARIABLE_STATUS> dataDirVars = new HashMap<String, VARIABLE_STATUS>();
		dataDirVars.put("var1", VARIABLE_STATUS.CREATE);
		dataDirVars.put("var2", VARIABLE_STATUS.CREATE);
		dataDirVars.put("var3", VARIABLE_STATUS.CREATE);
		dataDirVars.put("varA", VARIABLE_STATUS.CREATE);


		Map<String, OpenACCUpdateDirective.UPDATE_TYPE> updateVars = new HashMap<String, OpenACCUpdateDirective.UPDATE_TYPE>();
		updateVars.put("var1", OpenACCUpdateDirective.UPDATE_TYPE.HOST);
		OpenACCUpdateDirective up = new OpenACCUpdateDirective(6, f1, updateVars);

		Set<OpenACCUpdateDirective> updateDirectives = new HashSet<OpenACCUpdateDirective>();
		updateDirectives.add(up);
		OpenACCDataDirective dir3 = new OpenACCDataDirective(4, 11, f1, dataDirVars,
				new HashMap<String, Map.Entry<String, String>>(), updateDirectives);

		List<ADirective> directives = new ArrayList<ADirective>();
		directives.add(dir1);
		directives.add(dir2);
		directives.add(dir3);

		MockToolUtils toolUtils = new MockToolUtils();

		SortedSet<DataDirectiveInsertionPoint> insertionPoints = new TreeSet<DataDirectiveInsertionPoint>();
		toolUtils.addDataDirectiveInsertionPoint(f1, insertionPoints);

		toolUtils.addDataDirective(f1,4,11,Optional.of(dir3));

		toolUtils.addDataInfo(f1,5,dir1.getVariables());
		toolUtils.addDataInfo(f1,10,dir2.getVariables());
		toolUtils.addDataInfo(f1,4,dir3.getVariables());

		Patch p = new Patch(directives, LANGUAGE.CPP);

		MaxDirectivesFitnessFunction fitnessFunction = new MaxDirectivesFitnessFunction();
		DirectivePrune delta = new DirectivePrune(fitnessFunction, toolUtils, Optional.empty());

		delta.optimise(p);


		assertTrue(dir1.isActive());
		assertTrue(dir2.isActive());
		assertFalse(dir3.isActive());
	}
	
	@Test
	public void optimiseTest_noneRequired() throws IOException {
		int lineNo1 = 5;
		File f1 = File.createTempFile("test", ".tmp");
		Map<String, VARIABLE_STATUS>  vars1 = new HashMap<String, VARIABLE_STATUS>();
		vars1.put("var1", VARIABLE_STATUS.PRESENT);
		vars1.put("var2", VARIABLE_STATUS.PRESENT);
		vars1.put("var3", VARIABLE_STATUS.PRESENT);
		OpenACCParallelLoopDirective dir1 = new OpenACCParallelLoopDirective(lineNo1, f1, vars1,
				new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		
		int lineNo2 = 10;
		Map<String, VARIABLE_STATUS>  vars2 = new HashMap<String, VARIABLE_STATUS>();
		vars2.put("varA", VARIABLE_STATUS.PRESENT);
		OpenACCParallelLoopDirective dir2 = new OpenACCParallelLoopDirective(lineNo2, f1, vars2,
				new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		
		Map<String, VARIABLE_STATUS> dataDirVars = new HashMap<String, VARIABLE_STATUS>();
		dataDirVars.put("var1", VARIABLE_STATUS.CREATE);
		dataDirVars.put("var2", VARIABLE_STATUS.CREATE);
		dataDirVars.put("var3", VARIABLE_STATUS.CREATE);
		dataDirVars.put("varA", VARIABLE_STATUS.CREATE);

		
		Map<String, OpenACCUpdateDirective.UPDATE_TYPE> updateVars = new HashMap<String, OpenACCUpdateDirective.UPDATE_TYPE>();
		updateVars.put("var1", OpenACCUpdateDirective.UPDATE_TYPE.HOST);
		OpenACCUpdateDirective up = new OpenACCUpdateDirective(6, f1, updateVars);

		Set<OpenACCUpdateDirective> updateDirectives = new HashSet<OpenACCUpdateDirective>();
		updateDirectives.add(up);
		OpenACCDataDirective dir3 = new OpenACCDataDirective(4, 11, f1, dataDirVars,
				new HashMap<String, Map.Entry<String, String>>(), updateDirectives);
		
		List<ADirective> directives = new ArrayList<ADirective>();
		directives.add(dir1);
		directives.add(dir2);
		directives.add(dir3);

		MockToolUtils toolUtils = new MockToolUtils();

		SortedSet<DataDirectiveInsertionPoint> insertionPoints = new TreeSet<DataDirectiveInsertionPoint>();
		toolUtils.addDataDirectiveInsertionPoint(f1, insertionPoints);

		toolUtils.addDataInfo(f1,5,dir1.getVariables());
		toolUtils.addDataInfo(f1,10,dir2.getVariables());
		toolUtils.addDataInfo(f1,4,dir3.getVariables());

		Patch p = new Patch(directives, LANGUAGE.CPP);
		
		MinDirectivesFitnessFunction fitnessFunction = new MinDirectivesFitnessFunction();
		DirectivePrune delta = new DirectivePrune(fitnessFunction, toolUtils, Optional.empty());
		
		delta.optimise(p);
		
		for(ADirective dir : p.getDirectives()){
			assertFalse(dir.isActive());
		}
	}
}
