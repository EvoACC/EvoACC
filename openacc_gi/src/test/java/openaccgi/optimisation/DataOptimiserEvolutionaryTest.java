package openaccgi.optimisation;

import openaccgi.patch_model.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataOptimiserEvolutionaryTest {

	private static final File LOOP_ANALYSER = new File("../clang_tools/llvm-build/bin/loop-analyser");
	private static final File DATA_INSERTION_FINDER = new File("../clang_tools/llvm-build/bin/data-insertion-finder");
	private static final File DATA_DIRECTIVE_ANALYSER = new File("../clang_tools/llvm-build/bin/data-directive-analyser");

	@Test
	public void optimiseTest() throws IOException {
		
		//Create the patch
		int lineNo1 = 5;
		File f1 = File.createTempFile("test", ".tmp");
		OpenACCParallelLoopDirective dir1 = new OpenACCParallelLoopDirective(lineNo1, f1,
			new HashMap<String, VARIABLE_STATUS>(),new HashMap<String, Map.Entry<String, String>>(),Optional.empty(),
			Optional.empty(), Optional.empty());
		
		int lineNo2 = 17;
		File f2 = File.createTempFile("test", ".tmp");
		ADirective dir2 = new OpenACCParallelLoopDirective(lineNo2, f2, new HashMap<String, VARIABLE_STATUS>(),
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		
		List<ADirective> dirs = new ArrayList<ADirective>();
		dirs.add(dir1);
		dirs.add(dir2);
		
		Patch p = new Patch(dirs, LANGUAGE.CPP);
		
		//Create the ideal patch
		Set<ADirective> ideal = new HashSet<ADirective>();
		
		Map<String, VARIABLE_STATUS> v1 = new HashMap<String, VARIABLE_STATUS>();
		v1.put("var1", VARIABLE_STATUS.COPY);
		v1.put("var2", VARIABLE_STATUS.CREATE);
		OpenACCDataDirective d1 = new OpenACCDataDirective(0, 15, f1, v1,
				new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>());
		ideal.add(d1);
		

		Map<String, OpenACCUpdateDirective.UPDATE_TYPE> uInfo1 = new HashMap<String, OpenACCUpdateDirective.UPDATE_TYPE>();
		uInfo1.put("varA", OpenACCUpdateDirective.UPDATE_TYPE.HOST);
		OpenACCUpdateDirective u1 = new OpenACCUpdateDirective(13, f2, uInfo1);
		Set<OpenACCUpdateDirective> updateDirectives = new HashSet<OpenACCUpdateDirective>();
		updateDirectives.add(u1);
		Map<String, VARIABLE_STATUS> v2 = new HashMap<String, VARIABLE_STATUS>();
		v2.put("varA", VARIABLE_STATUS.COPY);
		v2.put("var1", VARIABLE_STATUS.COPYOUT);
		OpenACCDataDirective d2 = new OpenACCDataDirective(10, 20, f2, v1,
				new HashMap<String, Map.Entry<String, String>>(),updateDirectives);
		ideal.add(d2);
		
		CustomDataFitnessFunction fitnessFunction = new CustomDataFitnessFunction(ideal);
		
		//Populate the MockToolUtils with the correct data
		MockToolUtils toolUtils = new MockToolUtils();
		SortedSet<DataDirectiveInsertionPoint> insertionPoints1 = new TreeSet<DataDirectiveInsertionPoint>();
		insertionPoints1.add(new DataDirectiveInsertionPoint(f1,0,15));
		insertionPoints1.add(new DataDirectiveInsertionPoint(f1,2, 13));
		insertionPoints1.add(new DataDirectiveInsertionPoint(f1, 16, 18));
		toolUtils.addDataDirectiveInsertionPoint(f1, insertionPoints1);
		
		SortedSet<DataDirectiveInsertionPoint> insertionPoints2 = new TreeSet<DataDirectiveInsertionPoint>();
		insertionPoints2.add(new DataDirectiveInsertionPoint(f2, 10, 20));
		insertionPoints2.add(new DataDirectiveInsertionPoint(f2, 11, 19));
		insertionPoints2.add(new DataDirectiveInsertionPoint(f2, 25, 30));
		toolUtils.addDataDirectiveInsertionPoint(f2, insertionPoints2);

		toolUtils.addDataDirective(f1, 0 , 15, Optional.of(d1));

		toolUtils.addDataDirective(f2, 10, 20, Optional.of(d2));

		toolUtils.addDataDirective(f1, 2, 13, Optional.of(new OpenACCDataDirective(2,
				13, f1, new HashMap<String, VARIABLE_STATUS>(),
				new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>())));

		toolUtils.addDataDirective(f1, 16, 18, Optional.of(
				new OpenACCDataDirective(16,18,f1, new HashMap<String, VARIABLE_STATUS>(),
				new HashMap<String, Map.Entry<String, String>>(),new HashSet<OpenACCUpdateDirective>())));

		toolUtils.addDataDirective(f2, 11, 19, Optional.of(
				new OpenACCDataDirective(11, 19, f2, new HashMap<String, VARIABLE_STATUS>(),
				new HashMap<String, Map.Entry<String, String>>(), new HashSet<OpenACCUpdateDirective>())));

		toolUtils.addDataDirective(f2, 25, 30, Optional.of(
				new OpenACCDataDirective(25,30, f2, new HashMap<String, VARIABLE_STATUS>(),
				new HashMap<String, Map.Entry<String, String>>(), new HashSet<OpenACCUpdateDirective>())));
		
		//Run the optimiser
		DataOptimiserEvolutionary optimiser = new DataOptimiserEvolutionary(fitnessFunction, 500, 50,
			new Random(4),Optional.empty(), toolUtils);
		optimiser.optimise(p);

		//Check the results
		ideal.add(dir1);
		ideal.add(dir2);
		assertEquals(ideal.size(), p.getDirectives().length);
		for(int i=0; i < p.getDirectives().length; i++){
			assertTrue(ideal.contains(p.getDirectives()[i]));
		}
	}

	@Test
	public void optimiseTest_2() throws IOException{

		//This test just checks to ensure no assertion errors are thrown

		String input2 = "int global_1 = 0;" + System.lineSeparator();                 //1
		input2 += "extern int global_2;" + System.lineSeparator();                    //2
		input2 += "int add(int a, int b){" + System.lineSeparator();                  //3
		input2 += "return a + b;" + System.lineSeparator();                           //4
		input2 += "}" + System.lineSeparator();                                       //5
		input2 += "int plusOne(int a){" + System.lineSeparator();                     //6
		input2 += "return a + 1;" + System.lineSeparator();                           //7
		input2 += "}" + System.lineSeparator();                                       //8
		input2 += "int function_1(int input){" + System.lineSeparator();              //9
		input2 += "int* pointer;" + System.lineSeparator();                           //10
		input2 += "int toReturn = 0;" + System.lineSeparator();                       //11
		input2 += "if(true){" + System.lineSeparator();                               //12
		input2 += "int a = 0;" + System.lineSeparator();                              //13
		input2 += "toReturn++;" + System.lineSeparator();                             //14
		input2 += "}" + System.lineSeparator();                                       //15

		input2 += "for(int i=0; i < 10; i++){" + System.lineSeparator();              //16
		input2 += "int bla[20]; //Not initialised" + System.lineSeparator();          //17
		input2 += "for(int j=0; j < 20; j++){" + System.lineSeparator();              //18
		input2 += "toReturn = add(plusOne(bla[j]), input);" + System.lineSeparator(); //19
		input2 += "bla[j] = 6;" + System.lineSeparator();                             //20
		input2 += "}" + System.lineSeparator();                                       //21
		input2 += "}" + System.lineSeparator();										  //22
		input2 += "for(int i=0; i < 10; i++){" + System.lineSeparator();              //23
		input2 += "int bla[20]; //Not initialised" + System.lineSeparator();          //24
		input2 += "for(int j=0; j < 20; j++){" + System.lineSeparator();              //25
		input2 += "toReturn = add(plusOne(bla[j]), input);" + System.lineSeparator(); //26
		input2 += "bla[j] = 6;" + System.lineSeparator();                             //27
		input2 += "}" + System.lineSeparator();                                       //28
		input2 += "*(pointer+1) = bla[i];" + System.lineSeparator();                  //29
		input2 += "}" + System.lineSeparator();                                       //30
		input2 += "for(int i=0; i<20; i++){" + System.lineSeparator();				  //31
		input2 += "toReturn++;" + System.lineSeparator();							  //32
		input2 += "}" + System.lineSeparator();										  //33

		input2 += "for(int i=0; i < 10; i++){" + System.lineSeparator();              //34
		input2 += "int bla[20]; //Not initialised" + System.lineSeparator();          //35
		input2 += "for(int j=0; j < 20; j++){" + System.lineSeparator();              //36
		input2 += "toReturn = add(plusOne(bla[j]), input);" + System.lineSeparator(); //37
		input2 += "bla[j] = 6;" + System.lineSeparator();                             //38
		input2 += "}" + System.lineSeparator();                                       //39
		input2 += "}" + System.lineSeparator();										  //40
		input2 += "for(int i=0; i < 10; i++){" + System.lineSeparator();              //41
		input2 += "int bla[20]; //Not initialised" + System.lineSeparator();          //42
		input2 += "for(int j=0; j < 20; j++){" + System.lineSeparator();              //43
		input2 += "toReturn = add(plusOne(bla[j]), input);" + System.lineSeparator(); //44
		input2 += "bla[j] = 6;" + System.lineSeparator();                             //45
		input2 += "}" + System.lineSeparator();                                       //46
		input2 += "*(pointer+1) = bla[i];" + System.lineSeparator();                  //47
		input2 += "}" + System.lineSeparator();                                       //48
		input2 += "for(int i=0; i<20; i++){" + System.lineSeparator();				  //49
		input2 += "toReturn++;" + System.lineSeparator();							  //50
		input2 += "}" + System.lineSeparator();										  //51

		input2 += "for(int i=0; i < 10; i++){" + System.lineSeparator();              //52
		input2 += "int bla[20]; //Not initialised" + System.lineSeparator();          //53
		input2 += "for(int j=0; j < 20; j++){" + System.lineSeparator();              //54
		input2 += "toReturn = add(plusOne(bla[j]), input);" + System.lineSeparator(); //55
		input2 += "bla[j] = 6;" + System.lineSeparator();                             //56
		input2 += "}" + System.lineSeparator();                                       //57
		input2 += "}" + System.lineSeparator();										  //58
		input2 += "for(int i=0; i < 10; i++){" + System.lineSeparator();              //59
		input2 += "int bla[20]; //Not initialised" + System.lineSeparator();          //60
		input2 += "for(int j=0; j < 20; j++){" + System.lineSeparator();              //61
		input2 += "toReturn = add(plusOne(bla[j]), input);" + System.lineSeparator(); //62
		input2 += "bla[j] = 6;" + System.lineSeparator();                             //63
		input2 += "}" + System.lineSeparator();                                       //64
		input2 += "*(pointer+1) = bla[i];" + System.lineSeparator();                  //65
		input2 += "}" + System.lineSeparator();                                       //66
		input2 += "for(int i=0; i<20; i++){" + System.lineSeparator();				  //67
		input2 += "toReturn++;" + System.lineSeparator();							  //68
		input2 += "}" + System.lineSeparator();										  //69

		input2 += "for(int i=0; i < 10; i++){" + System.lineSeparator();              //70
		input2 += "int bla[20]; //Not initialised" + System.lineSeparator();          //71
		input2 += "for(int j=0; j < 20; j++){" + System.lineSeparator();              //72
		input2 += "toReturn = add(plusOne(bla[j]), input);" + System.lineSeparator(); //73
		input2 += "bla[j] = 6;" + System.lineSeparator();                             //74
		input2 += "}" + System.lineSeparator();                                       //75
		input2 += "}" + System.lineSeparator();										  //76
		input2 += "for(int i=0; i < 10; i++){" + System.lineSeparator();              //77
		input2 += "int bla[20]; //Not initialised" + System.lineSeparator();          //78
		input2 += "for(int j=0; j < 20; j++){" + System.lineSeparator();              //79
		input2 += "toReturn = add(plusOne(bla[j]), input);" + System.lineSeparator(); //80
		input2 += "bla[j] = 6;" + System.lineSeparator();                             //81
		input2 += "}" + System.lineSeparator();                                       //82
		input2 += "*(pointer+1) = bla[i];" + System.lineSeparator();                  //83
		input2 += "}" + System.lineSeparator();                                       //84
		input2 += "for(int i=0; i<20; i++){" + System.lineSeparator();				  //85
		input2 += "toReturn++;" + System.lineSeparator();							  //86
		input2 += "}" + System.lineSeparator();										  //87

		input2 += "return toReturn * global_1;" + System.lineSeparator();             //88
		input2 += "}";       						                                  //89

		ToolUtils t = new ToolUtils(new HashSet<String>(), LANGUAGE.CPP, LOOP_ANALYSER, DATA_INSERTION_FINDER,
			DATA_DIRECTIVE_ANALYSER, true);

		File f = File.createTempFile("test", ".tmp");
		try( PrintWriter out = new PrintWriter(f) ){
			out.println( input2 );
			out.close();
		}


		Map<String, VARIABLE_STATUS> m = new HashMap<String, VARIABLE_STATUS>();
		m.put("input", VARIABLE_STATUS.COPYIN);
		m.put("pointer", VARIABLE_STATUS.COPY);
		m.put("toReturn", VARIABLE_STATUS.COPY);
		Map<String, VARIABLE_STATUS> m2 = new HashMap<String, VARIABLE_STATUS>();
		m2.put("toReturn", VARIABLE_STATUS.COPY);
		m2.put("input", VARIABLE_STATUS.COPYIN);
		Map<String, VARIABLE_STATUS> m3 = new HashMap<String, VARIABLE_STATUS>();
		m3.put("toReturn", VARIABLE_STATUS.COPY);

		Patch p = new Patch(new ArrayList<ADirective>(), LANGUAGE.CPP);
		p.addADirective(new OpenACCParallelLoopDirective(16,f, m,
			new HashMap<String, Map.Entry<String, String>>(),  Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(23,f, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(31,f, m3,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));

		p.addADirective(new OpenACCParallelLoopDirective(34,f, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(41,f, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(49,f, m3,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));

		p.addADirective(new OpenACCParallelLoopDirective(52,f, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(59,f, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(67,f, m3,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));

		p.addADirective(new OpenACCParallelLoopDirective(70,f, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(77,f, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));
		p.addADirective(new OpenACCParallelLoopDirective(85,f, m3,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty()));

		DataOptimiserEvolutionary optimiser = new DataOptimiserEvolutionary(
			new MaxDirectivesFitnessFunction(), 20, 5, new Random(4),Optional.empty(), t);
		optimiser.optimise(p);
	}

}
