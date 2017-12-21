package org.kuali.asr.handler;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.kuali.ole.ncip.bo.OLENCIPConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by govindarajank on 18/12/15.
 */
public class FinePaymentRequestHandler extends RequestHandler {

    public Map parseRequest(JSONObject jsonObject) throws JSONException {
        String billId = getStringValueFromJsonObject(jsonObject, "billId");
        String amountPaid = getStringValueFromJsonObject(jsonObject, "amountPaid");
        String paymentType = getStringValueFromJsonObject(jsonObject, "paymentType");
        String transactionNumber = getStringValueFromJsonObject(jsonObject, "transactionNumber");
        String patronBarcode = getStringValueFromJsonObject(jsonObject, "patronBarcode");
        String feeTypeCode = getStringValueFromJsonObject(jsonObject, "feeTypeCode");
        String requestFormatType = getStringValueFromJsonObject(jsonObject, OLENCIPConstants.REQUEST_FORMAT_TYPE);
        String responseFormatType = getStringValueFromJsonObject(jsonObject, OLENCIPConstants.RESPONSE_FORMAT_TYPE);

        Map paidBillPartameters = new HashMap();
        paidBillPartameters.put("billIds", billId);
        paidBillPartameters.put("amountPaid", amountPaid);
        paidBillPartameters.put("feeTypeCode", feeTypeCode);
        paidBillPartameters.put("paymentType", paymentType);
        paidBillPartameters.put("transactionNumber",transactionNumber);
        paidBillPartameters.put("requestFormatType", requestFormatType);
        paidBillPartameters.put("responseFormatType", responseFormatType);
        paidBillPartameters.put("patronBarcode", patronBarcode);
        return paidBillPartameters;
    }

    private String getStringValue(Object object) {
        String itemBarcode;
        if(object instanceof Integer)
            itemBarcode = ((Integer)object).toString();
        else if(object instanceof Boolean)
            itemBarcode = ((Boolean)object).toString();
        else if(object instanceof Double)
            itemBarcode = ((Double)object).toString();
        else
            itemBarcode = (String)object;
        return itemBarcode;
    }


    public String getStringValueFromJsonObject(JSONObject jsonObject, String key) {
        String returnValue = null;
        try {
            returnValue = jsonObject.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return returnValue;
    }
}
