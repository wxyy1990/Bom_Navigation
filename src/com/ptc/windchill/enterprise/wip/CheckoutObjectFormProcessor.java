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
import java.util.List;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

import wt.fc.ObjectIdentifier;
import wt.fc.Persistable;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.vc.wip.Workable;
import wt.part.WTPart;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.DynamicRefreshInfo;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.FormResultAction;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.model.NmObjectHelper;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.c504.part.Debug;

public class CheckoutObjectFormProcessor extends DefaultObjectFormProcessor
		implements Serializable {
	private static Logger logger = LogR
			.getLogger(CheckoutObjectFormProcessor.class.getName());

	protected DynamicRefreshInfo di;

	public FormResult doOperation(NmCommandBean clientData,
			List<ObjectBean> objectList) throws WTException {
		logger.debug("doOperation clientData:" + clientData + " objectList:"
				+ objectList);

		try {
			NmOid oid = clientData.getActionOid();
			NmOid newOid = WIPHelper.doCheckOut(oid);

			di = new DynamicRefreshInfo(newOid, oid, NmCommandBean.DYNAMIC_UPD);
		} catch (WTException wte) {
			wte.printStackTrace();
			throw wte;
		}

		return super.doOperation(clientData, objectList);
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

		result= WIPHelper.wipSetResultNextAction(result, cb, objectList, di);
		if(cb.getParameterMap().keySet().contains("bomDetails")==true){
			result.setNextAction(FormResultAction.REFRESH_OPENER);
		}
		
        return result;
	}

	/**
	 * Method validateForLatestInteration Validates given workable for Latest
	 * Iteration and returns MESSAGE value.
	 *
	 * @param objRef
	 *            String
	 * @returns String indicating MESSAGE outcome of the isLatestIteration
	 */
	// TODO Remove this!!!
	public static String validateForLatestInteration(String objRef)
			throws Exception {
		logger.debug("validateForLatestInteration objRef:" + objRef);
		// LOGGER.debug("ENTER :
		// CheckoutObjectFormProcessor.validateForLatestInteration oid = " +
		// objRef);
		String responseMessage = "LATEST_ITERATION";
		try {
			Object obj = NmOid.newNmOid(objRef).getRefFactory().getReference(
					objRef).getObject();
			ObjectIdentifier objIdentifier = ObjectIdentifier
					.newObjectIdentifier(obj.toString());
			NmOid objOid = new NmOid("object", objIdentifier);
			Workable workable = (Workable) objOid.getRef();
			if (wt.vc.wip.WorkInProgressHelper.service
					.isCheckoutAllowed(workable)) {
				if (!(wt.vc.VersionControlHelper
						.isLatestIteration((wt.vc.Iterated) workable))) {
					responseMessage = "NON_LATEST_ITERATION";
					// LOGGER.debug(retMessage);
				}
			}
		} catch (Exception ex) {
			logger.debug("validateForLatestInteration - trapped exception");
			ex.printStackTrace();
			throw ex;
		}
		// LOGGER.debug("EXIT :
		// CheckoutObjectFormProcessor.validateForLatestInteration validation =
		// " + retMessage);
		return responseMessage;
	}
}
