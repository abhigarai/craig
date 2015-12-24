package project42;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.FlatMapFunction2;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.rdd.RDD;

import scala.Tuple2;

public class Project42 {

	@SuppressWarnings("unchecked")
	public static void main(String[] args)throws Exception {
		// TODO Auto-generated method stub

		SparkConf conf = new SparkConf();
		JavaSparkContext spark = new JavaSparkContext(conf);
		JavaRDD file = spark.textFile("s3n://15619cloudbucket/p422/testw");
		

		//REMOVE SPECIAL SYMBOLS AND SEPEARTE BY TAB
		JavaRDD<List<String>> filterFile = file.map(new Function<String, List<String>>()
				{
					public List<String> call(String s) 
					{ 
						String parts[]=s.split("\t");
						String xmlDoc[]=parts[3].replaceAll("<.*?>", " ").replaceAll("\\\\n", " ").replaceAll("[^a-zA-Z]+", " ").replaceAll(" ", "Garai").split("Garai");
						
						StringBuilder sb=new StringBuilder();
						for(int i=0;i<xmlDoc.length;i++)
						{
							sb.append(xmlDoc[i]+" ");
						}						
							String str= sb.toString().trim();
						return Arrays.asList(parts[1],str);
					}
				});

		//Total Documents-BY TITLE----------------------------------------------------------------------------------------------
		JavaRDD<String> titleFile = file.map(new Function<String, String>()
				{
					public String call(String s) 
					{
						String parts[]=s.split("\t");
						String result=parts[1];
						return result; 
					}
				}).distinct();
		
		
		final long N=titleFile.count();//STORING TOTAL COUNT
		// N CALCULATED----------------------------------------------------------------------------------------------------------

		//-Start df caculation-----------------------------------------------------------------------------------------------------
		//RETRIEVE WORD-TITLE RDD FOR PROCESSING
		JavaRDD<String> wordTitleRDD1 = filterFile.flatMap(new FlatMapFunction<List<String>,String>()
				{
					public Iterable<String> call(List<String> iList) 
					{ 	  		
						List<String> oList=new ArrayList<String>();	
						String title=iList.get(0);
						String parts[]=iList.get(1).split("\\s+");
						for(int i=0;i<parts.length;i++)
						{
							String result=parts[i]+"Garai"+title;
							oList.add(result);	
						}
						return oList; 
					}
				});
		//**********************************DF CALCULATION START**********************************************
		//CONVERT TO PAIR FOR PROCESSING
		JavaPairRDD<String,String> wordTitleRDD = wordTitleRDD1.mapToPair(new PairFunction<String, String, String>() 
				{
					public Tuple2<String,String> call(String s) 
					{  
						String parts[]=s.split("Garai");
						return new Tuple2(parts[0],parts[1]);
					}
				});
		
		//CALCULATE THE DF FOR EACH WORD
		JavaPairRDD<String,Integer> df = wordTitleRDD.groupByKey().mapValues(new Function<Iterable<String>, Integer>() {

			@Override
			public Integer call(Iterable<String> arg0) throws Exception {
				Set<String> setOfTitles = new HashSet<String>();
				for (String arg : arg0)
					setOfTitles.add(arg);
				return setOfTitles.size();
			}
		});

		//***************************************DF CALCULATION END*****************************************************************

		//----------------IDF CALCULATION START----------------------------------------------------------------------------------------

				JavaPairRDD<String,Double> idf = df.mapValues(new Function<Integer,Double>() 
						{
							@Override
							public Double call(Integer a) throws Exception {
								// TODO Auto-generated method stub
								return Math.log(N/a);
					}
						});
				
				

		//MAPPER FUNCTION
		JavaPairRDD<String,Integer> mapFile1 = wordTitleRDD1.mapToPair(new PairFunction<String, String, Integer>() 
				{
					public Tuple2<String,Integer> call(String s) 
					{  
						return new Tuple2(s, 1); 
					}
				});
							/////////DOUBT BUtCLEARED///////
		//REDUCER FUNCTION
		JavaPairRDD<String,Integer> tf1 = mapFile1.reduceByKey(new Function2<Integer,Integer,Integer>() 
				{
			@Override
					public Integer call(Integer a, Integer b) throws Exception {
						// TODO Auto-generated method stub
						return a+b;
					}
				});
		//===========================================================================================================================

		
		//pair-flatmap-pair
		RDD tf2=(RDD)JavaPairRDD.toRDD(tf1);//CONVERT PAIR TO RDD
		JavaRDD tf3=(JavaRDD)tf2.toJavaRDD();//CONVERT RDD TO JAVARDD*/
		
		JavaRDD<String> tf4 = tf3.map(new Function<Tuple2<String,Integer>, String>() 
				{
					public String call(Tuple2<String,Integer> tuple2) 
					{  
						String s=tuple2.toString();
						int c=s.lastIndexOf(",");
						int tf=Integer.parseInt(s.substring(c+1, s.length()-1));
						String temp=s.substring(1, c);
						String word=temp.split("Garai")[1];
						String title=temp.split("Garai")[0];
						String result=word+"Abhishek"+title+"Garai"+tf;
						
						return result;
						

					}
				});
	
		JavaPairRDD<String,String> tf5 = tf4.mapToPair(new PairFunction<String,String,String>() 
				{
			@Override
					public Tuple2<String,String> call(String s) throws Exception {
						// TODO Auto-generated method stub
				
						String parts[]=s.split("Abhishek");
						//String part[]=parts[1].split("Garai");
						return new Tuple2(parts[0],parts[1]);
					}
				});
		
		JavaPairRDD<String,Iterable<String>> tf = tf5.groupByKey();
		
		
		
		
		
		//tf-calculation ends------------------------------------------------------------------------------------------------------


		//PERFORM JOIN IDF AND TF
		JavaPairRDD<String,Tuple2<Iterable<String>,Double>> tfIdf1=tf.join(idf);
		
		JavaRDD<Tuple2<String,List<String>>> tfIdff = tfIdf1.map(new Function<Tuple2<String,Tuple2<Iterable<String>,Double>>, Tuple2<String,List<String>>>() 
				{
					public Tuple2<String,List<String>> call(Tuple2<String,Tuple2<Iterable<String>,Double>> tuple2) 
					{  
						List<String> oList=new ArrayList<String>();
						String word=tuple2._1();
						Tuple2 tuple=tuple2._2();
						Double idf=Double.parseDouble(tuple._2().toString());
						
						Iterable<String> tempTuple2= (Iterable)tuple._1();
						
						for(String temp:tempTuple2)
						{
							Double tfIdf=idf*Double.parseDouble(temp.split("Garai")[1]);
							String result=temp.split("Garai")[0]+"Garai"+tfIdf;
							oList.add(result);
						}				
						return new Tuple2(word,oList);
					}
				});
		
	
		
		
		
	
		File outfile = new File ("/root/output/tfidf");
		FileWriter fw = new FileWriter(outfile);
		BufferedWriter bw = new BufferedWriter(fw);
///+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		
		JavaRDD<List<String>> tfIdf = tfIdff.map(new Function<Tuple2<String,List<String>>,List<String>>() 
				{
					public List<String> call(Tuple2<String,List<String>> tuple2) 
					{  
						List<String> oList=new ArrayList<String>();
						String word=tuple2._1();
						if(word.equals("parish"))
						{
							for(String s:tuple2._2())
							{
								oList.add(s);
							}
						}
						return oList;
					}
				});
		
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		
		//CONVERT TO ARRAY FOR SORTING
		List<String> sortingList=tfIdf.collect().get(0);
	
		
		
		//PUT TO TREESET FOR SORTING
		TreeSet<SortClass> sortSet = new TreeSet<SortClass>();
		
		for(List<String> ls:sortingList)
		{
			for(String temp1:sortingList)
			{
			SortClass s=new SortClass(temp1);
			sortSet.add(s);
			}
		}

		int k=1;			//counter to iterate
		for (SortClass element : sortSet) {
			if(k<101)
			{
				bw.write(k);
				bw.write(element.toString());
				k++;
			}
			else
			{
				break;
			}
		}
		bw.close();
		fw.close();
	}

	
	private static class SortClass implements Comparable<SortClass> {

		String title ="dummy";
		double tfIdf = 0;	
		String returnValue="dummy";
		SortClass(String s) 
		{	
			tfIdf=Double.parseDouble(s.split("Garai")[1]);	
			title=s.split("Garai")[0];
			returnValue=title+"\t"+tfIdf;
		}
		public String toString() //to return the result
		{
			return returnValue;
		}

		@Override
		public int compareTo(SortClass o) //compare function to sort in descending order of count and ascending order of phrase
		{
			double diff = this.tfIdf - o.tfIdf;

			if (diff == 0)//if diff is same sort on phrase in ascending
			{
				if(this.title.compareTo(o.title)==0) 
					return 0;
				else if(this.title.compareTo(o.title)<0)
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
}
	

