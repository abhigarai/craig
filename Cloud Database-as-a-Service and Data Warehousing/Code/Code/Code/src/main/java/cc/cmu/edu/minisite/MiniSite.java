package cc.cmu.edu.minisite;


import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.*;
import java.lang.Integer;

import io.undertow.io.Sender;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.io.IoCallback;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;




// hbase api import
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;




import com.amazonaws.auth.BasicAWSCredentials;
// dynamodb api import
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.Map;
import java.util.HashMap;

public class MiniSite {

    public MiniSite() throws Exception
    {

    }


    public static void main(String[] args)throws Exception{
        final MiniSite minisite = new MiniSite();
        final ObjectMapper mapper = new ObjectMapper();
        
        //***********Connect with RDS******************
        // JDBC driver name and database URL
        final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
        
        //DB_URL for connectivity
        final String DB_URL = "jdbc:mysql://userstable.chrpnooqpt69.us-east-1.rds.amazonaws.com:3306/users_Table";

        //  Database credentials
        final String USER = "agarai";
        final String PASS = "dummypwd";
        
        //Register JDBC driver
        Class.forName("com.mysql.jdbc.Driver");

        //Open a connection
        //System.out.println("Connecting to database...");//For Debugging as per the reference site
        Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);

        //create Statement to Execute a query 
        //System.out.println("Creating statement...");//Debugging
        final Statement stmt = conn.createStatement();
        //**********************************************************
        
        //***************Connect with Hbase*****************
        Configuration config = HBaseConfiguration.create();
		config.set("hbase.zookeeper.quorum","172.31.55.212");
                        		
        final HTable table = new HTable(config, "friends_Relations");
        //********************************************************
        
        //Using the Builder API
        Undertow.builder()
        .addHttpListener(8080, "0.0.0.0")
        .setHandler(new HttpHandler() {

            public void handleRequest(final HttpServerExchange exchange) throws Exception {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json; encoding=UTF-8");
                //Creating the Sender Object for each request
                Sender sender = exchange.getResponseSender();
                //Creating the response object for each request
                JSONObject response=null;
                //setting the result for adding it to the JSON object 
                String user_Name=null;
                //get the path to check for the query step
                String path=exchange.getRequestPath();
                               
                if(path.contains("/step1"))//check if its step1
                {
                	 response = new JSONObject();
                	//Retrieve id and pwd from the url
                	String id=exchange.getQueryParameters().get("id").getFirst();
                	String pwd=exchange.getQueryParameters().get("pwd").getFirst();
                	
                	//prepare the query for execution
                    String sql = "SELECT user_Name FROM users_Table WHERE user_Id LIKE '"+id+"' AND user_Pwd LIKE '"+pwd+"'";
                    ResultSet rs = stmt.executeQuery(sql);//execute the query and store the result
                    
                    if(rs.next())//retrieve the only row using nect-it returns only one row because the id and pwd combination is unique
                    {
                       //Retrieve by column name
                        user_Name = rs.getString("user_Name");
                    }
                    else
                    {
                    	//else set it to unauthorized if nothing retrieved i.e. null
                    	user_Name = "Unauthorized";
                    }
                    response.put("name", user_Name);//set the response object with the username
                }
                else
                {
                	if(path.contains("/step2"))//check if its of query 2
                	{
                		 response = new JSONObject();
                		//retrieve id from url
                		String id=exchange.getQueryParameters().get("id").getFirst();
                		KeyValue value[]=null;//declare it to store the value of row returned as Keyvalue type
            
                		JSONArray friends = new JSONArray();//Array to store the final response for this query
                   		               		
                		Get get = new Get(Bytes.toBytes(id));//define the get object with parameters for a particular row
                		//get.addFamily(Bytes.toBytes("follow"));//Set the family --not required because now i have only one family 
                		Result result = table.get(get); //retrieve the result	                    
                        value=result.raw();//get the data from the result object
                        //System.out.println(value);//debug
                        String res=new String(value[0].getValue());//retrieve the only value column for each row
                        //System.out.println(res);//debug      
                        String parts[] = res.split("#");//split it on # as this was the seperator used for each follower of 
                        
                       int partsSorted[]=new int[parts.length];//used for sorting the array based on integer type
                        for(int i=0;i<parts.length;i++)
                        {
                        	partsSorted[i]=Integer.parseInt(parts[i]);//transfer elements from array to int array for sorting 
                        }
                        Arrays.sort(partsSorted);//Sorting the array using the Arrays Sort function
                        
                        //PREPARE the response 
                        for(int i=0;i<parts.length;i++)//iterate over the array and put it in the response object
                        {
                        	JSONObject friend = new JSONObject();
                        	friend.put("userid",parts[i]);
                        	friends.add(friend);
                        }
                        response.put("friends", friends);               		
                	}
                	else
                	{
                		if(path.contains("/step3"))
                		{               
                			 response = new JSONObject();
                		String createdDate=null,imageUrl=null;//to store the results to pass to the object
                		String id=exchange.getQueryParameters().get("id").getFirst();
                		                		
                		//To store the result 
                		//Map resultMap=new HashMap();
                		//Retrieving the credentials		
                		BasicAWSCredentials bawsc = new BasicAWSCredentials("", "");
                        //AmazonDynamoDBClient amazonDynamoDBClient = new AmazonDynamoDBClient(new ProfileCredentialsProvider());
                		AmazonDynamoDBClient amazonDynamoDBClient = new AmazonDynamoDBClient(bawsc);
                		amazonDynamoDBClient.setEndpoint("dynamodb.us-east-1.amazonaws.com");
                		GetItemRequest getItemRequest=new GetItemRequest();
                		AttributeValue attributeValue = new AttributeValue();
                		attributeValue.withN(id);
                		Map idMap=new HashMap();
                		idMap.put("userid", attributeValue);

                		getItemRequest.withKey(idMap)
                		.withTableName("P33Step3Table");
                		
                		
                		GetItemResult getItemResult =amazonDynamoDBClient.getItem(getItemRequest);
                		Map resultMap=getItemResult.getItem();
                		
                		AttributeValue date=(AttributeValue)resultMap.get("time");
                		AttributeValue url=(AttributeValue)resultMap.get("url");
                		
                		createdDate = date.getS();
                		imageUrl=url.getS();
                		response.put("time", createdDate);
                		response.put("url", imageUrl);                		
                		}              		
                	}
                	
                }
                String content = "returnRes("+mapper.writeValueAsString(response)+")";
                sender.send(content);
            }
        }).build().start();
    }
}

	
