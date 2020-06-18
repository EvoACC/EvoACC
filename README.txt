--- Setup and running the tool ---

To setup the tool please execute 'setup.bsh'

This will compile the tools in 'clang tools' (used by openacc_gi_v3), and then build the openacc_gi-3.0 jar.
 
You need wget, tar, unzip, cmake, a C++ compilation toolchain, and Maven to use this script or you will encounter errors (they should be noisy enough to fix)

Once complete the tool may be run it using './run.bsh'

'./run.bsh' takes the following arguments:
 ./run.bsh -f <csv_file> -x <fitness_function_script> -e <number_evaluations> -l <language>[-s <seed>] [-I <include_file> ... -I <another_include file>]

<csv_file> : A CSV file that specifies the targeted loops, as well as the pre-processed range analysis information for arrays utilised within for-loop structures. In format of '<file>,<loop_line_number>,<variable>,<range_begin>,<range_end>'. 
<fitness_function_script>: An executable that will return the fitness. Negative fitness is a break of hard constraints, otherwise closer to zero equals better.

<number_evaluations>: The number of evaluations (Due to some stochastic behaviour, this number may be slightly smaller or bigger than the actual evaluations done. Actual evaluations will be printed to STDOUT upon completion of execution.

<language> : The target language. Either 'C' or 'CPP'.

<include_file> : Files that should be included to get these files to compile. Note: C/CPP standard library is not included by default.

--- Running Experiments ---

To run experiments simply execute 'experiment_run.bsh'. This will download the experiment git repo: 'git@bitbucket.org:BobbyBruce1990/experiment.git' (you must request access to this repo). This repo contains its own README for information.

--- Description of OpenACC-GI's functionality and features ---

This tool contains various components. The following is a high-level overview.

When optimising an application, the tool goes four distinct stages:

0) Pre-processing --- Not technically part of the tool, but worthy of note. The <csv_file> must be provided. For the tool to run at its most optimal, this should provide the location of all for-loops that are deemed worthy of optimisation (i.e., determined worthy via profiling), and the range analysis of the arrays to be moved to and from the GPU. Both are available using GPROF (to profile) and the DawnCC compiler (to obtain the range analysis of arrays).

1) Loop Selection --- For each loop specialised in the <csv_file> input, the loop is parallelised using the OpenACC 'parallel loop' directive. The necessary range analysis for each is taken from the input CSV file, and whether a variable is moved to or from the GPU (or both or neither), is determined by a simple analysis implemented in clang. When a loop is parallelised, the <fitness_function_script> (taking in a patch to the program as an argument) is run in order to determine the fitness of the application when that loop is parallelised. If the code does not break the hard constraints (i.e., a value greater than '-1' is returned), then the for loop remains parallelised and the Loop Selection process continues to the next for loop in the CSV file; otherwise, the loop parallelisation is removed before proceeding to the next loop for evaluation. This process ends with, hopefully, a large number of loops being parallelised. In most cases, this will mean a significant slow-down in the code's performance.

2) Data Optimisation --- This stage attempts to find the optimal placement of OpenACC data directives using a genetic algorithm. These directives allow the transfer of data to and from the GPU across many loop parallelisations. The fitness function used is the <fitness_function_script> (taking in a patch to the program as an argument). This fitnesss function will guide the search towards a solution with a lower execution time. 

3) Delta Debugging --- This stage iteratively goes through the directives withinthe best patch found from the 'Data Optimisation' stage. For each, it is removed, and if this improves the performance (as determined by the <fitness_function_script>), it is permanently removed from the final patch. If the patch is worse having removed the directive, the directive is re-added before moving onto the next directive.

After all these stages the optimal patch is returned to the user via STDOUT.
