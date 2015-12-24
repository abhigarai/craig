import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
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

public class Query5 {
	public static class Map extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, Text> {

		private Text outputValue = new Text();
		private Text outputKey = new Text();

		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			String userId = null, stringDate = null, tweetId = null, friendsCount = null, followersCount = null;
			String currentLine = value.toString();

			try {
				if (!currentLine.isEmpty()) {
					Object currentLineObj = JSONValue.parse(currentLine);
					JSONObject currentLineJSONObject = (JSONObject) currentLineObj;

					JSONObject userIdArray = (JSONObject) currentLineJSONObject
							.get("user");
					userId = userIdArray.get("id").toString();

					friendsCount = userIdArray.get("friends_count").toString();

					followersCount = userIdArray.get("followers_count")
							.toString();

					DateFormat df = new SimpleDateFormat("MMM dd yyyy");
					stringDate = currentLineJSONObject.get("created_at")
							.toString();

					Date tempDate = df
							.parse((stringDate.substring(4, 10) + " " + stringDate
									.substring(26)));

					SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd");

					String displayDate = out.format(tempDate);

					tweetId = currentLineJSONObject.get("id").toString();

					outputKey.set(userId + "#" + displayDate);

					outputValue.set(tweetId + "#" + friendsCount + "#"
							+ followersCount);

					output.collect(outputKey, outputValue);
				}
			} catch (Exception e) {
				System.out.println(e);
			}

		}
	}

	public static class Reduce extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {
		private Text outputValue = new Text();
		private Text outputKey = new Text();

		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {

			int tweetCount = 0, maxFriendsCount = 0, maxFollowersCount = 0;
			HashSet<String> tweetId = new HashSet<String>();

			String currentValue = null;
			String KeyString = key.toString();
			String partsKey[] = KeyString.split("#");

			try {

				while (values.hasNext()) {

					currentValue = values.next().toString();

					String parts[] = currentValue.split("#");

					if (!tweetId.contains(parts[0])) {
						tweetCount++;
						tweetId.add(parts[0]);
					}
					if (Integer.parseInt(parts[1]) > maxFriendsCount) {
						maxFriendsCount = Integer.parseInt(parts[1]);
					}
					if (Integer.parseInt(parts[2]) > maxFollowersCount) {
						maxFollowersCount = Integer.parseInt(parts[2]);
					}

					outputKey.set(partsKey[0]);
					outputValue.set(partsKey[1] + "\t" + tweetCount + "\t"
							+ maxFriendsCount + "\t" + maxFollowersCount);

				}
			} catch (Exception e) {
				System.out.println(e);
			}

			output.collect(outputKey, outputValue);
		}
	}

	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(Query5.class);
		conf.setJobName("Query5");

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
