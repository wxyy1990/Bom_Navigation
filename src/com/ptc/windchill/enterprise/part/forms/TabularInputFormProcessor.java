package com.ptc.windchill.enterprise.part.forms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import wt.fc.ObjectIdentifier;
import wt.fc.ObjectNoLongerExistsException;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.collections.WTHashSet;
import wt.filter.DesignationIdentifier;
import wt.filter.NavigationFilterHelper;
import wt.generic.GenericizableUsageLink;
import wt.log4j.LogR;
import wt.part.PSBCacheHelper;
import wt.part.ReferenceDesignatorSet;
import wt.part.ReferenceDesignatorSetDelegateFactory;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.CreateEditFormProcessorHelper;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormProcessorHelper;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.FormResultAction;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.model.NmSimpleOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.part.dataUtilities.TabularInputUtility;
import com.ptc.windchill.enterprise.util.PartManagementHelper;

public class TabularInputFormProcessor extends DefaultObjectFormProcessor
{
	  private static final Logger LOGGER;
	  private static final String CLASSNAME = TabularInputFormProcessor.class.getName();
	  private static final String RESOURCE = "com.ptc.windchill.enterprise.part.partResource";
	  private static final String TYPE = "part";
	  private static final String OK_BUTTON = "Ok";
	  private static final String APPLY_BUTTON = "Apply";
	  private static final String CANCEL_BUTTON = "Cancel";
	  private String whichTabularInputButton = "Ok";
	  private WTPart fromPart = null;
	  private String linkType = "wt.part.WTPartUsageLink";
	  private List<NmOid> removeList = null;
	  private Collection<ObjectBean> objectBeans;
	  Collection<ObjectBean> objectBeansForAdd;
	  Collection<ObjectBean> objectBeansForUpdate;

	  public FormResult preProcess(NmCommandBean paramNmCommandBean, List<ObjectBean> paramList)
	    throws WTException
	  {
	    LOGGER.debug("ENTER : TabularInputFormProcessor.preProcess()");

	    FormResult localFormResult = new FormResult(FormProcessingStatus.SUCCESS);

	    this.whichTabularInputButton = paramNmCommandBean.getTextParameter("whichTabularInputButton");

	    if (!isCancelButton()) {
	      initialize(paramNmCommandBean);
	    } else {
	      localFormResult.setStatus(FormProcessingStatus.SUCCESS);
	      LOGGER.debug("EXIT : TabularInputFormProcessor.preProcess()");
	      return localFormResult;
	    }

	    this.objectBeans = FormProcessorHelper.getObjectBeans(paramNmCommandBean);
	    this.objectBeansForAdd = new ArrayList();
	    this.objectBeansForUpdate = new ArrayList();

	    TabularInputFormProcessorHelper.splitObjectBeansIntoAddAndUpdate(this.objectBeans, this.objectBeansForAdd, this.objectBeansForUpdate);

	    LOGGER.debug("EXIT : TabularInputFormProcessor.preProcess()");
	    return localFormResult;
	  }

	  public FormResult doOperation(NmCommandBean paramNmCommandBean, List<ObjectBean> paramList)
	    throws WTException
	  {
	    LOGGER.debug("ENTER : TabularInputFormProcessor.doOperation()");
	    FormResult localFormResult1 = new FormResult();

	    if (isCancelButton()) {
	      localFormResult1.setStatus(FormProcessingStatus.SUCCESS);
	      LOGGER.debug("EXIT : TabularInputFormProcessor.doOperation()");
	      return localFormResult1;
	    }

	    if (this.removeList != null)
	    {
	      removeUsageLinks(paramNmCommandBean);
	    }

	    validateNumbers(paramNmCommandBean, "ti_findNumber");
	    validateNumbers(paramNmCommandBean, "ti_lineNumber");
	    validateReferenceDesignators(paramNmCommandBean);

	    TabularInputEditFormProcessor localTabularInputEditFormProcessor = new TabularInputEditFormProcessor(this.objectBeansForUpdate, this.linkType);
	    localTabularInputEditFormProcessor.preProcess(paramNmCommandBean, paramList);
	    FormResult localFormResult2 = localTabularInputEditFormProcessor.doOperation(paramNmCommandBean, paramList);

	    TabularInputCreateFormProcessor localTabularInputCreateFormProcessor = new TabularInputCreateFormProcessor(this.objectBeansForAdd, this.linkType);
	    localTabularInputCreateFormProcessor.preProcess(paramNmCommandBean, paramList);
	    FormResult localFormResult3 = localTabularInputCreateFormProcessor.doOperation(paramNmCommandBean, paramList);
	    updateOptionRules(localTabularInputCreateFormProcessor.added_usageLinks, paramNmCommandBean);
	    localFormResult1 = mergeIntermediateResult(localFormResult3, localFormResult2);

	    LOGGER.debug("EXIT : TabularInputFormProcessor.doOperation()");
	    return localFormResult1;
	  }

	  public FormResult postTransactionProcess(NmCommandBean paramNmCommandBean, List<ObjectBean> paramList)
	    throws WTException
	  {
	    Object localObject = paramNmCommandBean.getActionOid().getRef();
	    if ((localObject instanceof WTPart)) {
	      Properties localProperties = (Properties)paramNmCommandBean.getMap().get("cgi_data_key");
	      PSBCacheHelper.service.refreshPSBCache(localProperties, ((WTPart)localObject).getMaster());
	    }

	    return new FormResult(FormProcessingStatus.SUCCESS);
	  }

	  private void updateOptionRules(List paramList, NmCommandBean paramNmCommandBean)
	    throws WTException
	  {
	    Map<Persistable, Set<DesignationIdentifier>>  localMap = collectOptionRuleData(paramNmCommandBean);

	    updateLinkReferences(paramList, localMap);

	    for (Map.Entry localEntry : localMap.entrySet()) {
	      if ((localEntry.getKey() instanceof WTPartUsageLink)) {
	        NavigationFilterHelper.service.updateOptionsOnUsageLinks((Set)localEntry.getValue(), Collections.singletonList((GenericizableUsageLink)localEntry.getKey()));
	      }
	      else {
	        throw new WTException("ERROR:Map still has unknown object as key.. need to be converted :" + localEntry.getKey());
	      }
	    }

	    LOGGER.debug("Updated Option Rules on UsageLinks.");
	  }

	  private void updateLinkReferences(List paramList, Map<Persistable, Set<DesignationIdentifier>> paramMap)
	  {
	    if ((paramList != null) && (!paramList.isEmpty())) {
	      LOGGER.debug("Number of usage links created : " + paramList.size());
	      for (Iterator localIterator = paramList.iterator(); localIterator.hasNext(); ) { Object localObject = localIterator.next();
	        WTPartUsageLink localWTPartUsageLink = null;
	        if ((localObject instanceof ObjectReference))
	          localWTPartUsageLink = (WTPartUsageLink)((ObjectReference)localObject).getObject();
	        else if ((localObject instanceof WTPartUsageLink))
	          localWTPartUsageLink = (WTPartUsageLink)localObject;
	        else {
	          LOGGER.debug("WARNING:Unknown type of objects in createdLinks List : " + localObject);
	        }
	        if (localWTPartUsageLink != null) {
	          WTPartMaster localWTPartMaster = (WTPartMaster)localWTPartUsageLink.getUses();
	          if (paramMap.containsKey(localWTPartMaster)) {
	            paramMap.put(localWTPartUsageLink, paramMap.get(localWTPartMaster));
	            paramMap.remove(localWTPartMaster);
	          }
	        }
	      }
	      LOGGER.debug("updated created usagelink references in option rule data map.");
	    }
	  }

	  private Map<Persistable, Set<DesignationIdentifier>> collectOptionRuleData(NmCommandBean paramNmCommandBean)
	    throws WTException, ObjectNoLongerExistsException
	  {
	    HashMap localHashMap1 = new HashMap();

	    HashMap localHashMap2 = paramNmCommandBean.getText();
	    for (Iterator localIterator = localHashMap2.entrySet().iterator(); localIterator.hasNext(); ) { Object localObject1 = localIterator.next();
	      String str1 = (String)((Map.Entry)localObject1).getKey();
	      if (str1.startsWith("$HdnBegin$_")) {
	        Persistable localPersistable = getRowObject(str1);
	        Object localObject2 = (Set)localHashMap1.get(localPersistable);
	        if (localObject2 == null) {
	          localObject2 = new HashSet();
	          localHashMap1.put(localPersistable, localObject2);
	        }
	        String str2 = (String)((Map.Entry)localObject1).getValue();
	        DesignationIdentifier localDesignationIdentifier = DesignationIdentifier.toDesignationIdentifier(str2);

	        ((Set)localObject2).add(localDesignationIdentifier);
	      }
	    }
	    LOGGER.debug("Option Rules found for " + localHashMap1.size() + " objects.");
	    return (Map<Persistable, Set<DesignationIdentifier>>)localHashMap1;
	  }

	  private Persistable getRowObject(String paramString) throws WTException, ObjectNoLongerExistsException
	  {
	    paramString = paramString.substring(paramString.indexOf("$HdnBegin$_") + "$HdnBegin$_".length());

	    String str = paramString.substring(0, paramString.indexOf("_$HdnEnd$"));
	    NmSimpleOid localNmSimpleOid = (NmSimpleOid)NmOid.newNmOid(str);
	    setReference(localNmSimpleOid);
	    return (Persistable)localNmSimpleOid.getRef();
	  }

	  private void setReference(NmSimpleOid paramNmSimpleOid) throws WTException, ObjectNoLongerExistsException
	  {
	    String str = paramNmSimpleOid.getInternalName();
	    ObjectIdentifier localObjectIdentifier = ObjectIdentifier.newObjectIdentifier(str);
	    Persistable localPersistable = PersistenceHelper.manager.refresh(localObjectIdentifier);
	    paramNmSimpleOid.setRef(localPersistable);
	  }

	  public FormResult setResultNextAction(FormResult paramFormResult, NmCommandBean paramNmCommandBean, List<ObjectBean> paramList)
	    throws WTException
	  {
	    if (CreateEditFormProcessorHelper.parentIsFolderBrowser(paramNmCommandBean)) {
	      return super.setResultNextAction(paramFormResult, paramNmCommandBean, paramList);
	    }

	    if ((paramFormResult.getStatus() == FormProcessingStatus.SUCCESS) || (paramFormResult.getStatus() == FormProcessingStatus.NON_FATAL_ERROR))
	    {
	      String str;
	      if ((isOkButton()) || (isCancelButton()))
	      {
	        str = determineRedirectURL(paramNmCommandBean);

	        paramFormResult.setNextAction(FormResultAction.JAVASCRIPT);
	        paramFormResult.setJavascript("tabularInputAfterSubmit(\"" + str + "\")");
	      }
	      else if (isApplyButton()) {
	        str = determineRedirectURL(paramNmCommandBean);
	        paramFormResult.setNextAction(FormResultAction.JAVASCRIPT);

	        paramFormResult.setJavascript("tabularInputAfterSubmitApply(\"" + str + "\")");
	      }
	      else {
	        paramFormResult.setNextAction(FormResultAction.NONE);
	      }
	    }
	    else if (paramFormResult.getStatus() == FormProcessingStatus.FAILURE) {
	      paramFormResult.setNextAction(FormResultAction.NONE);
	    }
	    
	    paramFormResult.setNextAction(FormResultAction.REFRESH_OPENER);
	    return paramFormResult;
	  }

	  protected String determineRedirectURL(NmCommandBean paramNmCommandBean)
	    throws WTException
	  {
	    NmOid localNmOid = paramNmCommandBean.getActionOid();
	    String str = PartManagementHelper.getInfoPageURL((Persistable)localNmOid.getRef());
	    return str;
	  }

	  private void removeUsageLinks(NmCommandBean paramNmCommandBean)
	    throws WTException
	  {
	    LOGGER.debug("ENTER : TabularInputFormProcessor.removeUsesLinkTI()");

	    WTHashSet localWTHashSet = new WTHashSet();

	    for (int i = 0; i < this.removeList.size(); i++) {
	      NmOid localNmOid = (NmOid)this.removeList.get(i);

	      if (((localNmOid instanceof NmSimpleOid)) && (localNmOid.getRef() == null)) {
	        setReference((NmSimpleOid)localNmOid);
	      }
	      if (localNmOid.isA(WTPartUsageLink.class)) {
	        WTPartUsageLink localWTPartUsageLink = (WTPartUsageLink)localNmOid.getRef();
	        localWTHashSet.add(localWTPartUsageLink);
	      }
	    }

	    if (localWTHashSet.size() > 0) {
	      PersistenceHelper.manager.delete(localWTHashSet);
	    }
	    LOGGER.debug("EXIT : TabularInputFormProcessor.removeUsageLinks()");
	  }

	  private void initialize(NmCommandBean paramNmCommandBean)
	    throws WTException
	  {
	    this.fromPart = TabularInputFormProcessorHelper.getFromPart(paramNmCommandBean);
	    this.linkType = getLinkType(paramNmCommandBean);

	    this.removeList = paramNmCommandBean.getRemovedItemsByName("tabular_input_edit_structure");

	    if ((this.removeList == null) || (this.removeList.size() == 0))
	      this.removeList = null;
	  }

	  protected void validateNumbers(NmCommandBean paramNmCommandBean, String paramString) throws WTException{
	    HashMap localHashMap = paramNmCommandBean.getText();
	    ArrayList localArrayList = new ArrayList();

	    for (Object localObject1 = localHashMap.keySet().iterator(); ((Iterator)localObject1).hasNext(); ) {
	      String str1 = (String)((Iterator)localObject1).next();

	      if ((str1.indexOf("quick_search") > -1) || 
	        (str1.indexOf("defaultColumn") > -1) || 
	        (TabularInputFormProcessorHelper.isSoftAttribute(str1)))
	        continue;
	      String localObject2 = TabularInputFormProcessorHelper.getColumnName(str1);

	      if (paramString.equals(localObject2))
	      {
	        String str3 = (String)localHashMap.get(str1);
	        localArrayList.add(str3);
	      }
	    }
	    Object localObject2[];
	    if (localArrayList.size() == 0) {
	      return;
	    }

	    String[] localObject1 = new String[localArrayList.size()];

	    for (int i = 0; i < localArrayList.size(); i++) {
	      localObject1[i] = ((String)localArrayList.get(i));
	    }
	    if ("ti_lineNumber".equals(paramString)) {
	      String str2 = identifyDuplicate(localArrayList);

	      if (str2 != null) {
	        localObject2 = new Object[] { str2 };
	        throw new WTException("com.ptc.windchill.enterprise.part.partResource", "95", localObject2);
	      }
	    } else if ("ti_findNumber".equals(paramString))
	    {
	      try
	      {
	        WTPartHelper.service.validateFindNumbers(localObject1);
	      }
	      catch (WTException localWTException)
	      {
	        if ((localWTException.getCause() instanceof WTPropertyVetoException))
	        {
	          localWTException.printStackTrace();
	          throw new WTException(localWTException.getCause().getLocalizedMessage());
	        }

	        localObject2 = new Object[] { identifyDuplicate(localArrayList) };
	        throw new WTException("com.ptc.windchill.enterprise.part.partResource", "94", localObject2);
	      }
	    }
	  }

	  protected void validateReferenceDesignators(NmCommandBean paramNmCommandBean) throws WTException
	  {
	    HashMap localHashMap = paramNmCommandBean.getText();
	    ArrayList localArrayList = new ArrayList();
	    ReferenceDesignatorSetDelegateFactory localReferenceDesignatorSetDelegateFactory = new ReferenceDesignatorSetDelegateFactory();

	    for (Object localObject1 = localHashMap.keySet().iterator(); ((Iterator)localObject1).hasNext(); ) {
	      String localObject2 = (String)((Iterator)localObject1).next();
	      if (TabularInputFormProcessorHelper.isSoftAttribute((String)localObject2))
	        continue;
	      String str1 = TabularInputFormProcessorHelper.getColumnName((String)localObject2);

	      if ((str1 != null) && ("ti_referenceDesignator".equals(str1))) {
	        String str2 = (String)localHashMap.get(localObject2);
	        ReferenceDesignatorSet localReferenceDesignatorSet = localReferenceDesignatorSetDelegateFactory.get(str2);
	        localArrayList.addAll(localReferenceDesignatorSet.getExpandedReferenceDesignators());
	      }
	    }
	    Object localObject2[];
	    Object localObject1;
	    if (localArrayList.size() > 0) {
	      localObject1 = identifyDuplicate(localArrayList);

	      if (localObject1 != null) {
	        localObject2 = new Object[] { localObject1 };
	        throw new WTException("com.ptc.windchill.enterprise.part.partResource", "96", localObject2);
	      }
	    }
	    
	  }

	  protected String identifyDuplicate(ArrayList<String> paramArrayList) {
	    TreeSet localTreeSet = new TreeSet();
	    int i = paramArrayList.size();

	    for (int j = 0; j < i; j++) {
	      String str = (String)paramArrayList.get(j);

	      if (str.length() == 0) {
	        continue;
	      }
	      if (localTreeSet.contains(str)) {
	        return str;
	      }

	      localTreeSet.add(str);
	    }

	    return null;
	  }

	  protected boolean isOkButton() {
	    return "Ok".equalsIgnoreCase(this.whichTabularInputButton);
	  }

	  protected boolean isApplyButton() {
	    return "Apply".equalsIgnoreCase(this.whichTabularInputButton);
	  }

	  protected boolean isCancelButton() {
	    return "Cancel".equalsIgnoreCase(this.whichTabularInputButton);
	  }

	  private static String getLinkType(NmCommandBean paramNmCommandBean) throws WTException
	  {
	    String str = null;

	    HashMap localHashMap = paramNmCommandBean.getRequestData().getParameterMap();
	    if ((localHashMap != null) && (localHashMap.containsKey("createType"))) {
	      Object localObject = localHashMap.get("createType");
	      if ((localObject instanceof String[])) {
	        String[] arrayOfString = (String[])(String[])localObject;
	        str = arrayOfString[0];
	      }
	    }

	    if (str == null) {
	      str = TabularInputUtility.getLinkType(paramNmCommandBean);
	    }

	    return str;
	  }

	  static
	  {
	    try
	    {
	      LOGGER = LogR.getLogger(TabularInputFormProcessor.class.getName());
	    } catch (Exception localException) {
	      throw new ExceptionInInitializerError(localException);
	    }
	  }
	}
