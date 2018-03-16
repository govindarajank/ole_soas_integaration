package org.kuali.ole.deliver.controller;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.ole.deliver.bo.*;

import org.kuali.rice.krad.service.KRADServiceLocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hemalathas on 1/18/16.
 */
public class ItemBarcodeUpdateHandler {

    Map<String,String> itemBarcodeMap = new HashMap<>();

    public void updateItemBarcode(String oldBarcode, String newBarcode,String itemId){
        updateDeliverRequest(itemId, newBarcode);
        updateCirculationHistory(itemId, newBarcode);
        updateTemporaryCirculationHistory(itemId, newBarcode);
        updateLoanDocument(itemId, newBarcode);
        updateFeeType(itemId, newBarcode);
        updateReturnHistoryRecord(itemId, newBarcode);
        updateRenewalHistoryRecord(itemId, newBarcode);
        updateDeliverRequestHistoryRecord(oldBarcode, newBarcode);
    }

    public void updateDeliverRequest(String itemId, String newBarcode){
        itemBarcodeMap.put("itemUuid", "wio-"+itemId);
        List<OleDeliverRequestBo> oleDeliverRequestBos = new ArrayList<>();
        List<OleDeliverRequestBo> deliverRequestBoList = (List<OleDeliverRequestBo>) KRADServiceLocator.getBusinessObjectService().findMatching(OleDeliverRequestBo.class, itemBarcodeMap);

        if (CollectionUtils.isNotEmpty(deliverRequestBoList)) {
            for (OleDeliverRequestBo oleDeliverRequestBo : deliverRequestBoList) {
                oleDeliverRequestBo.setItemId(newBarcode);
                oleDeliverRequestBos.add(oleDeliverRequestBo);
            }
            KRADServiceLocator.getBusinessObjectService().save(oleDeliverRequestBos);
        }
    }

    public void updateCirculationHistory(String itemId, String newBarcode){
        itemBarcodeMap.clear();
        itemBarcodeMap.put("itemUuid", "wio-"+itemId);
        List<OleCirculationHistory> oleCirculationHistories = new ArrayList<>();
        List<OleCirculationHistory> oleCirculationHistoryList = (List<OleCirculationHistory>) KRADServiceLocator.getBusinessObjectService().findMatching(OleCirculationHistory.class, itemBarcodeMap);
        if (CollectionUtils.isNotEmpty(oleCirculationHistoryList)) {
            for (OleCirculationHistory circulationHistory : oleCirculationHistoryList) {
                circulationHistory.setItemId(newBarcode);
                oleCirculationHistories.add(circulationHistory);
            }
            KRADServiceLocator.getBusinessObjectService().save(oleCirculationHistories);
        }
    }

    public void updateTemporaryCirculationHistory(String itemId, String newBarcode){
        itemBarcodeMap.clear();
        itemBarcodeMap.put("itemUuid", "wio-"+itemId);
        List<OleTemporaryCirculationHistory> oleTemporaryCirculationHistoryList = new ArrayList<>();
        List<OleTemporaryCirculationHistory> oleTemporaryCirculationHistories = (List<OleTemporaryCirculationHistory>) KRADServiceLocator.getBusinessObjectService().findMatching(OleTemporaryCirculationHistory.class, itemBarcodeMap);
        if (CollectionUtils.isNotEmpty(oleTemporaryCirculationHistories)) {
            for (OleTemporaryCirculationHistory temporaryCirculationHistory : oleTemporaryCirculationHistories) {
                temporaryCirculationHistory.setItemId(newBarcode);
                oleTemporaryCirculationHistoryList.add(temporaryCirculationHistory);
            }
            KRADServiceLocator.getBusinessObjectService().save(oleTemporaryCirculationHistoryList);
        }
    }

    public void updateLoanDocument(String itemId, String newBarcode){
        itemBarcodeMap.clear();
        itemBarcodeMap.put("itemUuid", "wio-"+itemId);
        List<OleLoanDocument> oleLoanDocuments = new ArrayList<>();
        List<OleLoanDocument> oleLoanDocumentList = (List<OleLoanDocument>) KRADServiceLocator.getBusinessObjectService().findMatching(OleLoanDocument.class, itemBarcodeMap);
        if (CollectionUtils.isNotEmpty(oleLoanDocumentList)) {
            for (OleLoanDocument loanDocument : oleLoanDocumentList) {
                loanDocument.setItemId(newBarcode);
                oleLoanDocuments.add(loanDocument);
            }
            KRADServiceLocator.getBusinessObjectService().save(oleLoanDocuments);
        }
    }


    public void updateFeeType(String itemId, String newBarcode){
        itemBarcodeMap.clear();
        itemBarcodeMap.put("itemUuid","wio-"+itemId);
        List<FeeType> feeTypes = new ArrayList<>();
        List<FeeType> feeTypeList = (List<FeeType>) KRADServiceLocator.getBusinessObjectService().findMatching(FeeType.class, itemBarcodeMap);
        if (CollectionUtils.isNotEmpty(feeTypeList)) {
            for (FeeType feeType : feeTypeList) {
                feeType.setItemBarcode(newBarcode);
                feeTypes.add(feeType);
            }
            KRADServiceLocator.getBusinessObjectService().save(feeTypes);
        }
    }

    public void updateReturnHistoryRecord(String itemId, String newBarcode){
        itemBarcodeMap.clear();
        itemBarcodeMap.put("itemUUID","wio-"+itemId);
        List<OLEReturnHistoryRecord> oleReturnHistoryRecords = new ArrayList<>();
        List<OLEReturnHistoryRecord> oleReturnHistoryRecordList = (List<OLEReturnHistoryRecord>) KRADServiceLocator.getBusinessObjectService().findMatching(OLEReturnHistoryRecord.class, itemBarcodeMap);
        if (CollectionUtils.isNotEmpty(oleReturnHistoryRecordList)) {
            for (OLEReturnHistoryRecord returnHistoryRecord : oleReturnHistoryRecordList) {
                returnHistoryRecord.setItemBarcode(newBarcode);
                oleReturnHistoryRecords.add(returnHistoryRecord);
            }
            KRADServiceLocator.getBusinessObjectService().save(oleReturnHistoryRecords);
        }
    }


    public void updateRenewalHistoryRecord(String itemId, String newBarcode){
        itemBarcodeMap.clear();
        itemBarcodeMap.put("itemId","wio-"+itemId);
        List<OleRenewalHistory> oleRenewalHistories = new ArrayList<>();
        List<OleRenewalHistory> oleRenewalHistoryList = (List<OleRenewalHistory>) KRADServiceLocator.getBusinessObjectService().findMatching(OleRenewalHistory.class, itemBarcodeMap);
        if (CollectionUtils.isNotEmpty(oleRenewalHistoryList)) {
            for (OleRenewalHistory renewalHistory : oleRenewalHistoryList) {
                renewalHistory.setItemBarcode(newBarcode);
                oleRenewalHistories.add(renewalHistory);
            }
            KRADServiceLocator.getBusinessObjectService().save(oleRenewalHistories);
        }

    }

    public void updateDeliverRequestHistoryRecord(String oleBarcode, String newBarcode){
        itemBarcodeMap.clear();
        itemBarcodeMap.put("itemBarcode",oleBarcode);
        List<OleDeliverRequestHistoryRecord> oleDeliverRequestHistoryRecords = new ArrayList<>();
        List<OleDeliverRequestHistoryRecord> oleDeliverRequestHistoryRecordList = (List<OleDeliverRequestHistoryRecord>) KRADServiceLocator.getBusinessObjectService().findMatching(OleDeliverRequestHistoryRecord.class, itemBarcodeMap);
        if (CollectionUtils.isNotEmpty(oleDeliverRequestHistoryRecordList)) {
            for (OleDeliverRequestHistoryRecord deliverRequestHistoryRecord : oleDeliverRequestHistoryRecordList) {
                deliverRequestHistoryRecord.setItemBarcode(newBarcode);
                oleDeliverRequestHistoryRecords.add(deliverRequestHistoryRecord);
            }
            KRADServiceLocator.getBusinessObjectService().save(oleDeliverRequestHistoryRecords);
        }
    }



}
