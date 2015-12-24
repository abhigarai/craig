package p41;
import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Mapper1Code {

  public static class phraseMapper
       extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable one = new IntWritable(1);//for printing the value as one
    private Text word = new Text();
    String outputResult=null;

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {//Mapper Function  
    	String line = value.toString();				//Read every Line from the input location i.e. S3
		String parts[]=line.split("\\P{Alpha}+");	// Split it based on all non-alphabetic characters i.e. numbers, underscore, punctuation
		
		for(int i=0;i<parts.length;i++)//iterate over the whole split array
 		{								
			for(int j=i;j<i+5&&j<parts.length;j++)//iterate until the length is between 1 to 5 and the index is within the length
			{
				StringBuilder result=new StringBuilder();//create a result object
				for(int k=i;k<=j;k++)
				{
					result.append(parts[k].toLowerCase()+" ");//convert it into lowercase and append it to result
				}
				outputResult=result.toString();
				outputResult=outputResult.substring(0, outputResult.length()-1);//remove the trailing space
				word.set(outputResult);//set the value of the key
				if(outputResult.length()>=1)//eliminating keys having empty value 
				{
					context.write(word, one);//print the output of mapper
				}
				
			}
		}	
    }
  }

  public static class IntSumReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;						//create a sum variable to collect the values
      for (IntWritable val : values) {
        sum += val.get();	//add the values for each key of the reducer
      }
      result.set(sum);//set the value for the output
      context.write(key, result); //output the result
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Mapper1Code");
    job.setJarByClass(Mapper1Code.class);
    job.setMapperClass(phraseMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}