package wisewires.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public abstract class API {
    static Logger logger = LoggerFactory.getLogger(API.class);

    static Predicate<Map<String, Object>> CAN_ADD_TO_CART = p -> {
        return p.get("addToCartOption").toString().equals("CAN_ADD_TO_CART");
    };

    static void addToCart(String apiEndpoint, String sku) throws Exception {
        try {
            int quantity = 1;
            List<Map<String, Object>> childProducts = null;
            if (sku.contains(":")) {
                String[] tokens = sku.split(":");
                sku = tokens[0];
                String afterColon = tokens[1];

                if (afterColon.matches("\\d+")) {
                    quantity = Integer.parseInt(afterColon);
                } else {
                    childProducts = List.of(
                            Map.of("product", Map.of("code", afterColon), "quantity", 1));
                }
            }
            if (sku.contains("(")) {
                List<String> tokens = new ArrayList<>();
                for (String s : sku.split("[\\s\\(\\),]")) {
                    if (!s.isBlank()) {
                        tokens.add(s);
                    }
                }
                sku = tokens.remove(0);
                childProducts = tokens.stream().map(s -> Map.of("product", Map.of("code", s), "quantity", 1)).toList();
            }
            String url = "%s/users/current/carts/current/entries".formatted(apiEndpoint);
            Object data = Map.of("product", Map.of("code", sku), "quantity", quantity);
            if (childProducts != null) {
                url = "%s/addToCart/multi/?fields=BASIC".formatted(apiEndpoint);
                data = List.of(Map.of("productCode", sku, "qty", quantity, "childProducts", childProducts));
            }
            String script = """
                        return fetch(arguments[0], {
                            method: "POST",
                            headers: {
                                'accept': 'application/json',
                                'content-type':'application/json'
                            },
                            body: `${arguments[1]}`,
                            mode: "cors",
                            credentials: "include"
                        })
                            .then(async res => {
                                if (res.status != 200) {
                                    var data = await res.text();
                                    throw new Error(JSON.parse(data).errors[0].message)
                                }
                                return res.text();
                            })
                            .then(data => arguments[2](JSON.parse(data)));
                    """;
            WebUI.driver.executeAsyncScript(script, url, new Gson().toJson(data));
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
    static List<Map<String, Object>> searchProducts(String endpoint) {
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
    static List<Map<String, Object>> getProductByCodes(String endpoint, List<String> skus) {
        String script = """
                return fetch(`${arguments[0]}/products?productCodes=${arguments[1]}&fields=FULL`, {
                    headers:{'accept':'application/json'},
                    credentials: "include"
                })
                    .then(res => {
                        if (res.status != 200) throw new Error('Get product info by API error with status code: ' + res.status);
                        return res.text();
                    })
                    .then(data => arguments[2](JSON.parse(data)));""";
        return (List<Map<String, Object>>) WebUI.driver.executeAsyncScript(script, endpoint, String.join(",", skus));
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
                    return fetch(`${arguments[0]}/users/current/carts/current/assessment/TRADE_IN/json?provider=${arguments[1] || ''}&lang=${lang}&curr=${curr}`, {
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

    public static List<Map<String, Object>> getKnoxServices(Context c, String sku) throws Exception {
        String category = "knox_manage";
        Map<String, Map<String, Object>> serviceData = c.getProfile().getServiceData();
        if (serviceData.get("knox_manage") != null) {
            category = serviceData.get("knox_manage").get("categoryCode").toString();
        }
        return getExternalServices(c.getAPIEndpoint(), sku, category, "");
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> checkServiceID(String apiEndpoint, Map<String, Object> data) {
        String script = """
                    const curr = JSON.parse(window.localStorage[`spartacus⚿⚿currency`]);
                    const lang = JSON.parse(window.localStorage[`spartacus⚿⚿language`]);
                    return fetch(`${arguments[0]}/users/current/carts/current/checkServiceID?lang=${lang}&curr=${curr}`, {
                        method: "POST",
                        headers:{'content-type':'application/json'},
                        body: `${arguments[1]}`,
                        mode: "cors",
                        credentials: "include"
                    })
                        .then(res => {
                            if (res.status!=200) throw new Error('response status code: ' + res.status);
                            return res.text();
                        })
                        .then(data => arguments[2](JSON.parse(data).serviceParameter));
                """;
        return (List<Map<String, Object>>) WebUI.driver.executeAsyncScript(script, apiEndpoint,
                new Gson().toJson(data));
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
