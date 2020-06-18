package openaccgi.patch_model;

import openaccgi.program_data.IProgramDataContainer;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PatchFactory {

	public static Patch createEmptyPatch(LANGUAGE lang){
		return new Patch(new ArrayList<ADirective>(),lang);
	}
	
	public static Patch createPatchWithAllLoopsParallelised(
		IProgramDataContainer container, LANGUAGE lang, IToolUtils toolUtils){
		SortedSet<ADirective> toAdd = new TreeSet<ADirective>();
		
		for(File f : container.getFiles()){
			for(int lineNo : container.getLines(f)){
				Map<String, VARIABLE_STATUS> variables = null;
				Map<String, Map.Entry<String, String>> variableRanges = new HashMap<String, Map.Entry<String, String>>();
				try {
					variables = toolUtils.getDataInfoForScope(f, lineNo);
					Set<String> varsInCSV = container.getVariables(f, lineNo);
					for(String var: variables.keySet()){
						if(varsInCSV.contains(var)) {
							Optional<Map.Entry<String, String>> variableRange
								= container.getVariableRange(f, lineNo, var);
							if (variableRange.isPresent()) {
								variableRanges.put(var, variableRange.get());
							}
						}
					}
				} catch(IOException e){
					System.err.println("Error when trying to get dataInfo for parallelised loop (File '"
						+ f.getAbsolutePath() + "', lineNo '"+lineNo+"'");
					System.err.println(e.getMessage());
					System.exit(1);
				}
				assert(variables != null && variableRanges != null);
				OpenACCParallelLoopDirective temp = new OpenACCParallelLoopDirective(lineNo,f, variables,
					variableRanges, Optional.empty(), Optional.empty(), Optional.empty());
				toAdd.add(temp);
			}
		}
		
		return new Patch(toAdd, lang);
	}
}
