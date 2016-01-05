import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;



public class Reducer {

    public static void main(String[] args) {
        // TODO Auto-generated method stub      
        System.out.println("Start");
    try 
    {
        //Load Mysql Driver class for database connection
        Class.forName("com.mysql.jdbc.Driver");
        // Get Database Connection Object 
        Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:3306","root","root");
        //Connection conn = new DatabaseConnection().getAPooledConnection();
        
        SimpleDateFormat from = new SimpleDateFormat("dd/MM/yyyy");
        //SimpleDateFormat to = new SimpleDateFormat("yyyy-MM-dd");
                
        //Read
        File inFile = new File("C:\\Users\\abhis_000\\Desktop\\testrun\\MapperOutput.txt");      
        FileReader fr= new FileReader(inFile);
        BufferedReader br = new BufferedReader(fr);
        String tempLine=null;
        String currentLine=null;
        while((currentLine=br.readLine()) != null)
        {
            String parts[] =currentLine.split(",");
            
            String valueParts[]=parts[1].split("\\|");
            String keyParts[] = parts[0].split("\\|");
            
            java.util.Date date1 = from.parse(valueParts[2]);       // 01/02/2014
            java.sql.Date date2 = new java.sql.Date(date1.getTime());
            
            if(parts[0].equals(tempLine))
            {
                System.out.println("User Exists");
                String nw_plmn_id=null;
                String crspnd1_id=null; 
                
                //INSERT INTO CORRESPONDENT
                String sql2="select crspnd1_id from test_call_log.crspnd where crspnd1_type like ? and crspnd1_isdn like ?";
                PreparedStatement val2= conn.prepareStatement(sql2);
                val2.setString(1, valueParts[7]);
                val2.setString(2, valueParts[8]);
                
                ResultSet rs1=val2.executeQuery();
                
                if(!rs1.next())
                {
                    String sql3="insert into test_call_log.crspnd (crspnd1_type,crspnd1_isdn) VALUES(?,?)";
                    PreparedStatement val3= conn.prepareStatement(sql3);
                    val3.setString(1, valueParts[7]);
                    val3.setString(2, valueParts[8]);
                    
                    val3.execute();
                    
                    //
                    rs1=val2.executeQuery();
                    if(rs1.next())
                    {
                        crspnd1_id=rs1.getString("crspnd1_id");
                    }
                }
                else
                {
                    crspnd1_id=rs1.getString("crspnd1_id");
                }
                
                //INSERT INTO N/W
                String sql4="select nw_plmn_id from test_call_log.network_log where nw_plmn like ?";
                PreparedStatement val4= conn.prepareStatement(sql4);
                val4.setString(1, valueParts[9]);
                
                ResultSet rs2=val4.executeQuery();
                
                if(!rs2.next())
                {
                    String sql5="insert into test_call_log.network_log (nw_plmn) VALUES(?)";
                    PreparedStatement val5= conn.prepareStatement(sql5);
                    val5.setString(1, valueParts[9]);
                    
                    val5.execute();
                    
                    //
                    rs2=val4.executeQuery();
                    if(rs2.next())
                    {
                        nw_plmn_id=rs2.getString("nw_plmn_id");
                    }
                }
                else
                {
                    nw_plmn_id=rs2.getString("nw_plmn_id");
                }
                
                //INSERT INTO CALL LOG
                String sql6="insert into test_call_log.log VALUES(?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement val6= conn.prepareStatement(sql6);
                val6.setString(1, keyParts[0]);
                val6.setString(2, valueParts[0]);
                val6.setString(3, valueParts[1]);
                val6.setDate(4, date2);
                val6.setString(5, valueParts[3]);
                val6.setString(6, valueParts[4]);
                val6.setString(7, valueParts[5]);
                val6.setString(8, valueParts[6]);
                val6.setString(9, crspnd1_id);
                val6.setString(10,nw_plmn_id);
               
                val6.execute();

                tempLine=parts[0];
            }
            else
            {
                System.out.println("New User");
                String nw_plmn_id=null;
                String crspnd1_id=null;                
                
                //INSERT INTO CUSTOMER
                String sql1="insert into test_call_log.customer VALUES(?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement val1= conn.prepareStatement(sql1);
                val1.setString(1, keyParts[0]);
                val1.setString(2, keyParts[1]);
                val1.setString(3, keyParts[2]);
                val1.setString(4, keyParts[3]);
                val1.setString(5, keyParts[4]);
                val1.setString(6, keyParts[5]);
                val1.setString(7, keyParts[6]);
                val1.setString(8, keyParts[7]);
                val1.setString(9, keyParts[8]);
                val1.setString(10,keyParts[9]);
               
                val1.execute();
                
                
                
                //INSERT INTO CORRESPONDENT
                String sql2="select crspnd1_id from test_call_log.crspnd where crspnd1_type like ? and crspnd1_isdn like ?";
                PreparedStatement val2= conn.prepareStatement(sql2);
                val2.setString(1, valueParts[7]);
                val2.setString(2, valueParts[8]);
                
                ResultSet rs1=val2.executeQuery();
                
                if(!rs1.next())
                {
                    String sql3="insert into test_call_log.crspnd (crspnd1_type,crspnd1_isdn) VALUES(?,?)";
                    PreparedStatement val3= conn.prepareStatement(sql3);
                    val3.setString(1, valueParts[7]);
                    val3.setString(2, valueParts[8]);
                    
                    val3.execute();
                    
                    //
                    rs1=val2.executeQuery();
                    if(rs1.next())
                    {
                        crspnd1_id=rs1.getString("crspnd1_id");
                    }
                }
                else
                {
                    crspnd1_id=rs1.getString("crspnd1_id");
                }
                
                //INSERT INTO N/W
                String sql4="select nw_plmn_id from test_call_log.network_log where nw_plmn like ?";
                PreparedStatement val4= conn.prepareStatement(sql4);
                val4.setString(1, valueParts[9]);
                
                ResultSet rs2=val4.executeQuery();
                
                if(!rs2.next())
                {
                    String sql5="insert into test_call_log.network_log (nw_plmn) VALUES(?)";
                    PreparedStatement val5= conn.prepareStatement(sql5);
                    val5.setString(1, valueParts[9]);
                    
                    val5.execute();
                    
                    //
                    rs2=val4.executeQuery();
                    if(rs2.next())
                    {
                        nw_plmn_id=rs2.getString("nw_plmn_id");
                    }
                }
                else
                {
                    nw_plmn_id=rs2.getString("nw_plmn_id");
                }
                
                               
                //INSERT INTO CALL LOG
                String sql6="insert into test_call_log.log VALUES(?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement val6= conn.prepareStatement(sql6);
                val6.setString(1, keyParts[0]);
                val6.setString(2, valueParts[0]);
                val6.setString(3, valueParts[1]);
                val6.setDate(4, date2);
                val6.setString(5, valueParts[3]);
                val6.setString(6, valueParts[4]);
                val6.setString(7, valueParts[5]);
                val6.setString(8, valueParts[6]);
                val6.setString(9, crspnd1_id);
                val6.setString(10,nw_plmn_id);
               
                val6.execute();

                tempLine=parts[0];
            }
        }
        
    } 
    catch (FileNotFoundException e) 
    {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }            
    
   }

}
