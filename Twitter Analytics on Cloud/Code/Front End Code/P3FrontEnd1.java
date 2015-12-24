import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import io.undertow.server.*;
import io.undertow.*;
import io.undertow.util.Headers;
import io.undertow.server.protocol.http.HttpOpenListener;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.*;

import com.mysql.jdbc.Driver;

public class FrontEnd1 {
	public static BigInteger xKey = new BigInteger(
			"8271997208960872478735181815578166723519929177896558845922250595511921395049126920528021164569045773");
	public static ConcurrentHashMap<String, Integer> query1Map = new ConcurrentHashMap<String, Integer>();
	public static TreeMap<Long, Integer> query6Map = new TreeMap<Long, Integer>();
	private static Connection connection = null;

	public static void main(String[] args) {

		final DateFormat dateFormat = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss");
		for (int i = 1; i < 100000; i++) {
			BigInteger k = (BigInteger.valueOf(i));
			int z;
			z = (i % 25) + 1;
			query1Map.put((xKey.multiply(k).toString()), z);
		}
		try 
		{
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://52.4.122.204:3306/crazy", "phase2", "phase2");

			for (int i = 0; i < 55811731; i += 1000000) 
			{
				// Query 6 Cache
				String sql = "SELECT * FROM query6 LIMIT " + i + ",1000000;";
				PreparedStatement statement = connection.prepareStatement(sql);
				ResultSet resultSetQuery6 = null;
				try 
				{
					resultSetQuery6 = statement.executeQuery();
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
				}

				while (resultSetQuery6.next()) 
				{
					query6Map.put(resultSetQuery6.getLong(1),
							resultSetQuery6.getInt(2));

				}

				resultSetQuery6.close();
			}

			String sql2 = "SELECT tweetid, tweetinfo FROM query2 WHERE userid=? AND compare_date=? ORDER BY tweetid;";
			
			final PreparedStatement statement2 = connection.prepareStatement(sql2);
			String sql3 = "SELECT response FROM query3 WHERE userid=?;";
			final PreparedStatement statement3 = connection.prepareStatement(sql3);
			
			String sql4 = "SELECT CONCAT_WS(',', tweetid, userid, DATE_FORMAT(date_created,'%Y-%m-%d+%H:%i:%s')) AS response FROM query4 WHERE hashtag=? AND compare_date >= ? AND compare_date <= ? ORDER BY tweetid;";

			final PreparedStatement statement4 = connection.prepareStatement(sql4);
			Undertow server = Undertow.builder().addHttpListener(80, "0.0.0.0")
					.setHandler(new HttpHandler() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see
						 * io.undertow.server.HttpHandler#handleRequest(io.undertow
						 * .server.HttpServerExchange)
						 */
						@Override
						public void handleRequest(
								final HttpServerExchange exchange) {
							try {
								StringBuilder responseMessage = new StringBuilder(
										"cloudaaeeeb,2746-2065-5864,3773-7548-5421,4686-5029-0598\n");
								exchange.getResponseHeaders().put(
										Headers.CONTENT_TYPE, "text/plain");
								String check = exchange.getRequestPath();

								// Query 1
								if (check.equals("/q1")) {
									responseMessage.append(dateFormat
											.format(new Date()) + "\n");

									String msg = UnSpiralMsg(exchange
											.getQueryParameters()
											.get("message").getFirst());

									String key = exchange.getQueryParameters()
											.get("key").getFirst();

									if (query1Map.containsKey(key)) {
										int zKey = query1Map.get(key);
										for (int i = 0, temp = 'a'; i < msg
												.length(); i++) {
											temp = (char) (msg.charAt(i) - zKey);
											responseMessage
													.append(temp >= 65 ? ((char) (temp))
															: ((char) (temp + 26)));
										}

										responseMessage.append("\n");
									} else {
										int zKey = decipher(key);
										for (int i = 0, temp = 'a'; i < msg
												.length(); i++) {
											temp = (char) (msg.charAt(i) - zKey);
											responseMessage
													.append(temp >= 65 ? ((char) (temp))
															: ((char) (temp + 26)));
										}
										responseMessage.append("\n");
									}
								}

								// Query 2
								else if (check.equals("/q2")) {
									String userid = exchange
											.getQueryParameters().get("userid")
											.getFirst();
									String timestamp = exchange
											.getQueryParameters()
											.get("tweet_time").getFirst();

									try {
										

								        statement2.setString(1, userid);
								        statement2.setString(2, timestamp.replaceAll(
														"[-+: ]", ""));
								        
										ResultSet rs = null;
										try {
											rs = statement2.executeQuery();
										} catch (SQLException e) {
											e.printStackTrace();
										}

										// append to response
										while (rs.next()) {
											responseMessage.append(rs.getLong(1));
											responseMessage.append(":");
											responseMessage.append(rs.getString(2));
										}

										// Clean-up environment
										rs.close();
									} catch (SQLException se) {
										// Handle errors for JDBC
										se.printStackTrace();
									} catch (Exception e) {
										// Handle errors for Class.forName
										e.printStackTrace();
									}
								}

								// Query 3
								else if (check.equals("/q3")) {
									String response = null;
									// System.out.println("Q3");
									String userId = exchange
											.getQueryParameters().get("userid")
											.getFirst();
									// System.out.println("UserId: " + userid);

									
										try {
											// Query
											
									        statement3.setString(1, userId);
											// Get Result
											ResultSet rs = null;
											try {
												rs = statement3.executeQuery();
											} catch (SQLException e) {
												e.printStackTrace();
											}

											// append to response
											while (rs.next()) {
												response = rs
														.getString("response");
												responseMessage
														.append(response);
											}

											// Clean-up environment
											rs.close();
										} catch (SQLException se) {
											// Handle errors for JDBC
											se.printStackTrace();
										} catch (Exception e) {
											// Handle errors for Class.forName
											e.printStackTrace();
										}
									}
								

								// Query 4
								else if (check.equals("/q4")) {
									String hashtag = exchange
											.getQueryParameters()
											.get("hashtag").getFirst();

									String start = exchange
											.getQueryParameters().get("start")
											.getFirst();

									String end = exchange.getQueryParameters()
											.get("end").getFirst();
									try {
										
								        statement4.setString(1, hashtag);
								        statement4.setString(2, start.replaceAll("[-+: ]", ""));
								        statement4.setString(3, end.replaceAll("[-+: ]", ""));
								        
										// Get Result
										ResultSet rs = null;
										try {
											rs = statement4.executeQuery();
										} catch (SQLException e) {
											e.printStackTrace();
										}

										// append to response
										while (rs.next()) {
											String response = rs
													.getString("response");
											responseMessage.append(response);
											responseMessage.append("\n");
										}

										// Clean-up environment
										rs.close();
									} catch (SQLException se) {
										// Handle errors for JDBC
										se.printStackTrace();
									} catch (Exception e) {
										// Handle errors for Class.forName
										e.printStackTrace();
									}
								}

								// Query 5
								else if (check.equals("/q5")) {
									String startDate = null, endDate = null;

									String userList = exchange
											.getQueryParameters()
											.get("userlist").getFirst();

									String start = exchange
											.getQueryParameters().get("start")
											.getFirst();

									String end = exchange.getQueryParameters()
											.get("end").getFirst();

									try {
										String sql = "SELECT userid, SUM(unique_tweet) + 3 * MAX(friend) + 5 * MAX(follower) AS score FROM query5 WHERE userid IN ("
												+ userList
												+ ") AND compare_date >= '"
												+ start.replaceAll("[-+: ]", "")
												+ "' AND compare_date <= '"
												+ end.replaceAll("[-+: ]", "")
												+ "'GROUP BY userid ORDER BY 2 DESC, 1 ASC;";
										// Get Result
										ResultSet rs = null;
										Statement statement = connection.createStatement();
								        
								        
										try {
											rs = statement.executeQuery(sql);
										} catch (SQLException e) {
											e.printStackTrace();
										}

										// append to response
										while (rs.next()) {
											responseMessage.append(rs.getLong(1));
											responseMessage.append(",");
											responseMessage.append(rs.getLong(2));
											responseMessage.append("\n");
										}

										// Clean-up environment
										rs.close();
									} catch (SQLException se) {
										// Handle errors for JDBC
										se.printStackTrace();
									} catch (Exception e) {
										// Handle errors for Class.forName
										e.printStackTrace();
									}

								}

								// Query 6
								else if (check.equals("/q6")) {
									long m = Long.parseLong(exchange
											.getQueryParameters().get("m")
											.getFirst());

									long n = Long.parseLong(exchange
											.getQueryParameters().get("n")
											.getFirst());

									String response = String
											.valueOf((query6Map.floorEntry(n)
													.getValue() - query6Map
													.ceilingEntry(m).getValue()) + 1);
									responseMessage.append(response);
									responseMessage.append("\n");
								} else {
									responseMessage
											.append("BAD Request!!! \n\nCloud crashed down to Earth!");
								}

								exchange.getResponseSender().send(
										responseMessage.toString());

							} catch (Exception e) {
								System.out.println(e);
							}
						}
					}).build();
			server.start();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public final static int decipher(String xykey) {
		BigInteger xyKey = new BigInteger(xykey);
		int zKey = 0;
		try {
			long yKey = xyKey.divide(xKey).longValue();
			zKey = (int) (yKey % 25) + 1;
			if (query1Map.size() < 200000)
				query1Map.put(xyKey.toString(), zKey);
		} catch (Exception e) {
			System.out.println(e);
		}
		return zKey;
	}

	public static String UnSpiralMsg(String message) {
		StringBuilder result = new StringBuilder();
		try {
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
			while (a < m && b < n) {
				for (i = b; i <= n - 1; i++)
					result.append(matrix[a][i]);
				a++;
				for (i = a; i <= m - 1; i++)
					result.append(matrix[i][n - 1]);
				n--;
				if (a < m) {
					for (i = n - 1; i >= b; i--)
						result.append(matrix[m - 1][i]);
					m--;
				}
				if (b < n) {
					for (i = m - 1; i >= a; i--)
						result.append(matrix[i][b]);
					b++;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return result.toString();
	}
}
