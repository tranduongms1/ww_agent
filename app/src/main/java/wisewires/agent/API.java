package wisewires.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public abstract class API {
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
                            .then(data => arguments[2](JSON.parse(data).id));
                    """;
            WebUI.driver.executeAsyncScript(script, apiEndpoint, new Gson().toJson(data));
        } catch (Exception e) {
            throw new Exception("Unable to add %s to cart".formatted(sku));
        }
    }

    static void deleteCartEntry(String apiEndpoint, int entry) {
        String script = """
                return fetch(`${arguments[0]}/users/current/carts/current/entries/${arguments[1]}`, {
                    method: "DELETE",
                    mode: "cors",
                    credentials: "include"
                })
                    .then(res => {
                        if (res.status >= 400) throw new Error('response status code: ' + res.status);
                        arguments[2](res.status);
                    })""";
        WebUI.driver.executeAsyncScript(script, apiEndpoint, entry);
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> getCurrentCartInfo(String endpoint) {
        String script = """
                return fetch(`${arguments[0]}/users/current/carts/current?fields=FULL`, {
                    headers:{'accept':'application/json'},
                    mode: "cors",
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
                    mode: "cors",
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
}
