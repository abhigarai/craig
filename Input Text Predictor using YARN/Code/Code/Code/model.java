package p41;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class Mapper2Code {

	public static class TokenizerMapper
	extends Mapper<Object, Text, Text, Text>{
		private String newValue=new String();//for storing the formatted output value

		public void map(Object key, Text value, Context context
				) throws IOException, InterruptedException {

			Configuration config = context.getConfiguration();	//for retrieving the configuration to get the parameter values
			String threshold = config.get("threshold");			//read the value of threshold from the configuration object

			String line = value.toString();					//read each line from the input
			String outputKey=new String();					//for displaying the formatted key 
			String parts[]=line.toString().split("\t");			//separate it on tab to separate into the key and VALUE1 
			int a=Integer.parseInt(parts[1]);					//store the value as after split the value is tab seperated to the right
			int b=Integer.parseInt(threshold);					//store the threshold read

			String keyParts[]=parts[0].split("\\s+");	//separate the key part on space to count the number of words in the key 
			if(keyParts.length==1)				//if it contains only one word then its just the key and the value
			{	
				if(a>Integer.parseInt(threshold)) //check if the value is greater than threshold
				{
					context.write(new Text(parts[0]), new Text(parts[1]));//output the key and value pair
				}
			}
			else//else if key contains more than one word
			{
				if(a>Integer.parseInt(threshold))//check for greater than the threshold 
				{
					context.write(new Text(parts[0]), new Text(parts[1]));//output in the default key value format
					StringBuilder appendKey=new StringBuilder();
					for(int i=0;i<keyParts.length-1;i++)//iterate over the key to print all the words except the last
					{
						appendKey.append(keyParts[i]+" ");		//and keep appendng it to the result
					}
					outputKey=appendKey.toString();//convert to string
					outputKey=outputKey.substring(0, outputKey.length()-1);//remove the trailing space
					newValue=keyParts[keyParts.length-1]+" "+parts[1];	//generate the value by concatenating the last word from the key and the count or VALUE1
					context.write(new Text(outputKey), new Text(newValue));//output as key value pair
				}
			}
		}
	}

	public static class IntSumReducer
	extends TableReducer<Text,Text,ImmutableBytesWritable> {
		public void reduce(Text key, Iterable<Text> values,
				Context context
				) throws IOException, InterruptedException {
			double baseCount=0;
			HashMap<String,Double> topN=new HashMap<String,Double>();//to store the topn values of probabilities
			Configuration config = context.getConfiguration();//retrieve the configuration
			String threshold = config.get("topN");  //read the value of threshold from the configuration 

			TreeSet<SortClass> sortSet = new TreeSet<SortClass>();//create a tree set to store the results in sorted order

			for(Text temp:values)//iterate over the values to store it in the tree set
			{
				String value=temp.toString();//convert it into string			
				sortSet.add(new SortClass(key.toString(),value));//pass the key and each value to add it to the treeset in sorted order
			}													// the key is required for keeping the base count for the root word
			for(SortClass element : sortSet)//iterate over the set to find the base count
			{
				if(!element.toString().contains(":"))//if element does not contain ":" that means it is the root phrase
				{
					baseCount=Double.parseDouble(element.toString());//assign the second part to be the value
					break;//once got it break out
				}
				else
				{
					continue;//else keep continuing - this is although not required but had been put to avoid an infinite loop
							//not required because the first element in the treeset is teh required root phrase
				}
			}
			//now data is stored in sorted order and we have the base count, iterate over it to calculate the probability and store top 5 in a hashmap
			int k=1;			//counter to iterate over and keep track of the number of elements
			for (SortClass element : sortSet) {//after sorting the elements are seperated as key value by a : to seperate from the /t and /s
				if(k<Integer.parseInt(threshold) && element.toString().contains(":")==true)//further for the next 5 iterate upto the threshold value
				{//in the above line check for the thresh hold value and also check if it contains a ":"--explained in the sort class to check if its not the root word
						String multipleParts[]=element.toString().split(":");//split it on ":" as seperated by the sort class
						topN.put(multipleParts[0].toString(),((Double.parseDouble(multipleParts[1].toString()))/baseCount));//store as key and probability
						k++;							//above is the calculation of the probability									
				}
				else//else condition to avoid any kind of infinite loop
				{
					if(k==6)//if already 5 elements are read then k is 6 so break out
					{
						break;
					}
					else//else continue for  the next element
					{//this is just to handle the first element
						continue;
					}
				}
			}
			Iterator iterator = topN.entrySet().iterator();//iterate over the map
			int m=1;//although not required but put for debugging
				//thhis keeps a count on the number of elemnts to write to hbase
			while (iterator.hasNext()) //check if it has the next value
			{
				if(m<6)//if already 5 elements are written as m is incremented after writing an element to hbase
				{
				m++;//increment
				Map.Entry pair = (Map.Entry)iterator.next();//retrieve the next element
				Put put = new Put(Bytes.toBytes(key.toString()));//create a put object
				put.add(Bytes.toBytes("phrase"), Bytes.toBytes(pair.getKey().toString()), Bytes.toBytes(pair.getValue().toString()));//put the value
				context.write(null,put);//store it in the table as per the above command
				}
				else
				{
					break;//else after 5 elements break
				}
			}
		}
	}

	private static class SortClass implements Comparable<SortClass> {//defined to sort the elements of the treeset 

		String returnLine=null;//variable for the formated result
		String phrase = null;//variable for the phrase
		int count = 0;	//variable for the count
		SortClass(String key, String value) 
		{	
			String valueParts[]=value.split("\\s+");//split the value on spaces to check if it contains only count or not 
			if(valueParts.length==1)//if length is 1 it contains only count
			{	
				phrase="";	//then phrase is "" as compareto function gives preference to blank over words
				count=Integer.parseInt(valueParts[0]);//and count would be the value part  having the single value
				returnLine=Integer.toString(count);//read the phrase and the count and generate the result to be returned after sorting*/
			}
			else//if it contains more than one parts
			{
				phrase=valueParts[0];//then 1st part is the phrase 
				count=Integer.parseInt(valueParts[1]);//and second part is the count
				returnLine=phrase+":"+count;//generate the result
			}
		}
		public String toString() //to return the result
		{
			return returnLine;
		}

		@Override
		public int compareTo(SortClass o) //compare function to sort in descending order of count and ascending order of phrase
		{
			long diff = this.count - o.count;

			if (diff == 0)//if diff is same sort on phrase in ascending
			{
				if(this.phrase.compareTo(o.phrase)==0) 
					return 0;
				else if(this.phrase.compareTo(o.phrase)<0)
					return -1;
				else
					return 1;
			}
			else if (diff < 0)//logic for descending
				return 1;
			else
				return -1;
		}
	}

	public static void main(String[] args) throws Exception {

		Configuration conf = HBaseConfiguration.create();//create a configuration object assigning it to the hbase table
		Job job = Job.getInstance(conf, "Mapper2Code");
		String[] otherArgs=new GenericOptionsParser(conf,args).getRemainingArgs();
		System.out.println(otherArgs[0]);
		System.out.println(otherArgs[1]);
		System.out.println(Integer.parseInt(otherArgs[2]));
		System.out.println(Integer.parseInt(otherArgs[3]));
		job.setJarByClass(Mapper2Code.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setReducerClass(IntSumReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(ImmutableBytesWritable.class);

		job.getConfiguration().set("threshold", otherArgs[2]);

		job.getConfiguration().set("topN", otherArgs[3]);

		TableMapReduceUtil.initTableReducerJob("predictNext", IntSumReducer.class, job);

		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
