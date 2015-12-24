
/* 
 * Abhishek Garai - agarai
 * The below implementation is a concurrent proxy server with caching feature.
 * At the high level the proxy creates two descriptors and reads from one of 
 * them, puts it in a buffer and forwards it to the other. And through
 * child threads it makes concurrent threads simultaneously executing by
 * detaching themselves from the parent thread.
 * For caching, a variation of the selection sort algorithm is used wherein
 * insertion is always based on the lruCounter value of a fixed poistion i.e.
 * the last index lruCounter in this case instead of the first index as in 
 * selection sort.
 * Since, LRU is implemented based on the lruCounter value the elements of the 
 * respective locations need not be moved.
 */
#include <stdio.h>
#include <stdlib.h>

/* Recommended max cache and object sizes */
#define MAX_CACHE_SIZE 1049000
#define MAX_OBJECT_SIZE 102400

/* You won't lose style points for including these long lines in your code */
static const char *user_agent_hdr = "User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:10.0.3) Gecko/20120305 Firefox/10.0.3\r\n";
static const char *accept_hdr = "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n";
static const char *accept_encoding_hdr = "Accept-Encoding: gzip, deflate\r\n";

#include "csapp.h"

void doit(int fd);
void parse_uri(char *uri, char *hostname, int *port1, char *filename);
void clienterror(int fd, char *cause, char *errnum, 
		         char *shortmsg, char *longmsg);
void *thread(void *vargp);


static void cache_init(void);
static void form_header(char *buf, char *header_params, char *hostname);
static int search_cache(char *identifier, char *response);
static void save_cache(char *identifier, char *response);
/*
 * This is the structure of the cache that is used.
 * Each cacheLine consists of an identifiers i.e. the uri,
 * the actual data to be stored i.e. the response and an lruCounter for 
 * eviction policy.
 */
struct cacheLine
{
    char *identifier;
    char *data;
	int lruCounter;
};

static void update_cache(struct cacheLine *cache, int index);

static struct cacheLine *cache;

sem_t mutex;

/*
 * The implementation of the main() remains almost the same as in text i.e.
 * checking for the proper number of arguments, creating the socket descriptors
 * and then move into an infinite loop which waits for a connection and creates 
 * a new thread on each connection.
 * Further a mutex was defined for maintaining mutually exclusive access to the 
 * operation on cache and was sigpipe was ignored. Cache Initialization also 
 * is performed.
 */
int main(int argc, char **argv) 
{
    int listenfd, *connfd;
	char *port;
    socklen_t clientlen=sizeof(struct sockaddr_in);
    struct sockaddr_in clientaddr;
    pthread_t tid;

    /* Check command line args */
    if (argc != 2) 
	{
	    fprintf(stderr, "usage: %s <port>\n", argv[0]);
	    exit(1);
    }
	port = argv[1];
    sem_init(&mutex, 0, 1);
    cache_init();
	Signal(SIGPIPE, SIG_IGN);
    listenfd = Open_listenfd(port);
    while (1) 
	{
        connfd = Malloc(sizeof(int));
	    *connfd = Accept(listenfd, (SA *)&clientaddr, &clientlen); 
        Pthread_create(&tid, NULL, thread, connfd);
    }
    return 0;
}


/*
 * The cache is initialised by mallocing space for the variables of the 
 * structure.
 */
static void cache_init()
{
    int i=0;
		cache = malloc(sizeof(struct cacheLine) * 10);
	   while(i < 10)
	   {
           cache[i].lruCounter = i;
           cache[i].identifier = malloc(MAXLINE);
           cache[i].data = malloc(MAX_OBJECT_SIZE);
		   i++;
       }
}

/*
 * The idea is to shift all the indices i.e. the value of the lruCounter in this
 * implementation to its previous one as the most recently used index is always
 * sent at the front and hence in a single access of all the elements the LRU
 * element goes to the end and hence we can replace that when we are saving 
 * a new element in the cache. this is the same technique used in selection sort
 */

static void update_cache(struct cacheLine *cache, int index)
{
    int i=0, j=0;
    while(i < 10)
	{
        if(cache[i].lruCounter == index) 
		{
             break;
        }
		i++;
    }
	j = i;
    while(j > 0)
	{
        cache[j].lruCounter = cache[j-1].lruCounter; 
		j--;
    }
	cache[0].lruCounter = index;
}

/*
 * The idea of search cache is simple wherein we iterate over the elements
 * of the cache to find the identifier value to be same as that of the request 
 * identifiers i.e. the uri. and if they are same update the cache lrucounter to 
 * to recent access for implementing LRU policy. This is done in a mutually
 * exclusive way using the semaphore. 
 */
static int search_cache(char *identifier, char *buffer) 
{
    int i = 0;
    while(i < 10)
	{
		if((strcmp(cache[i].identifier,identifier) == 0))
        {
            P(&mutex);
            update_cache(cache, i);
            V(&mutex);
            strncpy(buffer, cache[i].data, strlen(cache[i].data));
            return 1;
        }
		i++;
    }
	return 0;
}

/*
 * As mentioned above the idea of selection sort is used to store the location 
 * of insertion in the lruCounter. So, the lruCounter counter of the last index
 * provides the index of the next location to be stored. So, the location of
 * index is fixed but the location of insertion varies based on the value of
 * lruCounter.
 */
static void save_cache(char *identifier, char *response)
{
    int index=0;
	index = cache[9].lruCounter;
	strncpy(cache[index].identifier, identifier, strlen(identifier));
    strncpy(cache[index].data, response, strlen(response));
    update_cache(cache, index);
}

/*
 * The functionalities of the doit() is as below listed in-order:
 * 1. Malloc space for port number and make it default 80
 * 2. read the client file descriptor and extract the method uri and version 
 *    from the header line.
 * 3. Check for the method as GET and change the version to HTTP 1.0
 *     and call the parseuri() to retrieve details frmo the uri.
 * 4. Search cache and if found respnd from the buffer.
 * 5. else create the request header and send the request and read response 
 *    from the server fd.
 * 6. After the complete response is received put it in the cache.
 *  Logical flow is similar to that in the text only that the serve dynamic 
 *  and static part is replaced by the serve static from cache/server.
 */
void doit(int fd) 
{
    int sfd, len, response_data;

    int *port1;
    char port2[5], hostname[MAXLINE], filename[MAXLINE];
    char buf[MAXLINE], method[MAXLINE], uri[MAXLINE], version[MAXLINE];
    char buffer[MAX_OBJECT_SIZE];          
    char header_params[MAXLINE], response[MAXLINE];     
    rio_t rio, s_rio;             
	
    port1 = malloc(sizeof(int));
    *port1 = 80;  
	
    Rio_readinitb(&rio, fd);
	Rio_readlineb(&rio, buf, MAXLINE);

    sscanf(buf, "%s %s %s", method, uri, version);       
    
	if (strcasecmp(method, "GET")) 
	{     
        clienterror(fd, method, "501", "Not Implemented",
                    "This proxy does not implement this method");
        return;
    } 

	strncpy(version, "HTTP/1.0",8);

    parse_uri(uri, hostname, port1, filename);        
    
   if (search_cache(uri, buffer) == 1) 
	{
      Rio_writen(fd, buffer, sizeof(buffer));
    }
    else 
	{ 
		/* Form the request params */
		sprintf(header_params, "%s %s %s\r\n", method, filename, version);
		form_header(buf, header_params, hostname);
        sprintf(port2, "%d", *port1);
        
		sfd = Open_clientfd(hostname, port2);

        Rio_readinitb(&s_rio, sfd);
        Rio_writen(sfd, header_params, strlen(header_params));

        while((len = Rio_readnb(&s_rio, response,sizeof(response)))>0) 
		{
            Rio_writen(fd, response, len);

            strcat(buffer, response);
            response_data += len;
        }    
        if (response_data <= MAX_OBJECT_SIZE)
        {
            P(&mutex);
            save_cache(uri, buffer);
            V(&mutex);
        }
        close(sfd);
    }
}

/*
 * This function is used to form the header that needs to be sent to the 
 * server with the necessary header fields. 
 * The implementation is based on the strstr() that returns a pointer to the
 * first location of a substring within a string. So, based on the functionality
 * search is performed on different header parameters and put into the final
 * header-params for the server request. Teh idea is strstr() returns null if 
 * it doesn't find the existence of the string and hence the check is performed
 * and accordingly the params are added.
 */

static void form_header(char *buf, char *header_params, char *hostname)
{
    if(strstr(buf, "Host") == NULL)
    {
        strcat(header_params, "Host: ");
        strcat(header_params, hostname);
        strcat(header_params, "\r\n");
    }
	 if(strstr(buf, "User-Agent:") == NULL) 
	{
        strcat(header_params, user_agent_hdr);
    }
    if(strstr(buf, "Accept:") == NULL) 
	{
        strcat(header_params, accept_hdr);
    }
    if(strstr(buf, "Accept-Encoding:") == NULL) 
	{
        strcat(header_params, accept_encoding_hdr);
    }
    if(strstr(buf, "Proxy-Connection:") == NULL) 
	{
        strcat(header_params, "Proxy-Connection: close\r\n");
    }
    if(strstr(buf, "Connection:") == NULL) 
	{
        strcat(header_params, "Connection: close\r\n\r\n");
    }
}

/*
 * This function is called by every child process created by every request that 
 * is accepted by the server. The idea is to detach the created thread from the
 * parent and free the pointer from the parent and call the doit function for
 * performing the further operation. 
 * The implementation remains the same as the text only that the call to the 
 * doit() is from within the thread after it is detached from the parent thread
 * and later the pointer is freed and the connection is closed.
 */
void *thread(void *vargp)
{
    int connfd = *((int *)vargp);
    Pthread_detach(pthread_self());
    Free(vargp);
    doit(connfd);
    Close(connfd);
    return NULL;
}

/*
 * This function is only used to retrieve the values from the uri of the 
 * request and put it in the respective variables used in the rogram.
 * So, this retrieves the hotname, port and the actual content to be
 * fetched from the server. 
 * The idea is to find the occurrence of specific characters in the url
 * i.e. 'http://', ':', '/' etc. to reach specific location within the url
 * and retrieve number of characters from there.
 */

void parse_uri(char *uri, char *hostname, int *port, char *filename) 
{
	int i=0, uri_len=0, flag=-1;
    char *temp;
	temp = strstr(uri, "http://");
	temp += 7;
	uri_len = strlen(uri);
	
    while(i < uri_len) 
	{
        if((temp[i] == '/'))
		{
			flag = 0;
            break;
		}
		else if(temp[i] == ':')
		{
			flag = 1;
			break;
		}
        else
		{
            hostname[i] = temp[i];
		}
		i++;
    }
    hostname[i] = '\0';
    if(flag == 1)
        sscanf(&temp[i+1], "%d%s", port, filename);
    else
        sscanf(&temp[i], "%s", filename);
}

/*
 * This function is used to handle any kind of error while creating 
 * the request and sending it to the server, opening output buffer etc. 
 * Basically any error generated before the request is served by the
 * server calls this function to display the error contents.
 **********************************************************************
 * Implementation remains same as in the text. This is similar to just an
 * error handling function which displays the error when called.
 */
void clienterror(int fd, char *cause, char *errnum, 
		 char *shortmsg, char *longmsg) 
{
    char buf[MAXLINE], body[MAXBUF];

    /* Build the HTTP response body */
    sprintf(body, "<html><title>Tiny Error</title>");
    sprintf(body, "%s<body bgcolor=""ffffff"">\r\n", body);
    sprintf(body, "%s%s: %s\r\n", body, errnum, shortmsg);
    sprintf(body, "%s<p>%s: %s\r\n", body, longmsg, cause);
    sprintf(body, "%s<hr><em>The Tiny Web server</em>\r\n", body);

    /* Print the HTTP response */
    sprintf(buf, "HTTP/1.0 %s %s\r\n", errnum, shortmsg);
    Rio_writen(fd, buf, strlen(buf));
    sprintf(buf, "Content-type: text/html\r\n");
    Rio_writen(fd, buf, strlen(buf));
    sprintf(buf, "Content-length: %d\r\n\r\n", (int)strlen(body));
    Rio_writen(fd, buf, strlen(buf));
    Rio_writen(fd, body, strlen(body));
}