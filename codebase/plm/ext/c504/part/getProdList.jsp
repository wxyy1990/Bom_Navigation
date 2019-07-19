<%@page contentType="text/html; charset=UTF-8" pageEncoding="GBK" %>
<%@page import="ext.c504.part.CPartService" %>
<%@ page import="com.cascc.avidm.util.*" %>

<%
String sessionID = (String)session.getAttribute(AvidmConstDefine.SESSIONPARA_SESSIONID);
out.println(CPartService.getProdList(sessionID));
%>