












































#include <stdio.h>


#include <stdlib.h>


#include <math.h>



#include "applu.incl"


#include "timers.h"


#include "print_results.h"


#include <openacc.h>






double dxi, deta, dzeta;

double tx1, tx2, tx3;

double ty1, ty2, ty3;

double tz1, tz2, tz3;

int nx, ny, nz;

int nx0, ny0, nz0;

int ist, iend;

int jst, jend;

int ii1, ii2;

int ji1, ji2;

int ki1, ki2;






double dx1, dx2, dx3, dx4, dx5;

double dy1, dy2, dy3, dy4, dy5;

double dz1, dz2, dz3, dz4, dz5;

double dssp;










double u    [ISIZ3][ISIZ2/2*2+1][ISIZ1/2*2+1][5];

double rsd  [ISIZ3][ISIZ2/2*2+1][ISIZ1/2*2+1][5];

double frct [ISIZ3][ISIZ2/2*2+1][ISIZ1/2*2+1][5];

double flux [ISIZ1][5];

double qs   [ISIZ3][ISIZ2/2*2+1][ISIZ1/2*2+1];

double rho_i[ISIZ3][ISIZ2/2*2+1][ISIZ1/2*2+1];






int ipr, inorm;






double dt, omega, tolrsd[5], rsdnm[5], errnm[5], frc, ttotal;

int itmax, invert;



double a[ISIZ2][ISIZ1/2*2+1][5][5];

double b[ISIZ2][ISIZ1/2*2+1][5][5];

double c[ISIZ2][ISIZ1/2*2+1][5][5];

double d[ISIZ2][ISIZ1/2*2+1][5][5];







double ce[5][13];







double maxtime;

logical timeron;



int main(int argc, char *argv[])
{

	char Class;

	logical verified;

	double mflops;


	double t, tmax, trecs[t_last+1];

	int i;

	char *t_names[t_last+1];





	FILE *fp;

	if ((fp = fopen("timer.flag", "r")) != NULL) {

		timeron = true;

		t_names[t_total] = "total";

		t_names[t_rhsx] = "rhsx";

		t_names[t_rhsy] = "rhsy";

		t_names[t_rhsz] = "rhsz";

		t_names[t_rhs] = "rhs";

		t_names[t_jacld] = "jacld";

		t_names[t_blts] = "blts";

		t_names[t_jacu] = "jacu";

		t_names[t_buts] = "buts";

		t_names[t_add] = "add";

		t_names[t_l2norm] = "l2norm";

		fclose(fp);

	}
	else {

		timeron = false;

	}





	read_input();





	domain();





	setcoeff();





	setbv();





	setiv();





	erhs();





	ssor(1);





	setbv();

	setiv();





	ssor(itmax);





	error();





	pintgr();





	verify ( rsdnm, errnm, frc, &Class, &verified );

	mflops = (double)itmax * (1984.77 * (double)nx0* (double)ny0* (double)nz0- 10923.3 * pow(((double)(nx0+ny0+nz0)/3.0), 2.0) + 27770.9 * (double)(nx0+ny0+nz0)/3.0- 144010.0)
	         / (maxtime*1000000.0);


	print_results("LU", Class, nx0,ny0, nz0, itmax,maxtime, mflops, "          floating point", verified, NPBVERSION, COMPILETIME, CS1, CS2, CS3, CS4, CS5, CS6, "(none)");





	if (timeron) {

		for (i = 1; i <= t_last; i++) {

			trecs[i] = timer_read(i);

		}

		tmax = maxtime;

		if (tmax == 0.0) { tmax = 1.0;}


		printf("  SECTION     Time (secs)\n");

		for (i = 1; i <= t_last; i++) {

			printf("  %-8s:%9.3f  (%6.2f%%)\n",t_names[i], trecs[i], trecs[i]*100./tmax);

			if (i == t_rhs) {

				t = trecs[t_rhsx] + trecs[t_rhsy] + trecs[t_rhsz];

				printf("     --> %8s:%9.3f  (%6.2f%%)\n", "sub-rhs", t, t*100./tmax);

				t = trecs[i] - t;

				printf("     --> %8s:%9.3f  (%6.2f%%)\n", "rest-rhs", t, t*100./tmax);

			}

		}

	}


	return 0;

}


