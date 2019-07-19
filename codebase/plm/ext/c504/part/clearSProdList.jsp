<%@page contentType="text/html; charset=UTF-8" pageEncoding="GBK" %>
<%@page import="ext.c504.part.CPartService" %>

<%
CPartService.clearSProdList();
%>
<script type="text/javascript">

alert("Çå³ý³É¹¦!");
window.opener.location.href = window.opener.location.href;   
if (window.opener.progressWindow){
	  window.opener.progressWindow.close();
	}
window.close();   

</script>