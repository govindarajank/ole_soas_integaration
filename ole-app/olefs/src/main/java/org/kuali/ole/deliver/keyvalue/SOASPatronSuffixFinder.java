package org.kuali.ole.deliver.keyvalue;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.bo.ui.PersonDocumentName;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: giris
 * Date: 7/25/14
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class SOASPatronSuffixFinder extends KeyValuesBase {

    private static final String PARAM_BO_CLASSNAME = PersonDocumentName.class.getSimpleName();

    /*
     * @see org.kuali.keyvalues.KeyValuesFinder#getKeyValues()
     */
    @Override
    public List<KeyValue> getKeyValues() {

        Collection<String> values = CoreFrameworkServiceLocator.getParameterService().getParameterValuesAsString(
                KimConstants.NAMESPACE_CODE, PARAM_BO_CLASSNAME, "PATRON_SUFFIXES"
        );
        List<KeyValue> labels = new ArrayList<KeyValue>();
        labels.add(new ConcreteKeyValue("", ""));
        for (String suffix : values) {
            labels.add(new ConcreteKeyValue(suffix, suffix));
        }

        return labels;
    }
}
