import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Question10 {

	public static void main(String[] args)throws Exception {
		String currentLine=null;
		int num=0,currentRun=1,currentLongestRun=1,globalLongestRun=1,globalCount=0;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		while ( (currentLine = br.readLine()) != null) {// read each line
			//lineCount++;
				String[] parts = currentLine.split("\\s+");//split it based on the spaces or tabs and store it in array parts
				num=Integer.parseInt(parts[2].substring(9));//store the count of the first day in  a temp variable
			    for(int i=3;i<=31;i++)//and iterate over all the remaining days for comparing and the counts 
					{			    		
			    		if((Integer.parseInt(parts[i].substring(9)))>num)//extract the count and if it is greater than the previous value stored in the temp variable
			    		{                                               // implies that it is an incrementing run
			    			num=Integer.parseInt(parts[i].substring(9));//change the value of the variable
			    			currentRun++;// and increment the run count
			    		}
			    		else//now when the next count is not greater 
			    		{
			    			if(currentRun>currentLongestRun)//compare the run with the current longest run
			    			{                                // if it is greater then change the current longest run to current run
			    				currentLongestRun=currentRun;
			    				num=Integer.parseInt(parts[i].substring(9));// start the run again from the current value
				    			currentRun=1;//also reset the run 
			    			}
			    			else// if the current is smaller than previous then
			    			{
			    				num=Integer.parseInt(parts[i].substring(9));//just start the run again without modifying the current longest run
				    			currentRun=1;//and start the run again
			    			}
			    		}	
					}//till now it gives the longest run in a particular line
			    if(currentLongestRun>globalLongestRun)///compare it with the global longest run
			    {                                     // if it is greater then
			    	globalLongestRun=currentLongestRun;//modify the global longest run to current longest run
			    	globalCount=1;
			    	currentRun=1;
			    	currentLongestRun=1;//and reset all the counters
			    }
			    else
			    {
			    	if(currentLongestRun==globalLongestRun)//else if the run is same as the global longest run
			    	{
			    		globalCount++;//increment the count variable as it is of a different word
			    		currentRun=1;
			    		currentLongestRun=1;//and reset all the counters
			    	}
			    	else//this is the case when the current longest run is smaller than the global longest run
			    	{
			    		currentRun=1;
			    		currentLongestRun=1;//reset all the counters
			    	}
			    }
		
		}
		System.out.println(globalCount);//print the global count
		} 

}