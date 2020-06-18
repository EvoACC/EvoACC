package openaccgi.application;

import openaccgi.patch_model.LANGUAGE;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.AccessControlException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


public class ApplicationCommandLineParser {
	private final File csvFile;
	private final File scriptFile;
	private final Integer maxEvaluations;
	private final Optional<Integer> seed;
	private final Set<String> includes = new HashSet<String>();
	private final LANGUAGE language;
	private final File dataInsertionFinderFile;
	private final File dataDirectiveAnalyserFile;
	private final File loopAnalyserFile;
	private final Optional<File> logFile;
	private final boolean silent;
	private final boolean includeDataRanges;
	
	private void verifyTool(File tool) throws AccessControlException, FileNotFoundException{
		if(!tool.isFile()){
			throw new FileNotFoundException("Tool could not be found at specified path: " + tool.getAbsolutePath());
		}
		
		if(!tool.canExecute()){
			throw new AccessControlException("Tool " + tool.getAbsolutePath() + " is not an executable");
		}
	}
	
	public ApplicationCommandLineParser(String[] args) throws AccessControlException, FileNotFoundException,
		ParseException{
		//Special case when 'help' or '-h' is used. Special case, closes program after printing help
		if(args.length == 0 || (args.length==1 && (args[0].equals("-h") || args[0].equals("--help")))){
			ApplicationCommandLineParser.printHelp();
			System.exit(0);
		}
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			 cmd = parser.parse(ApplicationCommandLineParser.getOptions(), args);
		} catch (ParseException e1) {
			throw new ParseException("Could not parse arguments ('-h' for usage info)\nException Information:\n"
				+ e1.getLocalizedMessage());
		}
		assert(cmd != null);
		
		if(cmd.hasOption('h')){
			ApplicationCommandLineParser.printHelp();
			System.exit(0);
		}
		
		// Get seed value if available
		if(cmd.hasOption('s')){
			String value = cmd.getOptionValue('s');
			try{
				seed = Optional.of(Integer.parseInt(value));
			} catch(NumberFormatException e){
				throw new ParseException("Could not parse seed value (s) to Integer\nException Information:\n"
					+ e.getLocalizedMessage());
			}
		} else {
			seed = Optional.empty();
		}
		
		// Get input CSV file
		this.csvFile = new File(cmd.getOptionValue('f'));
			
		if(!this.csvFile.exists()){
			throw new FileNotFoundException("Input CSV file could not be found at specified path: "
				+ this.csvFile.getAbsolutePath());
		}
		
		if(this.csvFile.isDirectory()){
			throw new FileNotFoundException("Input CSV file input is a directory: "+this.csvFile.getAbsolutePath());
		}
		
		if(!this.csvFile.canRead()){
			throw new AccessControlException("Input CSV file "+this.csvFile.getAbsolutePath()+ " cannot be read");
		}
		
		// Get the script file
		this.scriptFile = new File(cmd.getOptionValue('x'));
		
		if(!this.scriptFile.exists()){
			throw new FileNotFoundException("Script file could not be found at specified path: "
				+ this.scriptFile.getAbsolutePath());
		}
		
		if(!this.scriptFile.isFile()){
			throw new FileNotFoundException("Script file is not a file: "+this.scriptFile.getAbsolutePath());
		}
		
		if(!this.scriptFile.canExecute()){
			throw new AccessControlException("Script file " + this.scriptFile.getAbsolutePath() + " is not executable");
		}
		
		//Get the max number of evaluations
		String maxEval = cmd.getOptionValue('e');
		try{
			this.maxEvaluations = Integer.parseInt(maxEval);
		} catch(NumberFormatException e){
			throw new ParseException("Could not parse maximum number of evaluations (-e) argument " +
				"to integer\nException Information:\n" + e.getLocalizedMessage());
		} 
		
		if(this.maxEvaluations<0){
			throw new ParseException("Number of evaluations (-e) must be a positive integer");
		}
		
		if(cmd.hasOption('I')){
			String[] inc = cmd.getOptionValues('I');
			for(String s : inc){
				includes.add(s);
			}
		}
		
		switch(cmd.getOptionValue('l')){
			case "C":
				language = LANGUAGE.C;
				break;
			case "CPP":
				language = LANGUAGE.CPP;
				break;
			default:
				throw new ParseException("Unrecognised language input (" + cmd.getOptionValue('l')
					+ ". Must be either 'C' or 'CPP'");
		}
		
		this.dataInsertionFinderFile = new File(cmd.getOptionValue('d'));
		this.dataDirectiveAnalyserFile = new File(cmd.getOptionValue('D'));
		this.loopAnalyserFile = new File(cmd.getOptionValue('a'));
		
		try{
			verifyTool(this.dataDirectiveAnalyserFile);
			verifyTool(this.dataInsertionFinderFile);
			verifyTool(this.loopAnalyserFile);
		} catch (AccessControlException e){
			throw e;
		} catch (FileNotFoundException e){
			throw e;
		}

		if(cmd.hasOption("L")){
			File logDir = new File(cmd.getOptionValue("L"));
			if(!logDir.exists()){
				logDir.mkdir();
			}

			if(!logDir.isDirectory()){
				throw new FileNotFoundException("'" + logDir.getAbsolutePath() + "' is not a directory!");
			}

			this.logFile = Optional.of(logDir);

		} else {
			this.logFile = Optional.empty();
		}

		this.silent = cmd.hasOption("i");
		this.includeDataRanges = cmd.hasOption("r");
	}
	
	public final static void printHelp(){
		
		HelpFormatter hf = new HelpFormatter();
		final String applicationName = "openacc_gi";
		final String header = "Automatically add and optimise OpenACC directives in C code";
		final String footer = "Please report issues to Bobby R. Bruce (r.bruce@cs.ucl.ac.uk)";
		
		hf.printHelp(applicationName, header, ApplicationCommandLineParser.getOptions(), footer, true);
	}
	
	private final static Options getOptions(){
		Option sourceFilesOption = Option.builder("f")
			.required()
			.longOpt("file")
			.desc("Input CSV file highlighting targeted for loops (in format: <file>,<for_loop_line_no>)")
			.argName("file")
			.hasArg()
			.build();
		
		Option scriptFile = Option.builder("x")
			.desc("Set executable to evaluate the software (must return double value, higher" +
				" is better, <0 is infeasible solution)")
			.longOpt("executable")
			.required()
			.hasArg()
			.argName("executable_file")
			.build();
		
		
		Option evaluations = Option.builder("e")
			.desc("Set the max number of evaluations")
			.longOpt("evaluations")
			.argName("num_evaluations")
			.hasArg()
			.required()
			.build();
		
		Option seed = Option.builder("s")
			.desc("Set the RNG seed")
			.longOpt("seed")
			.argName("seed_value")
			.hasArg()
			.build();
		
		Option help = Option.builder("h")
			.longOpt("help")
			.desc("Print Help")
			.build();

		Option silent = Option.builder("i")
			.longOpt("silent")
			.desc("No output (For testing purposes)")
			.build();
		
		Option includes = Option.builder("I")
			.longOpt("include")
			.argName("the")
			.hasArgs()
			.build();
		
		Option language = Option.builder("l")
			.longOpt("language")
			.desc("Programming Language: Either 'C' or 'CPP'")
			.hasArg()
			.required()
			.build();
		
		Option dataInsertionFinder = Option.builder("d")
			.longOpt("data_insertion_finder")
			.desc("Path of data-insertion-finder")
			.hasArg()
			.required()
			.build();
		
		Option dataDirectiveAnalyser = Option.builder("D")
			.longOpt("data_directive_analyser")
			.desc("Path of data-directive-analyser")
			.hasArg()
			.required()
			.build();
		
		Option loopAnalyser = Option.builder("a")
			.longOpt("loop_analyser")
			.desc("Path of loop-analyser")
			.hasArg()
			.required()
			.build();

		Option logFile = Option.builder("L")
			.longOpt("log_directory")
			.desc("Set the log directory")
			.hasArg()
			.build();

		Option includeDataRanges = Option.builder("r")
			.longOpt("include_data_ranges")
			.desc("Include the data ranges for variables (experimental)")
			.build();
		
		

		Options toReturn = new Options();
		toReturn.addOption(sourceFilesOption);
		toReturn.addOption(scriptFile);
		toReturn.addOption(evaluations);
		toReturn.addOption(language);
		toReturn.addOption(seed);
		toReturn.addOption(includes);
		toReturn.addOption(dataInsertionFinder);
		toReturn.addOption(dataDirectiveAnalyser);
		toReturn.addOption(loopAnalyser);
		toReturn.addOption(logFile);
		toReturn.addOption(includeDataRanges);
		toReturn.addOption(silent);
		toReturn.addOption(help);
		return toReturn;
	}
	
	public File getCsvFile() {
		return csvFile;
	}
	
	public File getScriptFile() {
		return scriptFile;
	}
	
	public Integer getMaxEvaluations() {
		return maxEvaluations;
	}
	
	public Optional<Integer> getSeed() {
		return seed;
	}
	
	public Set<String> getIncludes(){
		return includes;
	}
	
	public LANGUAGE getLanguage(){
		return this.language;
	}
	
	public File getDataInsertionFinder(){
		return this.dataInsertionFinderFile;
	}
	
	public File getDataDirectiveAnalyser(){
		return this.dataDirectiveAnalyserFile;
	}
	
	public File getLoopAnalyser(){
		return this.loopAnalyserFile;
	}

	public Optional<File> getLogFile(){
		return this.logFile;
	}

	public boolean isSilent(){
		return this.silent;
	}

	public boolean isIncludeDataRanges(){
		return this.includeDataRanges;
	}
	
}
