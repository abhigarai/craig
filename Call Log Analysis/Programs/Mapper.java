import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class Mapper {

    public static void main(String[] args) {
        // TODO Auto-generated method stub        
        try 
        {
            System.out.println("Start");
            //Read
            File inFile = new File("C:\\Users\\abhis_000\\Desktop\\testrun\\InputDataSet.txt");          
            FileReader fr= new FileReader(inFile);            
            BufferedReader br = new BufferedReader(fr); 
            
            //Write
            File outFile = new File ("C:\\Users\\abhis_000\\Desktop\\testrun\\MapperOutput.txt");
            FileWriter fw = new FileWriter(outFile);
            BufferedWriter bw = new BufferedWriter(fw);
            
            
            String currentLine=null;
            while((currentLine=br.readLine()) != null)
            {
                //System.out.println(currentLine);
                String parts[] = currentLine.split("\\|");
                /*for(int i=0;i<parts.length;i++)
                {
                    System.out.print(parts[i]+"\\");
                }*/
                StringBuilder key=new StringBuilder();
                StringBuilder value=new StringBuilder();
                for(int i=0;i<parts.length;i++)
                {
                    if(i==2 || i==6 || i==21 || i==22 || i>=24 )
                    {
                        continue;
                    }
                    else 
                        if(i<11)
                        {
                            key.append(parts[i]+"|");
                        }
                        else 
                            if(i==11)
                            {
                                key.append(parts[i]);
                            }
                            else 
                                if(i<23)
                                {
                                    value.append(parts[i]+"|");
                                }
                                else
                                {
                                    value.append(parts[i]);
                                }
                }
                bw.write(key+","+value);
                bw.newLine();
            }
            bw.close();
            br.close();
            System.out.println("Done");
        } 
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
