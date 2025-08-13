package wisewires.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Cart {
    static Logger logger = LoggerFactory.getLogger(Cart.class);

    static String getCartId(Context c) {
        if (WebUI.driver != null) {
            String js = "return window.sessionStorage.ref || window.localStorage[`spartacus⚿${arguments[0]}⚿cart`]";
            return (String) WebUI.driver.executeScript(js, c.getSiteUid());
        }
        return null;
    }

    static String waitCartId(Context c) {
        return WebUI.wait(30).withMessage("has cart id").until(d -> getCartId(c));
    }

    static String mustCartId(Context c) throws Exception {
        String id = getCartId(c);
        if (id == null || id.isEmpty()) {
            WebUI.openBrowser(c, c.getCartUrl());
            id = waitCartId(c);
        }
        return id;
    }

    public static void addProduct(Context c, String url, List<String> addedServices) throws Exception {
        WebUI.openBrowser(c, url);
        WebUI.mustCloseAllPopup(c);
        if (Util.isPDPage()) {
            if (addedServices.contains("SC+")) {
                PD.addSCPlus(c);
            }
            if (addedServices.contains("TradeIn")) {
                PD.addTradeIn(c);
            }
            if (addedServices.contains("TradeUp")) {
                PD.addTradeUp(c);
            }
            if (addedServices.contains("EWarranty")) {
                PD.addEWarranty(c);
            }
            PD.continueToCart();
        } else {
            if (addedServices.contains("TradeIn")) {
                BC.addTradeIn(c);
            }
            if (addedServices.contains("SC+")) {
                BC.addSCPlus(c);
            }
            BC.continueToCart();
        }
    }

    static void addTradeIn(Context c) throws Exception {
        try {
            String to = "cx-cart-item-v2 [data-an-la='add service:trade-in']";
            WebElement elm = WebUI.waitElement(to, 10);
            WebUI.scrollToCenter(elm);
            WebUI.delay(1);
            WebUI.click(to);
            logger.info("Add trade-in button clicked");
            WebUI.waitElement(TradeIn.MODAL_LOCATOR, 10);
            logger.info("Trade-in popup opened");
            TradeIn.process(c);
            logger.info("Trade-in added success on cart page");
        } catch (Exception e) {
            throw new Exception("Unable to add trade-in on cart page");
        }
    }

    static void addTradeUp(Context c) throws Exception {
        try {
            String to = "cx-cart-item-v2 [data-an-la='add service:trade-up']";
            WebElement elm = WebUI.waitElement(to, 10);
            WebUI.scrollToCenter(elm);
            WebUI.delay(1);
            WebUI.click(to);
            logger.info("Add trade-up button clicked");
            WebUI.waitElement(TradeUp.MODAL_LOCATOR, 10);
            logger.info("Trade-up popup opened");
            TradeUp.process(c);
            logger.info("Trade-up added success on cart page");
        } catch (Exception e) {
            throw new Exception("Unable to add trade-up on cart page");
        }
    }

    @SuppressWarnings("unchecked")
    static void addTradeInViaAPI(Context c, String sku, long entryNumber) throws Exception {
        try {
            Profile p = c.getProfile();
            Map<String, Object> data = p.getServiceData().get("TradeIn");
            if (data.containsKey("serviceCode")) {
                if (data.get("additionalInfos") instanceof Map) {
                    Map<String, Object> payload = (Map<String, Object>) data.get("additionalInfos");
                    payload.put("orderEntryNumber", entryNumber);
                    payload.put("targetProductCode", sku);
                    List<Object> info = API.getTradeInAdditionalInfos(c.getAPIEndpoint(), payload);
                    info.add(Map.of("key", "IMEI", "value", MockData.IMEI()));
                    data.put("additionalInfos", info);
                }
                API.addServiceToCart(c.getAPIEndpoint(), c.getSiteUid(), entryNumber, data);
            } else {
                String deviceId = API.getTradeInDevice(c.getExchangeEndpoint(), c.site, sku);
                data.put("device_id", deviceId);
                data.put("sku", sku);
                String exchangeId = API.createExchangeId(c.getExchangeEndpoint(), data);
                Map<String, Object> exchangeData = Map.of("exchangeId", exchangeId);
                API.addServiceToCart(c.getAPIEndpoint(), c.getSiteUid(), entryNumber, exchangeData);
            }
            logger.info("Trade-in added success for %s at cart entry %d".formatted(sku, entryNumber));
        } catch (Exception e) {
            throw new Exception("Unable to add trade-in for %s at cart entry %d".formatted(sku, entryNumber));
        }
    }

    static void addTradeUpViaAPI(Context c, String sku, long entryNumber) throws Exception {
        try {
            Profile p = c.getProfile();
            Map<String, Object> data = p.getServiceData().get("TradeUp");
            if (data.containsKey("serviceCode")) {
                API.addServiceToCart(c.getAPIEndpoint(), c.getSiteUid(), entryNumber, data);
            } else {
                String deviceId = API.getTradeInDevice(c.getExchangeEndpoint(), c.site.split("_")[0], sku);
                data.put("device_id", deviceId);
                data.put("sku", sku);
                String exchangeId = API.createExchangeId(c.getExchangeEndpoint(), data);
                Map<String, Object> exchangeData = Map.of("exchangeId", exchangeId, "provider", "EXCHANGE");
                API.addServiceToCart(c.getAPIEndpoint(), c.getSiteUid(), entryNumber, exchangeData);
            }
        } catch (Exception e) {
            throw new Exception("Unable to add trade-up for %s at cart entry %d".formatted(sku, entryNumber));
        }
    }

    static void addSMCViaAPI(Context c, long entryNumber, Map<String, Object> option) throws Exception {
        try {
            Map<String, Object> info = new HashMap<>(Map.of("key", "paymentCycle"));
            if (option.get("subscriptionFrequency") != null) {
                info.put("value", option.get("subscriptionFrequency"));
            }
            Map<String, Object> data = Map.of("serviceCode", option.get("code"), "additionalInfos", List.of(info));
            API.addServiceToCart(c.getAPIEndpoint(), c.getSiteUid(), entryNumber, data);
            logger.info("SC+ with code %s added to cart entry %d".formatted(option.get("code"), entryNumber));
        } catch (Exception e) {
            throw new Exception("Unable to add SC+ at cart entry %d".formatted(entryNumber));
        }
    }

    static void clickCheckoutButton() throws Exception {
        try {
            WebUI.wait(10).until(d -> {
                WebElement btn = WebUI.waitElement("[data-an-la='proceed to checkout']", 30);
                WebUI.scrollToCenter(btn);
                WebUI.delay(1);
                btn.click();
                return true;
            });
        } catch (Exception e) {
            throw new Exception("Unable to click checkout button on cart page", e);
        }
    }

    @SuppressWarnings("unchecked")
    static boolean mustEmpty(Context c) throws Exception {
        mustCartId(c);
        String endpoint = c.getAPIEndpoint();
        Map<String, Object> cart = API.getCurrentCartInfo(endpoint);
        List<Object> entries = (List<Object>) cart.get("entries");
        if (entries.isEmpty()) {
            logger.info("Cart is EMPTY now");
            return true;
        }
        entries.forEach(e -> {
            API.deleteCartEntry(endpoint, 0);
        });
        return false;
    }

    static boolean mustNotEmpty(Context c) throws Exception {
        mustCartId(c);
        String endpoint = c.getAPIEndpoint();
        Map<String, Object> cart = API.getCurrentCartInfo(endpoint);
        long totalItems = (long) cart.get("totalItems");
        if (totalItems > 0)
            return true;
        List<Map<String, Object>> variants = API.getProductVariants(endpoint);
        variants = new ArrayList<>(variants.stream().filter(Util::isPurchasable).toList());
        Collections.shuffle(variants);
        for (Map<String, Object> variant : variants) {
            try {
                API.addToCart(endpoint, (String) variant.get("code"));
                logger.info("Cart is NOT empty now");
                return false;
            } catch (Exception ignore) {
            }
        }
        throw new Exception("Unable to find product to add to cart");
    }

    static String getCartAlert() {
        WebElement alert = WebUI.findElement(".cart-alert");
        return alert != null ? alert.getText() : null;
    }

    static void navigateTo(Context c, boolean reloadIfReadyOnCart) throws Exception {
        if (WebUI.driver != null && WebUI.getUrl().contains("/cart")) {
            if (reloadIfReadyOnCart) {
                WebUI.driver.navigate().refresh();
            }
        } else {
            WebUI.openBrowser(c, c.getCartUrl());
        }
    }

    static void guestCheckout(Context c) throws Exception {
        try {
            logger.info("Continue to checkout as guest");
            String to = """
                    [data-an-la='proceed to checkout:guest checkout'],
                    [data-an-la='continue as guest']""";
            WebUI.wait(10).until(d -> {
                WebElement btn = WebUI.findElement(to);
                if (btn == null) {
                    btn = WebUI.findElement("[data-an-la='proceed to checkout']");
                }
                WebUI.scrollToCenter(btn);
                WebUI.delay(1);
                btn.click();
                return true;
            });
            String email = c.getProfile().getCustomerInfo().get("email");
            Object result = WebUI.wait(30, 1).withMessage("navigate to checkout").until(driver -> {
                String alert = getCartAlert();
                if (alert != null) {
                    return new Exception(alert);
                }
                String url = driver.getCurrentUrl();
                if (url.contains("/guestlogin/")) {
                    WebUI.fill("[formcontrolname='guestEmail']", email);
                    logger.info("Email %s used to checkout".formatted(email));
                    WebUI.delay(3);
                    WebUI.click("[data-an-tr='account-login'][data-an-la='guest'].pill-btn");
                }
                return url.contains("/checkout");
            });
            if (result instanceof Exception) {
                throw (Exception) result;
            }
            logger.info("Navigated to checkout page");
        } catch (Exception e) {
            throw new Exception("Unable to checkout as guest", e);
        }
    }

    static void ssoCheckout(Context c) throws Exception {

    }
}
