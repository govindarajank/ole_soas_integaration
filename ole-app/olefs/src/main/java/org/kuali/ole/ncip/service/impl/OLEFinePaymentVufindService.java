package org.kuali.ole.ncip.service.impl;

import org.apache.log4j.Logger;
import org.kuali.asr.handler.FinePaymentResponseHandler;
import org.kuali.asr.handler.ResponseHandler;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.deliver.bo.*;
import org.kuali.ole.deliver.service.OleLoanDocumentDaoOjb;
import org.kuali.ole.ncip.bo.OLENCIPConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.web.format.CurrencyFormatter;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.KRADServiceLocator;

import java.sql.Timestamp;
import java.util.*;

/**
 * Created by govindarajank on 18/12/15.
 */
public abstract class OLEFinePaymentVufindService {
    private static final Logger LOG = Logger.getLogger(OLEFinePaymentVufindService.class);

    protected String responseFormatType;
    protected String response;

    private ResponseHandler responseHandler;

    public ResponseHandler getResponseHandler() {
        if (null == responseHandler) {
            responseHandler = new FinePaymentResponseHandler();
        }
        return responseHandler;
    }

    public void setResponseHandler(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    public OlePaymentStatus getPaymentStatus(String paymentStatus) {
        LOG.debug("Inside the getPaymentStatus method");
        Map statusMap = new HashMap();
        statusMap.put("paymentStatusCode", paymentStatus);
        List<OlePaymentStatus> olePaymentStatusList = (List<OlePaymentStatus>) KRADServiceLocator.getBusinessObjectService().findMatching(OlePaymentStatus.class, statusMap);
        return olePaymentStatusList != null && olePaymentStatusList.size() > 0 ? olePaymentStatusList.get(0) : null;
    }

    private BusinessObjectService businessObjectService;

    private BusinessObjectService getBusinessObjectService() {
        if (null == businessObjectService) {
            businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }


    public String finePayment(Map finePaidParameters) {
        String operatorId = (String) finePaidParameters.get(OLENCIPConstants.OPERATOR_ID);
        String patronBarcode = (String) finePaidParameters.get(OLENCIPConstants.PATRON_BARCODE);
        List<String> billIDs = (List<String>) finePaidParameters.get(OLENCIPConstants.BILL_IDS);
        String amountPays = (String)(finePaidParameters.get(OLENCIPConstants.AMOUNT_PAID));
        KualiDecimal amountPaid = new KualiDecimal(amountPays);
        String paymentType = (String) finePaidParameters.get(OLENCIPConstants.PAYMENT_TYPE);
        String fineType = (String) finePaidParameters.get(OLENCIPConstants.FINE_TYPE);
        String transcationReference = (String) finePaidParameters.get(OLENCIPConstants.TRANSCATION_REFERENCE);

        responseFormatType = (String) finePaidParameters.get(OLENCIPConstants.RESPONSE_FORMAT_TYPE);
        if (responseFormatType == null) {
            responseFormatType = "xml";
        }
        responseFormatType = responseFormatType.toUpperCase();
        List<FeeType> feeTypes = new ArrayList<FeeType>();
        String patronId = "";
        String feeTypeId = "";
        Map barMap = new HashMap();
        barMap.put(OLEConstants.OlePatron.BARCODE, patronBarcode);
        List<OlePatronDocument> patronDocumentList = (List<OlePatronDocument>) KRADServiceLocator.getBusinessObjectService().findMatching(OlePatronDocument.class, barMap);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(patronDocumentList)) {
            patronId = patronDocumentList.get(0).getOlePatronId();
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(patronId)) {
            List<PatronBillPayment> patronBills = (List<PatronBillPayment>) ((OleLoanDocumentDaoOjb) SpringContext.getBean("oleLoanDao")).getPatronBills(patronId);
            String forgiveErrorNote = "";
            String transactionNote = "";
            if(operatorId.equalsIgnoreCase(OLEConstants.VUFIND)) {
                for (int lengthOfBill = 0; lengthOfBill < billIDs.size(); lengthOfBill++) {
                    feeTypeId = billIDs.get(lengthOfBill);
                    for (PatronBillPayment olPatronBillPayment : patronBills) {
                        for (FeeType feeType : olPatronBillPayment.getFeeType()) {
                            if (org.apache.commons.lang.StringUtils.isNotBlank(feeTypeId)) {
                                if (feeType.getId().equalsIgnoreCase(feeTypeId)) {
                                    feeTypes.add(feeType);
                                }
                            } else {
                                if (feeType.getOleFeeType().getFeeTypeCode().equalsIgnoreCase(paymentType)) {
                                    feeTypes.add(feeType);
                                }
                            }
                        }
                    }
                }
            }else{
                for (PatronBillPayment olPatronBillPayment : patronBills) {
                    for (FeeType feeType : olPatronBillPayment.getFeeType()) {
                        if (org.apache.commons.lang.StringUtils.isNotBlank(feeTypeId)) {
                            if (feeType.getBillNumber().equalsIgnoreCase(feeTypeId) && feeType.getOleFeeType().getFeeTypeCode().equalsIgnoreCase(fineType)) {
                                feeTypes.add(feeType);
                            }
                        } else {
                            if (feeType.getOleFeeType().getFeeTypeCode().equalsIgnoreCase(fineType)) {
                                feeTypes.add(feeType);
                            }
                        }
                    }
                }
            }
            List<OleItemLevelBillPayment> oleItemLevelBillPaymentList = new ArrayList<OleItemLevelBillPayment>();
            for (FeeType feeType : feeTypes) {
                 if (org.apache.commons.collections.CollectionUtils.isNotEmpty(feeType.getItemLevelBillPaymentList())) {
                        oleItemLevelBillPaymentList.addAll(feeType.getItemLevelBillPaymentList());
                 }
            }

            KualiDecimal unPaidAmount = itemWiseFeePaid(feeTypes, amountPaid, paymentType, OLEConstants.FULL_PAID, OLEConstants.PAR_PAID, forgiveErrorNote,
                        transcationReference, transactionNote, fineType, oleItemLevelBillPaymentList, operatorId);
            if(unPaidAmount==amountPaid){
                LOG.info("unPaidAmount : " + unPaidAmount);
                response = "Amount not paid";
            } else {
                LOG.info("unPaidAmount : " + unPaidAmount);
                Locale locale = Locale.UK;
                Currency curr = Currency.getInstance(locale);
                String symbol = curr.getSymbol(locale);
                response = symbol+amountPaid+" Paid successfully";
            }
        }
        return prepareResponse(response);
    }

    public KualiDecimal itemWiseFeePaid(List<FeeType> feeTypes, KualiDecimal paymentAmount, String paymentMethod,
                                        String fullyPaidStatus, String partiallyPaidStatus, String forgiveErrorNote,
                                        String transactionNumber, String transactionNote, String fineType, List<OleItemLevelBillPayment> currentSessionTransactions,String operatorId) {
        KualiDecimal payAmt = paymentAmount;
        LOG.debug("Inside itemWiseFeePaid");
        KualiDecimal unPaidAmount;
        for (FeeType feeType : feeTypes) {
            if (((feeType.getPaymentStatusCode().equalsIgnoreCase(OLEConstants.PAY_OUTSTANDING) || feeType.getPaymentStatusCode().equalsIgnoreCase(OLEConstants.PAY_PARTIALLY))
                    && paymentAmount.compareTo(OLEConstants.KUALI_BIGDECIMAL_DEF_VALUE) > 0)) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("billNumber", feeType.getBillNumber());
                PatronBillPayment patronBillPayment = getBusinessObjectService().findByPrimaryKey(PatronBillPayment.class, map);
                List<FeeType> patronFeeTypes = patronBillPayment.getFeeType();
                for (FeeType patronFeeType : patronFeeTypes) {
                    if (patronFeeType.getId().equalsIgnoreCase(feeType.getId())) {
                        if (paymentAmount.compareTo(patronFeeType.getBalFeeAmount()) >= 0) {
                            unPaidAmount = patronBillPayment.getUnPaidBalance().subtract(feeType.getBalFeeAmount());
                            paymentAmount = paymentAmount.subtract(patronFeeType.getBalFeeAmount());
                            OlePaymentStatus olePaymentStatus = getPaymentStatus(fullyPaidStatus);
                            patronFeeType.setOlePaymentStatus(olePaymentStatus);
                            patronFeeType.setPaymentStatus(olePaymentStatus.getPaymentStatusId());
                            List<OleItemLevelBillPayment> oleItemLevelBillPayments = patronFeeType.getItemLevelBillPaymentList() != null && patronFeeType.getItemLevelBillPaymentList().size() > 0
                                    ? patronFeeType.getItemLevelBillPaymentList() : new ArrayList<OleItemLevelBillPayment>();
                            OleItemLevelBillPayment oleItemLevelBillPayment = new OleItemLevelBillPayment();
                            oleItemLevelBillPayment.setPaymentDate(new Timestamp(System.currentTimeMillis()));
                            oleItemLevelBillPayment.setAmount(patronFeeType.getBalFeeAmount());
                            oleItemLevelBillPayment.setCreatedUser(operatorId);
                            oleItemLevelBillPayment.setTransactionNote(transactionNote);
                            oleItemLevelBillPayment.setTransactionNumber(transactionNumber);
                            oleItemLevelBillPayment.setPaymentMode(paymentMethod);
                            oleItemLevelBillPayments.add(oleItemLevelBillPayment);
                            currentSessionTransactions.add(oleItemLevelBillPayment);
                            patronFeeType.setBalFeeAmount(OLEConstants.KUALI_BIGDECIMAL_DEF_VALUE);
                            patronFeeType.setItemLevelBillPaymentList(oleItemLevelBillPayments);
                            if (paymentMethod != null && paymentMethod.equalsIgnoreCase(OLEConstants.ERROR)) {
                                patronFeeType.setErrorNote(forgiveErrorNote);
                            }
                            if (paymentMethod != null && paymentMethod.equalsIgnoreCase(OLEConstants.FORGIVE)) {
                                patronFeeType.setForgiveNote(forgiveErrorNote);
                            }
                            feeType.setActiveItem(false);
                            patronBillPayment.setPaymentAmount(payAmt.subtract(paymentAmount));
                            payAmt = paymentAmount;
                        } else {
                            unPaidAmount = patronBillPayment.getUnPaidBalance().subtract(paymentAmount);
                            KualiDecimal updatedFeeAmount = patronFeeType.getBalFeeAmount().subtract(paymentAmount);
                            KualiDecimal transAmount = paymentAmount;
                            paymentAmount = paymentAmount.subtract(patronFeeType.getBalFeeAmount());
                            OlePaymentStatus olePaymentStatus = getPaymentStatus(partiallyPaidStatus);
                            patronFeeType.setOlePaymentStatus(olePaymentStatus);
                            patronFeeType.setPaymentStatus(olePaymentStatus.getPaymentStatusId());
                            List<OleItemLevelBillPayment> oleItemLevelBillPayments = patronFeeType.getItemLevelBillPaymentList() != null && patronFeeType.getItemLevelBillPaymentList().size() > 0
                                    ? patronFeeType.getItemLevelBillPaymentList() : new ArrayList<OleItemLevelBillPayment>();
                            OleItemLevelBillPayment oleItemLevelBillPayment = new OleItemLevelBillPayment();
                            oleItemLevelBillPayment.setPaymentDate(new Timestamp(System.currentTimeMillis()));
                            oleItemLevelBillPayment.setAmount(transAmount.compareTo(OLEConstants.KUALI_BIGDECIMAL_DEF_VALUE) < 0 ? transAmount : transAmount);
                            oleItemLevelBillPayment.setCreatedUser(operatorId);
                            oleItemLevelBillPayment.setTransactionNote(transactionNote);
                            oleItemLevelBillPayment.setTransactionNumber(transactionNumber);
                            oleItemLevelBillPayment.setPaymentMode(paymentMethod);
                            oleItemLevelBillPayments.add(oleItemLevelBillPayment);
                            currentSessionTransactions.add(oleItemLevelBillPayment);
                            patronFeeType.setBalFeeAmount(updatedFeeAmount);
                            patronFeeType.setItemLevelBillPaymentList(oleItemLevelBillPayments);
                            if (paymentMethod != null && paymentMethod.equalsIgnoreCase(OLEConstants.ERROR)) {
                                patronFeeType.setErrorNote(forgiveErrorNote);
                            }
                            if (paymentMethod != null && paymentMethod.equalsIgnoreCase(OLEConstants.FORGIVE)) {
                                patronFeeType.setForgiveNote(forgiveErrorNote);
                            }
                            patronBillPayment.setPaymentAmount(payAmt);
                        }
                        //feeType = patronFeeType;
                        patronBillPayment.setUnPaidBalance(unPaidAmount);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("unPaidAmount" + patronBillPayment.getUnPaidBalance());
                        }
                        patronBillPayment.setPaymentOperatorId("VUFIND");
                        patronBillPayment.setPayDate(new java.sql.Date((new Date()).getTime()));
                        patronBillPayment.setPaymentMethod(paymentMethod);
                        if (patronBillPayment.getPaymentMethod().equals(OLEConstants.FORGIVE) || patronBillPayment.getPaymentMethod().equals(OLEConstants.CANCEL) || patronBillPayment.getPaymentMethod().equals(OLEConstants.ERROR)) {
                            if (patronBillPayment.getPaymentMethod().equalsIgnoreCase(OLEConstants.FORGIVE)) {
                                patronBillPayment.setFreeTextNote(CurrencyFormatter.getSymbolForCurrencyPattern() + patronBillPayment.getPaymentAmount() + " " + OLEConstants.FORGIVE_MESSAGE);
                            }
                            if (patronBillPayment.getPaymentMethod().equalsIgnoreCase(OLEConstants.CANCEL)) {
                                patronBillPayment.setFreeTextNote(OLEConstants.CANCEL_MESSAGE_AMT + " " + CurrencyFormatter.getSymbolForCurrencyPattern() + patronBillPayment.getPaymentAmount());
                            }
                            if (patronBillPayment.getPaymentMethod().equalsIgnoreCase(OLEConstants.ERROR)) {
                                patronBillPayment.setFreeTextNote(CurrencyFormatter.getSymbolForCurrencyPattern() + patronBillPayment.getPaymentAmount() + OLEConstants.ERROR_MESSAGE);
                            }
                        } else {
                            patronBillPayment.setFreeTextNote(CurrencyFormatter.getSymbolForCurrencyPattern() + patronBillPayment.getPaymentAmount() + " paid through " + patronBillPayment.getPaymentMethod());
                            patronBillPayment.setNote(null);
                        }
                        getBusinessObjectService().save(patronBillPayment);
                    }
                }
            }
        }
        return paymentAmount;
    }


    public abstract String getOperatorId(String operatorId);

    public abstract String prepareResponse(String finePaidResponse);
}
