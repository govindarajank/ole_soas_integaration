package org.kuali.ole.module.purap.aggresoInvoice;

import org.kuali.ole.OLEConstants;
import org.kuali.ole.docstore.common.util.DataSource;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.coreservice.api.CoreServiceApiServiceLocator;
import org.kuali.rice.coreservice.api.parameter.Parameter;
import org.kuali.rice.coreservice.api.parameter.ParameterKey;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.KRADServiceLocator;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by govindarajank on 5/9/17.
 */
public class InvoiceFileRecordProcessor{

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
        record.replace(pos - 1, pos + length, String.format("%-" + length + "s",s));
    }

    public BusinessObjectService getBusinessObjectService() {
        if (null == businessObjectService) {
            businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return businessObjectService;
    }

    public Timestamp getPreviousRunDate(){
        String parameter = getParameterAgresso(OLEConstants.AgressoCreateFile.LASTBATCHJOBRUNDATE);
        if(parameter!=null){
            SimpleDateFormat datetimeFormatter1 = new SimpleDateFormat(
                    "yyyy-MM-dd hh:mm:ss");
            try {
                Date lastRunDate = datetimeFormatter1.parse(parameter);
                return new Timestamp(lastRunDate.getTime());
            } catch (ParseException e) {
                //Do Nothing
            }

        }
        return null;
    }

    public List<String> getInvoiceDocumentIds(){
        String previousDate = "";
        Timestamp previousRunDateTime = getPreviousRunDate();
        if(previousRunDateTime != null){
            previousDate = previousRunDateTime.toString();
        }
        else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -1);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            previousDate = getFormatedDate(calendar.getTime());
        }
        List<String> invoiceIdList = new ArrayList<>();
        String currentDate = getFormatedDate(new Date());
        ResultSet invoiceIdResultSet=null;
        String query = "SELECT D.DOC_HDR_ID FROM KREW_DOC_HDR_T D,KREW_DOC_TYP_T T WHERE D.DOC_TYP_ID = T.DOC_TYP_ID AND D.DOC_HDR_STAT_CD = 'F' AND T.DOC_TYP_NM='OLE_PRQS' AND (D.CRTE_DT BETWEEN '"+previousDate+"' AND '"+currentDate+"')";
        try {
            PreparedStatement statement = getConnection().prepareStatement(query);
            invoiceIdResultSet = statement.executeQuery(query);
            while (invoiceIdResultSet.next()) {
                invoiceIdList.add(invoiceIdResultSet.getString(1));
            }
            statement.close();
            getConnection().close();
            return invoiceIdList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String getFormatedDate(Date lastExportDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(lastExportDate);
    }

    private Connection getConnection() throws SQLException {
        DataSource dataSource = null;
        try {
            dataSource = DataSource.getInstance();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
           e.printStackTrace();
        } catch (PropertyVetoException e) {
           e.printStackTrace();
        }
        return dataSource.getConnection();
    }

    public String toString() {
        return record.toString();
    }

    public String getParameterAgresso(String name) {
        ParameterKey parameterKey = ParameterKey.create(OLEConstants.APPL_ID, OLEConstants.SELECT_NMSPC, OLEConstants.SELECT_CMPNT,name);
        Parameter parameter = CoreServiceApiServiceLocator.getParameterRepositoryService().getParameter(parameterKey);
        if(parameter==null){
            parameterKey = ParameterKey.create(OLEConstants.APPL_ID_OLE, OLEConstants.SELECT_NMSPC, OLEConstants.SELECT_CMPNT,name);
            parameter = CoreServiceApiServiceLocator.getParameterRepositoryService().getParameter(parameterKey);
        }
        return parameter!=null?parameter.getValue():null;
    }

    public void updateParameterforNextRunTimeAgresso() {
        parameterService = (ParameterService) SpringContext.getService("parameterService");
        ParameterKey parameterKey = ParameterKey.create(OLEConstants.APPL_ID, OLEConstants.SELECT_NMSPC, OLEConstants.SELECT_CMPNT, OLEConstants.AgressoCreateFile.LASTBATCHJOBRUNDATE);
        Parameter parameter = CoreServiceApiServiceLocator.getParameterRepositoryService().getParameter(parameterKey);
        if ( parameter != null ) {
            Parameter.Builder updatedParameter = Parameter.Builder.create(parameter);
            updatedParameter.setValue(new Timestamp(new Date().getTime()).toString());
            parameterService.updateParameter(updatedParameter.build());
        }
    }

    public List<String> isDocumentFinal(List<String> documentNbr){
        List<String> documentNbrs = new ArrayList<>();
        String document = "";
        String prefix = "";
        for(String doc : documentNbr) {
            document = document +prefix+ doc ;
            prefix =",";
        }
        ResultSet invoiceIdResultSet=null;
        String query = "SELECT D.DOC_HDR_ID FROM KREW_DOC_HDR_T D WHERE D.DOC_HDR_STAT_CD = 'F' AND D.DOC_HDR_ID in("+document+");";
        try {
            PreparedStatement statement = getConnection().prepareStatement(query);
            invoiceIdResultSet = statement.executeQuery(query);
            while (invoiceIdResultSet.next()) {
                documentNbrs.add(invoiceIdResultSet.getString(1));
            }
            statement.close();
            getConnection().close();
            return documentNbrs;
        }catch (Exception e){
            e.printStackTrace();
            return documentNbrs;
        }
    }
}
