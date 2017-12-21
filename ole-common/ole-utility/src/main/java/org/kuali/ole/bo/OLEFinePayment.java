package org.kuali.ole.bo;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by govindarajank on 16/2/16.
 */
@JsonAutoDetect(JsonMethod.FIELD)
public class OLEFinePayment {
    @JsonProperty("response")
    private String response;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

}
