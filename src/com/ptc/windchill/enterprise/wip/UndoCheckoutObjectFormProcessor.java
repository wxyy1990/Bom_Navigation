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
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import wt.epm.workspaces.BaselineServiceUtility;
import wt.epm.workspaces.EPMWorkspace;
import wt.epm.workspaces.EPMWorkspaceHelper;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.collections.WTSet;
import wt.fc.collections.WTHashSet;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;
import wt.vc.Iterated;
import wt.vc.Mastered;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.DynamicRefreshInfo;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.FormResultAction;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.model.NmObjectHelper;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.c504.part.Debug;

public class UndoCheckoutObjectFormProcessor extends DefaultObjectFormProcessor
		implements Serializable {

	protected DynamicRefreshInfo di;

	private static Logger logger = LogR
			.getLogger(UndoCheckoutObjectFormProcessor.class.getName());
   private Mastered master;

   /**
	 * <BR>
	 * <BR>
	 * <B>Supported API: </B>false Method doOperation
	 *
	 * @param clientData
	 * @param objectList
	 * @return FormResult
	 * @exception wt.util.WTException
	 */
	public FormResult doOperation(NmCommandBean clientData,
			List<ObjectBean> objectList) throws WTException {
		logger.debug("doOperation - clientData:" + clientData
				+ "\n   objectList:" + objectList);
		NmOid nmOid = clientData.getActionOid();
		logger.debug("doOperation - nmOid:" + nmOid);
		Workable originalCopy = null;

		Workable persistObj = (Workable) ObjectReference.newObjectReference(
				nmOid.getOid()).getObject();
      if(persistObj instanceof Iterated) {
         this.master = ((Iterated)persistObj).getMaster();
      }

		try {
			// Check if the object being undo checked out is in any workspace.
			// If it is in the workspace then after undo checkout
			// the original copy of that object should be placed in the
			// workspace
			EPMWorkspace workspace = BaselineServiceUtility
					.getAssociatedWorkspace((Workable) persistObj);
			if (workspace != null) {
				WTSet workingCopies = new WTHashSet();
				workingCopies.add(persistObj);
				WTSet originals = EPMWorkspaceHelper.manager
						.undoCheckoutAndRestoreInWorkspace(workspace,
								workingCopies);
				for (Iterator iter = originals.persistableIterator(); iter
						.hasNext();) {
					originalCopy = (Workable) iter.next();
				}
			} else {
				originalCopy = (Workable) WorkInProgressHelper.service
						.undoCheckout((Workable) persistObj);
			}
		} catch (WTPropertyVetoException wtpve) {
			logger.debug("Caught exception " + wtpve);
			wtpve.printStackTrace();
			throw new WTException(wtpve);
		}

		NmOid newOid = new NmOid(originalCopy);

		di = new DynamicRefreshInfo(newOid, nmOid, NmCommandBean.DYNAMIC_UPD);
		return super.doOperation(clientData, objectList);
	}

	@Override
	public FormResult setResultNextAction(FormResult result, NmCommandBean cb,
			List<ObjectBean> objectList) throws WTException {
		Debug.P(cb.getParameterMap());
		
		
		result= WIPHelper.wipSetResultNextAction(result, cb, objectList, di);
		Debug.P(cb.getParameterMap().keySet().contains("bomDetails"));
		
		if(cb.getParameterMap().keySet().contains("bomDetails")==true){
			result.setNextAction(FormResultAction.REFRESH_OPENER);
		}
        
        return result;
	}

   @Override
   public FormResult postTransactionProcess(NmCommandBean clientData, List<ObjectBean> objectBeans) throws WTException {


      if (this.master instanceof wt.part.WTPartMaster) {
         Properties properties = (Properties)clientData.getMap().get(NmObjectHelper.CGI_DATA);
         wt.part.PSBCacheHelper.service.refreshPSBCache(properties, this.master);
      }

      return super.processDelegates(POST_TRANSACTION_PROCESS, clientData, objectBeans);

   }

   
   /**
    * This API checks whether the context string has reference to the checked out table.
    * In case the undo checkout action is called from the Checked Out table, this API returns true. 
    * This API is used because in case Undo checkout is called from the Checked Out table we want to 
    * remove the row from the table. The normal row refresh does not work with the Checked Out table. 
    * @param clientData
    * @return
    */
   private static boolean isCheckoutTable(NmCommandBean clientData) {
	   String compContextStr = clientData.getCompContext();	   
	   if(compContextStr != null){
		   logger.debug("compContextStr = "+compContextStr);
		   return compContextStr.indexOf("checkedOutStuff")>=0;
	   }
	   return false;
   }

}