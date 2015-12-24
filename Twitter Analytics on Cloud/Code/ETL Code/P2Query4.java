import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

/**
 * MapReduce for Query 4
 * 
 * @author Matt
 *
 */
public class Query4 {

	/**
	 * Mapper class
	 * 
	 * @author Matt
	 *
	 */
	public static class Map extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, Text> {

		// hash tag
		private Text hash_tag = new Text();
		// info containin tweetid, userid and date
		private Text info = new Text();

		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {

			String tweet = null, userId = null, tweetId = null, stringDate = null;

			String currentLine = value.toString();

			if (!currentLine.isEmpty()) {
				try {
					Object currentLineObj = JSONValue.parse(currentLine);
					JSONObject currentLineJSONObject = (JSONObject) currentLineObj;

					stringDate = currentLineJSONObject.get("created_at")
							.toString();

					DateFormat df = new SimpleDateFormat("MMM dd HH:mm:ss yyyy");

					Date date = df
							.parse((stringDate.substring(4, 19) + " " + stringDate
									.substring(26)));

					// see if hashtag has non-alphanumeric characters
					Pattern p = Pattern.compile("[^a-zA-Z0-9]");

					// Output format
					SimpleDateFormat out = new SimpleDateFormat(
							"yyyy-MM-dd+HH:mm:ss");

					JSONObject userObject = (JSONObject) currentLineJSONObject
							.get("user");
					userId = userObject.get("id").toString();
					tweetId = currentLineJSONObject.get("id").toString();
					JSONArray hashTags = (JSONArray) ((JSONObject) currentLineJSONObject
							.get("entities")).get("hashtags");

					// for each hashtag create an output
					for (int i = 0; i < hashTags.size(); i++) {
						JSONObject hashtag = (JSONObject) hashTags.get(i);
						String hashTag = hashtag.get("text").toString();

						if (!p.matcher(hashTag).find()) {
							hash_tag.set(hashTag);
							info.set(tweetId + "|,\t|" + userId + "|,\t|"
									+ out.format(date));
							output.collect(hash_tag, info);
						}
					}

				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}
	}

	/**
	 * Combiner class
	 * 
	 * @author Matt
	 *
	 */
	public static class Combiner extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {
		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {

			while (values.hasNext()) {
				output.collect(key, values.next());
			}
		}
	}

	/**
	 * Reducer class
	 * 
	 * @author Matt
	 *
	 */
	public static class Reduce extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {
		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {

			String temp = key.toString();

			temp = "#&#|" + temp + "|,";

			key.set(temp);

			while (values.hasNext()) {
				StringBuilder tweet = new StringBuilder("|");
				tweet.append(values.next().toString());
				tweet.append("|");

				// setting output value
				Text resultText = new Text();
				resultText.set(tweet.toString());

				output.collect(key, resultText);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(Query4.class);
		conf.setJobName("Query4");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(Query4.Map.class);
		conf.setCombinerClass(Query4.Combiner.class);
		conf.setReducerClass(Query4.Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);
	}
}
