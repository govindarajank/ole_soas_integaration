package org.kuali.ole.converter;

import com.thoughtworks.xstream.XStream;
import org.apache.log4j.Logger;
import org.kuali.ole.bo.OLEFinePayment;

/**
 * Created by govindarajank on 16/2/16.
 */
public class OLEFinePaymentConverter {
    final Logger LOG = Logger.getLogger(OLEFinePaymentConverter.class);

    private static XStream xStream = getXstream();
    private static XStream xStream1 = getXstream1();


    public String generateFinePaymentXml(String finePayment) {
        return xStream.toXML(finePayment);
    }


    public Object generateFinePaymentObject(String xml) {
        return xStream1.fromXML(xml);
    }


    public String generateFinePaymentJson(String xml) {
        OLEFinePayment finePayment = (OLEFinePayment) generateFinePaymentObject(xml);
        OleCirculationHandler xmlContentHandler = new OleCirculationHandler();
        if (finePayment == null) {
            finePayment = new OLEFinePayment();
        }
        try {
            return xmlContentHandler.marshalToJSON(finePayment);
        } catch (Exception e) {
            LOG.error(e, e);
        }
        return null;
    }


    private static XStream getXstream() {
        XStream xStream = new XStream();
        xStream.alias("finePayment", OLEFinePayment.class);
        xStream.omitField(OLEFinePayment.class, "response");
        return xStream;
    }

    private static XStream getXstream1() {
        XStream xStream = new XStream();
        xStream.alias("finePayment", OLEFinePayment.class);
        return xStream;
    }
}
