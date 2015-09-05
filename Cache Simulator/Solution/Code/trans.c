/* Abhishek garai
agarai
 * trans.c - Matrix transpose B = A^T
 *
 * Each transpose function must have a prototype of the form:
 * void trans(int M, int N, int A[N][M], int B[M][N]);
 *
 * A transpose function is evaluated by counting the number of misses
 * on a 1KB direct mapped cache with a block size of 32 bytes.
 */ 
#include <stdio.h>
#include "cachelab.h"
#include "contracts.h"

int is_transpose(int M, int N, int A[N][M], int B[M][N]);

/* 
 * transpose_submit - This is the solution transpose function that you
 *     will be graded on for Part B of the assignment. Do not change
 *     the description string "Transpose submission", as the driver
 *     searches for that string to identify the transpose function to
 *     be graded. The REQUIRES and ENSURES from 15-122 are included
 *     for your convenience. They can be removed if you like.
 */
char transpose_submit_desc[] = "Transpose submission";
void transpose_submit(int M, int N, int A[N][M], int B[M][N])
{
    REQUIRES(M > 0);
    REQUIRES(N > 0);
	if(N==32 && M==32)
	{
		int temp1=0, temp2 = 0;
		for(int i=0;i<N;i+=8)/*each row*/
		{
			for(int j=0;j<M;j+=8)/*each column*/
			{
				for(int i1=i;i1<i+8;i1++)/*inner block row*/
				{
					for(int j1=j;j1<j+8;j1++)/*inner block column*/
					{
						if(i1 != j1)/*if not diagonal*/
						{
							B[j1][i1] = A[i1][j1];
						}
						else/*else for that row store the diagonal*/
						{
							temp1 = A[i1][j1];
							temp2 = i1;
						}
					}
					if(i == j)/*check the diagonal*/
					{
						B[temp2][temp2] = temp1;
					}
				}
			}
		}
	}
	else
	{
		
		if(N==64 && M==64)
		{
			int temp3=0, temp4=0;
			for(int i=0;i<M;i=i+8)/*each row*/
			{
				for(int j=0;j<N;j=j+8)
				{
					for(int j1=j;j1<j+8;j1+=4)/*each inner row*/
					{
						for(int i1=i;i1<i+8;i1+=4)
						{
							for(int i2=i1;i2<i1+4;i2++)/*each row within the inner block*/
							{
								for(int j2=j1;j2<j1+4;j2++)
								{
									if(i2 != j2)
									{
										B[j2][i2] = A[i2][j2];
									}
									else
									{
										temp3 = A[i2][j2];
										temp4 = i2;
									}
								}
								if(i1 == j1)
								{
									B[temp4][temp4] = temp3;
								}
							}
						}	
					}
				}
			}
		}
		else
		{
			int temp5=0, temp6=0;
			for(int i=0;i<N;i=i+16)/*outer 16 block size*/
			{
				for(int j=0;j<M;j=j+16)
				{
					for(int i1=i;i1<i+16;i1++)
					{
						if(i1<N)/*check column of result should not exceed row*/
						{
							for(int j1=j;j1<j+16;j1++)
							{
								if(j1<M)/*check row of result should not exceed column*/
								{
									if(i1 != j1)
										{
											B[j1][i1] = A[i1][j1];
										}
										else
										{
											temp5 = A[i1][j1];
											temp6 = i1;
										}
								}
								else
								{
									continue;
								}
							}
							if(i == j)
							{	
								B[temp6][temp6] = temp5;
							}
						}
						else
						{
							continue;
						}
					}
				}
			}
		}
	}
    ENSURES(is_transpose(M, N, A, B));
}

/* 
 * You can define additional transpose functions below. We've defined
 * a simple one below to help you get started. 
 */ 

/* 
 * trans - A simple baseline transpose function, not optimized for the cache.
 */
char trans_desc[] = "Simple row-wise scan transpose";
void trans(int M, int N, int A[N][M], int B[M][N])
{
    int i, j, tmp;

    REQUIRES(M > 0);
    REQUIRES(N > 0);

    for (i = 0; i < N; i++) {
        for (j = 0; j < M; j++) {
            tmp = A[i][j];
            B[j][i] = tmp;
        }
    }    

    ENSURES(is_transpose(M, N, A, B));
}

/*
 * registerFunctions - This function registers your transpose
 *     functions with the driver.  At runtime, the driver will
 *     evaluate each of the registered functions and summarize their
 *     performance. This is a handy way to experiment with different
 *     transpose strategies.
 */
void registerFunctions()
{
    /* Register your solution function */
    registerTransFunction(transpose_submit, transpose_submit_desc); 

    /* Register any additional transpose functions */
    registerTransFunction(trans, trans_desc); 

}

/* 
 * is_transpose - This helper function checks if B is the transpose of
 *     A. You can check the correctness of your transpose by calling
 *     it before returning from the transpose function.
 */
int is_transpose(int M, int N, int A[N][M], int B[M][N])
{
    int i, j;

    for (i = 0; i < N; i++) {
        for (j = 0; j < M; ++j) {
            if (A[i][j] != B[j][i]) {
                return 0;
            }
        }
    }
    return 1;
}

