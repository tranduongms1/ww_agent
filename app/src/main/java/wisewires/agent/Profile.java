package wisewires.agent;

import java.util.HashMap;
import java.util.Map;

public class Profile {
    private Map<String, String> stores;
    private Map<String, String> pointingUrls;
    private Map<String, String> apiEndpoints;
    private Map<String, String> exchangeEndpoints;
    private Map<String, String> tariffEndpoints;

    private HashMap<String, String> customerInfo;
    private HashMap<String, String> customerAddress;
    private HashMap<String, String> customerInfoEdit;
    private HashMap<String, String> customerAddressEdit;
    private HashMap<String, String> billingAddress;
    private HashMap<String, String> tradeInData;
    private HashMap<String, String> tradeInInfo;
    private HashMap<String, String> tradeUpData;
    private HashMap<String, String> simInfo;

    private Map<String, Map<String, Object>> serviceData;

    private Map<String, String> creditCardData;
    private Map<String, String> masterCardData;
    private Map<String, String> amexCardData;
    private Map<String, String> threeDSCardData;
    private Map<String, String> paypalData;

    public Map<String, String> getPointingUrls() {
        return pointingUrls != null ? pointingUrls : new HashMap<>();
    }

    public void setPointingUrls(Map<String, String> pointingUrls) {
        this.pointingUrls = pointingUrls;
    }

    public Map<String, String> getStores() {
        return stores != null ? stores : new HashMap<>();
    }

    public void setStores(Map<String, String> stores) {
        this.stores = stores;
    }

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

    public Map<String, String> getTariffEndpoints() {
        if (tariffEndpoints == null) {
            return new HashMap<>();
        }
        return tariffEndpoints;
    }

    public void setTariffEndpoints(Map<String, String> tariffEndpoints) {
        this.tariffEndpoints = tariffEndpoints;
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

    public Map<String, String> getShippingAddress() {
        return Util.combined(customerInfo, customerAddress);
    }

    public Map<String, String> getShippingAddressEdit() {
        return Util.combined(customerInfoEdit, customerAddressEdit);
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

    public HashMap<String, String> getTradeUpData() {
        if (tradeUpData == null) {
            return new HashMap<>();
        }
        return tradeUpData;
    }

    public void setTradeUpData(HashMap<String, String> tradeUpData) {
        this.tradeUpData = tradeUpData;
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

    public Map<String, String> getCreditCardData() {
        return creditCardData;
    }

    public void setCreditCardData(Map<String, String> creditCardData) {
        this.creditCardData = creditCardData;
    }

    public Map<String, String> getMasterCardData() {
        return masterCardData;
    }

    public void setMasterCardData(Map<String, String> masterCardData) {
        this.masterCardData = masterCardData;
    }

    public Map<String, String> getAmexCardData() {
        return amexCardData;
    }

    public void setAmexCardData(Map<String, String> amexCardData) {
        this.amexCardData = amexCardData;
    }

    public Map<String, String> getThreeDSCardData() {
        return threeDSCardData;
    }

    public void setThreeDSCardData(Map<String, String> threeDSCardData) {
        this.threeDSCardData = threeDSCardData;
    }

    public Map<String, String> getPaypalData() {
        return paypalData;
    }

    public void setPaypalData(Map<String, String> paypalData) {
        this.paypalData = paypalData;
    }
}
