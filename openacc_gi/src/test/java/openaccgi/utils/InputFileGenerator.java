package openaccgi.utils;

import java.io.*;

public class InputFileGenerator {
	
	private final static File f1 = new File("/tmp/temp0");
	private final static File f2 = new File("/tmp/temp");
	private final static File f3 = new File("/tmp/temp2");
	
	private final static String CSVFileContents_1 =f1.getAbsolutePath() + ",11,,," +  System.lineSeparator() +
			f1.getAbsolutePath() + ",64,x,0,1024" +  System.lineSeparator() +
			f1.getAbsolutePath() + ",64,y,0,1024" +  System.lineSeparator() +
			f1.getAbsolutePath() + ",64,z,0,1024" +  System.lineSeparator() +
			f1.getAbsolutePath() + ",70,x,100,500" +  System.lineSeparator() +
			f1.getAbsolutePath() + ",80,z,," +  System.lineSeparator() +
			f1.getAbsolutePath() + ",80,y,NP,ND" +  System.lineSeparator() +
			f1.getAbsolutePath() + ",112,,," +  System.lineSeparator() +
			f1.getAbsolutePath() + ",118,,," +  System.lineSeparator() +
			f1.getAbsolutePath() + ",128,,," +  System.lineSeparator() +
			f1.getAbsolutePath() + ",292,,," +  System.lineSeparator() +
			f1.getAbsolutePath() + ",300,,,";

	/*
	 * 1: int x=2;
	 * 2: int y=0;
	 * 3: int z=8;
	 * 4: int function() {
	 * 5: 	for(int i=0;i<5;i++){
	 * 6:   if(false){
	 * 7:     int bla = 0;
	 * 8:   }
	 * 9:   if(true){
	 *10:     int bla = x;
	 *11:     y = bla + 5;
	 *12:   }
	 *13:  }
	 *14:  System.println(y);
	 *15: }
	 */
	
	private final static String CSVFileContents_2= f2.getAbsolutePath() + ",5,y,,";
	
	
	//I'm mostly using this as an example I can easily append if needed with corner cases
	/* 
	 * for(int i=0; i<10;i++);
	 * int x[i]=5+8*2;
	 */
	private final static String CSVFileContents_3 = f3.getAbsolutePath() + ",1,x,0,10";
	
	
	public static File getFileReference1(){
		return f1;
	}
	
	public static File getFileReference2(){
		return f2;
	}
	
	public static File getFileReference3(){
		return f3;
	}
	
	//Contains f1, f2, and f3
	public static File getTestCSVFile(){
		File toReturn = null;
		try{
			toReturn = File.createTempFile("csvData", ".tmp");
			BufferedWriter bw = new BufferedWriter(new FileWriter(toReturn));
			bw.write(CSVFileContents_1);
			bw.write(System.lineSeparator());
			bw.write(CSVFileContents_2);
			bw.write(System.lineSeparator());
			bw.write(CSVFileContents_3);
			bw.close();
		} catch (IOException e){
			e.printStackTrace();
			assert(false);
		}
		
		return toReturn;
	}
	
	
	public static File getRealF1(){
		File f = null;
		File realF = null;
		try {
			f = File.createTempFile("csvFile", ".tmp");
			realF = File.createTempFile("realCode", ".tmp");
		
		
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
		    
		    
		    PrintWriter out = new PrintWriter(realF);
		    out.println( input );
		    out.close();
		    
		    String csv = realF.getAbsolutePath() + ",16,input,," + System.lineSeparator();
			csv += realF.getAbsolutePath() + ",16,return,," + System.lineSeparator();
		    csv += realF.getAbsolutePath() + ",18,bla,0,20" + System.lineSeparator();
			csv += realF.getAbsolutePath() + ",18,input,," + System.lineSeparator();
			csv += realF.getAbsolutePath() + ",18,return,," + System.lineSeparator();
		    csv += realF.getAbsolutePath() + ",23,input,," + System.lineSeparator();
			csv += realF.getAbsolutePath() + ",23,return,," + System.lineSeparator();
		    csv += realF.getAbsolutePath() + ",25,bla,0,20" + System.lineSeparator();
			csv += realF.getAbsolutePath() + ",25,input,," + System.lineSeparator();
			csv += realF.getAbsolutePath() + ",25,return,,";
		    
		    PrintWriter out2 = new PrintWriter(f);
		    out2.print(csv);
		    out2.close();
	        
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return f;
		
	}
	
}
