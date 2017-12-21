package org.kuali.ole.identityAdmin.rule;

import org.kuali.ole.OLEConstants;
import org.kuali.ole.identityAdmin.bo.OleForgotPassword;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.encryption.EncryptionService;
import org.kuali.rice.kim.impl.identity.principal.PrincipalBo;
import org.kuali.rice.krad.maintenance.MaintenanceDocument;
import org.kuali.rice.krad.rules.MaintenanceDocumentRuleBase;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.KRADServiceLocator;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Created with IntelliJ IDEA.
 * User: nadimuthus
 * Date: 5/19/14
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * OleForgotPasswordRule validates maintenance object for OleForgotPassword Maintenance Document
 */
public class OleForgotPasswordRule extends MaintenanceDocumentRuleBase {

    private BusinessObjectService businessObjectService;

    /**
     * Gets the businessObjectService attribute.
     * @return Returns the businessObjectService
     */
    public BusinessObjectService getBusinessObjectService() {
        if (null == businessObjectService) {
            businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return businessObjectService;
    }

    protected boolean processCustomSaveDocumentBusinessRules(MaintenanceDocument document) {
        boolean isValid = true;
        boolean isValidUser=true;

        OleForgotPassword forgotPassword = (OleForgotPassword) document.getNewMaintainableObject().getDataObject();
        //Validate the User
        isValidUser=validateUser(forgotPassword.getPrincipalName());
        if(isValidUser){
            //Create new password for the user
            isValid &= validateAndSavePassword(forgotPassword);
            return isValid;
        }
        return false;
   }

    /**
     * This method  validates whether the user is available and return boolean value.
     *
     * @param prncplName
     * @return boolean
     */
    private boolean validateUser(String prncplName) {
        String returnValue=null;
        Map userMap = new HashMap();
        userMap.put("principalName", prncplName);
        List<PrincipalBo> result = (List<PrincipalBo>) getBusinessObjectService().findMatching(PrincipalBo.class, userMap);
        if (result!=null && result.size()>0){
            returnValue= result.get(0).getPrincipalName() ;
            if(prncplName.equals(returnValue)){
                return true;
            }
        } else{
            this.putFieldError(OLEConstants.OleForgotPassword.USER_NOT_EXSIT, "error.user.not.exist");
            return false;
        }
        return false;
    }

    /**
     * This method  validates the password fields and return boolean value.
     *
     * @param forgotPassword
     * @return boolean
     */
    private boolean validateAndSavePassword(OleForgotPassword forgotPassword) {
        if (forgotPassword.getNewPassword() != null && forgotPassword.getConfirmNewPassword() != null) {
            if (forgotPassword.getNewPassword().equalsIgnoreCase(forgotPassword.getConfirmNewPassword())) {
                /*Encryption logic*/
                String encryptedValue = encryptPassword(forgotPassword);
                forgotPassword.setPrincipalPassword(encryptedValue);
                return true;
            } else {
                this.putFieldError(OLEConstants.OleForgotPassword.PWD_CODE_VALIDATE, "error.pwd.mismatch");
                return false;
            }
        }
        return false;
    }
    private String encryptPassword(OleForgotPassword forgotPassword) {
        String encPwd = null;
        EncryptionService encryptionService = SpringContext.getBean(EncryptionService.class);
        try {
            encPwd = encryptionService.encrypt(forgotPassword.getConfirmNewPassword());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return encPwd;
    }

}
