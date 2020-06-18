package openaccgi.program_data;

import java.io.File;
import java.util.*;

public interface IProgramDataContainer {
	Set<File> getFiles();
	SortedSet<Integer> getLines(File f) throws NoSuchElementException; //Not sure this is really needs to be a sorted set
	Set<String> getVariables(File f, Integer i) throws NoSuchElementException;
	Optional<Map.Entry<String, String>> getVariableRange(File f, Integer i, String variable) throws NoSuchElementException;
}
