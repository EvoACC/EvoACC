package openaccgi.patch_model;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;

public interface IToolUtils {
	public Map<String, VARIABLE_STATUS> getDataInfoForScope(File f, int lineNo) throws IOException;
	SortedSet<DataDirectiveInsertionPoint> getDataDirectiveInsertionPoints(
		File f, ADirective[] presentDirective)throws IOException;
	Optional<OpenACCDataDirective> getDataDirective(
		File f, int startLineNo, int endLineNo, ADirective[] presentDirectives)throws IOException;
}
