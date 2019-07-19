package ext.c504.part;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;

import wt.access.NotAuthorizedException;
import wt.csm.businessentity.BusinessEntity;
import wt.csm.navigation.CSMClassificationNavigationException;
import wt.csm.navigation.litenavigation.ClassificationStructDefaultView;
import wt.csm.navigation.service.ClassificationHelper;
import wt.csm.navigation.service.ClassificationService;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.iba.constraint.ConstraintGroup;
import wt.iba.definition.AbstractAttributeDefinition;
import wt.iba.definition.DefinitionLoader;
import wt.iba.definition.IBADefinitionException;
import wt.iba.definition.litedefinition.AbstractAttributeDefinizerView;
import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.iba.definition.litedefinition.AttributeDefNodeView;
import wt.iba.definition.litedefinition.BooleanDefView;
import wt.iba.definition.litedefinition.FloatDefView;
import wt.iba.definition.litedefinition.IntegerDefView;
import wt.iba.definition.litedefinition.RatioDefView;
import wt.iba.definition.litedefinition.ReferenceDefView;
import wt.iba.definition.litedefinition.StringDefView;
import wt.iba.definition.litedefinition.TimestampDefView;
import wt.iba.definition.litedefinition.URLDefView;
import wt.iba.definition.litedefinition.UnitDefView;
import wt.iba.definition.service.IBADefinitionHelper;
import wt.iba.value.AttributeContainer;
import wt.iba.value.DefaultAttributeContainer;
import wt.iba.value.DefaultAttributeContainerHelper;
import wt.iba.value.IBAContainerException;
import wt.iba.value.IBAHolder;
import wt.iba.value.IBAValueException;
import wt.iba.value.IBAValueUtility;
import wt.iba.value.litevalue.AbstractContextualValueDefaultView;
import wt.iba.value.litevalue.AbstractValueView;
import wt.iba.value.litevalue.BooleanValueDefaultView;
import wt.iba.value.litevalue.FloatValueDefaultView;
import wt.iba.value.litevalue.IntegerValueDefaultView;
import wt.iba.value.litevalue.RatioValueDefaultView;
import wt.iba.value.litevalue.StringValueDefaultView;
import wt.iba.value.litevalue.TimestampValueDefaultView;
import wt.iba.value.litevalue.URLValueDefaultView;
import wt.iba.value.litevalue.UnitValueDefaultView;
import wt.part.WTPart;
import wt.util.WTContext;
import wt.iba.value.litevalue.ReferenceValueDefaultView;
import wt.iba.value.service.IBAValueDBService;
import wt.iba.value.service.IBAValueHelper;
import wt.iba.value.service.LoadValue;
import wt.lite.AbstractLiteObject;
import wt.log4j.LogR;
import wt.query.ClassAttribute;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.units.service.QuantityOfMeasureDefaultView;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;

import com.ptc.core.meta.server.impl.LogicalIdentifierMap;

public class IBAUtility {

    Map ibaContainer;

    Map ibaOrigContainer;

    Map ibaContainerLogical;

    public List attributesList;

    final static String UNITS = "SI";

    Map ibaNameLogicalIDMap;
    private static final Logger LOGGER = LogR.getLogger(IBAUtility.class.getName());

    public IBAUtility() {
        ibaContainer = new HashMap();
    }

    public IBAUtility(IBAHolder ibaHolder) throws WTException {
        super();
        try {
            initializeIBAValue(ibaHolder);

        } catch (Exception e) {
            throw new WTException(e);
        }
    }

    public String toString() {

        StringBuffer tempString = new StringBuffer();
        Enumeration enumKeys = CollectionUtil.toHashtable(ibaContainer).keys();
        while (enumKeys.hasMoreElements()) {
            String theKey = (String) enumKeys.nextElement();
            AbstractValueView theValue = (AbstractValueView) ((Object[]) ibaContainer.get(theKey))[1];
            try {
                tempString.append(theKey
                        + " - "
                        + IBAValueUtility
                                .getLocalizedIBAValueDisplayString(theValue, SessionHelper.manager.getLocale()));
            } catch (WTException e) {
                LOGGER.error(e.getMessage(), e);
            }
            tempString.append('\n');
        }

        return (tempString.toString());

    }

    public Enumeration getAttributeDefinitions() {
        return CollectionUtil.toHashtable(ibaContainer).keys();
    }

    public void removeAllAttributes() throws WTException, WTPropertyVetoException {
        ibaContainer.clear();
    }

    public void removeAttribute(String name) throws WTException, WTPropertyVetoException {
        // System.out.println("***" + ibaContainer.containsKey(name));
        ibaContainer.remove(name);
    }

    // public void removeIBAAttribute(String name) throws
    // WTException,WTPropertyVetoException {
    // ibaContainer.remove(name);
    // }

    /**
     * return single IBA value
     * 
     * @param name
     * @return
     * @throws WTException
     */
    public String getIBAValue(String name) throws WTException {
        String value = null;
        if (ibaContainer.get(name) != null) {
            AbstractValueView theValue = (AbstractValueView) ((Object[]) ibaContainer.get(name))[1];
            LOGGER.debug("getIBAValue locale ----------------- " + WTContext.getContext().getLocale());
            value = (IBAValueUtility.getLocalizedIBAValueDisplayString(theValue, WTContext.getContext().getLocale()));
        }
        return value;
    }

    /**
     * return multiple IBA values
     * 
     * @param name
     * @return
     * @throws WTException
     */
    public List getIBAValues(String name) throws WTException {
        List vector = new ArrayList();
        if (ibaContainer.get(name) != null) {
            Object[] objs = (Object[]) ibaContainer.get(name);
            for (int i = 1; i < objs.length; i++) {
                AbstractValueView theValue = (AbstractValueView) objs[i];
                vector.add(IBAValueUtility.getLocalizedIBAValueDisplayString(theValue, SessionHelper.manager
                        .getLocale()));
            }
        }
        return vector;
    }

    /**
     * return multiple IBA values & dependency relationship
     * 
     * @param name
     * @return
     * @throws WTException
     */
    public List getIBAValuesWithDependency(String name) throws WTException {
        List vector = new ArrayList();
        if (ibaContainer.get(name) != null) {
            Object[] objs = (Object[]) ibaContainer.get(name);
            for (int i = 1; i < objs.length; i++) {
                AbstractValueView theValue = (AbstractValueView) objs[i];
                String[] temp = new String[3];
                temp[0] = IBAValueUtility
                        .getLocalizedIBAValueDisplayString(theValue, SessionHelper.manager.getLocale());
                if ((theValue instanceof AbstractContextualValueDefaultView)
                        && ((AbstractContextualValueDefaultView) theValue).getReferenceValueDefaultView() != null) {
                    temp[1] = ((AbstractContextualValueDefaultView) theValue).getReferenceValueDefaultView()
                            .getReferenceDefinition().getName();
                    temp[2] = ((AbstractContextualValueDefaultView) theValue).getReferenceValueDefaultView()
                            .getLocalizedDisplayString();
                } else {
                    temp[1] = null;
                    temp[2] = null;
                }
                vector.add(temp);
            }
        }
        return vector;
    }

    public List getIBAValuesWithBusinessEntity(String name) throws WTRuntimeException, WTException {
        List vector = new ArrayList();
        if (ibaContainer.get(name) != null) {
            Object[] objs = (Object[]) ibaContainer.get(name);
            for (int i = 1; i < objs.length; i++) {
                AbstractValueView theValue = (AbstractValueView) objs[i];
                Object[] temp = new Object[2];
                temp[0] = IBAValueUtility
                        .getLocalizedIBAValueDisplayString(theValue, SessionHelper.manager.getLocale());
                if ((theValue instanceof AbstractContextualValueDefaultView)
                        && ((AbstractContextualValueDefaultView) theValue).getReferenceValueDefaultView() != null) {
                    ReferenceValueDefaultView referencevaluedefaultview = ((AbstractContextualValueDefaultView) theValue)
                            .getReferenceValueDefaultView();
                    ObjectIdentifier objectidentifier = ((wt.iba.value.litevalue.DefaultLiteIBAReferenceable) referencevaluedefaultview
                            .getLiteIBAReferenceable()).getObjectID();
                    Persistable persistable = ObjectReference.newObjectReference(objectidentifier).getObject();
                    temp[1] = (BusinessEntity) persistable;
                } else {
                    temp[1] = null;
                }
                vector.add(temp);
            }
        }
        return vector;
    }

    public BusinessEntity getIBABusinessEntity(String name) throws WTRuntimeException, WTException {
        BusinessEntity value = null;
        if (ibaContainer.get(name) != null) {
            AbstractValueView theValue = (AbstractValueView) ((Object[]) ibaContainer.get(name))[1];
            ReferenceValueDefaultView referencevaluedefaultview = (ReferenceValueDefaultView) theValue;
            ObjectIdentifier objectidentifier = ((wt.iba.value.litevalue.DefaultLiteIBAReferenceable) referencevaluedefaultview
                    .getLiteIBAReferenceable()).getObjectID();
            Persistable persistable = ObjectReference.newObjectReference(objectidentifier).getObject();
            value = (BusinessEntity) persistable;
        }
        return value;
    }

    public List getIBABusinessEntities(String name) throws WTRuntimeException, WTException {
        List vector = new ArrayList();
        if (ibaContainer.get(name) != null) {
            Object[] objs = (Object[]) ibaContainer.get(name);
            for (int i = 1; i < objs.length; i++) {
                AbstractValueView theValue = (AbstractValueView) objs[i];
                ReferenceValueDefaultView referencevaluedefaultview = (ReferenceValueDefaultView) theValue;
                ObjectIdentifier objectidentifier = ((wt.iba.value.litevalue.DefaultLiteIBAReferenceable) referencevaluedefaultview
                        .getLiteIBAReferenceable()).getObjectID();
                Persistable persistable = ObjectReference.newObjectReference(objectidentifier).getObject();
                vector.add(persistable);
            }
        }
        return vector;
    }

    private AbstractValueView getAbstractValueViewURLType(AttributeDefDefaultView theDef, String urlname,
            String urlvalue) throws WTException, WTPropertyVetoException {
        if (urlname == null || urlname.trim().equals("null")) {
            throw new WTException("Trace.. name = " + theDef.getName() + ", identifier = null value.");
        }
        if (urlvalue == null || urlvalue.trim().equals("null")) {
            throw new WTException("Trace.. name = " + theDef.getName() + ", identifier = null value.");
        }
        String name = theDef.getName();
        AbstractValueView ibaValue = null;

        ibaValue = internalCreateValue(theDef, urlvalue, urlname);
        if (ibaValue == null) {
            throw new WTException("Trace.. name = " + theDef.getName() + ", identifier = " + urlname + " not found.");
            // return;
        }

        if (ibaValue instanceof ReferenceValueDefaultView) {
            ibaValue = getOriginalReferenceValue(name, ibaValue);
        }
        ibaValue.setState(AbstractValueView.NEW_STATE);
        return ibaValue;
    }

    private AbstractValueView getAbstractValueView(AttributeDefDefaultView theDef, String value) throws WTException,
            WTPropertyVetoException {
        if (value == null || value.trim().equals("null")) {
            throw new WTException("Trace.. name = " + theDef.getName() + ", identifier = null value.");
        }
        String name = theDef.getName();
        String value2 = null;
        AbstractValueView ibaValue = null;

        if (theDef instanceof UnitDefView) {
            value = value + " " + getDisplayUnits((UnitDefView) theDef, UNITS);
        } else if (theDef instanceof ReferenceDefView) {
            value2 = value;
            value = ((ReferenceDefView) theDef).getReferencedClassname();
        }

        ibaValue = internalCreateValue(theDef, value, value2);
        if (ibaValue == null) {
            throw new WTException("Trace.. name = " + theDef.getName() + ", identifier = " + value + " not found.");
            // return;
        }

        if (ibaValue instanceof ReferenceValueDefaultView) {
            ibaValue = getOriginalReferenceValue(name, ibaValue);
        }
        ibaValue.setState(AbstractValueView.NEW_STATE);
        return ibaValue;
    }

    private AbstractValueView getOriginalReferenceValue(String name, AbstractValueView ibaValue)
            throws IBAValueException {
        Object[] objs = (Object[]) ibaOrigContainer.get(name);
        if (objs != null && (ibaValue instanceof ReferenceValueDefaultView)) {
            int businessvaluepos = 1;
            for (businessvaluepos = 1; businessvaluepos < objs.length; businessvaluepos++) {
                if (((AbstractValueView) objs[businessvaluepos]).compareTo(ibaValue) == 0) {
                    ibaValue = (AbstractValueView) objs[businessvaluepos];
                    break;
                }
            }
        }
        return ibaValue;
    }

    private AttributeDefDefaultView getDefDefaultView(String name) throws WTException, RemoteException {
        AttributeDefDefaultView theDef = null;
        Object[] obj = (Object[]) ibaContainer.get(name);
        if (obj != null) {
            theDef = (AttributeDefDefaultView) obj[0];
        } else {
            theDef = getAttributeDefinition(name);
        }
        if (theDef == null) {
            throw new WTException("Trace.. name = " + name + " not existed.");
        }
        return theDef;
    }

    public void setIBAValue(String name, String value) throws WTException, WTPropertyVetoException, RemoteException {
        AttributeDefDefaultView theDef = getDefDefaultView(name);
        Object theValue = getAbstractValueView(theDef, value);
        Object[] temp = new Object[2];
        temp[0] = theDef;
        temp[1] = theValue;
        ibaContainer.put(name, temp);
    }

    /**
     * Set the attribute with multiple values from the list
     * 
     * @param name
     * @param values
     * @throws WTPropertyVetoException
     * @throws WTException
     * @throws RemoteException
     */
    public void setIBAValues(String name, List values) throws WTPropertyVetoException, WTException, RemoteException {
        AttributeDefDefaultView theDef = getDefDefaultView(name);
        Object[] temp = new Object[values.size() + 1];
        temp[0] = theDef;
        for (int i = 0; i < values.size(); i++) {
            String value = (String) values.get(i);
            Object theValue = getAbstractValueView(theDef, value);
            temp[i + 1] = theValue;
        }
        ibaContainer.put(name, temp);
    }

    public void addIBAValueURLType(String name, String urlname, String urlvalue) throws WTException,
            WTPropertyVetoException, RemoteException {
        Object[] obj = (Object[]) ibaContainer.get(name);
        AttributeDefDefaultView theDef = getDefDefaultView(name);
        Object theValue = getAbstractValueViewURLType(theDef, urlname, urlvalue);

        Object[] temp;
        if (obj == null) {
            temp = new Object[2];
            temp[0] = theDef;
            temp[1] = theValue;
        } else {
            temp = new Object[obj.length + 1];
            int i;
            for (i = 0; i < obj.length; i++)
                temp[i] = obj[i];
            temp[i] = theValue;
        }
        ibaContainer.put(name, temp);
    }

    public void addIBAValue(String name, String value) throws WTException, WTPropertyVetoException, RemoteException {
        Object[] obj = (Object[]) ibaContainer.get(name);
        AttributeDefDefaultView theDef = getDefDefaultView(name);
        Object theValue = getAbstractValueView(theDef, value);

        Object[] temp;
        if (obj == null) {
            temp = new Object[2];
            temp[0] = theDef;
            temp[1] = theValue;
        } else {
            temp = new Object[obj.length + 1];
            int i;
            for (i = 0; i < obj.length; i++)
                temp[i] = obj[i];
            temp[i] = theValue;
        }

        ibaContainer.put(name, temp);
    }

    private AbstractValueView setDependency(AttributeDefDefaultView sourceDef, AbstractValueView sourceValue,
            AttributeDefDefaultView businessDef, AbstractValueView businessValue) throws WTPropertyVetoException,
            WTException {
        String sourcename = sourceDef.getName();
        String businessname = businessDef.getName();

        if (businessValue == null) {
            throw new WTException("This Business Entity:" + businessname
                    + " value doesn't exist in System Business Entity. Add IBA dependancy failed!!");
        }
        Object[] businessobj = (Object[]) ibaContainer.get(businessname);
        if (businessobj == null) {
            throw new WTException("IBA:" + businessname + " Value is null. Add IBA dependancy failed!!");
        }

        int businessvaluepos = 1;
        for (businessvaluepos = 1; businessvaluepos < businessobj.length; businessvaluepos++) {
            if (((AbstractValueView) businessobj[businessvaluepos]).compareTo(businessValue) == 0) {
                businessValue = (AbstractValueView) businessobj[businessvaluepos];
                break;
            }
        }
        if (businessvaluepos == businessobj.length) {
            throw new WTException("This Business Entity:" + businessname + " value:"
                    + businessValue.getLocalizedDisplayString()
                    + " is not existed in Part IBA values. Add IBA dependancy failed!!");
        }

        if (!(businessValue instanceof ReferenceValueDefaultView)) {
            throw new WTException("This Business Entity:" + businessname + " value:"
                    + businessValue.getLocalizedDisplayString()
                    + " is not a ReferenceValueDefaultView. Add IBA dependancy failed!!");
        }
        ((AbstractContextualValueDefaultView) sourceValue)
                .setReferenceValueDefaultView((ReferenceValueDefaultView) businessValue);
        return sourceValue;
    }

    public void setIBAValue(String sourcename, String sourcevalue, String businessname, String businessvalue)
            throws IBAValueException, WTPropertyVetoException, WTException, RemoteException {

        AttributeDefDefaultView sourceDef = getDefDefaultView(sourcename);
        AttributeDefDefaultView businessDef = getDefDefaultView(businessname);
        AbstractValueView sourceValue = getAbstractValueView(sourceDef, sourcevalue);
        AbstractValueView businessValue = getAbstractValueView(businessDef, businessvalue);
        sourceValue = setDependency(sourceDef, sourceValue, businessDef, businessValue);
        Object[] temp = new Object[2];
        temp[0] = sourceDef;
        temp[1] = sourceValue;
        ibaContainer.put(sourcename, temp);
    }

    /**
     * Add an IBA value with dependency relation
     * 
     * @param sourcename
     * @param sourcevalue
     * @param businessname
     * @param businessvalue
     * @throws IBAValueException
     * @throws WTPropertyVetoException
     * @throws WTException
     * @throws RemoteException
     */

    public void addIBAValue(String sourcename, String sourcevalue, String businessname, String businessvalue)
            throws IBAValueException, WTPropertyVetoException, WTException, RemoteException {
        AttributeDefDefaultView sourceDef = getDefDefaultView(sourcename);
        AttributeDefDefaultView businessDef = getDefDefaultView(businessname);
        AbstractValueView sourceValue = getAbstractValueView(sourceDef, sourcevalue);
        AbstractValueView businessValue = getAbstractValueView(businessDef, businessvalue);
        sourceValue = setDependency(sourceDef, sourceValue, businessDef, businessValue);

        Object[] obj = (Object[]) ibaContainer.get(sourcename);
        Object[] temp;
        if (obj == null) {
            temp = new Object[2];
            temp[0] = sourceDef;
            temp[1] = sourceValue;
        } else {
            temp = new Object[obj.length + 1];
            int i;
            for (i = 0; i < obj.length; i++)
                temp[i] = obj[i];
            temp[i] = sourceValue;
        }
        ibaContainer.put(sourcename, temp);
    }

    // initializePart() with this signature is designed to pre-populate values
    // from an existing IBA holder.

    private void initializeIBAValue(IBAHolder ibaHolder) throws WTException, RemoteException {
        String logicalIdentifier;
        ibaContainer = new Hashtable();
        ibaOrigContainer = new Hashtable();
        ibaContainerLogical = new Hashtable();
        ibaNameLogicalIDMap = new Hashtable();
        attributesList = new ArrayList();
        if (ibaHolder.getAttributeContainer() == null) {
            ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null, SessionHelper.manager
                    .getLocale(), null);
        }
        DefaultAttributeContainer theContainer = (DefaultAttributeContainer) ibaHolder.getAttributeContainer();
        if (theContainer != null) {
            AttributeDefDefaultView[] theAtts = theContainer.getAttributeDefinitions();
            for (int i = 0; i < theAtts.length; i++) {
                AbstractValueView[] theValues = theContainer.getAttributeValues(theAtts[i]);

                logicalIdentifier = LogicalIdentifierMap.getLogicalIdentifier(ObjectReference
                        .newObjectReference(theAtts[i].getObjectID()));
                if (logicalIdentifier != null) {
                    attributesList.add(logicalIdentifier);
                }

                if (theValues != null) {
                    Object[] temp = new Object[theValues.length + 1];
                    temp[0] = theAtts[i];
                    for (int j = 1; j <= theValues.length; j++) {
                        temp[j] = theValues[j - 1];
                    }
                    ibaContainer.put(theAtts[i].getName(), temp);
                    ibaOrigContainer.put(theAtts[i].getName(), temp);

                    if (logicalIdentifier != null) {
                        ibaContainerLogical.put(logicalIdentifier, temp);
                        ibaNameLogicalIDMap.put(theAtts[i].getName(), logicalIdentifier);
                    }
                }
            }
        }
    }

    private DefaultAttributeContainer suppressCSMConstraint(DefaultAttributeContainer theContainer, String s)
            throws WTException, ClassNotFoundException, WTPropertyVetoException, RemoteException {
        ClassificationStructDefaultView defStructure = null;
        defStructure = getClassificationStructDefaultViewByName(s);
        if (defStructure != null) {
            // ReferenceDefView ref = defStructure.getReferenceDefView();
            List cgs = theContainer.getConstraintGroups();
            List newCgs = new ArrayList();
            // AttributeConstraint immutable = null;
            // if (VERBOSE)
            // System.out.println("cgs size="+cgs.size());
            for (int i = 0; i < cgs.size(); i++) {
                ConstraintGroup cg = (ConstraintGroup) cgs.get(i);
                if (cg != null) {
                    // System.out.println(cg.getConstraintGroupLabel());
                    if (!cg.getConstraintGroupLabel().equals(
                            wt.csm.constraint.CSMConstraintFactory.CONSTRAINT_GROUP_LABEL)) {
                        newCgs.add(cg);
                    } else {
                        // Enumeration enum = cg.getConstraints();
                        ConstraintGroup newCg = new ConstraintGroup();
                        newCg.setConstraintGroupLabel(cg.getConstraintGroupLabel());
                        newCgs.add(newCg);
                    }
                }
            }
            theContainer.setConstraintGroups(CollectionUtil.toVector(newCgs));
        }
        return theContainer;
    }

    @SuppressWarnings("unchecked")
    private DefaultAttributeContainer removeCSMConstraint(DefaultAttributeContainer attributecontainer)
            throws WTPropertyVetoException {
        Object obj = attributecontainer.getConstraintParameter();
        if (obj == null) {
            obj = String.valueOf("CSM");
        } else if (obj instanceof List) {
            ((List) obj).add(String.valueOf("CSM"));
        } else {
            List vector1 = new ArrayList();
            vector1.add(obj);
            obj = vector1;
            ((List) obj).add(String.valueOf("CSM"));
        }
        attributecontainer.setConstraintParameter(obj);
        return attributecontainer;
    }

    /**
     * Update the IBAHolder's attribute container from the hashtable
     * 
     * @param ibaHolder
     * @return
     * @throws WTException
     * @throws WTPropertyVetoException
     * @throws RemoteException
     * @throws ClassNotFoundException
     */
    public IBAHolder updateAttributeContainer(IBAHolder ibaHolder) throws WTException, WTPropertyVetoException,
            RemoteException, ClassNotFoundException {
        if (ibaHolder.getAttributeContainer() == null) {
            ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null, SessionHelper.manager
                    .getLocale(), null);
        }
        DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaHolder
                .getAttributeContainer();

        defaultattributecontainer = suppressCSMConstraint(defaultattributecontainer, getIBAHolderClassName(ibaHolder));

        AttributeDefDefaultView[] theAtts = defaultattributecontainer.getAttributeDefinitions();
        // Delete existed iba if they aren't in the hashtable of this class
        for (int i = 0; i < theAtts.length; i++) {
            AttributeDefDefaultView theDef = theAtts[i];
            if (ibaContainer.get(theDef.getName()) == null) {
                createOrUpdateAttributeValuesInContainer(defaultattributecontainer, theDef, null);
            }
        }
        Enumeration enum1 = CollectionUtil.toHashtable(ibaContainer).elements();
        while (enum1.hasMoreElements()) {
            Object[] temp = (Object[]) enum1.nextElement();
            AttributeDefDefaultView theDef = (AttributeDefDefaultView) temp[0];
            AbstractValueView abstractvalueviews[] = new AbstractValueView[temp.length - 1];
            for (int i = 0; i < temp.length - 1; i++) {
                abstractvalueviews[i] = (AbstractValueView) temp[i + 1];
            }
            createOrUpdateAttributeValuesInContainer(defaultattributecontainer, theDef, abstractvalueviews);
        }

        defaultattributecontainer = removeCSMConstraint(defaultattributecontainer);
        ibaHolder.setAttributeContainer(defaultattributecontainer);

        return ibaHolder;
    }

    /**
     * Update without checkout/checkin
     * 
     * @param ibaholder
     * @return
     */
    public static boolean updateIBAHolder(IBAHolder ibaholder) throws WTException {
        IBAValueDBService ibavaluedbservice = new IBAValueDBService();
        boolean flag = true;
        PersistenceServerHelper.manager.update((Persistable) ibaholder);
        AttributeContainer attributecontainer = ibaholder.getAttributeContainer();
        Object obj = ((DefaultAttributeContainer) attributecontainer).getConstraintParameter();
        AttributeContainer attributecontainer1 = ibavaluedbservice.updateAttributeContainer(ibaholder, obj, null, null);
        ibaholder.setAttributeContainer(attributecontainer1);
        return flag;
    }

    /**
     * Referenced from method "createOrUpdateAttributeValueInContainer" of wt.iba.value.service.LoadValue.java ->
     * modified to have multi-values support
     * 
     * @param defaultattributecontainer
     * @param theDef
     * @param abstractvalueviews
     * @throws WTException
     */
    private void createOrUpdateAttributeValuesInContainer(DefaultAttributeContainer defaultattributecontainer,
            AttributeDefDefaultView theDef, AbstractValueView[] abstractvalueviews) throws WTException,
            WTPropertyVetoException {
        if (defaultattributecontainer == null) {
            throw new IBAContainerException(
                    "wt.iba.value.service.LoadValue.createOrUpdateAttributeValueInContainer :  DefaultAttributeContainer passed in is null!");
        }
        AbstractValueView abstractvalueviews0[] = defaultattributecontainer.getAttributeValues(theDef);
        if (abstractvalueviews0 == null || abstractvalueviews0.length == 0) {
            // Original valus is empty
            for (int j = 0; j < abstractvalueviews.length; j++) {
                AbstractValueView abstractvalueview = abstractvalueviews[j];
                defaultattributecontainer.addAttributeValue(abstractvalueview);
                // System.out.println("IBAUtil:"+abstractvalueview.getLocalizedDisplayString()+"
                // in "+abstractvalueview.getDefinition().getName());
            }
        } else if (abstractvalueviews == null || abstractvalueviews.length == 0) {
            // New value is empty, so delete all existed values
            for (int j = 0; j < abstractvalueviews0.length; j++) {
                AbstractValueView abstractvalueview = abstractvalueviews0[j];
                defaultattributecontainer.deleteAttributeValue(abstractvalueview);
            }
        } else if (abstractvalueviews0.length <= abstractvalueviews.length) {

            // More new valuss than (or equal to) original values,
            // So update existed values and add new values
            for (int j = 0; j < abstractvalueviews0.length; j++) {
                abstractvalueviews0[j] = LoadValue
                        .cloneAbstractValueView(abstractvalueviews[j], abstractvalueviews0[j]);
                // abstractvalueviews0[j] = abstractvalueviews[j];
                abstractvalueviews0[j] = cloneReferenceValueDefaultView(abstractvalueviews[j], abstractvalueviews0[j]);

                defaultattributecontainer.updateAttributeValue(abstractvalueviews0[j]);
            }
            for (int j = abstractvalueviews0.length; j < abstractvalueviews.length; j++) {
                AbstractValueView abstractvalueview = abstractvalueviews[j];
                // abstractvalueview.setState(AbstractValueView.CHANGED_STATE);
                defaultattributecontainer.addAttributeValue(abstractvalueview);
            }
        } else if (abstractvalueviews0.length > abstractvalueviews.length) {
            // Less new values than original values,
            // So delete some values
            for (int j = 0; j < abstractvalueviews.length; j++) {
                abstractvalueviews0[j] = LoadValue
                        .cloneAbstractValueView(abstractvalueviews[j], abstractvalueviews0[j]);
                abstractvalueviews0[j] = cloneReferenceValueDefaultView(abstractvalueviews[j], abstractvalueviews0[j]);
                // abstractvalueviews0[j] = abstractvalueviews[j];
                defaultattributecontainer.updateAttributeValue(abstractvalueviews0[j]);
            }
            for (int j = abstractvalueviews.length; j < abstractvalueviews0.length; j++) {
                AbstractValueView abstractvalueview = abstractvalueviews0[j];
                defaultattributecontainer.deleteAttributeValue(abstractvalueview);
            }
        }
    }

    // For dependency used.
    AbstractValueView cloneReferenceValueDefaultView(AbstractValueView abstractvalueview,
            AbstractValueView abstractvalueview1) throws IBAValueException, WTPropertyVetoException {
        if (abstractvalueview instanceof AbstractContextualValueDefaultView) {

            ((AbstractContextualValueDefaultView) abstractvalueview1)
                    .setReferenceValueDefaultView(((AbstractContextualValueDefaultView) abstractvalueview)
                            .getReferenceValueDefaultView());

        }
        return abstractvalueview1;
    }

    /**
     * another "black-box": pass in a string, and get back an IBA value object. Copy from
     * wt.iba.value.service.LoadValue.java -> please don't modify this method
     * 
     * @param abstractattributedefinizerview
     * @param s
     * @param s1
     * @return
     */
    private static AbstractValueView internalCreateValue(AbstractAttributeDefinizerView abstractattributedefinizerview,
            String s, String s1) {
        AbstractValueView abstractvalueview = null;
    //    Debug.P("======="+abstractattributedefinizerview);
        
        if (abstractattributedefinizerview instanceof FloatDefView) {
            abstractvalueview = LoadValue.newFloatValue(abstractattributedefinizerview, s, s1);
        } else if (abstractattributedefinizerview instanceof StringDefView) {
            abstractvalueview = LoadValue.newStringValue(abstractattributedefinizerview, s);
        } else if (abstractattributedefinizerview instanceof IntegerDefView) {
            abstractvalueview = LoadValue.newIntegerValue(abstractattributedefinizerview, s);
        } else if (abstractattributedefinizerview instanceof RatioDefView) {
            abstractvalueview = LoadValue.newRatioValue(abstractattributedefinizerview, s, s1);
        } else if (abstractattributedefinizerview instanceof TimestampDefView) {
            abstractvalueview = LoadValue.newTimestampValue(abstractattributedefinizerview, s);
        } else if (abstractattributedefinizerview instanceof BooleanDefView) {
            abstractvalueview = LoadValue.newBooleanValue(abstractattributedefinizerview, s);
        } else if (abstractattributedefinizerview instanceof URLDefView) {
        	String value[]=s.split("showValue");
            abstractvalueview = LoadValue.newURLValue(abstractattributedefinizerview, value[0], value[1]);
//        	if(s!=null && !s.trim().equals("")){
//	        	String value[]=s.split("showValue");
//	            abstractvalueview = LoadValue.newURLValue(abstractattributedefinizerview, value[0], value[1]);
//        	}else{
//        		abstractvalueview = LoadValue.newURLValue(abstractattributedefinizerview, null, null);
//        	}
            
        } else if (abstractattributedefinizerview instanceof ReferenceDefView) {
            abstractvalueview = LoadValue.newReferenceValue(abstractattributedefinizerview, s, s1);
        } else if (abstractattributedefinizerview instanceof UnitDefView) {
            abstractvalueview = LoadValue.newUnitValue(abstractattributedefinizerview, s, s1);
        }

        return abstractvalueview;
    }

    /**
     * This method is a "black-box": pass in a string, like "Electrical/Resistance/ ResistanceRating" and get back a IBA
     * definition object.
     * 
     * @param ibaPath
     * @return
     * @throws WTException
     * @throws RemoteException
     * @throws NotAuthorizedException
     * @throws IBADefinitionException
     */
    public AttributeDefDefaultView getAttributeDefinition(String ibaPath) throws IBADefinitionException,
            NotAuthorizedException, RemoteException, WTException {

        AttributeDefDefaultView ibaDef = null;
        ibaDef = IBADefinitionHelper.service.getAttributeDefDefaultViewByPath(ibaPath);
        if (ibaDef == null) {
            AbstractAttributeDefinizerView ibaNodeView = DefinitionLoader.getAttributeDefinition(ibaPath);
            if (ibaNodeView != null) {
                ibaDef = IBADefinitionHelper.service.getAttributeDefDefaultView((AttributeDefNodeView) ibaNodeView);
            }
        }

        return ibaDef;
    }

    public static String getDisplayUnits(UnitDefView unitdefview) {
        return getDisplayUnits(unitdefview, UNITS);
    }

    public static String getDisplayUnits(UnitDefView unitdefview, String s) {
        String s1 = "";
        QuantityOfMeasureDefaultView quantityofmeasuredefaultview = unitdefview.getQuantityOfMeasureDefaultView();
        s1 = quantityofmeasuredefaultview.getBaseUnit();
        if (s != null) {
            String s2 = unitdefview.getDisplayUnitString(s);
            if (s2 == null) {
                s2 = quantityofmeasuredefaultview.getDisplayUnitString(s);
            }
            if (s2 == null) {
                s2 = quantityofmeasuredefaultview.getDefaultDisplayUnitString(s);
            }
            if (s2 != null) {
                s1 = s2;
            }
        }
        if (s1 == null) {
            s1 = "";
        }
        return s1;
    }

    public static String getClassificationStructName(IBAHolder ibaHolder) throws CSMClassificationNavigationException,
            RemoteException, WTException, ClassNotFoundException {
        String s = getIBAHolderClassName(ibaHolder);
        ClassificationService classificationservice = ClassificationHelper.service;
        ClassificationStructDefaultView aclassificationstructdefaultview[] = null;
        aclassificationstructdefaultview = classificationservice.getAllClassificationStructures();
        for (int i = 0; aclassificationstructdefaultview != null && i < aclassificationstructdefaultview.length; i++)
            if (s.equals(aclassificationstructdefaultview[i].getPrimaryClassName())) {
                return s;
            }

        for (Class class1 = Class.forName(s).getSuperclass(); !class1.getName()
                .equals((wt.fc.WTObject.class).getName())
                && !class1.getName().equals((java.lang.Object.class).getName()); class1 = class1.getSuperclass()) {
            for (int j = 0; aclassificationstructdefaultview != null && j < aclassificationstructdefaultview.length; j++)
                if (class1.getName().equals(aclassificationstructdefaultview[j].getPrimaryClassName())) {
                    return class1.getName();
                }

        }

        return null;
    }

    /**
     * Please refer to the method "getIBAHolderClassName" of class "wt.csm.constraint.CSMConstraintFactory"
     * 
     * @param ibaholder
     * @return
     */
    private static String getIBAHolderClassName(IBAHolder ibaholder) {
        String s = null;
        if (ibaholder instanceof AbstractLiteObject) {
            s = ((AbstractLiteObject) ibaholder).getHeavyObjectClassname();
        } else {
            s = ibaholder.getClass().getName();
        }
        return s;
    }

    /**
     * Please refer to the method "getClassificationStructDefaultViewByName" of class
     * "wt.csm.constraint.CSMConstraintFactory"
     * 
     * @param s
     * @return
     * @throws ClassNotFoundException
     * @throws WTException
     * @throws RemoteException
     * @throws CSMClassificationNavigationException
     */
    private ClassificationStructDefaultView getClassificationStructDefaultViewByName(String s)
            throws ClassNotFoundException, CSMClassificationNavigationException, RemoteException, WTException {
        ClassificationService classificationservice = ClassificationHelper.service;
        ClassificationStructDefaultView aclassificationstructdefaultview[] = null;
        aclassificationstructdefaultview = classificationservice.getAllClassificationStructures();
        for (int i = 0; aclassificationstructdefaultview != null && i < aclassificationstructdefaultview.length; i++)
            if (s.equals(aclassificationstructdefaultview[i].getPrimaryClassName())) {
                return aclassificationstructdefaultview[i];
            }

        for (Class class1 = Class.forName(s).getSuperclass(); !class1.getName()
                .equals((wt.fc.WTObject.class).getName())
                && !class1.getName().equals((java.lang.Object.class).getName()); class1 = class1.getSuperclass()) {
            for (int j = 0; aclassificationstructdefaultview != null && j < aclassificationstructdefaultview.length; j++)
                if (class1.getName().equals(aclassificationstructdefaultview[j].getPrimaryClassName())) {
                    return aclassificationstructdefaultview[j];
                }

        }

        return null;
    }

    /**
     * This is used to get the IBA value for the given Logical Identifier
     * 
     * @param name
     * @return single IBA value
     * @throws WTException
     */
    public String getIBAValueByLogicalIdentifier(String name) throws WTException {
        String value = null;
        if (ibaContainerLogical.get(name) != null) {
            AbstractValueView theValue = (AbstractValueView) ((Object[]) ibaContainerLogical.get(name))[1];
            value = (IBAValueUtility.getLocalizedIBAValueDisplayString(theValue, SessionHelper.manager.getLocale()));
        }
        return value;
    }

    /**
     * This is used to check whether an IBA exists or for the given Logical Identifier
     * 
     * @param name
     * @return true/false. true, if the IBA exists else false.
     */
    public static boolean isLogicalIdentifierExists(String logicalIdentifier) throws WTException {
        if (LogicalIdentifierMap.getMapEntry(LogicalIdentifierMap.ATTRIBUTE_GROUP, logicalIdentifier) != null) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * This method is for getting LogicalIdentifier by IBA Name
     * 
     * @param name
     * @return IBA LogicalIdentifier
     */
    public String getIBALogicalIdentifierByName(String name) {
        String value = null;
        value = (String) ibaNameLogicalIDMap.get(name);
        return value;
    }

    public static String getIBAHierarchyID(String IBAName) throws WTException {
        String IBAobjectID = null;
        QuerySpec queryspec = new QuerySpec(wt.iba.definition.AbstractAttributeDefinition.class);
        queryspec.appendSearchCondition(new SearchCondition(wt.iba.definition.AbstractAttributeDefinition.class,
                "name", "=", IBAName));
        QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
        if (queryresult.hasMoreElements()) {
            AbstractAttributeDefinition attributeDefinition = (AbstractAttributeDefinition) queryresult.nextElement();
            IBAobjectID = attributeDefinition.getHierarchyID();
        }
        return IBAobjectID;
    }

    public static String getIBADisplayName(String ibaName) throws WTException {
        String name = null;

        String hierarchyID = getIBAHierarchyID(ibaName);

        QuerySpec queryspec = new QuerySpec(wt.iba.definition.AbstractAttributeDefinition.class);
        queryspec.appendSearchCondition(new SearchCondition(wt.iba.definition.AbstractAttributeDefinition.class,
                "hierarchyID", "=", hierarchyID));
        QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
        if (queryresult.hasMoreElements()) {
            AbstractAttributeDefinition attributeDefinition = (AbstractAttributeDefinition) queryresult.nextElement();
            name = attributeDefinition.getDisplayName();
        }
        return name;
    }

    public static Map getBulkIBAString(List vall, String ibaname) throws WTException, WTPropertyVetoException {
        QuerySpec mainSelect = new QuerySpec();
        mainSelect.getFromClause().setAliasPrefix("A");
        Class stringValueClass = wt.iba.value.StringValue.class;
        Class stringDefClass = wt.iba.definition.StringDefinition.class;
        int stringValueIndex = mainSelect.appendClassList(stringValueClass, false);
        int stringDefIndex = mainSelect.appendClassList(stringDefClass, false);
        ClassAttribute stringDefAttr = new ClassAttribute(stringDefClass, wt.iba.definition.StringDefinition.NAME);
        ClassAttribute stringValueAttr = new ClassAttribute(stringValueClass, wt.iba.value.StringValue.VALUE2);
        ClassAttribute stringIdAttr = new ClassAttribute(stringValueClass, "theIBAHolderReference.key.id");
        mainSelect.appendSelect(stringIdAttr, stringValueIndex, false);
        mainSelect.appendSelect(stringDefAttr, stringDefIndex, false);
        mainSelect.appendSelect(stringValueAttr, stringValueIndex, false);
        SearchCondition join = new SearchCondition(stringValueClass, "definitionReference.key.id", stringDefClass,
                WTAttributeNameIfc.ID_NAME);
        mainSelect.appendWhere(join, stringValueIndex, stringDefIndex);
        mainSelect.appendAnd();
        mainSelect.appendOpenParen();
        for (int j = 0; j < vall.size(); j++) {
            if (!(vall.get(j) instanceof Persistable)) {
                continue;
            }
            Long lid = Long.valueOf(((Persistable) (vall.get(j))).getPersistInfo().getObjectIdentifier().getId());
            SearchCondition sc = new SearchCondition(stringValueClass, "theIBAHolderReference.key.id",
                    SearchCondition.EQUAL, lid.longValue());
            mainSelect.appendWhere(sc);
            if (j != vall.size() - 1) {
                mainSelect.appendOr();
            }
        }
        mainSelect.appendCloseParen();
        QueryResult qr = PersistenceHelper.manager.find(mainSelect);
        java.util.HashMap map = new java.util.HashMap();
        int i = 0;
        while (qr.hasMoreElements()) {
            Object[] obj = new Object[3];
            obj = (Object[]) qr.nextElement();
            if (obj[1].toString().equals(ibaname)) {
                // s[i][0] = obj[0].toString();
                // s[i][1] = obj[1].toString();
                // s[i][2] = obj[2] != null ? obj[2].toString() : "";
                if (obj[2] != null) {
                    map.put(obj[0].toString(), obj[2].toString());
                }
                i++;
            }
        }
        return map;
    }

    public static Map getBulkIBAString(List vall, String ibaname, String ibavalue) throws WTException,
            WTPropertyVetoException {
        QuerySpec mainSelect = new QuerySpec();
        mainSelect.getFromClause().setAliasPrefix("A");
        Class stringValueClass = wt.iba.value.StringValue.class;
        Class stringDefClass = wt.iba.definition.StringDefinition.class;
        int stringValueIndex = mainSelect.appendClassList(stringValueClass, false);
        int stringDefIndex = mainSelect.appendClassList(stringDefClass, false);
        ClassAttribute stringDefAttr = new ClassAttribute(stringDefClass, wt.iba.definition.StringDefinition.NAME);
        ClassAttribute stringValueAttr = new ClassAttribute(stringValueClass, wt.iba.value.StringValue.VALUE2);
        ClassAttribute stringIdAttr = new ClassAttribute(stringValueClass, "theIBAHolderReference.key.id");
        mainSelect.appendSelect(stringIdAttr, stringValueIndex, false);
        mainSelect.appendSelect(stringDefAttr, stringDefIndex, false);
        mainSelect.appendSelect(stringValueAttr, stringValueIndex, false);
        SearchCondition join = new SearchCondition(stringValueClass, "definitionReference.key.id", stringDefClass,
                WTAttributeNameIfc.ID_NAME);
        mainSelect.appendWhere(join, stringValueIndex, stringDefIndex);
        mainSelect.appendAnd();
        mainSelect.appendOpenParen();
        for (int j = 0; j < vall.size(); j++) {
            if (!(vall.get(j) instanceof Persistable)) {
                continue;
            }
            Long lid = Long.valueOf(((Persistable) (vall.get(j))).getPersistInfo().getObjectIdentifier().getId());
            SearchCondition sc = new SearchCondition(stringValueClass, "theIBAHolderReference.key.id",
                    SearchCondition.EQUAL, lid.longValue());
            mainSelect.appendWhere(sc);
            if (j != vall.size() - 1) {
                mainSelect.appendOr();
            }
        }
        mainSelect.appendCloseParen();
        QueryResult qr = PersistenceHelper.manager.find(mainSelect);
        java.util.HashMap map = new java.util.HashMap();
        int i = 0;
        while (qr.hasMoreElements()) {
            Object[] obj = new Object[3];
            obj = (Object[]) qr.nextElement();
            if (ibaname.equals(obj[1].toString())) {
                // s[i][0] = obj[0].toString();
                // s[i][1] = obj[1].toString();
                // s[i][2] = obj[2] != null ? obj[2].toString() : "";
                if (obj[2] != null && ibavalue.equals(obj[2])) {
                    map.put(obj[0].toString(), obj[2].toString());
                }
                i++;
            }
        }
        return map;
    }

    public static String[][] getBulkIBAString(List vall) throws WTException, WTPropertyVetoException {
        QuerySpec mainSelect = new QuerySpec();
        mainSelect.getFromClause().setAliasPrefix("A");
        Class stringValueClass = wt.iba.value.StringValue.class;
        Class stringDefClass = wt.iba.definition.StringDefinition.class;
        int stringValueIndex = mainSelect.appendClassList(stringValueClass, false);
        int stringDefIndex = mainSelect.appendClassList(stringDefClass, false);
        ClassAttribute stringDefAttr = new ClassAttribute(stringDefClass, wt.iba.definition.StringDefinition.NAME);
        ClassAttribute stringValueAttr = new ClassAttribute(stringValueClass, wt.iba.value.StringValue.VALUE2);
        ClassAttribute stringIdAttr = new ClassAttribute(stringValueClass, "theIBAHolderReference.key.id");
        mainSelect.appendSelect(stringIdAttr, stringValueIndex, false);
        mainSelect.appendSelect(stringDefAttr, stringDefIndex, false);
        mainSelect.appendSelect(stringValueAttr, stringValueIndex, false);
        SearchCondition join = new SearchCondition(stringValueClass, "definitionReference.key.id", stringDefClass,
                WTAttributeNameIfc.ID_NAME);
        mainSelect.appendWhere(join, stringValueIndex, stringDefIndex);
        mainSelect.appendAnd();
        mainSelect.appendOpenParen();
        for (int j = 0; j < vall.size(); j++) {
            if (!(vall.get(j) instanceof Persistable)) {
                continue;
            }
            Long lid = Long.valueOf(((Persistable) (vall.get(j))).getPersistInfo().getObjectIdentifier().getId());
            SearchCondition sc = new SearchCondition(stringValueClass, "theIBAHolderReference.key.id",
                    SearchCondition.EQUAL, lid.longValue());
            mainSelect.appendWhere(sc);
            if (j != vall.size() - 1) {
                mainSelect.appendOr();
            }
        }
        mainSelect.appendCloseParen();
        QueryResult qr = PersistenceHelper.manager.find(mainSelect);
        String[][] s = new String[qr.size()][3];
        int i = 0;
        while (qr.hasMoreElements()) {
            Object[] obj = new Object[3];
            obj = (Object[]) qr.nextElement();
            s[i][0] = obj[0].toString();
            s[i][1] = obj[1].toString();
            s[i][2] = obj[2] != null ? obj[2].toString() : "";
            i++;
        }
        return s;
    }

    /**
     * copy all the attribute from original object to the copy object except the iba in the HashMap
     * 
     * @param original
     * @param copy
     * @param map
     *            : which IBAs must changed
     * @throws RemoteException
     * @throws WTPropertyVetoException 
     * @throws Exception
     */
    public static void saveIBAValues(IBAHolder original, IBAHolder copy, Map map) throws WTException, RemoteException, WTPropertyVetoException {
        WTCollection holders = new WTArrayList();
        holders.add(original);
        holders = IBAValueHelper.service.refreshAttributeContainer(holders);
        original = (WTPart) holders.persistableIterator().next();
        DefaultAttributeContainer copyContainer = new DefaultAttributeContainer();
        DefaultAttributeContainer originalContainer = (DefaultAttributeContainer) original.getAttributeContainer();
        AbstractValueView[] avv = (copy.getAttributeContainer() != null ? DefaultAttributeContainerHelper
                .getUnclonedAttributeValues((DefaultAttributeContainer) copy.getAttributeContainer())
                : DefaultAttributeContainerHelper.getUnclonedAttributeValues(originalContainer));

        AttributeDefDefaultView[] orignalDefs = originalContainer.getAttributeDefinitions();
        for (int i = 0; orignalDefs != null && i < orignalDefs.length; i++) {
            AttributeDefDefaultView orignalDef = orignalDefs[i];
            String ibaName = orignalDef.getName();
            // System.out.println("ibaName = " + ibaName);
            String ibaValue = "";
            if (map.get(ibaName) != null) {
                ibaValue = (String) map.get(ibaName);
                AttributeDefDefaultView attributedefdefaultview = IBADefinitionHelper.service
                        .getAttributeDefDefaultViewByPath(ibaName);
                if (attributedefdefaultview != null) {

                    AbstractValueView aabstractvalueview[] = copyContainer.getAttributeValues(attributedefdefaultview);
                    // System.out.println("copy container value length= " + aabstractvalueview.length);
                    if (aabstractvalueview == null || aabstractvalueview.length == 0) {
                        AbstractValueView abstractValueView = createAbstractValueView(ibaName, ibaValue);
                        if (abstractValueView != null) {
                            copyContainer.addAttributeValue(abstractValueView);
                        }
                    } else if (aabstractvalueview.length == 1) {
                        try {
                            AbstractValueView abstractValueView = createAbstractValueView(ibaName, ibaValue);
                            aabstractvalueview[0] = cloneAbstractValueView(abstractValueView, aabstractvalueview[0]);

                        } catch (IBAValueException ibavalueexception) {
                            throw ibavalueexception;
                        }
                        copyContainer.updateAttributeValue(aabstractvalueview[0]);
                    } else {
                        AbstractValueView abstractValueView = createAbstractValueView(ibaName, ibaValue);
                        copyContainer.addAttributeValue(abstractValueView);
                    }

                }

            } else {
                // System.out.println("clone the ibaValue from original");
                // clone the IBA from original to copy
                AbstractValueView[] abstractValueViews = originalContainer.getAttributeValues(orignalDef);
                if (abstractValueViews != null && abstractValueViews.length == 1) {
                    copyContainer.addAttributeValue(abstractValueViews[0]);
                }

            }

            copy.setAttributeContainer(copyContainer);
        }

    }

    public static void createOrUpdateIBAValues(IBAHolder holder, Map map) throws WTException, WTPropertyVetoException,
            RemoteException {
        Object EDIT_IBAS = new wt.epm.attributes.EPMIBAConstraintFactory.EditFileBasedAttributes();

        DefaultAttributeContainer container = (DefaultAttributeContainer) holder.getAttributeContainer();
        if (container == null) {
            container = new DefaultAttributeContainer();
            holder.setAttributeContainer(container);
        }
        container.setConstraintParameter(EDIT_IBAS);

        Locale locale = WTContext.getContext().getLocale();
        holder = IBAValueHelper.service.refreshAttributeContainer(holder, EDIT_IBAS, locale, null);
        holder = IBAValueHelper.service.refreshAttributeContainerWithoutConstraints(holder);
        // System.out.println("holder == " + holder);
        DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) holder
                .getAttributeContainer();
        // delete all the iba before update
        AbstractValueView oldAbstractValueViews[] = defaultattributecontainer.getAttributeValues();
        for (int i = 0; i < oldAbstractValueViews.length; i++) {
            AbstractValueView oldAbstractValueView = oldAbstractValueViews[i];
            // System.out.println("delete attribute");
            defaultattributecontainer.deleteAttributeValue(oldAbstractValueView);
        }

        Iterator keys = map.keySet().iterator();
        while (keys.hasNext()) {
            String ibaName = (String) keys.next();
            String ibaValues = (String) map.get(ibaName);
            String[] ibaValue = ibaValues.split(",");

            wt.iba.definition.litedefinition.AttributeDefDefaultView attributedefdefaultview = IBADefinitionHelper.service
                    .getAttributeDefDefaultViewByPath(ibaName);
            if (attributedefdefaultview != null) {

                AbstractValueView aabstractvalueview[] = defaultattributecontainer
                        .getAttributeValues(attributedefdefaultview);
                if (aabstractvalueview == null || aabstractvalueview.length == 0) {
                    for (int i = 0; i < ibaValue.length; i++) {
                        String signalIbaValue = ibaValue[i];
                        AbstractValueView abstractValueView = createAbstractValueView(ibaName, signalIbaValue);
                        if (abstractValueView != null) {
                            defaultattributecontainer.addAttributeValue(abstractValueView);
                        }
                    }
                } else if (aabstractvalueview.length == 1) {
                    for (int i = 0; i < ibaValue.length; i++) {
                        String signalIbaValue = ibaValue[i];
                        AbstractValueView abstractValueView = createAbstractValueView(ibaName, signalIbaValue);
                        aabstractvalueview[0] = cloneAbstractValueView(abstractValueView, aabstractvalueview[0]);
                    }
                    defaultattributecontainer.updateAttributeValue(aabstractvalueview[0]);
                } else {
                    for (int i = 0; i < ibaValue.length; i++) {
                        String signalIbaValue = ibaValue[i];
                        AbstractValueView abstractValueView = createAbstractValueView(ibaName, signalIbaValue);
                        defaultattributecontainer.addAttributeValue(abstractValueView);
                    }
                }
                holder.setAttributeContainer(defaultattributecontainer);
            }
        }
        // update the iba value, contain the default attributes
        LoadValue.applySoftAttributes(holder);
    }

    /**
     * @param ibaName
     * @param ibaValue
     * @return
     */
    private static AbstractValueView createAbstractValueView(String ibaName, String ibaValue) {
        AbstractValueView abstractValueView = null;
        AbstractAttributeDefinizerView abstractattributedefinizerview = LoadValue.getCachedAttributeDefinition(ibaName);
        if ("Part".equals(ibaName)) {
            abstractValueView = internalCreateValue(abstractattributedefinizerview,
                    "wt.csm.navigation.ClassificationNode", ibaValue);
        } else {
            abstractValueView = internalCreateValue(abstractattributedefinizerview, ibaValue, "-1");
        }
        return abstractValueView;
    }

    public static AbstractValueView cloneAbstractValueView(AbstractValueView abstractvalueview,
            AbstractValueView abstractvalueview1) throws IBAValueException, WTPropertyVetoException {
        if (abstractvalueview instanceof AbstractContextualValueDefaultView) {
            if ((abstractvalueview instanceof BooleanValueDefaultView)
                    && (abstractvalueview1 instanceof BooleanValueDefaultView)) {
                ((BooleanValueDefaultView) abstractvalueview1).setValue(((BooleanValueDefaultView) abstractvalueview)
                        .isValue());
            } else if ((abstractvalueview instanceof FloatValueDefaultView)
                    && (abstractvalueview1 instanceof FloatValueDefaultView)) {
                ((FloatValueDefaultView) abstractvalueview1).setValue(((FloatValueDefaultView) abstractvalueview)
                        .getValue());
                ((FloatValueDefaultView) abstractvalueview1).setPrecision(((FloatValueDefaultView) abstractvalueview)
                        .getPrecision());
            } else if ((abstractvalueview instanceof IntegerValueDefaultView)
                    && (abstractvalueview1 instanceof IntegerValueDefaultView)) {
                ((IntegerValueDefaultView) abstractvalueview1).setValue(((IntegerValueDefaultView) abstractvalueview)
                        .getValue());
            } else if ((abstractvalueview instanceof RatioValueDefaultView)
                    && (abstractvalueview1 instanceof RatioValueDefaultView)) {
                ((RatioValueDefaultView) abstractvalueview1).setValue(((RatioValueDefaultView) abstractvalueview)
                        .getValue());
                ((RatioValueDefaultView) abstractvalueview1).setDenominator(((RatioValueDefaultView) abstractvalueview)
                        .getDenominator());
            } else if ((abstractvalueview instanceof StringValueDefaultView)
                    && (abstractvalueview1 instanceof StringValueDefaultView)) {
                ((StringValueDefaultView) abstractvalueview1).setValue(((StringValueDefaultView) abstractvalueview)
                        .getValue());
            } else if ((abstractvalueview instanceof TimestampValueDefaultView)
                    && (abstractvalueview1 instanceof TimestampValueDefaultView)) {
                ((TimestampValueDefaultView) abstractvalueview1)
                        .setValue(((TimestampValueDefaultView) abstractvalueview).getValue());
            } else if ((abstractvalueview instanceof UnitValueDefaultView)
                    && (abstractvalueview1 instanceof UnitValueDefaultView)) {
                ((UnitValueDefaultView) abstractvalueview1).setValue(((UnitValueDefaultView) abstractvalueview)
                        .getValue());
                ((UnitValueDefaultView) abstractvalueview1).setPrecision(((UnitValueDefaultView) abstractvalueview)
                        .getPrecision());
            } else if ((abstractvalueview instanceof URLValueDefaultView)
                    && (abstractvalueview1 instanceof URLValueDefaultView)) {
                ((URLValueDefaultView) abstractvalueview1).setValue(((URLValueDefaultView) abstractvalueview)
                        .getValue());
                ((URLValueDefaultView) abstractvalueview1).setDescription(((URLValueDefaultView) abstractvalueview)
                        .getDescription());
            } else {
                throw new IBAValueException(
                        "Unknown subclass of AbstractContextualValueDefaultView encountered, please update wt.part.LoadPart");
            }
        } else if ((abstractvalueview instanceof ReferenceValueDefaultView)
                && (abstractvalueview1 instanceof ReferenceValueDefaultView)) {
            ((ReferenceValueDefaultView) abstractvalueview1)
                    .setReferenceDefinition(((ReferenceValueDefaultView) abstractvalueview).getReferenceDefinition());
            ((ReferenceValueDefaultView) abstractvalueview1)
                    .setLiteIBAReferenceable(((ReferenceValueDefaultView) abstractvalueview).getLiteIBAReferenceable());
        } else {
            throw new IBAValueException(
                    "Unknown subclass of AbstractValueView encountered, please update wt.part.LoadPart");
        }
        return abstractvalueview1;
    }
    
}
