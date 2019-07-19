<%@ page contentType="text/html; charset=UTF-8" pageEncoding="GBK" %>

<%@ page import="com.cascc.avidm.util.*" %>
<%@ page import="com.cascc.platform.aa.*" %>
<%@ page import = "com.bjsasc.platform.i18n.*"%>
<%@ taglib uri="/WEB-INF/framework-ext.tld" prefix="ext" %> 

<%
//清除缓存
response.setHeader("Pragma", "No-cache");
response.setHeader("Cache-Control", "no-cache");
response.setDateHeader("Expires", 0);

 //登录用户SessionId
String sessionID = (String)session.getAttribute(AvidmConstDefine.SESSIONPARA_SESSIONID);
AAContext ctx = new AAContext(sessionID);

//定义起始位置
int pageStart = 0;

//定义列表页面每页数据量
int pageSize = 10;

//列表中是否允许checkbox
String checkBox = "true";
String orderDirection = "ASC";
String url="getProdList.jsp";

%>
<html>
<body>
<ext:ext >
<ext:grid id="grid" div="g" initCond="" url="<%=url%>" orderField="" checkbox="<%=checkBox%>"
		 orderDirection="" pageStart="<%=pageStart%>" pageSize="<%=pageSize%>" 
		 pageButtonAlign="none" >
		
		<ext:column field="PRODIID"  title='<%=PLU.getString(request,"pt.sysadm.selectproduct.prodIID")%>'  hidden="true" />
		<ext:column field="PRODID"  title='<%=PLU.getString(request,"pt.sysadm.selectproduct.prodID")%>'  sortable="false"/>
		<ext:column field="PRODNAME"  title='<%=PLU.getString(request,"pt.sysadm.selectproduct.prodName")%>' sortable="false"/>
		<ext:column field="PRODTYPE"  title='<%=PLU.getString(request,"pt.sysadm.selectproduct.prodType")%>' hidden="true"/>
		<ext:column field="DISPLAY"  title='<%=PLU.getString(request,"pt.sysadm.selectproduct.display")%>' hidden="true"/>
</ext:grid>

<ext:toolBar id="bar1" style="classic" div="f" >
	<ext:button onClick="confirmProd" iconCls="add" text='确定'/>
	<ext:button onClick="cancel" iconCls="delete" text='取消'/>
</ext:toolBar>	
</ext:ext>

<script type="text/javascript">
function confirmProd(){
	var selections = grid_extGrid.getSelectedRow();
	var records = "";
	for(var i = 0 ; i < selections.length ; i++){
		var newRecord = selections[i].copy();
		records= records+";"+newRecord.data.PRODIID;
		//records= records+";"+newRecord.data.PRODIID+"|"+newRecord.data.PRODID+"-"+newRecord.data.PRODNAME;
	}
	if(records==""){
		alert("请选择产品");
		return;
	}
	
	grid_extGrid.clearSelections();
	//console.log(records);
	parent.opener.sendProdList(records);
	parent.window.close();
}

function cancel(){
	parent.window.close();
}

</script>


	<table width="100%" height="100%" border="0">
		<tr height="90%">
			<td width="100%"><div id="g" style="height:100%;width:100%;"></div></td>
		</tr>
		
		<tr height="10%">
			<td align="center"><div id="f" style="height:100%;width:100%;"></div></td>
		</tr>		
	</table>
</body>
</html>