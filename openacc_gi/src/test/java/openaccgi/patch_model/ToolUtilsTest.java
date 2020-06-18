package openaccgi.patch_model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static org.junit.Assert.*;

public class ToolUtilsTest {
	
	/*
	 * It should be noted that that there may be some redundancy here. The tools have
	 * their own test-cases. However, there is no harm in having more tests.
	 */
	
	private static final File LOOP_ANALYSER = new File("../clang_tools/llvm-build/bin/loop-analyser");
	private static final File DATA_INSERTION_FINDER = new File("../clang_tools/llvm-build/bin/data-insertion-finder");
	private static final File DATA_DIRECTIVE_ANALYSER = new File("../clang_tools/llvm-build/bin/data-directive-analyser");
	
	File f;
	File f2;
	File f3;
	File f3Header;
	
	@Before
	public void setup() throws IOException{
		String input = "int global_1 = 0;" + System.lineSeparator();                 //1
	    input += "extern int global_2;" + System.lineSeparator();                    //2
	    input += "int add(int a, int b){" + System.lineSeparator();                  //3
	    input += "return a + b;" + System.lineSeparator();                           //4
	    input += "}" + System.lineSeparator();                                       //5
	    input += "int plusOne(int a){" + System.lineSeparator();                     //6
	    input += "return a + 1;" + System.lineSeparator();                           //7
	    input += "}" + System.lineSeparator();                                       //8
	    input += "int function_1(int input){" + System.lineSeparator();              //9
	    input += "int* pointer;" + System.lineSeparator();                           //10
	    input += "int toReturn = 0;" + System.lineSeparator();                       //11
	    input += "if(true){" + System.lineSeparator();                               //12
	    input += "int a = 0;" + System.lineSeparator();                              //13
	    input += "toReturn++;" + System.lineSeparator();                             //14
	    input += "}" + System.lineSeparator();                                       //15
	    input += "for(int i=0; i < 10; i++){" + System.lineSeparator();              //16
	    input += "int bla[20]; //Not initialised" + System.lineSeparator();          //17
	    input += "for(int j=0; j < 20; j++){" + System.lineSeparator();              //18
	    input += "toReturn = add(plusOne(bla[j]), input);" + System.lineSeparator(); //19
	    input += "bla[j] = 6;" + System.lineSeparator();                             //20
	    input += "}" + System.lineSeparator();                                       //21
	    input += "}" + System.lineSeparator();										 //22
	    input += "for(int i=0; i < 10; i++){" + System.lineSeparator();              //23
	    input += "int bla[20]; //Not initialised" + System.lineSeparator();          //24
	    input += "for(int j=0; j < 20; j++){" + System.lineSeparator();              //25
	    input += "toReturn = add(plusOne(bla[j]), input);" + System.lineSeparator(); //26
	    input += "bla[j] = 6;" + System.lineSeparator();                             //27
	    input += "}" + System.lineSeparator();                                       //28
	    input += "*(pointer+1) = bla[i];" + System.lineSeparator();                  //29
	    input += "}" + System.lineSeparator();                                       //30
	    input += "return toReturn * global_1;" + System.lineSeparator();             //31
	    input += "}";       						                                 //32

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
		input2 += "return toReturn * global_1;" + System.lineSeparator();             //34
		input2 += "}";																  //35

		String input3Header = "int iArray[506];";


		f3Header = File.createTempFile("test",".tmp");
		try(  PrintWriter out = new PrintWriter(f3Header)  ){
			out.println( input3Header );
			out.close();
		}

		String input3 = "#include <" + f3Header.getName() + ">" + System.lineSeparator();	//1
		input3 += "int global_1 = 0;" + System.lineSeparator();						  		//2
		input3 += "extern int global_2;" + System.lineSeparator();                    		//3
		input3 += "int add(int a, int b){" + System.lineSeparator();                  		//4
		input3 += "return a + b;" + System.lineSeparator();                           		//5
		input3 += "}" + System.lineSeparator();                                       		//6
		input3 += "int plusOne(int a){" + System.lineSeparator();                     		//7
		input3 += "return a + 1;" + System.lineSeparator();                           		//8
		input3 += "}" + System.lineSeparator();                                       		//9
		input3 += "int function_1(int input){" + System.lineSeparator();              		//10
		input3 += "int* pointer;" + System.lineSeparator();                           		//11
		input3 += "int toReturn = 0;" + System.lineSeparator();                       		//12
		input3 += "if(true){" + System.lineSeparator();                               		//13
		input3 += "int a = 0;" + System.lineSeparator();                              		//14
		input3 += "toReturn++;" + System.lineSeparator();                             		//15
		input3 += "}" + System.lineSeparator();                                       		//16
		input3 += "for(int i=0; i < 10; i++){" + System.lineSeparator();              		//17
		input3 += "iArray[i] = 1;" + System.lineSeparator();          				  		//18
		input3 += "for(int j=0; j < 20; j++){" + System.lineSeparator();              		//19
		input3 += "toReturn = add(plusOne(iArray[j]), input);" + System.lineSeparator(); 	//20
		input3 += "}" + System.lineSeparator();                                       		//21
		input3 += "}" + System.lineSeparator();										  		//22
		input3 += "for(int i=0; i < 10; i++){" + System.lineSeparator();              		//23
		input3 += "iArray[i] *= 2;" + System.lineSeparator();          				  		//24
		input3 += "for(int j=0; j < 20; j++){" + System.lineSeparator();              		//25
		input3 += "toReturn = add(plusOne(iArray[j]), input);" + System.lineSeparator(); 	//26
		input3 += "}" + System.lineSeparator();                                       		//27
		input3 += "*(pointer+1) = iArray[i];" + System.lineSeparator();                  	//28
		input3 += "}" + System.lineSeparator();                                       		//29
		input3 += "for(int i=0; i<20; i++){" + System.lineSeparator();				  		//30
		input3 += "toReturn++;" + System.lineSeparator();							  		//31
		input3 += "}" + System.lineSeparator();										  		//32
		input3 += "return toReturn * global_1;" + System.lineSeparator();             		//33
		input3 += "}";																  		//34
	    
	    
	   f = File.createTempFile("test", ".tmp");
	    try(  PrintWriter out = new PrintWriter(f)  ){
	        out.println( input );
	        out.close();
	    }

	    f2 = File.createTempFile("test", ".tmp");
	    try ( PrintWriter out = new PrintWriter (f2)){
	    	out.println(input2);
	    	out.close();
		}

		f3 = File.createTempFile("test", ".tmp");
		try ( PrintWriter out = new PrintWriter (f3)){
			out.println(input3);
			out.close();
		}
	}

	@Test
	public void getDataInfoForScopeTest() throws IOException {
	    ToolUtils t = new ToolUtils(new HashSet<String>(), LANGUAGE.CPP, LOOP_ANALYSER, DATA_INSERTION_FINDER,
		    DATA_DIRECTIVE_ANALYSER, true);
	    Map<String, VARIABLE_STATUS> output = t.getDataInfoForScope(f, 16);
	    
	    /* loop-analyser output:
	    16,23,input,COPY_IN
	    16,23,pointer,COPY
	    16,23,toReturn,COPYOUT*/
	    
	    assertTrue(output.containsKey("input"));
	    assertEquals(VARIABLE_STATUS.COPY, output.get("input"));
	    assertTrue(output.containsKey("toReturn"));
	    assertEquals(VARIABLE_STATUS.COPYOUT, output.get("toReturn"));
	}

	@Test
	public void getDataInfoForScopeTest_withIncludes() throws IOException {
		Set<String> incs = new HashSet<String>();
		incs.add(f3Header.getParent());
		ToolUtils t = new ToolUtils(incs, LANGUAGE.CPP, LOOP_ANALYSER, DATA_INSERTION_FINDER,
			DATA_DIRECTIVE_ANALYSER, true);
		Map<String, VARIABLE_STATUS> output = t.getDataInfoForScope(f3,  17);
	}
	
	@Test
	public void getDataDirectiveInsertionPointsTest()throws IOException{
		ToolUtils t = new ToolUtils(new HashSet<String>(), LANGUAGE.CPP,LOOP_ANALYSER, DATA_INSERTION_FINDER,
			DATA_DIRECTIVE_ANALYSER, true);
		
		ADirective[] presentDirectives = new ADirective[2];
		Map<String, VARIABLE_STATUS> m = new HashMap<String, VARIABLE_STATUS>();
		m.put("input", VARIABLE_STATUS.COPYIN);
		m.put("pointer", VARIABLE_STATUS.COPY);
		m.put("toReturn", VARIABLE_STATUS.COPY);
		presentDirectives[0] = new OpenACCParallelLoopDirective(16,f, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		presentDirectives[1] = new OpenACCParallelLoopDirective(23,f, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		SortedSet<DataDirectiveInsertionPoint> output = t.getDataDirectiveInsertionPoints(f, presentDirectives);

		for(int i=0; i<50; i++){ //Just want to make sure the outputs are consistent across different runs
			SortedSet<DataDirectiveInsertionPoint> output2 = t.getDataDirectiveInsertionPoints(f, presentDirectives);
			assertEquals(output, output2);
		}

		/* data-insertion-finder output:
			16,31
		 */

		assertEquals(1, output.size());
		assertTrue(output.contains(new DataDirectiveInsertionPoint(f, 16,31)));
	}

    @Test
    public void getDataDirectiveInsertionPointsTest_OneDirectiveInactive()throws IOException{
        ToolUtils t = new ToolUtils(new HashSet<String>(), LANGUAGE.CPP,LOOP_ANALYSER, DATA_INSERTION_FINDER,
	        DATA_DIRECTIVE_ANALYSER, true);

        ADirective[] presentDirectives = new ADirective[2];
        Map<String, VARIABLE_STATUS> m = new HashMap<String, VARIABLE_STATUS>();
        m.put("input", VARIABLE_STATUS.COPYIN);
        m.put("pointer", VARIABLE_STATUS.COPY);
        m.put("toReturn", VARIABLE_STATUS.COPY);
        presentDirectives[0] = new OpenACCParallelLoopDirective(16,f, m,
	        new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
        presentDirectives[0].setActive(false);
        presentDirectives[1] = new OpenACCParallelLoopDirective(23,f, m,
	        new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
        SortedSet<DataDirectiveInsertionPoint> output = t.getDataDirectiveInsertionPoints(f, presentDirectives);

        assertEquals(0, output.size());
    }

	@Test
	public void getDataDirectiveInsertionPointsTest_WithDataDirective() throws IOException{
		ToolUtils t = new ToolUtils(new HashSet<String>(), LANGUAGE.CPP,LOOP_ANALYSER, DATA_INSERTION_FINDER,
			DATA_DIRECTIVE_ANALYSER, true);

		ADirective[] presentDirectives = new ADirective[4];
		Map<String, VARIABLE_STATUS> m = new HashMap<String, VARIABLE_STATUS>();
		m.put("input", VARIABLE_STATUS.COPYIN);
		m.put("pointer", VARIABLE_STATUS.COPY);
		m.put("toReturn", VARIABLE_STATUS.COPY);
		Map<String, VARIABLE_STATUS> m2 = new HashMap<String, VARIABLE_STATUS>();
		m2.put("toReturn", VARIABLE_STATUS.COPY);
		m2.put("input", VARIABLE_STATUS.COPYIN);
		Map<String, VARIABLE_STATUS> m3 = new HashMap<String, VARIABLE_STATUS>();
		m3.put("toReturn", VARIABLE_STATUS.COPY);
		presentDirectives[0] = new OpenACCDataDirective(16, 31, f2,m2,
			new HashMap<String, Map.Entry<String, String>>(), new HashSet<OpenACCUpdateDirective>());
		presentDirectives[1] = new OpenACCParallelLoopDirective(16,f2, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		presentDirectives[2] = new OpenACCParallelLoopDirective(23,f2, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		presentDirectives[3] = new OpenACCParallelLoopDirective(31,f2, m3,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		SortedSet<DataDirectiveInsertionPoint> output = t.getDataDirectiveInsertionPoints(f2, presentDirectives);

		/* data-insertion-finder output:
			16,34
		 */

		assertEquals(1, output.size());
		assertTrue(output.contains(new DataDirectiveInsertionPoint(f2, 16,34)));
	}

	@Test
	public void getDataDirectiveTest_basic1()throws IOException{
		ToolUtils t = new ToolUtils(new HashSet<String>(), LANGUAGE.CPP,LOOP_ANALYSER, DATA_INSERTION_FINDER,
			DATA_DIRECTIVE_ANALYSER, true);
		int startLine = 16;
		int endLine = 34;
		ADirective[] presentDirectives = new ADirective[3];
		Map<String, VARIABLE_STATUS> m = new HashMap<String, VARIABLE_STATUS>();
		m.put("input", VARIABLE_STATUS.COPYIN);
		m.put("pointer", VARIABLE_STATUS.COPY);
		m.put("toReturn", VARIABLE_STATUS.COPY);
		Map<String, VARIABLE_STATUS> m2 = new HashMap<String, VARIABLE_STATUS>();
		m2.put("toReturn", VARIABLE_STATUS.COPY);
		Map<String, Map.Entry<String, String>> mRanges = new HashMap<String, Map.Entry<String, String>>();
		mRanges.put("toReturn", new HashMap.SimpleEntry<String, String>("1","3"));
		mRanges.put("input", new HashMap.SimpleEntry<String, String>("0","100"));
		mRanges.put("pointer", new HashMap.SimpleEntry<String, String>("10","99"));
		Map<String, Map.Entry<String, String>> mmRanges = new HashMap<String, Map.Entry<String, String>>();
		mmRanges.put("toReturn", new HashMap.SimpleEntry<String, String>("0","2"));
		mmRanges.put("input", new HashMap.SimpleEntry<String, String>("50","1000"));
		mmRanges.put("pointer", new HashMap.SimpleEntry<String, String>("",""));
		Map<String, Map.Entry<String, String>> m2Ranges = new HashMap<String, Map.Entry<String, String>>();
		m2Ranges.put("toReturn", new HashMap.SimpleEntry<String,String>("0","1"));
		presentDirectives[0] = new OpenACCParallelLoopDirective(16,f2, m, mRanges,
			Optional.empty(), Optional.empty(), Optional.empty());
		presentDirectives[1] = new OpenACCParallelLoopDirective(23,f2, m, mmRanges,
			Optional.empty(), Optional.empty(), Optional.empty());
		presentDirectives[2] = new OpenACCParallelLoopDirective(31,f2, m2, m2Ranges,
			Optional.empty(), Optional.empty(), Optional.empty());
		Optional<OpenACCDataDirective> output = t.getDataDirective(f2, startLine, endLine, presentDirectives);

		/* data-directive-analyser output:
		 	16,34,COPY,toReturn,0,3
		 	16,34,COPY_IN,input,0,1000
		 	16,34,COPY,pointer,,

		 */

		assertTrue(output.isPresent());

		assertEquals(16, output.get().getStartLineNumber());
		assertEquals(34, output.get().getEndLineNumber());
		assertEquals(3, output.get().getVariables().size());
		assertTrue(output.get().getVariables().containsKey("toReturn"));
		assertEquals(VARIABLE_STATUS.COPY, output.get().getVariables().get("toReturn"));
		assertTrue(output.get().getVariables().containsKey("input"));
		assertEquals(VARIABLE_STATUS.COPYIN, output.get().getVariables().get("input"));
		assertTrue(output.get().getVariables().containsKey("pointer"));
		assertEquals(VARIABLE_STATUS.COPYIN, output.get().getVariables().get("pointer"));
		assertEquals(2, output.get().getVariableRanges().size());
		assertTrue(output.get().getVariableRanges().containsKey("toReturn"));
		assertEquals("0",output.get().getVariableRanges().get("toReturn").getKey());
		assertEquals("3", output.get().getVariableRanges().get("toReturn").getValue());
		assertTrue(output.get().getVariableRanges().containsKey("input"));
		assertEquals("0", output.get().getVariableRanges().get("input").getKey());
		assertEquals("1000", output.get().getVariableRanges().get("input").getValue());
	}

	@Test
	public void getDataDirectiveTest_basic2()throws IOException{
		ToolUtils t = new ToolUtils(new HashSet<String>(), LANGUAGE.CPP,LOOP_ANALYSER, DATA_INSERTION_FINDER,
			DATA_DIRECTIVE_ANALYSER, true);
		int startLine = 16;
		int endLine = 34;
		ADirective[] presentDirectives = new ADirective[3];
		Map<String, VARIABLE_STATUS> m = new HashMap<String, VARIABLE_STATUS>();
		m.put("input", VARIABLE_STATUS.COPYIN);
		m.put("pointer", VARIABLE_STATUS.COPY);
		m.put("toReturn", VARIABLE_STATUS.COPY);
		Map<String, VARIABLE_STATUS> m2 = new HashMap<String, VARIABLE_STATUS>();
		m2.put("toReturn", VARIABLE_STATUS.COPY);
		Map<String, Map.Entry<String, String>> mRanges = new HashMap<String, Map.Entry<String, String>>();
		mRanges.put("toReturn", new HashMap.SimpleEntry<String, String>("0","1"));
		mRanges.put("input", new HashMap.SimpleEntry<String, String>("0","1000"));
		Map<String, Map.Entry<String, String>> m2Ranges = new HashMap<String, Map.Entry<String, String>>();
		m2Ranges.put("toReturn", new HashMap.SimpleEntry<String, String>("0","10"));
		presentDirectives[0] = new OpenACCParallelLoopDirective(16,f2, m, mRanges, Optional.empty(),
			Optional.empty(), Optional.empty());
		presentDirectives[1] = new OpenACCParallelLoopDirective(23,f2, m2, m2Ranges, Optional.empty(),
			Optional.empty(), Optional.empty());
		presentDirectives[2] = new OpenACCParallelLoopDirective(31,f2, m2, m2Ranges, Optional.empty(),
			Optional.empty(), Optional.empty());
		Optional<OpenACCDataDirective> output = t.getDataDirective(f2, startLine, endLine, presentDirectives);

		/* data-directive-analyser output:
		 	16,34,COPY,toReturn,0,10
		 */

		assertTrue(output.isPresent());

		assertEquals(16, output.get().getStartLineNumber());
		assertEquals(34, output.get().getEndLineNumber());
		assertEquals(1, output.get().getVariables().size());
		assertTrue(output.get().getVariables().containsKey("toReturn"));
		assertEquals(VARIABLE_STATUS.COPY, output.get().getVariables().get("toReturn"));
		assertEquals(1, output.get().getVariableRanges().size());
		assertTrue(output.get().getVariableRanges().containsKey("toReturn"));
		assertEquals("0", output.get().getVariableRanges().get("toReturn").getKey());
		assertEquals("10", output.get().getVariableRanges().get("toReturn").getValue());
	}

	@Test
	public void getDataDirectiveTest_NoInnerDirectives()throws IOException{
		/*
		In this test, the new data directive insert location is a an
		 */
		ToolUtils t = new ToolUtils(new HashSet<String>(), LANGUAGE.CPP,LOOP_ANALYSER, DATA_INSERTION_FINDER,
			DATA_DIRECTIVE_ANALYSER, true);
		int startLine = 23;
		int endLine = 31;
		ADirective[] presentDirectives = new ADirective[1];
		Map<String, VARIABLE_STATUS> m = new HashMap<String, VARIABLE_STATUS>();
		m.put("input", VARIABLE_STATUS.COPYIN);
		m.put("pointer", VARIABLE_STATUS.COPY);
		m.put("toReturn", VARIABLE_STATUS.COPY);
		presentDirectives[0] = new OpenACCParallelLoopDirective(16,f, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		Optional<OpenACCDataDirective> output = t.getDataDirective(f, startLine, endLine, presentDirectives);

		for(int i=0; i < 50; i++){ //Just want to make sure the outputs are consistent across different runs
			Optional<OpenACCDataDirective> output2 = t.getDataDirective(f, startLine, endLine, presentDirectives);
			assertEquals(output, output2);
		}

		/*
		data-directive-analyser output should be empty as the region specified (23 to 31), does not contain any
		directives
		 */

		assertFalse(output.isPresent());

	}



	@Test
	public void getDataDirectiveTest_updateDirectives()throws IOException{
		ToolUtils t = new ToolUtils(new HashSet<String>(), LANGUAGE.CPP,LOOP_ANALYSER, DATA_INSERTION_FINDER,
			DATA_DIRECTIVE_ANALYSER, true);
		int startLine = 16;
		int endLine = 34;
		ADirective[] presentDirectives = new ADirective[2];
		Map<String, VARIABLE_STATUS> m = new HashMap<String, VARIABLE_STATUS>();
		m.put("input", VARIABLE_STATUS.COPYIN);
		m.put("pointer", VARIABLE_STATUS.COPY);
		m.put("toReturn", VARIABLE_STATUS.COPYOUT);
		presentDirectives[0] = new OpenACCParallelLoopDirective(16,f2, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		Map<String, VARIABLE_STATUS> m2 = new HashMap<String, VARIABLE_STATUS>();
		m2.put("toReturn", VARIABLE_STATUS.COPY);
		presentDirectives[1] = new OpenACCParallelLoopDirective(31,f2,m2,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		Optional<OpenACCDataDirective> output = t.getDataDirective(f2, startLine, endLine, presentDirectives);

		assertTrue(output.isPresent());
		assertEquals(1,output.get().getUpdateDirectives().size());

		OpenACCUpdateDirective updateDir = output.get().getUpdateDirectives().iterator().next();

		assertEquals(27, updateDir.getLineNumber());
		assertEquals(1, updateDir.getVariables().size());
		assertTrue(updateDir.getVariables().containsKey("toReturn"));
		Assert.assertEquals(OpenACCUpdateDirective.UPDATE_TYPE.DEVICE, updateDir.getVariables().get("toReturn"));
	}

	@Test
	public void getDataDirectiveTest_WithDataDirective()throws IOException{
		ToolUtils t = new ToolUtils(new HashSet<String>(), LANGUAGE.CPP,LOOP_ANALYSER, DATA_INSERTION_FINDER,
			DATA_DIRECTIVE_ANALYSER, true);
		int startLine = 16;
		int endLine = 34;
		ADirective[] presentDirectives = new ADirective[4];
		Map<String, VARIABLE_STATUS> m = new HashMap<String, VARIABLE_STATUS>();
		m.put("input", VARIABLE_STATUS.COPYIN);
		m.put("pointer", VARIABLE_STATUS.COPY);
		m.put("toReturn", VARIABLE_STATUS.COPY);
		Map<String, VARIABLE_STATUS> m2 = new HashMap<String, VARIABLE_STATUS>();
		m2.put("toReturn", VARIABLE_STATUS.COPY);
		m2.put("input", VARIABLE_STATUS.COPYIN);
		Map<String, VARIABLE_STATUS> m3 = new HashMap<String, VARIABLE_STATUS>();
		m3.put("toReturn", VARIABLE_STATUS.COPY);

		Map<String, Map.Entry<String, String>> m2ranges = new HashMap<String, Map.Entry<String, String>>();
		m2ranges.put("toReturn", new HashMap.SimpleEntry<String, String>("0","1"));
		m2ranges.put("input", new HashMap.SimpleEntry<String, String>("0","1000"));
		Map<String, Map.Entry<String, String>> m3Ranges = new HashMap<String, Map.Entry<String, String>>();
		m3Ranges.put("toReturn", new HashMap.SimpleEntry<String, String>("0", "10"));
		presentDirectives[0] = new OpenACCDataDirective(16, 31, f2,m2, m2ranges,
			new HashSet<OpenACCUpdateDirective>());
		presentDirectives[1] = new OpenACCParallelLoopDirective(16,f2, m, m2ranges, Optional.empty(),
			Optional.empty(), Optional.empty());
		presentDirectives[2] = new OpenACCParallelLoopDirective(23,f2, m, m2ranges, Optional.empty(),
			Optional.empty(), Optional.empty());
		presentDirectives[3] = new OpenACCParallelLoopDirective(31,f2, m3, m3Ranges, Optional.empty(),
			Optional.empty(), Optional.empty());
		Optional<OpenACCDataDirective> output = t.getDataDirective(f2, startLine, endLine, presentDirectives);


		/* data-directive-analyser output:
		 	16,34,COPY,toReturn,0,10

		 */

		assertTrue(output.isPresent());

		assertEquals(16, output.get().getStartLineNumber());
		assertEquals(34, output.get().getEndLineNumber());
		assertEquals(1, output.get().getVariables().size());
		assertTrue(output.get().getVariables().containsKey("toReturn"));
		assertEquals(VARIABLE_STATUS.COPY, output.get().getVariables().get("toReturn"));
		assertEquals(1, output.get().getVariableRanges().size());
		assertTrue(output.get().getVariableRanges().containsKey("toReturn"));
		assertEquals("0", output.get().getVariableRanges().get("toReturn").getKey());
		assertEquals("10", output.get().getVariableRanges().get("toReturn").getValue());
	}

	@Test
	public void getDataDirectiveTest_WithDataDirective_noVarRanges()throws IOException{
		ToolUtils t = new ToolUtils(new HashSet<String>(), LANGUAGE.CPP,LOOP_ANALYSER, DATA_INSERTION_FINDER,
			DATA_DIRECTIVE_ANALYSER, false);
		int startLine = 16;
		int endLine = 34;
		ADirective[] presentDirectives = new ADirective[4];
		Map<String, VARIABLE_STATUS> m = new HashMap<String, VARIABLE_STATUS>();
		m.put("input", VARIABLE_STATUS.COPYIN);
		m.put("pointer", VARIABLE_STATUS.COPY);
		m.put("toReturn", VARIABLE_STATUS.COPY);
		Map<String, VARIABLE_STATUS> m2 = new HashMap<String, VARIABLE_STATUS>();
		m2.put("toReturn", VARIABLE_STATUS.COPY);
		m2.put("input", VARIABLE_STATUS.COPYIN);
		Map<String, VARIABLE_STATUS> m3 = new HashMap<String, VARIABLE_STATUS>();
		m3.put("toReturn", VARIABLE_STATUS.COPY);

		Map<String, Map.Entry<String, String>> m2ranges = new HashMap<String, Map.Entry<String, String>>();
		m2ranges.put("toReturn", new HashMap.SimpleEntry<String, String>("0","1"));
		m2ranges.put("input", new HashMap.SimpleEntry<String, String>("0","1000"));
		Map<String, Map.Entry<String, String>> m3Ranges = new HashMap<String, Map.Entry<String, String>>();
		m3Ranges.put("toReturn", new HashMap.SimpleEntry<String, String>("0", "10"));
		presentDirectives[0] = new OpenACCDataDirective(16, 31, f2,m2, m2ranges,
			new HashSet<OpenACCUpdateDirective>());
		presentDirectives[1] = new OpenACCParallelLoopDirective(16,f2, m, m2ranges, Optional.empty(),
			Optional.empty(), Optional.empty());
		presentDirectives[2] = new OpenACCParallelLoopDirective(23,f2, m, m2ranges, Optional.empty(),
			Optional.empty(), Optional.empty());
		presentDirectives[3] = new OpenACCParallelLoopDirective(31,f2, m3, m3Ranges, Optional.empty(),
			Optional.empty(), Optional.empty());
		Optional<OpenACCDataDirective> output = t.getDataDirective(f2, startLine, endLine, presentDirectives);


		/* data-directive-analyser output:
		 	16,34,COPY,toReturn,0,10

		 */

		assertTrue(output.isPresent());

		assertEquals(16, output.get().getStartLineNumber());
		assertEquals(34, output.get().getEndLineNumber());
		assertEquals(1, output.get().getVariables().size());
		assertTrue(output.get().getVariables().containsKey("toReturn"));
		assertEquals(VARIABLE_STATUS.COPY, output.get().getVariables().get("toReturn"));
		assertTrue(output.get().getVariableRanges().isEmpty());

	}

    @Test
    public void getDataDirectiveTest_DirectivesInactive()throws IOException{
        ToolUtils t = new ToolUtils(new HashSet<String>(), LANGUAGE.CPP,LOOP_ANALYSER, DATA_INSERTION_FINDER,
	        DATA_DIRECTIVE_ANALYSER, true);
        int startLine = 16;
        int endLine = 34;
        ADirective[] presentDirectives = new ADirective[4];
        Map<String, VARIABLE_STATUS> m = new HashMap<String, VARIABLE_STATUS>();
        m.put("input", VARIABLE_STATUS.COPYIN);
        m.put("pointer", VARIABLE_STATUS.COPY);
        m.put("toReturn", VARIABLE_STATUS.COPY);
        Map<String, VARIABLE_STATUS> m2 = new HashMap<String, VARIABLE_STATUS>();
        m2.put("toReturn", VARIABLE_STATUS.COPY);
        m2.put("input", VARIABLE_STATUS.COPYIN);
        Map<String, VARIABLE_STATUS> m3 = new HashMap<String, VARIABLE_STATUS>();
        m3.put("toReturn", VARIABLE_STATUS.COPY);
        presentDirectives[0] = new OpenACCDataDirective(16, 31, f2,m2,
	        new HashMap<String, Map.Entry<String, String>>(), new HashSet<OpenACCUpdateDirective>());
        presentDirectives[0].setActive(false);
        presentDirectives[1] = new OpenACCParallelLoopDirective(16,f2, m,
	        new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
        presentDirectives[1].setActive(false);
        presentDirectives[2] = new OpenACCParallelLoopDirective(23,f2, m,
	        new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
        presentDirectives[2].setActive(false);
        presentDirectives[3] = new OpenACCParallelLoopDirective(31,f2, m3,
	        new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
        presentDirectives[3].setActive(false);
        Optional<OpenACCDataDirective> output = t.getDataDirective(f2, startLine, endLine, presentDirectives);



        assertFalse(output.isPresent());
    }

	@Test
	public void getDataDirectiveTest_allInsertionPointsValid()throws IOException{
		ToolUtils t = new ToolUtils(new HashSet<String>(), LANGUAGE.CPP,LOOP_ANALYSER, DATA_INSERTION_FINDER,
			DATA_DIRECTIVE_ANALYSER, true);

		ADirective[] presentDirectives = new ADirective[4];
		Map<String, VARIABLE_STATUS> m = new HashMap<String, VARIABLE_STATUS>();
		m.put("input", VARIABLE_STATUS.COPYIN);
		m.put("pointer", VARIABLE_STATUS.COPY);
		m.put("toReturn", VARIABLE_STATUS.COPY);
		Map<String, VARIABLE_STATUS> m2 = new HashMap<String, VARIABLE_STATUS>();
		m2.put("toReturn", VARIABLE_STATUS.COPY);
		m2.put("input", VARIABLE_STATUS.COPYIN);
		Map<String, VARIABLE_STATUS> m3 = new HashMap<String, VARIABLE_STATUS>();
		m3.put("toReturn", VARIABLE_STATUS.COPY);
		presentDirectives[0] = new OpenACCDataDirective(16, 31, f2,m2,
			new HashMap<String, Map.Entry<String, String>>(), new HashSet<OpenACCUpdateDirective>());
		presentDirectives[1] = new OpenACCParallelLoopDirective(16,f2, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		presentDirectives[2] = new OpenACCParallelLoopDirective(23,f2, m,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		presentDirectives[3] = new OpenACCParallelLoopDirective(31,f2, m3,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		SortedSet<DataDirectiveInsertionPoint> output = t.getDataDirectiveInsertionPoints(f2, presentDirectives);

		/* data-insertion-finder output:
			12,34,2,3,0
			16,34,2,3,0
		 */

		for(DataDirectiveInsertionPoint d : output){
			Optional<OpenACCDataDirective> dataDirectiveOut = t.getDataDirective(f2, d.getStartLineNumber(),
				d.getEndLineNumber(), presentDirectives);
			assertTrue(dataDirectiveOut.isPresent());
		}
	}

}