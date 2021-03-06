package com.car;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class CreateAdsController
 */
public class CreateAdsController extends HttpServlet {
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
            
            String adId=req.getParameter("adId");
            String contents=req.getParameter("contents");
            
            
            String sql="insert into detail_table.advertisement VALUES(?,?,?,?)";
            PreparedStatement val= conn.prepareStatement(sql);
            val.setString(1, adId);
            val.setString(2,"0");
            val.setString(3,"0");
            val.setString(4, "<a href=\""+"http://localhost:8080/Ads Server/ImpressionController?param1="+adId+"&param2="+contents+"\">"+contents+"</a>");
            
            //r.append("<a href=\""+contents+"\">"+contents+"</a>");
            
            val.execute();
            
            resp.sendRedirect("Admin.jsp");
        }
        catch (SQLException e) {
            
            // if some thing database error than compiler come this part 
            e.printStackTrace();
        }
	}

}
