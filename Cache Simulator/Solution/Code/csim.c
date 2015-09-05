/*Abhishek Garai - agarai*/
#include<stdio.h>
#include"cachelab.h"
#include<getopt.h>
#include<stdlib.h>
#include<unistd.h>

int setBits=0, EWay=0, blockBits=0;
int tagBits=0, tag=-1, set=0;
unsigned int hits=0, miss=0, evicts=0;
int size=0, numberSets=1, param=0, index=0;
int i=0, j=0, r1=0;
char identifier;	
unsigned address;
char *fileName;

typedef struct
{
	int valid;
	int tagb;
	int lruCounter;
}cacheLine;

typedef struct
{
	int cacheLineCount;
	int maxLruCounter;
	cacheLine *line;
}sets;

sets *cache;
void printcache()
{
	for (i=0; i<numberSets; i++)
	{
		for (j=0; j<EWay; j++)
		{
			printf("%d ",(((cache+set)->line)+j)->valid);
			printf("%d ",(((cache+set)->line)+j)->tagb);
			printf("%d   ",(((cache+set)->line)+j)->lruCounter);
		}
		printf("\n");
	}
}
void allocateSet()
{
	cache = (sets *)malloc(numberSets * sizeof(sets));
}

void allocateAssociativity()
{
	for(i=0;i<numberSets;i++)
	{
		(cache+i)->line = (cacheLine *)malloc(EWay * sizeof(cacheLine));
		(cache+i)->cacheLineCount = 0;
		(cache+i)->maxLruCounter = 0;
	}
}

void retrieveTag()
{
	tag = address>>(setBits+blockBits);
	/*printf("%d \n",tag);*/
}

void retrieveSet()
{
	set = (address<<tagBits)>>(tagBits+blockBits);
	/*printf("%d \n",set);*/
}

void initCache()
{
	for (i=0; i<numberSets; i++)
	{
		for (j=0; j<EWay; j++)
		{
			(((cache+i)->line)+j)->valid = 0;
			(((cache+i)->line)+j)->tagb = -1;
			(((cache+i)->line)+j)->lruCounter = 0;
		}
	}
}
int searchCache()
{
	/*printf("Search");
	printf("%p /n	", cache);
	printf("%p", cache+set);*/
	
	int r=-1;
	for (i=0; i<EWay; i++)
	{
		if((((cache+set)->line)+i)->valid == 1)
		{
			if((((cache+set)->line)+i)->tagb == tag)
			{
				r = i;
				break;
			}
			else
			{
				r = -1;
			}
		}
		else
		{
			r = -1;
		}
	}
	return r;
}


void load()
{
	/*printf("load");*/
	r1 = searchCache();
	if(r1 != -1)/*hit*/
	{
		/*test1*/
		hits++;
		/*printf("tag %d ",(((cache+set)->line)+r)->tagb);
		printf("set %d ",set);
		printf("hit \n");*/
		(cache+set)->maxLruCounter = ((cache+set)->maxLruCounter)+1;/*increment max lru counter*/
		(((cache+set)->line)+r1)->lruCounter = (cache+set)->maxLruCounter;/*assign that to lru counter of line*/
		/*printcache();*/
	}
	else/*miss*/
	{
		if((cache+set)->cacheLineCount == EWay)/*full*/
		{
			/*printf("tag %d ",(((cache+set)->line)+r)->tagb);
			printf("set %d ",set);*/
			miss++;
			/*printf("miss ");*/
			evicts++;
			/*printf("e \n");*/
			/*lru-load*/
			int min=99999999;
			for(int i=0;i<(cache+set)->cacheLineCount;i++)
			{
				if(((((cache+set)->line)+i)->lruCounter)<min)
				{
					min = (((cache+set)->line)+i)->lruCounter;
					index = i;
				}
			}
			(((cache+set)->line)+index)->valid = 1;/*valid bit 1*/
			(((cache+set)->line)+index)->tagb = tag;/*tag bit set*/
			(cache+set)->maxLruCounter = ((cache+set)->maxLruCounter)+1;/*increment max lru counter*/
			(((cache+set)->line)+index)->lruCounter = (cache+set)->maxLruCounter;/*assign that to lru counter of line*/	
			/*printcache();*/
		}
		else/*not full*/
		{
			/*printf("tag %d ",(((cache+set)->line)+r)->tagb);
			printf("set %d ",set);*/
			miss++;
			/*printf("miss \n");*/
			/*load*/
			(((cache+set)->line)+((cache+set)->cacheLineCount))->valid = 1;/*valid bit 1*/
			(((cache+set)->line)+((cache+set)->cacheLineCount))->tagb = tag;/*tag bit set to tag*/
			(cache+set)->maxLruCounter = ((cache+set)->maxLruCounter)+1;/*increment max lru counter*/
			(((cache+set)->line)+((cache+set)->cacheLineCount))->lruCounter = (cache+set)->maxLruCounter;/*set it to the counter of line*/
			/*printcache();*/
			(cache+set)->cacheLineCount=((cache+set)->cacheLineCount)+1;/*increment cache line count*/
		}
	}
}

void store()
{
	/*printf("store");*/
	load();
}

void modify()
{
	/*printf("modify");*/
	load();
	store();
}

int main(int argc, char *argv[])
{
	/*printf("start");*/
	opterr = 0;
	while((param = getopt(argc, argv, "s:E:b:t:")) != -1)
	{
		switch(param)
		{
			case 's': 	setBits = atoi(optarg);
						break;
			case 'E':	EWay = atoi(optarg);
						break;
			case 'b': 	blockBits = atoi(optarg);
						break;
			case 't': 	fileName = optarg;
						break;
			default:	printf("Invalid Command Line Arguments \n Program Exiting \n");
						exit(1);
		}
	}
	
	if(setBits == 0 || EWay == 0 || blockBits == 0 || fileName == NULL) 
	{
        printf("Invalid Command Line Arguments \n Program Exiting \n");
        exit(1);
    }

	for(i=1;i<=setBits;i++)
    {
        numberSets = numberSets * 2;
    }

	tagBits = 64 - blockBits - setBits;
		
	allocateSet();
	/*printf("Allocate set");*/
	allocateAssociativity();
	/*printf("Allocate associativity");*/
	initCache();
	/*printf("init cache");*/
	FILE * pFile;
	pFile = fopen(fileName, "r");

	while(fscanf(pFile," %c %x,%d", &identifier, &address, &size)==3)
	{	
		/*printf("%s ", int2bin(address));*/
		retrieveTag();
		/*printf("retrieve tag");*/
		retrieveSet();
		/*printf("retrieve set");*/
		switch(identifier) 
		{
			case 'I':	break;
			case 'L':	load();
						break;
			case 'S':	store();
						break;
			case 'M':	modify();					
						break;
			default:	break;
		}
	}		
	fclose(pFile);
	printSummary(hits, miss, evicts);
	return 0;
}
