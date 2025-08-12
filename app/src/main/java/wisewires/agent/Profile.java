package wisewires.agent;

import java.util.HashMap;
import java.util.Map;

public class Profile {
    private Map<String, String> apiEndpoints;
    private Map<String, String> exchangeEndpoints;

    private HashMap<String, String> customerInfo;
    private HashMap<String, String> customerAddress;
    private HashMap<String, String> billingAddress;
    private HashMap<String, String> tradeInData;
    private HashMap<String, String> tradeInInfo;
    private HashMap<String, String> simInfo;

    private Map<String, Map<String, Object>> serviceData;

    public Map<String, String> getApiEndpoints() {
        return apiEndpoints;
    }

    public void setApiEndpoints(Map<String, String> apiEndpoints) {
        this.apiEndpoints = apiEndpoints;
    }

    public Map<String, String> getExchangeEndpoints() {
        return exchangeEndpoints;
    }

    public void setExchangeEndpoints(Map<String, String> exchangeEndpoints) {
        this.exchangeEndpoints = exchangeEndpoints;
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

    public HashMap<String, String> getTradeInData() {
        if (tradeInData == null) {
            return new HashMap<>();
        }
        return tradeInData;
    }

    public void setTradeInData(HashMap<String, String> tradeInData) {
        this.tradeInData = tradeInData;
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

    public Map<String, Map<String, Object>> getServiceData() {
        if (serviceData == null) {
            return new HashMap<>();
        }
        return serviceData;
    }

    public void setServiceData(Map<String, Map<String, Object>> serviceData) {
        this.serviceData = serviceData;
    }
}
