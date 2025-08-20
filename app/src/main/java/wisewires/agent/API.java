package wisewires.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public abstract class API {
    static Logger logger = LoggerFactory.getLogger(API.class);

    static void addToCart(String apiEndpoint, String sku) throws Exception {
        try {
            int quantity = 1;
            if (sku.contains(":")) {
                String[] tokens = sku.split(":");
                sku = tokens[0];
                quantity = Integer.parseInt(tokens[1]);
            }
            Map<String, Object> data = Map.of("product", Map.of("code", sku), "quantity", quantity);
            String script = """
                        return fetch(`${arguments[0]}/users/current/carts/current/entries`, {
                            method: "POST",
                            headers:{'content-type':'application/json'},
                            body: `${arguments[1]}`,
                            credentials: "include"
                        })
                            .then(async res => {
                                if (res.status != 200) {
                                    var data = await res.text();
                                    throw new Error(JSON.parse(data).errors[0].message)
                                }
                                return res.text();
                            })
                            .then(data => arguments[2](JSON.parse(data).id));
                    """;
            WebUI.driver.executeAsyncScript(script, apiEndpoint, new Gson().toJson(data));
            logger.info("Product %s added to cart".formatted(sku));
        } catch (Exception e) {
            throw new Exception("Unable to add %s to cart".formatted(sku));
        }
    }

    public static Object addServiceToCart(String apiEndpoint, String storeID, long entryNumber,
            Map<String, Object> data) {
        String script = """
                    const cardID = window.sessionStorage.ref || JSON.parse(window.localStorage[`spartacus⚿${arguments[0]}⚿cart`]).active;
                    const curr = JSON.parse(window.localStorage[`spartacus⚿⚿currency`]);
                    const lang = JSON.parse(window.localStorage[`spartacus⚿⚿language`]);
                    return fetch(`${arguments[1]}/users/current/carts/${cardID}/entries/${arguments[2]}/services?lang=${lang}&curr=${curr}`, {
                        method: "POST",
                        headers:{'content-type':'application/json'},
                        body: `${arguments[3]}`,
                        credentials: "include"
                    })
                        .then(res => {
                            if (res.status!=200) throw new Error('response status code: ' + res.status);
                            return res.text();
                        })
                        .then(data => arguments[4](data));
                """;
        return WebUI.driver.executeAsyncScript(script, storeID, apiEndpoint, entryNumber, new Gson().toJson(data));
    }

    static void deleteCartEntry(String apiEndpoint, int entry) {
        String script = """
                return fetch(`${arguments[0]}/users/current/carts/current/entries/${arguments[1]}`, {
                    method: "DELETE",
                    credentials: "include"
                })
                    .then(res => {
                        if (res.status >= 500) throw new Error('response status code: ' + res.status);
                        arguments[2](res.status);
                    })""";
        WebUI.driver.executeAsyncScript(script, apiEndpoint, entry);
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> getCurrentCartInfo(String endpoint) {
        String script = """
                return fetch(`${arguments[0]}/users/current/carts/current?fields=FULL`, {
                    headers:{'accept':'application/json'},
                    credentials: "include"
                })
                    .then(res => {
                        if (res.status!=200) throw new Error('Get current cart info by API error with status code: ' + res.status);
                        return res.text();
                    })
                    .then(data => arguments[1](JSON.parse(data)));""";
        return (Map<String, Object>) WebUI.driver.executeAsyncScript(script, endpoint);
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> getProducts(String endpoint) {
        String script = """
                return fetch(`${arguments[0]}/products/search?pageSize=100&fields=FULL`, {
                    headers:{'accept':'application/json'},
                    credentials: "include"
                })
                    .then(res => {
                        if (res.status!=200) throw new Error('Get products by API error with status code: ' + res.status);
                        return res.text();
                    })
                    .then(data => arguments[1](JSON.parse(data).products));""";
        return (List<Map<String, Object>>) WebUI.driver.executeAsyncScript(script, endpoint);
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> getProductVariants(String endpoint) {
        List<Map<String, Object>> variants = new ArrayList<>();
        for (Map<String, Object> product : getProducts(endpoint)) {
            if (product.get("variantOptions") != null) {
                variants.addAll((List<Map<String, Object>>) product.get("variantOptions"));
            }
        }
        return variants;
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> getExternalServices(String apiEndpoint, String sku, String c, String p) {
        String script = """
                    return fetch(`${arguments[0]}/servicesv2/externalService/${arguments[1]}?categoryCode=${arguments[2]}&provider=${arguments[3]}`, {
                        method: "GET",
                        credentials: "include"
                    })
                        .then(res => {
                            if (res.status!=200) throw new Error('response status code: ' + res.status);
                            return res.text();
                        })
                        .then(data => arguments[4](JSON.parse(data).serviceProductDataGroup.serviceProducts.reduce((p, c) => [...p, ...c.serviceProductVariants], [])));
                """;
        return (List<Map<String, Object>>) WebUI.driver.executeAsyncScript(script, apiEndpoint, sku, c, p);
    }

    public static String getSIMServiceCode(String apiEndpoint, String sku, Object carrier) {
        String script = """
                    return fetch(`${arguments[0]}/products/${arguments[1]}?fields=FULL`, {
                        method: "GET",
                        credentials: "include"
                    })
                        .then(res => {
                            if (res.status!=200) throw new Error('response status code: ' + res.status);
                            return res.text();
                        })
                        .then(body => {
                            const code = JSON.parse(body).availableServices
                                .find(s => s.serviceCategory.code.toLowerCase() == 'sim_plan')
                                .addedServiceConfig.configParameters.entry
                                .find(e => e.key =='carrier.service.sku.code.' + arguments[2]).value;
                            arguments[3](code);
                        });
                """;
        return WebUI.driver.executeAsyncScript(script, apiEndpoint, sku, carrier).toString();
    }

    public static List<Map<String, Object>> getSMCServices(Context c, String sku) throws Exception {
        String category = "smc";
        Map<String, Map<String, Object>> serviceData = c.getProfile().getServiceData();
        if (serviceData.get("SMC") != null) {
            category = serviceData.get("SMC").get("categoryCode").toString();
        }
        return getExternalServices(c.getAPIEndpoint(), sku, category, "SMC");
    }

    public static Map<String, Object> getStdSMCService(Context c, String sku) throws Exception {
        return getSMCServices(c, sku).stream()
                .filter(s -> !s.containsKey("subscriptionFrequency"))
                .findFirst()
                .orElse(null);
    }

    public static Map<String, Object> getSubSMCService(Context c, String sku) throws Exception {
        return getSMCServices(c, sku).stream()
                .filter(s -> s.containsKey("subscriptionFrequency"))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getTradeInAdditionalInfos(String apiEndpoint, Map<String, Object> data) {
        String body = new Gson().toJson(data);
        String script = """
                    const curr = JSON.parse(window.localStorage[`spartacus⚿⚿currency`]);
                    const lang = JSON.parse(window.localStorage[`spartacus⚿⚿language`]);
                    return fetch(`${arguments[0]}/users/current/carts/current/assessment/TRADE_IN/json?provider=${arguments[1]}&lang=${lang}&curr=${curr}`, {
                        method: "POST",
                        headers: {"Content-Type": "application/json"},
                        body: `${arguments[2]}`,
                        credentials: "include"
                    })
                        .then(res => {
                            if (res.status!=200) throw new Error('response status code: ' + res.status);
                            return res.text();
                        })
                        .then(data => arguments[3](JSON.parse(data).result.additionalInfos));
                """;
        return (List<Object>) WebUI.driver.executeAsyncScript(script, apiEndpoint, data.get("provider"), body);
    }

    public static String getTradeInDevice(String exchangeEndpoint, String site, String targetSKU) {
        String script = """
                    return fetch(`${arguments[0]}/trade-in/sku-devices/${arguments[1]}/${arguments[2]}`)
                        .then(res => {
                            if (res.status!=200) throw new Error('Get Trade-in device error with status code: ' + res.status);
                            return res.text();
                        })
                        .then(data => arguments[3](JSON.parse(data)[0].id));
                """;
        return WebUI.driver
                .executeAsyncScript(script, exchangeEndpoint, site.toLowerCase(), targetSKU)
                .toString();
    }

    public static String createExchangeId(String exchangeEndpoint, Map<String, Object> data) {
        String script = """
                    return fetch(`${arguments[0]}/trade-in/create-exchange`, {
                        method: "POST",
                        headers:{'content-type':'application/json'},
                        body: `${arguments[1]}`,
                        credentials: "include"
                    })
                        .then(res => {
                            if (res.status!=200) throw new Error('response status code: ' + res.status);
                            return res.text();
                        })
                        .then(data => arguments[2](JSON.parse(data).id));
                """;
        String body = new Gson().toJson(data);
        return WebUI.driver.executeAsyncScript(script, exchangeEndpoint, body).toString();
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getSIMPlans(Context c, String sku) throws Exception {
        String url = c.getTariffEndpoint() != null
                ? "%s/sku/%s/plans?fields=DEFAULT".formatted(c.getTariffEndpoint(), sku)
                : "%s/carriers/device/%s/plans?fields=DEFAULT".formatted(c.getAPIEndpoint(), sku);
        String script = """
                    return fetch(arguments[0], {
                        method: "GET",
                        credentials: "include"
                    })
                        .then(res => {
                            if (res.status!=200) throw new Error('response status code: ' + res.status);
                            return res.text();
                        })
                        .then(data => arguments[1](JSON.parse(data).carriers.reduce((p, c) => [...p, ...c.tariffPlans], [])));
                """;
        return (List<Map<String, Object>>) WebUI.driver.executeAsyncScript(script, url);
    }

    public static String getSIMServiceCode(Context c, String sku, Object carrier) throws Exception {
        String script = """
                    return fetch(`${arguments[0]}/products/${arguments[1]}?fields=FULL`, {
                        method: "GET",
                        mode: "cors",
                        credentials: "include"
                    })
                        .then(res => {
                            if (res.status!=200) throw new Error('response status code: ' + res.status);
                            return res.text();
                        })
                        .then(body => {
                            const code = JSON.parse(body).availableServices
                                .find(s => s.serviceCategory.code.toLowerCase() == 'sim_plan')
                                .addedServiceConfig.configParameters.entry
                                .find(e => e.key =='carrier.service.sku.code.' + arguments[2]).value;
                            arguments[3](code);
                        });
                """;
        return WebUI.driver.executeAsyncScript(script, c.getAPIEndpoint(), sku, carrier).toString();
    }

    public static List<Map<String, Object>> getWarrantyServices(Context c, String sku) throws Exception {
        String category = "warranty";
        Map<String, Map<String, Object>> serviceData = c.getProfile().getServiceData();
        if (serviceData.get("Warranty") != null) {
            category = serviceData.get("Warranty").get("categoryCode").toString();
        }
        return getExternalServices(c.getAPIEndpoint(), sku, category, "");
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> getServiceLocations(String apiEndpoint, Map<String, Object> params) {
        String query = String.join("&", params.keySet().stream().map(k -> k + "=" + params.get(k)).toList());
        String script = """
                    return fetch(`${arguments[0]}/servicesv2/mcs/getServiceLocations?${arguments[1]}`, {
                        method: "GET",
                        credentials: "include"
                    })
                        .then(res => {
                            if (res.status!=200) throw new Error('response status code: ' + res.status);
                            return res.text();
                        })
                        .then(data => arguments[2](JSON.parse(data)));
                """;
        return (Map<String, Object>) WebUI.driver.executeAsyncScript(script, apiEndpoint, query);
    }
}
