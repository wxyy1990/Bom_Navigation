<% request.setAttribute(NmAction.SHOW_CONTEXT_INFO, "false"); %>
<%@ page import="com.ptc.windchill.enterprise.part.partResource" %>
<%@ page import="com.ptc.netmarkets.util.beans.NmHelperBean"%>
<%@ page import="wt.part.WTPart"%>
<%@ page import="wt.part.WTProductInstance2"%>
<%@ page import="java.util.Stack"%>
<%@ include file="/netmarkets/jsp/util/begin.jspf"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt" %>

<fmt:setBundle basename="com.ptc.windchill.enterprise.part.partResource" />

<fmt:message var="referencesDocTableHeader" key="<%= partResource.REFERENCES_DOC_TABLE_HEADER%>" />
<fmt:message var="describedByDocTableHeader" key="<%= partResource.DESCRIBED_BY_DOC_TABLE_HEADER%>" />

<%-->*****************************************************************************************************<--%>
<%
      boolean isPartInstance = false;
      //Changes start for SPR 1669275
	NmHelperBean nmhelperbean = null;
	nmhelperbean = (NmHelperBean) pageContext.getAttribute("com.ptc.jca.NmHelperBean");
	Stack stk =nmhelperbean.getNmContextBean().getContext().getContextItems();
      //Changes End for SPR 1669275
      int stkSize = stk.size();
      for (int i=0;i<stkSize;i++) {
      	NmContextItem ci = (NmContextItem)stk.get(i);
      	if (ci.getAction().equalsIgnoreCase("relatedPartInstanceDocuments")) {
      		isPartInstance = true;
      	}
      }

      String methodRef = "";
      String objectType = "";
	  String tableIdRef = "";
	  String tableIdDesc = "";

	  if (isPartInstance) {
	  	methodRef  = "getAssociatedReferenceDocumentVersions";
	  	objectType = "wt.part.WTProductInstance2";
	  	tableIdRef  = "part.relatedPartInstancesReferencesDocuments.list";
	  	tableIdDesc = "part.relatedPartInstancesDescribedByDocuments.list";
	  } else {
	  	methodRef  = "getAssociatedReferenceDocuments";
	  	objectType = "wt.part.WTPart";
	  	tableIdRef  = "part.relatedPartsReferencesDocuments.list";
	  	tableIdDesc = "part.relatedPartsDescribedByDocuments.list";
	  }

	  request.setAttribute("methodRef",methodRef);
	  request.setAttribute("objectType",objectType);
	  request.setAttribute("tableIdRef",tableIdRef);
	  request.setAttribute("tableIdDesc",tableIdDesc);

%>

<%-->Get the Described By relation NmHTMLTable from the command<--%>
<jca:describeTable var="described" id="${tableIdDesc}" type="wt.doc.WTDocument" label="${describedByDocTableHeader}" configurable="true" >

   <jca:setComponentProperty key="actionModel" value="relatedDocumentDescribesToolBar"/>
   <jca:describeColumn id="type_icon" />
   <jca:describeColumn id="number"    isInfoPageLink="true"  />
   <jca:describeColumn id="orgid" />
   <jca:describeColumn id="version" />
   <jca:describeColumn id="infoPageAction"/>
   <jca:describeColumn id="name" />
   <jca:describeColumn id="containerName" />
   <jca:describeColumn id="state" />
   <jca:describeColumn id="last_modified" />
</jca:describeTable>


<c:set target="${described.properties}" property="selectable" value="true"/>

<%-->Get a component model for our table<--%>
<jca:getModel var="tableModelDesc" descriptor="${described}"
              serviceName="com.ptc.windchill.enterprise.part.commands.PartDocServiceCommand"
              methodName="getAssociatedDescribeDocuments" >
<jca:addServiceArgument type="${objectType}" value="${param.oid}"/>
</jca:getModel>

<%-->Get the NmHTMLTable from the command<--%>
<jca:renderTable model="${tableModelDesc}"  pageLimit="0" helpContext=""
                 showPagingLinks="true" />

<%-->*****************************************************************************************************<--%>


<%-->*****************************************************************************************************<--%>

<%@ include file="/netmarkets/jsp/util/end.jspf"%>