import java.io.*;

public class Mapper {

	public static void main(String[] args)throws Exception {
			BufferedReader br = null;
		 
			String wordCount[];//string array to store the words of a line
			String date,currentLine,tempString1,projTitle,noOfAccesses,tempString2,finalString;
			
			String path=System.getenv("mapreduce_map_input_file");//for fetching the name of the file using the variable
						
			br = new BufferedReader(new InputStreamReader(System.in));//stream to read from the console
					
			while ( (currentLine = br.readLine()) != null) {//read each line from the console
				
				//ForLineWordCount
				wordCount = currentLine.split("\\s+");//split it based on space or tabs and store it in the array
				/*
				 * Reference
				cuurent line= language /t title /t number of accesses /t size
				tempstring1= title /t number of accesses /t size
				tempstring2=number of accesses /t size		
				
				*/
				//Apply all the Filtering rules
				if(wordCount.length==4)//if wordcount is equal to four i.e exclude missing articles
				{
				if(currentLine.startsWith("en "))//starts with "en"
				 {	
					tempString1 = currentLine.substring(currentLine.indexOf(" ")+1);//extract the substring starting from the title till the end of current line to filter it based on the starting word 
					if((tempString1.startsWith("Media:") || tempString1.startsWith("Special:") || //and store it in a temp string
							tempString1.startsWith("Talk:") || tempString1.startsWith("User:") ||
							tempString1.startsWith("User_talk:") || tempString1.startsWith("Project:") || 
							tempString1.startsWith("Project_talk:") || tempString1.startsWith("File:") || 
							tempString1.startsWith("File_talk:") || tempString1.startsWith("MediaWiki:") || 
							tempString1.startsWith("MediaWiki_talk:") || tempString1.startsWith("Template:") || 
							tempString1.startsWith("Template_talk:") || tempString1.startsWith("Help:") || 
							tempString1.startsWith("Help_talk:") || tempString1.startsWith("Category:") || 
							tempString1.startsWith("Category_talk:") || tempString1.startsWith("Portal:") || 
							tempString1.startsWith("Wikipedia:") || tempString1.startsWith("Wikipedia_talk:")) != true)// filter based on the starting word
					{
						if((tempString1.startsWith("a") || tempString1.startsWith("b") || 
								tempString1.startsWith("c") || tempString1.startsWith("d") ||
								tempString1.startsWith("e") || tempString1.startsWith("f") || 
								tempString1.startsWith("g") || tempString1.startsWith("h") || 
								tempString1.startsWith("i") || tempString1.startsWith("j") || 
								tempString1.startsWith("k") || tempString1.startsWith("l") || 
								tempString1.startsWith("m") || tempString1.startsWith("n") || 
								tempString1.startsWith("o") || tempString1.startsWith("p") || 
								tempString1.startsWith("q") || tempString1.startsWith("r") || 
								tempString1.startsWith("s") || tempString1.startsWith("t") ||
								tempString1.startsWith("u") || tempString1.startsWith("v") || 
								tempString1.startsWith("w") || tempString1.startsWith("x") || 
								tempString1.startsWith("y") || tempString1.startsWith("z")) != true)//filter out if it starts with lower case
						{
							projTitle = tempString1.substring(0,tempString1.indexOf(" "));//extract only the title i.e. from start of the previous string extracted till the index of the first space
							if ((projTitle.endsWith(".jpg") ||                            // this is the title of the article
									projTitle.endsWith(".gif") || 
									projTitle.endsWith(".png") || 
									projTitle.endsWith(".JPG") || 
									projTitle.endsWith(".GIF") || 
									projTitle.endsWith(".PNG") || 
									projTitle.endsWith(".txt") || 
									projTitle.endsWith(".ico")) != true)// check if it ends with the specific values
							{								
								if((projTitle.equals("404_error/") || 
										projTitle.equals("Main_Page") || 
										projTitle.equals("Hypertext_Transfer_Protocol") || 
										projTitle.equals("Search")) != true)//filter the articles with these names i.e. check for the article name
								{
									
										date=path.substring(path.indexOf("-2014")+1,path.indexOf("-2014")+9);//now extract just the date from the filename string returned by the env variable
										//by searching for the characters -2014 which is unique in the whole path
										tempString2 = tempString1.substring(tempString1.indexOf(" ")+1);//from the temp string remove the title 
										noOfAccesses = tempString2.substring(0, tempString2.indexOf(" "));//and extract the number of access 
										finalString=projTitle+"\t"+noOfAccesses+"\t"+date;
										
										System.out.println(finalString);//final string is diaplayed in the above format		
										//final String output is : title numberofAccesses date
									}
									
								}							
							}
							
						}
					}
					
				
			}
			}
		
	}

}
