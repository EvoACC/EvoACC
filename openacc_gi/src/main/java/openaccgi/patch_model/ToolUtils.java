package openaccgi.patch_model;

import openaccgi.patch_model.OpenACCUpdateDirective.UPDATE_TYPE;

import java.io.*;
import java.util.*;

public class ToolUtils implements IToolUtils {
	private final File loopAnalyser;
	private final File dataInsertionAnalyser ;
	private final File dataDirectiveAnalyser;
	private final Set<String> includes;
	private final LANGUAGE language;
	private final boolean includeVariableRanges;
	
	public ToolUtils(Set<String> incs, LANGUAGE l, File loopAnalyserFile, File dataInsertionFinderFile,
	                 File dataDirectiveAnalyserFile, boolean incVariableRanges){
		this.includes = incs;
		this.language = l;
		this.loopAnalyser = loopAnalyserFile;
		this.dataInsertionAnalyser = dataInsertionFinderFile;
		this.dataDirectiveAnalyser = dataDirectiveAnalyserFile;
		this.includeVariableRanges = incVariableRanges;
	}
	
	private String getLanguageString(){
		switch(this.language){
		case C:
			return "C";
		case CPP:
			return "CPP";
		default:
			assert(false); //Should not be necessary;
		}
		
		assert(false);
		return "";
	}
	
	@Override
	public Map<String, VARIABLE_STATUS> getDataInfoForScope(File f, int lineNo) throws IOException{
		Map<String, VARIABLE_STATUS> toReturn = new HashMap<String, VARIABLE_STATUS>();
		
		assert(this.loopAnalyser.exists() && this.loopAnalyser.canExecute());
		
		Process process = null;
		try {
			// loop-analyser <file> <lineNo> <language> [includes...]
			String command = this.loopAnalyser.getAbsolutePath()+" "
				+f.getAbsolutePath() + "  " + lineNo + " " + getLanguageString();
			for(String s:  includes){
				command += " " + s;
			}

			process = Runtime.getRuntime().exec(command);
		
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			String line;

			if((line = errorReader.readLine()) != null){
				String tempLine = "";
				while((tempLine = errorReader.readLine()) != null){
					line+=System.lineSeparator() + tempLine;
				}
				throw new IOException("loop-analyser output the following error: " + System.lineSeparator() + line
					+ System.lineSeparator() + "For command: " + System.lineSeparator() + command);
			}
			
			while((line=reader.readLine()) != null){
				//cout << lineNo << ',' << endScopeLine  << ',' << current.variable.c_str() << ',' << flowValue << endl;
				String[] temp = line.split(",");
				if(temp.length != 4) {
					throw new IOException("loop-analyser output a CSV where the number of entries was not 4: "
						+ System.lineSeparator() + line);
				}
				
				String variable = temp[2];
				String variableFlow = temp[3];
				if(Integer.parseInt(temp[0]) != lineNo) {
					throw new IOException("Specified line " + lineNo +" but loop-analyser returned " + temp[0]);
				}
				
				VARIABLE_STATUS varStatus;
				switch(variableFlow) {
					case "COPY_IN":
						varStatus = VARIABLE_STATUS.COPYIN;
						break;
					case "COPY_OUT":
						varStatus = VARIABLE_STATUS.COPYOUT;
						break;
					case "COPY":
						varStatus = VARIABLE_STATUS.COPY;
						break;
					case "PRESENT":
						varStatus = VARIABLE_STATUS.PRESENT;
						break;
					case "CREATE":
						varStatus = VARIABLE_STATUS.CREATE;
						break;
					default:
						throw new IOException("Unrecognised flow status from loop-analyser: " + variableFlow);
				}
				
				toReturn.put(variable, varStatus);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return toReturn;
	}

	@Override
	public SortedSet<DataDirectiveInsertionPoint> getDataDirectiveInsertionPoints(
		File f, ADirective[] presentDirectives) throws IOException {
		SortedSet<DataDirectiveInsertionPoint> toReturn = new TreeSet<DataDirectiveInsertionPoint>();
		
		assert(this.dataInsertionAnalyser.exists() && this.dataInsertionAnalyser.canExecute());
		
		try{
			File csvPresentDirectives = File.createTempFile("presentDirectivesCSV", ".tmp");
			
			//startLine, endLine, status, variable
			PrintWriter out = new PrintWriter(csvPresentDirectives);
			 for(ADirective dir : presentDirectives){
			 	 if(dir.isActive()) {
					 if (dir instanceof OpenACCDataDirective) {
						 int startLine = ((OpenACCDataDirective) dir).getStartLineNumber();
						 int endLine = ((OpenACCDataDirective) dir).getEndLineNumber();
						 for (Map.Entry<String, VARIABLE_STATUS> e : ((OpenACCDataDirective) dir)
							 .getVariables().entrySet()) {
							 String flow = null;
							 switch (e.getValue()) {
								 case COPY:
									 flow = "COPY";
									 break;
								 case COPYIN:
									 flow = "COPY_IN";
									 break;
								 case COPYOUT:
									 flow = "COPY_OUT";
									 break;
								 case PRESENT:
									 flow = "PRESENT";
									 break;
								 case CREATE:
									 flow = "CREATE";
									 break;
								 default:
									 assert (false);
									 break;
							 }
							 assert (flow != null);

							 String rangeStart = "";
							 String rangeEnd = "";
							 if(((OpenACCDataDirective) dir).getVariableRanges().containsKey(e.getKey())){
							 	rangeStart = ((OpenACCDataDirective) dir).getVariableRanges().get(e.getKey()).getKey();
							 	rangeEnd = ((OpenACCDataDirective) dir).getVariableRanges().get(e.getKey()).getValue();
							 }

							 out.println(startLine + "," + endLine + "," + flow + "," + e.getKey()
								 + "," + rangeStart + "," + rangeEnd);
						 }
					 }
				 }
			 }
			out.close();

			Process process = null;
			
			String command = this.dataInsertionAnalyser.getAbsolutePath()+" "+f.getAbsolutePath() + "  "
				+ csvPresentDirectives.getAbsolutePath() + " " + getLanguageString();
			for(String s : this.includes) {
				command += " " + s;
			}

			//data-insertion-finder <source_file> <present_directives_csv> <language> [includes...]
			process = Runtime.getRuntime().exec(command);

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			String line;

			if((line = errorReader.readLine()) != null){
				String tempLine = "";
				while((tempLine = errorReader.readLine()) != null){
					line+=System.lineSeparator() + tempLine;
				}
				throw new IOException("data-insertion-finder output the following error: "
					+ System.lineSeparator() + line);
			}


			while((line=reader.readLine()) != null){
				//StartLine, endLine
				String[] temp = line.split(",");
				if(temp.length != 2) {
					throw new IOException("data-insertion-finder output a CSV where the number of entries was not 2: "
						+ line);
				}
				
				int startLine = Integer.parseInt(temp[0]);
				int endLine = Integer.parseInt(temp[1]);
				
				toReturn.add(new DataDirectiveInsertionPoint(f,startLine, endLine));
			}
			reader.close();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
		
		return toReturn;
	}

	@Override
	public Optional<OpenACCDataDirective> getDataDirective(File f, int startLineNo,
	                                                       int endLineNo,
	                                                       ADirective[] presentDirectives) throws IOException {
		Optional<OpenACCDataDirective> toReturn = Optional.empty();

		assert(this.dataDirectiveAnalyser.exists() && this.dataDirectiveAnalyser.canExecute());
		
		try{
			File csvPresentDirectives = File.createTempFile("presentDirectivesCSV", ".tmp");
			
			//startLine, endLine, status, variable, start_range, end_range
			 PrintWriter out = new PrintWriter(csvPresentDirectives);
			 for(ADirective dir : presentDirectives){
			 	if(dir.isActive()) {
					if (dir instanceof OpenACCDataDirective) {
						int startLine = ((OpenACCDataDirective) dir).getStartLineNumber();
						int endLine = ((OpenACCDataDirective) dir).getEndLineNumber();
						for (Map.Entry<String, VARIABLE_STATUS> e : ((OpenACCDataDirective) dir)
							.getVariables().entrySet()) {
							String flow = null;
							switch (e.getValue()) {
								case COPY:
									flow = "COPY";
									break;
								case COPYIN:
									flow = "COPY_IN";
									break;
								case COPYOUT:
									flow = "COPY_OUT";
									break;
								case PRESENT:
									flow = "PRESENT";
									break;
								case CREATE:
									flow = "CREATE";
									break;
								default:
									assert (false);
									break;
							}
							assert (flow != null);
							String beginRange = "";
							String endRange = "";
							if(((OpenACCDataDirective) dir).getVariableRanges().containsKey(e.getKey())){
								beginRange = ((OpenACCDataDirective) dir).getVariableRanges().get(e.getKey()).getKey();
								endRange = ((OpenACCDataDirective) dir).getVariableRanges().get(e.getKey()).getValue();
							}
							out.println(startLine + "," + endLine + "," + flow + "," + e.getKey() + "," + beginRange + "," + endRange);
						}
					}
				}
			 }
			 out.close();
			    
			
			
			Process process = null;
			String command = this.dataDirectiveAnalyser.getAbsolutePath()+" "+f.getAbsolutePath() + "  "
				+ startLineNo + " " + endLineNo + " "
				+ csvPresentDirectives.getAbsolutePath() + " " + getLanguageString();
			for(String s :  this.includes){
				command += " " + s;
			}

			//data-directive-analyser <source_file> <data_directive_start_line> <data_directive_end_line> <present_directives_csv> <language> [includes...]
			process = Runtime.getRuntime().exec(command);
		
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			String line;

			if((line = errorReader.readLine()) != null){
				String tempLine = "";
				while((tempLine = errorReader.readLine()) != null){
					line+=System.lineSeparator() + tempLine;
				}
				throw new IOException("data-directive-analyser output the following error: "
					+ System.lineSeparator() + line);
			}

			int dataStartLine = -1;
			int dataEndLine = -1;
			Map<String, VARIABLE_STATUS> vars = new HashMap<String, VARIABLE_STATUS>();
			Map<String, Map.Entry<String,String>> varRanges = new HashMap<String, Map.Entry<String,String>>();
			Set<OpenACCUpdateDirective> updateDirectives = new HashSet<OpenACCUpdateDirective>();

			while((line=reader.readLine()) != null){
				// cout << newScopeLoc.startLineNo << ',' << newScopeLoc.endLineNo << ',' << toString(dfi.flow) << ',' << dfi.variable << ',' << dfi.start_range << ',' << dfi.end_range << endl;
				String[] temp = line.split(",",-1);
				if(temp.length != 6 && temp.length !=4) {
					throw new IOException("data-directive-analyser output a CSV where the number of entries was not 6" +
						" (for data directives) or 4 (for update directives). Was found to be "
						+temp.length +": " + System.lineSeparator() + line);
				}


				int startLine = Integer.parseInt(temp[0]);
				int endLine = Integer.parseInt(temp[1]);
				Optional<VARIABLE_STATUS> varStatus; //If empty then it's an update
				
				switch(temp[2]) {
					case "COPY":
						varStatus = Optional.of(VARIABLE_STATUS.COPY);
						break;
					case "COPY_IN":
						varStatus = Optional.of(VARIABLE_STATUS.COPYIN);
						break;
					case "COPY_OUT":
						varStatus = Optional.of(VARIABLE_STATUS.COPYOUT);
						break;
					case "PRESENT":
						varStatus = Optional.of(VARIABLE_STATUS.PRESENT);
						break;
					case "CREATE":
						varStatus = Optional.of(VARIABLE_STATUS.CREATE);
						break;
					default:
						varStatus = Optional.empty();
						break;
				}
				
				String variable = temp[3];
				
				if(varStatus.isPresent()){ // I.e. is a Data Directive
					assert((dataStartLine == -1 && dataEndLine == -1)
							|| (dataStartLine == startLine && endLine == dataEndLine));
					dataStartLine = startLine;
					dataEndLine = endLine;

					if(temp.length != 6) {
						throw new IOException("data-directive-analyser output a CSV where the number of entries was not" +
							" 6 for a data directive. Was found to be "+temp.length +": " + System.lineSeparator() + line);
					}
					String startRange = temp[4];
					String endRange = temp[5];

					vars.put(variable, varStatus.get());
					if(this.includeVariableRanges && !startRange.isEmpty() && !endRange.isEmpty()) {
						varRanges.put(variable, new HashMap.SimpleEntry<String, String>(startRange, endRange));
					}
				} else{ //I.e. is an update directive
					boolean host = false;
					if(temp[2].equals("UPDATE_HOST")){
						host = true;
					} else if(temp[2].equals("UPDATE_DEVICE")){
						host = false;
					} else {
						assert(false);
					}

					if(temp.length !=4) {
						throw new IOException("data-directive-analyser output a CSV where the number of entries was" +
							" 4 (for update directives). Was found to be "+temp.length +": "
							+ System.lineSeparator() + line);
					}
					
					Optional<OpenACCUpdateDirective> updateDir = Optional.empty();
					for(OpenACCUpdateDirective dir: updateDirectives){
						if(dir.getLineNumber() == startLine){
							updateDir = Optional.of(dir);
							break;
						}
					}
					
					if(updateDir.isPresent()){
						updateDir.get().addVariable(variable, host ? UPDATE_TYPE.HOST : UPDATE_TYPE.DEVICE);
					} else {
						Map<String, UPDATE_TYPE> m = new HashMap<String, UPDATE_TYPE>();
						m.put(variable, host ? UPDATE_TYPE.HOST : UPDATE_TYPE.DEVICE);
						updateDirectives.add(new OpenACCUpdateDirective(startLine,f, m));
					}
				}
			}
			reader.close();

			if(dataEndLine == -1 || dataStartLine == -1){
				assert(dataEndLine == -1);
				assert(dataStartLine == -1);
				toReturn = Optional.empty();
			} else {
				toReturn = Optional.of(new OpenACCDataDirective(dataStartLine, dataEndLine, f, vars,
						varRanges, updateDirectives));
			}
		} catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
		
		return toReturn;
	}
}
