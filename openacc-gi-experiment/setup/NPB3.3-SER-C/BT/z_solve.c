

































#include "header.h"


#include "work_lhs.h"


#include "timers.h"


#include <openacc.h>











void z_solve()
{

	int i, j, k, m, n, ksize;





	if (timeron) { timer_start(t_zsolve);}









	ksize = grid_points[2]-1;






	for (j = 1; j <= grid_points[1]-2; j++) {

		for (i = 1; i <= grid_points[0]-2; i++) {

			for (k = 0; k <= ksize; k++) {

				tmp1 = 1.0 / u[k][j][i][0];

				tmp2 = tmp1 * tmp1;

				tmp3 = tmp1 * tmp2;


				fjac[k][0][0] = 0.0;

				fjac[k][1][0] = 0.0;

				fjac[k][2][0] = 0.0;

				fjac[k][3][0] = 1.0;

				fjac[k][4][0] = 0.0;


				fjac[k][0][1] = -( u[k][j][i][1]*u[k][j][i][3] ) * tmp2;

				fjac[k][1][1] = u[k][j][i][3] * tmp1;

				fjac[k][2][1] = 0.0;

				fjac[k][3][1] = u[k][j][i][1] * tmp1;

				fjac[k][4][1] = 0.0;


				fjac[k][0][2] = -( u[k][j][i][2]*u[k][j][i][3] ) * tmp2;

				fjac[k][1][2] = 0.0;

				fjac[k][2][2] = u[k][j][i][3] * tmp1;

				fjac[k][3][2] = u[k][j][i][2] * tmp1;

				fjac[k][4][2] = 0.0;


				fjac[k][0][3] = -(u[k][j][i][3]*u[k][j][i][3] * tmp2 )
				                + c2 * qs[k][j][i];

				fjac[k][1][3] = -c2 *  u[k][j][i][1] * tmp1;

				fjac[k][2][3] = -c2 *  u[k][j][i][2] * tmp1;

				fjac[k][3][3] = ( 2.0 - c2 ) *  u[k][j][i][3] * tmp1;

				fjac[k][4][3] = c2;


				fjac[k][0][4] = ( c2 * 2.0 * square[k][j][i] - c1 * u[k][j][i][4] )
				                * u[k][j][i][3] * tmp2;

				fjac[k][1][4] = -c2 * ( u[k][j][i][1]*u[k][j][i][3] ) * tmp2;

				fjac[k][2][4] = -c2 * ( u[k][j][i][2]*u[k][j][i][3] ) * tmp2;

				fjac[k][3][4] = c1 * ( u[k][j][i][4] * tmp1 )
				                - c2 * ( qs[k][j][i] + u[k][j][i][3]*u[k][j][i][3] * tmp2 );

				fjac[k][4][4] = c1 * u[k][j][i][3] * tmp1;


				njac[k][0][0] = 0.0;

				njac[k][1][0] = 0.0;

				njac[k][2][0] = 0.0;

				njac[k][3][0] = 0.0;

				njac[k][4][0] = 0.0;


				njac[k][0][1] = -c3c4 * tmp2 * u[k][j][i][1];

				njac[k][1][1] =   c3c4 * tmp1;

				njac[k][2][1] =   0.0;

				njac[k][3][1] =   0.0;

				njac[k][4][1] =   0.0;


				njac[k][0][2] = -c3c4 * tmp2 * u[k][j][i][2];

				njac[k][1][2] =   0.0;

				njac[k][2][2] =   c3c4 * tmp1;

				njac[k][3][2] =   0.0;

				njac[k][4][2] =   0.0;


				njac[k][0][3] = -con43 * c3c4 * tmp2 * u[k][j][i][3];

				njac[k][1][3] =   0.0;

				njac[k][2][3] =   0.0;

				njac[k][3][3] =   con43 * c3 * c4 * tmp1;

				njac[k][4][3] =   0.0;


				njac[k][0][4] = -(  c3c4- c1345 ) * tmp3 * (u[k][j][i][1]*u[k][j][i][1])
				                - ( c3c4 - c1345 ) * tmp3 * (u[k][j][i][2]*u[k][j][i][2])
				                - ( con43 * c3c4- c1345 ) * tmp3 * (u[k][j][i][3]*u[k][j][i][3])
				                - c1345 * tmp2 * u[k][j][i][4];


				njac[k][1][4] = (  c3c4 - c1345 ) * tmp2 * u[k][j][i][1];

				njac[k][2][4] = (  c3c4 - c1345 ) * tmp2 * u[k][j][i][2];

				njac[k][3][4] = ( con43 * c3c4- c1345 ) * tmp2 * u[k][j][i][3];

				njac[k][4][4] = ( c1345 )* tmp1;

			}





			lhsinit(lhs, ksize);

			for (k = 1; k <= ksize-1; k++) {

				tmp1 = dt * tz1;

				tmp2 = dt * tz2;


				lhs[k][AA][0][0] = -tmp2 * fjac[k-1][0][0]
				                   - tmp1 * njac[k-1][0][0]
				                   - tmp1 * dz1;

				lhs[k][AA][1][0] = -tmp2 * fjac[k-1][1][0]
				                   - tmp1 * njac[k-1][1][0];

				lhs[k][AA][2][0] = -tmp2 * fjac[k-1][2][0]
				                   - tmp1 * njac[k-1][2][0];

				lhs[k][AA][3][0] = -tmp2 * fjac[k-1][3][0]
				                   - tmp1 * njac[k-1][3][0];

				lhs[k][AA][4][0] = -tmp2 * fjac[k-1][4][0]
				                   - tmp1 * njac[k-1][4][0];


				lhs[k][AA][0][1] = -tmp2 * fjac[k-1][0][1]
				                   - tmp1 * njac[k-1][0][1];

				lhs[k][AA][1][1] = -tmp2 * fjac[k-1][1][1]
				                   - tmp1 * njac[k-1][1][1]
				                   - tmp1 * dz2;

				lhs[k][AA][2][1] = -tmp2 * fjac[k-1][2][1]
				                   - tmp1 * njac[k-1][2][1];

				lhs[k][AA][3][1] = -tmp2 * fjac[k-1][3][1]
				                   - tmp1 * njac[k-1][3][1];

				lhs[k][AA][4][1] = -tmp2 * fjac[k-1][4][1]
				                   - tmp1 * njac[k-1][4][1];


				lhs[k][AA][0][2] = -tmp2 * fjac[k-1][0][2]
				                   - tmp1 * njac[k-1][0][2];

				lhs[k][AA][1][2] = -tmp2 * fjac[k-1][1][2]
				                   - tmp1 * njac[k-1][1][2];

				lhs[k][AA][2][2] = -tmp2 * fjac[k-1][2][2]
				                   - tmp1 * njac[k-1][2][2]
				                   - tmp1 * dz3;

				lhs[k][AA][3][2] = -tmp2 * fjac[k-1][3][2]
				                   - tmp1 * njac[k-1][3][2];

				lhs[k][AA][4][2] = -tmp2 * fjac[k-1][4][2]
				                   - tmp1 * njac[k-1][4][2];


				lhs[k][AA][0][3] = -tmp2 * fjac[k-1][0][3]
				                   - tmp1 * njac[k-1][0][3];

				lhs[k][AA][1][3] = -tmp2 * fjac[k-1][1][3]
				                   - tmp1 * njac[k-1][1][3];

				lhs[k][AA][2][3] = -tmp2 * fjac[k-1][2][3]
				                   - tmp1 * njac[k-1][2][3];

				lhs[k][AA][3][3] = -tmp2 * fjac[k-1][3][3]
				                   - tmp1 * njac[k-1][3][3]
				                   - tmp1 * dz4;

				lhs[k][AA][4][3] = -tmp2 * fjac[k-1][4][3]
				                   - tmp1 * njac[k-1][4][3];


				lhs[k][AA][0][4] = -tmp2 * fjac[k-1][0][4]
				                   - tmp1 * njac[k-1][0][4];

				lhs[k][AA][1][4] = -tmp2 * fjac[k-1][1][4]
				                   - tmp1 * njac[k-1][1][4];

				lhs[k][AA][2][4] = -tmp2 * fjac[k-1][2][4]
				                   - tmp1 * njac[k-1][2][4];

				lhs[k][AA][3][4] = -tmp2 * fjac[k-1][3][4]
				                   - tmp1 * njac[k-1][3][4];

				lhs[k][AA][4][4] = -tmp2 * fjac[k-1][4][4]
				                   - tmp1 * njac[k-1][4][4]
				                   - tmp1 * dz5;


				lhs[k][BB][0][0] = 1.0
				                   + tmp1 * 2.0 * njac[k][0][0]
				                   + tmp1 * 2.0 * dz1;

				lhs[k][BB][1][0] = tmp1 * 2.0 * njac[k][1][0];

				lhs[k][BB][2][0] = tmp1 * 2.0 * njac[k][2][0];

				lhs[k][BB][3][0] = tmp1 * 2.0 * njac[k][3][0];

				lhs[k][BB][4][0] = tmp1 * 2.0 * njac[k][4][0];


				lhs[k][BB][0][1] = tmp1 * 2.0 * njac[k][0][1];

				lhs[k][BB][1][1] = 1.0
				                   + tmp1 * 2.0 * njac[k][1][1]
				                   + tmp1 * 2.0 * dz2;

				lhs[k][BB][2][1] = tmp1 * 2.0 * njac[k][2][1];

				lhs[k][BB][3][1] = tmp1 * 2.0 * njac[k][3][1];

				lhs[k][BB][4][1] = tmp1 * 2.0 * njac[k][4][1];


				lhs[k][BB][0][2] = tmp1 * 2.0 * njac[k][0][2];

				lhs[k][BB][1][2] = tmp1 * 2.0 * njac[k][1][2];

				lhs[k][BB][2][2] = 1.0
				                   + tmp1 * 2.0 * njac[k][2][2]
				                   + tmp1 * 2.0 * dz3;

				lhs[k][BB][3][2] = tmp1 * 2.0 * njac[k][3][2];

				lhs[k][BB][4][2] = tmp1 * 2.0 * njac[k][4][2];


				lhs[k][BB][0][3] = tmp1 * 2.0 * njac[k][0][3];

				lhs[k][BB][1][3] = tmp1 * 2.0 * njac[k][1][3];

				lhs[k][BB][2][3] = tmp1 * 2.0 * njac[k][2][3];

				lhs[k][BB][3][3] = 1.0
				                   + tmp1 * 2.0 * njac[k][3][3]
				                   + tmp1 * 2.0 * dz4;

				lhs[k][BB][4][3] = tmp1 * 2.0 * njac[k][4][3];


				lhs[k][BB][0][4] = tmp1 * 2.0 * njac[k][0][4];

				lhs[k][BB][1][4] = tmp1 * 2.0 * njac[k][1][4];

				lhs[k][BB][2][4] = tmp1 * 2.0 * njac[k][2][4];

				lhs[k][BB][3][4] = tmp1 * 2.0 * njac[k][3][4];

				lhs[k][BB][4][4] = 1.0
				                   + tmp1 * 2.0 * njac[k][4][4]
				                   + tmp1 * 2.0 * dz5;


				lhs[k][CC][0][0] =  tmp2 * fjac[k+1][0][0]
				                   - tmp1 * njac[k+1][0][0]
				                   - tmp1 * dz1;

				lhs[k][CC][1][0] =  tmp2 * fjac[k+1][1][0]
				                   - tmp1 * njac[k+1][1][0];

				lhs[k][CC][2][0] =  tmp2 * fjac[k+1][2][0]
				                   - tmp1 * njac[k+1][2][0];

				lhs[k][CC][3][0] =  tmp2 * fjac[k+1][3][0]
				                   - tmp1 * njac[k+1][3][0];

				lhs[k][CC][4][0] =  tmp2 * fjac[k+1][4][0]
				                   - tmp1 * njac[k+1][4][0];


				lhs[k][CC][0][1] =  tmp2 * fjac[k+1][0][1]
				                   - tmp1 * njac[k+1][0][1];

				lhs[k][CC][1][1] =  tmp2 * fjac[k+1][1][1]
				                   - tmp1 * njac[k+1][1][1]
				                   - tmp1 * dz2;

				lhs[k][CC][2][1] =  tmp2 * fjac[k+1][2][1]
				                   - tmp1 * njac[k+1][2][1];

				lhs[k][CC][3][1] =  tmp2 * fjac[k+1][3][1]
				                   - tmp1 * njac[k+1][3][1];

				lhs[k][CC][4][1] =  tmp2 * fjac[k+1][4][1]
				                   - tmp1 * njac[k+1][4][1];


				lhs[k][CC][0][2] =  tmp2 * fjac[k+1][0][2]
				                   - tmp1 * njac[k+1][0][2];

				lhs[k][CC][1][2] =  tmp2 * fjac[k+1][1][2]
				                   - tmp1 * njac[k+1][1][2];

				lhs[k][CC][2][2] =  tmp2 * fjac[k+1][2][2]
				                   - tmp1 * njac[k+1][2][2]
				                   - tmp1 * dz3;

				lhs[k][CC][3][2] =  tmp2 * fjac[k+1][3][2]
				                   - tmp1 * njac[k+1][3][2];

				lhs[k][CC][4][2] =  tmp2 * fjac[k+1][4][2]
				                   - tmp1 * njac[k+1][4][2];


				lhs[k][CC][0][3] =  tmp2 * fjac[k+1][0][3]
				                   - tmp1 * njac[k+1][0][3];

				lhs[k][CC][1][3] =  tmp2 * fjac[k+1][1][3]
				                   - tmp1 * njac[k+1][1][3];

				lhs[k][CC][2][3] =  tmp2 * fjac[k+1][2][3]
				                   - tmp1 * njac[k+1][2][3];

				lhs[k][CC][3][3] =  tmp2 * fjac[k+1][3][3]
				                   - tmp1 * njac[k+1][3][3]
				                   - tmp1 * dz4;

				lhs[k][CC][4][3] =  tmp2 * fjac[k+1][4][3]
				                   - tmp1 * njac[k+1][4][3];


				lhs[k][CC][0][4] =  tmp2 * fjac[k+1][0][4]
				                   - tmp1 * njac[k+1][0][4];

				lhs[k][CC][1][4] =  tmp2 * fjac[k+1][1][4]
				                   - tmp1 * njac[k+1][1][4];

				lhs[k][CC][2][4] =  tmp2 * fjac[k+1][2][4]
				                   - tmp1 * njac[k+1][2][4];

				lhs[k][CC][3][4] =  tmp2 * fjac[k+1][3][4]
				                   - tmp1 * njac[k+1][3][4];

				lhs[k][CC][4][4] =  tmp2 * fjac[k+1][4][4]
				                   - tmp1 * njac[k+1][4][4]
				                   - tmp1 * dz5;

			}























			binvcrhs( lhs[0][BB], lhs[0][CC], rhs[0][j][i] );






			for (k = 1; k <= ksize-1; k++) {






				matvec_sub(lhs[k][AA], rhs[k-1][j][i], rhs[k][j][i]);






				matmul_sub(lhs[k][AA], lhs[k-1][CC], lhs[k][BB]);






				binvcrhs( lhs[k][BB], lhs[k][CC], rhs[k][j][i] );

			}









			matvec_sub(lhs[ksize][AA], rhs[ksize-1][j][i], rhs[ksize][j][i]);







			matmul_sub(lhs[ksize][AA], lhs[ksize-1][CC], lhs[ksize][BB]);





			binvrhs( lhs[ksize][BB], rhs[ksize][j][i] );












			for (k = ksize-1; k >= 0; k--) {

				for (m = 0; m < BLOCK_SIZE; m++) {

					for (n = 0; n < BLOCK_SIZE; n++) {

						rhs[k][j][i][m] = rhs[k][j][i][m]
						                  - lhs[k][CC][n][m]*rhs[k+1][j][i][n];

					}

				}

			}

		}

	}

	if (timeron) { timer_stop(t_zsolve);}

}

