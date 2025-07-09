package wisewires.agent;

import java.util.HashMap;
import java.util.Map;

public class Profile {
    private Map<String, String> apiEndpoints;

    private HashMap<String, String> customerInfo;

    public Map<String, String> getApiEndpoints() {
        return apiEndpoints;
    }

    public void setApiEndpoints(Map<String, String> apiEndpoints) {
        this.apiEndpoints = apiEndpoints;
    }

    public HashMap<String, String> getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(HashMap<String, String> customerInfo) {
        this.customerInfo = customerInfo;
    }
}
