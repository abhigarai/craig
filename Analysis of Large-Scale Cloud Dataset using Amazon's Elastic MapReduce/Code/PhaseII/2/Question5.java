import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Question5 {

	public static void main(String[] args)throws Exception{
		// TODO Auto-generated method stub
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));// Stream to Read from Console
		String currentLine=null, google=args[0], amazon=args[1];// the value of google and amazon taken from parameters
		int days=0;
		int gCount[]=new int[30];//daily count for article google 
		int aCount[]=new int[30];//daily count for article amazon.com
		
		while ( (currentLine = br.readLine()) != null) { //read each line
			
			String[] parts = currentLine.split("\\s+"); // split all the parts based on space or tab delimiter
			if(parts[1].equals(google)) // check article name as parts of 1
			{
				for(int i=2;i<=31;i++)//iterate over all the days of the month
				{
					gCount[i-2]=Integer.parseInt(parts[i].substring(9));//store only the count of each day i.e. after 2014110x: for each day
				}
			}
			else // else check if it is amazon.com // do the same things else do nothing and move to the next line
			{
				if(parts[1].equals(amazon))
				{
					for(int i=2;i<=31;i++)
					{
						aCount[i-2]=Integer.parseInt(parts[i].substring(9));
					}	
				}
			}			
		}
		
		for(int i=0;i<30;i++)
		{
			if(gCount[i]>aCount[i])// now compare corresponding elements of both the arrays and if google has greater count the increment the count variable
			{
				days++;
			}
		}
		
		System.out.println(days); // finally print the output
	}

}
