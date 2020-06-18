package openaccgi.patch_model;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PatchTest {

	@Test
	public void getPatchTest_oneDirective() throws IOException {
		File f = File.createTempFile("test", ".tmp");
		Map<String, VARIABLE_STATUS> variables = new HashMap<String, VARIABLE_STATUS>();
		OpenACCParallelLoopDirective parallelLoop = new OpenACCParallelLoopDirective(5, f, variables,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		parallelLoop.setVariable("var1", VARIABLE_STATUS.COPY);
		parallelLoop.setNumGangs(Optional.of(2));
		List<ADirective> directives = new ArrayList<ADirective>();
		directives.add(parallelLoop);
		
		Patch p = new Patch(directives, LANGUAGE.CPP);
		
		String expected = "--- " + f.getAbsolutePath() + System.lineSeparator()
						+ "+++ " + f.getAbsolutePath() + System.lineSeparator()
						+ "@@ -4,0 +4,1 @@" + System.lineSeparator()
						+ "+#pragma acc parallel loop num_gangs(4) pcopy(var1) " + System.lineSeparator();
		
		assertEquals(expected, p.getPatch());
	}
	
	@Test
	public void getPatchTest_twoDirectives() throws IOException {
		File f1 = File.createTempFile("testA", ".tmp");
		Map<String, VARIABLE_STATUS> variables1 = new HashMap<String, VARIABLE_STATUS>();
		OpenACCParallelLoopDirective parallelLoop1 = new OpenACCParallelLoopDirective(5, f1, variables1,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		parallelLoop1.setVariable("var1", VARIABLE_STATUS.COPY);
		parallelLoop1.setVariable("var2", VARIABLE_STATUS.NONE);
		parallelLoop1.setVariable("var3", VARIABLE_STATUS.NONE);
		parallelLoop1.setNumGangs(Optional.of(2));
		
		File f2 = File.createTempFile("testB", ".tmp");
		Map<String, VARIABLE_STATUS> variables2 = new HashMap<String, VARIABLE_STATUS>();
		OpenACCParallelLoopDirective parallelLoop2 = new OpenACCParallelLoopDirective(10, f2, variables2,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		parallelLoop2.setVariable("varA", VARIABLE_STATUS.COPYIN);
		parallelLoop2.setVariable("varB", VARIABLE_STATUS.COPYOUT);
		parallelLoop2.setVariable("varC", VARIABLE_STATUS.COPYIN);
		parallelLoop2.setVectorLength(Optional.of(6));
		parallelLoop2.setNumWorkers(Optional.of(6));
		
		List<ADirective> directives = new ArrayList<ADirective>();
		directives.add(parallelLoop1);
		directives.add(parallelLoop2);
		
		Patch p = new Patch(directives, LANGUAGE.CPP);
		
		String expected = "--- " + f1.getAbsolutePath() + System.lineSeparator()
			+ "+++ " + f1.getAbsolutePath() + System.lineSeparator()
			+ "@@ -4,0 +4,1 @@" + System.lineSeparator()
			+ "+#pragma acc parallel loop num_gangs(4) pcopy(var1) " + System.lineSeparator()
			+ "--- " + f2.getAbsolutePath() + System.lineSeparator()
			+ "+++ " + f2.getAbsolutePath() + System.lineSeparator()
			+ "@@ -9,0 +9,1 @@" + System.lineSeparator()
			+ "+#pragma acc parallel loop num_workers(64) vector_length(64) pcopyin(varC,varA)"
			+ " pcopyout(varB) " + System.lineSeparator();
						
		
		assertEquals(expected, p.getPatch());
	}
	
	@Test
	public void getPatchTest_twoDirectives_oneInactive() throws IOException {
		File f1 = File.createTempFile("testA", ".tmp");
		Map<String, VARIABLE_STATUS> variables1 = new HashMap<String, VARIABLE_STATUS>();
		OpenACCParallelLoopDirective parallelLoop1 = new OpenACCParallelLoopDirective(5, f1, variables1,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		parallelLoop1.setVariable("var1", VARIABLE_STATUS.COPY);
		parallelLoop1.setVariable("var2", VARIABLE_STATUS.NONE);
		parallelLoop1.setVariable("var3", VARIABLE_STATUS.NONE);
		parallelLoop1.setNumGangs(Optional.of(2));
		
		File f2 = File.createTempFile("testB", ".tmp");
		Map<String, VARIABLE_STATUS> variables2 = new HashMap<String, VARIABLE_STATUS>();
		OpenACCParallelLoopDirective parallelLoop2 = new OpenACCParallelLoopDirective(10, f2, variables2,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		parallelLoop2.setVariable("varA", VARIABLE_STATUS.COPYIN);
		parallelLoop2.setVariable("varB", VARIABLE_STATUS.COPYOUT);
		parallelLoop2.setVariable("varC", VARIABLE_STATUS.COPYIN);
		parallelLoop2.setVectorLength(Optional.of(6));
		parallelLoop2.setNumWorkers(Optional.of(6));
		
		parallelLoop2.setActive(false);
		
		List<ADirective> directives = new ArrayList<ADirective>();
		directives.add(parallelLoop1);
		directives.add(parallelLoop2);
		
		Patch p = new Patch(directives, LANGUAGE.CPP);
		
		String expected = "--- " + f1.getAbsolutePath() + System.lineSeparator()
						+ "+++ " + f1.getAbsolutePath() + System.lineSeparator()
						+ "@@ -4,0 +4,1 @@" + System.lineSeparator()
						+ "+#pragma acc parallel loop num_gangs(4) pcopy(var1) " + System.lineSeparator();
		assertEquals(expected, p.getPatch());
	}
	
	@Test
	public void addDirectiveTest() throws IOException {
		File f1 = File.createTempFile("testA", ".tmp");
		Map<String, VARIABLE_STATUS> variables1 = new HashMap<String, VARIABLE_STATUS>();
		OpenACCParallelLoopDirective parallelLoop1 = new OpenACCParallelLoopDirective(5, f1, variables1,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		parallelLoop1.setVariable("var1", VARIABLE_STATUS.COPY);
		parallelLoop1.setVariable("var2", VARIABLE_STATUS.NONE);
		parallelLoop1.setVariable("var3", VARIABLE_STATUS.NONE);
		parallelLoop1.setNumGangs(Optional.of(2));
		
		File f2 = File.createTempFile("testB", ".tmp");
		Map<String, VARIABLE_STATUS> variables2 = new HashMap<String, VARIABLE_STATUS>();
		OpenACCParallelLoopDirective parallelLoop2 = new OpenACCParallelLoopDirective(10, f2, variables2,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		parallelLoop2.setVariable("varA", VARIABLE_STATUS.COPYIN);
		parallelLoop2.setVariable("varB", VARIABLE_STATUS.COPYOUT);
		parallelLoop2.setVariable("varC", VARIABLE_STATUS.COPYIN);
		parallelLoop2.setVectorLength(Optional.of(6));
		parallelLoop2.setNumWorkers(Optional.of(6));
		
		List<ADirective> directives = new ArrayList<ADirective>();
		directives.add(parallelLoop1);
		
		Patch p = new Patch(directives, LANGUAGE.CPP);
		
		p.addADirective(parallelLoop2);
		
		String expected = "--- " + f1.getAbsolutePath() + System.lineSeparator()
			+ "+++ " + f1.getAbsolutePath() + System.lineSeparator()
			+ "@@ -4,0 +4,1 @@" + System.lineSeparator()
			+ "+#pragma acc parallel loop num_gangs(4) pcopy(var1) " + System.lineSeparator()
			+ "--- " + f2.getAbsolutePath() + System.lineSeparator()
			+ "+++ " + f2.getAbsolutePath() + System.lineSeparator()
			+ "@@ -9,0 +9,1 @@" + System.lineSeparator()
			+ "+#pragma acc parallel loop num_workers(64) vector_length(64) pcopyin(varC,varA)"
			+ " pcopyout(varB) " + System.lineSeparator();
		
		assertEquals(expected, p.getPatch());
	}
	
	@Test
	public void addDataDirectiveTest() throws IOException {
		File f1 = File.createTempFile("testC", ".tmp");
		Map<String, VARIABLE_STATUS> variables1 = new HashMap<String, VARIABLE_STATUS>();
		variables1.put("var1", VARIABLE_STATUS.COPY);
		variables1.put("var2", VARIABLE_STATUS.COPYOUT);
		variables1.put("var3", VARIABLE_STATUS.NONE);

		
		Map<String, OpenACCUpdateDirective.UPDATE_TYPE> updateVars = new HashMap<String, OpenACCUpdateDirective.UPDATE_TYPE>();
		updateVars.put("var1", OpenACCUpdateDirective.UPDATE_TYPE.DEVICE);
		OpenACCUpdateDirective update1 = new OpenACCUpdateDirective(7, f1, updateVars);
		Set<OpenACCUpdateDirective> updateDirectives = new HashSet<OpenACCUpdateDirective>();
		updateDirectives.add(update1);

		OpenACCDataDirective dataDir1 = new OpenACCDataDirective(5, 10, f1, variables1,
				new HashMap<String, Map.Entry<String, String>>(), updateDirectives);
		
		List<ADirective> directives = new ArrayList<ADirective>();
		directives.add(dataDir1);
		
		Patch p = new Patch(directives, LANGUAGE.CPP);
		
		String expected = "--- " + f1.getAbsolutePath() + System.lineSeparator()
						+ "+++ " + f1.getAbsolutePath() + System.lineSeparator()
						+ "@@ -4,0 +4,2 @@" + System.lineSeparator()
						+ "+#pragma acc data pcopy(var1) pcopyout(var2) " + System.lineSeparator()
						+ "+{" + System.lineSeparator()
						+ "@@ -6,0 +6,1 @@" + System.lineSeparator()
						+ "+#pragma acc update device(var1)" + System.lineSeparator()
						+ "@@ -9,0 +9,1 @@" + System.lineSeparator()
						+ "+}" + System.lineSeparator();
		
		assertEquals(expected, p.getPatch());
	}

	@Test
	public void correctInsertionLocationTest() throws IOException{
		/*
		The purpose of this test is to check that the patches generated are inserting directives in the correct order
		For instance, if a '#pragma acc parallel loop' directive is inserted at line 12, and a '#pragma acc data'
		directive is inserted at line 12, the patch should insert the '#pragma acc data' directive about the
		'#pragma acc parallel loop' directive. This is a basic test to check this happens
		 */
		File f1 = File.createTempFile("testC", ".tmp");
		Map<String, VARIABLE_STATUS> m = new HashMap<String, VARIABLE_STATUS>();
		m.put("input", VARIABLE_STATUS.COPYIN);
		m.put("pointer", VARIABLE_STATUS.COPY);
		m.put("toReturn", VARIABLE_STATUS.COPY);
		Map<String, VARIABLE_STATUS> m2 = new HashMap<String, VARIABLE_STATUS>();
		m2.put("toReturn", VARIABLE_STATUS.COPY);
		m2.put("input", VARIABLE_STATUS.COPYIN);
		Map<String, VARIABLE_STATUS> m3 = new HashMap<String, VARIABLE_STATUS>();
		m3.put("toReturn", VARIABLE_STATUS.COPY);

		List<ADirective> directives = new ArrayList<ADirective>();
		directives.add(new OpenACCDataDirective(16, 31, f1,m2,
				new HashMap<String, Map.Entry<String, String>>(), new HashSet<OpenACCUpdateDirective>()));
		directives.add(new OpenACCParallelLoopDirective(16,f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		directives.add(new OpenACCParallelLoopDirective(23,f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		directives.add(new OpenACCParallelLoopDirective(31,f1, m3,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));

		Patch p = new Patch(directives, LANGUAGE.CPP);

		String expected = "--- " + f1.getAbsolutePath() + System.lineSeparator()
						+ "+++ " + f1.getAbsolutePath() + System.lineSeparator()
						+ "@@ -15,0 +15,3 @@" + System.lineSeparator()
						+ "+#pragma acc data pcopy(toReturn) pcopyin(input) " + System.lineSeparator()
						+ "+{" + System.lineSeparator()
						+ "+#pragma acc parallel loop pcopy(pointer,toReturn) pcopyin(input) " + System.lineSeparator()
						+ "@@ -22,0 +22,1 @@" + System.lineSeparator()
						+ "+#pragma acc parallel loop pcopy(pointer,toReturn) pcopyin(input) " + System.lineSeparator()
						+ "@@ -30,0 +30,2 @@" + System.lineSeparator()
						+ "+}" + System.lineSeparator()
						+ "+#pragma acc parallel loop pcopy(toReturn) " + System.lineSeparator();

		assertEquals(expected, p.getPatch());

	}

	@Test
	public void correctInsertionLocationTest_InnerAndOuterScopes() throws IOException{
		/*
		This test is similar to correctInsertionLocationTest(). The primary purpose of this test is to check that
		outer-scopes are always inserted before the inner scopes.
		 */

		File f1 = File.createTempFile("testC", ".tmp");
		List<ADirective> directives = new ArrayList<ADirective>();
		Map<String, VARIABLE_STATUS> m1 = new HashMap<String, VARIABLE_STATUS>();
		m1.put("A", VARIABLE_STATUS.COPY);
		HashMap<String, Map.Entry<String, String>> m1Ranges = new HashMap<String, Map.Entry<String, String>>();
		m1Ranges.put("A", new HashMap.SimpleEntry<String,String>("0","1000"));
		directives.add(new OpenACCDataDirective(16,22, f1, m1, m1Ranges,
				new HashSet<OpenACCUpdateDirective>()));
		Map<String, VARIABLE_STATUS> m2 = new HashMap<String, VARIABLE_STATUS>();
		m2.put("B", VARIABLE_STATUS.COPY);
		directives.add(new OpenACCDataDirective(16, 23,f1,m2,
				new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>()));
		Map<String, VARIABLE_STATUS> m3 = new HashMap<String, VARIABLE_STATUS>();
		m3.put("C",VARIABLE_STATUS.COPY);
		directives.add(new OpenACCDataDirective(16,30,f1,m3,
				new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>()));
		Map<String, VARIABLE_STATUS> m4 = new HashMap<String, VARIABLE_STATUS>();
		m4.put("D", VARIABLE_STATUS.COPY);
		directives.add(new OpenACCDataDirective(16, 21,f1,m4,
				new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>()));
		Map<String, VARIABLE_STATUS> m5 = new HashMap<String, VARIABLE_STATUS>();
		m5.put("E", VARIABLE_STATUS.COPY);
		directives.add(new OpenACCDataDirective(16,24, f1, m5,
				new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>()));

		Patch p = new Patch(directives, LANGUAGE.CPP);

		String expected = "--- " + f1.getAbsolutePath() + System.lineSeparator()
						+ "+++ " + f1.getAbsolutePath() + System.lineSeparator()
						+ "@@ -15,0 +15,10 @@" + System.lineSeparator()
						+ "+#pragma acc data pcopy(D) " + System.lineSeparator()
						+ "+{" + System.lineSeparator()
						+ "+#pragma acc data pcopy(A[0:1000]) " + System.lineSeparator()
						+ "+{" + System.lineSeparator()
						+ "+#pragma acc data pcopy(B) " + System.lineSeparator()
						+ "+{" + System.lineSeparator()
						+ "+#pragma acc data pcopy(E) " + System.lineSeparator()
						+ "+{" + System.lineSeparator()
						+ "+#pragma acc data pcopy(C) " + System.lineSeparator()
						+ "+{" + System.lineSeparator()
						+ "@@ -20,0 +20,1 @@" + System.lineSeparator()
						+ "+}" + System.lineSeparator()
						+ "@@ -21,0 +21,1 @@" + System.lineSeparator()
						+ "+}" + System.lineSeparator()
						+ "@@ -22,0 +22,1 @@" + System.lineSeparator()
						+ "+}" + System.lineSeparator()
						+ "@@ -23,0 +23,1 @@" + System.lineSeparator()
						+ "+}" + System.lineSeparator()
						+ "@@ -29,0 +29,1 @@" + System.lineSeparator()
						+ "+}" + System.lineSeparator();

		assertEquals(expected, p.getPatch());
	}

	@Test
	public void removeDirectiveTest() throws IOException{
		File f1 = File.createTempFile("testC", ".tmp");
		Map<String, VARIABLE_STATUS> m = new HashMap<String, VARIABLE_STATUS>();
		m.put("input", VARIABLE_STATUS.COPYIN);
		m.put("pointer", VARIABLE_STATUS.COPY);
		m.put("toReturn", VARIABLE_STATUS.COPY);
		Map<String, VARIABLE_STATUS> m2 = new HashMap<String, VARIABLE_STATUS>();
		m2.put("toReturn", VARIABLE_STATUS.COPY);
		m2.put("input", VARIABLE_STATUS.COPYIN);
		Map<String, VARIABLE_STATUS> m3 = new HashMap<String, VARIABLE_STATUS>();
		m3.put("toReturn", VARIABLE_STATUS.COPY);

		List<ADirective> directives = new ArrayList<ADirective>();
		directives.add(new OpenACCParallelLoopDirective(16,f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		directives.add(new OpenACCParallelLoopDirective(23,f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		directives.add(new OpenACCParallelLoopDirective(31,f1, m3,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		directives.add(new OpenACCDataDirective(16, 31, f1,m2,
				new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>()));

		Patch p = new Patch(directives, LANGUAGE.CPP);

		assertTrue(p.removeDirective(new OpenACCDataDirective(16,31, f1, m2,
				new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>())));
		assertEquals(3, p.getDirectives().length);
		for(ADirective dir : p.getDirectives()){
			assert(dir instanceof OpenACCParallelLoopDirective);
		}
	}

	@Test
	public void removeDirectiveTest_2() throws IOException{
		File f1 = File.createTempFile("testC", ".tmp");
		Map<String, VARIABLE_STATUS> m = new HashMap<String, VARIABLE_STATUS>();
		m.put("input", VARIABLE_STATUS.COPYIN);
		m.put("pointer", VARIABLE_STATUS.COPY);
		m.put("toReturn", VARIABLE_STATUS.COPY);
		Map<String, VARIABLE_STATUS> m2 = new HashMap<String, VARIABLE_STATUS>();
		m2.put("toReturn", VARIABLE_STATUS.COPY);
		m2.put("input", VARIABLE_STATUS.COPYIN);
		Map<String, VARIABLE_STATUS> m3 = new HashMap<String, VARIABLE_STATUS>();
		m3.put("toReturn", VARIABLE_STATUS.COPY);

		List<ADirective> directives = new ArrayList<ADirective>();
		directives.add(new OpenACCParallelLoopDirective(16,f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		directives.add(new OpenACCParallelLoopDirective(23,f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		directives.add(new OpenACCParallelLoopDirective(31,f1, m3,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));

		Patch p = new Patch(directives, LANGUAGE.CPP);

		p.addADirective(new OpenACCDataDirective(16, 31, f1,m2,
				new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>()));

		assertTrue(p.removeDirective(new OpenACCDataDirective(16,31, f1, m2,
				new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>())));
		assertEquals(3, p.getDirectives().length);
		for(ADirective dir : p.getDirectives()){
			assertTrue(dir instanceof OpenACCParallelLoopDirective);
		}
	}

	@Test
	public void removeDirectiveTest_3() throws IOException{
		File f1 = File.createTempFile("testC", ".tmp");
		Map<String, VARIABLE_STATUS> m = new HashMap<String, VARIABLE_STATUS>();
		m.put("i", VARIABLE_STATUS.COPY);
		m.put("j", VARIABLE_STATUS.COPY);
		m.put("k", VARIABLE_STATUS.COPY);
		m.put("m", VARIABLE_STATUS.COPY);

		Patch p = new Patch(new ArrayList<ADirective>(), LANGUAGE.CPP);
		p.addADirective(new OpenACCParallelLoopDirective(90, f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(165, f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(189, f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(205, f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(286, f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(300, f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(312, f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(330, f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(344, f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(356, f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(416, f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(434, f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(450, f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(471, f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(490, f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(508, f1, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));

		List<ADirective> directivesToAdd = new ArrayList<ADirective>();
		directivesToAdd.add(new OpenACCDataDirective(58,356,f1,m,
				new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>()));
		directivesToAdd.add(new OpenACCDataDirective(434,504,f1,m,
				new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>()));
		Map<String, OpenACCUpdateDirective.UPDATE_TYPE> update = new HashMap<String, OpenACCUpdateDirective.UPDATE_TYPE>();
		update.put("k", OpenACCUpdateDirective.UPDATE_TYPE.HOST);
		directivesToAdd.add(new OpenACCUpdateDirective(471, f1, update));
		directivesToAdd.add(new OpenACCUpdateDirective(489, f1, update));

		for(ADirective dir: directivesToAdd){
			p.addADirective(dir);
		}

		for(ADirective dir: directivesToAdd){
			assertTrue(p.removeDirective(dir));
		}

		assertEquals(16, p.getDirectives().length);

		for(ADirective dir: p.getDirectives()){
			assertTrue(dir instanceof OpenACCParallelLoopDirective);
		}


	}

	@Test
	public void emptyPatchTest(){
		assertEquals("",(new Patch(new ArrayList<ADirective>(),
			LANGUAGE.CPP)).getPatch());
	}

}
