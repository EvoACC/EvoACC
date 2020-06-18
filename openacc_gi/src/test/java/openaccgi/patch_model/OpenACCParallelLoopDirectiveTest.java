package openaccgi.patch_model;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class OpenACCParallelLoopDirectiveTest {

	private File f;
	private int lineNum = 10;
	private OpenACCParallelLoopDirective directive;
	private Map<String, VARIABLE_STATUS> vars;
	private Map<String, Map.Entry<String,String>> varRanges;
	private Optional<Integer> gangs;
	private Optional<Integer> workers;
	private Optional<Integer> vector;

	@Before
	public void setup() throws FileNotFoundException, AccessControlException, IOException {
		this.f = File.createTempFile("openaccdirectiveTest", ".tmp");
		this.vars = new HashMap<String, VARIABLE_STATUS>();
		this.varRanges = new HashMap<String, Map.Entry<String, String>>();
		this.vars.put("var1", VARIABLE_STATUS.COPY);
		this.vars.put("var2", VARIABLE_STATUS.NONE);
		this.vars.put("var3", VARIABLE_STATUS.PRESENT);
		this.gangs = Optional.of(5);
		this.workers = Optional.of(3);
		this.vector = Optional.of(2);
		
		this.directive = new OpenACCParallelLoopDirective(this.lineNum, this.f, this.vars, this.varRanges,
			this.gangs, this.workers, this.vector);
	}
	
	@Test
	public void setAndGetActiveTest() {
		directive.setActive(false);
		assertFalse(directive.isActive());
		directive.setActive(true);
		assertTrue(directive.isActive());
	}
	
	@Test
	public void getNumVariablesTest(){
		assertEquals(vars.size(), directive.getVariables().size());
	}
	
	@Test
	public void getVariablesTest() {
		for(Map.Entry<String, VARIABLE_STATUS> e : vars.entrySet()) {
			assertTrue(directive.getVariables().containsKey(e.getKey()));
			assertEquals(e.getValue(), directive.getVariables().get(e.getKey()));
		}
	}
	
	@Test
	public void setVariableStatusTest(){
		directive.setVariable("var1", VARIABLE_STATUS.COPYIN);
		assertEquals(VARIABLE_STATUS.COPYIN, directive.getVariables().get("var1"));
		
	}
	
	@Test
	public void getFileTest(){
		assertEquals(f, directive.getFile());
	}
	
	@Test
	public void getLineNumberTest(){
		assertEquals(lineNum, directive.getStartLineNumber());
		assertEquals(lineNum, directive.getEndLineNumber());
	}
	
	@Test
	public void setNumGangsTest(){
		Optional<Integer> toSet = Optional.of(4);
		directive.setNumGangs(toSet);
		assertEquals(toSet, directive.getNumGangs());
	}
	
	@Test
	public void setNumWorkersTest(){
		Optional<Integer> toSet = Optional.of(6);
		directive.setNumWorkers(toSet);
		assertEquals(toSet, directive.getNumWorkers());
	}
	
	@Test
	public void setVectorLengthTest(){
		Optional<Integer> toSet = Optional.of(5);
		directive.setVectorLength(toSet);
		assertEquals(toSet, directive.getVectorLength());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void  setNumGangsTest_inputLessLessThanZero(){
		Optional<Integer> toSet = Optional.of(-1);
		directive.setNumGangs(toSet);
	}

	
	@Test(expected=IllegalArgumentException.class)
	public void  setNumWorkersTest_inputLessLessThanZero(){
		Optional<Integer> toSet = Optional.of(-5);
		directive.setNumWorkers(toSet);
	}

	@Test(expected=IllegalArgumentException.class)
	public void  setVectorLengthTest_inputLessLessThanZero(){
		Optional<Integer> toSet = Optional.of(-100);
		directive.setVectorLength(toSet);
	}
	
	@Test
	public void getStringToInsertTest(){
		Optional<Integer> vectorLength = Optional.of(2);
		Optional<Integer> numGangs = Optional.of(3);
		directive.setVectorLength(vectorLength);
		directive.setNumGangs(numGangs);
		
		directive.setVariable("var1", VARIABLE_STATUS.COPY);
		directive.setVariable("var2", VARIABLE_STATUS.CREATE);
		
		String expected = "#pragma acc parallel loop num_gangs(8) num_workers(8) vector_length(4) pcopy(var1)" +
			" create(var2) present(var3)";
		Map<Integer, String> returned = directive.getStringsToInsert(LANGUAGE.CPP);
		assertEquals(1, returned.size());
		assertTrue(returned.containsKey(this.lineNum));
		assertEquals(expected.trim(), returned.get(this.lineNum).trim());
	}
}
