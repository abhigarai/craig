<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>	
<center>
<h1>Advertisement Recommendation System</h1>		
<form action="AdminController" method=post>
<h3>Option:</h3>
<select name="optName" id="optId">
<option value="createAd">Create Ads</option>
<option value="deleteAd">Delete Ads</option>
</select>   
<br>
<br>
<input type="submit" value="Go Ahead">
</form>		
</center>
</body>
</html>