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
 * Servlet implementation class SystemController
 */
public class SystemController extends HttpServlet {
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
        
        
        try {
            PrintWriter printWriter= resp.getWriter();
            conn = new DatabaseConnection().getAPooledConnection();
            // Create    for Query
            String data=req.getParameter("roleName");
            if(data.equals("admin"))
            {
                resp.sendRedirect("Admin.jsp");
            }
            else
            {
                String sql1="select AdId, Contents from detail_table.advertisement where NoClicks LIKE ( select MAX(NoClicks) from detail_table.advertisement) LIMIT 1";
                
                PreparedStatement sel1= conn.prepareStatement(sql1);
                ResultSet res1=null;
                res1=sel1.executeQuery();
                
                StringBuilder ad1=new StringBuilder();
                while(res1.next())
                {
                    ad1.append(res1.getString("AdId")+"  "+res1.getString("Contents"));
                }
                
              ///***//
                String updDb111="select AdId from detail_table.advertisement where NoClicks LIKE (select MAX(NoClicks) from detail_table.advertisement) LIMIT 1";
                PreparedStatement updPStmt111= conn.prepareStatement(updDb111);
                ResultSet upd111 = updPStmt111.executeQuery();
                int Intupd111=0;
                while(upd111.next())
                {
                     Intupd111=Integer.parseInt(upd111.getString("AdId"));
                }
                
                
                //Update1
                String updDb11 ="select NoImpr from detail_table.advertisement where AdId LIKE ?";
                PreparedStatement updPStmt11= conn.prepareStatement(updDb11);
                updPStmt11.setString(1, String.valueOf(Intupd111));
                
                ResultSet upd11 = updPStmt11.executeQuery();
                int Intupd11=0;
                while(upd11.next())
                {
                     Intupd11=Integer.parseInt(upd11.getString("NoImpr"));
                }
                Intupd11++;
                
                String updDb12 ="update detail_table.advertisement set NoImpr=? where AdId LIKE ?";
                PreparedStatement updPStmt12= conn.prepareStatement(updDb12);
                updPStmt12.setString(1,String.valueOf(Intupd11));
                updPStmt12.setString(2, String.valueOf(Intupd111));
                
                updPStmt12.executeUpdate();
                
                           ///***//
                String sql2="select AdId, Contents from detail_table.advertisement where NoClicks LIKE ( select MIN(NoClicks) from detail_table.advertisement) LIMIT 1";
                
                PreparedStatement sel2= conn.prepareStatement(sql2);
                ResultSet res2=null;
                res2=sel2.executeQuery();
                
                StringBuilder ad2=new StringBuilder();
                while(res2.next())
                {
                    ad2.append(res2.getString("AdId")+"  "+res2.getString("Contents"));
                }
                /***/

              ///***//
                String updDb211="select AdId from detail_table.advertisement where NoClicks LIKE (select MIN(NoClicks) from detail_table.advertisement) LIMIT 1";
                PreparedStatement updPStmt211= conn.prepareStatement(updDb211);
                ResultSet upd211 = updPStmt211.executeQuery();
                int Intupd211=0;
                while(upd211.next())
                {
                     Intupd211=Integer.parseInt(upd211.getString("AdId"));
                }
                
                
                //Update1
                String updDb21 ="select NoImpr from detail_table.advertisement where AdId LIKE ?";
                PreparedStatement updPStmt21= conn.prepareStatement(updDb21);
                updPStmt21.setString(1, String.valueOf(Intupd211));
                
                ResultSet upd21 = updPStmt21.executeQuery();
                int Intupd21=0;
                while(upd21.next())
                {
                     Intupd21=Integer.parseInt(upd21.getString("NoImpr"));
                }
                Intupd21++;
                
                String updDb22 ="update detail_table.advertisement set NoImpr=? where AdId LIKE ?";
                PreparedStatement updPStmt22= conn.prepareStatement(updDb22);
                updPStmt22.setString(1,String.valueOf(Intupd21));
                updPStmt22.setString(2, String.valueOf(Intupd211));
                
                updPStmt22.executeUpdate();
                
                           ///***//
                
                
                /***/
                
                
                printWriter.write(ad1.toString()+"<br>");
                printWriter.write("<br>");
                printWriter.write(ad2.toString()+"<br>");
                printWriter.write("<br>");
                printWriter.close();
            }
        }
        catch (SQLException e) {
            
            // if some thing database error than compiler come this part 
            e.printStackTrace();
        }

	}

}
