

































#include "header.h"


#include "work_lhs.h"


#include "timers.h"


#include <openacc.h>













void x_solve()
{

	int i, j, k, m, n, isize;





	if (timeron) { timer_start(t_xsolve);}









	isize = grid_points[0]-1;





	for (k = 1; k <= grid_points[2]-2; k++) {

		for (j = 1; j <= grid_points[1]-2; j++) {

			for (i = 0; i <= isize; i++) {

				tmp1 = rho_i[k][j][i];

				tmp2 = tmp1 * tmp1;

				tmp3 = tmp1 * tmp2;




				fjac[i][0][0] = 0.0;

				fjac[i][1][0] = 1.0;

				fjac[i][2][0] = 0.0;

				fjac[i][3][0] = 0.0;

				fjac[i][4][0] = 0.0;


				fjac[i][0][1] = -(u[k][j][i][1] * tmp2 * u[k][j][i][1])
				                + c2 * qs[k][j][i];

				fjac[i][1][1] = ( 2.0 - c2 ) * ( u[k][j][i][1] / u[k][j][i][0] );

				fjac[i][2][1] = -c2 * ( u[k][j][i][2] * tmp1 );

				fjac[i][3][1] = -c2 * ( u[k][j][i][3] * tmp1 );

				fjac[i][4][1] = c2;


				fjac[i][0][2] = -( u[k][j][i][1]*u[k][j][i][2] ) * tmp2;

				fjac[i][1][2] = u[k][j][i][2] * tmp1;

				fjac[i][2][2] = u[k][j][i][1] * tmp1;

				fjac[i][3][2] = 0.0;

				fjac[i][4][2] = 0.0;


				fjac[i][0][3] = -( u[k][j][i][1]*u[k][j][i][3] ) * tmp2;

				fjac[i][1][3] = u[k][j][i][3] * tmp1;

				fjac[i][2][3] = 0.0;

				fjac[i][3][3] = u[k][j][i][1] * tmp1;

				fjac[i][4][3] = 0.0;


				fjac[i][0][4] = ( c2 * 2.0 * square[k][j][i] - c1 * u[k][j][i][4] )
				                * ( u[k][j][i][1] * tmp2 );

				fjac[i][1][4] = c1 *  u[k][j][i][4] * tmp1
				                - c2 * ( u[k][j][i][1]*u[k][j][i][1] * tmp2 + qs[k][j][i] );

				fjac[i][2][4] = -c2 * ( u[k][j][i][2]*u[k][j][i][1] ) * tmp2;

				fjac[i][3][4] = -c2 * ( u[k][j][i][3]*u[k][j][i][1] ) * tmp2;

				fjac[i][4][4] = c1 * ( u[k][j][i][1] * tmp1 );


				njac[i][0][0] = 0.0;

				njac[i][1][0] = 0.0;

				njac[i][2][0] = 0.0;

				njac[i][3][0] = 0.0;

				njac[i][4][0] = 0.0;


				njac[i][0][1] = -con43 * c3c4 * tmp2 * u[k][j][i][1];

				njac[i][1][1] =   con43 * c3c4 * tmp1;

				njac[i][2][1] =   0.0;

				njac[i][3][1] =   0.0;

				njac[i][4][1] =   0.0;


				njac[i][0][2] = -c3c4 * tmp2 * u[k][j][i][2];

				njac[i][1][2] =   0.0;

				njac[i][2][2] =   c3c4 * tmp1;

				njac[i][3][2] =   0.0;

				njac[i][4][2] =   0.0;


				njac[i][0][3] = -c3c4 * tmp2 * u[k][j][i][3];

				njac[i][1][3] =   0.0;

				njac[i][2][3] =   0.0;

				njac[i][3][3] =   c3c4 * tmp1;

				njac[i][4][3] =   0.0;


				njac[i][0][4] = -( con43 * c3c4- c1345 ) * tmp3 * (u[k][j][i][1]*u[k][j][i][1])
				                - ( c3c4 - c1345 ) * tmp3 * (u[k][j][i][2]*u[k][j][i][2])
				                - ( c3c4 - c1345 ) * tmp3 * (u[k][j][i][3]*u[k][j][i][3])
				                - c1345 * tmp2 * u[k][j][i][4];


				njac[i][1][4] = ( con43 * c3c4- c1345 ) * tmp2 * u[k][j][i][1];

				njac[i][2][4] = ( c3c4 - c1345 ) * tmp2 * u[k][j][i][2];

				njac[i][3][4] = ( c3c4 - c1345 ) * tmp2 * u[k][j][i][3];

				njac[i][4][4] = ( c1345 ) * tmp1;

			}




			lhsinit(lhs, isize);

			for (i = 1; i <= isize-1; i++) {

				tmp1 = dt * tx1;

				tmp2 = dt * tx2;


				lhs[i][AA][0][0] = -tmp2 * fjac[i-1][0][0]
				                   - tmp1 * njac[i-1][0][0]
				                   - tmp1 * dx1;

				lhs[i][AA][1][0] = -tmp2 * fjac[i-1][1][0]
				                   - tmp1 * njac[i-1][1][0];

				lhs[i][AA][2][0] = -tmp2 * fjac[i-1][2][0]
				                   - tmp1 * njac[i-1][2][0];

				lhs[i][AA][3][0] = -tmp2 * fjac[i-1][3][0]
				                   - tmp1 * njac[i-1][3][0];

				lhs[i][AA][4][0] = -tmp2 * fjac[i-1][4][0]
				                   - tmp1 * njac[i-1][4][0];


				lhs[i][AA][0][1] = -tmp2 * fjac[i-1][0][1]
				                   - tmp1 * njac[i-1][0][1];

				lhs[i][AA][1][1] = -tmp2 * fjac[i-1][1][1]
				                   - tmp1 * njac[i-1][1][1]
				                   - tmp1 * dx2;

				lhs[i][AA][2][1] = -tmp2 * fjac[i-1][2][1]
				                   - tmp1 * njac[i-1][2][1];

				lhs[i][AA][3][1] = -tmp2 * fjac[i-1][3][1]
				                   - tmp1 * njac[i-1][3][1];

				lhs[i][AA][4][1] = -tmp2 * fjac[i-1][4][1]
				                   - tmp1 * njac[i-1][4][1];


				lhs[i][AA][0][2] = -tmp2 * fjac[i-1][0][2]
				                   - tmp1 * njac[i-1][0][2];

				lhs[i][AA][1][2] = -tmp2 * fjac[i-1][1][2]
				                   - tmp1 * njac[i-1][1][2];

				lhs[i][AA][2][2] = -tmp2 * fjac[i-1][2][2]
				                   - tmp1 * njac[i-1][2][2]
				                   - tmp1 * dx3;

				lhs[i][AA][3][2] = -tmp2 * fjac[i-1][3][2]
				                   - tmp1 * njac[i-1][3][2];

				lhs[i][AA][4][2] = -tmp2 * fjac[i-1][4][2]
				                   - tmp1 * njac[i-1][4][2];


				lhs[i][AA][0][3] = -tmp2 * fjac[i-1][0][3]
				                   - tmp1 * njac[i-1][0][3];

				lhs[i][AA][1][3] = -tmp2 * fjac[i-1][1][3]
				                   - tmp1 * njac[i-1][1][3];

				lhs[i][AA][2][3] = -tmp2 * fjac[i-1][2][3]
				                   - tmp1 * njac[i-1][2][3];

				lhs[i][AA][3][3] = -tmp2 * fjac[i-1][3][3]
				                   - tmp1 * njac[i-1][3][3]
				                   - tmp1 * dx4;

				lhs[i][AA][4][3] = -tmp2 * fjac[i-1][4][3]
				                   - tmp1 * njac[i-1][4][3];


				lhs[i][AA][0][4] = -tmp2 * fjac[i-1][0][4]
				                   - tmp1 * njac[i-1][0][4];

				lhs[i][AA][1][4] = -tmp2 * fjac[i-1][1][4]
				                   - tmp1 * njac[i-1][1][4];

				lhs[i][AA][2][4] = -tmp2 * fjac[i-1][2][4]
				                   - tmp1 * njac[i-1][2][4];

				lhs[i][AA][3][4] = -tmp2 * fjac[i-1][3][4]
				                   - tmp1 * njac[i-1][3][4];

				lhs[i][AA][4][4] = -tmp2 * fjac[i-1][4][4]
				                   - tmp1 * njac[i-1][4][4]
				                   - tmp1 * dx5;


				lhs[i][BB][0][0] = 1.0
				                   + tmp1 * 2.0 * njac[i][0][0]
				                   + tmp1 * 2.0 * dx1;

				lhs[i][BB][1][0] = tmp1 * 2.0 * njac[i][1][0];

				lhs[i][BB][2][0] = tmp1 * 2.0 * njac[i][2][0];

				lhs[i][BB][3][0] = tmp1 * 2.0 * njac[i][3][0];

				lhs[i][BB][4][0] = tmp1 * 2.0 * njac[i][4][0];


				lhs[i][BB][0][1] = tmp1 * 2.0 * njac[i][0][1];

				lhs[i][BB][1][1] = 1.0
				                   + tmp1 * 2.0 * njac[i][1][1]
				                   + tmp1 * 2.0 * dx2;

				lhs[i][BB][2][1] = tmp1 * 2.0 * njac[i][2][1];

				lhs[i][BB][3][1] = tmp1 * 2.0 * njac[i][3][1];

				lhs[i][BB][4][1] = tmp1 * 2.0 * njac[i][4][1];


				lhs[i][BB][0][2] = tmp1 * 2.0 * njac[i][0][2];

				lhs[i][BB][1][2] = tmp1 * 2.0 * njac[i][1][2];

				lhs[i][BB][2][2] = 1.0
				                   + tmp1 * 2.0 * njac[i][2][2]
				                   + tmp1 * 2.0 * dx3;

				lhs[i][BB][3][2] = tmp1 * 2.0 * njac[i][3][2];

				lhs[i][BB][4][2] = tmp1 * 2.0 * njac[i][4][2];


				lhs[i][BB][0][3] = tmp1 * 2.0 * njac[i][0][3];

				lhs[i][BB][1][3] = tmp1 * 2.0 * njac[i][1][3];

				lhs[i][BB][2][3] = tmp1 * 2.0 * njac[i][2][3];

				lhs[i][BB][3][3] = 1.0
				                   + tmp1 * 2.0 * njac[i][3][3]
				                   + tmp1 * 2.0 * dx4;

				lhs[i][BB][4][3] = tmp1 * 2.0 * njac[i][4][3];


				lhs[i][BB][0][4] = tmp1 * 2.0 * njac[i][0][4];

				lhs[i][BB][1][4] = tmp1 * 2.0 * njac[i][1][4];

				lhs[i][BB][2][4] = tmp1 * 2.0 * njac[i][2][4];

				lhs[i][BB][3][4] = tmp1 * 2.0 * njac[i][3][4];

				lhs[i][BB][4][4] = 1.0
				                   + tmp1 * 2.0 * njac[i][4][4]
				                   + tmp1 * 2.0 * dx5;


				lhs[i][CC][0][0] =  tmp2 * fjac[i+1][0][0]
				                   - tmp1 * njac[i+1][0][0]
				                   - tmp1 * dx1;

				lhs[i][CC][1][0] =  tmp2 * fjac[i+1][1][0]
				                   - tmp1 * njac[i+1][1][0];

				lhs[i][CC][2][0] =  tmp2 * fjac[i+1][2][0]
				                   - tmp1 * njac[i+1][2][0];

				lhs[i][CC][3][0] =  tmp2 * fjac[i+1][3][0]
				                   - tmp1 * njac[i+1][3][0];

				lhs[i][CC][4][0] =  tmp2 * fjac[i+1][4][0]
				                   - tmp1 * njac[i+1][4][0];


				lhs[i][CC][0][1] =  tmp2 * fjac[i+1][0][1]
				                   - tmp1 * njac[i+1][0][1];

				lhs[i][CC][1][1] =  tmp2 * fjac[i+1][1][1]
				                   - tmp1 * njac[i+1][1][1]
				                   - tmp1 * dx2;

				lhs[i][CC][2][1] =  tmp2 * fjac[i+1][2][1]
				                   - tmp1 * njac[i+1][2][1];

				lhs[i][CC][3][1] =  tmp2 * fjac[i+1][3][1]
				                   - tmp1 * njac[i+1][3][1];

				lhs[i][CC][4][1] =  tmp2 * fjac[i+1][4][1]
				                   - tmp1 * njac[i+1][4][1];


				lhs[i][CC][0][2] =  tmp2 * fjac[i+1][0][2]
				                   - tmp1 * njac[i+1][0][2];

				lhs[i][CC][1][2] =  tmp2 * fjac[i+1][1][2]
				                   - tmp1 * njac[i+1][1][2];

				lhs[i][CC][2][2] =  tmp2 * fjac[i+1][2][2]
				                   - tmp1 * njac[i+1][2][2]
				                   - tmp1 * dx3;

				lhs[i][CC][3][2] =  tmp2 * fjac[i+1][3][2]
				                   - tmp1 * njac[i+1][3][2];

				lhs[i][CC][4][2] =  tmp2 * fjac[i+1][4][2]
				                   - tmp1 * njac[i+1][4][2];


				lhs[i][CC][0][3] =  tmp2 * fjac[i+1][0][3]
				                   - tmp1 * njac[i+1][0][3];

				lhs[i][CC][1][3] =  tmp2 * fjac[i+1][1][3]
				                   - tmp1 * njac[i+1][1][3];

				lhs[i][CC][2][3] =  tmp2 * fjac[i+1][2][3]
				                   - tmp1 * njac[i+1][2][3];

				lhs[i][CC][3][3] =  tmp2 * fjac[i+1][3][3]
				                   - tmp1 * njac[i+1][3][3]
				                   - tmp1 * dx4;

				lhs[i][CC][4][3] =  tmp2 * fjac[i+1][4][3]
				                   - tmp1 * njac[i+1][4][3];


				lhs[i][CC][0][4] =  tmp2 * fjac[i+1][0][4]
				                   - tmp1 * njac[i+1][0][4];

				lhs[i][CC][1][4] =  tmp2 * fjac[i+1][1][4]
				                   - tmp1 * njac[i+1][1][4];

				lhs[i][CC][2][4] =  tmp2 * fjac[i+1][2][4]
				                   - tmp1 * njac[i+1][2][4];

				lhs[i][CC][3][4] =  tmp2 * fjac[i+1][3][4]
				                   - tmp1 * njac[i+1][3][4];

				lhs[i][CC][4][4] =  tmp2 * fjac[i+1][4][4]
				                   - tmp1 * njac[i+1][4][4]
				                   - tmp1 * dx5;

			}























			binvcrhs( lhs[0][BB], lhs[0][CC], rhs[k][j][0] );






			for (i = 1; i <= isize-1; i++) {




				matvec_sub(lhs[i][AA], rhs[k][j][i-1], rhs[k][j][i]);





				matmul_sub(lhs[i][AA], lhs[i-1][CC], lhs[i][BB]);







				binvcrhs( lhs[i][BB], lhs[i][CC], rhs[k][j][i] );

			}





			matvec_sub(lhs[isize][AA], rhs[k][j][isize-1], rhs[k][j][isize]);





			matmul_sub(lhs[isize][AA], lhs[isize-1][CC], lhs[isize][BB]);





			binvrhs( lhs[isize][BB], rhs[k][j][isize] );








			for (i = isize-1; i >=0; i--) {

				for (m = 0; m < BLOCK_SIZE; m++) {

					for (n = 0; n < BLOCK_SIZE; n++) {

						rhs[k][j][i][m] = rhs[k][j][i][m]
						                  - lhs[i][CC][n][m]*rhs[k][j][i+1][n];

					}

				}

			}

		}

	}

	if (timeron) { timer_stop(t_xsolve);}

}

