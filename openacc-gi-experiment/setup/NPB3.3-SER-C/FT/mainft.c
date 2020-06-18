





































#include <stdio.h>


#include <math.h>



#include "global.h"


#include "print_results.h"


#include <openacc.h>


static char getclass();


logical timers_enabled;



int main(int argc, char *argv[])
{

	int niter;

	char Class;

	double total_time, mflops;

	logical verified;


	FILE *fp;

	if ((fp = fopen("timer.flag", "r")) != NULL) {

		timers_enabled = true;

		fclose(fp);

	}
	else {

		timers_enabled = false;

	}


	niter = NITER_DEFAULT;


	printf("\n\n NAS Parallel Benchmarks (NPB3.3-SER-C) - FT Benchmark\n\n");

	printf(" Size                : %4dx%4dx%4d\n", NX, NY, NZ);

	printf(" Iterations          :     %10d\n", niter);

	printf("\n");


	Class = getclass();


	appft(niter, &total_time, &verified);


	if (total_time != 0.0) {

		mflops = 1.0e-6 * (double)NTOTAL *
		         (14.8157 + 7.19641 * log((double)NTOTAL)+ (5.23518 + 7.21113 * log((double)NTOTAL)) * niter)
		         / total_time;

	}
	else {

		mflops = 0.0;

	}


	print_results("FT", Class, NX, NY, NZ, niter,total_time, mflops, "          floating point", verified, NPBVERSION, COMPILETIME, CS1, CS2, CS3, CS4, CS5, CS6, CS7);


	return 0;

}



static char getclass()
{

	if ((NX == 64) && (NY == 64) &&                 (NZ == 64) && (NITER_DEFAULT == 6)) {

		return 'S';

	}
	else if ((NX == 128) && (NY == 128) &&(NZ == 32) && (NITER_DEFAULT == 6)) {

		return 'W';

	}
	else if ((NX == 256) && (NY == 256) &&(NZ == 128) && (NITER_DEFAULT == 6)) {

		return 'A';

	}
	else if ((NX == 512) && (NY == 256) &&(NZ == 256) && (NITER_DEFAULT == 20)) {

		return 'B';

	}
	else if ((NX == 512) && (NY == 512) &&(NZ == 512) && (NITER_DEFAULT == 20)) {

		return 'C';

	}
	else if ((NX == 2048) && (NY == 1024) &&(NZ == 1024) && (NITER_DEFAULT == 25)) {

		return 'D';

	}
	else {

		return 'U';

	}

}


