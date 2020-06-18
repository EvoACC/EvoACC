package openaccgi.patch_model;

import java.io.File;
import java.util.Comparator;

public class DataDirectiveInsertionPoint implements
		Comparator<DataDirectiveInsertionPoint>, Comparable<DataDirectiveInsertionPoint> {
	private final int startLineNumber;
	private final int endLineNumber;
	private final File f;
	
	public DataDirectiveInsertionPoint(File file, int startLineNo, int endLineNo) {
		this.startLineNumber = startLineNo;
		this.endLineNumber = endLineNo;
		this.f = file;
	}
	
	public int getStartLineNumber() {
		return this.startLineNumber;
	}
	
	public int getEndLineNumber() {
		return this.endLineNumber;
	}
	
	public File getFile() {
		return this.f;
	}

	@Override
	public int compare(DataDirectiveInsertionPoint o1, DataDirectiveInsertionPoint o2) {
		if(o1.getFile().equals(o2.getFile())) {
			if(o1.getStartLineNumber() == o2.getStartLineNumber()) {
				return Integer.compare(o1.getEndLineNumber(), o2.getEndLineNumber());
			}
			return Integer.compare(o1.getStartLineNumber(), o2.getStartLineNumber());
		}
		return o1.getFile().compareTo(o2.getFile());
	}

	@Override
	public int compareTo(DataDirectiveInsertionPoint o) {
		return compare(this, o);
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof DataDirectiveInsertionPoint){
			DataDirectiveInsertionPoint temp = (DataDirectiveInsertionPoint)o;
			return temp.getFile().equals(this.getFile()) && temp.getStartLineNumber()
				== this.getStartLineNumber() && temp.getEndLineNumber() == this.getEndLineNumber();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
	    return 53 * 3 + this.f.hashCode() + this.startLineNumber*31 + this.endLineNumber;
	}
}
