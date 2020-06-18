package openaccgi.patch_model;

import java.io.File;
import java.util.*;

/*
NOTE: OpenACCUpdateDirective should only exist within an OpenACCDataDirective
 */
public class OpenACCUpdateDirective extends AOpenACCDirective {
	
	public enum UPDATE_TYPE{
		HOST,
		DEVICE
	};
	
	private final int lineNumber;
	private final Map<String, UPDATE_TYPE> variables;

	public OpenACCUpdateDirective(int lineNo, File f, Map<String, UPDATE_TYPE> vars) {
		super(f, false);
		this.lineNumber = lineNo;
		this.variables = vars;
	}
	
	public Map<String, UPDATE_TYPE> getVariables(){
		return Collections.unmodifiableMap(this.variables);
	}
	
	public void addVariable(String var , UPDATE_TYPE type) {
		this.variables.put(var, type);
	}
	
	public void removeVariable(String var) {
		variables.remove(var);
	}
	
	public int getLineNumber(){
		return this.lineNumber;
	}

	@Override
	protected Map<Integer, String> getStringsToInsert(LANGUAGE lang) {
		Map<Integer, String> toReturn = new HashMap<Integer, String>();
		
		switch(lang) {
			case CPP:
			case C:
				StringBuilder dirString = new StringBuilder();
				dirString.append("#pragma acc update ");
				
				Set<String> host = new HashSet<String>();
				Set<String> device = new HashSet<String>();
				for(Map.Entry<String, UPDATE_TYPE> e : this.variables.entrySet()) {
					if(e.getValue() == UPDATE_TYPE.HOST) {
						host.add(e.getKey());
					} else if(e.getValue() == UPDATE_TYPE.DEVICE) {
						device.add(e.getKey());
					} else {
						assert(false);
					}
				}
				
				if(!host.isEmpty()) {
					dirString.append("host(");
					Iterator<String> it = host.iterator();
					while(it.hasNext()) {
						dirString.append(it.next());
						if(it.hasNext()) {
							dirString.append(", ");
						}
					}
					dirString.append(") ");
				}
				
				if(!device.isEmpty()) {
					dirString.append("device(");
					Iterator<String> it = device.iterator();
					while(it.hasNext()) {
						dirString.append(it.next());
						if(it.hasNext()) {
							dirString.append(", ");
						}
					}
					dirString.append(")");
				}
				
				toReturn.put(this.lineNumber, dirString.toString());
				break;
			default:
				assert(false);
				break;
		}
		
		return toReturn;
	}
	
	@Override
	protected SortedSet<Integer> insertionPoints() {
		SortedSet<Integer> toReturn = new TreeSet<Integer>();
		toReturn.add(this.lineNumber);
		return toReturn;
	}
}
