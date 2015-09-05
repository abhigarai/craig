/*

Abhishek Garai 
Andrew Id: agarai

Block structure:

Every block is structured as below:
1. It consists of a header containing details about the size of the block 
and the detail about whether it is allocated or free. The macro PACK() 
is used to store this detail implementation for which is same as that 
given in the text.

2.The block also contains a footer maintaining the same information i.e. 
allocation and size.

3. Further, within the free block two pointers are maintained pointing to 
the predecessor and successor nodes and if the block is allocated this pointers 
are overwritten with user data. Predecessor and the 
successor node points to the previous and next block respectively.

4. So, based on the above block structure a minimum block size of 24 bytes 
is required 2*8 bytes for the pointers to previous and next block and 
2*4 bytes for the header and the footer i.e. a total 24 bytes.

5. Further a segregated list is used starting from 24 bytes block size 
as that is the minimum block size required as per the design and moving 
upto 65536 bytes as mem_sbrk allocates memory 
in chunks of multiples of 4096 when asked for memory from the kernel.
So, the number of partitions that exist are 14 based on the above criteria.
The maximum size was experimented and reached to near optimal.
Also, the increment is in multiples of 2 i.e. 24, 32, 64 except the first one
which is based on minimum block size.

6. Pointers to the first block of every free-list is maintained in pointers 
to the first free block to be stored of that particular size. So there are 
14 pointers used.

7. first-fit is used to insert a free block within the free-list that is
 returned by a function part_indx() that returns the index of the 
proper fit in the free-list based on the size of the free 
pointer passed to it.

8. Also, the free block insertion is done at the beginning of the block.
So, effectively its First In Last Out policy that is used.

9. Further functions are described in block statements before the implementation.

10. A chunksize of 280 is used. This was to improve the performance score 
based on memory utilization so that less memory is wasted while allocating.
The exact number used was based on trial and error.
 */
 
#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "mm.h"
#include "memlib.h"

/* If you want debugging output, use the following macro.  When you hand
 * in, remove the #define DEBUG line. */
#define DEBUG
#ifdef DEBUG
# define dbg_printf(...) printf(__VA_ARGS__)
#else
# define dbg_printf(...)
#endif


/* do not change the following! */
#ifdef DRIVER
/* create aliases for driver tests */
#define malloc mm_malloc
#define free mm_free
#define realloc mm_realloc
#define calloc mm_calloc
#endif /* def DRIVER */

/* single word (4) or double word (8) alignment */
#define ALIGNMENT 8

/* rounds up to the nearest multiple of ALIGNMENT */
#define ALIGN(p) (((size_t)(p) +(ALIGNMENT-1)) & ~0x7)

#define SIZE_T_SIZE (ALIGN(sizeof(size_t)))

#define SIZE_PTR(p)  ((size_t*)(((char*)(p)) - SIZE_T_SIZE))

/* Basic constants and macros */
#define WSIZE 4 /* Word and header/footer size (bytes) */
#define DSIZE 8 /* Double word size (bytes) */
#define CHUNKSIZE 280 /* Extend heap by this amount (bytes) */  
#define PARTITIONS	14  	

#define MAX(x, y) ((x) > (y) ? (x) : (y))

/* Pack a size and allocated bit into a word */
#define PACK(size, alloc)  ((size) | (alloc)) 

/* Read and write a word at address p */
#define GET(p)       (*(unsigned int *)(p))            
#define PUT(p, val)  (*(unsigned int *)(p) = (val))    

/* Read the size and allocated fields from address p */
#define GET_SIZE(p)  (GET(p) & ~0x7)                   
#define GET_ALLOC(p) (GET(p) & 0x1)                    

/* Given block ptr bp, compute address of its header and footer */
#define HDRP(bp)       ((char *)(bp) - WSIZE)                      
#define FTRP(bp)       ((char *)(bp) + GET_SIZE(HDRP(bp)) - DSIZE)

/* Given block ptr bp, compute address of next and previous blocks */
#define NEXT_BLKP(bp)  ((char *)(bp) + GET_SIZE(((char *)(bp) - WSIZE))) 
#define PREV_BLKP(bp)  ((char *)(bp) - GET_SIZE(((char *)(bp) - DSIZE))) 

/* init-list is used to initialise the 
   list values to NULL within mm_init() */
#define INIT_LIST(list,i) (*(void **)(list + (i*DSIZE)) = NULL)

/* Next and Previous block pointers are found traversing the list */
#define LIST_INDX_NEXT_BLKP(bp)  			(*(void **)(bp)) 
#define LIST_INDX_PREV_BLKP(bp)  			(*(void **)(bp + DSIZE)) 

/* Store values in the next and the previous blocks respectively 
    given pointers to them */
#define STORE_NEXT_BLKP(bp, bp1) 	(*(void **)(bp) = bp1)
#define STORE_PREV_BLKP(bp, bp1)  	(*(void **)(bp + DSIZE) = bp1)


/* Variables and function prototypes */	
static char *heap_listp = 0;
void *list0;
void *list1;
void *list2;
void *list3;
void *list4;
void *list5;
void *list6;
void *list7;
void *list8;
void *list9;
void *list10;
void *list11;
void *list12;
void *list13;
static void **LIST_INDX(int i);
static void *extend_heap(size_t words);
static void *find_fit(size_t asize);
static void place(void *bp, size_t asize);
static int part_indx(size_t blksize);
static void *coalesce(void *bp);
static void add(void *bp);
static void delete(void *bp);
static void check(void *bp);
static void print(void *bp);
static void loop();


/*
 * mm_init - Called when a new trace starts. 
 * CAUTION: You must reset all of your global pointers here.
 **************************************************************
 * The implementation is similar to the implicit list in text
 * added with the initial pointers to the segregated 
 * blocks for individual partitions and initialised to NULL.
 * Also, the heap is not extended initially unless explicitly 
 * required by call to extend_heap function from malloc().
 */
int mm_init(void) 
{
	list0=NULL;
	list1=NULL;
	list2=NULL;
	list3=NULL;
	list4=NULL;
	list5=NULL;
	list6=NULL;
	list7=NULL;
	list8=NULL;
	list9=NULL;
	list10=NULL;
	list11=NULL;
	list12=NULL;
	list13=NULL;
    /* Create the initial empty heap */
    if ((heap_listp = mem_sbrk(4*WSIZE)) == (void *)-1)
	   return -1; 

    PUT(heap_listp, 0);                          /* Alignment padding */
    PUT(heap_listp + (1*WSIZE), PACK(DSIZE, 1)); /* Prologue header */ 
    PUT(heap_listp + (2*WSIZE), PACK(DSIZE, 1)); /* Prologue footer */ 
    PUT(heap_listp + (3*WSIZE), PACK(0, 1));     /* Epilogue header */
    heap_listp += (2*WSIZE);

    return 0;
}

/*
 * malloc - Allocate a block by incrementing the brk pointer.
 *      Always allocate a block whose size is a multiple of the alignment.
 ***************************************************************************
 *      implementation is similar to text added with the logic that whenever
 *      whenever a fit is found by findfit(), a call to delete removes the 
 *      block from free-list immediately i.e. basically before calling 
 *      place().
 */
void *malloc (size_t size) {
    size_t asize;    	/* Adjusted block size */
    size_t extendsize; 	/* Amount to extend heap if no fit */
    char *bp;

    /* Ignore spurious requests */
    if (size == 0)
    	return NULL;

	if ( size <= DSIZE )
        asize = 3*DSIZE;
    else
        asize = DSIZE * ((size + (DSIZE) + (DSIZE-1)) / DSIZE);
   
    /* Search the free list for a fit */
    if ((bp = find_fit(asize)) != NULL) {  
		delete(bp);
    	place(bp, asize);
		return bp;
    } 

    /* No fit found. Get more memory and place the block */
	extendsize = MAX(asize, CHUNKSIZE);
	if ((bp = extend_heap(extendsize/WSIZE)) == NULL)  
	   	return NULL;
    
	delete(bp);	
	place(bp, asize);  
    return bp;
}
/*
 * free - We don't know how to free a block.  So we ignore this call.
 *      Computers have big memories; surely it won't be a problem.
 *********************************************************************
 *      Implementation same as text added with the logic that whenever 
 *      pointer is 0 it just returns.
 */
void free (void *ptr) {
    if (ptr == 0) 
    	return;
	else
	{
		size_t size = GET_SIZE(HDRP(ptr));

		PUT(HDRP(ptr), PACK(size, 0));
		PUT(FTRP(ptr), PACK(size, 0));
		coalesce(ptr);
	}
	/*mm_checkheap(1)*/
}

/* IMPLEMENTATION REMAINS THE SAME AS GIVEN IN mm_naive.c
 * realloc - Change the size of the block by mallocing a new block,
 *      copying its data, and freeing the old block.  I'm too lazy
 *      to do better.
 */
void *realloc (void *oldptr, size_t size) 
{
  size_t oldsize;
  void *newptr;

  /* If size == 0 then this is just free, and we return NULL. */
  if(size == 0) {
    free(oldptr);
    return 0;
  }

  /* If oldptr is NULL, then this is just malloc. */
  if(oldptr == NULL) {
    return malloc(size);
  }

  newptr = malloc(size);

  /* If realloc() fails the original block is left untouched  */
  if(!newptr) {
    return 0;
  }

  /* Copy the old data. */
  oldsize = *SIZE_PTR(oldptr);
  if(size < oldsize) oldsize = size;
  memcpy(newptr, oldptr, oldsize);

  /* Free the old block. */
  free(oldptr);

  /*mm_checkheap(1)*/
  
  return newptr;
}

/*IMPLEMENTATION REMAINS THE SAME AS GIVEN IN mm_naive.c
 * calloc - Allocate the block and set it to zero.
 */
void *calloc (size_t nmemb, size_t size) {
  	size_t bytes = nmemb * size;
  	void *newptr;

  	newptr = malloc(bytes);
  	memset(newptr, 0, bytes);

	/*mm_checkheap(1)*/
	
  	return newptr;
}


/* Most of the implementation remains same as for implicit in text with the 
 * below changes:
 * Coalesce() is called when we need to merge two free blocks by checking
 * the next and the previous blocks, so before we return from coalesce()
 * we have a chunk of free block that is added to the list by calling add().
 * Also, depending on whether the previous or next block is free, we need 
 * to delete that block from the free-list as the current block after merging
 * is added back to the free list.
 * And, subsequently modify the current block pointer.
 */
static void *coalesce(void *bp) 
{
    size_t prev_alloc = GET_ALLOC(FTRP(PREV_BLKP(bp)));
    size_t next_alloc = GET_ALLOC(HDRP(NEXT_BLKP(bp)));
    size_t size = GET_SIZE(HDRP(bp));

    if (prev_alloc && next_alloc) {		/* Case 1 */
		// do nothing just add to the free-list and return
    }
    else if (prev_alloc && !next_alloc) {      /* Case 2 */
		delete(NEXT_BLKP(bp));
		
		size += GET_SIZE(HDRP(NEXT_BLKP(bp)));
		PUT(HDRP(bp), PACK(size,0));
		PUT(FTRP(bp), PACK(size,0));
    }
    else if (!prev_alloc && next_alloc) {      /* Case 3 */
    	
		size += GET_SIZE(HDRP(PREV_BLKP(bp)));
		delete(PREV_BLKP(bp)); 
		
		bp = PREV_BLKP(bp);
		
		PUT(HDRP(bp), PACK(size, 0));
		PUT(FTRP(bp), PACK(size, 0));
    }

    else {                                     /* Case 4 */
    	
		size += (GET_SIZE(HDRP(PREV_BLKP(bp))) + 
		     GET_SIZE(FTRP(NEXT_BLKP(bp))));
		
		delete(PREV_BLKP(bp));
    	delete(NEXT_BLKP(bp));
		
		bp = PREV_BLKP(bp);
		
		PUT(HDRP(bp), PACK(size, 0));
		PUT(FTRP(bp), PACK(size, 0));
    }
		add(bp);
		/*mm_checkheap(1)*/
	    return bp;
}
/*
* Implementation same as text.
*/
static void *extend_heap(size_t words) 
{
    char *bp;
    size_t size;

    /* Allocate an even number of words to maintain alignment */
    size = (words % 2) ? (words+1) * WSIZE : words * WSIZE; 
    if ((long)(bp = mem_sbrk(size)) == -1)  
		return NULL;                                        

    /* Initialize free block header/footer and the epilogue header */
    PUT(HDRP(bp), PACK(size, 0));         /* Free block header */
    PUT(FTRP(bp), PACK(size, 0));         /* Free block footer */
    PUT(HDRP(NEXT_BLKP(bp)), PACK(0, 1)); /* New epilogue header */ 

    /* Coalesce if the adjacent blocks are free */
	/*mm_checkheap(1)*/
    return coalesce(bp);
}

/* 
 * The pointer returned by find-fit is to be allocated right now.
 * Since it is already deleted by delete() we basically have the complete
 * removed from free-list. So, here we check the requested block size by 
 * malloc and if its not enough to keep a free block with minimum 
 * we assign the whole block as allocated by putting 1 in allocation bit
 * else we assign only the requested size and free the next having the
 * residue size immediately after it and add it to the free list.
 */
static void place(void *bp, size_t asize)
{
    size_t partition = GET_SIZE(HDRP(bp));
	size_t residue = partition - asize;

	if ((residue < (3*DSIZE))) 
	{ 
		PUT(HDRP(bp), PACK(partition, 1));
		PUT(FTRP(bp), PACK(partition, 1));
	} 
	else 
	{
		PUT(HDRP(bp), PACK(asize, 1));
		PUT(FTRP(bp), PACK(asize, 1));
		
		PUT(HDRP(NEXT_BLKP(bp)), PACK(residue, 0));
		PUT(FTRP(NEXT_BLKP(bp)), PACK(residue, 0));		
		
		add(NEXT_BLKP(bp));
	}	
	/*mm_checkheap(1)*/
}
/*
* The logic is that the call to the part_indx() returns the proper index 
* based on the size value passed. So, for a segment containing 3 & 4 bytes
* blocks and the request comes for 4 byte block and initial list starts 
* currently with a 3 byte block then it returns a block of wrong size to the
* user. So a check is also performed to check the size of the block returned
* and if its less then its returned.
* Certain redundant conditions were put in the code as there were some edge 
* that made the code run into segmentation fault which got removed after
* putting the else statements in the code.
*/
static void *find_fit(size_t asize)
{
	int i=0;
    void *bp;
    void *firstfit = NULL;
	 
    for(i = part_indx(asize); i < PARTITIONS; i++) 
	{		
			bp = *LIST_INDX(i);
			if( bp!=NULL) 
			{
				if(asize <= GET_SIZE(HDRP(bp)))
				{
					firstfit = bp;
					break;
				}
				else
				{
					continue;
				}
			}
			else
			{
				continue;
			}
    }
	/*mm_checkheap(1)*/
    return firstfit;
}

/*
 * The logic of adding the block as mentioned in the block comment is 
 * First In Last Out i.e. whenever a block is freed and put into the 
 * free-list it is inserted at the starting of the list and also while 
 * removing removed from the start of the list.
 * So, while adding their may be i.e. either the list might be empty 
 * or the list might contain some free blocks.
 */
static void add(void *bp){
	int i = part_indx(GET_SIZE(HDRP(bp)));
	void *bp_temp;
	void **temp;
	
	if ((bp_temp=*LIST_INDX(i))!= NULL)
	{ 		
		STORE_PREV_BLKP(bp, NULL);
		STORE_NEXT_BLKP(bp, bp_temp);
		STORE_PREV_BLKP(bp_temp, bp);
	} 
	else 
	{
		STORE_PREV_BLKP(bp, NULL);
		STORE_NEXT_BLKP(bp, NULL);
	}
	temp = LIST_INDX(i);
	*temp = bp;
	/*mm_checkheap(1)*/
}

/*
 * Delete a free block from free list.
 * Here 4 conditions are checked i.e.
 * 1. if its the only block - case 4
 * 2. if its the first block - case 2
 * 3. if its the last block - case 3
 * 4. if its the block in middle - case 1
 */
static void delete(void *bp){
	int i = part_indx(GET_SIZE(HDRP(bp)));
	char *prev = LIST_INDX_PREV_BLKP(bp);
	char *next = LIST_INDX_NEXT_BLKP(bp);
	void **temp;

	STORE_PREV_BLKP(bp, NULL);
	STORE_NEXT_BLKP(bp, NULL);
	
	if (prev != NULL)
	{
		if (next != NULL)
		{
			STORE_PREV_BLKP(next, prev);
			STORE_NEXT_BLKP(prev, next);
		} 
		else 
		{
			STORE_NEXT_BLKP(prev, NULL);
		}
	} 
	else if (next != NULL)
	{
		temp = LIST_INDX(i);
		*temp = next;
		STORE_PREV_BLKP(next, NULL);
	} 
	else 
	{
		temp = LIST_INDX(i);
		*temp = NULL;	
	}
	/*mm_checkheap(1)*/
}

/* This function returns the index of the partition for which the 
   requested block is fit. The partitions are based on the logic 
   that minimum block size is 24 and maximum block size 
   is selected based on the utilization score and was
   increased till a limit. Also, after the first block
   the size increases in multiples of 2.
 */
static int part_indx(size_t asize){
	int part;
	if (asize <= 24)
		part = 0;
	else if (asize <= 32)
		part = 1;
	else if (asize <= 64) 
		part = 2;
	else if (asize <= 128) 
		part = 3;
	else if (asize <= 256) 
		part = 4;
	else if (asize <= 512) 
		part = 5;
	else if (asize <= 1024) 
		part = 6;
	else if (asize <= 2048) 
		part = 7;
	else if (asize <= 4096) 
		part = 8;
	else if (asize <= 8192) 
		part = 9;
	else if (asize <= 16384) 
		part = 10;
	else if (asize <= 32768) 
		part = 11;
	else if (asize <= 65536) 
		part = 12;
	else 		
		part = 13;
	return part;
}

/*
* This function is used to return the pointer to the start
* of the list for a particular size of the swgregated list
* based on the value of the index passed as parameter.
*/
static void **LIST_INDX(int i)
{
	void **ptr;
	switch(i)
	{
		case 0: ptr = &list0; break;
		case 1: ptr = &list1; break;
		case 2: ptr = &list2; break;
		case 3: ptr = &list3; break;
		case 4: ptr = &list4; break;
		case 5: ptr = &list5; break;
		case 6: ptr = &list6; break;
		case 7: ptr = &list7; break;
		case 8: ptr = &list8; break;
		case 9: ptr = &list9; break;
		case 10: ptr = &list10; break;
		case 11: ptr = &list11; break;
		case 12: ptr = &list12; break;
		default: ptr = &list13; break;
	}
	return ptr;
}
/*
 * mm_checkheap - There are no bugs in my code, so I don't need to
 *      check, so nah! (But if I did, I could call this function using
 *      mm_checkheap(__LINE__) to identify the call site.)
 ***********************************************************************
 *
 * IN check_heap() the headder and footer are checked if they have the
 * matching size and allocation value.
 * The whole heap is printed.
 * The list of free blocks is printed for all the segregated list
 * And infinite loop is being tested using the hare/tortoise algo.
 * Also, alignment of the every block is tested.
 */
void mm_checkheap(int lineno){
	/*Get gcc to be quiet. */
	printf("%d\n",lineno);
	/************************/
	void *bp;
	int i=0;
	
    for (bp = heap_listp; bp != NULL; bp = NEXT_BLKP(bp)) 
	{
           check(heap_listp);
		   print(bp);
    }
	
	for(i=0; i<PARTITIONS;i++) 
	{ 
		for (bp = LIST_INDX(i); bp != NULL; bp = LIST_INDX_NEXT_BLKP(bp)) 
		{    
			print(bp);
		}
    }
	
	loop();
}

static void loop() 
{
    void *h;
    void *t;
    for (int i = 0; i < PARTITIONS; i++) 
	{
        h= LIST_INDX(i);
        t = LIST_INDX(i);

        while( t != NULL && h != NULL  ) 
		{
            if( t !=  NULL ) 
			{
                t = LIST_INDX_NEXT_BLKP(t);
				h = LIST_INDX_NEXT_BLKP(t);
            }
            if( t == h )
			{
                printf("Error Infinite Loop");       
            }
        }
    }
}

static void print(void *bp) 
{   
    size_t size_header=-1, alloc_header=-1, size_footer=-1, alloc_footer=-1;

    if ( bp != NULL) 
	{
        size_header = GET_SIZE(HDRP(bp));
        alloc_header = GET_ALLOC(HDRP(bp));  
        size_footer = GET_SIZE(FTRP(bp));
        alloc_footer = GET_ALLOC(FTRP(bp));  
		printf("%p:\n",bp);
		printf("header: %lu \n", size_header);
        printf("footer: %lu \n", size_footer);
		printf("header: %lu \n", alloc_header);
        printf("footer: %lu \n", alloc_footer); 
    } 
	else 
	{
        printf("END OF LIST\n");
    }
}

static void check(void *bp) 
{
    if(GET_ALLOC(HDRP(bp)) != GET_ALLOC(FTRP(bp))) 
	{
        printf("Allocation Bit Mis-match in Header and Footer for %p",bp);
    }

	if(GET_SIZE(HDRP(bp)) != GET_SIZE(FTRP(bp))) 
	{
        printf("Size Mis-match in Header and Footer for %p",bp);
    }
	
	if ((size_t)bp % 8) 
	{
       printf("NOT ALIGNED: %p", bp);
    }
}