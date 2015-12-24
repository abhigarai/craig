package project42;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.rdd.RDD;

import scala.Tuple2;

public class project422 {

	@SuppressWarnings("unchecked")
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		
		SparkConf conf = new SparkConf();
		JavaSparkContext spark = new JavaSparkContext(conf);
		JavaRDD file = spark.textFile("s3n://15619cloudbucket/p4222/pagerank");
		JavaRDD file1 = spark.textFile("s3n://15619cloudbucket/p4222/mapping");
		
		//KEEP AN RDD FOR MAPPINGS
		JavaPairRDD<Integer,String> mappings = file1.mapToPair(new PairFunction<String,Integer,String>()
				{
					public Tuple2 call(String s) 
					{ 
						String parts[]=s.split("\t");
						return new Tuple2(Integer.parseInt(parts[0]),parts[1]); 
					}
				});
		
		//RETURN AS A PAIR
		JavaPairRDD<Integer,Integer> adjacencyList1 = file.mapToPair(new PairFunction<String, Integer, Integer>() 
				{
					public Tuple2 call(String s) 
					{  
						String parts[]=s.split("\t");
						return new Tuple2(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]));
					}
				}).distinct();
		

		JavaPairRDD<Integer,Integer> contribTempRDD = file.mapToPair(new PairFunction<String, Integer, Integer>() 
				{
					public Tuple2 call(String s) 
					{  
						String parts[]=s.split("\t");
						return new Tuple2(Integer.parseInt(parts[1]),Integer.parseInt(parts[0]));
					}
				});
		
		final JavaPairRDD<Integer,Iterable<Integer>> contributingNodes = contribTempRDD.groupByKey();
		
		
		JavaPairRDD<Integer,Integer> numberOfContributingNodes = contributingNodes.mapValues(new Function<Iterable<Integer>, Integer>() {

			@Override
			public Integer call(Iterable<Integer> arg0) throws Exception {
				Set<Integer> setOfNodes = new HashSet<Integer>();
				for (Integer arg : arg0)
					setOfNodes.add(arg);
				return setOfNodes.size();
			}
		});
		
		
		JavaPairRDD<Integer,Iterable<Integer>> adjacencyList = adjacencyList1.groupByKey();
		
		//CALCULATE THE NUMBER OF ADJACENT NODES FOR EACH NODE
			final	JavaPairRDD<Integer,Integer> numberAdjacentNodes = adjacencyList.mapValues(new Function<Iterable<Integer>, Integer>() {

					@Override
					public Integer call(Iterable<Integer> arg0) throws Exception {
						Set<Integer> setOfNodes = new HashSet<Integer>();
						for (Integer arg : arg0)
							setOfNodes.add(arg);
						return setOfNodes.size();
					}
				});

			
				
				//CALCULATE THE NUMBER OF UNIQUE NODES
				JavaRDD<Integer> uniqueNodes1 = file.map(new Function<String, Integer>()
						{
							public Integer call(String s) 
							{
								String parts[]=s.split("\t");
								int result=Integer.parseInt(parts[0]);
								return result; 
							}
						}).distinct();
				long N1=uniqueNodes1.count();
				
				JavaRDD<Integer> uniqueNodes2 = file.map(new Function<String, Integer>()
						{
							public Integer call(String s) 
							{
								String parts[]=s.split("\t");
								int result=Integer.parseInt(parts[1]);
								return result; 
							}
						}).distinct();
				long N2=uniqueNodes1.count();
				
				//TOTAL NODES
				final long N=N1+N2;
				
				
		JavaRDD<Integer> danglingNodes1=uniqueNodes2.subtract(uniqueNodes1);
		
		JavaPairRDD<Integer,Integer> danglingNodes = danglingNodes1.mapToPair(new PairFunction<Integer, Integer, Integer>() 
				{
					public Tuple2 call(Integer a) 
					{  
						return new Tuple2(a,1);
					}
				});
				
		//final		List<Integer> danglingNodesList=danglingNodes.collect();
				
				JavaRDD<Integer> allNodes =uniqueNodes1.union(uniqueNodes2);
				
				//MAPPING BETWEEN NODE AND PAGERANK
				JavaPairRDD<Integer,Integer> pageRank = uniqueNodes1.mapToPair(new PairFunction<Integer, Integer, Integer>() 
						{
							public Tuple2 call(Integer a) 
							{  
								return new Tuple2(a,1);
							}
						});
				
						
				JavaPairRDD rankAndNumberofAdjacentNodes=pageRank.join(numberAdjacentNodes);
				JavaPairRDD iterativePageRankStructure=rankAndNumberofAdjacentNodes.join(contributingNodes);
				
				
		for(int i=1;i<=10;i++)
		{
			RDD pageRank1=JavaPairRDD.toRDD(pageRank);//CONVERT PAIR TO RDD
			JavaRDD pageRank2=pageRank1.toJavaRDD();
		
			final JavaPairRDD<Integer,Integer> pageRank3=pageRank2.mapToPair(new PairFunction<Tuple2,Integer,Integer>()
				{
				public Tuple2 call(Tuple2 tuple2)
				{
					return new Tuple2(tuple2._1(),tuple2._2());
			
				}});
					
				 pageRank = pageRank2.mapToPair(new PairFunction<Tuple2,Integer,Integer>() 
					{
						public Tuple2 call(Tuple2 tuple2) 
						{  
							double newPageRank=0;
							double danglingTotalContributions=0;
							double nodeTotalContributions=0;
							int node=Integer.parseInt(tuple2._1().toString());		//for a particular node
							Iterable<Integer> contributors=contributingNodes.lookup(node).get(0); 	//all the contributors
							
							for(Integer contributor:contributors)	//for each contributor
							{
								Integer itsPageRank=pageRank3.lookup(contributor).get(0);
								Integer itsNumberOfAdjacentNodes=numberAdjacentNodes.lookup(contributor).get(0);
								double itsContribution= (double) (itsPageRank/itsNumberOfAdjacentNodes);
								
								nodeTotalContributions=nodeTotalContributions+itsContribution;
							}
							
							//DANGLING NODES CONTRIBUTIONS
							for(int i=0;i<danglingNodesList.size();i++)
							{
								int danglingNode=danglingNodesList.get(i);
								double contributions=(double) (pageRank3.lookup(danglingNode).get(0)/N);
								
								danglingTotalContributions=danglingTotalContributions+contributions;
							}
									
							newPageRank=0.15+0.85*(nodeTotalContributions+danglingTotalContributions);
							return new Tuple2(node,newPageRank);
						}
					});
		}
		
		JavaRDD titlePageRank=mappings.join(pageRank).values();
		
		
		List<Tuple2> sortingList=titlePageRank.collect();
		
		TreeSet<SortClass> sortSet = new TreeSet<SortClass>();
		
		for(Tuple2 temp:sortingList)//iterate over the values to store it in the tree set
		{			
			sortSet.add(new SortClass(temp));
		}
		
		File outfile = new File ("/root/output/pagerank");
		try
		{
		FileWriter fw = new FileWriter(outfile);
		BufferedWriter bw = new BufferedWriter(fw);
		int k=1;			//counter to iterate
		for (SortClass element : sortSet) {
			if(k<101 && k<=sortSet.size())
			{
				//sortedList.add(element.toString());
				bw.write(k);
				bw.write(element.toString());
				k++;
			}
		}
		bw.close();
		fw.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		
		
	}
	
	private static class SortClass implements Comparable<SortClass> {

		String title = null;
		double pageRank = 0;	
		String returnValue=null;
		SortClass(Tuple2 tuple2) 
		{	
			pageRank= (double) tuple2._2();
			
			title=tuple2._1().toString();
			String returnValue=title+"\t"+pageRank;
			
				
		}
		public String toString() //to return the result
		{
			return returnValue;
		}

		@Override
		public int compareTo(SortClass o) //compare function to sort in descending order of count and ascending order of phrase
		{
			double diff = this.pageRank - o.pageRank;

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