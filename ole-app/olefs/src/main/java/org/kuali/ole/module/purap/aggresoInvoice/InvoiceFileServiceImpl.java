package org.kuali.ole.module.purap.aggresoInvoice;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kuali.ole.DocumentUniqueIDPrefix;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.coa.businessobject.Account;
import org.kuali.ole.module.purap.businessobject.PurApSummaryItem;
import org.kuali.ole.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.ole.module.purap.document.VendorCreditMemoDocument;
import org.kuali.ole.module.purap.document.dataaccess.impl.CreditMemoDaoOjb;
import org.kuali.ole.module.purap.service.impl.PurapAccountingServiceImpl;
import org.kuali.ole.module.purap.util.SummaryAccount;
import org.kuali.ole.select.businessobject.OleFormatType;
import org.kuali.ole.select.businessobject.OleInvoiceItem;
import org.kuali.ole.select.businessobject.OlePurchaseOrderItem;
import org.kuali.ole.select.document.OleInvoiceDocument;
import org.kuali.ole.select.document.OlePurchaseOrderDocument;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.service.KRADServiceLocator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by govindarajank on 5/9/17.
 */
public class InvoiceFileServiceImpl extends PurapAccountingServiceImpl {
    private static Log LOG = LogFactory.getLog(InvoiceFileServiceImpl.class);
    private InvoiceFileRecordProcessor invoiceFileRecord;
    private List<String> invoiceDocumentIds = new ArrayList<>();

    Map<String, String> formatTypeMap = new HashMap<>();

    public InvoiceFileServiceImpl() {
        invoiceFileRecord = new InvoiceFileRecordProcessor(2142);
    }

    public void createFile() {
        Timestamp previousRunDate = invoiceFileRecord.getPreviousRunDate();
        invoiceDocumentIds = invoiceFileRecord.getInvoiceDocumentIds();
        StringBuilder fileToWriteLocalVendor = new StringBuilder();
        StringBuilder fileToWriteForeignVendor = new StringBuilder();
        StringBuilder fileToWriterrorText = new StringBuilder();
        StringBuilder tmpFileToWriteForeignVendor = new StringBuilder();
        StringBuilder tmpFileToWriteLocalVendor = new StringBuilder();
        String batchId = null;
        String currency = null;
        String invoiceAmount = null;
        String itemAmount = null;
        String invoiceDescription = "LIB";
        int voucherNumber = 0;
        String itemDescription = null;
        String voucherDate = null;
        String extInvoiceReference = null;
        String invoiceDueDate = null;
        String aparId = "10000";
        String dim_4 = "";
        String itemCostCentre = "";
        String invoiceContent = "";
        String itemLineContent = "";
        Map<String, String> accountMap = new HashMap<>();
        DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        batchId = "INV_" + df.format(new Date());
        List<Account> accounts = (List<Account>) KRADServiceLocator.getBusinessObjectService().findAll(Account.class);
        if (CollectionUtils.isNotEmpty(accounts) && accounts.size() > 0) {
            for (Account account : accounts) {
                accountMap.put(account.getAccountNumber(), account.getUniversityAccountNumber());
            }
        }
        if (CollectionUtils.isNotEmpty(invoiceDocumentIds) && invoiceDocumentIds.size() > 0) {
            Map<String, Object> map = new HashMap<String, Object>();
            Map<String, Integer> invoiceIdMap = new HashMap<>();
            List<OleFormatType> oleFormatTypes = (List<OleFormatType>) KRADServiceLocator.getBusinessObjectService().findAll(OleFormatType.class);
            if (CollectionUtils.isNotEmpty(oleFormatTypes) && oleFormatTypes.size() > 0) {
                for (OleFormatType oleFormatType : oleFormatTypes) {
                    formatTypeMap.put(oleFormatType.getFormatTypeId().toString(), oleFormatType.getFormatTypeName());
                }
            }
            map.put("documentNumber", invoiceDocumentIds);
            List<OleInvoiceDocument> documents = (List<OleInvoiceDocument>) KRADServiceLocator.getBusinessObjectService().findMatching(OleInvoiceDocument.class, map);
            if (CollectionUtils.isNotEmpty(documents) && documents.size() > 0) {
                String voucherType = "";
                boolean addFileInfo = true;
                boolean addErrFile = true;
                for (OleInvoiceDocument document : documents) {
                    if(!isSubscriptionPOExists(document)) {
                        boolean isCheckForeignVendor = true;
                        voucherNumber++;
                        invoiceContent = "";
                        itemDescription = "";
                        BigDecimal invoiceTotalAmt = BigDecimal.ZERO;
                        BigDecimal lineItemTotalAmt = BigDecimal.ZERO;
                        if (!document.getVendorDetail().getVendorAliasesAsString().contains("pm21") && !document.getInvoiceNumber().equalsIgnoreCase("barclaycard")) {
                            currency = document.getOleCurrencyType().getCurrencyAlphaCode();
                            extInvoiceReference = document.getInvoiceNumber();
                            aparId = document.getVendorDetail().getVendorDunsNumber();

                            if (isForeignCurrency(currency)) {
                                if (document.getForeignVendorInvoiceAmount() != null) {
                                    invoiceAmount = "-" + document.getForeignVendorInvoiceAmount().multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_DOWN);
                                    invoiceTotalAmt = document.getForeignVendorInvoiceAmount();
                                    voucherType = OLEConstants.AgressoCreateFile.FOREIGN_VOUCHER_TYPE;
                                }
                            } else {
                                if (document.getVendorInvoiceAmount() != null) {
                                    invoiceAmount = "-" + new BigDecimal(document.getVendorInvoiceAmount().multiply(new KualiDecimal(100)).toString()).setScale(0, BigDecimal.ROUND_DOWN);
                                    invoiceTotalAmt = new BigDecimal(document.getVendorInvoiceAmount().toString());
                                    if(document.getVendorDetail().getVendorHeader().getVendorForeignIndicator()){
                                        isCheckForeignVendor =false;
                                        voucherType = OLEConstants.AgressoCreateFile.FOREIGN_VOUCHER_TYPE;
                                    } else {
                                        voucherType = OLEConstants.AgressoCreateFile.LOCAL_VOUCHER_TYPE;
                                    }
                                }
                            }
                            if (document.getInvoiceDate() != null) {
                                voucherDate = df.format(document.getInvoiceDate());
                            }
                            if (document.getInvoicePayDate() != null) {
                                invoiceDueDate = df.format(document.getInvoicePayDate());
                            }
                            invoiceContent = getFileContent(batchId, OLEConstants.AgressoCreateFile.INTER_FACE, voucherType, OLEConstants.AgressoCreateFile.INVOICE_TRANS_TYPE, OLEConstants.AgressoCreateFile.CLIENT, OLEConstants.AgressoCreateFile.INVOICEACCOUNT,
                                    OLEConstants.AgressoCreateFile.BLANK, OLEConstants.AgressoCreateFile.BLANK, OLEConstants.AgressoCreateFile.TAXCODE, (currency != null ? currency : ""), (invoiceAmount != null ? invoiceAmount : ""), (invoiceAmount != null ? invoiceAmount : ""),
                                    (invoiceDescription != null ? invoiceDescription : ""),
                                    (voucherDate != null ? voucherDate : ""), String.format("%015d", voucherNumber),
                                    (extInvoiceReference != null ? extInvoiceReference : ""),
                                    (invoiceDueDate != null ? invoiceDueDate : ""),
                                    OLEConstants.AgressoCreateFile.STATUS, OLEConstants.AgressoCreateFile.APARTYPE, (aparId != null ? aparId : "10000"),
                                    OLEConstants.AgressoCreateFile.RESPONSIBLE) + "\n";

                            if (isForeignCurrency(currency)) {
                                tmpFileToWriteForeignVendor.append(invoiceContent);
                            } else {
                                tmpFileToWriteLocalVendor.append(invoiceContent);
                            }
                            List<SummaryAccount> summaryAccounts = generateSummaryAccounts1(document.getItems(), ZERO_TOTALS_RETURNED_VALUE, USE_TAX_INCLUDED, isCheckForeignVendor);
                            if (CollectionUtils.isNotEmpty(summaryAccounts) && summaryAccounts.size() > 0) {
                                for (SummaryAccount summaryAccount : summaryAccounts) {
                                    itemLineContent = "";
                                    String itemTaxcode = "0";
                                    itemDescription = document.getDocumentNumber() + ";";
                                    if (summaryAccount != null && summaryAccount.getAccount() != null) {
                                        dim_4 = accountMap.get(summaryAccount.getAccount().getAccountNumber());
                                        if (isForeignCurrency(currency)) {
                                            if (summaryAccount.getAccount().getForeignAmount() != null) {
                                                itemAmount = "" + summaryAccount.getAccount().getForeignAmount().multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_DOWN);
                                                lineItemTotalAmt = lineItemTotalAmt.add(summaryAccount.getAccount().getForeignAmount());
                                            }
                                        } else {
                                            if (summaryAccount.getAccount().getAmount() != null) {
                                                itemAmount = "" + new BigDecimal(summaryAccount.getAccount().getAmount().multiply(new KualiDecimal(100)).toString()).setScale(0, BigDecimal.ROUND_DOWN);
                                                lineItemTotalAmt = lineItemTotalAmt.add(new BigDecimal(summaryAccount.getAccount().getAmount().toString()));
                                            }
                                        }
                                        for (PurApSummaryItem purApSummaryItem : summaryAccount.getItems()) {
                                            if (purApSummaryItem.getItemTypeCode().equalsIgnoreCase(OLEConstants.ITEM)) {
                                                int purApSummaryItemItemIdentifier = purApSummaryItem.getItemIdentifier();
                                                invoiceIdMap.put("itemIdentifier", purApSummaryItemItemIdentifier);
                                                OleInvoiceItem oleInvoiceItem = (OleInvoiceItem) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OleInvoiceItem.class, invoiceIdMap);
                                                if (oleInvoiceItem != null) {
                                                    itemDescription = itemDescription + "PO" + oleInvoiceItem.getPurchaseOrderIdentifier() + ";BIB" + DocumentUniqueIDPrefix.getDocumentId(oleInvoiceItem.getItemTitleId()) + ";";
                                                    itemCostCentre = getItemCostCentre(oleInvoiceItem.getFormatType().getFormatTypeName());
                                                    if (StringUtils.isNotBlank(itemCostCentre)) {
                                                        if (itemCostCentre.equalsIgnoreCase("4025") || itemCostCentre.equalsIgnoreCase("4026")) {
                                                            if (itemCostCentre.equalsIgnoreCase("4025") && isCheckForeignVendor) {
                                                                itemTaxcode = "Z";
                                                            } else if(isCheckForeignVendor){
                                                                itemTaxcode = "SP";
                                                            }
                                                        } else {
                                                            addFileInfo = false;
                                                        }
                                                    } else {
                                                        addFileInfo = false;
                                                    }
                                                }
                                            }
                                        }
                                        itemLineContent = getFileContent(batchId, OLEConstants.AgressoCreateFile.INTER_FACE, voucherType, OLEConstants.AgressoCreateFile.ITEM_TRANS_TYPE,
                                                OLEConstants.AgressoCreateFile.CLIENT, itemCostCentre != null ? itemCostCentre : "", OLEConstants.AgressoCreateFile.BLANK,
                                                dim_4 != null ? dim_4 : "",
                                                itemTaxcode, currency, itemAmount != null ? itemAmount : "0",
                                                itemAmount != null ? itemAmount : "0",
                                                itemDescription != null ? itemDescription : "",
                                                voucherDate, String.format("%015d", voucherNumber), extInvoiceReference, invoiceDueDate, OLEConstants.AgressoCreateFile.STATUS, OLEConstants.AgressoCreateFile.APARTYPE,
                                                (aparId != null ? aparId : "10000"), OLEConstants.AgressoCreateFile.RESPONSIBLE) + "\n";


                                        if (isForeignCurrency(currency)) {
                                            tmpFileToWriteForeignVendor.append(itemLineContent);
                                        } else {
                                            tmpFileToWriteLocalVendor.append(itemLineContent);
                                        }
                                    }
                                }
                            }
                        }
                        if (addFileInfo) {
                            fileToWriteForeignVendor.append(tmpFileToWriteForeignVendor);
                            fileToWriteLocalVendor.append(tmpFileToWriteLocalVendor);
                            tmpFileToWriteForeignVendor.setLength(0);
                            tmpFileToWriteLocalVendor.setLength(0);
                        } else if (!addErrFile) {
                            fileToWriterrorText.append(tmpFileToWriteForeignVendor);
                            fileToWriterrorText.append(tmpFileToWriteLocalVendor);
                            tmpFileToWriteForeignVendor.setLength(0);
                            tmpFileToWriteLocalVendor.setLength(0);
                            addFileInfo = true;
                            addErrFile = true;
                        } else {
                            tmpFileToWriteForeignVendor.setLength(0);
                            tmpFileToWriteLocalVendor.setLength(0);
                            addFileInfo = true;
                            addErrFile = true;
                        }
                    }
                }
            }
        }
        CreditMemoDaoOjb creditMemoServiceDao = (CreditMemoDaoOjb) SpringContext.getService("creditMemoDao");
        List<VendorCreditMemoDocument> vendorCreditMemoDocumentList = creditMemoServiceDao.getCreditMemoByCriteria(previousRunDate);
        if(vendorCreditMemoDocumentList .size()>0) {
            List<String> documentNbr = new ArrayList<>();
            List<String> documentNumber = new ArrayList<>();
            for (VendorCreditMemoDocument vendorCreditMemoDocument : vendorCreditMemoDocumentList ) {
                documentNbr.add(vendorCreditMemoDocument.getDocumentNumber());
            }
            documentNumber = invoiceFileRecord.isDocumentFinal(documentNbr);
            for (VendorCreditMemoDocument vendorCreditMemoDocument : vendorCreditMemoDocumentList ) {
                if(!vendorCreditMemoDocument.getVendorDetail().getVendorAliasesAsString().contains("pm21")) {
                    if((documentNumber.contains(vendorCreditMemoDocument.getDocumentNumber()))) {
                        voucherNumber++;
                        String costCentre = "";
                        String currencyAlphaCode = vendorCreditMemoDocument.getVendorDetail().getCurrencyType().getCurrencyAlphaCode();
                        String creditmemoAmt = "";
                        if (vendorCreditMemoDocument.getCreditMemoAmount() != null) {
                            creditmemoAmt = (new BigDecimal(vendorCreditMemoDocument.getCreditMemoAmount().multiply(new KualiDecimal(100)).toString()).setScale(0, BigDecimal.ROUND_DOWN)).toString();
                        }
                        String account = "";
                        String description = "";
                        if (vendorCreditMemoDocument.getPurchaseOrderDocument() != null) {
                            description = vendorCreditMemoDocument.getDocumentNumber() + ";PO" + vendorCreditMemoDocument.getPurchaseOrderDocument().getPurapDocumentIdentifier();
                            OlePurchaseOrderItem purchaseOrderItem = (OlePurchaseOrderItem) vendorCreditMemoDocument.getPurchaseOrderDocument().getItem(0);
                            costCentre = getItemCostCentre(purchaseOrderItem.getFormatTypeName().getFormatTypeName());
                        } else {
                            description = vendorCreditMemoDocument.getDocumentNumber() + ";VENDOR" + vendorCreditMemoDocument.getVendorDetail().getVendorName();
                        }
                        String creditMemoNumber = vendorCreditMemoDocument.getCreditMemoNumber();
                        aparId = vendorCreditMemoDocument.getVendorDetail().getVendorDunsNumber();
                        if (vendorCreditMemoDocument.getCreditMemoDate() != null) {
                            voucherDate = df.format(vendorCreditMemoDocument.getCreditMemoDate());
                        }
                        try {
                            account = vendorCreditMemoDocument.getItem(0).getSourceAccountingLines().get(0).getAccount().getAccountNumber();
                            account = accountMap.get(account);
                        } catch (Exception e) {
                            account = "";
                        }
                        boolean isCheckForeignVendor = true;
                        String itemTaxcode = "0";
                        String voucherType = "";
                        if(!description.contains("BARCLAYCARD")) {
                            if(vendorCreditMemoDocument.getVendorDetail().getVendorHeader().getVendorForeignIndicator()){
                                isCheckForeignVendor =false;
                                voucherType = OLEConstants.AgressoCreateFile.FOREIGN_VOUCHER_TYPE;
                            } else {
                                voucherType = OLEConstants.AgressoCreateFile.LOCAL_VOUCHER_TYPE;
                            }
                            if (costCentre.equalsIgnoreCase("4025") || costCentre.equalsIgnoreCase("4026")) {
                                if (isForeignCurrency(currencyAlphaCode)) {
                                    fileToWriteForeignVendor.append(getFileContent(batchId, OLEConstants.AgressoCreateFile.INTER_FACE, OLEConstants.AgressoCreateFile.FOREIGN_VOUCHER_TYPE, OLEConstants.AgressoCreateFile.INVOICE_TRANS_TYPE,
                                            OLEConstants.AgressoCreateFile.CLIENT, OLEConstants.AgressoCreateFile.INVOICEACCOUNT, OLEConstants.AgressoCreateFile.BLANK, OLEConstants.AgressoCreateFile.BLANK, OLEConstants.AgressoCreateFile.TAXCODE, currencyAlphaCode, (creditmemoAmt != null ? creditmemoAmt : ""),
                                            (creditmemoAmt != null ? creditmemoAmt : ""), invoiceDescription, voucherDate, String.format ("%015d", voucherNumber), creditMemoNumber, voucherDate,
                                            voucherDate, OLEConstants.AgressoCreateFile.APARTYPE, (aparId != null ? aparId : "10000"), OLEConstants.AgressoCreateFile.RESPONSIBLE) + "\n");
                                    fileToWriteForeignVendor.append(getFileContent(batchId, OLEConstants.AgressoCreateFile.INTER_FACE, OLEConstants.AgressoCreateFile.FOREIGN_VOUCHER_TYPE, OLEConstants.AgressoCreateFile.ITEM_TRANS_TYPE,
                                            OLEConstants.AgressoCreateFile.CLIENT, costCentre, OLEConstants.AgressoCreateFile.BLANK, account, itemTaxcode, currencyAlphaCode, (creditmemoAmt != null ? "-" + creditmemoAmt : ""),
                                            (creditmemoAmt != null ? "-" + creditmemoAmt : ""), description, voucherDate, String.format ("%015d", voucherNumber), creditMemoNumber, voucherDate,
                                            voucherDate, OLEConstants.AgressoCreateFile.APARTYPE, (aparId != null ? aparId : "10000"), OLEConstants.AgressoCreateFile.RESPONSIBLE) + "\n");
                                } else {
                                    if (costCentre.equalsIgnoreCase("4025") && isCheckForeignVendor) {
                                        itemTaxcode = "Z";
                                    } else if(isCheckForeignVendor){
                                        itemTaxcode = "SP";
                                    }
                                    fileToWriteLocalVendor.append(getFileContent(batchId, OLEConstants.AgressoCreateFile.INTER_FACE, OLEConstants.AgressoCreateFile.LOCAL_VOUCHER_TYPE, OLEConstants.AgressoCreateFile.INVOICE_TRANS_TYPE,
                                            OLEConstants.AgressoCreateFile.CLIENT, OLEConstants.AgressoCreateFile.INVOICEACCOUNT, OLEConstants.AgressoCreateFile.BLANK, OLEConstants.AgressoCreateFile.BLANK, OLEConstants.AgressoCreateFile.TAXCODE, currencyAlphaCode, (creditmemoAmt != null ? creditmemoAmt : ""),
                                            (creditmemoAmt != null ? creditmemoAmt : ""), invoiceDescription, voucherDate, String.format ("%015d", voucherNumber), creditMemoNumber, voucherDate,
                                            OLEConstants.AgressoCreateFile.STATUS, OLEConstants.AgressoCreateFile.APARTYPE, (aparId != null ? aparId : "10000"), OLEConstants.AgressoCreateFile.RESPONSIBLE) + "\n");
                                    fileToWriteLocalVendor.append(getFileContent(batchId, OLEConstants.AgressoCreateFile.INTER_FACE, voucherType, OLEConstants.AgressoCreateFile.ITEM_TRANS_TYPE,
                                            OLEConstants.AgressoCreateFile.CLIENT, costCentre, OLEConstants.AgressoCreateFile.BLANK, account, itemTaxcode, currencyAlphaCode, (creditmemoAmt != null ? "-" + creditmemoAmt : ""),
                                            (creditmemoAmt != null ? "-" + creditmemoAmt : ""), description, voucherDate, String.format ("%015d", voucherNumber), creditMemoNumber, voucherDate,
                                            OLEConstants.AgressoCreateFile.STATUS, OLEConstants.AgressoCreateFile.APARTYPE, (aparId != null ? aparId : "10000"), OLEConstants.AgressoCreateFile.RESPONSIBLE) + "\n");
                                }
                            }
                        }
                    }
                }
            }
        }
        String path = OLEConstants.AgressoCreateFile.GBP_INVOICES_EXTERNAL_DELIVERY_DIRECTORY;
        batchId = batchId.replace("INV_","");
        try {
            generateFileForContent(fileToWriteLocalVendor, "LibraryInvoices_"+batchId.concat("_local"),"/Agresso/");
            if(StringUtils.isNotBlank(path)) {
                generateFile(fileToWriteLocalVendor, "LibraryInvoices_" + batchId.concat("_local"), path);
            }
            fileToWriteLocalVendor.setLength(0);
        } catch (Exception e) {
            LOG.error("Exception occurred while generating file for Local Vendor");
            e.printStackTrace();
        }
        try {
            generateFileForContent(fileToWriterrorText, "LibraryInvoices_"+batchId.concat("_error"),"/Agresso/");
            if(StringUtils.isNotBlank(path)) {
                generateFile(fileToWriterrorText, "LibraryInvoices_" + batchId.concat("_error"), path);
            }
            fileToWriteLocalVendor.setLength(0);
        } catch (Exception e) {
            LOG.error("Exception occurred while generating file for Local Vendor");
            e.printStackTrace();
        }
        try {
            generateFileForContent(fileToWriteForeignVendor, "LibraryInvoices_"+batchId.concat("_foreign"),"/Agresso/");
            if(StringUtils.isNotBlank(path)) {
                generateFile(fileToWriteForeignVendor, "LibraryInvoices_" + batchId.concat("_foreign"), path);
            }
            fileToWriteForeignVendor.setLength(0);
        } catch (Exception e) {
            LOG.error("Exception occurred while generating file for Foreign Vendor");
            e.printStackTrace();
        }
        invoiceFileRecord.updateParameterforNextRunTimeAgresso();
    }

    public String getItemCostCentre(String formatType) {
        StringBuilder sb = new StringBuilder();
        String itemCostCentre = "";
        if (formatType != null) {
            for (int i = 0; i < formatType.length(); i++) {
                final char c = formatType.charAt(i);
                if (c > 47 && c < 58) {
                    sb.append(c);
                }
            }
            itemCostCentre = sb.toString();
        }
        return itemCostCentre;
    }

    public String getFileContent(String batchId, String inter_face, String voucher_type, String trans_type,
                                 String client, String account, String blank, String dim_4, String tax_code,
                                 String currency, String cur_amt, String amt, String description, String voucher_date, String VoucherNumber,
                                 String ext_inv_ref, String due_date, String status, String apar_type,
                                 String apar_id, String responsible) {
        invoiceFileRecord.setField(1, 25, batchId);
        invoiceFileRecord.setField(26, 25, inter_face);
        invoiceFileRecord.setField(51, 25, voucher_type);
        invoiceFileRecord.setField(76, 2, trans_type);
        invoiceFileRecord.setField(78, 25, client);
        invoiceFileRecord.setField(103, 25, account);
        invoiceFileRecord.setField(128, 75, blank);
        invoiceFileRecord.setField(203, 25, dim_4);
        invoiceFileRecord.setField(228, 75, blank);
        invoiceFileRecord.setField(303, 25, tax_code);
        invoiceFileRecord.setField(328, 25, blank);
        invoiceFileRecord.setField(353, 25, currency);
        invoiceFileRecord.setField(378, 2, blank);
        int currentAmountlength = cur_amt.length();
        invoiceFileRecord.setField(380, (20 - currentAmountlength), blank);
        invoiceFileRecord.setField((380 + (20 - currentAmountlength)), currentAmountlength, cur_amt);
        int amountLength = amt.length();
        invoiceFileRecord.setField(400, (20 - amountLength), blank);
        invoiceFileRecord.setField((400 + (20 - amountLength)), amountLength, amt);
        invoiceFileRecord.setField(420, 71, blank);
        invoiceFileRecord.setField(491, 255, description);
        invoiceFileRecord.setField(746, 8, blank);
        invoiceFileRecord.setField(754, 8, voucher_date);
        invoiceFileRecord.setField(762, 15, VoucherNumber);
        invoiceFileRecord.setField(777, 7, blank);
        invoiceFileRecord.setField(784, 100, ext_inv_ref);
        invoiceFileRecord.setField(884, 255, blank);
        invoiceFileRecord.setField(1139, 8, due_date);
        invoiceFileRecord.setField(1147, 97, blank);
        invoiceFileRecord.setField(1244, 1, status);
        invoiceFileRecord.setField(1245, 1, apar_type);
        invoiceFileRecord.setField(1246, 25, apar_id);
        invoiceFileRecord.setField(1271, 75, blank);
        invoiceFileRecord.setField(1346, 25, responsible);
        invoiceFileRecord.setField(1371, 771, blank);
        return invoiceFileRecord.toString();
    }

    public void generateFileForContent(StringBuilder fileContent, String fileName, String filePath) throws IOException {
        try {
            String path = ConfigContext.getCurrentContextConfig().getProperty("project.home");
            File dirFile = new File(path + filePath);
            if (!dirFile.exists()) {
                dirFile.getParentFile().mkdirs();
                dirFile.mkdir();
            }
            File batFile = new File(dirFile + "/" + fileName + ".txt");
            if (!batFile.exists()) {
                batFile.createNewFile();
            }
            FileWriter fw = new FileWriter(batFile.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(fileContent.toString());
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void generateFile(StringBuilder fileContent, String fileName, String filePath) throws IOException {
        try {
            File dirFile = new File(filePath);
            if (!dirFile.exists()) {
                dirFile.getParentFile().mkdirs();
                dirFile.mkdir();
            }
            File batFile = new File(dirFile + "/" + fileName + ".txt");
            if (!batFile.exists()) {
                batFile.createNewFile();
            }
            FileWriter fw = new FileWriter(batFile.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(fileContent.toString());
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isForeignCurrency(String currencyCode) {
        if (currencyCode != null) {
            if (!currencyCode.equalsIgnoreCase(OLEConstants.AgressoCreateFile.CURRENCYCODE)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSubscriptionPOExists(OleInvoiceDocument oleInvoiceDocument){
        if(CollectionUtils.isNotEmpty(oleInvoiceDocument.getPurchaseOrderDocuments()) && oleInvoiceDocument.getPurchaseOrderDocuments().size() > 0){
            for(OlePurchaseOrderDocument olePurchaseOrderDocument : oleInvoiceDocument.getPurchaseOrderDocuments()){
                if(olePurchaseOrderDocument.getOrderType() != null){
                    if(olePurchaseOrderDocument.getOrderType().getPurchaseOrderType().equalsIgnoreCase("Subscription")){
                        return true;
                    }
                }
            }
        }else{
            if(CollectionUtils.isNotEmpty(oleInvoiceDocument.getItems()) && oleInvoiceDocument.getItems().size() > 0){
                Map poMap = new HashMap();
                for(OleInvoiceItem oleInvoiceItem : (List<OleInvoiceItem>)oleInvoiceDocument.getItems()){
                    if (oleInvoiceItem.getItemTypeCode().equalsIgnoreCase("ITEM")) {
                        if(oleInvoiceItem.getPurchaseOrderIdentifier() != null){
                            poMap.put("purapDocumentIdentifier",oleInvoiceItem.getPurchaseOrderIdentifier());
                            List<OlePurchaseOrderDocument> olePurchaseOrderDocuments = (List<OlePurchaseOrderDocument>)KRADServiceLocator.getBusinessObjectService().findMatching(OlePurchaseOrderDocument.class,poMap);
                            if(CollectionUtils.isNotEmpty(olePurchaseOrderDocuments) && olePurchaseOrderDocuments.size() > 0){
                                for(OlePurchaseOrderDocument olePurchaseOrderDocument : olePurchaseOrderDocuments){
                                    if(olePurchaseOrderDocument.getOrderType() != null && olePurchaseOrderDocument.getOrderType().getPurchaseOrderType().equalsIgnoreCase("Subscription")){
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}