<%@page contentType="text/html; charset=UTF-8" pageEncoding="GBK" %>
<%@page import="ext.c504.part.CPartService" %>
<%
String oid=request.getParameter("oid");
String node=request.getParameter("node");
out.println(CPartService.getSubJsonArray(node,oid));
%>