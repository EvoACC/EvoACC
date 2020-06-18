

































#include "header.h"


#include <openacc.h>








void x_solve()
{

	int i, j, k, i1, i2, m;

	double ru1, fac1, fac2;


	if (timeron) { timer_start(t_xsolve);}

	for (k = 1; k <= nz2; k++) {

		lhsinit(nx2+1, ny2);









		for (j = 1; j <= ny2; j++) {

			for (i = 0; i <= grid_points[0]-1; i++) {

				ru1 = c3c4*rho_i[k][j][i];

				cv[i] = us[k][j][i];

				rhon[i] = max(max(dx2+con43*ru1,dx5+c1c5*ru1), max(dxmax+ru1,dx1));

			}


			for (i = 1; i <= nx2; i++) {

				lhs[j][i][0] =  0.0;

				lhs[j][i][1] = -dttx2 * cv[i-1] - dttx1 * rhon[i-1];

				lhs[j][i][2] =  1.0 + c2dttx1 * rhon[i];

				lhs[j][i][3] =  dttx2 * cv[i+1] - dttx1 * rhon[i+1];

				lhs[j][i][4] =  0.0;

			}

		}





		for (j = 1; j <= ny2; j++) {

			i = 1;

			lhs[j][i][2] = lhs[j][i][2] + comz5;

			lhs[j][i][3] = lhs[j][i][3] - comz4;

			lhs[j][i][4] = lhs[j][i][4] + comz1;


			lhs[j][i+1][1] = lhs[j][i+1][1] - comz4;

			lhs[j][i+1][2] = lhs[j][i+1][2] + comz6;

			lhs[j][i+1][3] = lhs[j][i+1][3] - comz4;

			lhs[j][i+1][4] = lhs[j][i+1][4] + comz1;

		}


		for (j = 1; j <= ny2; j++) {

			for (i = 3; i <= grid_points[0]-4; i++) {

				lhs[j][i][0] = lhs[j][i][0] + comz1;

				lhs[j][i][1] = lhs[j][i][1] - comz4;

				lhs[j][i][2] = lhs[j][i][2] + comz6;

				lhs[j][i][3] = lhs[j][i][3] - comz4;

				lhs[j][i][4] = lhs[j][i][4] + comz1;

			}

		}


		for (j = 1; j <= ny2; j++) {

			i = grid_points[0]-3;

			lhs[j][i][0] = lhs[j][i][0] + comz1;

			lhs[j][i][1] = lhs[j][i][1] - comz4;

			lhs[j][i][2] = lhs[j][i][2] + comz6;

			lhs[j][i][3] = lhs[j][i][3] - comz4;


			lhs[j][i+1][0] = lhs[j][i+1][0] + comz1;

			lhs[j][i+1][1] = lhs[j][i+1][1] - comz4;

			lhs[j][i+1][2] = lhs[j][i+1][2] + comz5;

		}






		for (j = 1; j <= ny2; j++) {

			for (i = 1; i <= nx2; i++) {

				lhsp[j][i][0] = lhs[j][i][0];

				lhsp[j][i][1] = lhs[j][i][1] - dttx2 * speed[k][j][i-1];

				lhsp[j][i][2] = lhs[j][i][2];

				lhsp[j][i][3] = lhs[j][i][3] + dttx2 * speed[k][j][i+1];

				lhsp[j][i][4] = lhs[j][i][4];

				lhsm[j][i][0] = lhs[j][i][0];

				lhsm[j][i][1] = lhs[j][i][1] + dttx2 * speed[k][j][i-1];

				lhsm[j][i][2] = lhs[j][i][2];

				lhsm[j][i][3] = lhs[j][i][3] - dttx2 * speed[k][j][i+1];

				lhsm[j][i][4] = lhs[j][i][4];

			}

		}









		for (j = 1; j <= ny2; j++) {

			for (i = 0; i <= grid_points[0]-3; i++) {

				i1 = i + 1;

				i2 = i + 2;

				fac1 = 1.0/lhs[j][i][2];

				lhs[j][i][3] = fac1*lhs[j][i][3];

				lhs[j][i][4] = fac1*lhs[j][i][4];

				for (m = 0; m < 3; m++) {

					rhs[k][j][i][m] = fac1*rhs[k][j][i][m];

				}

				lhs[j][i1][2] = lhs[j][i1][2] - lhs[j][i1][1]*lhs[j][i][3];

				lhs[j][i1][3] = lhs[j][i1][3] - lhs[j][i1][1]*lhs[j][i][4];

				for (m = 0; m < 3; m++) {

					rhs[k][j][i1][m] = rhs[k][j][i1][m] - lhs[j][i1][1]*rhs[k][j][i][m];

				}

				lhs[j][i2][1] = lhs[j][i2][1] - lhs[j][i2][0]*lhs[j][i][3];

				lhs[j][i2][2] = lhs[j][i2][2] - lhs[j][i2][0]*lhs[j][i][4];

				for (m = 0; m < 3; m++) {

					rhs[k][j][i2][m] = rhs[k][j][i2][m] - lhs[j][i2][0]*rhs[k][j][i][m];

				}

			}

		}







		for (j = 1; j <= ny2; j++) {

			i  = grid_points[0]-2;

			i1 = grid_points[0]-1;

			fac1 = 1.0/lhs[j][i][2];

			lhs[j][i][3] = fac1*lhs[j][i][3];

			lhs[j][i][4] = fac1*lhs[j][i][4];

			for (m = 0; m < 3; m++) {

				rhs[k][j][i][m] = fac1*rhs[k][j][i][m];

			}

			lhs[j][i1][2] = lhs[j][i1][2] - lhs[j][i1][1]*lhs[j][i][3];

			lhs[j][i1][3] = lhs[j][i1][3] - lhs[j][i1][1]*lhs[j][i][4];

			for (m = 0; m < 3; m++) {

				rhs[k][j][i1][m] = rhs[k][j][i1][m] - lhs[j][i1][1]*rhs[k][j][i][m];

			}





			fac2 = 1.0/lhs[j][i1][2];

			for (m = 0; m < 3; m++) {

				rhs[k][j][i1][m] = fac2*rhs[k][j][i1][m];

			}

		}





		for (j = 1; j <= ny2; j++) {

			for (i = 0; i <= grid_points[0]-3; i++) {

				i1 = i + 1;

				i2 = i + 2;


				m = 3;

				fac1 = 1.0/lhsp[j][i][2];

				lhsp[j][i][3]    = fac1*lhsp[j][i][3];

				lhsp[j][i][4]    = fac1*lhsp[j][i][4];

				rhs[k][j][i][m]  = fac1*rhs[k][j][i][m];

				lhsp[j][i1][2]   = lhsp[j][i1][2] - lhsp[j][i1][1]*lhsp[j][i][3];

				lhsp[j][i1][3]   = lhsp[j][i1][3] - lhsp[j][i1][1]*lhsp[j][i][4];

				rhs[k][j][i1][m] = rhs[k][j][i1][m] - lhsp[j][i1][1]*rhs[k][j][i][m];

				lhsp[j][i2][1]   = lhsp[j][i2][1] - lhsp[j][i2][0]*lhsp[j][i][3];

				lhsp[j][i2][2]   = lhsp[j][i2][2] - lhsp[j][i2][0]*lhsp[j][i][4];

				rhs[k][j][i2][m] = rhs[k][j][i2][m] - lhsp[j][i2][0]*rhs[k][j][i][m];


				m = 4;

				fac1 = 1.0/lhsm[j][i][2];

				lhsm[j][i][3]    = fac1*lhsm[j][i][3];

				lhsm[j][i][4]    = fac1*lhsm[j][i][4];

				rhs[k][j][i][m]  = fac1*rhs[k][j][i][m];

				lhsm[j][i1][2]   = lhsm[j][i1][2] - lhsm[j][i1][1]*lhsm[j][i][3];

				lhsm[j][i1][3]   = lhsm[j][i1][3] - lhsm[j][i1][1]*lhsm[j][i][4];

				rhs[k][j][i1][m] = rhs[k][j][i1][m] - lhsm[j][i1][1]*rhs[k][j][i][m];

				lhsm[j][i2][1]   = lhsm[j][i2][1] - lhsm[j][i2][0]*lhsm[j][i][3];

				lhsm[j][i2][2]   = lhsm[j][i2][2] - lhsm[j][i2][0]*lhsm[j][i][4];

				rhs[k][j][i2][m] = rhs[k][j][i2][m] - lhsm[j][i2][0]*rhs[k][j][i][m];

			}

		}





		for (j = 1; j <= ny2; j++) {

			i  = grid_points[0]-2;

			i1 = grid_points[0]-1;


			m = 3;

			fac1 = 1.0/lhsp[j][i][2];

			lhsp[j][i][3]    = fac1*lhsp[j][i][3];

			lhsp[j][i][4]    = fac1*lhsp[j][i][4];

			rhs[k][j][i][m]  = fac1*rhs[k][j][i][m];

			lhsp[j][i1][2]   = lhsp[j][i1][2] - lhsp[j][i1][1]*lhsp[j][i][3];

			lhsp[j][i1][3]   = lhsp[j][i1][3] - lhsp[j][i1][1]*lhsp[j][i][4];

			rhs[k][j][i1][m] = rhs[k][j][i1][m] - lhsp[j][i1][1]*rhs[k][j][i][m];


			m = 4;

			fac1 = 1.0/lhsm[j][i][2];

			lhsm[j][i][3]    = fac1*lhsm[j][i][3];

			lhsm[j][i][4]    = fac1*lhsm[j][i][4];

			rhs[k][j][i][m]  = fac1*rhs[k][j][i][m];

			lhsm[j][i1][2]   = lhsm[j][i1][2] - lhsm[j][i1][1]*lhsm[j][i][3];

			lhsm[j][i1][3]   = lhsm[j][i1][3] - lhsm[j][i1][1]*lhsm[j][i][4];

			rhs[k][j][i1][m] = rhs[k][j][i1][m] - lhsm[j][i1][1]*rhs[k][j][i][m];





			rhs[k][j][i1][3] = rhs[k][j][i1][3]/lhsp[j][i1][2];

			rhs[k][j][i1][4] = rhs[k][j][i1][4]/lhsm[j][i1][2];

		}





		for (j = 1; j <= ny2; j++) {

			i  = grid_points[0]-2;

			i1 = grid_points[0]-1;

			for (m = 0; m < 3; m++) {

				rhs[k][j][i][m] = rhs[k][j][i][m] - lhs[j][i][3]*rhs[k][j][i1][m];

			}


			rhs[k][j][i][3] = rhs[k][j][i][3] - lhsp[j][i][3]*rhs[k][j][i1][3];

			rhs[k][j][i][4] = rhs[k][j][i][4] - lhsm[j][i][3]*rhs[k][j][i1][4];

		}





		for (j = 1; j <= ny2; j++) {

			for (i = grid_points[0]-3; i >= 0; i--) {

				i1 = i + 1;

				i2 = i + 2;

				for (m = 0; m < 3; m++) {

					rhs[k][j][i][m] = rhs[k][j][i][m] -
					                  lhs[j][i][3]*rhs[k][j][i1][m] -
					                  lhs[j][i][4]*rhs[k][j][i2][m];

				}





				rhs[k][j][i][3] = rhs[k][j][i][3] -
				                  lhsp[j][i][3]*rhs[k][j][i1][3] -
				                  lhsp[j][i][4]*rhs[k][j][i2][3];

				rhs[k][j][i][4] = rhs[k][j][i][4] -
				                  lhsm[j][i][3]*rhs[k][j][i1][4] -
				                  lhsm[j][i][4]*rhs[k][j][i2][4];

			}

		}

	}

	if (timeron) { timer_stop(t_xsolve);}





	ninvr();

}


