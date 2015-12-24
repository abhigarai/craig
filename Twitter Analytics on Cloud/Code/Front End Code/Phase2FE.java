package phase2;

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

import org.async.jdbc.AsyncConnection;
import org.async.mysql.MysqlConnection;
import org.async.net.Multiplexer;
import org.async.jdbc.*;

import com.mysql.jdbc.Driver;

public class Phase2FE {
	public static BigInteger xKey = new BigInteger(
			"8271997208960872478735181815578166723519929177896558845922250595511921395049126920528021164569045773");
	public static ConcurrentHashMap<String, Integer> chm = new ConcurrentHashMap<String, Integer>();

	private static boolean first_q6 = true;
	private static TreeMap<Integer, Integer> tm;
	
	public static void main(String[] args) {
		final DateFormat dateFormat = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss");
		private static boolean first_q6 = true;
		private static TreeMap<Integer, Integer> tm;
		for (int i = 1; i < 500000; i++) {
			BigInteger k = (BigInteger.valueOf(i));
			int z;
			z = (i % 25) + 1;
			chm.put((xKey.multiply(k).toString()), z);
		}
		try {
			// JDBC driver name and database URL
			final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
			final String mysql_ip = "52.5.160.248:3306";
			final String DB = "phase2";
			final String DB_URL = "jdbc:mysql://" + mysql_ip + "/" + DB;

			// Database credentials
			final String USER = "phase2";
			final String PASS = "phase2";

			// Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver").newInstance();

			// Open a connection
			final Connection conn = DriverManager.getConnection(DB_URL, USER,
					PASS);

			// System.out.println("Connected to Database: " + DB + " on "+
			// mysql_ip);

			Undertow server = Undertow.builder().addHttpListener(80, "0.0.0.0")
					.setHandler(new HttpHandler() {
						@Override
						public void handleRequest(
								final HttpServerExchange exchange) {
							try {
								StringBuilder responseMessage = new StringBuilder(
										"cloudaaeeeb,2746-2065-5864,3773-7548-5421\n");
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

									if (chm.containsKey(key)) {
										int zKey = chm.get(key);
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

									System.out.println("Q2");

									String userid = exchange
											.getQueryParameters().get("userid")
											.getFirst();

									String timestamp = exchange
											.getQueryParameters()
											.get("tweet_time").getFirst();

									System.out.println("UserId: " + userid
											+ "\tTweet_Time: " + timestamp);

									try {
										// Query
										Statement stmt = conn.createStatement();
										String sql = "SELECT CONCAT_WS(':', tweetid, tweetinfo) AS response FROM query2 WHERE userid="
												+ userid
												+ " AND date_created='"
												+ timestamp
												+ "' ORDER BY tweetid;";

										// Get Result
										ResultSet rs = null;
										try {
											rs = stmt.executeQuery(sql);
										} catch (SQLException e) {
											e.printStackTrace();
										}

										// append to response
										while (rs.next()) {
											String response = rs
													.getString("response");
											responseMessage.append(response);
											// responseMessage.append("\n");
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

									System.out.println("Q3");

									String userid = exchange
											.getQueryParameters().get("userid")
											.getFirst();

									System.out.println("UserId: " + userid);

									try {
										// Query
										Statement stmt = conn.createStatement();
										String sql = "SELECT response FROM query3 WHERE userid="
												+ userid + ";";

										// Get Result
										ResultSet rs = null;
										try {
											rs = stmt.executeQuery(sql);
										} catch (SQLException e) {
											e.printStackTrace();
										}

										// append to response
										while (rs.next()) {
											String response = rs
													.getString("response");
											responseMessage.append(response);
											// responseMessage.append("\n");
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

									System.out.println("Q4");

									String hashtag = exchange
											.getQueryParameters()
											.get("hashtag").getFirst();

									String start = exchange
											.getQueryParameters().get("start")
											.getFirst();

									String end = exchange.getQueryParameters()
											.get("end").getFirst();

									System.out.println("Hashtag: " + hashtag
											+ "\tStart_Time: " + start
											+ "\tEnd_Time: " + end);

									try {
										// Query
										Statement stmt = conn.createStatement();
										String sql = "SELECT CONCAT_WS(',', tweetid, userid, DATE_FORMAT(date_created,'%Y-%m-%d+%H:%i:%s')) AS response FROM query4 WHERE hashtag='"
												+ hashtag
												+ "' AND date_created >= '"
												+ start
												+ "' AND date_created <= DATE_FORMAT('"
												+ end
												+ "+23:59:59','%Y-%m-%d+%H:%i:%s') ORDER BY tweetid;";

										// Get Result
										ResultSet rs = null;
										try {
											rs = stmt.executeQuery(sql);
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

								// Query 6
								else if (check.equals("/q6")) {

									System.out.println("Q6");

									double user1 = Double.parseDouble(exchange
											.getQueryParameters().get("m")
											.getFirst());

									double user2 = Double.parseDouble(exchange
											.getQueryParameters().get("n")
											.getFirst());

									if (first_q6) {
										tm = new TreeMap<Double, Integer>();
										try {
											// Query
											Statement stmt = conn
													.createStatement();
											String sql = "SELECT * from query6";

											// Get Result
											ResultSet rs = null;
											try {
												rs = stmt.executeQuery(sql);
											} catch (SQLException e) {
												e.printStackTrace();
											}

											// append to response
											while (rs.next()) {
												tm.put(rs.getDouble(1),
														rs.getInt(2));
												System.out.print(rs.getDouble(1)+"\t"+
														rs.getInt(2));
											}

											// Clean-up environment
											rs.close();

											first_q6 = false;
										} catch (SQLException se) {
											// Handle errors for JDBC
											se.printStackTrace();
										} catch (Exception e) {
											// Handle errors for Class.forName
											e.printStackTrace();
										}

									}
									String response = String.valueOf((tm.floorEntry(user2).getValue() - tm.ceilingEntry(user1).getValue())+1);
									responseMessage.append(response);
									responseMessage.append("\n");

								}
								// Bad request
								else {
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