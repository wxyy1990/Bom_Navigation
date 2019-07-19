package ext.c504.part;

/*
 * PersistenceHelper.manager.getNextSequence("ManagedBaselineIdentity_seq")
 */
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.PropertyResourceBundle;
import java.util.Vector;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentServerHelper;
import wt.content.FormatContentHolder;
import wt.doc.WTDocument;
import wt.fc.Identified;
import wt.fc.IdentityHelper;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.inf.container.WTContainer;
import wt.inf.library.WTLibrary;
import wt.inf.team.ContainerTeam;
import wt.lifecycle.LifeCycleException;
import wt.lifecycle.LifeCycleHistory;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.maturity.PromotionNotice;
import wt.maturity.PromotionSeed;
import wt.maturity.PromotionTarget;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.part.WTPartConfigSpec;
import wt.part.WTPartMasterIdentity;
import wt.part.WTPartStandardConfigSpec;
import wt.part.WTPartUsageLink;
import wt.pdmlink.PDMLinkProduct;
import wt.pds.StatementSpec;
import wt.pom.PersistenceException;
import wt.project.Role;
import wt.projmgmt.admin.Project2;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.util.CollationKeyFactory;
import wt.util.SortedEnumeration;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.vc.Iterated;
import wt.vc.Mastered;
import wt.vc.VersionControlHelper;
import wt.vc.VersionIdentifier;
import wt.vc.VersionInfo;
import wt.vc.Versioned;
import wt.vc.config.LatestConfigSpec;
import wt.vc.views.ViewHelper;
import wt.vc.wip.CheckoutInfo;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.WorkInProgressState;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;

public class Util implements wt.method.RemoteAccess{

	public static boolean VERBOSE = true;

	/*
	 * @param propKey ����keyֵ
	 *  @param proppertyFile �����ļ�λ�� �� ext.properties.properties
	 *  return ָ��������ֵ��
	 */
	public static String getPropertyValue(String propKey, String propertyFile) {
        String value="";
        try {
            PropertyResourceBundle prBundle=(PropertyResourceBundle)PropertyResourceBundle.getBundle(propertyFile);
            byte[] tempvalue = null;
		//test branch asd
            tempvalue = propKey.getBytes("GB2312");
            propKey = new String(tempvalue,"ISO-8859-1");
            tempvalue = prBundle.getString(propKey).getBytes("ISO-8859-1");
            value = new String(tempvalue,"GB2312");
            value.trim();
        } catch(Exception e) {
            if(VERBOSE)Debug.P(e);
        }
        if(VERBOSE)Debug.P("the value is : " + value );
        return value;
    }

	public static String getRootPath(){
        String dbPath = "";
        try {
              WTProperties properties = WTProperties.getLocalProperties();
              dbPath = properties.getProperty("WNC/Windchill.dir");
        }catch(Exception exp)
        {
              exp.printStackTrace();
        }
         return dbPath;
    }

	public static Object getBVByMaster(Mastered m) throws PersistenceException, WTException{
		Object obj = null;
		QueryResult qr = wt.vc.VersionControlHelper.service.allIterationsOf(m);
		qr = new LatestConfigSpec().process(qr);
		if (qr.hasMoreElements())
			obj =  qr.nextElement();
		return obj;

	}
	/*
	 * ����Number ��ȡ��Ӧ��Mastered
	 * number	������
	 * masterClass	Master������
	 * 		�����Ǿ����Master��WTPartMaster��WTDocumentMaster��MPMProcessPlanMaster��MPMOperationMaster�� etc
	 */
	public static Mastered getMaster(String number,Class masterClass) throws WTException{
		Object obj =null ;
		QuerySpec qs = new QuerySpec(masterClass);
		SearchCondition sc = new SearchCondition(masterClass,
				"number", SearchCondition.EQUAL, number);
		qs.appendWhere(sc, new int[] { 0 });
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		while(qr.hasMoreElements()){
			obj=qr.nextElement();
		}
		return  (Mastered)obj;
}

	public static WTObject getObjByOid(String oid) throws WTException{
		ReferenceFactory referencefactory = new ReferenceFactory();
	    WTObject obj = (WTObject) referencefactory.getReference(oid).getObject();
		return obj;
	}
	public static Object getObj(String number,Class masterClass) throws WTException{
		Object obj =null ;
		QuerySpec qs = new QuerySpec(masterClass);
		SearchCondition sc = null;
		try{
			sc = new SearchCondition(masterClass,
				"master>number", SearchCondition.EQUAL, number);
		}catch(wt.query.QueryException e){
			sc = new SearchCondition(masterClass,
					"number", SearchCondition.EQUAL, number);
		}
		qs.appendWhere(sc, new int[] { 0 });
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		while(qr.hasMoreElements()){
			obj=qr.nextElement();
		}
		return  (Object) obj;
	}
	public static Object getObj(String searchValue,Class masterClass,String searchCondition) throws WTException{
		Object obj =null ;
		QuerySpec qs = new QuerySpec(masterClass);
		SearchCondition sc = null;
		try{
			sc = new SearchCondition(masterClass,
				searchCondition, SearchCondition.EQUAL, searchValue);
		}catch(wt.query.QueryException e){
			e.printStackTrace();
		}
		qs.appendWhere(sc, new int[] { 0 });
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		while(qr.hasMoreElements()){
			obj=qr.nextElement();
		}
		return  (Object) obj;
	}
	public static String getWTProperty(String key){
		String value = "";
		try {
			WTProperties wtp = WTProperties.getLocalProperties();
			value = wtp.getProperty(key);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return value;
	}

	/*
	 * ��ȡ��ǰ��������״̬
	 * @param	obj ��ǰ����
	 * ���ص�ǰ״̬

	 public static String getstate(WTObject obj) throws WTException {
	        String state = "";
	        Phase ph = LifeCycleHelper.service.getCurrentPhase((LifeCycleManaged)obj);
	        Debug.P(ph);
	        state = ph.getName();
	        return state;
	    }
	 */
	    //�õ��������°汾��Ϣ	�磺A.1
	    public static String getbanbenhao(WTObject obj) throws WTException {
	        String version = "";
	        String iterate = "";
	        String banbenhao = "";
	        version = ((Versioned)obj).getVersionIdentifier().getValue();
	        iterate = ((Iterated)obj).getIterationIdentifier().getValue();
	        banbenhao = version + "." + iterate;
	        return banbenhao;
	    }

	    public static String getpname(WTObject wtobj){
			InputStream inputStream = null;
			ContentHolder ch = (ContentHolder) wtobj;
			try {
				ch = ContentHelper.service.getContents(ch);
				ContentItem primary = ContentHelper.service.getPrimary((FormatContentHolder) ch);
				ApplicationData myData = (ApplicationData) primary;
				if(VERBOSE)Debug.P("The primary name is " + myData.getFileName());
				if (myData != null) {
					ContentServerHelper.service.writeContentStream(myData, Util.getWTProperty("wt.temp") + myData.getFileName());
					return myData.getFileName();
		 		}
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			return null;
		}

	    public static State getFrontState(LifeCycleManaged obj){
	    	HashMap hm = new HashMap();
			try {
				QueryResult qr = wt.vc.VersionControlHelper.service.allIterationsOf(((WTPart)obj).getMaster());
				while(qr.hasMoreElements()){
					WTPart obj2 =(WTPart)qr.nextElement();
					Debug.P("The version is :::>>" + obj2.getDisplayIdentifier());
					QueryResult qr2 = wt.lifecycle.LifeCycleHelper.service.getHistory((LifeCycleManaged)obj2);
					while(qr2.hasMoreElements()){
						LifeCycleHistory lch = (LifeCycleHistory)qr2.nextElement();
						if(VERBOSE)
							Debug.P("GET the front state is " +  lch.getPhaseName() +  ";" + lch.getState().toString() + ";" + lch.getModifyTimestamp() + ";;" + lch.getType());

					}


				}


				Debug.P(hm.size());
				Debug.P(hm);

//				LifeCycleHistory lfh = (LifeCycleHistory)qrLife.nextElement();
//				if(VERBOSE)
//					Debug.P("GET the front state is " +  lfh.getPhaseName() +  ";" + lfh.getState().toString() + ";" + lfh.getModifyTimestamp());
				return null;
			} catch (LifeCycleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    	return null;
	    }

	    public static String getOid(WTObject obj){
	    	String oid ="";
	    	wt.fc.ReferenceFactory rf = new ReferenceFactory();
	    	try {
				oid = rf.getReferenceString(obj);
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	return oid;
	    }

	    public static WTPart getWTPartByNumber(String number, String version)
	    throws WTException {
	        WTPart part = null;

	        QuerySpec qs = new QuerySpec(WTPart.class);
	        qs.appendWhere(new SearchCondition(WTPart.class,
	                WTPart.NUMBER,
	                SearchCondition.EQUAL,
	                number.toUpperCase(),
	                false));
	        qs.appendAnd();
	        qs.appendWhere(new SearchCondition(WTPart.class,
//	                Versioned.VERSION_INFO + "." +
//	                VersionInfo.IDENTIFIER + "." +
//	                "versionId"
	        		WTPart.VERSION_IDENTIFIER + "." + VersionIdentifier.VERSIONID,
	                SearchCondition.EQUAL,
	                version,
	                false));

	        QueryResult qr = PersistenceHelper.manager.find(qs);
	        if (qr.size() > 0) {
	            part = (WTPart) qr.nextElement();
	            //if(version == null){
	            part = (WTPart) VersionControlHelper.getLatestIteration(part);
	            //}
	            if(part != null)
	                if (VERBOSE)  System.out.println("the Part is �� " + part.getDisplayIdentity());
	        }
	        if(part == null)
	            if (VERBOSE) System.out.println("û�б��Ϊ:" + number + "�汾Ϊ��" + version + "��WTPart��");

	        return part;

	    }

	    public static WTPart getDesignPartByNumber(String number)
	    throws WTException {
	        WTPart part = null;

	        QuerySpec qs = new QuerySpec(WTPart.class);
	        SearchCondition sc = new SearchCondition(WTPart.class,
	                WTPart.NUMBER,
	                SearchCondition.EQUAL,
	                number.toUpperCase(),
	                false);
	        qs.appendWhere(sc);
	        WTPartConfigSpec configspec = WTPartConfigSpec
			.newWTPartConfigSpec(WTPartStandardConfigSpec
					.newWTPartStandardConfigSpec(ViewHelper.service
							.getView("Design"), null));
//	        configspec.appendSearchCriteria(qs);
	        QueryResult qr = PersistenceHelper.manager.find(qs);
	        qr = configspec.process(qr);
	        if (qr.size() > 0) {
	            part = (WTPart) qr.nextElement();
	            //if(version == null){
	            part = (WTPart) VersionControlHelper.getLatestIteration(part);
	            //}
	            if(part != null)
	                if (VERBOSE)  System.out.println("the Part is �� " + part.getDisplayIdentity());
	        }
	        if(part == null)
	            if (VERBOSE) System.out.println("Design ��ͼ,����Ϊ:" + number + "�Ĳ���.");

	        return part;

	    }

	    //ͨ����Ż�ȡ������ͼ�Ĳ���Oid
	    public static String getMPMPartOidByNumber(String number)
	    throws Exception {
	        WTPart part = null;
	        String oid = "";

	        QuerySpec qs = new QuerySpec(WTPart.class);
	        SearchCondition sc = new SearchCondition(WTPart.class,
	                WTPart.NUMBER,
	                SearchCondition.EQUAL,
	                number.toUpperCase(),
	                false);
	        qs.appendWhere(sc);
	        WTPartConfigSpec configspec = WTPartConfigSpec
			.newWTPartConfigSpec(WTPartStandardConfigSpec
					.newWTPartStandardConfigSpec(ViewHelper.service
							.getView("Manufacturing"), null));
//	        configspec.appendSearchCriteria(qs);
	        QueryResult qr = PersistenceHelper.manager.find(qs);
	        qr = configspec.process(qr);
	        if (qr.size() > 0) {
	            part = (WTPart) qr.nextElement();
	            //if(version == null){
	            part = (WTPart) VersionControlHelper.getLatestIteration(part);
	            //}
	            if(part != null)
	                if (VERBOSE)  System.out.println("the Part is �� " + part.getDisplayIdentity());
	        }
	        if(part == null)
	            if (VERBOSE) System.out.println("Manufacturing ��ͼ,����Ϊ:" + number + "�Ĳ���.");

	        if(part==null){
	        	throw new Exception("δ�ҵ���Ӧ�����첿����");
	        } else {
	        	ReferenceFactory rf = new ReferenceFactory();
	        	oid = rf.getReferenceString(part);
	        }
	        return oid;

	    }



	    //ͨ����Ż�ȡ�����ͼ�Ĳ���Oid
	    public static String getDPartOidByNumber(String number)
	    throws Exception {
	        WTPart part = null;
	        String oid = "";

	        QuerySpec qs = new QuerySpec(WTPart.class);
	        SearchCondition sc = new SearchCondition(WTPart.class,
	                WTPart.NUMBER,
	                SearchCondition.EQUAL,
	                number.toUpperCase(),
	                false);
	        qs.appendWhere(sc);
	        WTPartConfigSpec configspec = WTPartConfigSpec
			.newWTPartConfigSpec(WTPartStandardConfigSpec
					.newWTPartStandardConfigSpec(ViewHelper.service
							.getView("Design"), null));
//	        configspec.appendSearchCriteria(qs);
	        QueryResult qr = PersistenceHelper.manager.find(qs);
	        qr = configspec.process(qr);
	        if (qr.size() > 0) {
	            part = (WTPart) qr.nextElement();
	            //if(version == null){
	            part = (WTPart) VersionControlHelper.getLatestIteration(part);
	            //}
	            if(part != null)
	                if (VERBOSE)  System.out.println("the Part is �� " + part.getDisplayIdentity());
	        }
	        if(part == null)
	            if (VERBOSE) System.out.println("Design ��ͼ,����Ϊ:" + number + "�Ĳ���.");

	        if(part==null){
	        	throw new Exception("δ�ҵ���Ӧ�����첿����");
	        } else {
	        	ReferenceFactory rf = new ReferenceFactory();
	        	oid = rf.getReferenceString(part);
	        }
	        return oid;

	    }




	  //ͨ����Ż�ȡ������ͼ�Ĳ���
	    public static WTPart getMPMPartByNumber(String number)
	    throws Exception {
	        WTPart part = null;

	        QuerySpec qs = new QuerySpec(WTPart.class);
	        SearchCondition sc = new SearchCondition(WTPart.class,
	                WTPart.NUMBER,
	                SearchCondition.EQUAL,
	                number.toUpperCase(),
	                false);
	        qs.appendWhere(sc);
	        WTPartConfigSpec configspec = WTPartConfigSpec
			.newWTPartConfigSpec(WTPartStandardConfigSpec
					.newWTPartStandardConfigSpec(ViewHelper.service
							.getView("Manufacturing"), null));
//	        configspec.appendSearchCriteria(qs);
	        QueryResult qr = PersistenceHelper.manager.find(qs);
	        qr = configspec.process(qr);
	        if (qr.size() > 0) {
	            part = (WTPart) qr.nextElement();
	            //if(version == null){
	            part = (WTPart) VersionControlHelper.getLatestIteration(part);
	            //}
	            if(part != null)
	                if (VERBOSE)  System.out.println("the Part is �� " + part.getDisplayIdentity());
	        }
	        if(part == null)
	            if (VERBOSE) System.out.println("Manufacturing ��ͼ,����Ϊ:" + number + "�Ĳ���.");

	        if(part==null){
	        	throw new Exception("δ�ҵ���Ӧ�����첿����");
	        } else {
	        	return part;
	        }

	    }

	    public static WTDocument getWTDocumentByNumber(String number, String version)
	    throws WTException {
	        WTDocument doc = null;

	        QuerySpec qs = new QuerySpec(WTDocument.class);
	        qs.appendWhere(new SearchCondition(WTDocument.class,
	                WTDocument.NUMBER,
	                SearchCondition.EQUAL,
	                number.toUpperCase(),
	                false));
	        qs.appendAnd();
	        qs.appendWhere(new SearchCondition(WTDocument.class,
	                Versioned.VERSION_INFO + "." +
	                VersionInfo.IDENTIFIER + "." +
	                "versionId",
	                SearchCondition.EQUAL,
	                version,
	                false));

	        QueryResult qr = PersistenceHelper.manager.find(qs);
	        if (qr.size() > 0) {
	            doc = (WTDocument) qr.nextElement();
	            doc = (WTDocument) VersionControlHelper.getLatestIteration(doc);
	            if(doc != null)
	                if (VERBOSE)  System.out.println("the Doc is �� " + doc.getDisplayIdentity());
	        }
	        if(doc == null)
	            if (VERBOSE)  System.out.println("û�б��Ϊ:" + number + "�汾Ϊ��" + version + "��WTDocument��");

	        return doc;

	    }


	    public static WTDocument findDocument(String number, String version,
	            boolean checkedOutOnly) throws Exception {
	        QuerySpec qs = new QuerySpec(WTDocument.class);
	        int index = 0;

	        // ����: ָ�����,//���ڹ���״̬
	        qs.appendWhere(new SearchCondition(WTDocument.class, WTDocument.NUMBER,
	                SearchCondition.EQUAL, number), new int[]{index});
//	        qs.appendAnd();
//	        qs.appendWhere(new SearchCondition(WTDocument.class,
//	                WTDocument.CHECKOUT_INFO + "." + CheckoutInfo.STATE,
//	                SearchCondition.NOT_EQUAL, WorkInProgressState.WORKING),
//	                new int[]{0});

	        // ������ָ����汾
	        if (version != null && !version.trim().equals("")) {
	            String vVersion = version;
	            int posLastDot = vVersion.lastIndexOf(".");
	            if (posLastDot > 0)
	                vVersion = vVersion.substring(0, posLastDot);
	            qs.appendAnd();
	            qs.appendWhere(new SearchCondition(WTDocument.class,
	                    WTDocument.VERSION_IDENTIFIER + "." + VersionIdentifier.VERSIONID,
	                    SearchCondition.EQUAL, vVersion), new int[]{index});
	        }

	        // �������������ù���
	        qs = new LatestConfigSpec().appendSearchCriteria(qs);

	        // ִ�в�ѯ
	        Debug.P("find document query: ", qs);
	        QueryResult qr = PersistenceHelper.manager.find(qs);
	        Debug.P("result size: " + qr.size());
	        if (version == null || version.trim().equals("")) {
	            // �������°汾, δָ���汾ʱ��ֻȡ���´�汾������С�汾
	            qr = new LatestConfigSpec().process(qr);
	            Debug.P("filtered size: " + qr.size());
	        }

	        // �����ѯ���
	        if (qr.size() == 1) {
	            WTDocument doc = (WTDocument) qr.nextElement();

	            if (checkedOutOnly) {
	                WorkInProgressState wipState = doc.getCheckoutInfo().getState();
	                if (!wipState.equals(WorkInProgressState.CHECKED_OUT)) {
	                    Debug.P("checkout state=", doc.getCheckoutInfo().getState());
	                    throw new Exception("ָ�����Ϊ<" + number + ">���ĵ�δ�����.");
	                }

	                WTUser me = (WTUser) SessionHelper.getPrincipal();
	                if (!WorkInProgressHelper.isCheckedOut(doc, me))
	                    throw new Exception("ָ�����Ϊ<" + number
	                            + ">���ĵ����ǵ�ǰ�û�(" + me.getFullName()+ ")���.");
	            }

	            return doc;
	        }
	        else if (qr.size() < 1)
	            throw new Exception("δ�ҵ�ָ����Ű汾Ϊ<" + number + ", "
	                    + version + ">���ĵ�.");
	        else {
	            if (Debug.enabled()) {
	                while (qr.hasMoreElements()) {
	                    WTDocument d = (WTDocument) qr.nextElement();
	                    Debug.P("number=", d.getNumber(),
	                            ", version=", d.getVersionIdentifier().getValue(),
	                            ".", d.getIterationIdentifier().getValue(),
	                            ", checkoutState=",
	                            d.getCheckoutInfo().getState().getDisplay());
	                }
	            }

	            throw new Exception("�ҵ����ָ����Ű汾(" + number + ", " + version
	                    + ")���ĵ�: " + qr.size() + "��");
	        }
	    }

	    public static PromotionNotice findPN(Object obj) throws WTException{
	    	QueryResult qrPn = PersistenceHelper.manager.navigate((Persistable) obj,
                    PromotionSeed.PROMOTION_NOTICE_ROLE, PromotionTarget.class,
                    true);
	    	 // ��ʱ������,ȡ����һ��
	        CollationKeyFactory timeKeyFact = new CollationKeyFactory() {
	            public String getCollationString(Object o) {
	                if (!(o instanceof Persistable) || !PersistenceHelper.isPersistent(o))
	                    return "";
	                return ((Persistable) o).getPersistInfo().getModifyStamp().toString();
	            }
	        };
	        Enumeration enProcs = new SortedEnumeration(qrPn, timeKeyFact,SortedEnumeration.DESCENDING);
	        //��ȡ����
	        WfProcess proc = (WfProcess) enProcs.nextElement();
	        ReferenceFactory rf = new ReferenceFactory();
	        //��ȡPBO
	        Object pbo = proc.getBusinessObjectReference(rf).getObject();
	        return (PromotionNotice)obj;
//	        // ȡ���������еĸ��
//	        Enumeration enSteps = WfEngineHelper.service.getProcessSteps(proc, null);
//	        enSteps = new SortedEnumeration(enSteps, timeKeyFact,
//	                SortedEnumeration.DESCENDING);
	    }
	    public static QueryResult getLinks(Class linkClass,Persistable role,String roleDesc) throws WTException{

//	    	QueryResult qr = PersistenceHelper.manager.find(
//					WTPartUsageLink.class, (Persistable) wtpart,
//					WTPartUsageLink.BUILD_TARGET_ROLE, (Persistable) mastered);

	    	QueryResult qr = PersistenceHelper.manager.navigate(role, roleDesc, linkClass,false);
	    	return qr;
	    }

	    public static boolean isExist(wt.org.WTPrincipal principal ,Role role , WTContainer wtc) throws WTException{
	    	ContainerTeam cTeam = null;
	    	if(wtc instanceof PDMLinkProduct){
	    		PDMLinkProduct procduct = (PDMLinkProduct)wtc;
	    		cTeam = (ContainerTeam) procduct.getContainerTeamReference().getObject();

	    	}else if(wtc instanceof wt.inf.library.WTLibrary){
	    		WTLibrary lib = (WTLibrary)wtc;
	    		cTeam = (ContainerTeam)lib.getContainerTeamReference().getObject();
	    	}else if(wtc instanceof wt.inf.library.WTLibrary){
	    		WTLibrary lib = (WTLibrary)wtc;
	    		cTeam = (ContainerTeam)lib.getContainerTeamReference().getObject();
	    	}else if(wtc instanceof wt.projmgmt.admin.Project2){
	    		Project2 project = (Project2)wtc;
	    		cTeam = (ContainerTeam)project.getContainerTeamReference().getObject();
	    	}
	    	if(cTeam != null){
	    		ArrayList al = cTeam.getAllPrincipalsForTarget(role);
	    		for(int i = 0;i<al.size() ; i++){
	    			WTPrincipal principalTmp = (WTPrincipal) ((WTPrincipalReference)al.get(i)).getObject();
	    			if(principalTmp.equals(principal)){
	    				return true;
	    			}
	    		}
	    	}
	    	return false;

	    }

	    public static Vector sortHashMap(HashMap hm){
	    	Vector v = new Vector();
	    	if(hm == null || hm.size() == 0)return v;
	    	Object[] keys = hm.keySet().toArray();
	    	Arrays.sort(keys);
	    	for(Object key : keys){
	    		v.add(hm.get(key));
	    	}
	    	return v;
	    }

	    public static String toChinadate(String date){
	    	HashMap hm = new HashMap();
	    	hm.put("1", "һ");
	    	hm.put("2", "��");
	    	hm.put("3", "��");
	    	hm.put("4", "��");
	    	hm.put("5", "��");
	    	hm.put("6", "��");
	    	hm.put("7", "��");
	    	hm.put("8", "��");
	    	hm.put("9", "��");
	    	hm.put("0", "��");
	    	Object[] keys = hm.keySet().toArray();
	    	Arrays.sort(keys);
	    	for(Object key : keys){
	    		date = date.replace(key.toString(),  hm.get(key).toString());
	    	}
	    	return date;
	    }
	    public static void reName(Mastered m,String name) throws WTException, WTPropertyVetoException{
//	    	WTPart part = (WTPart)obj;
            Identified identified1 = (Identified)m;
            WTPartMasterIdentity wtpartmasteridentity = (WTPartMasterIdentity)identified1.getIdentificationObject();
//            wtpartmasteridentity.setNumber("xname");
            wtpartmasteridentity.setName(name);
            IdentityHelper.service.changeIdentity(identified1, wtpartmasteridentity);

	    }
	    public static void reNum(Mastered m,String num) throws WTException, WTPropertyVetoException{
//	    	WTPart part = (WTPart)obj;
            Identified identified1 = (Identified)m;
            WTPartMasterIdentity wtpartmasteridentity = (WTPartMasterIdentity)identified1.getIdentificationObject();
//            wtpartmasteridentity.setNumber("xname");
            wtpartmasteridentity.setNumber(num);
            IdentityHelper.service.changeIdentity(identified1, wtpartmasteridentity);

	    }
}
