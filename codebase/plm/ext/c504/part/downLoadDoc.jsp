<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="wt.httpgw.WTContextBean,ext.c504.part.*"%>
<%@page import="java.io.File"%>
<%@ page contentType="text/html; charset=UTF-8" import="com.ptc.netmarkets.model.NmException"
%><%
 response.setContentType("text/html; charset=UTF-8");
%><%@ page import="com.ptc.netmarkets.util.beans.*"
%><%@ page import="com.ptc.netmarkets.util.misc.*"
%><%@ page import="com.ptc.netmarkets.util.wizard.*"
%><%@ page import="com.ptc.netmarkets.part.NmPartHelper"
%><%@ page import="com.ptc.netmarkets.part.NmPartCommands"
%><%@ page import="com.ptc.netmarkets.part.partResource,
                    com.ptc.netmarkets.model.*,
                    com.ptc.netmarkets.model.NmSimpleOid,
                    com.ptc.netmarkets.folder.NmFolderHelper,
                    com.ptc.netmarkets.util.beans.NmClipboardUtility,
                    java.util.*,
                    com.ptc.netmarkets.util.utilResource,
                    wt.inf.container.WTContainerRef,
                    com.ptc.netmarkets.part.NmPartCommands"
%><jsp:useBean id="clipboardBean" class="com.ptc.netmarkets.util.beans.NmClipboardBean" scope="session"/><%//
%>

<%@ taglib uri="http://www.ptc.com/windchill/taglib/components"	prefix="jca"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<jsp:useBean id="sessionBean" class="com.ptc.netmarkets.util.beans.NmSessionBean" scope="session"/>
<jsp:useBean id="nmcontext" class="com.ptc.netmarkets.util.beans.NmContextBean" scope="request">
<jsp:setProperty name="nmcontext" property="portlet" param="portlet"/>
</jsp:useBean>
<jca:getPageModel var="jcaPageModel" scope="page" />
<c:set target="${nmcontext}" property="bodystart_rendered" value="${true}" />
	<%!
final static String OBJECT_RESOURCE = "com.ptc.netmarkets.util.utilResource";
%>
<%
try{
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
	NmCommandBean cb1 = new NmCommandBean();
   // cb1.setInBeginJsp        (true);
   // cb1.setOpenerCompContext (request.getParameter("compContext"));
   // cb1.setOpenerElemAddress (NmCommandBean.convert(request.getParameter("openerElemAddress")));
    cb1.setCompContext       (NmCommandBean.convert(request.getParameter("compContext")));
   // cb1.setElemAddress       (NmCommandBean.convert(request.getParameter("elemAddress")));
    cb1.setSessionBean       (sessionBean);
    cb1.setRequest           (request);
   // cb1.setResponse          (response);
   // cb1.setOut               (out);
  //  cb1.setWtcontextBean ((WTContextBean)request.getAttribute("wtcontext"));
    

	ArrayList oidList = cb1.getSelectedOidForPopup();
    cb1.setSelected(oidList);
    System.out.println("-------size----->"+oidList);
    
    if(oidList.size()==0){
    	%>
    	<script type="text/javascript">
    	alert("请先选择要下载的文档!");
    	window.close();
    	</script>
    	<%
    }else{
    	ArrayList list=CPartService.getDownLoadDoc(oidList);
    	if(list.size()==0){
    		%>
        	<script type="text/javascript">
        	alert("所选文档主要内容为空，无法下载!");
        	window.close();
        	</script>
        	<%
    	}else{
    		DownLoadPrintPackage dpp = new DownLoadPrintPackage(pageContext);
    		dpp.downLoadFile(list);
    	}
    }
}catch(Exception e){
}
%>
