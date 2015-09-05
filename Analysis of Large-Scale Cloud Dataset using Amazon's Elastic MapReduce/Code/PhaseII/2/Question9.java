import java.io.BufferedReader;
import java.io.InputStreamReader;
public class Question9 {

	public static void main(String[] args)throws Exception {
		// TODO Auto-generated method stub
		String currentLine=null,mostPopularArticle=null;
		int max=0;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));//stream to read from console
		
		
		while ( (currentLine = br.readLine()) != null) {//read each line
			
			String[] parts = currentLine.split("\\s+");//split it into parts based on spaces or tabs and store it in a string array parts
			    if (Integer.parseInt(parts[2].substring(9))==0 && Integer.parseInt(parts[0])>max)//now check if the count at parts[2] is zero and greater than previous max
			    {                                      //i.e. parts 0 will contain total count, parts 1 will contain the name and further parts the date and daily count as per the format
			    		max=Integer.parseInt(parts[0]);// if it is greater store it in max
			    		mostPopularArticle=parts[1];					//store the name of the article for displaying
			    }	
		}
		System.out.println(mostPopularArticle);//finally after iterating over all the values the displayed variable will contain the name respective to the maximum count
		} 
	}


