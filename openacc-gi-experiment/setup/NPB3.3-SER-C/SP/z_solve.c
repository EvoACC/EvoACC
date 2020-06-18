

































#include "header.h"


#include <openacc.h>








void z_solve()
{

	int i, j, k, k1, k2, m;

	double ru1, fac1, fac2;


	if (timeron) { timer_start(t_zsolve);}

	for (j = 1; j <= ny2; j++) {

		lhsinitj(nz2+1, nx2);









		for (i = 1; i <= nx2; i++) {

			for (k = 0; k <= nz2+1; k++) {

				ru1 = c3c4*rho_i[k][j][i];

				cv[k] = ws[k][j][i];

				rhos[k] = max(max(dz4+con43*ru1, dz5+c1c5*ru1), max(dzmax+ru1, dz1));

			}


			for (k = 1; k <= nz2; k++) {

				lhs[k][i][0] =  0.0;

				lhs[k][i][1] = -dttz2 * cv[k-1] - dttz1 * rhos[k-1];

				lhs[k][i][2] =  1.0 + c2dttz1 * rhos[k];

				lhs[k][i][3] =  dttz2 * cv[k+1] - dttz1 * rhos[k+1];

				lhs[k][i][4] =  0.0;

			}

		}





		for (i = 1; i <= nx2; i++) {

			k = 1;

			lhs[k][i][2] = lhs[k][i][2] + comz5;

			lhs[k][i][3] = lhs[k][i][3] - comz4;

			lhs[k][i][4] = lhs[k][i][4] + comz1;


			k = 2;

			lhs[k][i][1] = lhs[k][i][1] - comz4;

			lhs[k][i][2] = lhs[k][i][2] + comz6;

			lhs[k][i][3] = lhs[k][i][3] - comz4;

			lhs[k][i][4] = lhs[k][i][4] + comz1;

		}


		for (k = 3; k <= nz2-2; k++) {

			for (i = 1; i <= nx2; i++) {

				lhs[k][i][0] = lhs[k][i][0] + comz1;

				lhs[k][i][1] = lhs[k][i][1] - comz4;

				lhs[k][i][2] = lhs[k][i][2] + comz6;

				lhs[k][i][3] = lhs[k][i][3] - comz4;

				lhs[k][i][4] = lhs[k][i][4] + comz1;

			}

		}


		for (i = 1; i <= nx2; i++) {

			k = nz2-1;

			lhs[k][i][0] = lhs[k][i][0] + comz1;

			lhs[k][i][1] = lhs[k][i][1] - comz4;

			lhs[k][i][2] = lhs[k][i][2] + comz6;

			lhs[k][i][3] = lhs[k][i][3] - comz4;


			k = nz2;

			lhs[k][i][0] = lhs[k][i][0] + comz1;

			lhs[k][i][1] = lhs[k][i][1] - comz4;

			lhs[k][i][2] = lhs[k][i][2] + comz5;

		}





		for (k = 1; k <= nz2; k++) {

			for (i = 1; i <= nx2; i++) {

				lhsp[k][i][0] = lhs[k][i][0];

				lhsp[k][i][1] = lhs[k][i][1] - dttz2 * speed[k-1][j][i];

				lhsp[k][i][2] = lhs[k][i][2];

				lhsp[k][i][3] = lhs[k][i][3] + dttz2 * speed[k+1][j][i];

				lhsp[k][i][4] = lhs[k][i][4];

				lhsm[k][i][0] = lhs[k][i][0];

				lhsm[k][i][1] = lhs[k][i][1] + dttz2 * speed[k-1][j][i];

				lhsm[k][i][2] = lhs[k][i][2];

				lhsm[k][i][3] = lhs[k][i][3] - dttz2 * speed[k+1][j][i];

				lhsm[k][i][4] = lhs[k][i][4];

			}

		}






		for (k = 0; k <= grid_points[2]-3; k++) {

			k1 = k + 1;

			k2 = k + 2;

			for (i = 1; i <= nx2; i++) {

				fac1 = 1.0/lhs[k][i][2];

				lhs[k][i][3] = fac1*lhs[k][i][3];

				lhs[k][i][4] = fac1*lhs[k][i][4];

				for (m = 0; m < 3; m++) {

					rhs[k][j][i][m] = fac1*rhs[k][j][i][m];

				}

				lhs[k1][i][2] = lhs[k1][i][2] - lhs[k1][i][1]*lhs[k][i][3];

				lhs[k1][i][3] = lhs[k1][i][3] - lhs[k1][i][1]*lhs[k][i][4];

				for (m = 0; m < 3; m++) {

					rhs[k1][j][i][m] = rhs[k1][j][i][m] - lhs[k1][i][1]*rhs[k][j][i][m];

				}

				lhs[k2][i][1] = lhs[k2][i][1] - lhs[k2][i][0]*lhs[k][i][3];

				lhs[k2][i][2] = lhs[k2][i][2] - lhs[k2][i][0]*lhs[k][i][4];

				for (m = 0; m < 3; m++) {

					rhs[k2][j][i][m] = rhs[k2][j][i][m] - lhs[k2][i][0]*rhs[k][j][i][m];

				}

			}

		}







		k  = grid_points[2]-2;

		k1 = grid_points[2]-1;

		for (i = 1; i <= nx2; i++) {

			fac1 = 1.0/lhs[k][i][2];

			lhs[k][i][3] = fac1*lhs[k][i][3];

			lhs[k][i][4] = fac1*lhs[k][i][4];

			for (m = 0; m < 3; m++) {

				rhs[k][j][i][m] = fac1*rhs[k][j][i][m];

			}

			lhs[k1][i][2] = lhs[k1][i][2] - lhs[k1][i][1]*lhs[k][i][3];

			lhs[k1][i][3] = lhs[k1][i][3] - lhs[k1][i][1]*lhs[k][i][4];

			for (m = 0; m < 3; m++) {

				rhs[k1][j][i][m] = rhs[k1][j][i][m] - lhs[k1][i][1]*rhs[k][j][i][m];

			}





			fac2 = 1.0/lhs[k1][i][2];

			for (m = 0; m < 3; m++) {

				rhs[k1][j][i][m] = fac2*rhs[k1][j][i][m];

			}

		}





		for (k = 0; k <= grid_points[2]-3; k++) {

			k1 = k + 1;

			k2 = k + 2;

			for (i = 1; i <= nx2; i++) {

				m = 3;

				fac1 = 1.0/lhsp[k][i][2];

				lhsp[k][i][3]    = fac1*lhsp[k][i][3];

				lhsp[k][i][4]    = fac1*lhsp[k][i][4];

				rhs[k][j][i][m]  = fac1*rhs[k][j][i][m];

				lhsp[k1][i][2]   = lhsp[k1][i][2] - lhsp[k1][i][1]*lhsp[k][i][3];

				lhsp[k1][i][3]   = lhsp[k1][i][3] - lhsp[k1][i][1]*lhsp[k][i][4];

				rhs[k1][j][i][m] = rhs[k1][j][i][m] - lhsp[k1][i][1]*rhs[k][j][i][m];

				lhsp[k2][i][1]   = lhsp[k2][i][1] - lhsp[k2][i][0]*lhsp[k][i][3];

				lhsp[k2][i][2]   = lhsp[k2][i][2] - lhsp[k2][i][0]*lhsp[k][i][4];

				rhs[k2][j][i][m] = rhs[k2][j][i][m] - lhsp[k2][i][0]*rhs[k][j][i][m];


				m = 4;

				fac1 = 1.0/lhsm[k][i][2];

				lhsm[k][i][3]    = fac1*lhsm[k][i][3];

				lhsm[k][i][4]    = fac1*lhsm[k][i][4];

				rhs[k][j][i][m]  = fac1*rhs[k][j][i][m];

				lhsm[k1][i][2]   = lhsm[k1][i][2] - lhsm[k1][i][1]*lhsm[k][i][3];

				lhsm[k1][i][3]   = lhsm[k1][i][3] - lhsm[k1][i][1]*lhsm[k][i][4];

				rhs[k1][j][i][m] = rhs[k1][j][i][m] - lhsm[k1][i][1]*rhs[k][j][i][m];

				lhsm[k2][i][1]   = lhsm[k2][i][1] - lhsm[k2][i][0]*lhsm[k][i][3];

				lhsm[k2][i][2]   = lhsm[k2][i][2] - lhsm[k2][i][0]*lhsm[k][i][4];

				rhs[k2][j][i][m] = rhs[k2][j][i][m] - lhsm[k2][i][0]*rhs[k][j][i][m];

			}

		}





		k  = grid_points[2]-2;

		k1 = grid_points[2]-1;

		for (i = 1; i <= nx2; i++) {

			m = 3;

			fac1 = 1.0/lhsp[k][i][2];

			lhsp[k][i][3]    = fac1*lhsp[k][i][3];

			lhsp[k][i][4]    = fac1*lhsp[k][i][4];

			rhs[k][j][i][m]  = fac1*rhs[k][j][i][m];

			lhsp[k1][i][2]   = lhsp[k1][i][2] - lhsp[k1][i][1]*lhsp[k][i][3];

			lhsp[k1][i][3]   = lhsp[k1][i][3] - lhsp[k1][i][1]*lhsp[k][i][4];

			rhs[k1][j][i][m] = rhs[k1][j][i][m] - lhsp[k1][i][1]*rhs[k][j][i][m];


			m = 4;

			fac1 = 1.0/lhsm[k][i][2];

			lhsm[k][i][3]    = fac1*lhsm[k][i][3];

			lhsm[k][i][4]    = fac1*lhsm[k][i][4];

			rhs[k][j][i][m]  = fac1*rhs[k][j][i][m];

			lhsm[k1][i][2]   = lhsm[k1][i][2] - lhsm[k1][i][1]*lhsm[k][i][3];

			lhsm[k1][i][3]   = lhsm[k1][i][3] - lhsm[k1][i][1]*lhsm[k][i][4];

			rhs[k1][j][i][m] = rhs[k1][j][i][m] - lhsm[k1][i][1]*rhs[k][j][i][m];






			rhs[k1][j][i][3] = rhs[k1][j][i][3]/lhsp[k1][i][2];

			rhs[k1][j][i][4] = rhs[k1][j][i][4]/lhsm[k1][i][2];

		}






		k  = grid_points[2]-2;

		k1 = grid_points[2]-1;

		for (i = 1; i <= nx2; i++) {

			for (m = 0; m < 3; m++) {

				rhs[k][j][i][m] = rhs[k][j][i][m] - lhs[k][i][3]*rhs[k1][j][i][m];

			}


			rhs[k][j][i][3] = rhs[k][j][i][3] - lhsp[k][i][3]*rhs[k1][j][i][3];

			rhs[k][j][i][4] = rhs[k][j][i][4] - lhsm[k][i][3]*rhs[k1][j][i][4];

		}










		for (k = grid_points[2]-3; k >= 0; k--) {

			k1 = k + 1;

			k2 = k + 2;

			for (i = 1; i <= nx2; i++) {

				for (m = 0; m < 3; m++) {

					rhs[k][j][i][m] = rhs[k][j][i][m] -
					                  lhs[k][i][3]*rhs[k1][j][i][m] -
					                  lhs[k][i][4]*rhs[k2][j][i][m];

				}





				rhs[k][j][i][3] = rhs[k][j][i][3] -
				                  lhsp[k][i][3]*rhs[k1][j][i][3] -
				                  lhsp[k][i][4]*rhs[k2][j][i][3];

				rhs[k][j][i][4] = rhs[k][j][i][4] -
				                  lhsm[k][i][3]*rhs[k1][j][i][4] -
				                  lhsm[k][i][4]*rhs[k2][j][i][4];

			}

		}

	}

	if (timeron) { timer_stop(t_zsolve);}


	tzetar();

}

