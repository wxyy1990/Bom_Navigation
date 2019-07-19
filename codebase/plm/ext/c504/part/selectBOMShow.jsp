
<html>
<link rel="stylesheet" type="text/css" href="../../../../css/avidm.css" />
<link rel="stylesheet" type="text/css" href="../../../../css/plm/left.css" />
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/javascript/ext/resources/css/ext-all.css" /> 
<script type="text/javascript" src="<%=request.getContextPath() %>/javascript/ext/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/javascript/ext/ext-all-debug.js"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/javascript/ext/CollectGarbage.js"></script>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/javascript/ext/resources/css/ext-sasc.css" /> 
<script type="text/javascript" src="<%=request.getContextPath() %>/javascript/ext/source/locale/ext-lang-zh_CN.js" ></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/javascript/ext/ext-sasc.js" ></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/javascript/edo/edo.js"></script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>

<script type="text/javascript">
Ext.BLANK_IMAGE_URL = '<%=request.getContextPath() %>/javascript/ext/resources/images/default/s.gif';
var tree4=null;
Ext.onReady(function() {
	sendProdList(null);
});
function sendProdList(oid){
	alert(oid);
	document.getElementById("tree-div").innerHTML = "";
	tree4= new Ext.tree.TreePanel({
	    	id:"prodstructTree",
	        autoScroll:true,      //自动滚动   
	        containerScroll: true,  
	        animate:true,     //动画效果   
	        border:false,      //边框   
	        enableDD:true,        //节点是否可以拖拽              
	        rootVisible : false,  //跟节点是否可见
	        title:"产品结构",     //标题栏
	        lines:false,           //节点之间连接的横竖线
	        loader: new Ext.tree.TreeLoader({   
		        dataUrl: 'treeGetChildPart.jsp?oid='+oid
	        }) 
	     });
	   tree4.on('click', function(node){
	    	 //console.log(parent.parent);
	    	 parent.parent.mainFrame.location = node.attributes.url;
	    }); 
	   

		var root4 = new Ext.tree.AsyncTreeNode({ 
			id: "root", 
			text: "跟节点",
			url:"",
			draggable:false,
			hrefTarget:'rightFrame'
			}); 
		tree4.setRootNode(root4);
		tree4.render("tree-div");
		root4.expand();
}

function showProdWin(){
	var openWind = window.open(
			"showProdList.jsp",
			"SelectProduct",
			"scrollbars=true,Width=640,status=no,Height=540");
 	openWind.focus();
}

function clearSelectedWin(){
	  if (confirm("确定要清除选择的产品吗？")==true) {
		  var openWind = window.open(
					"clearSProdList.jsp",
					"clearSProdList",
					"scrollbars=true,Width=300,status=no,Height=200");
		 	openWind.focus();
	  }
}


</script>
<form name="form1" method="post">
<table border="0" width="100%"  height="100%" cellspacing="0">
 	<tr>
	 	<td class="left_top" height="25">
		  <table width="100%" border="0" cellspacing="0" cellpadding="0" >
		
			<tr>
			<td >
				<!--<input type="button" bak_value="OK" class="left_search"  onclick="showProdstruct()" />-->
			
			  <input type="button" value="选择产品" class="AvidmButton" onclick="showProdWin()" name="button">
			  <input type="button" value="清除选择" class="AvidmButton" onclick="clearSelectedWin()" name="button">
			</td>
			
			</tr>
	
		  </table>
	  </td>
  </tr>
  <tr>
    <td >
		<div id="tree-div" style="position:absolute;width:100%; height:100%; overflow:auto" ></div>
    </td>
  </tr>
</table>


</form>
</body>
</html>