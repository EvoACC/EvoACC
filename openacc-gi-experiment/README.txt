--- Training Cases ---

The training cases for each application were chosen by finding the a testcase that run within 5 to 30 seconds. If none existed a custom test were created (for ease of use we set this as CLASS='F').

BT: we needed to create a custom test. W is too small (2.5 seconds), A is too large (62.5 seconds). A custom has been created (40x40x40 grids after 200 time steps with DT = 0.8e-03)

CG: we needed to create a custom test. A is too small (1.4 seconds), B is too long (78.7 seconds). A custom has been created (na = 30000; nonzer=12; niter=30; shift=20)

EP: CLASS=A, 25.9 seconds

FT: CLASS=A, 7.9 seconds

LU: CLASS=W, 5.5 seconds

MG: CLASS=B, 7.5 seconds

SP: CLASS=W, 7.6 seconds

These training cases can be run by executing '[application]_fitness_function_training.bsh' in the 'setup' directory.

--- Test Cases ---

The test case for each application was chosen by finding the testcase that run in over a minute but no more than 30 minutes. In all cases, such a testcase existed. Where there were multiple within this range, the larger was chosen. It should be noted, that as these test cases will not be run as much as the training, we can afford them taking longer to execute.

BT: CLASS=B, 4 minutes 54 seconds
CG: CLASS=C, 5 minutes 34 seconds
EP: CLASS=C, 7 minutes 14 seconds
FT: CLASS=B, 1 minute 59 seconds
LU: CLASS=C, 16 minutes 19 seconds
MG: CLASS=C, 20 minutes 49 seconds
SP: CLASS=C, 13 minutes 26 seconds

These test cases can be run by executing '[application_fitness_function_test.bsh" in the 'setup' directory.

--- How profiling was undertaken ---

I created a gprof analysis of each application using the test cases mentioned in the previous section. The gprof analysis of each application is stored in info/[application]_gprof_analysis.txt

I highlight below the methods that make up 90% of the execution time. I start from the method which takes the largest execution time and keep adding until >90% has been achieved. I also include the Main method (as this obviously runs for 90% of the execution time).

BT: solve_subs.c:binvcrhs(), y_solve.c:y_solve(), solve_subs.c:matmul_sub(), z_solve.c:z_solve(), x_solve.c:x_solve(), rhs.c:compute_rhs(), bt.c:main()

CG: cg.c:conj_grad(), cg.c:main()

EP: randdp.c:vranlc(), ep.c:main()

FT: fft3d.c:Swarztrauber(), randdp.c:vranlc(), fft3d.c:fftXTZ(), mainft.c:main()

LU: buts.c:buts(), rhs.c:rhs(), jacld.c:jacld(), blts.c:blts(), jacu.c:jacu(), lu.c:main()

MG: mg.c:resid(), mg.c:psinv(), randdp.c:vranlc(), mg.c:rprj3(), mg.c:main()

SP: rhs.c:compute_rhs(), z_solve.c:z_solve(), y_solve.c:y_solve(), x_solve.c:x_solve(), sp.c:main()


'[application]_data_[test/training].csv' in the setup for each application is only for these methods.

NOTE: we have moved common/randdp.c to the application directories as this came up in profiling and we did not wish to alter the same file for multiple applications.

--- Determining the array ranges ---

The '[application]_data_[test/training].csv' in the 'setup' directory has the following schema:

'<file>,<line_number>,<variable>,<range_begin>,<range_end>'

The first two fields relate to the location of a targetted for-loop (the file and the relevant line number). The last three fields relate to recording the ranges of arrays. Our framework does not analyse the range of the arrays to be moved to and from the GPU for any given loop parallelisation. Instead we treat the range analysis as a pre-processing step. This is done using the DawnCC tool (https://github.com/gleisonsdm/DawnCC-Compiler . Also see 'DawnCC: Automatic Annotation for Data Parallelisation and Offloading' by Mendonca et al.). 

DawnCC automatically paralleliss loops (which can be proven safe to parallelise) by adding OpenACC/OpenMP directives and determining, through range analysis, the ranges of the arrays to copy to and from the GPU a given loop (e.g., only x[5] to x[50] to be copied, despite x ranging from x[0] to x[1000] in memory). We have hacked DawnCC to parallelise as much loops as possible (regardless as to whether it is 'safe' to do so; though loops which contains function calls are not permitted for parallelisation in DawnCC despite this hack). We then read the range analysis information from the annotated for loops output by the tool and manually create the '[application]_data_[test/training.csv' files in the 'setup' directory. The tools necessisary to create the annotated code in order to manually create this '[application]_data_[test/training].csv' can be found in 'array_ranges_setup'. To get this data one must execute './dawncc_build.bsh && ./meta_run.bsh' in the 'array_ranges_setup' directory.

At present, only bt_data_training.csv has this range analysis data included. The implementation of the others is a 'todo'. At present 'bt_data_training.csv' serves as a good test for the framework's effectiveness.

Special note: The NAS benchmark suite is setup in an odd way. Different tests are run, not by using different inputs, but by compiling the code with different parameter settings. This means the code's must semantics change for every test. For this reason, the same array range analysis will not be constant across different tests. This is why there are different inputs for training and test runs (i.e., 'bt_data_test.csv' and 'bt_data_training.csv' declare the same for-loops to be parallelised, and the same variables to be moved to and from the GPU, but the range analysis information, '<range_begin>,<range_end>' will differ).
