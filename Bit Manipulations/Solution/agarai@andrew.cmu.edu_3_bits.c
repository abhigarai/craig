/* 
 * CS:APP Data Lab 
 * 
 * <Please put your name and userid here>
 * 
 * bits.c - Source file with your solutions to the Lab.
 *          This is the file you will hand in to your instructor.
 *
 * WARNING: Do not include the <stdio.h> header; it confuses the dlc
 * compiler. You can still use printf for debugging without including
 * <stdio.h>, although you might get a compiler warning. In general,
 * it's not good practice to ignore compiler warnings, but in this
 * case it's OK.  
 */

#if 0
/*
 * Instructions to Students:
 *
 * STEP 1: Read the following instructions carefully.
 */

You will provide your solution to the Data Lab by
editing the collection of functions in this source file.

INTEGER CODING RULES:
 
  Replace the "return" statement in each function with one
  or more lines of C code that implements the function. Your code 
  must conform to the following style:
 
  int Funct(arg1, arg2, ...) {
      /* brief description of how your implementation works */
      int var1 = Expr1;
      ...
      int varM = ExprM;

      varJ = ExprJ;
      ...
      varN = ExprN;
      return ExprR;
  }

  Each "Expr" is an expression using ONLY the following:
  1. Integer constants 0 through 255 (0xFF), inclusive. You are
      not allowed to use big constants such as 0xffffffff.
  2. Function arguments and local variables (no global variables).
  3. Unary integer operations ! ~
  4. Binary integer operations & ^ | + << >>
    
  Some of the problems restrict the set of allowed operators even further.
  Each "Expr" may consist of multiple operators. You are not restricted to
  one operator per line.

  You are expressly forbidden to:
  1. Use any control constructs such as if, do, while, for, switch, etc.
  2. Define or use any macros.
  3. Define any additional functions in this file.
  4. Call any functions.
  5. Use any other operations, such as &&, ||, -, or ?:
  6. Use any form of casting.
  7. Use any data type other than int.  This implies that you
     cannot use arrays, structs, or unions.

 
  You may assume that your machine:
  1. Uses 2s complement, 32-bit representations of integers.
  2. Performs right shifts arithmetically.
  3. Has unpredictable behavior when shifting an integer by more
     than the word size.

EXAMPLES OF ACCEPTABLE CODING STYLE:
  /*
   * pow2plus1 - returns 2^x + 1, where 0 <= x <= 31
   */
  int pow2plus1(int x) {
     /* exploit ability of shifts to compute powers of 2 */
     return (1 << x) + 1;
  }

  /*
   * pow2plus4 - returns 2^x + 4, where 0 <= x <= 31
   */
  int pow2plus4(int x) {
     /* exploit ability of shifts to compute powers of 2 */
     int result = (1 << x);
     result += 4;
     return result;
  }

FLOATING POINT CODING RULES

For the problems that require you to implent floating-point operations,
the coding rules are less strict.  You are allowed to use looping and
conditional control.  You are allowed to use both ints and unsigneds.
You can use arbitrary integer and unsigned constants.

You are expressly forbidden to:
  1. Define or use any macros.
  2. Define any additional functions in this file.
  3. Call any functions.
  4. Use any form of casting.
  5. Use any data type other than int or unsigned.  This means that you
     cannot use arrays, structs, or unions.
  6. Use any floating point data types, operations, or constants.


NOTES:
  1. Use the dlc (data lab checker) compiler (described in the handout) to 
     check the legality of your solutions.
  2. Each function has a maximum number of operators (! ~ & ^ | + << >>)
     that you are allowed to use for your implementation of the function. 
     The max operator count is checked by dlc. Note that '=' is not 
     counted; you may use as many of these as you want without penalty.
  3. Use the btest test harness to check your functions for correctness.
  4. Use the BDD checker to formally verify your functions
  5. The maximum number of ops for each function is given in the
     header comment for each function. If there are any inconsistencies 
     between the maximum ops in the writeup and in this file, consider
     this file the authoritative source.

/*
 * STEP 2: Modify the following functions according the coding rules.
 * 
 *   IMPORTANT. TO AVOID GRADING SURPRISES:
 *   1. Use the dlc compiler to check that your solutions conform
 *      to the coding rules.
 *   2. Use the BDD checker to formally verify that your solutions produce 
 *      the correct answers.
 */


#endif
/* 
 * bitOr - x|y using only ~ and & 
 *   Example: bitOr(6, 5) = 7
 *   Legal ops: ~ &
 *   Max ops: 8
 *   Rating: 1
 */
int bitOr(int x, int y) {
  /*The idea is to first negate it to get 0s in 1s and AND them together to  get 0s instead of 1s in the final res ult and again negate it */
  return ~(~x&~y);
}

/* 
 * tmin - return minimum two's complement integer 
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 4
 *   Rating: 1
 */
int tmin(void) {
  /* tmin is the negation MSB set to 1 and rest all zeroes  */
  return 0x1<<31;
}
/* 
 * negate - return -x 
 *   Example: negate(1) = -1.
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 5
 *   Rating: 2
 */
int negate(int x) {
  /* 2s complement logic  */
  return (~x+1);
}
/* 
 * getByte - Extract byte n from word x
 *   Bytes numbered from 0 (LSB) to 3 (MSB)
 *   Examples: getByte(0x12345678,1) = 0x56
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 6
 *   Rating: 2
 */
int getByte(int x, int n) {
  /* left shifting n by 3 has an effect of multiplicationc by 8, thus giving the number of bits from the right for that byte, shifting right takes that byte to the first and ANDing  with FF keeps that byes removing the rest  */
  return (x >> (n << 3)) & 0xFF;
}
/* 
 * divpwr2 - Compute x/(2^n), for 0 <= n <= 30
 *  Round toward zero
 *   Examples: divpwr2(15,1) = 7, divpwr2(-33,4) = -2
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 15
 *   Rating: 2
 */
int divpwr2(int x, int n) {
    /* idea is to subtract one from divisor intially for taking care of exact divisibiity i.e. it rounds to away from zero in case of negative to bring that to floor, AND it with signx for getting the adjusted value for negative numbers, positive rounds properly and roundadjust value is zero*/
    int divminusone = ((1<<n)+(~0));
    int signx = (x>>31); 
    int roundadjust = (signx & divminusone);
    int temp = x + roundadjust; 
    return temp >> n;
}
/* 
 * logicalShift - shift x to the right by n, using a logical shift
 *   Can assume that 0 <= n <= 31
 *   Examples: logicalShift(0x87654321,4) = 0x08765432
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 20
 *   Rating: 3 
 */
int logicalShift(int x, int n) {
  /* 1 << 31 makes the 1st bit one for right shifting using it, right shifting by n makes n+1 bits as 1s, thus left shifting by 1 to compensate, negating this creates the mask
 *  ANDing this mask with the arithmetic shift gives the result  */
  int mask = ((0x1 << 31) >> (n)) << 1;
  return (x>>n)& ~mask;
}
/* 
 * isPositive - return 1 if x > 0, return 0 otherwise 
 *   Example: isPositive(-1) = 0.
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 8
 *   Rating: 3
 */
int isPositive(int x) {
    /* first condition  inside tests for negative numbers and the second condition for non-zero number, if either of them is true, ORing returns true and we bang it to negate the result            ANDing 1<<31 with number gives zero for non-negative and non-zero for rest, so we double bang it to return 0 for negative and 1 for positive, second condition is direct bang appl*/
    return !(!!(x&(1<<31)) | !x );
}
/* 
 * isLess - if x < y  then return 1, else return 0 
 *   Example: isLess(4,5) = 1.
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max opis: 24
 *   Rating: 3
 */
int isLess(int x, int y) {
  /* idea is to find the sign of x and y, check whether they are same or not ,if yes find the sign of the result and if not check the sign of x and return the resukt accordingly */  
  int signx = x >> 31;
  int signy = y >> 31;
  int samesign = (signx^signy);
  int negy = ((~y)+1);
  int res = x + negy;
  int signres = res >> 31;
  return  !!(((~samesign) & signres )|(samesign & signx)); 
}
/* 
 * bang - Compute !x without using !
 *   Examples: bang(3) = 0, bang(0) = 1
 *   Legal ops: ~ & ^ | + << >>
 *   Max ops: 12
 *   Rating: 4 
 */
int bang(int x) {
  /* idea is that for any number either the number is negative or its negation is negative thus giving the sign bit as 1 for either case, thus ORing would set the sign bit and shifting and ADDing 1 gives the result */
  return ((x | (~x + 1)) >> 31) + 1;
}
/*
 * isPower2 - returns 1 if x is a power of 2, and 0 otherwise
 *   Examples: isPower2(5) = 0, isPower2(8) = 1, isPower2(0) = 0
 *   Note that no negative number is a power of 2.
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 20
 *   Rating: 4
 */
int isPower2(int x) {
  /* we need to handle the cases of tmin, normal cases and zero, tmin we left shift by 1 double bang it becoz negative numbers are not allowed, zero is laos handled here, rest we subtract 1 and ANDing should give 0, thus negating the result */
  return (!!(x<<1)) & !(x & (x+(~0x0)));
}
/*
 * ilog2 - return floor(log base 2 of x), where x > 0
 *   Example: ilog2(16) = 4
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 90
 *   Rating: 4
 */
int ilog2(int x) {
  /* Basically the idea is counting the greatest bit position becuase log base 2 is number of times i need to divide a number to make it a zero*/ 
    int a,b,c,d,e;
  /* these operations make all bits from the leading bit set to one  */  
    x = x | (x >> 1);
    x = x | (x >> 2);
    x = x | (x >> 4);
    x = x | (x >> 8);
    x = x | (x >> 16); 
    
    /* now log base 2 can be calculated by counting the number of 1s in the bit pattern because that many shifts will make it zero*/
    a = 0x55 | (0x55 << 8);
    a = a | (a << 16);
    /* generate 55555555*/ 

    b = 0x33 | (0x33 << 8);
    b = b | (b << 16);
    /*generate 33333333*/

    c = 0x0f | (0x0f << 8);
    c = c | (c << 16);
    /* generate 0F0F0F0F*/
 
    d = 0xff | (0xff << 16);
    /*generate 00FF00FF*/
  
    e = 0xff | (0xff << 8);
    /* generate 0000FFFF*/

    x = (x & a) + ((x >> 1) & a);/*count number of 1s in every 2 bits*/
    x = (x & b) + ((x >> 2) & b);/*count 1s in every 4 bits*/
    x = (x & c) + ((x >> 4) & c);/* count 1s in every 8 bits*/
    x = (x & d) + ((x >> 8) & d);/*count 1s in every 16  bits*/
    x = (x & e) + ((x >> 16) & e);/* count 1s in 32 bits*/
    x = x + ~0;/* subtract one to get the correct position*/
    return x;
} 
/* float_half - Return bit-level equivalent of expression 0.5*f for
 *   floating point argument f.
 *   Both the argument and result are passed as unsigned int's, but
 *   they are to be interpreted as the bit-level representation of
 *   single-precision floating point values.
 *   When argument is NaN, return argument
 *   Legal ops: Any integer/unsigned operations incl. ||, &&. also if, while
 *   Max ops: 30
 *   Rating: 4
 */
unsigned float_half(unsigned uf) {
 unsigned s;
 unsigned mf;
 unsigned e;
 unsigned res;
 int lastbit;
 s = uf & 0x80000000;
 e = uf & 0x7F800000;
 mf = uf & 0x007FFFFF;
 lastbit=((mf & 0x00000001) == 0x00000001);
 if(e == 0x7F800000)
 {
 if((mf == 0x0)) /* case of infinity  */
 {
  res = 0x7F800000; 
 }
 else
 {
  /* case of NAN  */
  res = uf;
 }
 }
 else
 {
 if(e == 0x0)/* denormalised to denormalised */
 { 
   if(lastbit) /* last bit one */
   {
     mf = mf >> 1; /* throw away last bit */
     if((mf & 0x00000001) == 0x00000001) /* second last bit is 1 */
     {
	mf = mf + 0x00000001;/* add 1 */
     }
  }
  else
  {
    mf = mf >> 1; /* jsut throw away */
  }
 }
 else/* normalised*/
 {
        if(e==0x00800000) /* to denormalised*/
	{
           e= 0x0; /* make e  0 for denormalised*/
           /* rounding logic */
           if(lastbit)
	   {
             mf = mf >> 1;
             if((mf & 0x1)==0x1)
	     {
               mf = mf + 0x00000001;
	     }
           }
           else
	   {
      	     mf = mf >> 1;
           }
         mf = mf + 0x00400000;/* add one for leading 1 adjustment */
	}
	else/* normalised to normalised reduce exponent */
	{
	  e = e - 0x00800000;
	}
 } 
 }
 res = s+e+mf;/* add all for the result */
 return res;
}
/* 
 * float_i2f - Return bit-level equivalent of expression (float) x
 	*   Result is returned as unsigned int, but
 *   it is to be interpreted as the bit-level representation of a
 *   single-precision floating point values.
 *   Legal ops: Any integer/unsigned operations incl. ||, &&. also if, while
 *   Max ops: 30
 *   Rating: 4
 */
unsigned float_i2f(int x) {
 unsigned res;
 unsigned abs; 
 unsigned s;
 unsigned e;
 unsigned e1;
 unsigned mf;
 unsigned temp;
 unsigned temp1;
 unsigned temp2;
 unsigned incr;
 unsigned temp3;
 unsigned final;
 unsigned mask;
 unsigned m;
 unsigned countTemp;
 unsigned countTemp1;
 int count;
 int condition;
 count = 0xFFFFFFFF;/*initilize with -1*/
 mask = 0x007FFFFF;/* mask for fraction*/ 
 temp = 0x80000000;/* mask for sign*/
 /* check sign and adjsut the sign bit also find the absolute using 2s complement*/
 if((x >> 31)==0xFFFFFFFF)
 {
  s = temp;
  abs = ((~x) + 1);
 } 
 else
 {
  s = 0;
  abs = x;
 }
 temp1 = abs;
 /* calculate the exponent*/
 while(abs > 0)
 { 
  abs = abs >> 1;
  count++;
 }
 countTemp = count + 127;
 e= countTemp << 23;
 m = (temp1 << (32 - count));/* fraction to be rounded to 23 bits*/
 if(temp1==0x0)/* if 0 */
 {
  res= 0;
 }
 else
 {
  if(m==0)/*  if mantissa 0*/
  {
   mf=0;
  }
  else
  {
   if(count <= 23)/* within 23 bits*/
   {
    mf=m;
   }
   else
   {
    final = m & 0xFFFFFE00;/* top 23 bits of mantissa*/
    temp2 = m << 22;/* keep leading bit for rounding decision*/
    temp3 = (temp2 << 1);/* bits after 23 */
    incr = (final)+0x00000200;/* incremented value of fraction*/
    countTemp1 = countTemp + 1;
    e1 = countTemp1 << 23;/* incremented exponent*/
    condition = (incr ==0);
    /* rounding logic as above problem*/
     if(temp3==temp)
     {		
      if((temp2 & temp)==temp)
      {
           if(condition)
           {
             mf = 0;
             e = e1;
           }
           else
           {
             mf=incr;
          } 
      }
      else
      {
       mf=final;
      }  
     }
     else
     {
      if(temp3 > temp)
      {
       if(condition)
       {
         mf = 0;
         e  = e1;
       }
       else
       {
        mf = incr;
       }
      }
      else
      {      
       mf = final;
      }
     }
    }
  } 	
  mf = (mf >> 9) & (mask);/* adjust the bits in result*/ 
  res=(s+e+mf);
 }
 return res;
}
