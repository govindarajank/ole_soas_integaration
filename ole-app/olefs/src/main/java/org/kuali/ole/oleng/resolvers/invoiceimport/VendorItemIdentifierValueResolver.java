package org.kuali.ole.oleng.resolvers.invoiceimport;

import org.kuali.ole.constants.OleNGConstants;
import org.kuali.ole.pojo.OleInvoiceRecord;

/**
 * Created by SheikS on 1/27/2016.
 */
public class VendorItemIdentifierValueResolver extends InvoiceRecordResolver {

    @Override
    public boolean isInterested(String attributeName) {
        return OleNGConstants.BatchProcess.VENDOR_ITEM_IDENTIFIER.equalsIgnoreCase(attributeName);
    }

    @Override
    public void setAttributeValue(OleInvoiceRecord oleInvoiceRecord, String attributeValue) {
        oleInvoiceRecord.setVendorItemIdentifier(attributeValue);
    }
}
