package openaccgi.program_data;

import java.io.*;
import java.security.AccessControlException;
import java.util.*;


public class CSVDataContainer implements IProgramDataContainer {

	// One giant map to store the data.
	private Map<File, Map<Integer,Map<String, Optional<Map.Entry<String, String>>>>> data = new HashMap<File, Map<Integer,Map<String, Optional<Map.Entry<String, String>>>>>();
	private final boolean includeVarRanges;
	
	public CSVDataContainer(File csvFile, boolean incVarRanges) throws AccessControlException, FileNotFoundException, IOException{
		this.includeVarRanges = incVarRanges;
		if(!csvFile.exists()){
			throw new FileNotFoundException("File '"+csvFile.getAbsolutePath()+"' does not exist");
		}
		
		if(csvFile.isDirectory()){
			throw new FileNotFoundException("'"+csvFile.getAbsolutePath()+"' is a directory. File expected");
		}
		
		if(!csvFile.canRead()){
			throw new AccessControlException("File '"+csvFile.getAbsolutePath()+" is unreadable");
		}
		
		try(BufferedReader br = new BufferedReader(new FileReader(csvFile))){
			String line;
			while ((line = br.readLine()) != null){
				processEntry(line);
			}
		}
	}
	
	@Override
	public Set<File> getFiles() {
		return this.data.keySet();
	}
	
	private void checkFile(File f) throws NoSuchElementException {
		if(!data.containsKey(f)){
			throw new NoSuchElementException("No file '"+f.getAbsolutePath()+"' contained in CSVDataContainer object");
		}
	}

	private void checkLineNo(File f, Integer i) throws NoSuchElementException {
		checkFile(f);
		if(!this.data.get(f).containsKey(i)){
			throw new NoSuchElementException("No line number '" + i + "' in file '"+f.getAbsolutePath()
				+"' contained in CSVDataContainer object");
		}
	}

	private void checkVariable(File f, Integer i, String variable) throws NoSuchElementException {
		checkLineNo(f, i);
		if(!this.data.get(f).get(i).containsKey(variable)){
			throw new NoSuchElementException("No variable '" +variable+"' found at line '" + i + "' in file '"
				+f.getAbsolutePath()+"' contained in CSVDataContainer object");
		}
	}

	@Override
	public SortedSet<Integer> getLines(File f) throws NoSuchElementException{
		checkFile(f);
		return new TreeSet<Integer>(this.data.get(f).keySet());
	}

	@Override
	public Set<String> getVariables(File f, Integer i) throws NoSuchElementException{
		checkLineNo(f, i);
		return this.data.get(f).get(i).keySet();
	}

	@Override
	public Optional<Map.Entry<String, String>> getVariableRange(
		File f, Integer i, String variable) throws NoSuchElementException{
		checkVariable(f, i, variable);
		return this.data.get(f).get(i).get(variable);
	}
	
	private void processEntry(String entry) throws IOException{
		final String[] entryFields = entry.split(",", -1);
		if(entryFields.length != 5){
			throw new IOException("Expected CSV record with 5 entries. Recieved: '"+entry+"'");
		}
		
		File f = new File(entryFields[0]);
		int lineNo;
		
		try{
			lineNo = Integer.parseInt(entryFields[1]);
		} catch(NumberFormatException e){
			throw new IOException("Following exception caught while processing CSV entry '"+entry+"'"
				+ System.lineSeparator() + e.getLocalizedMessage());
		}

		String variable = entryFields[2].isEmpty() ? null : entryFields[2];
		String startRange = entryFields[3].isEmpty() ? null : entryFields[3];
		String endRange = entryFields[4].isEmpty() ? null : entryFields[4];

		if((startRange == null || endRange == null) && startRange != endRange){
			throw new IOException("Cannot have start of range without end of range (or vice-versa). Concerning the" +
				" following entry: '"+entry+"'");
		}

		if(!this.data.containsKey(f)){
			this.data.put(f, new HashMap<Integer,Map<String, Optional<Map.Entry<String, String>>>>());
		}

		if(!this.data.get(f).containsKey(lineNo)){
			this.data.get(f).put(lineNo, new HashMap<String, Optional<Map.Entry<String, String>>>());
		}

		if(variable != null && !this.data.get(f).get(lineNo).containsKey(variable)){
			if (startRange == null || !this.includeVarRanges) {
				this.data.get(f).get(lineNo).put(variable, Optional.empty());
			} else {
				this.data.get(f).get(lineNo).put(variable, Optional.of(
					new HashMap.SimpleEntry<String, String>(startRange, endRange)));
			}
		}
		//TODO should be check to see if already been added and warning given to the user
	}
}
