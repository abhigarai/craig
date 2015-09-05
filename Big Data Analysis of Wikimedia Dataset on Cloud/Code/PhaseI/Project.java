import java.io.*;

public class Project {

	public static void main(String[] args) {
		BufferedReader br = null;
		 
		try {
 
			String currentLine,subString1,subString2,subString3,tempString;
			//File f = new File("C:\\Users\\abhis_000\\Desktop\\CMU_Pittsburgh\\CC\\s3cmd\\pagecounts-20141101-000000");
			File f = new File("pagecounts-20141101-000000");
			br = new BufferedReader(new FileReader(f));
			
			//Write
			//File outfile = new File ("C:\\Users\\abhis_000\\Desktop\\CMU_Pittsburgh\\CC\\s3cmd\\Output2.txt");
			//FileWriter fw = new FileWriter(outfile);
			//BufferedWriter bw = new BufferedWriter(fw);
						
			while ( (currentLine = br.readLine()) != null) {
				if(currentLine.startsWith("en "))
				 {					
					subString1 = currentLine.substring(currentLine.indexOf(" ")+1);
					if((subString1.startsWith("Media:") || subString1.startsWith("Special:") || 
							subString1.startsWith("Talk:") || subString1.startsWith("User:") ||
							subString1.startsWith("User_talk:") || subString1.startsWith("Project:") || 
							subString1.startsWith("Project_talk:") || subString1.startsWith("File:") || 
							subString1.startsWith("File_talk:") || subString1.startsWith("MediaWiki:") || 
							subString1.startsWith("MediaWiki_talk:") || subString1.startsWith("Template:") || 
							subString1.startsWith("Template_talk:") || subString1.startsWith("Help:") || 
							subString1.startsWith("Help_talk:") || subString1.startsWith("Category:") || 
							subString1.startsWith("Category_talk:") || subString1.startsWith("Portal:") || 
							subString1.startsWith("Wikipedia:") || subString1.startsWith("Wikipedia_talk:")) != true)
					{
						if((subString1.startsWith("a") || subString1.startsWith("b") || 
								subString1.startsWith("c") || subString1.startsWith("d") ||
								subString1.startsWith("e") || subString1.startsWith("f") || 
								subString1.startsWith("g") || subString1.startsWith("h") || 
								subString1.startsWith("i") || subString1.startsWith("j") || 
								subString1.startsWith("k") || subString1.startsWith("l") || 
								subString1.startsWith("m") || subString1.startsWith("n") || 
								subString1.startsWith("o") || subString1.startsWith("p") || 
								subString1.startsWith("q") || subString1.startsWith("r") || 
								subString1.startsWith("s") || subString1.startsWith("t") ||
								subString1.startsWith("u") || subString1.startsWith("v") || 
								subString1.startsWith("w") || subString1.startsWith("x") || 
								subString1.startsWith("y") || subString1.startsWith("z")) != true)
						{
							subString2 = subString1.substring(0,subString1.indexOf(" "));
							if ((subString2.endsWith(".jpg") || 
									subString2.endsWith(".gif") || 
									subString2.endsWith(".png") || 
									subString2.endsWith(".JPG") || 
									subString2.endsWith(".GIF") || 
									subString2.endsWith(".PNG") || 
									subString2.endsWith(".txt") || 
									subString2.endsWith(".ico")) != true)
							{								
								if((subString2.equals("404_error/") || 
										subString2.equals("Main_Page") || 
										subString2.equals("Hypertext_Transfer_Protocol") || 
										subString2.equals("Search")) != true)
								{
									tempString	=subString1.substring(subString1.indexOf(" ")+1);
									subString3=tempString.substring(0,tempString.indexOf(" "));
									
									System.out.println(subString2+"\t"+subString3);
									
									//bw.write(currentLine);
									//bw.newLine();
									
								}							
							}
							
						}
					}
					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

}
