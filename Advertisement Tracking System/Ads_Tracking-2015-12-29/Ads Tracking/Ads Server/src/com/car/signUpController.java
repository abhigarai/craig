package com.car;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class SignUpController
 */
public class signUpController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
	    Connection conn=null;
        try
        {
            conn = new DatabaseConnection().getAPooledConnection();
            String userId=req.getParameter("userId");
            String pwd=req.getParameter("pwd");
            String userName=req.getParameter("userName");
            
            String sql="insert into detail_table.userdetails VALUES(?,?,?)";
            PreparedStatement val= conn.prepareStatement(sql);
            val.setString(1, userId);
            val.setString(2,pwd);
            val.setString(3,userName);            
            val.execute();
            
            resp.sendRedirect("login.jsp");
        }
        catch (SQLException e) {
            
            // if some thing database error than compiler come this part 
            e.printStackTrace();
        }   
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
	    doPost(req, resp);
	}

}
