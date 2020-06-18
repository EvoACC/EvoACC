package openaccgi.optimisation;

import ec.vector.BitVectorIndividual;
import openaccgi.patch_model.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//All the tests here are horribly dependent on the MockToolUtils. It's messy so this is messy

public class DataOptimiserEvolutionaryECJFitnessTest {

    @Test
    public void setPatchTest(){
        Patch p = new Patch(new ArrayList<ADirective>(), LANGUAGE.CPP);
        DataOptimiserEvolutionaryECJFitness.setPatch(p);
        assertEquals(p, DataOptimiserEvolutionaryECJFitness.getPatch());
    }

    @Test
    public void applyToPatchTest1(){
        /* In this test, none of the insertion points interact with each other, causing epistasis.
         * Furthermore, there is no UPDATE statements necessisary for any of the insertion points.
         */

        File f = null;
        try {
            f = File.createTempFile("test", ".tmp");
        } catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
        Patch p = new Patch(new ArrayList<ADirective>(), LANGUAGE.CPP);
        DataOptimiserEvolutionaryECJFitness.setPatch(p);

        MockToolUtils toolUtils = new MockToolUtils();
        Map<String, VARIABLE_STATUS> variableInfo = new HashMap<String, VARIABLE_STATUS>();
        variableInfo.put("tempVar", VARIABLE_STATUS.COPY);

        OpenACCDataDirective dataDir1 = new OpenACCDataDirective(1,5,f,variableInfo,
                new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>());
        toolUtils.addDataDirective(f, 1, 5, Optional.of(dataDir1));

        OpenACCDataDirective dataDir2 = new OpenACCDataDirective(7,10,f, variableInfo,
                new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>());
        toolUtils.addDataDirective(f, 7, 10, Optional.of(dataDir2));

        OpenACCDataDirective dataDir3 = new OpenACCDataDirective(12,16,f, variableInfo,
                new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>());
        toolUtils.addDataDirective(f, 12, 16, Optional.of(dataDir3));

        OpenACCDataDirective dataDir4 = new OpenACCDataDirective(18,32,f, variableInfo,
                new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>());
        toolUtils.addDataDirective(f, 18, 32, Optional.of(dataDir4));

        DataOptimiserEvolutionaryECJFitness.setToolUtils(toolUtils);

        DataOptimiserEvolutionaryECJFitness.setFitnessFunction(new MaxDirectivesFitnessFunction());

        SortedSet<DataDirectiveInsertionPoint> insertionPoints = new TreeSet<DataDirectiveInsertionPoint>();
        insertionPoints.add(new DataDirectiveInsertionPoint(f, 1, 5));
        insertionPoints.add(new DataDirectiveInsertionPoint(f, 7,10));
        insertionPoints.add(new DataDirectiveInsertionPoint(f, 12, 16));
        insertionPoints.add(new DataDirectiveInsertionPoint(f, 18, 32));
        toolUtils.addDataDirectiveInsertionPoint(f, insertionPoints);
        ADirective dirs[] = {};
        DataOptimiserEvolutionaryECJFitness.setInsertionPoints(toolUtils.getDataDirectiveInsertionPoints(f, dirs));

        BitVectorIndividual bInd = new BitVectorIndividual();
        boolean[] genotype = {true, false, true, true};
        bInd.setGenome(genotype);

        DataOptimiserEvolutionaryECJFitness.applyToPatch(bInd);
        DataOptimiserEvolutionaryECJFitness.close();

        Set<OpenACCDataDirective> directivesExpected = new HashSet<OpenACCDataDirective>();
        directivesExpected.add(dataDir1);
        directivesExpected.add(dataDir3);
        directivesExpected.add(dataDir4);
        assertEquals(3, p.getDirectives().length);
        for(ADirective d : p.getDirectives()){
            assertTrue(d.isActive());
            assertTrue(directivesExpected.contains(d));
        }
    }

    @Test
    public void applyToPatchTest2(){
        /* In this test, there is interaction between two of the Data directive scopes that may be inserted
         * (i.e. they overlap, only one may exist).
         */

        File f = null;
        try {
            f = File.createTempFile("test", ".tmp");
        } catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
        Patch p = new Patch(new ArrayList<ADirective>(), LANGUAGE.CPP);
        DataOptimiserEvolutionaryECJFitness.setPatch(p);

        MockToolUtils toolUtils = new MockToolUtils();
        Map<String, VARIABLE_STATUS> variableInfo = new HashMap<String, VARIABLE_STATUS>();
        variableInfo.put("tempVar", VARIABLE_STATUS.COPY);

        //New udpdate statment here (Host, "mysteryVar", line 3)
        Map<String, OpenACCUpdateDirective.UPDATE_TYPE> updateStatementVars = new HashMap<String, OpenACCUpdateDirective.UPDATE_TYPE>();
        updateStatementVars.put("mysteryVar", OpenACCUpdateDirective.UPDATE_TYPE.HOST);
        OpenACCUpdateDirective updateStatement = new OpenACCUpdateDirective(3, f, updateStatementVars);
        Set<OpenACCUpdateDirective> updateStatements = new HashSet<OpenACCUpdateDirective>();
        updateStatements.add(updateStatement);
        Map<String, VARIABLE_STATUS> newVariableInfo = new HashMap<String, VARIABLE_STATUS>();
        OpenACCDataDirective dataDir1 = new OpenACCDataDirective(1,5,f,variableInfo,
                new HashMap<String, Map.Entry<String, String>>(),updateStatements);
        toolUtils.addDataDirective(f, 1, 5, Optional.of(dataDir1));

        OpenACCDataDirective dataDir2 = new OpenACCDataDirective(7,10,f, variableInfo,
                new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>());
        toolUtils.addDataDirective(f, 7, 10, Optional.of(dataDir2));


        OpenACCDataDirective dataDir3 = new OpenACCDataDirective(12,16,f, variableInfo,
                new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>());
        toolUtils.addDataDirective(f, 12, 16, Optional.of(dataDir3));
        //Note: the following cannot exist if dataDir2 is present: the scopes overlap
        toolUtils.addExclusion(dataDir2, dataDir3);
        toolUtils.addExclusion(dataDir3, dataDir2);

        OpenACCDataDirective dataDir4 = new OpenACCDataDirective(18,32,f, variableInfo,
                new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>());
        toolUtils.addDataDirective(f, 18, 32, Optional.of(dataDir4));

        DataOptimiserEvolutionaryECJFitness.setToolUtils(toolUtils);

        DataOptimiserEvolutionaryECJFitness.setFitnessFunction(new MaxDirectivesFitnessFunction());

        SortedSet<DataDirectiveInsertionPoint> insertionPoints = new TreeSet<DataDirectiveInsertionPoint>();
        insertionPoints.add(new DataDirectiveInsertionPoint(f, 1, 5));
        insertionPoints.add(new DataDirectiveInsertionPoint(f, 7,10));
        insertionPoints.add(new DataDirectiveInsertionPoint(f, 12, 16));
        insertionPoints.add(new DataDirectiveInsertionPoint(f, 18, 32));
        toolUtils.addDataDirectiveInsertionPoint(f, insertionPoints);
        ADirective dirs[] = {};
        DataOptimiserEvolutionaryECJFitness.setInsertionPoints(toolUtils.getDataDirectiveInsertionPoints(f, dirs));

        BitVectorIndividual bInd = new BitVectorIndividual();
        boolean[] genotype = {true, true, true, true};
        bInd.setGenome(genotype);

        DataOptimiserEvolutionaryECJFitness.applyToPatch(bInd);
        DataOptimiserEvolutionaryECJFitness.close();

        Set<ADirective> directivesExpected = new HashSet<ADirective>();
        directivesExpected.add(dataDir1);
        directivesExpected.add(dataDir2);
        directivesExpected.add(dataDir4);

        assertEquals(3, p.getDirectives().length);
        for(ADirective d : p.getDirectives()){
            assertTrue(d.isActive());
            assertTrue(directivesExpected.contains(d));
        }
    }

}
