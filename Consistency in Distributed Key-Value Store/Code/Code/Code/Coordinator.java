import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.sql.Timestamp;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;

public class Coordinator extends Verticle {
	
	//Map used to store the key-lock pairing based on the key value for the strong consistency model across all the datacentres
	HashMap<String,ReentrantLock> strongMap=new HashMap<String,ReentrantLock>();
	
	//Map used for storing the key-lock pairing based on key value for causal consistency for individual datacentre
	//Since key-lock pairing is for individual data centre, there are three different hash-maps for individual data-centre
	HashMap<String,ReentrantLock> DC1=new HashMap<String,ReentrantLock>();
	HashMap<String,ReentrantLock> DC2=new HashMap<String,ReentrantLock>();
	HashMap<String,ReentrantLock> DC3=new HashMap<String,ReentrantLock>();
	
	//Default mode: Strongly consistent. Possible values are "strong" and "causal"
	private static String consistencyType = "strong";
	private String resultValue=null;//for storing the result vale in case of get operations
	/**
	 * TODO: Set the values of the following variables to the DNS names of your
	 * three dataCenter instances
	 */
	//Addresses of the Datacentres
	private static final String dataCenter1 = "ec2-52-4-117-83.compute-1.amazonaws.com";
	private static final String dataCenter2 = "ec2-52-4-205-2.compute-1.amazonaws.com";
	private static final String dataCenter3 = "ec2-52-4-207-64.compute-1.amazonaws.com";

	//Using the Reentrant Lock for maintaining the Thread order accessing individual datacentre based on key value
	//So if there are two consecutive calls on a particular datacentre for the same key the second call waits until the first call releases the lock.
	//This is made possible by maintaining individual Threads for each operation for individual datacentre
	
	
	//Lock used to maintain the access to put operations in datacentre 1 based on key
	ReentrantLock lockDC1=new ReentrantLock();
	
	
	
	//This function checks if a lock is hold by a particular key on data centre 1 based on the key parameter passed.
	//And it returns the lock back to the calling function.
	//It checks if lock is present on a key, then it returns the lock from the map, else it creates a new lock and returns it
	//this function is used for causal consistency only
	public ReentrantLock DC1Map(String key)//takes the key as parameter for locking based on key 
	{
		ReentrantLock lock1;//lock to be returned from hashmap or after creating and locking based on a key
		try
		{
		lockDC1.lock();//lock the datacentre based on the key
		if(DC1.containsKey(key))//if present in the hashmap
		{
			lock1=DC1.get(key);//retrieve from the map
		}
		else
		{
			lock1 = new ReentrantLock();//else create a lock and put it in the map and return it
			DC1.put(key, lock1);					
		}
		}
		finally
		{
			lockDC1.unlock();//unlock after the operation
		}
		return lock1;//return the lock for performing further operation on it.
	}
	
	//This function has the same functionality as before but maintains the keuy-lock mappings for Data centre 2 for causal consistency
	ReentrantLock lockDC2=new ReentrantLock();
	public ReentrantLock DC2Map(String key)
	{
		ReentrantLock lock2;
		try
		{
		lockDC2.lock();
		if(DC2.containsKey(key))
		{
			lock2=DC2.get(key);
		}
		else
		{
			lock2 = new ReentrantLock();
			DC2.put(key, lock2);					
		}
		}
		finally
		{
			lockDC2.unlock();
		}
		return lock2;
	}
	
	//Similarly this function maintains the key-lock mappings for Datacentre 3 for causal consistency
	ReentrantLock lockDC3=new ReentrantLock();
	public ReentrantLock DC3Map(String key)
	{
		ReentrantLock lock3;
		try
		{
			lockDC3.lock();
		if(DC3.containsKey(key))
		{
			lock3=DC3.get(key);
		}
		else
		{
			lock3 = new ReentrantLock();
			DC3.put(key, lock3);					
		}
		}
		finally
		{
			lockDC3.unlock();
		}
		return lock3;
	}
	
	//This function maintains the key-lock mapping for strong consistency across all the datacentre  
	ReentrantLock lockHash=new ReentrantLock();
	public ReentrantLock lockHashMap(String key)
	{
		ReentrantLock lock;
		try
		{
		lockHash.lock();
		if(strongMap.containsKey(key))
		{
			lock=strongMap.get(key);
		}
		else
		{
			lock = new ReentrantLock();
			strongMap.put(key, lock);					
		}
		}
		finally
		{
			lockHash.unlock();
		}
		return lock;
	}
	
	@Override
	public void start() {
		//DO NOT MODIFY THIS
		KeyValueLib.dataCenters.put(dataCenter1, 1);
		KeyValueLib.dataCenters.put(dataCenter2, 2);
		KeyValueLib.dataCenters.put(dataCenter3, 3);
		final RouteMatcher routeMatcher = new RouteMatcher();
		final HttpServer server = vertx.createHttpServer();
		server.setAcceptBacklog(32767);
		server.setUsePooledBuffers(true);
		server.setReceiveBufferSize(4 * 1024);

	
		
		routeMatcher.get("/put", new Handler<HttpServerRequest>() {
			
			//This function is used to perform the causal put operation for data centre 1.
			//parameters taken are key,value and lock on a particular key at datacentre 1.
			public void lockUpdateCausal1(String key,String value,ReentrantLock lock1)
			{
				lock1.lock();//lock the datacentre based on the key
				try
				{
					KeyValueLib.PUT(dataCenter1, key,value);//update the key-value at datacentre 1
				}
				catch(Exception e)
				{
					System.out.println(e);
				}
				finally
				{
					lock1.unlock();//finally unloack the key after the operation
				}
			}
			
			//This function is similar to the previous function but for datacentre 2
			//PUT operation for Datacentre 2
			public void lockUpdateCausal2(String key,String value,ReentrantLock lock2)
			{
				lock2.lock();
				try
				{
					KeyValueLib.PUT(dataCenter2, key,value);
				}
				catch(Exception e)
				{
					System.out.println(e);
				}
				finally
				{
					lock2.unlock();
				}
			}
			
			//Similarly this function is for Datacentre 3 and has te same functionality as explained above.
			//PUT operation for Datacentre 3
			public void lockUpdateCausal3(String key,String value,ReentrantLock lock3)
			{
				lock3.lock();
				try
				{
					KeyValueLib.PUT(dataCenter3, key,value);
				}
				catch(Exception e)
				{
					System.out.println(e);
				}
				finally
				{
					lock3.unlock();
				}
			}
			
			//This update operation is used to lock based on the key across all the datacentres
			//This function has the same functionalities as above but performs the operations across all the data centres in an atomic fashion
			public void lockUpdate(String key,String value,ReentrantLock lock)
			{
				lock.lock();//lock based on the key
				try
				{
				//Perform the operation accross all the datacentres
				KeyValueLib.PUT(dataCenter1, key,value);
				KeyValueLib.PUT(dataCenter2, key,value);
				KeyValueLib.PUT(dataCenter3, key,value);
				}
				catch(Exception e)
				{
					System.out.println(e);
				}
				finally
				{
					lock.unlock();//finally unlock the key
				}
			}
			
		
			@Override
			public void handle(final HttpServerRequest req) {
				String resultValue=null;
				MultiMap map = req.params();
				final String key = map.get("key");
				final String value = map.get("value");
				//You may use the following timestamp for ordering requests
                                final String timestamp = new Timestamp(System.currentTimeMillis() 
                                                                + TimeZone.getTimeZone("EST").getRawOffset()).toString();
              
                //The concept here is based on the fact that Reentrant lock maintains the Thread order based on the order in which the Threads 
                                //called the function on which the lock is attained. So, every operation is sent on a different Thread.
                                //And since all the operations for strong are sequential, Reentrant lock maintains te order of te operation.
                                //And for causal consistency the operations are isolated across the datacentres by creating individual Threads 
                                //maintained for every operations accross ten adtacentres.
                                                                
                                                             
						if(consistencyType.equals("strong"))//if consistency type is Strong
						{
							Thread t = new Thread(new Runnable()//create a new Thread and perform the operation
							{
								public void run() {
									ReentrantLock lock=lockHashMap(key);//Retrieve the lock from the hashmap if already present else create a lock,store it in the Hashmap and return 
									lockUpdate(key,value,lock);//perform the operation for the particular key using the lock maintained for it in the Hashmap 
							}});
							t.start();//start the Thread and return
						}
						else//else for causal create Three parallel Threads and perfrom the same operations on individual datacentres
						{
							//Thread created to perform the operation on Datacentre 1 for which mappings are maintained by seperate Hashmaps and updae functions
							Thread t1 = new Thread(new Runnable()
							{
								public void run() {
									ReentrantLock lock1=DC1Map(key);
									lockUpdateCausal1(key,value,lock1);
							}});
							//Similarly this Thread performs teh operation at Datacentre 2 
							Thread t2 = new Thread(new Runnable()
							{
								public void run() {
									ReentrantLock lock2=DC2Map(key);
									lockUpdateCausal2(key,value,lock2);
							}});
							//Similarly for Datacentre 3
							Thread t3 = new Thread(new Runnable()
							{
								public void run() {
									ReentrantLock lock3=DC3Map(key);
									lockUpdateCausal3(key,value,lock3);
							}});
							
							//Start all the Threads to execute them concurrently
							t1.start();
							t2.start();
							t3.start();
						}
						//TODO: Write code for PUT operation here.
						//Each PUT operation is handled in a different thread.
						//Highly recommended that you make use of helper functions.
				req.response().end(); //Do not remove this
			}
		});

		routeMatcher.get("/get", new Handler<HttpServerRequest>() {
			
			//Get function for causal consistency i.e. it immediately returns the value once it gets the request
			public String GetCausal(String key,String loc)//take sthe key and the location of DC to retrieve from
			{
				String result=null;
				try
				{//No lock is implemented as this is causal consistency
					switch(Integer.parseInt(loc))//based on location valure retrieve from particular datacentre
					{
					case 1:result=KeyValueLib.GET(dataCenter1, key);
					case 2:result=KeyValueLib.GET(dataCenter2, key);
					case 3:result=KeyValueLib.GET(dataCenter3, key);
					}
				}
				catch(Exception e)
				{
					System.out.println(e);
				}
				return result;
			}
			
			//function for retrieving in case of strong consistency. here first we lock it and then return the value
			public String lockGet(String key,String loc,ReentrantLock lock)
			{
				//This function has the similar functionality but locks based on key before the operation 
				String result=null;
				lock.lock();
				try
				{
					switch(Integer.parseInt(loc))
					{
					case 1:result=KeyValueLib.GET(dataCenter1, key);
					case 2:result=KeyValueLib.GET(dataCenter2, key);
					case 3:result=KeyValueLib.GET(dataCenter3, key);
					}
				
				}
				catch(Exception e)
				{
					System.out.println(e);
				}
				finally
				{
					lock.unlock();
				}
				return result;
			}
			@Override
			public void handle(final HttpServerRequest req) {
				
				MultiMap map = req.params();
				final String key = map.get("key");
				final String loc = map.get("loc");
				//You may use the following timestamp for ordering requests
				final String timestamp = new Timestamp(System.currentTimeMillis() 
								+ TimeZone.getTimeZone("EST").getRawOffset()).toString();
				
				
				//Every operation is performed on a sepearte Thread.
				Thread t = new Thread(new Runnable() {
					public void run() {
						if(consistencyType.equals("strong"))//if strong consistency
						{
							ReentrantLock lock=lockHashMap(key);//retrieve the lock from the map else create and return
							resultValue=lockGet(key,loc,lock);//call the function and pass key, location to retrieve and lock used for that key
						}
						else
						{
							resultValue=GetCausal(key,loc);//retrieve immediately based on key 
						}
						//TODO: Write code for GET operation here.
                                                //Each GET operation is handled in a different thread.
                                                //Highly recommended that you make use of helper functions.
						req.response().end(resultValue); //Default response = 0
					}
				});
				t.start();
			}
		});

		routeMatcher.get("/consistency", new Handler<HttpServerRequest>() {
                        @Override
                        public void handle(final HttpServerRequest req) {
                                MultiMap map = req.params();
                                consistencyType = map.get("consistency");
                                //This endpoint will be used by the auto-grader to set the 
				//consistency type that your key-value store has to support.
                                //You can initialize/re-initialize the required data structures here
                                req.response().end();
                        }
                });

		routeMatcher.noMatch(new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				req.response().putHeader("Content-Type", "text/html");
				String response = "Not found.";
				req.response().putHeader("Content-Length",
						String.valueOf(response.length()));
				req.response().end(response);
				req.response().close();
			}
		});
		server.requestHandler(routeMatcher);
		server.listen(8080);
	}
}
