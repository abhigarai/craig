import java.io.*;

public class Reducer {

	public static void main(String[] args)throws Exception {
		 
		 BufferedReader br = null;
		 int dailyCount[]=new int[30];//Array to store the daily counts
		 String currentLine=null,currentWord=null,word=null;
 		 int hourlyCount=0,currentDate=0,initDate=20141101,monthCount=0;
	      
 		 br = new BufferedReader(new InputStreamReader(System.in));//stream to read from the console
			
					
			while ( (currentLine = br.readLine()) != null) {//read each line from the console
				
				String[] parts = currentLine.split("\t");//split it based on spaces or tab
				currentWord = parts[0];
				hourlyCount = Integer.parseInt(parts[1]);
				currentDate = Integer.parseInt(parts[2]);//store the word, hourly count i.e. number of accesses and the date
				// these are the output from the mapper i.e the output format of mapper
				//Key:Title  Vale:Number of Access and Date
				
				if(currentWord.equals(word))// So the logic here is to read the word, check for the date in which it was accessed and then store it 
				{                             // in the index corrresponding to that date
						for(int i=0;i<30;i++)// So, iterate over all the dates
						{
							if(currentDate==(initDate+i))//search for the date of access 
							{
								dailyCount[i]=dailyCount[i]+hourlyCount;//and store the accesscount in an array index corresponding to the that date
								break;//it is used because once you find the date there is no need to iterate further
							}
						}
				}
				else//else if its the first word or the word has changed
				{
					if(word!=null)//word change
					{
						for(int i=0;i<30;i++)//since word has changed, calculate the total count for the previous word
						{
							monthCount=monthCount+dailyCount[i];//by iterating and aading the counts for all the days from daily count
						}
						if(monthCount>100000)//check toatl count > 100000
						{   								// if true print it in the required format
							System.out.print(monthCount+"\t");
							System.out.print(word+"\t");
						for(int i=0;i<30;i++)
						{
							System.out.print((initDate+i)+":"+dailyCount[i]+"\t");
						}
						System.out.print("\n");
						}
						//Since the word has changed now reset the values of monthly and daily count
						monthCount=0;
						for(int i=0;i<30;i++)
						{
							dailyCount[i]=0;
						}
						//And again search for the date on which the current aricle was accessed and store its access count in corresponding daily count
						for(int i=0;i<30;i++)
						{
							if(currentDate==(initDate+i))
							{
								dailyCount[i]=hourlyCount;
								break;
							}
						}
						word=currentWord;//store it in temp variable for comapring the next time
					}
					else//this is the first access i.e. for the first word
					{
						for(int i=0;i<30;i++)
						{
							if(currentDate==(initDate+i))
							{
								dailyCount[i]=dailyCount[i]+hourlyCount;
								break;
							}
						}
						word=currentWord;
						}
				}
			
	}
			//this is for the details of the last word to be displayed, logic being the same i.e. calculate monthly count from daily count and display in required format
			for(int i=0;i<30;i++)
			{
				monthCount=monthCount+dailyCount[i];
			}
			System.out.print(monthCount+"\t");
			System.out.print(word+"\t");
			for(int i=0;i<30;i++)
			{
				System.out.print((initDate+i)+":"+dailyCount[i]+"\t");
			}
			System.out.print("\n");
			
}
}

