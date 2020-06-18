package openaccgi.patch_model;

import java.io.File;
import java.util.*;

public class Patch {
	
	private final SortedSet<ADirective> directives;
	private final LANGUAGE lang;
	
	public Patch(Collection<ADirective> dirs, LANGUAGE l){
		this.directives = new TreeSet<ADirective>(dirs); //dirs.toArray(new ADirective[dirs.size()]);
		this.lang = l;
	}
	
	public ADirective[] getDirectives(){
		return (ADirective[]) this.directives.toArray(new ADirective[this.directives.size()]);
	}
	
	public void addADirective(ADirective dir) {
		this.directives.add(dir);
	}
	
	public boolean removeDirective(ADirective dir){
		return this.directives.remove(dir);
	}

	//Map to Return is sorted simply for consistency --- i.e. I do not want one genotype to produce more than one patch
	private SortedMap<File, SortedMap<Integer, String>> getPatchData(){
		//<The file, map<LineNumber, List<ADirective, String (derived from the ADirective) to insert at that line number>>>>
		Map<File, SortedMap<Integer, List<Map.Entry<ADirective,String>>>> dataMap = new TreeMap<File, SortedMap<Integer, List<Map.Entry<ADirective,String>>>>();
		for(ADirective d: this.getDirectives()){
			if(d.isActive()){
				if(!dataMap.containsKey(d.getFile())){
					dataMap.put(d.getFile(), new TreeMap<Integer, List<Map.Entry<ADirective,String>>>());
				}
				for(Map.Entry<Integer, String> e : d.getStringsToInsert(this.lang).entrySet()){
					int lineNo = e.getKey();
					String toAdd = e.getValue();
					List<Map.Entry<ADirective, String>> l =null;
					assert(dataMap.containsKey(d.getFile()));
					if(dataMap.get(d.getFile()).containsKey(e.getKey())){
						l = dataMap.get(d.getFile()).get(e.getKey());
					} else {
						l = new ArrayList<Map.Entry<ADirective, String>>();
					}
					assert(l != null);

					if(d.isScopeCreator() && !l.isEmpty()){
						int locToInsert = -1;
						for(int i=0; i<l.size(); i++){
							if(!l.get(i).getKey().isScopeCreator()) {
								locToInsert = i;
								break;
							}
						}
						if(locToInsert == -1){
							l.add(new AbstractMap.SimpleEntry<ADirective, String>(d, toAdd));
						} else {
							l.add(locToInsert, new AbstractMap.SimpleEntry<ADirective, String>(d, toAdd));
						}
					} else {
						l.add(new AbstractMap.SimpleEntry<ADirective, String>(d, toAdd));
					}

					assert(dataMap.containsKey(d.getFile()));
					dataMap.get(d.getFile()).put(e.getKey(), l);
				}
			}
		}

		//Convert the 'dataMap' to the 'toReturn'
		SortedMap<File, SortedMap<Integer, String>> toReturn = new TreeMap<File, SortedMap<Integer, String>>();

		for(Map.Entry<File, SortedMap<Integer, List<Map.Entry<ADirective,String>>>> e1 : dataMap.entrySet()){
			SortedMap<Integer, String> toReturnMap = new TreeMap<Integer, String>();
			toReturn.put(e1.getKey(), toReturnMap);
			for(Map.Entry<Integer, List<Map.Entry<ADirective,String>>> e2 : e1.getValue().entrySet()){
				StringBuilder sb = new StringBuilder();
				for(Map.Entry<ADirective,String> e3 : e2.getValue()){
					sb.append(e3.getValue() + System.lineSeparator());
				}
				toReturn.get(e1.getKey()).put(e2.getKey(), sb.toString());
			}
		}

		return toReturn;
	}
	
	public String getPatch(){
		StringBuilder toReturn = new StringBuilder();
		boolean isFirst = true;
		for(Map.Entry<File, SortedMap<Integer, String>> entry1 : this.getPatchData().entrySet()){
			File f = entry1.getKey();
			if(!isFirst){
				toReturn.append(System.lineSeparator());
			} else {
				isFirst = false;
			}
			toReturn.append("--- ");
			toReturn.append(f.getAbsolutePath());
			toReturn.append(System.lineSeparator());
			toReturn.append("+++ ");
			toReturn.append(f.getAbsolutePath());
			
			for(Map.Entry<Integer, String> entry2 : entry1.getValue().entrySet()){
				int lineNo = entry2.getKey();
				Scanner scanner = new Scanner(entry2.getValue());
				toReturn.append(System.lineSeparator());
				toReturn.append("@@ -");
				toReturn.append(lineNo - 1);
				toReturn.append(",0 +");
				toReturn.append(lineNo - 1);
				toReturn.append("," + entry2.getValue().split("\n").length + " @@");
				while(scanner.hasNextLine()){
					toReturn.append(System.lineSeparator());
					toReturn.append("+");
					toReturn.append(scanner.nextLine());
				}
				scanner.close();
			}
		}
		if(!this.getPatchData().entrySet().isEmpty()) { // No newline if the patch is empty.
			toReturn.append(System.lineSeparator());
		}
		return toReturn.toString();
	}
}
