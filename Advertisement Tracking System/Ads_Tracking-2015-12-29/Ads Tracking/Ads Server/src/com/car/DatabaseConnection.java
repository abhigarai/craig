package com.car;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
 
import com.mchange.v2.c3p0.ComboPooledDataSource;
 
public class DatabaseConnection
{
                private static final ComboPooledDataSource newConnectionPool = new ComboPooledDataSource();
                private static final String MY_SQL_DRIVER = "com.mysql.jdbc.Driver";
                private static final String URL = "jdbc:mysql://localhost:3306/";
                private static final String USERNAME = "root";
                private static final String PASSWORD = "root";
                //static Connection giveConnection;
               
                public DatabaseConnection()
                {
                                init();
                }
                private final void init()
                {
                                try {
                                                newConnectionPool.setDriverClass(MY_SQL_DRIVER);
                                } catch (PropertyVetoException e) {
                                                e.printStackTrace();
                                }     
                                newConnectionPool.setJdbcUrl(URL);
                                newConnectionPool.setUser(USERNAME);                                 
                                newConnectionPool.setPassword(PASSWORD);                                 
                                newConnectionPool.setMinPoolSize(5);                                    
                                newConnectionPool.setAcquireIncrement(5);
                                newConnectionPool.setMaxPoolSize(20);
                                /*System.out.println("DB Connection pool created");*/
 
                }
 
                public static synchronized Connection getAPooledConnection() throws SQLException
                {
                                return newConnectionPool.getConnection();
                }
               
}