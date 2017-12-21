package org.kuali.ole.select.businessobject.options;

import org.kuali.ole.OLEConstants;
import org.kuali.ole.describe.bo.OleItemAvailableStatus;
import org.kuali.ole.select.OleSelectConstant;
import org.kuali.ole.select.businessobject.OleItemPriceSource;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.coreservice.api.CoreServiceApiServiceLocator;
import org.kuali.rice.coreservice.api.parameter.Parameter;
import org.kuali.rice.coreservice.api.parameter.ParameterKey;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.krad.service.KRADServiceLocator;

import java.util.*;
import java.util.prefs.Preferences;

/**
 * ItemStatus used to render the values for ItemStatus dropdown control.
 */
public class OleItemStatusKeyValuesFinder extends KeyValuesBase {
    /**
     * This method returns the List of  ConcreteKeyValue,
     * ConcreteKeyValue has two arguments itemAvailableStatusCode and
     * itemAvailableStatusName.
     * @return   List<KeyValue>
     */
    @Override
    public List<KeyValue> getKeyValues() {

        List<KeyValue> options = new ArrayList<KeyValue>();
        List<KeyValue> optionsList = new ArrayList<KeyValue>();
        Collection<OleItemAvailableStatus> oleItemAvailableStatuses = KRADServiceLocator.getBusinessObjectService().findAll(OleItemAvailableStatus.class);
        options.add(new ConcreteKeyValue("ONORDER", "On Order"));
        String excludeItemStatus = getParameter(OLEConstants.EXCLUDE_ITEM_STATUS);
        Map<String,String> map = new HashMap<>();
        if(excludeItemStatus!=null && !excludeItemStatus.isEmpty()){
            String[] itemStatusList = excludeItemStatus.split(",");
            for(String itemStatus : itemStatusList){
                map.put(itemStatus,itemStatus);
            }
        }
        for (OleItemAvailableStatus type : oleItemAvailableStatuses) {
            if (type.isActive() && !map.containsKey(type.getItemAvailableStatusCode())) {
                if(!type.getItemAvailableStatusCode().equalsIgnoreCase("ONORDER")){
                    options.add(new ConcreteKeyValue(type.getItemAvailableStatusCode(), type.getItemAvailableStatusName()));
                }
            }
        }
        return options;
    }

    public String getParameter(String name){
        ParameterKey parameterKey = ParameterKey.create(OLEConstants.APPL_ID, OLEConstants.SELECT_NMSPC, OLEConstants.SELECT_CMPNT,name);
        Parameter parameter = CoreServiceApiServiceLocator.getParameterRepositoryService().getParameter(parameterKey);
        return parameter!=null?parameter.getValue():null;
    }
}
