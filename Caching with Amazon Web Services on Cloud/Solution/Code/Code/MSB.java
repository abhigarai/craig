package Project2_3;


/*
 * MSB.java
 * 
 * This is a web-service used by the MSB to get targets' private
 * conversations from the databases. The conversations have been
 * encrypted, but I have heard rumors about the key being a part 
 * of the results retrieved from the database. 
 * 
 * 02/08/15 - I have replicated the database instances to make
 * the web service go faster.
 * 
 * To do (before 02/15/15): My team lead says that I can get a 
 * higher RPS by optimizing the retrieveDetails function. I 
 * stack overflowed "how to optimize retrieveDetails function", 
 * but could not find any helpful results. I need to get it done
 * before 02/15/15 or I will lose my job to that new junior systems
 * architect.
 * 
 * 02/15/15 - :'(
 * 
 * 
 */ 
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Verticle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map.Entry;
import java.util.LinkedHashMap;
import java.util.HashMap;


public class MSB extends Verticle {

	private int dbInstance=0;
	private static final int MAX_ENTRIES = 999;
	File f;
	BufferedWriter bw ;
	FileWriter fw;
	HashMap<String, String> cache1= new HashMap<String,String>();
	LinkedHashMap<String, String> cache =new LinkedHashMap<String, String>();
	String key="1", value=null;
	private String[] databaseInstances = new String[2];		
	/* 
	 * init -initializes the variables which store the 
	 *	     DNS of your database instances
	 */
	private void init() {
		try
		{
		f  = new File("/home/ubuntu/Project2_3/trace");
		fw = new FileWriter(f);
		bw = new BufferedWriter(fw);
		}
		catch(Exception e)
		{
			System.out.println("Error while writing file");
		}
		/* Add the DNS of your database instances here */
		databaseInstances[0] = "ec2-52-0-76-77.compute-1.amazonaws.com";
		databaseInstances[1] = "ec2-52-0-238-119.compute-1.amazonaws.com";
	}
	
	/*
	 * checkBackend - verifies that the DCI are running before starting this server
	 */	
    	private boolean checkBackend() {
        	try{
            		if(sendRequest(generateURL(0,"1")) == null ||
                	sendRequest(generateURL(1,"1")) == null)
                		return true;
        	} catch (Exception ex) {
            		System.out.println("Exception is " + ex);
			return true;
        	}

        	return false;
    	}

	/*
	 * sendRequest
	 * Input: URL
	 * Action: Send a HTTP GET request for that URL and get the response
	 * Returns: The response
	 */
	private String sendRequest(String requestUrl) throws Exception {
		 
		URL url = new URL(requestUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent", "Mozilla/5.0");
 
		BufferedReader in = new BufferedReader(
					new InputStreamReader(connection.getInputStream(), "UTF-8"));
		
		String responseCode = Integer.toString(connection.getResponseCode());
		if(responseCode.startsWith("2")){
			String inputLine;
			StringBuffer response = new StringBuffer();
 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			connection.disconnect();
			return response.toString();
        	} else {
            		System.out.println("Unable to connect to "+requestUrl+
            		". Please check whether the instance is up and also the security group settings"); 
            		connection.disconnect();
            		return null;
	    	}   
	}
	/*
	 * generateURL
	 * Input: Instance ID of the Data Center
	 * 		  targetID
	 * Returns: URL which can be used to retrieve the target's details
	 * 			from the data center instance
	 * Additional info: the target's details are cached on backend instance
	 */
	private String generateURL(Integer instanceID, String key) {
		return "http://" + databaseInstances[instanceID] + "/target?targetID=" + key;
	}
	
	/*
	 * generateRangeURL
	 * Input: 	Instance ID of the Data Center
	 * 		  	startRange - starting range (targetID)
	 *			endRange - ending range (targetID)
	 * Returns: URL which can be used to retrieve the details of all
	 * 			targets in the range from the data center instance
	 * Additional info: the details of the last 10,000 targets are cached
	 * 					in the database instance
	 * 				
	 */
	private String generateRangeURL(Integer instanceID, Integer startRange, Integer endRange) {
		return "http://" + databaseInstances[instanceID] + "/range?start_range="
				+ Integer.toString(startRange) + "&end_range=" + Integer.toString(endRange);
	}

	/* 
	 * retrieveDetails - you have to modify this function to achieve a higher RPS value
	 * Input: the targetID
	 * Returns: The result from querying the database instance
	 */
	private String retrieveDetails(String targetID) {
		try{
			if(dbInstance==0)
			{
				dbInstance=1;
				return sendRequest(generateURL(0, targetID));
			}
			else
			{
				dbInstance=0;
				return sendRequest(generateURL(1, targetID));
			}
			/*
			if(dbInstance==0)
			{
				key=targetID;
				dbInstance=1;
				return sendRequest(generateURL(0, targetID));
				
			}
			else
			{
				key=targetID;
				dbInstance=0;
				return sendRequest(generateURL(1, targetID));
				
			}
			*/
		} catch (Exception ex){
			System.out.println(ex);
			return null;
		}
	}
	
	/* 
	 * processRequest - calls the retrieveDetails function with the targetID
	 */
	private void processRequest(String targetID, HttpServerRequest req) {
		String result=null;
		if(targetID=="1")
		{
			if(cache1.size()!=0)
			{
				result=cache1.get("1");
				//file write
				try
				{
				bw.write(targetID+"\n");
				}
				catch(Exception e)
				{
					System.out.println("Error");
				}
				req.response().end(result);	
			}
			else
			{
				result = retrieveDetails("1");
				//value=result;
				cache1.put(targetID, result);
				//file write
				try
				{
				bw.write(targetID+"\n");
				}
				catch(Exception e)
				{
					System.out.println("Error");
				}
				req.response().end(result);	
			}
		}
		else
		{
			result=cache.get(targetID);
			if(result==null)
			{
				result = retrieveDetails(targetID);
				if(result==null)
				{
					//
					try
					{
					bw.write(targetID+"\n");
					}
					catch(Exception e)
					{
						System.out.println("Error");
					}
					//
					req.response().end("No response received");
				}
				else
				{		
				    if(cache.size()<999)
				    {
				    	cache.put(targetID, result);
				    }
				    else
				    {	
				    	cache.put(targetID, result);
				    }
				    //
				    try
					{
					bw.write(targetID+"\n");
					}
					catch(Exception e)
					{
						System.out.println("Error");
					}
				    //
				    req.response().end(result);
				}
			}
			else
			{	
				cache.put(targetID, result);
		    	//
		    	try
				{
				bw.write(targetID+"\n");
				}
				catch(Exception e)
				{
					System.out.println("Error");
				}
		    	//
					req.response().end(result);	
			}
		}
	}
	
	private void processRequestRange(String startRange, String endRange, HttpServerRequest req)
	{
		String n=startRange;
		int k=0,m=0;
		String result[] = new String[Integer.parseInt(endRange)-Integer.parseInt(startRange)+1];
		for(int i=Integer.parseInt(startRange);i<=Integer.parseInt(endRange);i++)
		{
			result[k] = retrieveDetails(startRange);
			
			k++;
		}
		if(result != null)
			for(int i=0;i<=(result.length);i++)
			{
			req.response().end(result[i]);	
			}
		//else
			//req.response().end("No resopnse received");
		
		
	}
	
	
	/*
	 * start - starts the server
	 */
  	public void start() {
  		init();
		if(!checkBackend()){
  			vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
				public void handle(HttpServerRequest req) {
				    String query_type = req.path();		
				    req.response().headers().set("Content-Type", "text/plain");
				    
				    if(query_type.equals("/target")){
					    String key = req.params().get("targetID");
					    processRequest(key,req);
				    }
				    else {
					    String key = "1";
					    processRequest(key,req);
				    }
			    }               
			}).listen(80);
		} else {
			System.out.println("Please make sure that both your DCI are up and running");
			System.exit(0);
		}
	}
}

