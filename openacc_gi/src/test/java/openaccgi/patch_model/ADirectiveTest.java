package openaccgi.patch_model;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotSame;

public class ADirectiveTest {

    @Test
    public void equalsTest_Equals1() throws IOException {
        int p1LineNo = 10;
        File p1File = File.createTempFile("test", ".tmp");
        Map<String, VARIABLE_STATUS> p1Vars = new HashMap<String,VARIABLE_STATUS>();
        Map<String, Map.Entry<String, String>> p1VarsRanges = new HashMap<String, Map.Entry<String, String>>();
        OpenACCParallelLoopDirective p1 = new OpenACCParallelLoopDirective(p1LineNo, p1File, p1Vars, p1VarsRanges,
            Optional.empty(), Optional.empty(), Optional.empty());
        OpenACCParallelLoopDirective p2 = new OpenACCParallelLoopDirective(p1LineNo, p1File, p1Vars, p1VarsRanges,
            Optional.empty(), Optional.empty(), Optional.empty());

        assertEquals(p1,p2);
    }

    @Test
    public void equalsTest_NotEquals1() throws IOException {
        int p1LineNo = 10;
        File p1File = File.createTempFile("test", ".tmp");
        Map<String, VARIABLE_STATUS> p1Vars = new HashMap<String, VARIABLE_STATUS>();
        Map<String, Map.Entry<String, String>> p1VarsRanges = new HashMap<String, Map.Entry<String, String>>();
        OpenACCParallelLoopDirective p1 = new OpenACCParallelLoopDirective(p1LineNo, p1File, p1Vars, p1VarsRanges,
            Optional.empty(), Optional.empty(), Optional.empty());

        //int startLineNo, int endLineNo, File f, Map<String, VARIABLE_STATUS> vars, boolean isScopeCreator
        OpenACCDataDirective d1 = new OpenACCDataDirective(p1.getStartLineNumber(), 20, p1File, p1Vars,
            p1VarsRanges, new HashSet<OpenACCUpdateDirective>());

        assertNotSame(p1, d1);
    }

    @Test
    public void equalsTest_NotEquals2() throws IOException {
        int p1LineNo = 10;
        File p1File = File.createTempFile("test", ".tmp");
        Map<String, VARIABLE_STATUS> p1Vars = new HashMap<String, VARIABLE_STATUS>();
        Map<String, Map.Entry<String, String>> p1VarsRanges = new HashMap<String, Map.Entry<String, String>>();
        OpenACCParallelLoopDirective p1 = new OpenACCParallelLoopDirective(p1LineNo, p1File, p1Vars, p1VarsRanges,
            Optional.empty(), Optional.empty(), Optional.empty());

        //int startLineNo, int endLineNo, File f, Map<String, VARIABLE_STATUS> vars, boolean isScopeCreator
        OpenACCDataDirective d1 = new OpenACCDataDirective(p1.getStartLineNumber(), p1.getEndLineNumber(), p1File,
            p1Vars, p1VarsRanges, new HashSet<OpenACCUpdateDirective>());

        assertNotSame(p1, d1);
    }
}
