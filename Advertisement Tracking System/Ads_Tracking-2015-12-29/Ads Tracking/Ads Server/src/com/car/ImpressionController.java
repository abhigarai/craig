package com.car;

import java.io.IOException;
import java.io.PrintWriter;
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
 * Servlet implementation class ImpressionController
 */
public class ImpressionController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
	    doPost(req,resp);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
Connection conn=null;
        
        
        try {
            PrintWriter printWriter= resp.getWriter();
            conn = new DatabaseConnection().getAPooledConnection();
            String adId=req.getParameter("param1");
            
            String url=req.getParameter("param2");
            
            
            String updDb1 ="select NoClicks from detail_table.advertisement where AdId LIKE ?";
            PreparedStatement updPStmt1= conn.prepareStatement(updDb1);
            updPStmt1.setString(1, String.valueOf(adId));
            
            ResultSet upd11 = updPStmt1.executeQuery();
            int Intupd11=0;
            while(upd11.next())
            {
                 Intupd11=Integer.parseInt(upd11.getString("NoClicks"));
            }
            Intupd11++;
            
            String updDb12 ="update detail_table.advertisement set NoClicks=? where AdId LIKE ?";
            PreparedStatement updPStmt12= conn.prepareStatement(updDb12);
            updPStmt12.setString(1,String.valueOf(Intupd11));
            updPStmt12.setString(2, String.valueOf(adId));
            
            updPStmt12.executeUpdate();
            
            
            resp.sendRedirect(url);
            
            
        }
        catch (SQLException e) {
            
            // if some thing database error than compiler come this part 
            e.printStackTrace();
        }
	}

}
