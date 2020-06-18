package openaccgi.patch_model;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.*;

public class OpenACCDataDirectiveTest {
	
	private OpenACCDataDirective directive;
	private File f;
	private int startLineNo;
	private int endLineNo;
	private Map<String, VARIABLE_STATUS> vars;
	private Map<String, Map.Entry<String, String>> varRanges;
	
	@Before
	public void setup() throws FileNotFoundException, AccessControlException, IOException {
		f = File.createTempFile("openaccdirectiveTest", ".tmp");
		startLineNo=45;
		endLineNo=50;
		vars = new HashMap<String, VARIABLE_STATUS>();
		varRanges = new HashMap<String, Map.Entry<String, String>>();
		vars.put("var1", VARIABLE_STATUS.NONE);
		vars.put("var2", VARIABLE_STATUS.COPY);
		vars.put("var3", VARIABLE_STATUS.NONE);
		directive = new OpenACCDataDirective(startLineNo, endLineNo, f, vars, varRanges,
				new HashSet<OpenACCUpdateDirective>());
	}

	@Test
	public void getVariablesTest() {
		for(Map.Entry<String, VARIABLE_STATUS> e : vars.entrySet()) {
			assertTrue(directive.getVariables().containsKey(e.getKey()));
			assertEquals(e.getValue(), directive.getVariables().get(e.getKey()));
		}
	}
	
	@Test
	public void setVariablesTest_add() {
		directive.setVariable("bla", VARIABLE_STATUS.PRESENT);
		assertTrue(directive.getVariables().containsKey("bla"));
		assertEquals(VARIABLE_STATUS.PRESENT, directive.getVariables().get("bla"));
	}
	
	@Test
	public void setVariablesTest_modify() {
		directive.setVariable("var1", VARIABLE_STATUS.COPY);
		assertTrue(directive.getVariables().containsKey("var1"));
		assertEquals(VARIABLE_STATUS.COPY, directive.getVariables().get("var1"));
	}
	
	@Test
	public void setAndGetActivationTest() {
		directive.setActive(false);
		assertFalse(directive.isActive());
		directive.setActive(true);
		assertTrue(directive.isActive());
	}
	
	@Test
	public void getVariablesToInsertTest() {
		this.directive.setVariable("bla", VARIABLE_STATUS.COPYOUT);
		String expected1 = "#pragma acc data pcopy(var2) pcopyout(bla) " + System.lineSeparator() + "{";
		String expected2 = "}";
		
		Map<Integer, String> output = this.directive.getStringsToInsert(LANGUAGE.CPP);
		
		assertTrue(output.containsKey(this.startLineNo));
		assertEquals(expected1, output.get(this.startLineNo));
		assertTrue(output.containsKey(this.endLineNo));
		assertEquals(expected2, output.get(this.endLineNo));
	}
}
