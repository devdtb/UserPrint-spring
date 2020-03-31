<%@page import="java.util.Date"%>
<%@page import="dtb.user.print.web.controller.UserPrintController"%>
<%@page import="java.util.Iterator"%>
<%@page import="dtb.user.print.entity.UserData"%>
<%@page import="dtb.user.print.poi.XlsUserDataExtractor"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Main User Table</title>
	</head>
	
	<body>
		<form method="post" action="/stop">
			<button type="submit">Stopp application</button>
		</form>
		
		<p>${msg}</p>
		<p>${error}</p>
		
		<br>
		
		<form method="post" action="/upload" enctype="multipart/form-data">
			<input type="file" name="file">
			<button type="submit">Upload</button>
		</form>
		
		<br><br>
		
		<%
		Date issueDate = (Date) session.getAttribute(UserPrintController.ISSUE_DATE);
		%>
		<p>Issue Date: <%=issueDate != null ? UserPrintController.DATE_FORMAT.format(issueDate) : "Not initialized" %></p>
		<form method="post" action="/changeIssueDate">
			<input type="text" name="<%=UserPrintController.ISSUE_DATE %>" placeholder="dd.mm.yyyy">
			<button type="submit">Change Issue Date</button>
		</form>
		
		<br><br>
		
		<form method="post" action="/reset">
			<button type="submit">Reset Table</button>
		</form>
		
		<br><br>
		
		<p>Name separator: <%=XlsUserDataExtractor.NAME_SEPARATOR %></p>
		<table>
			<thead>
				<tr>
					<th>Nr. Crt.</th>
					<th></th>
					<th>Nume</th>
					<th>Prenume</th>
					<th>C.N.P</th>
					<th>Domiciliu</th>
					<th>Act de identitate</th>
				</tr>
			</thead>
			
			<tbody>
		<%
		
		Iterable<UserData> userDataItr = (Iterable<UserData>) request.getAttribute("userDataItr"); 
		
		%>
		
		<%if(userDataItr != null){ %>
			<%
			Iterator<UserData> iter = userDataItr.iterator();
			while(iter.hasNext()){
				UserData userData = iter.next();
			%>
			<tr>
				<td><%=userData.getCtr() != null ? userData.getCtr() : "" %></td>
				<td>
					<a href="/download?id=<%=String.valueOf(userData.getId()) %>" target="_blank">Print</a>
				</td>
				
				<td><%=userData.getLname() != null ? userData.getLname() : ""  %></td>
				<td><%=userData.getFname() != null ? userData.getFname() : ""  %></td>
				<td><%=userData.getCnp() != null ? userData.getCnp() : ""  %></td>
				<td><%=userData.getAddress() != null ? userData.getAddress() : ""  %></td>
				<td><%=userData.getIdnr() != null ? userData.getIdnr() : ""  %></td>
			</tr>
				
			<%}%>
		<%}%>
			</tbody>
		</table>
	
	</body>
	
	
	<style>
	
		table {
			border-collapse: collapse;
		}
		
		table, th, td {
			border: 1px solid black;
		}
	
		thead {
			background-color: #F5F5F5;
			text-align: left;
			align: left;
		}
		
		th, td {
			text-align: left;
			align: left;
			border-style: solid;
			border-width: 0.5;
			padding: 5px;
		}
	
	</style>

</html>

