Some manual tests.

--- Tests to Run for loop-analysis: ---

test1.c:

For targetting line 10:

Variable input is COPY_IN
Variable toReturn is COPY

For targetting line 5:

Variable toReturn is COPY

for ep.c:

For targetting line 68:

Variable i is COPY
Variable t1 is COPY
Variable t2 is COPY_OUT

For targetting line 125:

Variable i is COPY
Variable gc is COPY_IN
Variable q is COPY_IN

For targetting line 110:

Variable i is COPY
Variable l is CREATE
Variable q is COPY
Variable sx is COPY_IN
Variable sy is COPY_IN
Variable t1 is COPY_IN
Variable t2 is COPY_IN
Variable t3 is COPY_IN
Variable t4 is CREATE
Variable x is COPY_IN
Variable x1 is CREATE
Variable x2 is CREATE

--- Tests to run for data-directive-analyser ---

./data-directive-analyser test2.c 10 21 test2present.csv CPP

Expected output:
10,21,COPY_IN,input
10,21,COPY,toReturn
16,16,UPDATE_HOST,toReturn
18,20,UPDATE_DEVICE,toReturn

--- Tests to run for data-insertion-finder ---

./data-insertion-finder test2.c test2present.csv CPP

Expected output:
4,22,2,1,0
5,22,2,2,0
10,22,2,2,0

(Still have some TODOs which will change these expected results)
