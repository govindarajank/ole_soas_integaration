package org.kuali.asr.handler;

import org.kuali.ole.converter.OLEFinePaymentConverter;

/**
 * Created by govindarajank on 16/2/16.
 */
public class FinePaymentResponseHandler extends ResponseHandler {
    private OLEFinePaymentConverter oleFinePaymentConverter;


    public String marshalObjectToXml(Object object) {
        String finePaidXml = getoleFinePaymentConverter().generateFinePaymentXml(object.toString());
        return finePaidXml;
    }

    @Override
    public String marshalObjectToJson(Object object) {
        return super.marshalObjectToJson(object);
    }


    private OLEFinePaymentConverter getoleFinePaymentConverter() {
        if (null == oleFinePaymentConverter) {
            oleFinePaymentConverter = new OLEFinePaymentConverter();
        }
        return oleFinePaymentConverter;
    }
}
