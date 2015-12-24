import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

public class Query6 {
	public static class Map extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, Text> {
		private Text outputKey = new Text();
		private Text outputValue = new Text();

		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			String userIdTweeter = null, place = null;
			String currentLine = value.toString();

			if (!currentLine.isEmpty()) {
				try {
					Object currentLineObj = JSONValue.parse(currentLine);

					JSONObject currentLineJSONObject = (JSONObject) currentLineObj;
					JSONObject userIdArray = (JSONObject) currentLineJSONObject
							.get("user");
					userIdTweeter = userIdArray.get("id").toString();

					JSONObject placeArray = (JSONObject) currentLineJSONObject
							.get("place");

					outputKey.set(userIdTweeter);

					if (placeArray != null) {
						place = placeArray.get("name").toString();
						if (place == null) {
							outputValue.set("0");
						} else {
							outputValue.set("1");
						}
					} else {
						outputValue.set("0");
					}

					output.collect(outputKey, outputValue);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}
	}

	public static class Reduce extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {

			String currentLine = null;
			boolean location = false;

			try {

				while (values.hasNext()) {

					String value = values.next().toString();

					if (Integer.parseInt(value) == 1) {
						location = true;
						break;
					}
				}

				if (!location) {
					output.collect(key, null);
				}

			} catch (Exception e) {
				System.out.println(e);
			}

		}
	}

	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(Query6.class);
		conf.setJobName("Query6");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(Map.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);
	}
}
