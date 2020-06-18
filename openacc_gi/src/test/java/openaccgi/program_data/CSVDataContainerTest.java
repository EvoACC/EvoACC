package openaccgi.program_data;

import org.junit.Before;
import org.junit.Test;
import openaccgi.utils.InputFileGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.AccessControlException;
import java.util.*;

import static org.junit.Assert.*;

public class CSVDataContainerTest {
	File csvFile1Object;
	File csvFile2Object;
	File csvFile3Object;
	CSVDataContainer csvFile;
	
	@Before
	public void setupCSVFiles() throws FileNotFoundException, IOException{
		csvFile1Object = InputFileGenerator.getFileReference1();
		csvFile2Object = InputFileGenerator.getFileReference2();
		csvFile3Object = InputFileGenerator.getFileReference3();
		csvFile = new CSVDataContainer(InputFileGenerator.getTestCSVFile(), true);
	}

	@Test
	public void CSVFile2_getFiles(){
		Set<File> expected = new HashSet<File>();
		expected.add(csvFile1Object);
		expected.add(csvFile2Object);
		expected.add(csvFile3Object);
		
		Set<File> actual = this.csvFile.getFiles();
		
		assertTrue(actual.containsAll(expected));
		assertTrue(expected.containsAll(actual));
	}
	
	
	@Test
	public void CSVFile1_getLines(){
		SortedSet<Integer> expected = new TreeSet<Integer>();
		expected.add(11);
		expected.add(64);
		expected.add(70);
		expected.add(80);
		expected.add(112);
		expected.add(118);
		expected.add(128);
		expected.add(292);
		expected.add(300);

		SortedSet<Integer> actual = this.csvFile.getLines(csvFile1Object);
		assertTrue(actual.containsAll(expected));
		assertTrue(expected.containsAll(actual));
		
		Iterator<Integer> actualIterator = actual.iterator();
		Iterator<Integer> expectedIterator = expected.iterator();
		
		while(actualIterator.hasNext()){
			assertEquals(expectedIterator.next(),actualIterator.next());
		}
	}

	@Test
	public void CSVFile1_getVariables(){
		Set<String> variables64 = new HashSet<String>();
		variables64.add("x");
		variables64.add("y");
		variables64.add("z");

		Set<String> variables70 = new HashSet<String>();
		variables70.add("x");

		Set<String> variables80 = new HashSet<String>();
		variables80.add("y");
		variables80.add("z");

		assertEquals(variables64, this.csvFile.getVariables(this.csvFile1Object,64));
		assertEquals(variables70, this.csvFile.getVariables(this.csvFile1Object, 70));
		assertEquals(variables80, this.csvFile.getVariables(this.csvFile1Object, 80));

		assertTrue(this.csvFile.getVariables(this.csvFile1Object,11).isEmpty());
		assertTrue(this.csvFile.getVariables(this.csvFile1Object,112).isEmpty());
		assertTrue(this.csvFile.getVariables(this.csvFile1Object,118).isEmpty());
		assertTrue(this.csvFile.getVariables(this.csvFile1Object,128).isEmpty());
		assertTrue(this.csvFile.getVariables(this.csvFile1Object,292).isEmpty());
		assertTrue(this.csvFile.getVariables(this.csvFile1Object,300).isEmpty());
	}

	@Test
	public void CSVFile1_getVariableRange(){

		assertEquals("0", this.csvFile.getVariableRange(
			this.csvFile1Object,64,"x").get().getKey());
		assertEquals("1024", this.csvFile.getVariableRange(
			this.csvFile1Object,64,"x").get().getValue());
		assertEquals("0", this.csvFile.getVariableRange(
			this.csvFile1Object,64,"y").get().getKey());
		assertEquals("1024", this.csvFile.getVariableRange(
			this.csvFile1Object,64,"y").get().getValue());
		assertEquals("0", this.csvFile.getVariableRange(
			this.csvFile1Object,64,"z").get().getKey());
		assertEquals("1024", this.csvFile.getVariableRange(
			this.csvFile1Object,64,"z").get().getValue());

		assertEquals("100", this.csvFile.getVariableRange(
			this.csvFile1Object, 70, "x").get().getKey());
		assertEquals("500", this.csvFile.getVariableRange(
			this.csvFile1Object, 70, "x").get().getValue());

		assertEquals("NP", this.csvFile.getVariableRange(
			this.csvFile1Object, 80, "y").get().getKey());
		assertEquals("ND", this.csvFile.getVariableRange(
			this.csvFile1Object, 80, "y").get().getValue());

		assertFalse(this.csvFile.getVariableRange(this.csvFile1Object,80, "z").isPresent());
	}

	@Test(expected=NoSuchElementException.class)
	public void noSuchFile(){
		this.csvFile.getLines(new File("/tmp/blablabla111"));
	}

	@Test(expected=NoSuchElementException.class)
	public void noSuchLine(){
		this.csvFile.getVariables(this.csvFile1Object,10);
	}

	@Test(expected=NoSuchElementException.class)
	public void noSuchVariable(){
		this.csvFile.getVariableRange(this.csvFile1Object,64,"a");
	}
	
	@Test(expected=FileNotFoundException.class)
	public void inputFileDoesNotExist() throws FileNotFoundException, IOException{
		File f = new File("NonExistantFile");
		assertFalse(f.exists());
		
		@SuppressWarnings("unused") //Is actually used in this test, it should throw an exception
		CSVDataContainer c = new CSVDataContainer(f, true);
	}
	
	@Test(expected=AccessControlException.class)
	public void inputFileIsUnreadable() throws IOException, AccessControlException{
		File f = File.createTempFile("csv", ".tmp");
		f.setReadable(false);
		
		@SuppressWarnings("unused") //Is actually used in this test, it should throw an exception
		CSVDataContainer c = new CSVDataContainer(f, true);
	}
	
	@Test(expected=IOException.class)
	public void inputCSVIsNotTwoEntries() throws IOException{
		File f = File.createTempFile("csv", ".tmp");
		
		PrintWriter p = new PrintWriter(f);
		p.println("/tmp/temp,10,0");
		p.close();
		
		@SuppressWarnings("unused") //Is actually used in this test, it should throw an exception
		CSVDataContainer c = new CSVDataContainer(f, true);
	}
}
