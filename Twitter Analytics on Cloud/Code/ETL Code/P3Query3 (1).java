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

public class Query3 {

	/**
	 * Mapper Class
	 * 
	 * @author Matt
	 *
	 */
	public static class Map extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, Text> {

		private Text outputKey = new Text();
		private Text outputValue = new Text();

		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {

			String userIdTweeter = null, userIdRetweeter = null;
			String currentLine = value.toString();

			if (!currentLine.isEmpty()) {

				try {
					Object currentLineObj = JSONValue.parse(currentLine);
					JSONObject currentLineJSONObject = (JSONObject) currentLineObj;
					JSONObject userIdArray = (JSONObject) currentLineJSONObject
							.get("user");
					userIdTweeter = userIdArray.get("id").toString();

					JSONObject reTweetedLineJSONObj = (JSONObject) currentLineJSONObject
							.get("retweeted_status");
					JSONObject reTweetedUserIdArray = (JSONObject) reTweetedLineJSONObj
							.get("user");
					userIdRetweeter = reTweetedUserIdArray.get("id").toString();

					outputKey.set(userIdTweeter);
					outputValue.set(userIdRetweeter + "," + "reTweeted");

					output.collect(outputKey, outputValue);

					outputKey.set(userIdRetweeter);
					outputValue.set(userIdTweeter + "," + "reTweetedBy");

					output.collect(outputKey, outputValue);
				} catch (Exception e) {
					e.printStackTrace();
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
	 * Reducer Class
	 * 
	 * @author Matt
	 *
	 */
	public static class Reduce extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {

		private static class CountClass implements Comparable<CountClass> {
			long user = 0;
			int count = 0;

			CountClass(String user, int count) {
				this.user = Long.parseLong(user);
				this.count = count;
			}

			public String toString() {
				return "" + count + "," + user;
			}

			@Override
			public int compareTo(CountClass o) {
				int diff = this.count - o.count;
				if (diff != 0)
					return -diff;
				else {
					if (this.user < o.user)
						return -1;
					else if (this.user > o.user)
						return +1;
					else
						return 0;
				}
			}
		}

		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {

			HashMap<String, Integer> plus = new HashMap<String, Integer>();
			HashMap<String, Integer> minus = new HashMap<String, Integer>();
			HashMap<String, Integer> star = new HashMap<String, Integer>();

			TreeSet<CountClass> plusSet = new TreeSet<CountClass>();
			TreeSet<CountClass> minusSet = new TreeSet<CountClass>();
			TreeSet<CountClass> starSet = new TreeSet<CountClass>();

			String currentLine = null;

			StringBuilder result = new StringBuilder("|");

			while (values.hasNext()) {
				currentLine = values.next().toString();
				String[] parts = currentLine.split(",");

				if (parts.length == 2) {
					String user = parts[0];
					String tweet_type = parts[1];

					if (tweet_type.equals("reTweeted")) {
						if (star.containsKey(user)) {
							int count = star.get(user);
							star.put(user, count + 1);
						} else if (minus.containsKey(user)) {
							int count = minus.get(user);
							star.put(user, count + 1);
							minus.remove(user);
						} else if (plus.containsKey(user)) {
							int count = plus.get(user);
							plus.put(user, count + 1);
						} else {
							plus.put(user, 1);
						}
					} else {
						if (star.containsKey(user)) {
							int count = star.get(user);
							star.put(user, count + 1);
						} else if (plus.containsKey(user)) {
							int count = plus.get(user);
							star.put(user, count + 1);
							plus.remove(user);
						} else if (minus.containsKey(user)) {
							int count = minus.get(user);
							minus.put(user, count + 1);
						} else {
							minus.put(user, 1);
						}
					}
				}
			}

			CountClass temp = null;

			for (String each : star.keySet()) {
				starSet.add(new CountClass(each, star.get(each).intValue()));
			}
			for (String each : plus.keySet()) {
				plusSet.add(new CountClass(each, plus.get(each).intValue()));
			}
			for (String each : minus.keySet()) {
				minusSet.add(new CountClass(each, minus.get(each).intValue()));
			}

			for (CountClass each : starSet) {
				result.append("*," + each.toString() + "\n");
			}
			for (CountClass each : minusSet) {
				result.append("+," + each.toString() + "\n");
			}
			for (CountClass each : plusSet) {
				result.append("-," + each.toString() + "\n");
			}

			result.append("|");

			String tempKey = key.toString();

			tempKey = "#&#|" + tempKey + "|,";

			key.set(tempKey);

			// setting output value
			Text resultText = new Text();
			resultText.set(result.toString());

			output.collect(key, resultText);
		}
	}

	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(Query3.class);
		conf.setJobName("Query3");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(Query3.Map.class);
		conf.setCombinerClass(Query3.Combiner.class);
		conf.setReducerClass(Query3.Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);
	}
}
