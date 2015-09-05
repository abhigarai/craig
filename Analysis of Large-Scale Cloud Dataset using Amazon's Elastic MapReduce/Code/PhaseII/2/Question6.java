import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.File;
import java.util.ArrayList;
public class Question6 {

	public static void main(String[] args)throws Exception {
		// TODO Auto-generated method stub
		String currentLine=null;
		ArrayList<String> movieList = new ArrayList<String>();//read each line from the file and store it in the list, basically reading the file for  processing
		String movieSort[][];//this will contain the final sorted values
		String movieName=null;
		int max=0,k=0;//max is temporary variable used to store the max count for a article
		String tempMovieName=null,tempMaxCount=null;//temporary values required for sorting in descending order
		
		File q6 = new File("q6");
		BufferedReader br2 = new BufferedReader(new FileReader(q6)); // read the file in the program
		
		BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));// stream to read from the command line
		
		
		while((movieName=br2.readLine()) != null)//read each line from the console
		{
			movieList.add(movieName);//add it to the list
		}
		movieSort = new String[movieList.size()][2];//create an array of the same length as the number of movies in the file 
		while ( (currentLine = br1.readLine()) != null) {//read each line from the file
			
			String[] parts = currentLine.split("\\s+");//split the line based on the spaces or tabs
			for (String mov : movieList)// and then compare the name with each of the movies in the file
			    if (mov.equals(parts[1]))//compare the titles and and if they are same then process
			    {
			    	movieName=parts[1];//store the title in a variable
			    	for(int i=2;i<=31;i++)//iterate over all the days of the month
					{
			    		if((Integer.parseInt(parts[i].substring(9)))>max)//and compare the count by extracting the count from the output format
			    		{
			    			max=Integer.parseInt(parts[i].substring(9));//if it is greater than max change the max to the new value
			    		}				
					}
			    	movieSort[k][0]=movieName;
			    	movieSort[k][1]=Integer.toString(max);//after iterating over all the values store both the name and max count in the array
			    	k++;//increment the counter of the array
			    }			
		}
		
		for(int i=0;i<movieList.size();i++)
		{
			for(int j=1;j<(movieList.size()-i);j++)//this loop is used to do a bubble sort in descending order for the array 
			{
				if(Integer.parseInt(movieSort[j-1][1])<Integer.parseInt(movieSort[j][1]))//for each element check if next value is greater than the previous and swap
				{
					tempMovieName=movieSort[j-1][0];
					tempMaxCount=movieSort[j-1][1];
					movieSort[j-1][0]=movieSort[j][0];
					movieSort[j-1][1]=movieSort[j][1];
					movieSort[j][0]=tempMovieName;
					movieSort[j][1]=tempMaxCount;// the swapping logic
				}
			}
		}
		for(int i=0;i<movieList.size();i++)//finally print the sorted array
		{
			System.out.println(movieSort[i][0]+",");
		}
		 br2.close();
		} 
	}


