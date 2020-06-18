package openaccgi.patch_model;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

public class OpenACCParallelLoopDirective extends OpenACCDataDirective {
	
	private Optional<Integer> numGangs;
	private Optional<Integer> numWorkers;
	private Optional<Integer> vectorLength;

	public OpenACCParallelLoopDirective(int lineNo, File f, Map<String, VARIABLE_STATUS> vars,
	                                    Map<String, Map.Entry<String, String>> varRanges,
	                                    Optional<Integer> numGangs, Optional<Integer> numWorkers,
	                                    Optional<Integer> vectorLength){
		super(lineNo, lineNo, f, vars, varRanges, new HashSet<OpenACCUpdateDirective>(), false);
		this.numGangs = numGangs;
		this.numWorkers = numWorkers;
		this.vectorLength = vectorLength;
	}
	
	private void checkParameterValidity(Optional<Integer> parameter) throws IllegalArgumentException{
		if(parameter.isPresent() && parameter.get() < 0) {
			throw new IllegalArgumentException("Parameter values must be >= 2^0. Value given: 2^" + parameter.get());
		}
	}
	
	public Optional<Integer> getNumGangs(){
		return this.numGangs;
	}
	
	public void setNumGangs(Optional<Integer> gangs) throws IllegalArgumentException {
		checkParameterValidity(gangs);
		this.numGangs = gangs;
	}
	
	public Optional<Integer> getNumWorkers(){
		return this.numWorkers;
	}
	
	public void setNumWorkers(Optional<Integer> workers) throws IllegalArgumentException {
		checkParameterValidity(workers);
		this.numWorkers = workers;
	}
	
	public Optional<Integer> getVectorLength(){
		return this.vectorLength;
	}
	
	public void setVectorLength(Optional<Integer> vecLength) throws IllegalArgumentException {
		checkParameterValidity(vecLength);
		this.vectorLength = vecLength;
	}
	
	@Override
	protected Map<Integer, String> getStringsToInsert(LANGUAGE lang) {
		Map<Integer, String> toReturn = new HashMap<Integer, String>();
		switch(lang){
			case CPP:
			case C:
				StringBuilder directive = new StringBuilder();
				directive.append("#pragma acc parallel loop ");
				if(numGangs.isPresent()){
					directive.append("num_gangs(");
					directive.append((int)Math.pow(2, numGangs.get()));
					directive.append(") ");
				}
				if(numWorkers.isPresent()){
					directive.append("num_workers(");
					directive.append((int)Math.pow(2, numWorkers.get()));
					directive.append(") ");
				}
				if(vectorLength.isPresent()){
					directive.append("vector_length(");
					directive.append((int)Math.pow(2, vectorLength.get()));
					directive.append(") ");
				}
				directive.append(getDataDirectivesString(lang));
				toReturn.put(this.getStartLineNumber(), directive.toString());
				break;
			default:
				assert(false);
				break;
		}
		return toReturn;
	}
	

}

