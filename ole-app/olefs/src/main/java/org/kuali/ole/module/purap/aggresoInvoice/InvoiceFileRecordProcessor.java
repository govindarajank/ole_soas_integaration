package org.kuali.ole.module.purap.aggresoInvoice;

import org.kuali.ole.OLEConstants;
import org.kuali.ole.deliver.service.ParameterValueResolver;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.coreservice.api.CoreServiceApiServiceLocator;
import org.kuali.rice.coreservice.api.parameter.Parameter;
import org.kuali.rice.coreservice.api.parameter.ParameterKey;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.service.BusinessObjectService;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by govindarajank on 5/9/17.
 */
public class InvoiceFileRecordProcessor {

    final StringBuilder record;
    private BusinessObjectService businessObjectService;
    protected ParameterService parameterService;

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public InvoiceFileRecordProcessor(int recordSize) {

        record = new StringBuilder(recordSize);
        record.setLength(recordSize);
    }

    public void setField(int pos, int length, String s) {
        record.replace(pos - 1, pos + length, String.format("%-" + length + "s", s));
    }

    /*public BusinessObjectService getBusinessObjectService() {
        if (null == businessObjectService) {
            businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return businessObjectService;
    }*/

    public Timestamp getPreviousRunDate() {
        String previousRunDateTime = ParameterValueResolver.getInstance().getParameter(OLEConstants
                .APPL_ID_OLE, OLEConstants.SELECT_NMSPC, OLEConstants.SELECT_CMPNT, OLEConstants.AgressoCreateFile.LASTBATCHJOBRUNDATE);
        if (previousRunDateTime != null) {
            SimpleDateFormat datetimeFormatter1 = new SimpleDateFormat(
                    "yyyy-MM-dd hh:mm:ss");
            try {
                Date lastRunDate = datetimeFormatter1.parse(previousRunDateTime);
                return new Timestamp(lastRunDate.getTime());
            } catch (ParseException e) {
                //Do Nothing
            }

        }
        return null;
    }

    public String toString() {
        return record.toString();
    }

    public void updateParameterforNextRunTimeAgresso(String lastRunDate) {
        parameterService = (ParameterService) SpringContext.getService("parameterService");
        ParameterKey parameterKey = ParameterKey.create(OLEConstants.APPL_ID, OLEConstants.SELECT_NMSPC, OLEConstants.SELECT_CMPNT, OLEConstants.AgressoCreateFile.LASTBATCHJOBRUNDATE);
        Parameter parameter = CoreServiceApiServiceLocator.getParameterRepositoryService().getParameter(parameterKey);
        if (parameter != null) {
            Parameter.Builder updatedParameter = Parameter.Builder.create(parameter);
            updatedParameter.setValue(lastRunDate);
            parameterService.updateParameter(updatedParameter.build());
        }
    }
}
