package ext.c504.part;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.ejb.CreateException;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import wt.doc.WTDocument;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.httpgw.URLFactory;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.pdmlink.PDMLinkProduct;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.type.ClientTypedUtility;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;

import com.bjsasc.platform.webframework.tag.util.JsonUtil;
import com.cascc.avidm.docman.model.DmFile;
import com.cascc.avidm.docman.web.DmDocumentManagerDelegate;
import com.cascc.avidm.prodadm.ejb.PAOrgMgr;
import com.cascc.avidm.prodadm.ejb.PAOrgMgrHome;
import com.cascc.avidm.servicelocator.web.ServiceLocator;
import com.cascc.avidm.sysadm.model.ProductModel;
import com.cascc.avidm.sysadm.web.SAProductCrtMgrCaller;
import com.cascc.platform.aa.org.ejb20.UserFactory;

import ext.avidm.AvidmUtil;
import ext.avidm.IBAConstants;
import ext.generic.GenericUtil;
import ext.generic.IBAUtil;
public class CPartService implements RemoteAccess{
	public static DBConnectionUtil dbUtil;
	private static String SPLIT_CHAR=";";
	private static HashMap<String,String> subTypeMap=new HashMap<String,String>();
	static{
		subTypeMap.put("StandardPart", "标准件");
		subTypeMap.put("StandalonePart", "单机");
		subTypeMap.put("fxt", "分系统");
		subTypeMap.put("Part", "零件");
		subTypeMap.put("softpart", "软件配置项");
		subTypeMap.put("ModelPart", "型号");
		subTypeMap.put("SubSystemPart", "子系统");
		subTypeMap.put("SubChildSystemPart", "子系统、多功能组件");
		subTypeMap.put("AssemblyPart", "组件");
	}
	private static String IBATYPE_STANDARDPART="StandardPart"; //标准件软类型
	private static String IBATYPE_MODELPART="ModelPart"; //型号软类型
	private static String IBA_PHASE="document_phase";//软属性
	
	public static void clearSProdList() throws Exception{
		if(!RemoteMethodServer.ServerFlag){
			RemoteMethodServer method = RemoteMethodServer.getDefault();

				method.invoke("clearSProdList", CPartService.class.getName(), null, 
						new Class[]{}, new Object[]{});
			
		}else{
			String userName=SessionHelper.manager.getPrincipal().getName();
        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false); 
        	
        	
        	String sql="delete from SelectedPDMLinkProductInfo where userName='"+userName+"'";
        	Debug.P("==>sql:"+sql);
			excuteQuery(sql);
        	
        	SessionServerHelper.manager.setAccessEnforced(enforce);
		}
	}
	/**
	 * 获取当前用户关联的产品
	 * @param sessionID
	 * @return
	 * @throws JSONException
	 * @throws WTException
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws InvocationTargetException
	 */
	public static String getProdList(String sessionID) throws JSONException, WTException, RemoteException, CreateException, InvocationTargetException{
		if(!RemoteMethodServer.ServerFlag){
			RemoteMethodServer method = RemoteMethodServer.getDefault();

				return (String)method.invoke("getProdList", CPartService.class.getName(), null, 
						new Class[]{String.class}, new Object[]{sessionID});
			
		}else{
			  Debug.P("==>sessionID:"+sessionID);
			  ServiceLocator loc = ServiceLocator.getInstance();
			 
			  PAOrgMgrHome homePAOrg = (PAOrgMgrHome)loc.getRemoteHome("PAOrgMgr", PAOrgMgrHome.class);
			  PAOrgMgr beanPAOrg = homePAOrg.create(); 
	//			  Vector userProds = new Vector();
			  
			  UserFactory userFactory = UserFactory.getInstance();
			  String userInnerID = userFactory.convertIDtoInnerID(SessionHelper.manager.getPrincipal().getName());
			  Debug.P("==>userId:"+userInnerID);
			 
			  Vector vecAllPrjs = beanPAOrg.getUserProducts(sessionID,userInnerID);
			
	//			  SAProductCrtMgrHome homePrj = (SAProductCrtMgrHome)loc.getRemoteHome("SAProductCrtMgr", SAProductCrtMgrHome.class);
	//			  SAProductCrtMgr beanPrj = homePrj.create();
	//			  Vector vecAllPrjs = beanPrj.getAllProducts();
			  Debug.P("===>vecAllPrjs:"+vecAllPrjs.size());

	
			List list  = new ArrayList();
			for(int i =0 ;i<vecAllPrjs.size();i++){
				ProductModel m = (ProductModel)vecAllPrjs.get(i);
				Map map = new HashMap();
				map.put("PRODTYPE",m.sPType);
				map.put("PRODIID",m.sIID);
				map.put("PRODID",m.sId);
//				if(m.sPName.equals("国家标准件库")){
//					continue;
//				}
				
				map.put("PRODNAME",m.sPName);
				String display = m.sId+"("+m.sPName+")";
				map.put("DISPLAY",display);
				list.add(map);
			}
			return JsonUtil.listToJson(list.size(), list);
		}
	}
	
	
	/**
	 * 获取存在内容的OID
	 * @param oidList
	 * @return
	 * @throws WTException
	 */
	public static ArrayList getDownLoadDoc(ArrayList oidList) throws WTException{
//		SessionServerHelper.manager.setAccessEnforced(false);
		DmDocumentManagerDelegate docman = new DmDocumentManagerDelegate();
		ArrayList resultList=new ArrayList();
		
//		HashMap<String,ApplicationData> hashMap=new HashMap<String,ApplicationData>();
		for(int i=0;i<oidList.size();i++){
			com.ptc.netmarkets.model.NmOid nmOid = (com.ptc.netmarkets.model.NmOid)oidList.get(i);
			String oid = (String)nmOid.toString();
			Object docObj=Util.getObjByOid(oid);
			Debug.P("===>docObj:"+docObj);
			
			if(docObj instanceof WTDocument){
				WTDocument doc=(WTDocument)docObj;
				IBAUtil ibaUtil = new IBAUtil(doc);
				String productIID = ibaUtil.getIBAValue(IBAConstants.PRODUCTIID);
				String versionIID = ibaUtil.getIBAValue(IBAConstants.VERSIONIID);
				Debug.P("===>productIID:"+productIID);
				Debug.P("===>versionIID:"+versionIID);
				
//				String documentIID = ibaUtil.getIBAValue(IBAConstants.DOCUMENTIID);
//				DmDocument dmDocument = docman.getDocument(productIID, documentIID, "");
				
				// 获取当前WTDocument对应的Avidm文档版本
//				DmVersion designVersion = docman.getVersion(productIID, versionIID, null);
				
				List fileList=docman.getVersionFileList(productIID, versionIID, null);
				
				for (int k = 0; k < fileList.size(); k++) {
					Object obj = fileList.get(k);
					if (obj instanceof DmFile) {
						DmFile tempFile = (DmFile) obj;
						resultList.add(tempFile);
					}
				}
				
				
				
//				ApplicationData applicationdata = (ApplicationData) ContentHelper.service.getPrimaryContent(ObjectReference.newObjectReference(doc));
//				if(applicationdata!=null){
//					hashMap.put(doc.getNumber(), applicationdata);
//				}
			}
			Debug.P("===>resultList:"+resultList);
		}
		return resultList;
	}	
	
	
	
	public static String getSubJsonArray(String node,String oid) throws Exception{
		 if (!RemoteMethodServer.ServerFlag) {
             return (String) RemoteMethodServer.getDefault().invoke("getSubJsonArray", CPartService.class.getName(),
                     null, new Class[] { String.class,String.class}, 
                     new Object[] {node,oid});
         } else {
        	 
        	String userName=SessionHelper.manager.getPrincipal().getName();
        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false); 
        	Debug.P("==>userName:"+userName+"node:"+node+"===id:"+oid);
        	
      		URLFactory uf = new URLFactory();
      		String hostURL = uf.getBaseHREF();
      		
      		JSONArray returnArray=new JSONArray();
      		if(node.equals("root")){
      			//如果是从父页面选择过来的 ，ID非空
      			if(oid==null||oid.equals("null")){
      				ResultSet rs1 = excuteQueryForSearch("select * from SelectedPDMLinkProductInfo where userName='"+userName+"'");
      				while(rs1.next()){
      					JSONObject product=getPDJsObj(hostURL,rs1.getString("pdID"),rs1.getString("pdXH"),rs1.getString("pdName"));
      					returnArray.put(product);
      				}
      				return returnArray.toString();
      			}else{
      				dbUtil = new DBConnectionUtil();
      				//删除所有记录
      				String sql="delete from SelectedPDMLinkProductInfo where userName='"+userName+"'";
      				dbUtil.executeQuery(sql);
      				
      				SAProductCrtMgrCaller caller = new SAProductCrtMgrCaller();
          			String result[]=oid.split(";");
          			for(int i=0;i<result.length;i++){
          				String siid=result[i];
          				Debug.P("===>siid:"+siid);
          				if("".equals(siid)){
          					continue;
          				}
          				ProductModel  m = caller.getProduct(siid);
          				
          				JSONObject product=getPDJsObj(hostURL,m.sIID,m.sId,m.sPName);
          				if(product.length()!=0){
          					saveSelecedInfo(m.sIID,m.sPName,m.sId,userName);
              				returnArray.put(product);
          				}
          			}//end for
          			
          			dbUtil.commit();
          			dbUtil.close();
          			return returnArray.toString();
      			}
      		}else{
      			WTPart part=(WTPart)Util.getObjByOid(node);
      			QueryResult qr = WTPartHelper.service.getUsesWTParts(part, new LatestConfigSpec());
      			Debug.P("=======part:"+part.getNumber()+"==qr.size()"+qr.size());
      			while(qr.hasMoreElements()){
      				Persistable[] obj = (Persistable[]) qr.nextElement();
      	            WTPart childPart = (WTPart) obj[1];
		  	        String type=getPartType(childPart);
		            Debug.P("===>type:"+type);
		            if(type.equalsIgnoreCase(IBATYPE_STANDARDPART)){
		                 continue;
		            }
      	            
      	            JSONObject jsObj=getTreeJsonNode(hostURL,childPart,type);
      	            jsObj.put("leaf",!getSubPart(childPart));
      	            jsObj.put("icon","images/part.gif");
      	            returnArray.put(jsObj);
      			}//end while
      		}//end if
      		SessionServerHelper.manager.setAccessEnforced(enforce);
      		return returnArray.toString();
         }
	}
	
	public static JSONObject getPDJsObj(String hostURL,String id,String xdh,String name) throws Exception{
	    	JSONObject product=new JSONObject();
			WTContainer container=AvidmUtil.getContainerByProductiid(id);
			if(container instanceof PDMLinkProduct){
				 product.put("id", xdh);
				PDMLinkProduct pdmProduct = (PDMLinkProduct)container;
				Debug.P("===>pdmProduct:"+pdmProduct);
				JSONArray sub= new JSONArray();
				if(pdmProduct!=null){
					sub=getPartByContainer(pdmProduct,hostURL);
				}
				if(sub.length()==0){
					product.put("leaf", true);
				}else{
					product.put("leaf", false);
					product.put("children",sub);
				}
				product.put("text", xdh+SPLIT_CHAR+name);
				product.put("icon","images/folder_navigator.gif");
				product.put("url", hostURL + "sysadm/productType/productAttribute.jsp?sign=1&&sIID=" +id );
			}
			return product;
	}
	
	public static void saveSelecedInfo(String pdID,String pdName,String xdh,String userName) throws Exception{
		ResultSet rs1 = excuteQueryForSearch("select SEQ_SELECTEDPDINFO.nextval from dual");
		int num=0;
		if(rs1.next())
			num=rs1.getInt(1);
		
		String sql = "INSERT INTO SelectedPDMLinkProductInfo VALUES ('"+num+"','"+pdID+"','"+pdName+"','"+xdh+"','"+userName+"')";
		Debug.P("===>sql:"+sql);
		dbUtil.executeQuery(sql);
	}
	
	/**
	 * 定义树上零部件的显示值
	 * @param hostURL
	 * @param part
	 * @param type
	 * @return
	 * @throws JSONException
	 * @throws WTException
	 */
	public static JSONObject getTreeJsonNode(String hostURL,WTPart part,String type) throws JSONException, WTException{
		JSONObject obj=new JSONObject();
		String partOid=Util.getOid(part);
		
		obj.put("id", partOid);
		String value=subTypeMap.get(type);
		
		Debug.P("===&&&&&&&&>type:"+type+"===value:"+value);
		String showName="";
		if(value!=null){
			showName=part.getNumber()+SPLIT_CHAR+part.getName()+SPLIT_CHAR+value;
		}else{
			showName=part.getNumber()+SPLIT_CHAR+part.getName();
		}
		IBAUtility partUtility = new IBAUtility(part);
		String phase=StringUtils.defaultIfEmpty(partUtility.getIBAValue(IBA_PHASE), ""); 
		if(!"".equals(phase)){
			showName=showName+SPLIT_CHAR+phase;
		}
		obj.put("text", showName);
		obj.put("url", hostURL + "servlet/TypeBasedIncludeServlet?u8=1&oid=" + partOid );
		
		return obj;
	}
	
	public static String getPartType(WTPart part) throws RemoteException, WTException{
		String docType = ClientTypedUtility.getExternalTypeIdentifier(part);
		Debug.P("docType-------------->" + docType);
		docType=docType.substring(docType.lastIndexOf(".")+1, docType.length());
		Debug.P("docType-------------->" + docType);
		
		return docType;
	}
	

	/**
	 * 判断其是否存在子节点（排除掉标准件)
	 * @param part
	 * @return
	 * @throws WTException
	 * @throws RemoteException
	 */
	public static Boolean getSubPart(WTPart part) throws WTException, RemoteException{
		QueryResult qr = WTPartHelper.service.getUsesWTParts(part, new LatestConfigSpec());
		Debug.P("=======part:"+part.getNumber()+"==qr.size()"+qr.size());
		boolean exsit=false;
		while(qr.hasMoreElements()){
			Persistable[] obj = (Persistable[]) qr.nextElement();
            WTPart childPart = (WTPart) obj[1];
            String type=getPartType(childPart);
            Debug.P("===>type:"+type);
            if(!type.equalsIgnoreCase(IBATYPE_STANDARDPART)){
            	exsit=true;
            	break;
            }
		}
		return exsit;
	}
	
	
	
	public static JSONArray getSubTreeNode(String xdh,String hostURL) throws RemoteException, InvocationTargetException, WTException, JSONException{
            JSONArray jsonArray = new JSONArray();
           
        	 WTPart mPart=getPartByNumber(xdh+"-M", false);
        	 Debug.P("===>mPart:"+mPart);
			 if(mPart!=null){
				JSONObject jsObj=getXDHPartJsonNode(hostURL,mPart);
				jsObj.put("leaf",!getSubPart(mPart));
				jsonArray.put(jsObj);
			}
			 
			 WTPart cPart=getPartByNumber(xdh+"-C", false);
			 Debug.P("===>cPart:"+cPart);
			 if(cPart!=null){
				JSONObject jsObj=getXDHPartJsonNode(hostURL,cPart);
				jsObj.put("leaf",!getSubPart(cPart));
				jsonArray.put(jsObj);
			}
			 
			 WTPart cjPart=getPartByNumber(xdh+"-C-J", false);
			 Debug.P("===>cjPart:"+cjPart);
			 if(cjPart!=null){
				JSONObject jsObj=getXDHPartJsonNode(hostURL,cjPart);
				jsObj.put("leaf",!getSubPart(cjPart));
				jsonArray.put(jsObj);
			}
			 
			 WTPart zPart=getPartByNumber(xdh+"-Z", false);
			 Debug.P("===>zPart:"+zPart);
			 if(zPart!=null){
				JSONObject jsObj=getXDHPartJsonNode(hostURL,zPart);
				jsObj.put("leaf",!getSubPart(zPart));
				jsonArray.put(jsObj);
			}
			
			return 	jsonArray;
	}
	
	public static JSONObject getXDHPartJsonNode(String hostURL,WTPart part) throws JSONException{
		JSONObject obj=new JSONObject();
		
		String partOid=Util.getOid(part);
		obj.put("id", partOid);
		obj.put("text", part.getNumber()+SPLIT_CHAR+part.getName());
		obj.put("url", hostURL + "servlet/TypeBasedIncludeServlet?u8=1&oid=" + partOid );
		
		return obj;
	}
	
	public static WTPart getPartByNumber(String number, boolean accessControlled) throws WTException {
        WTPart part = null;
        number = number.toUpperCase();
        WTPartMaster partMaster = null;
        QuerySpec querySpec = new QuerySpec(WTPartMaster.class);
        SearchCondition searchCondition = new SearchCondition(WTPartMaster.class, WTPartMaster.NUMBER,
                SearchCondition.EQUAL, number, false);
        querySpec.appendSearchCondition(searchCondition);
        QueryResult queryResult = PersistenceHelper.manager.find(querySpec);
        while (queryResult.hasMoreElements()) {
            partMaster = (WTPartMaster) queryResult.nextElement();
            QueryResult queryResult2 = VersionControlHelper.service.allVersionsOf(partMaster);
            if (queryResult2.hasMoreElements()) {
                part = (WTPart) queryResult2.nextElement();
            }
        }
        return part;
    }
	
	public static void getPartByNumber(String number)throws Exception{
		QuerySpec qs = new QuerySpec(WTPart.class);

		SearchCondition where = VersionControlHelper.getSearchCondition(WTPart.class, true);
		qs.appendWhere(where);
		
		// 成品条件
		where = new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL,number);
		qs.appendAnd();
		qs.appendWhere(where);
		
		Debug.P("===>qs:"+qs);
		
		// 执行查询
		QueryResult qr = PersistenceHelper.manager.find(qs);

		// 获取最新大版本
		qr = GenericUtil.getLatestRevisionForEachView(qr);

		Debug.P("===>qr.size():"+qr.size());
		while(qr.hasMoreElements()){
//			Persistable[] ps = (Persistable[])qr.nextElement();
			WTPart part = (WTPart)qr.nextElement();
			Debug.P("===>partVersion:"+part.getNumber()+"==>partVersion:"+part.getVersionIdentifier().getValue());
		}
	}
	
	public static JSONArray getPartByContainer(PDMLinkProduct product,String hostURL) throws Exception{
		JSONArray jsonArray = new JSONArray();
		
		QuerySpec qs = new QuerySpec(WTPart.class);
		
		WhereExpression where = WTContainerHelper.getWhereContainerIs(product);
		qs.appendWhere(where);
		
		 qs.appendAnd();
		where = VersionControlHelper.getSearchCondition(WTPart.class, true);
		qs.appendWhere(where);
		
		// 成品条件
		where = new SearchCondition(WTPart.class, WTPart.END_ITEM, SearchCondition.IS_TRUE);
		qs.appendAnd();
		qs.appendWhere(where);
		
		Debug.P("===>qs:"+qs);
		
		// 执行查询
		QueryResult qr = PersistenceHelper.manager.find(qs);

		// 获取最新大版本
		qr = GenericUtil.getLatestRevisionForEachView(qr);

		Debug.P("===>qr.size():"+qr.size());
		while(qr.hasMoreElements()){
//			Persistable[] ps = (Persistable[])qr.nextElement();
			WTPart part = (WTPart)qr.nextElement();
			if(!getPartType(part).equals(IBATYPE_MODELPART)){
				continue;
			}
			Debug.P("===>partVersion:"+part.getNumber()+"==>partVersion:"+part.getVersionIdentifier().getValue());
			JSONObject jsObj=getXDHPartJsonNode(hostURL,part);
			jsObj.put("leaf",!getSubPart(part));
			jsObj.put("icon","images/prod.gif");
			
			jsonArray.put(jsObj);
		}
		return jsonArray;
	}
	
	public static void excuteQuery(String sql) throws Exception{
		try {
			dbUtil = new DBConnectionUtil();
			dbUtil.executeQuery(sql);
			dbUtil.commit();
		} finally {
			try {
				dbUtil.close();
			} catch (SQLException e) {
				 e.printStackTrace();
			}
		}
	}
	
	public static ResultSet excuteQueryForSearch(String sql) throws Exception{
		ResultSet rs = null;
		dbUtil = new DBConnectionUtil();
		rs = dbUtil.executeQuery(sql);
		return rs;
	}
	
	public void commit() throws SQLException {
		if (dbUtil != null) {
			dbUtil.commit();
		}
	}
	
	public static void main(String[] args) throws Exception{
		getPartByNumber("AVP_TEST-C");
	}
}
