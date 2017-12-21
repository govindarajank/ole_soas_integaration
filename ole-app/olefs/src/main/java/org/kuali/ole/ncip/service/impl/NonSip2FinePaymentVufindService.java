package org.kuali.ole.ncip.service.impl;

/**
 * Created by govindarajank on 22/12/15.
 */
public class NonSip2FinePaymentVufindService extends OLEFinePaymentVufindService {

    @Override
    public String prepareResponse(String finePaidStatus) {
        switch (responseFormatType) {
            case ("XML"):
                response = getResponseHandler().marshalObjectToXml(finePaidStatus);
                break;
            case ("JSON"):
                response = getResponseHandler().marshalObjectToJson(finePaidStatus);
                break;
        }

        return response;
    }

    @Override
    public String getOperatorId(String operatorId) {
        return operatorId;
    }
}
