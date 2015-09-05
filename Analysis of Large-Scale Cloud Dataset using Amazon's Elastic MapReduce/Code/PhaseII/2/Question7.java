import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.File;
import java.util.ArrayList;
public class Question7 {

	public static void main(String[] args)throws Exception {
		// TODO Auto-generated method stub
		String currentLine=null;//for reading each line in the array
		ArrayList<String> cityList = new ArrayList<String>();//store the cities from the file into this list for processing
		String citySort[][];//final sorted array
		String cityName=null;
		int k=0;
		String tempCityName=null,tempMaxCount=null;
		
		File q7 = new File("q7");//read file 
		BufferedReader br2 = new BufferedReader(new FileReader(q7));
		
		BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));//stream for reading from console
		
		
		while((cityName=br2.readLine()) != null)//read each line
		{
			cityList.add(cityName);//store it in the array
		}
		citySort = new String[cityList.size()][2];//create the sort array of the same size as the number of movies in the file
		while ( (currentLine = br1.readLine()) != null) {//read each line
			
			String[] parts = currentLine.split("\\s+");//split it based on spaces or tabs
			for (String city : cityList)//iterate over the list of cities
			    if (city.equals(parts[1]))//if there is a match
			    {
			    	citySort[k][0]=parts[1];//store the name of the city
			    	citySort[k][1]=parts[0];//store the value of the month count
			    	k++;//and increment the counter
			    }			
		}
		
		for(int i=0;i<cityList.size();i++)
		{
			for(int j=1;j<(cityList.size()-i);j++)//finally sort it in descending order for using bubble sort as previous
			{
				if(Integer.parseInt(citySort[j-1][1])<Integer.parseInt(citySort[j][1]))
				{
					tempCityName=citySort[j-1][0];
					tempMaxCount=citySort[j-1][1];
					citySort[j-1][0]=citySort[j][0];
					citySort[j-1][1]=citySort[j][1];
					citySort[j][0]=tempCityName;
					citySort[j][1]=tempMaxCount;
				}
			}
		}
		for(int i=0;i<cityList.size();i++)
		{
			System.out.println(citySort[i][0]+",");
		}
		 br2.close();
		} 
	}


