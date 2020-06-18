package openaccgi.patch_model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class OpenACCUpdateDirectiveTest {
	
	private OpenACCUpdateDirective directive;
	private int lineNum;
	private File f;
	private Map<String, OpenACCUpdateDirective.UPDATE_TYPE> vars;
	
	@Before
	public void setup() throws FileNotFoundException, AccessControlException, IOException {
		f = File.createTempFile("openaccdirectiveTest", ".tmp");
		lineNum = 10;
		vars = new HashMap<String, OpenACCUpdateDirective.UPDATE_TYPE>();
		vars.put("var1", OpenACCUpdateDirective.UPDATE_TYPE.DEVICE);
		vars.put("var2", OpenACCUpdateDirective.UPDATE_TYPE.HOST);
		directive = new OpenACCUpdateDirective(lineNum, f, vars);
	}

	@Test
	public void getVariablesTest() {
		for(Map.Entry<String, OpenACCUpdateDirective.UPDATE_TYPE> e : vars.entrySet()) {
			assertTrue(this.directive.getVariables().containsKey(e.getKey()));
			assertEquals(e.getValue(), this.directive.getVariables().get(e.getKey()));
		}
	}
	
	@Test
	public void addVariableTest_add() {
		this.directive.addVariable("var3", OpenACCUpdateDirective.UPDATE_TYPE.HOST);
		assertTrue(this.directive.getVariables().containsKey("var3"));
		Assert.assertEquals(OpenACCUpdateDirective.UPDATE_TYPE.HOST, this.directive.getVariables().get("var3"));
	}
	
	@Test
	public void addVariableTest_replace() {
		this.directive.addVariable("var1", OpenACCUpdateDirective.UPDATE_TYPE.DEVICE);
		assertTrue(this.directive.getVariables().containsKey("var1"));
		Assert.assertEquals(OpenACCUpdateDirective.UPDATE_TYPE.DEVICE, this.directive.getVariables().get("var1"));
	}
	
	@Test
	public void removeVariableTest() {
		this.directive.removeVariable("var1");
		assertFalse(this.directive.getVariables().containsKey("var1"));
	}
	
	@Test
	public void getStringsToInsertTest() {
		this.directive.addVariable("var3", OpenACCUpdateDirective.UPDATE_TYPE.HOST);
		String expected = "#pragma acc update host(var3, var2) device(var1)";
		Map<Integer, String> output = this.directive.getStringsToInsert(LANGUAGE.CPP);
		assertEquals(1, output.size());
		assertTrue(output.containsKey(this.lineNum));
		assertEquals(expected, output.get(this.lineNum));
	}

}
