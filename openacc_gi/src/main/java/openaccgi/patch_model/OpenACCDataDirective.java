package openaccgi.patch_model;

import java.io.File;
import java.util.*;

public class OpenACCDataDirective extends AOpenACCDirective {

	//TODO: not a fan of how I've had to modify some of these access controls to make this all work, fix.
	private final Map<String, VARIABLE_STATUS> variables;
	private final Map<String, Map.Entry<String, String>> variableRange;
	private final int startLineNumber;
	private final int endLineNumber;
	private final Set<OpenACCUpdateDirective> updateDirectives;

	public OpenACCDataDirective(int startLineNo, int endLineNo, File f, Map<String, VARIABLE_STATUS> vars, Map<String,
			Map.Entry<String, String>> varRanges, Set<OpenACCUpdateDirective> updates) {
		this(startLineNo, endLineNo, f, vars, varRanges, updates,true);

	}

	//Might seem weird, but subclasses may not be scope creators (such as OpenACCParallelLoop)
	protected OpenACCDataDirective(int startLineNo, int endLineNo, File f, Map<String, VARIABLE_STATUS> vars,
								   Map<String, Map.Entry<String, String>> varRanges,
								   Set<OpenACCUpdateDirective> updates, boolean isScopeCreator){
		super(f, isScopeCreator);
		startLineNumber = startLineNo;
		endLineNumber = endLineNo;
		variables = vars;
		variableRange = varRanges;
		updateDirectives = updates;
		assert(startLineNo <= endLineNo);
	}
	
	public Map<String, VARIABLE_STATUS> getVariables(){
		return Collections.unmodifiableMap(variables);
	}

	public Map<String, Map.Entry<String, String>> getVariableRanges() {
		return Collections.unmodifiableMap(variableRange);
	}

	public void setVariable(String variable, VARIABLE_STATUS varStatus) {
		variables.put(variable, varStatus);
	}

	public void setVariableRange(String variable, Map.Entry<String, String> ranges){
		variableRange.put(variable, ranges);
	}

	public void wipeVariables(){
		variables.clear();
		variableRange.clear();
	}

	public void wipeUpdateDirectives(){
		updateDirectives.clear();
	}

	public void addUpdateDirective(OpenACCUpdateDirective dir){
		updateDirectives.add(dir);
	}

	public boolean isEqualTo(DataDirectiveInsertionPoint d){
		return d.getFile() == this.getFile() && d.getStartLineNumber()
			== this.startLineNumber && d.getEndLineNumber() == this.endLineNumber;
	}

	public Set<OpenACCUpdateDirective> getUpdateDirectives(){
		return Collections.unmodifiableSet(this.updateDirectives);
	}

	@Override
	protected Map<Integer, String> getStringsToInsert(LANGUAGE lang) {
		Map<Integer, String> toReturn = new HashMap<Integer, String>();
		switch(lang){
			case CPP:
			case C:
				toReturn.put(this.startLineNumber, "#pragma acc data "
					+ getDataDirectivesString(lang) + System.lineSeparator() + "{");
				toReturn.put(this.endLineNumber, "}");
				for(OpenACCUpdateDirective update : this.updateDirectives){
					toReturn.putAll(update.getStringsToInsert(lang));
				}
				break;
			default:
				assert(false);
				break;
		}


		return toReturn;
	}
	
	public int getStartLineNumber(){
		return this.startLineNumber;
	}
	
	public int getEndLineNumber(){
		return this.endLineNumber;
	}
	
	protected String getDataDirectivesString(LANGUAGE lang){
		StringBuilder dataDirective = new StringBuilder();
		switch(lang){
			case CPP:
			case C:
				List<String> copy = new ArrayList<String>();
				List<String> copyin = new ArrayList<String>();
				List<String> copyout = new ArrayList<String>();
				List<String> create = new ArrayList<String>();
				List<String> present = new ArrayList<String>();
				for(Map.Entry<String, VARIABLE_STATUS> p : variables.entrySet()){
					switch(p.getValue()){
						case NONE:
							break;
						case COPY:
							copy.add(p.getKey());
							break;
						case COPYIN:
							copyin.add(p.getKey());
							break;
						case COPYOUT:
							copyout.add(p.getKey());
							break;
						case CREATE:
							create.add(p.getKey());
							break;
						case PRESENT:
							present.add(p.getKey());
							break;
						default:
							assert(false);
							break;
						
					}
				}
			
				dataDirective.append(getDataArgument("pcopy", copy));
				dataDirective.append(getDataArgument("pcopyin", copyin));
				dataDirective.append(getDataArgument("pcopyout", copyout));
				dataDirective.append(getDataArgument("create", create));
				dataDirective.append(getDataArgument("present", present));
				break;
		}
		return dataDirective.toString();
	}
	
	private String getDataArgument(String argumentName, List<String> vars){
		StringBuilder toReturn = new StringBuilder();
		
		if(!vars.isEmpty()){
			toReturn.append(argumentName);
			toReturn.append("(");
			for(int i=0; i<vars.size();i++){
				toReturn.append(vars.get(i));
				if(this.variableRange.containsKey(vars.get(i))){
					toReturn.append("[" + this.variableRange.get(vars.get(i)).getKey()
							+ ":" + this.variableRange.get(vars.get(i)).getValue() + "]");
				}
				if(i!=vars.size()-1){
					toReturn.append(",");
				}
			}
			toReturn.append(") ");
		}
		
		return toReturn.toString();
	}

	@Override
	protected SortedSet<Integer> insertionPoints() {
		SortedSet<Integer> sortedSet = new TreeSet<Integer>();
		sortedSet.add(this.startLineNumber);
		sortedSet.add(this.endLineNumber);
		return sortedSet;
	}
}
