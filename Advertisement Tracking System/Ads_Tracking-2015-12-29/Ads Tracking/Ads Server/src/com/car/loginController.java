package com.car;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.jdbc.pool.DataSource;

/**
 * Servlet implementation class loginController
 */
public class loginController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
       
    /**
     * @see HttpServlet#HttpServlet()
     */

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
	    doPost(req, resp);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
	    Connection conn=null;
	    try
	    {
	        
	        
	        conn = new DatabaseConnection().getAPooledConnection();
	        
            
            String userId=req.getParameter("userId");
            String pwd=req.getParameter("pwd");
            
            String sql="select pwd from detail_table.userdetails where userId LIKE ?";
            PreparedStatement val= conn.prepareStatement(sql);
            val.setString(1, "%" + userId + "%");
            
            ResultSet res=val.executeQuery();
            
                if(res.next())
                {
                    System.out.println("Valid");
                    if(res.getString("pwd").equals(pwd))
                    {
                        //valid
                        System.out.println("Valid pwd");
                       //String id=req.getParameter("userId");
                        resp.sendRedirect("EnterSystem.jsp");
                    }
                    else
                    {
                        //login page
                        System.out.println("Output2");
                        resp.sendRedirect("login.jsp");
                    }
                }
                else
                {
                    //login page
                    System.out.println("Output3");
                    resp.sendRedirect("signup.jsp");
                }
            //}
	    }
	    catch (SQLException e) {
           
           // if some thing database error than compiler come this part 
           e.printStackTrace();
       }
	}
}
