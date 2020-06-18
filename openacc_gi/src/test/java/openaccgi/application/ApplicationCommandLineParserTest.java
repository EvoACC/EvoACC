package openaccgi.application;

import openaccgi.patch_model.LANGUAGE;
import org.apache.commons.cli.ParseException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessControlException;

import static org.junit.Assert.*;

public class ApplicationCommandLineParserTest {
	
	private static final File LOOP_ANALYSER = new File(
		"../clang_tools/llvm-build/bin/loop-analyser");
	private static final File DATA_INSERTION_FINDER = new File(
		"../clang_tools/llvm-build/bin/data-insertion-finder");
	private static final File DATA_DIRECTIVE_ANALYSER = new File(
		"../clang_tools/llvm-build/bin/data-directive-analyser");
	/*package*/ static final String TOOL_ARGUMENTS = " -d" + DATA_INSERTION_FINDER.getAbsolutePath()
		+ " -D" + DATA_DIRECTIVE_ANALYSER.getAbsolutePath() + " -a" + LOOP_ANALYSER.getAbsolutePath();

	@Test
	public void standardInputTest_1() throws IOException, AccessControlException, ParseException {
		File csvFile = File.createTempFile("test", ".tmp");
		File scriptFile = File.createTempFile("test", ".tmp");
		File smallScriptFile = File.createTempFile("test", ".tmp");
		assertTrue(scriptFile.setExecutable(true));
		assertTrue(smallScriptFile.setExecutable(true));
		int seed = 6;
		int evaluations = 1000;
		String includes1 = "/tmp/";
		String includes2 = "/dev/null";
		LANGUAGE l = LANGUAGE.C;
		String input = "-f "+ csvFile.getAbsolutePath() + " -x" + scriptFile.getAbsolutePath() + " -s " + seed
			+ " -e" + evaluations + " -i" + " -I" + includes1 + " -I"+includes2 + " -l C" + TOOL_ARGUMENTS;
		String[] inputSplit = input.split("\\s+");
		ApplicationCommandLineParser parser = new ApplicationCommandLineParser(inputSplit);
		
		assertEquals(csvFile, parser.getCsvFile());
		assertEquals(scriptFile, parser.getScriptFile());
		assertTrue(parser.getSeed().isPresent());
		assertEquals(new Integer(seed), parser.getSeed().get());
		assertEquals(new Integer(evaluations), parser.getMaxEvaluations());
		assertTrue(parser.isSilent());
		assertEquals(2, parser.getIncludes().size());
		assertTrue(parser.getIncludes().contains(includes1));
		assertTrue(parser.getIncludes().contains(includes2));
		Assert.assertEquals(l, parser.getLanguage());
	}
	
	@Test
	public void standardInputTest_2() throws IOException, AccessControlException, ParseException {
		File csvFile = File.createTempFile("test", ".tmp");
		File scriptFile = File.createTempFile("test", ".tmp");
		scriptFile.setExecutable(true);
		int evaluations = 2;
		LANGUAGE l = LANGUAGE.CPP;
		String input = "-f "+ csvFile.getAbsolutePath() + " -x" + scriptFile.getAbsolutePath()  + " -e "
			+ evaluations +" -lCPP" + TOOL_ARGUMENTS;
		String[] inputSplit = input.split("\\s+");
		ApplicationCommandLineParser parser = new ApplicationCommandLineParser(inputSplit);
		
		assertEquals(csvFile, parser.getCsvFile());
		assertEquals(scriptFile, parser.getScriptFile());
		assertFalse(parser.getSeed().isPresent());
		assertEquals(new Integer(evaluations), parser.getMaxEvaluations());
		assertFalse(parser.isSilent());
		assertTrue(parser.getIncludes().isEmpty());
		Assert.assertEquals(l, parser.getLanguage());
	}
	
	@Test(expected=ParseException.class)
	public void parseFailureTest() throws AccessControlException, FileNotFoundException, ParseException{
		String input = "-fg";
		String[] inputSplit = input.split("\\s+");
		@SuppressWarnings("unused") //Expect exception to be thrown
		ApplicationCommandLineParser parser = new ApplicationCommandLineParser(inputSplit);
	}
	
	
	@Test(expected=FileNotFoundException.class)
	public void csvFileDoesNotExistTest() throws IOException, AccessControlException, ParseException{
		File csvFile = File.createTempFile("test", ".tmp");
		assertTrue(csvFile.delete());
		File scriptFile = File.createTempFile("test", ".tmp");
		scriptFile.setExecutable(true);
		String input = "-f "+ csvFile.getAbsolutePath() + " -x" + scriptFile.getAbsolutePath() + " -e2"
			+ " -lC" + TOOL_ARGUMENTS;
		String[] inputSplit = input.split("\\s+");
		@SuppressWarnings("unused") //Expect exception to be thrown
		ApplicationCommandLineParser parser = new ApplicationCommandLineParser(inputSplit);
	}
	
	@Test(expected=FileNotFoundException.class)
	public void csvFileIsADirectoryTest() throws IOException, AccessControlException, ParseException{
		File csvFile = File.createTempFile("test", ".tmp");
		assertTrue(csvFile.delete());
		assertTrue(csvFile.mkdir());
		File scriptFile = File.createTempFile("test", ".tmp");
		scriptFile.setExecutable(true);
		String input = "-f "+ csvFile.getAbsolutePath() + " -x" + scriptFile.getAbsolutePath() + " -e 40"
			+ " -lC" + TOOL_ARGUMENTS;
		String[] inputSplit = input.split("\\s+");
		@SuppressWarnings("unused") //Expect exception to be thrown
		ApplicationCommandLineParser parser = new ApplicationCommandLineParser(inputSplit);
	}
	
	@Test(expected=AccessControlException.class)
	public void csvFileIsUnreadableTest() throws IOException, AccessControlException, ParseException{
		File csvFile = File.createTempFile("test", ".tmp");
		assertTrue(csvFile.setReadable(false));
		File scriptFile = File.createTempFile("test", ".tmp");
		scriptFile.setExecutable(true);
		String input = "-f "+ csvFile.getAbsolutePath() + " -x" + scriptFile.getAbsolutePath() + " -e 100"
			+ " -lC" + TOOL_ARGUMENTS;
		String[] inputSplit = input.split("\\s+");
		@SuppressWarnings("unused") //Expect exception to be thrown
		ApplicationCommandLineParser parser = new ApplicationCommandLineParser(inputSplit);
		
	}
	
	@Test(expected=FileNotFoundException.class)
	public void scriptFileDoesNotExistTest() throws IOException, AccessControlException, ParseException{
		File csvFile = File.createTempFile("test", ".tmp");
		File scriptFile = File.createTempFile("test", ".tmp");
		scriptFile.setExecutable(true);
		assertTrue(scriptFile.delete());
		String input = "-f "+ csvFile.getAbsolutePath() + " -x" + scriptFile.getAbsolutePath() + " -e55"
			+ " -lC" + TOOL_ARGUMENTS;
		String[] inputSplit = input.split("\\s+");
		@SuppressWarnings("unused") //Expect exception to be thrown
		ApplicationCommandLineParser parser = new ApplicationCommandLineParser(inputSplit);
		
	}
	
	@Test(expected=FileNotFoundException.class)
	public void scriptFileIsADirectoryTest() throws IOException, AccessControlException, ParseException{
		File csvFile = File.createTempFile("test", ".tmp");
		File scriptFile = File.createTempFile("test", ".tmp");
		scriptFile.setExecutable(true);
		assertTrue(scriptFile.delete());
		assertTrue(scriptFile.mkdir());
		String input = "-f "+ csvFile.getAbsolutePath() + " -x" + scriptFile.getAbsolutePath() + " -e 4"
			+ " -lC" + TOOL_ARGUMENTS;
		String[] inputSplit = input.split("\\s+");
		@SuppressWarnings("unused") //Expect exception to be thrown
		ApplicationCommandLineParser parser = new ApplicationCommandLineParser(inputSplit);
	}
	
	@Test(expected=AccessControlException.class)
	public void scriptIsNotExecutableTest() throws IOException, AccessControlException, ParseException{
		File csvFile = File.createTempFile("test", ".tmp");
		File scriptFile = File.createTempFile("test", ".tmp");
		String input = "-f "+ csvFile.getAbsolutePath() + " -x" + scriptFile.getAbsolutePath() + " -e 55"
			+ " -lC" + TOOL_ARGUMENTS;
		String[] inputSplit = input.split("\\s+");
		@SuppressWarnings("unused") //Expect exception to be thrown
		ApplicationCommandLineParser parser = new ApplicationCommandLineParser(inputSplit);
	}
	
	@Test(expected=ParseException.class)
	public void seedIsNotAnIntegerTest() throws IOException, AccessControlException, ParseException{
		File csvFile = File.createTempFile("test", ".tmp");
		File scriptFile = File.createTempFile("test", ".tmp");
		scriptFile.setExecutable(true);
		String input = "-f "+ csvFile.getAbsolutePath() + " -x" + scriptFile.getAbsolutePath() + " -s 1a"
			+ " -lC" + TOOL_ARGUMENTS;
		String[] inputSplit = input.split("\\s+");
		@SuppressWarnings("unused") //Expect exception to be thrown
		ApplicationCommandLineParser parser = new ApplicationCommandLineParser(inputSplit);
	}
	
	@Test(expected=ParseException.class)
	public void numEvaluationsIsNotAnIntegerTest() throws IOException, AccessControlException, ParseException{
		File csvFile = File.createTempFile("test", ".tmp");
		File scriptFile = File.createTempFile("test", ".tmp");
		scriptFile.setExecutable(true);
		String input = "-f "+ csvFile.getAbsolutePath() + " -x" + scriptFile.getAbsolutePath() + " -e 1a"
			+ "- lC" + TOOL_ARGUMENTS;
		String[] inputSplit = input.split("\\s+");
		@SuppressWarnings("unused") //Expect exception to be thrown
		ApplicationCommandLineParser parser = new ApplicationCommandLineParser(inputSplit);
	}
	
	@Test(expected=ParseException.class)
	public void numEvaluationsIsLessThanZeroTest() throws IOException, AccessControlException, ParseException{
		File csvFile = File.createTempFile("test", ".tmp");
		File scriptFile = File.createTempFile("test", ".tmp");
		scriptFile.setExecutable(true);
		String input = "-f "+ csvFile.getAbsolutePath() + " -x" + scriptFile.getAbsolutePath() + " -e -1"
			+ "- lC" + TOOL_ARGUMENTS;
		String[] inputSplit = input.split("\\s+");
		@SuppressWarnings("unused") //Expect exception to be thrown
		ApplicationCommandLineParser parser = new ApplicationCommandLineParser(inputSplit);
	}
	
	@Test(expected=ParseException.class)
	public void invalidLanguageTest() throws IOException, AccessControlException, ParseException{
		File csvFile = File.createTempFile("test", ".tmp");
		File scriptFile = File.createTempFile("test", ".tmp");
		scriptFile.setExecutable(true);
		String input = "-f "+ csvFile.getAbsolutePath() + " -x" + scriptFile.getAbsolutePath() + " -e 100"
			+ " -ljava" + TOOL_ARGUMENTS;
		String[] inputSplit = input.split("\\s+");
		@SuppressWarnings("unused") //Expect exception to be thrown
		ApplicationCommandLineParser parser = new ApplicationCommandLineParser(inputSplit);
	}

}
