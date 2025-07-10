package wisewires.agent;

import java.util.HashMap;
import java.util.Map;

public class Profile {
    private Map<String, String> apiEndpoints;

    private HashMap<String, String> customerInfo;
    private HashMap<String, String> customerAddress;
    private HashMap<String, String> billingAddress;
    private HashMap<String, String> tradeInInfo;
    private HashMap<String, String> simInfo;

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

    public HashMap<String, String> getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(HashMap<String, String> customerAddress) {
        this.customerAddress = customerAddress;
    }

    public HashMap<String, String> getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(HashMap<String, String> billingAddress) {
        this.billingAddress = billingAddress;
    }

    public HashMap<String, String> getTradeInInfo() {
        if (tradeInInfo == null) {
            return new HashMap<>();
        }
        return tradeInInfo;
    }

    public void setTradeInInfo(HashMap<String, String> tradeInInfo) {
        this.tradeInInfo = tradeInInfo;
    }

    public HashMap<String, String> getSIMInfo() {
        return simInfo;
    }

    public void setSIMInfo(HashMap<String, String> simInfo) {
        this.simInfo = simInfo;
    }
}
