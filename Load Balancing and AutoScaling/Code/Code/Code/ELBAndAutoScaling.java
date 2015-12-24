	
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.DeleteAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.DeleteLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.DeletePolicyRequest;
import com.amazonaws.services.autoscaling.model.InstanceMonitoring;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyResult;
import com.amazonaws.services.autoscaling.model.Tag;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.DeleteAlarmsRequest;
import com.amazonaws.services.cloudwatch.model.Metric;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.ApplySecurityGroupsToLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.AttachLoadBalancerToSubnetsRequest;
import com.amazonaws.services.elasticloadbalancing.model.AttachLoadBalancerToSubnetsResult;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckRequest;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckResult;
import com.amazonaws.services.elasticloadbalancing.model.ConnectionDraining;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerListenersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthResult;
import com.amazonaws.services.elasticloadbalancing.model.HealthCheck;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerAttributes;
import com.amazonaws.services.elasticloadbalancing.model.ModifyLoadBalancerAttributesRequest;


public class ELBAndAutoScaling {
	
	
	/* These global variables are used to store the values globally to reduce the parameter passing to the functions*/
	public static String sLcName = null;//for launch configuration name
	public static String sAsgName = null;//for ASG name
	public static String sLbName = null;//for load balancer name
	public static String sSubnetId = null;//for the subnet id
	
	
	public static void main(String[] args)throws Exception {
		// TODO Auto-generated method stub
		
		String secGroupName="CustProj22SecGroupN19"+System.currentTimeMillis();//for creating different security group name for individual run
		
		ArrayList<Listener> listenerList= new ArrayList<Listener>(); // list to store the listener parameters for load gnerator
		
		Listener listener = new Listener(); // the values for the various load generator parameters
		listener.setInstanceProtocol("HTTP");
		listener.setLoadBalancerPort(80);
		listener.setProtocol("HTTP");
		listener.setInstancePort(80);
		
		listenerList.add(listener);//adding the listener object to the list to pass it to the load balancer
		
		//Creating the tagging request
		com.amazonaws.services.elasticloadbalancing.model.Tag tag1 = new com.amazonaws.services.elasticloadbalancing.model.Tag();
		tag1.setKey("Project");
		tag1.setValue("2.2");

		//Getting Credentials
		Properties properties = new Properties();
		properties.load(ELBAndAutoScaling.class.getResourceAsStream("credentials.properties"));
		BasicAWSCredentials bawsc = new BasicAWSCredentials(properties.getProperty("accessKey"), properties.getProperty("secretKey"));

		//Create LoadBalancingEC2 Client using the credentials
		AmazonElasticLoadBalancingClient amazonElasticLoadBalancingClient = new AmazonElasticLoadBalancingClient(bawsc);
		AmazonEC2Client amazonEC2Client = new AmazonEC2Client(bawsc); 
		
		CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest();
		createSecurityGroupRequest.withGroupName(secGroupName)
									.withDescription("Custom Security Group for Project 2.2");
		IpPermission ipPermission = new IpPermission();  	
		ipPermission.withIpRanges("0.0.0.0/0")
					.withIpProtocol("-1")
					.withFromPort(-1)
					.withToPort(-1);
		CreateSecurityGroupResult createSecurityGroupResult =  amazonEC2Client.createSecurityGroup(createSecurityGroupRequest);
		AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest =	new AuthorizeSecurityGroupIngressRequest();   	
		authorizeSecurityGroupIngressRequest.withGroupName(secGroupName)
											.withIpPermissions(ipPermission);
		amazonEC2Client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
		String secId=createSecurityGroupResult.getGroupId();
		
		String lbName="ELBProject221"+System.currentTimeMillis();
		sLbName = lbName;
		//create Load balancer request with the parameters
		CreateLoadBalancerRequest createLoadBalancerRequest = new CreateLoadBalancerRequest();
		
		createLoadBalancerRequest.withLoadBalancerName(lbName)
								.withListeners(listenerList)
								.withAvailabilityZones("us-east-1a")
								.withTags(tag1)
								.withSecurityGroups(secId);
		//launch the Load balancer client
		CreateLoadBalancerResult createLoadBalancerResult=amazonElasticLoadBalancingClient.createLoadBalancer(createLoadBalancerRequest);
		
		//create a health  check up request with the respective parameters
		HealthCheck healthCheck = new HealthCheck();
		healthCheck.withHealthyThreshold(5)
		.withUnhealthyThreshold(2)
		.withTimeout(4)
		.withInterval(5)
		.withTarget("HTTP:80/heartbeat");
		
		//create a request to configure the health check		
		ConfigureHealthCheckRequest configureHealthCheckRequest = new ConfigureHealthCheckRequest();
		configureHealthCheckRequest.setHealthCheck(healthCheck);
		configureHealthCheckRequest.setLoadBalancerName(lbName);
		
		//Attach the health check up object to the LB Client
		amazonElasticLoadBalancingClient.configureHealthCheck(configureHealthCheckRequest);
				
		
		String publicDnsNameELB=createLoadBalancerResult.getDNSName();//retrieve the public DNS name
		
		//call the respective methods 
		
		// 1st call the launch config method and store the autoscaling client for further use
		AmazonAutoScalingClient amazonAutoScalingClient = instanceLaunchConfig(secGroupName,secId,amazonElasticLoadBalancingClient);
		
		//create autoscaling group and store the cloud watch client for reference
		AmazonCloudWatchClient amazonCloudWatchClient=createAutoScalingGroup(lbName,amazonAutoScalingClient,amazonElasticLoadBalancingClient);
		
		//warm up ELB
		warmUpELB(publicDnsNameELB);
		
		//Call the launchjunior test and store the testid 
		String testId=launchJuniorTest(publicDnsNameELB);
		//System.out.println(testId);
		//System.out.println("Test Sleep");
		
		
		Thread.sleep(2910000);//Sleep for 48 mins for test to complete
		
		//Finally delete the configs
		deleteConfig(amazonElasticLoadBalancingClient, amazonAutoScalingClient, amazonCloudWatchClient);
		
		//And the security group delete request
		DeleteSecurityGroupRequest deleteSecurityGroupRequest = new DeleteSecurityGroupRequest();
		deleteSecurityGroupRequest.withGroupName(secGroupName);
		
		amazonEC2Client.deleteSecurityGroup(deleteSecurityGroupRequest);


	}
	
	static AmazonAutoScalingClient instanceLaunchConfig(String secGroupName,String secId,AmazonElasticLoadBalancingClient amazonElasticLoadBalancingClient)
	{
		//System.out.println("Enter LC");
		String lcName="ASGLCP22TC"+System.currentTimeMillis();//For the LC NAme
		sLcName=lcName;//Assign to the global variable
		
		AmazonAutoScalingClient amazonAutoScalingClient = new AmazonAutoScalingClient();//create the AutoScaling Client
		InstanceMonitoring instanceMonitoring = new InstanceMonitoring();//for enabling detailed monitoring
		instanceMonitoring.setEnabled(true);//setting detailed monitoring to true
		
		//creating the launch config request with the parameters
		CreateLaunchConfigurationRequest createLaunchConfigurationRequest = new CreateLaunchConfigurationRequest();
		createLaunchConfigurationRequest.withImageId("ami-7c0a4614")
										.withInstanceType("m3.medium")
										.withLaunchConfigurationName(lcName)
										.withSecurityGroups(secGroupName)
										.withInstanceMonitoring(instanceMonitoring);
		
		//Creating the client with the LC
		amazonAutoScalingClient.createLaunchConfiguration(createLaunchConfigurationRequest);
		//System.out.println("Done LC");
		return amazonAutoScalingClient;// return ASG client for further use.
		
										
	}
	
	static AmazonCloudWatchClient createAutoScalingGroup(String lbName,AmazonAutoScalingClient amazonAutoScalingClient, AmazonElasticLoadBalancingClient amazonElasticLoadBalancingClient)
	{
		String asgName="ASGP22TC"+ System.currentTimeMillis();//for creating the ASG name uniquely 
		sAsgName= asgName;//assign to the global variable
		//System.out.println("Enter ASG");
		//CreateTag -- withTag
				Tag tag = new Tag();
				tag.setKey("Project");
				tag.setValue("2.2");
				
		//Request to create the ASG with the respective parameters		
		CreateAutoScalingGroupRequest createAutoScalingGroupRequest = new CreateAutoScalingGroupRequest();
		createAutoScalingGroupRequest.withAutoScalingGroupName(asgName)
									.withMinSize(2)
									.withMaxSize(6)
									.withDesiredCapacity(2)
									.withLoadBalancerNames(lbName)
									.withHealthCheckType("ELB")
									.withAvailabilityZones("us-east-1a")
									.withTags(tag)
									.withLaunchConfigurationName(sLcName)
									.withHealthCheckGracePeriod(300);
		//Creating the ASG
		amazonAutoScalingClient.createAutoScalingGroup(createAutoScalingGroupRequest);
		
		//Request to create the policy for Scale in and Scale out
		PutScalingPolicyRequest putScalingPolicyRequest1 =new PutScalingPolicyRequest();
		putScalingPolicyRequest1.withAutoScalingGroupName(asgName)
								.withPolicyName("Scale out policy")
								.withCooldown(120)
								.withAdjustmentType("ChangeInCapacity")
								.withScalingAdjustment(1);
		//Attaching the policy to the client
		PutScalingPolicyResult putScalingPolicyResult1= amazonAutoScalingClient.putScalingPolicy(putScalingPolicyRequest1);
		
		//System.out.println("Done Pol 1");
		//Retrieving the ARN to attach it to the Alarm 
		String ARN1 = putScalingPolicyResult1.getPolicyARN();
		
		//Creating the alarm request
		PutMetricAlarmRequest putMetricAlarmRequest1 = new PutMetricAlarmRequest();
		putMetricAlarmRequest1.withAlarmName("Scale Out Alarm")
							.withMetricName("CPUUtilization")
							.withUnit("Percent")
							.withStatistic("Average")
							.withComparisonOperator("GreaterThanOrEqualToThreshold")
							.withEvaluationPeriods(1)
							.withPeriod(60)
							.withThreshold(85.00)
							.withAlarmActions(ARN1)//attaching with the above policy
							.withNamespace("AWS\\EC2");
		
		//System.out.println("Done Alarm 1");
							
		
		
		//Finally attaching the Scaling policy with the client
		amazonAutoScalingClient.putScalingPolicy(putScalingPolicyRequest1);
		
		//Similarly cretae the policy for Scale in and attach it to the Alarm request as above and attach it to the Client
		
		PutScalingPolicyRequest putScalingPolicyRequest2 =new PutScalingPolicyRequest();
		putScalingPolicyRequest2.withAutoScalingGroupName(asgName)
								.withPolicyName("Scale in policy")
								.withCooldown(120)
								.withAdjustmentType("ChangeInCapacity")
								.withScalingAdjustment(-1);
		
		//System.out.println("Done Pol 2");
		//Attch with the Client
		PutScalingPolicyResult putScalingPolicyResult2= amazonAutoScalingClient.putScalingPolicy(putScalingPolicyRequest2);
		String ARN2 = putScalingPolicyResult2.getPolicyARN();//For attaching to the alarm
		
		PutMetricAlarmRequest putMetricAlarmRequest2 = new PutMetricAlarmRequest();//Request for the Alarm
		putMetricAlarmRequest2.withAlarmName("Scale In Alarm")
							.withMetricName("CPUUtilization")
							.withUnit("Percent")
							.withStatistic("Average")
							.withComparisonOperator("LessThanOrEqualToThreshold")
							.withEvaluationPeriods(1)
							.withPeriod(60)
							.withThreshold(65.00)
							.withAlarmActions(ARN2)//attaching the alarm with the request
							.withNamespace("AWS\\EC2");
		
		amazonAutoScalingClient.putScalingPolicy(putScalingPolicyRequest2);
		//System.out.println("Done Alarm 2");
		
		
		//Attach alarm to the cloud watch by creating the object of CW
		AmazonCloudWatchClient amazonCloudWatchClient = new  AmazonCloudWatchClient();
		amazonCloudWatchClient.putMetricAlarm(putMetricAlarmRequest1);
		amazonCloudWatchClient.putMetricAlarm(putMetricAlarmRequest2);
		
		//System.out.println("AlarmPut");
		
		
		

		return amazonCloudWatchClient;//return the Cloud Watch Client for further reference

	}	

	
	static void warmUpELB(String publicDnsNameELB)throws Exception
	{
		//System.out.println("Enter Warm up");
		String publicDnsNameLG="ec2-52-0-95-68.compute-1.amazonaws.com";//public dns of the Load generator
		String warmUpLink="http://"+publicDnsNameLG+"/warmup?dns="+publicDnsNameELB;//creating the link
		URL warmUpURL = new URL(warmUpLink);//create the URL object from the link

		loop:while(true)//continue looping until we are able to launch the link
		{
			try
			{
				BufferedReader br1 = new BufferedReader(new InputStreamReader(warmUpURL.openStream()));//launch the link by opening it
			}
			catch(Exception e)//if we get an exception implies that the instances are not ready
			{
				//System.out.println("Still Waiting");
				Thread.sleep(5000);//wait for 5:30 mins
				continue loop;//try to open the link again by continuing the loop
			}

			for(int i=0; i<2;i++)//if the link opens up implies the instances are ready
			{
				if(i==0)//for first time the link is launched so wait for 5:30 mins
				{
					System.out.println("Warm Up Sleep"+(i+1));
					Thread.sleep(330000);
				}
				else//else launch the link and wait for 5 mins
				{
					BufferedReader br2 = new BufferedReader(new InputStreamReader(warmUpURL.openStream()));//launch the link by opening it
					System.out.println("Warm Up Sleep"+(i+1));
					Thread.sleep(330000);
				}

			}
			break loop;//once launched break the loop
		}


	}
	static String launchJuniorTest(String publicDnsNameELB)throws Exception
	{
		//System.out.println("Launch Junior");
		String publicDnsNameLG="ec2-52-0-95-68.compute-1.amazonaws.com";//the DNS of the Load Generator
		String testId=null;//to return the testid after the test is launched
		//System.out.println("Entered launchTest");
		String launchLink=null;//for the test link 


		launchLink="http://"+publicDnsNameLG+"/junior?dns="+publicDnsNameELB;//create the link
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
		//System.out.println(testId);
		return testId;//return it for the monitor()
		

	}
	static void deleteConfig(AmazonElasticLoadBalancingClient amazonElasticLoadBalancingClient, AmazonAutoScalingClient amazonAutoScalingClient, AmazonCloudWatchClient amazonCloudWatchClient)
	{
		//System.out.println("Deleting Configurations");
		
		//Deleting REquest for the Alarms for Scale up and Scale down
		DeleteAlarmsRequest deleteAlarmsRequest1 = new DeleteAlarmsRequest();
		deleteAlarmsRequest1.withAlarmNames("Scale In Alarm"); 

		DeleteAlarmsRequest deleteAlarmsRequest2 = new DeleteAlarmsRequest();
		deleteAlarmsRequest2.withAlarmNames("Scale Out Alarm");

		//Deleting the Alarms
		amazonCloudWatchClient.deleteAlarms(deleteAlarmsRequest1);
		amazonCloudWatchClient.deleteAlarms(deleteAlarmsRequest2);

		//Delete Requests for deleting the policies
		DeletePolicyRequest deletePolicyRequest1 = new DeletePolicyRequest();
		deletePolicyRequest1.withPolicyName("Scale in policy")
							.withAutoScalingGroupName(sAsgName);

		DeletePolicyRequest deletePolicyRequest2 = new DeletePolicyRequest();
		deletePolicyRequest2.withPolicyName("Scale out policy")
							.withAutoScalingGroupName(sAsgName);

		//Deleting the policies
		amazonAutoScalingClient.deletePolicy(deletePolicyRequest1);
		amazonAutoScalingClient.deletePolicy(deletePolicyRequest2);


		//Creating a REquest to modify the ASG and set the Min & mAx sizes to zero
		CreateAutoScalingGroupRequest createAutoScalingGroupRequest = new CreateAutoScalingGroupRequest();
		createAutoScalingGroupRequest.withAutoScalingGroupName(sAsgName)
										.withLaunchConfigurationName(sLcName)
										.withAvailabilityZones("us-east-1a");

		createAutoScalingGroupRequest.setMaxSize(0);//set max to zero
		createAutoScalingGroupRequest.setMinSize(0);//set min to zero
		//Setting the values as per the policies above to remove all the data instances
		amazonAutoScalingClient.createAutoScalingGroup(createAutoScalingGroupRequest);

		//Deleting the ASG
		DeleteAutoScalingGroupRequest deleteAutoScalingGroupRequest = new DeleteAutoScalingGroupRequest();
		deleteAutoScalingGroupRequest.withAutoScalingGroupName(sAsgName)
										.withForceDelete(true);
		//Request to delete the launch configuration with the launch config name
		DeleteLaunchConfigurationRequest deleteLaunchConfigurationRequest = new DeleteLaunchConfigurationRequest();
		deleteLaunchConfigurationRequest.withLaunchConfigurationName(sLcName);
		//Deleting the launch config
		amazonAutoScalingClient.deleteLaunchConfiguration(deleteLaunchConfigurationRequest);

		//Request to delete the load balancer with the specific name
		DeleteLoadBalancerRequest deleteLoadBalancerRequest = new DeleteLoadBalancerRequest();
		deleteLoadBalancerRequest.withLoadBalancerName(sLbName);

		//Deleting the Load Balancer
		amazonElasticLoadBalancingClient.deleteLoadBalancer(deleteLoadBalancerRequest);

	}
}
	