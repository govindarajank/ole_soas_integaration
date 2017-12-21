package org.kuali.ole.identityAdmin.rule;

import org.kuali.ole.OLEConstants;
import org.kuali.ole.identityAdmin.bo.OleResetPassword;
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
 * Time: 7:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class OleResetPasswordRule extends MaintenanceDocumentRuleBase {

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

        OleResetPassword oldObject = (OleResetPassword) document.getOldMaintainableObject().getDataObject();
        OleResetPassword newObject = (OleResetPassword) document.getNewMaintainableObject().getDataObject();

        /*Decrypt password*/
        String decryptedPwdValue = decryptPassword(oldObject);
        oldObject.setPrincipalPassword(decryptedPwdValue);

        //Validate the User
        isValidUser=validateUser(newObject.getPrincipalName());

        if(isValidUser){
            //Reset the password
            if (newObject.getPrincipalPassword() != null && oldObject.getPrincipalPassword()!=null) {
                if(newObject.getPrincipalPassword().equalsIgnoreCase(oldObject.getPrincipalPassword())){
                    isValid &= validateAndSavePassword(newObject);
                }   else {
                    this.putFieldError(OLEConstants.OleResetPassword.CUR_PWD_CODE_VALIDATE, "error.current.pwd.mismatch");
                }
            }
        }
      return isValid;
    }

    /**
     * This method  validates whether the user is available and return boolean value.
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
            this.putFieldError(OLEConstants.OleResetPassword.USER_NOT_EXSIT, "error.user.not.exist");
            return false;
        }

        return false;
    }

    /**
     * This method  validates the password fields and return boolean value.
     * @param newObject
     * @return boolean
     */
    private boolean validateAndSavePassword(OleResetPassword newObject) {
        if (newObject.getNewPassword() != null && newObject.getConfirmNewPassword() != null) {
            if (newObject.getNewPassword().equalsIgnoreCase(newObject.getConfirmNewPassword())) {
                 /*Encryption logic*/
                String encryptedValue = encryptPassword(newObject);
                newObject.setPrincipalPassword(encryptedValue);
                return true;
            } else {
                this.putFieldError(OLEConstants.OleResetPassword.PWD_CODE_VALIDATE, "error.pwd.mismatch");
                return false;
            }
        }
        return false;
    }

    private String encryptPassword(OleResetPassword newObject) {
        String encPwd = null;

        EncryptionService encryptionService = SpringContext.getBean(EncryptionService.class);
        try {
            encPwd = encryptionService.encrypt(newObject.getConfirmNewPassword());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return encPwd;
    }

    private String decryptPassword(OleResetPassword oldPassword) {
        String decryptPwd = null;
        EncryptionService encryptService = SpringContext.getBean(EncryptionService.class);
        try {
            decryptPwd = encryptService.decrypt(oldPassword.getPrincipalPassword());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return decryptPwd;
    }
}
