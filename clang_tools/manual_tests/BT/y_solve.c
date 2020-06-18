

































#include "header.h"


#include "work_lhs.h"


#include "timers.h"


#include <openacc.h>











void y_solve()
{

	int i, j, k, m, n, jsize;





	if (timeron) { timer_start(t_ysolve);}









	jsize = grid_points[1]-1;






	for (k = 1; k <= grid_points[2]-2; k++) {

		for (i = 1; i <= grid_points[0]-2; i++) {

			for (j = 0; j <= jsize; j++) {

				tmp1 = rho_i[k][j][i];

				tmp2 = tmp1 * tmp1;

				tmp3 = tmp1 * tmp2;


				fjac[j][0][0] = 0.0;

				fjac[j][1][0] = 0.0;

				fjac[j][2][0] = 1.0;

				fjac[j][3][0] = 0.0;

				fjac[j][4][0] = 0.0;


				fjac[j][0][1] = -( u[k][j][i][1]*u[k][j][i][2] ) * tmp2;

				fjac[j][1][1] = u[k][j][i][2] * tmp1;

				fjac[j][2][1] = u[k][j][i][1] * tmp1;

				fjac[j][3][1] = 0.0;

				fjac[j][4][1] = 0.0;


				fjac[j][0][2] = -( u[k][j][i][2]*u[k][j][i][2]*tmp2)
				                + c2 * qs[k][j][i];

				fjac[j][1][2] = -c2 *  u[k][j][i][1] * tmp1;

				fjac[j][2][2] = ( 2.0 - c2 ) *  u[k][j][i][2] * tmp1;

				fjac[j][3][2] = -c2 * u[k][j][i][3] * tmp1;

				fjac[j][4][2] = c2;


				fjac[j][0][3] = -( u[k][j][i][2]*u[k][j][i][3] ) * tmp2;

				fjac[j][1][3] = 0.0;

				fjac[j][2][3] = u[k][j][i][3] * tmp1;

				fjac[j][3][3] = u[k][j][i][2] * tmp1;

				fjac[j][4][3] = 0.0;


				fjac[j][0][4] = ( c2 * 2.0 * square[k][j][i] - c1 * u[k][j][i][4] )
				                * u[k][j][i][2] * tmp2;

				fjac[j][1][4] = -c2 * u[k][j][i][1]*u[k][j][i][2] * tmp2;

				fjac[j][2][4] = c1 * u[k][j][i][4] * tmp1
				                - c2 * ( qs[k][j][i] + u[k][j][i][2]*u[k][j][i][2] * tmp2 );

				fjac[j][3][4] = -c2 * ( u[k][j][i][2]*u[k][j][i][3] ) * tmp2;

				fjac[j][4][4] = c1 * u[k][j][i][2] * tmp1;


				njac[j][0][0] = 0.0;

				njac[j][1][0] = 0.0;

				njac[j][2][0] = 0.0;

				njac[j][3][0] = 0.0;

				njac[j][4][0] = 0.0;


				njac[j][0][1] = -c3c4 * tmp2 * u[k][j][i][1];

				njac[j][1][1] =   c3c4 * tmp1;

				njac[j][2][1] =   0.0;

				njac[j][3][1] =   0.0;

				njac[j][4][1] =   0.0;


				njac[j][0][2] = -con43 * c3c4 * tmp2 * u[k][j][i][2];

				njac[j][1][2] =   0.0;

				njac[j][2][2] =   con43 * c3c4 * tmp1;

				njac[j][3][2] =   0.0;

				njac[j][4][2] =   0.0;


				njac[j][0][3] = -c3c4 * tmp2 * u[k][j][i][3];

				njac[j][1][3] =   0.0;

				njac[j][2][3] =   0.0;

				njac[j][3][3] =   c3c4 * tmp1;

				njac[j][4][3] =   0.0;


				njac[j][0][4] = -(  c3c4- c1345 ) * tmp3 * (u[k][j][i][1]*u[k][j][i][1])
				                - ( con43 * c3c4- c1345 ) * tmp3 * (u[k][j][i][2]*u[k][j][i][2])
				                - ( c3c4 - c1345 ) * tmp3 * (u[k][j][i][3]*u[k][j][i][3])
				                - c1345 * tmp2 * u[k][j][i][4];


				njac[j][1][4] = (  c3c4 - c1345 ) * tmp2 * u[k][j][i][1];

				njac[j][2][4] = ( con43 * c3c4 - c1345 ) * tmp2 * u[k][j][i][2];

				njac[j][3][4] = ( c3c4 - c1345 ) * tmp2 * u[k][j][i][3];

				njac[j][4][4] = ( c1345 ) * tmp1;

			}





			lhsinit(lhs, jsize);

			for (j = 1; j <= jsize-1; j++) {

				tmp1 = dt * ty1;

				tmp2 = dt * ty2;


				lhs[j][AA][0][0] = -tmp2 * fjac[j-1][0][0]
				                   - tmp1 * njac[j-1][0][0]
				                   - tmp1 * dy1;

				lhs[j][AA][1][0] = -tmp2 * fjac[j-1][1][0]
				                   - tmp1 * njac[j-1][1][0];

				lhs[j][AA][2][0] = -tmp2 * fjac[j-1][2][0]
				                   - tmp1 * njac[j-1][2][0];

				lhs[j][AA][3][0] = -tmp2 * fjac[j-1][3][0]
				                   - tmp1 * njac[j-1][3][0];

				lhs[j][AA][4][0] = -tmp2 * fjac[j-1][4][0]
				                   - tmp1 * njac[j-1][4][0];


				lhs[j][AA][0][1] = -tmp2 * fjac[j-1][0][1]
				                   - tmp1 * njac[j-1][0][1];

				lhs[j][AA][1][1] = -tmp2 * fjac[j-1][1][1]
				                   - tmp1 * njac[j-1][1][1]
				                   - tmp1 * dy2;

				lhs[j][AA][2][1] = -tmp2 * fjac[j-1][2][1]
				                   - tmp1 * njac[j-1][2][1];

				lhs[j][AA][3][1] = -tmp2 * fjac[j-1][3][1]
				                   - tmp1 * njac[j-1][3][1];

				lhs[j][AA][4][1] = -tmp2 * fjac[j-1][4][1]
				                   - tmp1 * njac[j-1][4][1];


				lhs[j][AA][0][2] = -tmp2 * fjac[j-1][0][2]
				                   - tmp1 * njac[j-1][0][2];

				lhs[j][AA][1][2] = -tmp2 * fjac[j-1][1][2]
				                   - tmp1 * njac[j-1][1][2];

				lhs[j][AA][2][2] = -tmp2 * fjac[j-1][2][2]
				                   - tmp1 * njac[j-1][2][2]
				                   - tmp1 * dy3;

				lhs[j][AA][3][2] = -tmp2 * fjac[j-1][3][2]
				                   - tmp1 * njac[j-1][3][2];

				lhs[j][AA][4][2] = -tmp2 * fjac[j-1][4][2]
				                   - tmp1 * njac[j-1][4][2];


				lhs[j][AA][0][3] = -tmp2 * fjac[j-1][0][3]
				                   - tmp1 * njac[j-1][0][3];

				lhs[j][AA][1][3] = -tmp2 * fjac[j-1][1][3]
				                   - tmp1 * njac[j-1][1][3];

				lhs[j][AA][2][3] = -tmp2 * fjac[j-1][2][3]
				                   - tmp1 * njac[j-1][2][3];

				lhs[j][AA][3][3] = -tmp2 * fjac[j-1][3][3]
				                   - tmp1 * njac[j-1][3][3]
				                   - tmp1 * dy4;

				lhs[j][AA][4][3] = -tmp2 * fjac[j-1][4][3]
				                   - tmp1 * njac[j-1][4][3];


				lhs[j][AA][0][4] = -tmp2 * fjac[j-1][0][4]
				                   - tmp1 * njac[j-1][0][4];

				lhs[j][AA][1][4] = -tmp2 * fjac[j-1][1][4]
				                   - tmp1 * njac[j-1][1][4];

				lhs[j][AA][2][4] = -tmp2 * fjac[j-1][2][4]
				                   - tmp1 * njac[j-1][2][4];

				lhs[j][AA][3][4] = -tmp2 * fjac[j-1][3][4]
				                   - tmp1 * njac[j-1][3][4];

				lhs[j][AA][4][4] = -tmp2 * fjac[j-1][4][4]
				                   - tmp1 * njac[j-1][4][4]
				                   - tmp1 * dy5;


				lhs[j][BB][0][0] = 1.0
				                   + tmp1 * 2.0 * njac[j][0][0]
				                   + tmp1 * 2.0 * dy1;

				lhs[j][BB][1][0] = tmp1 * 2.0 * njac[j][1][0];

				lhs[j][BB][2][0] = tmp1 * 2.0 * njac[j][2][0];

				lhs[j][BB][3][0] = tmp1 * 2.0 * njac[j][3][0];

				lhs[j][BB][4][0] = tmp1 * 2.0 * njac[j][4][0];


				lhs[j][BB][0][1] = tmp1 * 2.0 * njac[j][0][1];

				lhs[j][BB][1][1] = 1.0
				                   + tmp1 * 2.0 * njac[j][1][1]
				                   + tmp1 * 2.0 * dy2;

				lhs[j][BB][2][1] = tmp1 * 2.0 * njac[j][2][1];

				lhs[j][BB][3][1] = tmp1 * 2.0 * njac[j][3][1];

				lhs[j][BB][4][1] = tmp1 * 2.0 * njac[j][4][1];


				lhs[j][BB][0][2] = tmp1 * 2.0 * njac[j][0][2];

				lhs[j][BB][1][2] = tmp1 * 2.0 * njac[j][1][2];

				lhs[j][BB][2][2] = 1.0
				                   + tmp1 * 2.0 * njac[j][2][2]
				                   + tmp1 * 2.0 * dy3;

				lhs[j][BB][3][2] = tmp1 * 2.0 * njac[j][3][2];

				lhs[j][BB][4][2] = tmp1 * 2.0 * njac[j][4][2];


				lhs[j][BB][0][3] = tmp1 * 2.0 * njac[j][0][3];

				lhs[j][BB][1][3] = tmp1 * 2.0 * njac[j][1][3];

				lhs[j][BB][2][3] = tmp1 * 2.0 * njac[j][2][3];

				lhs[j][BB][3][3] = 1.0
				                   + tmp1 * 2.0 * njac[j][3][3]
				                   + tmp1 * 2.0 * dy4;

				lhs[j][BB][4][3] = tmp1 * 2.0 * njac[j][4][3];


				lhs[j][BB][0][4] = tmp1 * 2.0 * njac[j][0][4];

				lhs[j][BB][1][4] = tmp1 * 2.0 * njac[j][1][4];

				lhs[j][BB][2][4] = tmp1 * 2.0 * njac[j][2][4];

				lhs[j][BB][3][4] = tmp1 * 2.0 * njac[j][3][4];

				lhs[j][BB][4][4] = 1.0
				                   + tmp1 * 2.0 * njac[j][4][4]
				                   + tmp1 * 2.0 * dy5;


				lhs[j][CC][0][0] =  tmp2 * fjac[j+1][0][0]
				                   - tmp1 * njac[j+1][0][0]
				                   - tmp1 * dy1;

				lhs[j][CC][1][0] =  tmp2 * fjac[j+1][1][0]
				                   - tmp1 * njac[j+1][1][0];

				lhs[j][CC][2][0] =  tmp2 * fjac[j+1][2][0]
				                   - tmp1 * njac[j+1][2][0];

				lhs[j][CC][3][0] =  tmp2 * fjac[j+1][3][0]
				                   - tmp1 * njac[j+1][3][0];

				lhs[j][CC][4][0] =  tmp2 * fjac[j+1][4][0]
				                   - tmp1 * njac[j+1][4][0];


				lhs[j][CC][0][1] =  tmp2 * fjac[j+1][0][1]
				                   - tmp1 * njac[j+1][0][1];

				lhs[j][CC][1][1] =  tmp2 * fjac[j+1][1][1]
				                   - tmp1 * njac[j+1][1][1]
				                   - tmp1 * dy2;

				lhs[j][CC][2][1] =  tmp2 * fjac[j+1][2][1]
				                   - tmp1 * njac[j+1][2][1];

				lhs[j][CC][3][1] =  tmp2 * fjac[j+1][3][1]
				                   - tmp1 * njac[j+1][3][1];

				lhs[j][CC][4][1] =  tmp2 * fjac[j+1][4][1]
				                   - tmp1 * njac[j+1][4][1];


				lhs[j][CC][0][2] =  tmp2 * fjac[j+1][0][2]
				                   - tmp1 * njac[j+1][0][2];

				lhs[j][CC][1][2] =  tmp2 * fjac[j+1][1][2]
				                   - tmp1 * njac[j+1][1][2];

				lhs[j][CC][2][2] =  tmp2 * fjac[j+1][2][2]
				                   - tmp1 * njac[j+1][2][2]
				                   - tmp1 * dy3;

				lhs[j][CC][3][2] =  tmp2 * fjac[j+1][3][2]
				                   - tmp1 * njac[j+1][3][2];

				lhs[j][CC][4][2] =  tmp2 * fjac[j+1][4][2]
				                   - tmp1 * njac[j+1][4][2];


				lhs[j][CC][0][3] =  tmp2 * fjac[j+1][0][3]
				                   - tmp1 * njac[j+1][0][3];

				lhs[j][CC][1][3] =  tmp2 * fjac[j+1][1][3]
				                   - tmp1 * njac[j+1][1][3];

				lhs[j][CC][2][3] =  tmp2 * fjac[j+1][2][3]
				                   - tmp1 * njac[j+1][2][3];

				lhs[j][CC][3][3] =  tmp2 * fjac[j+1][3][3]
				                   - tmp1 * njac[j+1][3][3]
				                   - tmp1 * dy4;

				lhs[j][CC][4][3] =  tmp2 * fjac[j+1][4][3]
				                   - tmp1 * njac[j+1][4][3];


				lhs[j][CC][0][4] =  tmp2 * fjac[j+1][0][4]
				                   - tmp1 * njac[j+1][0][4];

				lhs[j][CC][1][4] =  tmp2 * fjac[j+1][1][4]
				                   - tmp1 * njac[j+1][1][4];

				lhs[j][CC][2][4] =  tmp2 * fjac[j+1][2][4]
				                   - tmp1 * njac[j+1][2][4];

				lhs[j][CC][3][4] =  tmp2 * fjac[j+1][3][4]
				                   - tmp1 * njac[j+1][3][4];

				lhs[j][CC][4][4] =  tmp2 * fjac[j+1][4][4]
				                   - tmp1 * njac[j+1][4][4]
				                   - tmp1 * dy5;

			}



















			binvcrhs( lhs[0][BB], lhs[0][CC], rhs[k][0][i] );






			for (j = 1; j <= jsize-1; j++) {






				matvec_sub(lhs[j][AA], rhs[k][j-1][i], rhs[k][j][i]);





				matmul_sub(lhs[j][AA], lhs[j-1][CC], lhs[j][BB]);






				binvcrhs( lhs[j][BB], lhs[j][CC], rhs[k][j][i] );

			}





			matvec_sub(lhs[jsize][AA], rhs[k][jsize-1][i], rhs[k][jsize][i]);







			matmul_sub(lhs[jsize][AA], lhs[jsize-1][CC], lhs[jsize][BB]);





			binvrhs( lhs[jsize][BB], rhs[k][jsize][i] );








			for (j = jsize-1; j >= 0; j--) {

				for (m = 0; m < BLOCK_SIZE; m++) {

					for (n = 0; n < BLOCK_SIZE; n++) {

						rhs[k][j][i][m] = rhs[k][j][i][m]
						                  - lhs[j][CC][n][m]*rhs[k][j+1][i][n];

					}

				}

			}

		}

	}

	if (timeron) { timer_stop(t_ysolve);}

}

