import org.ini4j.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.amazonaws.services.ec2.model.RequestSpotInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SpotInstanceRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.auth.*;

import java.net.URL;

public class HorizontalTest {	

	public static void main(String[] args)throws Exception {
		// TODO Auto-generated method stub

		String publicDnsNameDC=null,publicDnsNameLG=null;//for storing the publicDnsNames of the DC and LG to launch the test link and the monitor test link
		String testId=null;// for passing the testid to the monitor test function from the launchtest function

		//Getting Credentials
		Properties properties = new Properties();
		properties.load(HorizontalTest.class.getResourceAsStream("credentials.properties"));
		BasicAWSCredentials bawsc = new BasicAWSCredentials(properties.getProperty("accessKey"), properties.getProperty("secretKey"));

		//Create EC2 Client using the credentials
		AmazonEC2Client amazonEC2Client = new AmazonEC2Client(bawsc);

		//Create Security Group 
		//creating the request with the name and description parameters 
		CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest();
		createSecurityGroupRequest.withGroupName("Custom Project 2.1 Security Group1")
									.withDescription("Custom Secuirty Group for Project 2.1");
		//creating a security group result to attach the amazon client created with the security group parameters
		CreateSecurityGroupResult createSecurityGroupResult = amazonEC2Client.createSecurityGroup(createSecurityGroupRequest);
		//other parameters attached to the security group to like ip range, protocol and ports 
		IpPermission ipPermission = new IpPermission();  	
		ipPermission.withIpRanges("0.0.0.0/0")
					.withIpProtocol("tcp")
					.withFromPort(80)
					.withToPort(80);		            

		//attache the above permissions allocated to the security group ; basically these are the input request permissions 
		AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest =	new AuthorizeSecurityGroupIngressRequest();   	
		authorizeSecurityGroupIngressRequest.withGroupName("Custom Project 2.1 Security Group1")
											.withIpPermissions(ipPermission);
		//attach the ingress permission through the request created to the amazon client
		amazonEC2Client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);


		//Call respective methods 

		/* So basically multiple methods are created for each function and are 
		 * called from the main function serially according to the order of execution
		 * i.e. get the dns of both the LG and the initial DC
		 * then launch the test and monitor it and if rps<4000 
		 * then call the AddDCtoLG() and again go to monitor test and keep looping until rps<4000 
		 * */
		publicDnsNameLG=launchLGInstance(amazonEC2Client);//for launching the LG and return the publicdns name
		//System.out.println(publicDnsNameLG);
		publicDnsNameDC=launchDCInstance(amazonEC2Client);//for launching the DC and return the publicdns name
		//System.out.println(publicDnsNameDC);
		testId=launchHorTest(publicDnsNameLG,publicDnsNameDC);//for launching the test
		//System.out.println(testId);
		monitorTest(amazonEC2Client,testId,publicDnsNameLG);//for monitoring the test

	}
	static String launchDCInstance(AmazonEC2Client amazonEC2Client)throws Exception
	{
		//System.out.println("Entered LaunchDCInstance");

		String publicDnsNameDC=null, instanceIdDC=null;//for storing the publicdnsname of dc and its id

		//Launching an DC ec2 instance
		RunInstancesRequest runInstancesRequestDC = new RunInstancesRequest();//create a request for running an instance
		//assign the different parameters
		runInstancesRequestDC.withImageId("ami-b04106d8")
							.withInstanceType("m3.medium")
							.withMinCount(1)
							.withMaxCount(1)
							.withKeyName("15619DemoAgarai")
							.withSecurityGroups("Custom Project 2.1 Security Group");		  
		RunInstancesResult runInstancesResultDC = amazonEC2Client.runInstances(runInstancesRequestDC);//create a result object to associate the request to the amazonclient
		Thread.sleep(10000);//sleep to get the instance started
		
		//Get Instance Id
		Instance instanceDC=runInstancesResultDC.getReservation().getInstances().get(0);//since only 1 instance is launched,
		//use the reservation class to get the instance at index 0
		instanceIdDC = instanceDC.getInstanceId();//retrieve the id using getid()
		//System.out.println(instanceIdDC);

		//Creating Tags for LG Instance
		//create a request to tag the resources
		CreateTagsRequest createTagsRequestDC= new CreateTagsRequest();
		createTagsRequestDC.withResources(instanceIdDC)
							.withTags(new Tag("Project","2.1"));

		//Get the publicDns Name 	
		loop:while(true)
		{
			//create an object of the below class to get the updated status from the amazonclient about the instance state
			DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
			describeInstancesRequest.withInstanceIds(instanceIdDC);//provide the instance id
			//associate the request to the amazonclient using the result class object
			//loop through the resul returned to check for the particular instance id and then its state using the respective functions
			DescribeInstancesResult describeInstancesResult = amazonEC2Client.describeInstances(describeInstancesRequest);
			List<Reservation> reservations=describeInstancesResult.getReservations();
			for (  Reservation reservation : reservations) {
				for (    Instance instance : reservation.getInstances()) {

					if(instance.getInstanceId().equals(instanceIdDC))
					{

						if (instance.getState().getName().equals("running")) 
						{
							//System.out.println("Sleep to get Ready");
							Thread.sleep(120000);//sleep to get the instance initialised i.e actually start receiving data
							amazonEC2Client.createTags(createTagsRequestDC);//associate the tag request generated to tag the instance
							publicDnsNameDC=instance.getPublicDnsName();//retrieve the publicdns name
							break loop;//and if we get it break out of everything
						} 
					}

				}
			}		

		}//repeat this until instance state is not running
		//System.out.println(publicDnsNameDC);
		return publicDnsNameDC;//return the dnsname 
	}
//logic flow for LG is same as the above DC function.
	static String launchLGInstance(AmazonEC2Client amazonEC2Client) throws Exception
	{
		//System.out.println("Enter LG Launch");
		String publicDnsNameLG=null, instanceIdLG=null;

		//Launching an LG EC2 Instance
		RunInstancesRequest runInstancesRequestLG = new RunInstancesRequest();					        	
		runInstancesRequestLG.withImageId("ami-4c4e0f24")
							.withInstanceType("m3.medium")
							.withMinCount(1)
							.withMaxCount(1)
							.withKeyName("15619DemoAgarai")
							.withSecurityGroups("Custom Project 2.1 Security Group");	  
		RunInstancesResult runInstancesResultLG =  amazonEC2Client.runInstances(runInstancesRequestLG);
		Thread.sleep(10000);
		
		//Get Instance ID
		Instance instanceLG=runInstancesResultLG.getReservation().getInstances().get(0);
		instanceIdLG = instanceLG.getInstanceId();

		//Tagging LG Instance
		CreateTagsRequest createTagsRequestLG= new CreateTagsRequest();
		createTagsRequestLG.withResources(instanceIdLG)
							.withTags(new Tag("Project","2.1"));

		//Get the publicDns Name 	
		loop:while(true)
		{
			DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
			describeInstancesRequest.withInstanceIds(instanceIdLG);
			DescribeInstancesResult describeInstancesResult = amazonEC2Client.describeInstances(describeInstancesRequest);
			List<Reservation> reservations=describeInstancesResult.getReservations();
			for (  Reservation reservation : reservations) {
				for (    Instance instance : reservation.getInstances()) {

					if(instance.getInstanceId().equals(instanceIdLG))
					{

						if (instance.getState().getName().equals("running")) 
						{
							System.out.println("Running sleep");
							Thread.sleep(30000);
							amazonEC2Client.createTags(createTagsRequestLG);
							publicDnsNameLG=instance.getPublicDnsName();
							break loop;
						} 
					}

				}
			}		

		}
		//System.out.println(publicDnsNameLG);
		return publicDnsNameLG;
	}

	static String launchHorTest(String publicDnsNameLG, String publicDnsNamesDC)throws Exception
	{
		String testId=null;//to return the testid after the test is launched
		//System.out.println("Entered launchHorTest");
		String launchLink=null;//for the test link 


		launchLink="http://"+publicDnsNameLG+"/test/horizontal?dns="+publicDnsNamesDC;//create teh link
		//System.out.println(launchLink);
		URL launchUrl = new URL(launchLink);//creating an url to launch it
		BufferedReader br1 = new BufferedReader(new InputStreamReader(launchUrl.openStream()));//open the link for the etst to start
		String line =br1.readLine();//read the line of the page having the testid
		//System.out.println(line);

		String[] parts = line.split("\\.");//split it on '.' as testid id between two '.'


		testId=parts[1];//get the id
		//System.out.println(line);

		//System.out.println(testId);
		br1.close();
		return testId;//retrun it for the monitor()
	}
	static void monitorTest(AmazonEC2Client amazonEC2Client, String testId, String publicDnsNameLG)throws Exception
	{
		//System.out.println("Entered MonitorTest");
		Thread.sleep(120000);//sleep for the test to be conducted for two minutes to check the rps and also because we cannot launch DCs within 2 minutes
		Ini ini=new Ini();//for parsing the log page

		int rps=0;//initialise rps to 0
		String monitorLink=null;
		monitorLink="http://"+publicDnsNameLG+"/log?name=test."+testId+".log";//create the monitor link
		//System.out.println(monitorLink);
		URL monitorUrl = new URL(monitorLink);//create the URl from the link

		ini.load(monitorUrl);//launch the link

		int lastMinute = 0;//calculate the last minute in the log
		for (int i = 1; i <= 30; ++i) {//traverse through all the minutes
			if (!ini.containsKey("Minute " + i))//if it does not contain the key as [minute i] in the section 
				break;							// indicates that the previous miniute was last and so break
			else
				lastMinute = i;
		}
		Ini.Section section = ini.get("Minute " + lastMinute);//retrieve the value of last minute

		for (String r : section.values()) {//traverse through the values of the section and retrieve add the rps value recursively
			Float f = Float.parseFloat(r);
			rps += f;//the values contains the rps of the DC 
		}

		if(rps<4000)//check if < 4000
		{
			addDCToLG(amazonEC2Client,testId,publicDnsNameLG);//if yes add another dc	
		}
	}
	static void addDCToLG(AmazonEC2Client amazonEC2Client,String testId, String publicDnsNameLG)throws Exception
	{
		//LOGIC FOR LAUNCHING ANOTHER DC IS SAME AS ABOVE FUNCTIONS
		//System.out.println("Entered AddDCtoLG");

		String publicDnsNameDC=null, instanceIdDC=null;

		//Launching an DC ec2 instance
		RunInstancesRequest runInstancesRequestDC = new RunInstancesRequest();

		runInstancesRequestDC.withImageId("ami-b04106d8")
							.withInstanceType("m3.medium")
							.withMinCount(1)
							.withMaxCount(1)
							.withKeyName("15619DemoAgarai")
							.withSecurityGroups("Custom Project 2.1 Security Group");		  
		RunInstancesResult runInstancesResultDC = amazonEC2Client.runInstances(runInstancesRequestDC);
		Thread.sleep(10000);
		
		//Get Instance Id
		Instance instanceDC=runInstancesResultDC.getReservation().getInstances().get(0);
		instanceIdDC = instanceDC.getInstanceId();

		//Create Tags for LG Instance
		CreateTagsRequest createTagsRequestDC= new CreateTagsRequest();
		createTagsRequestDC.withResources(instanceDC.getInstanceId())
							.withTags(new Tag("Project","2.1"));

		//Get Public DNS Name--Reference Code 
		loop:while(true)
		{
			DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
			describeInstancesRequest.withInstanceIds(instanceIdDC);
			DescribeInstancesResult describeInstancesResult = amazonEC2Client.describeInstances(describeInstancesRequest);
			List<Reservation> reservations=describeInstancesResult.getReservations();
			for (  Reservation reservation : reservations) {
				for (    Instance instance : reservation.getInstances()) {

					if(instance.getInstanceId().equals(instanceIdDC))
					{

						if (instance.getState().getName().equals("running")) 
						{
							System.out.println("Sleep to get ready");
							Thread.sleep(120000);
							amazonEC2Client.createTags(createTagsRequestDC);
							publicDnsNameDC=instance.getPublicDnsName();
							break loop;
						}

					}

				}
			}		

		}
		//System.out.println(publicDnsNameDC);



		//Adding the DC to the LG
		String addLink=null;
		addLink= "http://"+publicDnsNameLG+"/test/horizontal/add?dns="+publicDnsNameDC;//create the link to add
		//System.out.println(addLink);
		URL addUrl = new URL(addLink);//create the URL object from the link
		BufferedReader br1 = new BufferedReader(new InputStreamReader(addUrl.openStream()));//launch the link by opening it

		System.out.println(br1.readLine());

		br1.close();

		monitorTest(amazonEC2Client, testId, publicDnsNameLG);//and call the monitor function again
	}
}

