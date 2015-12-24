import io.undertow.server.*;
import io.undertow.*;
import io.undertow.util.Headers;
import java.math.BigInteger;
import java.sql.*;
import java.sql.DriverManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

//res
//stmnt
//conn
public class Query2 
{

	public static BigInteger xKey = new BigInteger("8271997208960872478735181815578166723519929177896558845922250595511921395049126920528021164569045773");
	public static ConcurrentHashMap<String, Integer> chm = new ConcurrentHashMap<String, Integer>();

	public static void main(String[] args) 
	{
		 // JDBC driver name and database URL
        final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
        final String DB_URL = "jdbc:mysql://52.1.159.161:3306/tweet_table";

        // Database credentials
        final String USER = "tweet";
        final String PASS = "password";
        

       

		
		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		for (int i = 1; i < 500000; i++) 
		{
			BigInteger k = (BigInteger.valueOf(i));
			int z;
			z = (i % 25) + 1;
			chm.put((xKey.multiply(k).toString()), z);
		}
		try 
		{
			 // Register JDBC driver
	        Class.forName("com.mysql.jdbc.Driver").newInstance();
	        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	        
			 Undertow server = Undertow.builder()
		                .addHttpListener(8080, "0.0.0.0")
		                .setHandler(new HttpHandler()

			{
				@Override
				public void handleRequest(final HttpServerExchange exchange) throws Exception
				{
					try 
					{
						StringBuilder responseMessage = new StringBuilder("o1h53x,0268-5591-9323,2746-2065-5864,3773-7548-5421\n");
						responseMessage.append(dateFormat.format(new Date())+ "\n");
						exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,"text/plain");
						String check = exchange.getRequestPath();
						if (check.equals("/q1")) 
						{
							String msg=UnSpiralMsg(exchange.getQueryParameters().get("message").getFirst());
							String key = exchange.getQueryParameters().get("key").getFirst();
							if (chm.containsKey(key)) 
							{
								int zKey = chm.get(key);
								for (int i = 0, temp = 'a'; i < msg.length(); i++) 
								{
									temp = (char) (msg.charAt(i) - zKey);
									responseMessage.append(temp >= 65 ? ((char) (temp)): ((char) (temp + 26)));
								}
								responseMessage.append("\n");
							} 
							else
							{
								int zKey = decipher(key);
								for (int i = 0, temp = 'a'; i < msg.length(); i++) 
								{
									temp = (char) (msg.charAt(i) - zKey);
									responseMessage.append(temp >= 65 ? ((char) (temp)): ((char) (temp + 26)));
								}
								responseMessage.append("\n");
							}
							exchange.getResponseSender().send(responseMessage.toString());
						}
						else
						{
                                String u = exchange.getQueryParameters().get("userid").getFirst();

                                String timestamp = exchange
                                                .getQueryParameters()
                                                .get("tweet_time").getFirst();

                                String[] parts = timestamp.split("\\s+");

                                String getRequest = "U" + u + "T"
                                                + parts[0] + "+" + parts[1];

                                try
                                {
                                        // Execute a query
                                        Statement stmt = conn.createStatement();
                                        String sql;
                                        sql = "SELECT * FROM tweet_tab WHERE id = '"
                                                        + getRequest + "'";

                                        // Get Result
                                        ResultSet rs = null;
                                        try 
                                        {
                                                rs = stmt.executeQuery(sql);
                                        } 
                                        catch (SQLException e)
                                        {
                                                e.printStackTrace();
                                        }
                                        // Extract data from result set
                                        // Retrieve by column name
                                        while (rs.next()) 
                                        {
                                                String response = rs
                                                                .getString("response");

                                                responseMessage.append(response);
                                                break;
                                        }
                                }
                                catch (SQLException se) 
                                {
                                        // Handle errors for JDBC
                                        se.printStackTrace();
                                } 
                                catch (Exception e)
                                {
                                        // Handle errors for Class.forName
                                        e.printStackTrace();
                                }
                        
							//
						}
					} 
					catch (Exception e) 
					{
						System.out.println(e);
					}
				}
			}).build();
			 server.start();

		} 
		catch (Exception e) 
		{
			System.out.println(e);
		}
	}



		public static int decipher(String xykey) 
		{
			BigInteger xyKey=new BigInteger(xykey);
			int zKey=0;
			try
			{
				int yKey = xyKey.divide(xKey).intValue();
				zKey = (yKey % 25) + 1;
				if (chm.size() < 1000000)
					chm.put(xyKey.toString(), zKey);
			} 
			catch (Exception e) 
			{
				System.out.println(e);
			}
			return zKey;
		}



	public static String UnSpiralMsg(String message) 
	{
		StringBuilder result = new StringBuilder();
		try 
		{
			int msg_length = message.length();
			int sq = (int) Math.sqrt(msg_length);

			// indices
			int a = 0, b = 0;
			int m = sq, n = sq;

			char[][] matrix = new char[sq][sq];

			int i = 0, j = 0, k = 0;

			// writing to matrix
			for (i = 0; i < sq; i++) {
				for (j = 0; j < sq; j++) {
					matrix[i][j] = message.charAt(k++);
				}
			}
			// reading spirally
			while (a < m && b < n)
			{
				for (i = b; i <= n - 1; i++)
					result.append(matrix[a][i]);
				a++;
				for (i = a; i <= m - 1; i++)
					result.append(matrix[i][n - 1]);
				n--;
				if (a < m)
				{
					for (i = n - 1; i >= b; i--)
						result.append(matrix[m - 1][i]);
					m--;
				}
				if (b < n) 
				{
					for (i = m - 1; i >= a; i--)
						result.append(matrix[i][b]);
					b++;
				}
			}
		} 
		catch (Exception e)
		{
			System.out.println(e);
		}
		return result.toString();
	}
}
