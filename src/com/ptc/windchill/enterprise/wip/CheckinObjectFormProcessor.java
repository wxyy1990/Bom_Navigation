/* bcwti
 *
 * Copyright (c) 2008 Parametric Technology Corporation (PTC). All Rights Reserved.
 *
 * This software is the confidential and proprietary information of PTC
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 * ecwti
 */
package com.ptc.windchill.enterprise.wip;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import wt.enterprise.Templateable;
import wt.enterprise.TemplatesUtility;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceServerHelper;
import wt.fc.ReferenceFactory;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.pom.PersistenceException;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.VersionControlHelper;
import wt.vc.VersionReference;
import wt.vc.wip.WorkInProgressException;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.DynamicRefreshInfo;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.FormResultAction;
import com.ptc.netmarkets.model.NmObjectHelper;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.misc.NmContext;

import ext.c504.part.Debug;
import ext.c504.part.Util;

public class CheckinObjectFormProcessor extends DefaultObjectFormProcessor
		implements Serializable {
	
	protected DynamicRefreshInfo di;
	private static final boolean IS_DTI_CHECKIN;
	static {
        try {
            wt.util.WTProperties properties = wt.util.WTProperties.getLocalProperties();
            IS_DTI_CHECKIN = properties.getProperty("wt.doc.msoi.checkinAfterDTIUpload", true);
        } catch (Throwable t) {
            System.err.println("Error initializing " + CheckinObjectFormProcessor.class.getName());
            t.printStackTrace(System.err);
            throw new ExceptionInInitializerError(t);
        }

	}
	private static Logger logger = LogR
			.getLogger(CheckinObjectFormProcessor.class.getName());

	public FormResult preProcess(NmCommandBean clientData,
			List<ObjectBean> objectList) throws WTException {
		logger.debug("ENTER : CheckinObjectFormProcessor.preProcess()");
		FormResult result = new FormResult();
		for (ObjectBean objectBean : objectList) {
			NmOid nmoid = clientData.getActionOid();
			Object obj = nmoid.getRef();
			objectBean.setObject(obj);
		}
		result.setStatus(FormProcessingStatus.SUCCESS);
		// Call super(), which will call registered processor delegates
		FormResult superResult = super.preProcess(clientData, objectList);
		result = mergeIntermediateResult(superResult, result);
		if (!continueProcessing(result))
			return result;
		logger.debug("EXIT : CheckinObjectFormProcessor.preProcess()");
		return result;

	}

	public FormResult doOperation(NmCommandBean clientData,
			List<ObjectBean> objectList) throws WTException {
		logger.debug("ENTER : CheckinObjectFormProcessor.doOperation()");
		FormResult superResult = new FormResult();
		FormResult phaseResult = new FormResult();
		phaseResult.setStatus(FormProcessingStatus.SUCCESS);
		Workable workingObj = null;
		Workable workingCopy = null;
		if (objectList.size() > 0) {
			NmOid nmOid = clientData.getActionOid();
			if (nmOid.isA(Workable.class)) {
				Workable origWorkable = (Workable) nmOid.getRef();
				// if the object is checked out to the user, but we don't have
				// the working copy, get the working copy.
				if (WorkInProgressHelper.isCheckedOut(origWorkable,
						SessionHelper.getPrincipal())
						&& !WorkInProgressHelper.isWorkingCopy(origWorkable)) {
					// get working copy
					Workable newWorkable = WorkInProgressHelper.service
							.workingCopyOf(origWorkable);
					for (ObjectBean objectBean : objectList) {
						Object obj = objectBean.getObject();
						// obj =
						// PersistenceHelper.manager.refresh((Persistable)newWorkable);
						if (WorkInProgressHelper.isWorkingCopy((Workable) obj) == false)
							obj = WorkInProgressHelper.service
									.workingCopyOf((Workable) objectBean
											.getObject());
						objectBean.setObject(obj);
						workingCopy = (Workable)obj;
					}
				}
			}
			superResult = super.doOperation(clientData, objectList);
			// Do Object checkin
			String comment = clientData.getTextParameter("comment");
			
	        //Check whether request is coming from DTI
	        String userAgent = clientData.getTextParameter("ua");
	        logger.debug("***userAgent: " + userAgent);

	        boolean isDTI = false;

	        if(userAgent != null && userAgent.equals("DTI"))
	            isDTI=true;
	        if(!isDTI){
	        	String wizardResponseHandler = clientData.getTextParameter("wizardResponseHandler");
	        	if(wizardResponseHandler != null && wizardResponseHandler.indexOf("ua=DTI")>=0){
	        		isDTI=true;
	        	}
	        }

	        if(IS_DTI_CHECKIN && isDTI){
	        	/**
	        	 * If the checkin wizard is invoked from DTI, we will save the checkin comment on the working copy and will not perform the actual checkin now.
	        	 * Checkin will be done after the content is uploaded and attached to this iteration.
	        	 */
	        	logger.debug("isDTI & wt.doc.msoi.checkinAfterDTIUpload are true");
	        	
	        	if(workingCopy != null){
	        		try {
	        			VersionControlHelper.setNote(workingCopy, comment);
	        			PersistenceServerHelper.manager.update(workingCopy);
	        			NmOid newOid = new NmOid(workingObj);
	        			phaseResult = mergeIntermediateResult(superResult, phaseResult);
	        			return phaseResult;

	        		} catch (WTPropertyVetoException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		}
	        	}
	        }
			
			workingObj = objectServiceTransactionCheckIn(clientData, comment,
					null, null, null);
			
			if (workingObj != null) {
				NmOid newOid = new NmOid(workingObj);
				di = new DynamicRefreshInfo(newOid, nmOid,
						NmCommandBean.DYNAMIC_UPD);
			}
		}

		phaseResult = mergeIntermediateResult(superResult, phaseResult);

		// For checkin action in Document Structure Browser
		boolean forDSB = Boolean.parseBoolean(clientData
				.getTextParameter("forDSB"));
		if (forDSB) {
			String oldOid = clientData.getActionOid().getReferenceString();
			VersionReference ref = VersionReference
					.newVersionReference(workingObj);
			ReferenceFactory rf = new ReferenceFactory();
			String newOid = rf.getReferenceString(ref);
			phaseResult.setJavascript("javascript:RefreshDSB(\"" + oldOid
					+ "\",\"" + newOid + "\",\"" + phaseResult.getStatus().id()
					+ "\")");
		}

		logger.debug("EXIT : CheckinObjectFormProcessor.doOperation()");
		if (!continueProcessing(phaseResult))
			return phaseResult;

		return phaseResult;

	}

   /**
     * Calls the postTransactionProcess() methods of the wizard's
     * ObjectFormProcessorDelegates Delegates are called in no particular order.
     *
     * <BR>
     * <BR>
     * <B>Supported API: </B>true
     *
     * @param clientData -
     *                Contains all the request form data and other wizard
     *                context information. Input.
     * @param objectBeans -
     *                Contain the form data for each target object of the
     *                wizard. One bean per object. Input.
     * @return FormResult - the result of this method
     * @exception wt.util.WTException
     */
   @Override
   public FormResult postTransactionProcess(NmCommandBean clientData, List<ObjectBean> objectBeans) throws WTException {

      Object object = clientData.getActionOid().getRef();
      if (object instanceof WTPart) {
         Properties properties = (Properties)clientData.getMap().get(NmObjectHelper.CGI_DATA);
         wt.part.PSBCacheHelper.service.refreshPSBCache(properties, ((WTPart)object).getMaster());
      }

      return super.processDelegates(POST_TRANSACTION_PROCESS, clientData, objectBeans);

   }

	@Override
	public FormResult setResultNextAction(FormResult result, NmCommandBean cb,
			List<ObjectBean> objectList) throws WTException {
//		Debug.P(((String[])cb.getParameterMap().get("openerExecuteLocation"))[0]);
        //Check whether request is coming from DTI
        String userAgent = cb.getTextParameter("ua");
        logger.debug("***userAgent: " + userAgent);

        boolean isDTI = false;

        if(userAgent != null && userAgent.equals("DTI"))
            isDTI=true;
        if(!isDTI){
        	String wizardResponseHandler = cb.getTextParameter("wizardResponseHandler");
        	if(wizardResponseHandler != null && wizardResponseHandler.indexOf("ua=DTI")>=0){
        		isDTI=true;
        	}
        }
        if(isDTI){

            //Get the value for Keep Checked Out check box
            String checkOutFlag = cb.getTextParameter("checkOut");
            logger.debug("setResultNextAction :checkOut="+checkOutFlag);
            checkOutFlag = (checkOutFlag != null) ? checkOutFlag : "";
            String wizardResponseHandler = null;
            if (cb.getParameterMap().get("wizardResponseHandler") instanceof String[]) {
                wizardResponseHandler = ((String[])cb.getParameterMap().get("wizardResponseHandler"))[0];
            }
            else if (cb.getParameterMap().get("wizardResponseHandler") instanceof String) {
                wizardResponseHandler = (String)cb.getParameterMap().get("wizardResponseHandler");
            }
            
               // In case of DTI...send this value to DTIActionServlet
                if(wizardResponseHandler != null && !wizardResponseHandler.equals("")) {
                    if (wizardResponseHandler.contains("?")) {
                        if("checkOut".equals(checkOutFlag)){
                            wizardResponseHandler += "&keepCheckedOut=true";
                        }
                        if(IS_DTI_CHECKIN)
                        	wizardResponseHandler += "&dtiCheckin=true";
                        
                    }
                    
                    result.setForcedUrl(wizardResponseHandler);
                }
            
        }
        result= WIPHelper.wipSetResultNextAction(result, cb, objectList, di);		
        
        Object object = cb.getActionOid().getRef();
        Debug.P(object);
        if (object instanceof WTPart) {
        	WTPart part=(WTPart)object;
        	if(WorkInProgressHelper.isWorkingCopy(part)==false){
        		result.setNextAction(FormResultAction.REFRESH_OPENER);
        	}
        }
        
			
		return result;
	}

	private Workable objectServiceTransactionCheckIn(NmCommandBean cb,
			String comment, String file, String tempFile, String enabled)
			throws WTException {
		logger
				.debug("ENTER : CheckinObjectFormProcessor.objectServiceTransactionCheckIn()");
		// rbhaumik: null pointer exceptions for nmOid can be avoided on info
		// page of objects by referencing cb.getActionOid(bug fix)
		NmOid nmOid = cb.getActionOid();
		Persistable persistObj = ObjectReference.newObjectReference(nmOid.getOidObject()).getObject();			
		Workable workingObj = null;

		if (!WorkInProgressHelper.isWorkingCopy((Workable) persistObj)) {
			workingObj = WorkInProgressHelper.service
					.workingCopyOf((Workable) persistObj);
		} else {
			workingObj = (Workable) persistObj;
		}
		PersistenceServerHelper.manager.lock((Persistable) workingObj);
		if (WorkInProgressHelper.isWorkingCopy(workingObj)) {
			try {

				boolean enable = false;

				HashMap checkBoxMap = cb.getChecked();
				if (checkBoxMap.size() > 0) {
					Iterator keys = checkBoxMap.keySet().iterator();
					while (keys.hasNext()) {
						String key = (String) keys.next();
						logger
								.debug("Checkin - key in the CheckBoxMap: "
										+ key);
						if (key.equalsIgnoreCase("enable")) {
							enable = true;
							break;
						}
					}
				}

				if (workingObj instanceof Templateable) {
					if (enable) {
						TemplatesUtility.setEnabledonTemplateable(workingObj,
								enable);
					}
				}
				
				

				workingObj = WorkInProgressHelper.service.checkin(workingObj,
						comment);

				if (workingObj instanceof WTPart) {
					wt.representation.RepresentationHelper.service
							.emitReadyToPublishEvent(workingObj);
				}

				if (workingObj instanceof Templateable) {
					TemplatesUtility
							.disablePriorIterations((Templateable) workingObj);
				}

			} catch (WTPropertyVetoException wtpve) {
				throw new WTException(wtpve);
			} catch (WorkInProgressException wipe) {
				throw new WTException(wipe);
			} catch (PersistenceException pe) {
				throw new WTException(pe);
			} catch (WTException wte) {
				throw wte;
			}
		}
		String requestCheckOutFlag = cb.getTextParameter("checkOut");
		

        String userAgent = cb.getTextParameter("ua");
        logger.debug("***userAgent: " + userAgent);

        boolean isDTI = false;
        if(userAgent != null && userAgent.equals("DTI"))
            isDTI=true;
        if(!isDTI){
        	String wizardResponseHandler = cb.getTextParameter("wizardResponseHandler");
        	if(wizardResponseHandler != null && wizardResponseHandler.indexOf("ua=DTI")>=0){
        		isDTI=true;
        	}
        }

        String keepcheckedout = (requestCheckOutFlag != null) ? requestCheckOutFlag : "";

        if (!isDTI && keepcheckedout.equalsIgnoreCase("checkOut")) {
            try {
                NmOid nmoid1 = new NmOid("object", workingObj.getPersistInfo()
                        .getObjectIdentifier());
                logger.debug("Perform Checkout...");
                WIPHelper.doCheckOut(nmoid1);
            } catch (WTException wte) {
                wte.printStackTrace();
            }

		
		}
		logger
				.debug("EXIT : CheckinObjectFormProcessor.objectServiceTransactionCheckIn()");
		return workingObj;
	}

}