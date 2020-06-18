package openaccgi.patch_model;

import java.io.File;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;

public abstract class ADirective implements Comparator<ADirective>, Comparable<ADirective>{
	private final File file;
	protected boolean active = true;

	/*
	A 'scope creator' is an directive that creates a new scope. For example '#pragma acc data', in the current setup,
	always creates a new scope in code while '#pragma acc parallel loop' does not.
	 */
	private final boolean scopeCreator;
	
	protected ADirective(File f, boolean isScopeCreator){
		this.file = f;
		this.scopeCreator = isScopeCreator;
	}
	
	public File getFile(){
		return this.file;
	}
	
	public boolean isActive(){
		return this.active;
	}
	
	public void setActive(boolean isActive){
		this.active = isActive;
	}

	public boolean isScopeCreator(){
		return this.scopeCreator;
	}

	protected abstract SortedSet<Integer> insertionPoints();
	
	protected abstract Map<Integer, String> getStringsToInsert(LANGUAGE lang);
	
	@Override
	public int compare(ADirective o1, ADirective o2) {
		//In our setup, ADirectives are equal if they target the same file and insert the same directives at the same lines

		if(!o1.getClass().equals(o2.getClass())){
			return o1.getClass().toString().compareTo(o2.getClass().toString());
		}

		if(o1.getFile().equals(o2.getFile())) { //If File is the same
			if(o1.insertionPoints().size() != o2.insertionPoints().size()) {
				return Integer.compare(o1.insertionPoints().size(), o2.insertionPoints().size());
			}
			
			//Check to ensure the insertion points are the same.
			Integer[] o1Array =  (Integer[]) o1.insertionPoints().toArray(new Integer[o1.insertionPoints().size()]);
			Integer[] o2Array = (Integer[]) o2.insertionPoints().toArray(new Integer[o2.insertionPoints().size()]);

			if(o1Array.length != o2Array.length){
				return Integer.compare(o1Array.length, o2Array.length);
			}

			for(int i=0; i<o1Array.length;i++) {
				if(o1Array[i] != o2Array[i]) {
					return Integer.compare(o1Array[i], o2Array[i]);
				}
			}
			
			// Check to see if that to be inserted is the same
			// Don't like this being hardcoded to C_CPP but no alternative right now.
			Map<Integer, String> o1StringsToInsert = o1.getStringsToInsert(LANGUAGE.CPP);
			Map<Integer, String> o2StringsToInsert = o2.getStringsToInsert(LANGUAGE.CPP);
			if(o1StringsToInsert.size() != o2StringsToInsert.size()){
				return Integer.compare(o1StringsToInsert.size(), o2StringsToInsert.size());
			}
			
			for(Map.Entry<Integer, String> o1e : o1StringsToInsert.entrySet()){
				if(!o1e.getValue().equals(o2StringsToInsert.get(o1e.getKey()))){
					return o1e.getValue().compareTo(o2StringsToInsert.get(o1e.getKey()));
				}
			}
			
			//If they are the same then they are equal.
			return Integer.compare(0, 0);
			
		}
		
		return o1.getFile().compareTo(o2.getFile());
	}
	
	@Override
	public int compareTo(ADirective o) {
		return compare(this, o);
	}
	
	
	@Override
	public boolean equals(Object o){
		if(o instanceof ADirective){
			return this.compareTo(((ADirective)o)) == 0 ? true : false;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		int hash=0;
		//Again, I don't like this being hardcoded
		Map<Integer, String> stringsToInsert = this.getStringsToInsert(LANGUAGE.CPP);
		int i=0;
		for(Map.Entry<Integer, String> e : stringsToInsert.entrySet()){
			hash+= i * e.getKey() + i*e.getValue().hashCode();
			i++;
		}
		return hash;
	}
	
}
