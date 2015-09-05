import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Question8 {

	public static void main(String[] args)throws Exception {
		// TODO Auto-generated method stub
		String currentLine=null;
		int date=0;//for storing the date
		int max=0;// for storing the max count
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));//stream to read from console
		
		while ( (currentLine = br.readLine()) != null) {//read each line from console
			
			String[] parts = currentLine.split("\\s+");//split it based on spaces and tabs
			    if (parts[1].equals("Interstellar_(film)"))//and compare the second column in the i.e. parts[1] to check if its is the title mentioned 
			    {
			    	for(int i=2;i<=31;i++)//iterate over all the all the days of the month when the title matches
					{
			    		
			    		if((Integer.parseInt(parts[i].substring(9)))>max)//and extract just the count from the output format i.e. YYYYMMDD:count using substring
			    		{
			    			max=Integer.parseInt(parts[i].substring(9));//and compare it with previous value if its greater then change the max to current
			    			date=Integer.parseInt(parts[i].substring(0,8));//store the date for which it is max
			    		}				
					}
			    }	
		}
		System.out.println(date);//finally after iterating over all the days print the max as it will contain the max count
		} 
	}
