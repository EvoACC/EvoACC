package openaccgi.patch_model;

import java.io.File;
import java.util.*;

public class MockToolUtils implements IToolUtils {
	
	//To best honest, this is really hacky and could be improved, but it's just for testing so I grin and bare it.
	
	private Map<File, Map<Integer, Map<String, VARIABLE_STATUS>>> dataInfo = new HashMap<File, Map<Integer, Map<String, VARIABLE_STATUS>>>();
	private Map<File, SortedSet<DataDirectiveInsertionPoint>> dataInsertionInfo = new HashMap<File, SortedSet<DataDirectiveInsertionPoint>>();
	private Map<File, Map<Integer, Map<Integer, Optional<OpenACCDataDirective>>>> dataDirectiveInfo = new HashMap<File, Map<Integer, Map<Integer,  Optional<OpenACCDataDirective>>>>();

	//If -first- is present, those in the -second- set are invalid
	private Map<ADirective, Set<ADirective>> exclusions = new HashMap<ADirective, Set<ADirective>>();
	
	public void addDataInfo(File f, int lineNo, Map<String, VARIABLE_STATUS> toAdd){
		if(!dataInfo.containsKey(f)){
			dataInfo.put(f, new HashMap<Integer, Map<String, VARIABLE_STATUS>>());
		}
		if(!dataInfo.get(f).containsKey(lineNo)){
			dataInfo.get(f).put(lineNo, toAdd);
		} else {
			dataInfo.get(f).get(lineNo).putAll(toAdd);
		}
	}

	@Override
	public Map<String, VARIABLE_STATUS> getDataInfoForScope(File f, int lineNo) {
		return dataInfo.get(f).get(lineNo);
	}
	
	public void addDataDirectiveInsertionPoint(File f, SortedSet<DataDirectiveInsertionPoint> s){
		this.dataInsertionInfo.put(f, s);
	}

	@Override
	public SortedSet<DataDirectiveInsertionPoint> getDataDirectiveInsertionPoints(File f,
			ADirective[] presentDirective) {
		SortedSet<DataDirectiveInsertionPoint> toReturn = new TreeSet<DataDirectiveInsertionPoint>(dataInsertionInfo.get(f));

		//The purpose of this absolute mess is to remove any DataDirectiveInsertionPoints that should be excluded
		for(ADirective d : presentDirective){

			SortedSet<DataDirectiveInsertionPoint> toRemove = new TreeSet<DataDirectiveInsertionPoint>();
			if(this.exclusions.containsKey(d)) {
				for(ADirective d2 : this.exclusions.get(d)){
					if(d2 instanceof OpenACCDataDirective){
						OpenACCDataDirective d2OpenACC = ((OpenACCDataDirective)d2);
						for(DataDirectiveInsertionPoint tr : toReturn) {
							if (d2OpenACC.isEqualTo(tr)){
								toRemove.add(tr);
							}
						}
					}
				}


				toReturn.removeAll(toRemove);
			}
		}
		return toReturn;
	}
	
	public void addDataDirective(File f, int startLineNo, int endLineNo,  Optional<OpenACCDataDirective> d){
		if(!this.dataDirectiveInfo.containsKey(f)){
			this.dataDirectiveInfo.put(f, new HashMap<Integer, Map<Integer,  Optional<OpenACCDataDirective>>>());
		}
		if(!this.dataDirectiveInfo.get(f).containsKey(startLineNo)){
			this.dataDirectiveInfo.get(f).put(startLineNo, new HashMap<Integer,  Optional<OpenACCDataDirective>>());
		}
		this.dataDirectiveInfo.get(f).get(startLineNo).put(endLineNo, d);
	}

	@Override
	public Optional<OpenACCDataDirective> getDataDirective(File f, int startLineNo, int endLineNo,
														   ADirective[] presentDirectives) {
		return dataDirectiveInfo.get(f).get(startLineNo).get(endLineNo);
	}

	public void addExclusion(ADirective ifExists, ADirective excludeThis){
		if(!exclusions.containsKey(ifExists)){
			exclusions.put(ifExists, new HashSet<ADirective>());
		}
		exclusions.get(ifExists).add(excludeThis);
	}

}
