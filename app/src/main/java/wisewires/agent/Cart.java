package wisewires.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Cart {
    static Logger logger = LoggerFactory.getLogger(Cart.class);

    static List<String> CATEGORIES = List.of("mobile", "tv", "washing machine", "refrigerator", "monitor");

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
                c.mustSCPProcess();
                PD.addSCPlus(c);
            }
            if (addedServices.contains("TradeIn")) {
                c.mustTradeInProcess();
                PD.addTradeIn(c);
            }
            if (addedServices.contains("TradeUp")) {
                PD.addTradeUp(c);
            }
            if (addedServices.contains("EWarranty")) {
                c.mustEWProcess();
                PD.addEWarranty(c);
            }
            PD.continueToCart();
        } else {
            if (addedServices.contains("TradeIn")) {
                c.mustTradeInProcess();
                BC.addTradeIn(c);
            }
            if (addedServices.contains("SC+")) {
                c.mustSCPProcess();
                BC.addSCPlus(c);
            }
            if (addedServices.contains("SIM")) {
                c.mustSIMProcess();
                BC.addSIM(c);
            }
            if (addedServices.contains("GalaxyClub")) {
                c.mustGalaxyClubProcess();
                BC.addGalaxyClub(c);
            }
            BC.continueToCart();
        }
        // FIX: When close popup on AEM but not on Hybrid
        c.unsetPopupClosed();
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
            String to = """
                    cx-cart-item-v2 [data-an-la='add service:trade-up'],
                    cx-cart-item-v2 [data-an-la='add service:tradeinTv']""";
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

    static void addSCPlus(Context c) throws Exception {
        SCPProcess p = c.scpProcess;
        try {
            String to = ".cart-item [data-an-la='add service:samsung care']";
            WebElement btn = WebUI.waitElement(to, 5);
            WebUI.scrollToCenterAndClick(btn, 1000);
            SCPPopup.waitForOpen(10);
            SCPPopup.selectOption(p.selectOption);
            SCPPopup.acceptTermAndConditions();
            SCPPopup.clickConfirm();
            SCPPopup.waitForClose(10);
            logger.info("Popup closed, Samsung Care+ added successfully");
        } catch (Exception e) {
            throw new Exception("Unable to add SC+ on cart page", e);
        }
    }

    static void addEWarranty(Context c) throws Exception {
        EWProcess p = c.ewProcess;
        try {
            String to = ".cart-item [data-an-la='add service:warranty'], .cart-item [data-an-la='add service:EW']";
            WebElement btn = WebUI.waitElement(to, 5);
            WebUI.scrollToCenterAndClick(btn, 1000);
            logger.info("E-Warranty option 'Yes' clicked");
            EWPopup.waitForOpen(10);
            // Handle E-Warranty Popup
            EWPopup.selectFirstOption(p.selectOption);
            EWPopup.acceptTermAndConditions();
            EWPopup.clickConfirm();
            EWPopup.waitForClose(10);
            logger.info("E-Warranty added success on Cart page");
        } catch (Exception e) {
            throw new Exception("Unable to add e-warranty on Cart page");
        }
    }

    static void addGalaxyClub(Context c) throws Exception {
        GalaxyClubProcess p = c.galaxyClubProcess;
        try {
            String to = ".cart-item [data-an-la='add service:samsung galaxy club']";
            WebElement btn = WebUI.waitElement(to, 5);
            WebUI.scrollToCenterAndClick(btn, 1000);
            GalaxyClubPopup.waitForOpen(10);
            GalaxyClubPopup.clickContinue();
            GalaxyClubPopup.selectOption(p.selectOption);
            GalaxyClubPopup.clickConfirm();
            GalaxyClubPopup.waitForClose(10);
            logger.info("Popup closed, Galaxy Club added successfully");
        } catch (Exception e) {
            throw new Exception("Unable to add Galaxy Club on cart page", e);
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

    static void addSIMViaAPI(Context c, String sku, long entryNumber, Map<String, Object> plan) throws Exception {
        try {
            Map<String, Object> data = new HashMap<>(Map.of(
                    "serviceType", "SIM_PLAN",
                    "planId", plan.get("id")));
            if (plan.get("serviceProduct") != null) {
                data.put("serviceCode", plan.get("serviceProduct"));
            } else {
                data.put("serviceCode", API.getSIMServiceCode(c, sku, plan.get("carrier")));
            }
            API.addServiceToCart(c.getAPIEndpoint(), c.getSiteUid(), entryNumber, data);
            logger.info("SIM added success for %s at cart entry %d".formatted(sku, entryNumber));
        } catch (Exception e) {
            throw new Exception("Unable to add SIM  for %s at cart entry %d".formatted(sku, entryNumber));
        }
    }

    static void addWarrantyViaAPI(Context c, String sku, long entryNumber, String code) throws Exception {
        try {
            Map<String, Object> data = Map.of("serviceCode", code);
            API.addServiceToCart(c.getAPIEndpoint(), c.getSiteUid(), entryNumber, data);
            logger.info("Warranty added success for %s at cart entry %d".formatted(sku, entryNumber));
        } catch (Exception e) {
            throw new Exception("Unable to add Warranty for %s at cart entry %d".formatted(sku, entryNumber));
        }
    }

    static void applyVoucher(String voucher) throws Exception {
        try {
            logger.info("applying voucher code %s".formatted(voucher));
            String input = "[formcontrolname='couponCode']";
            WebUI.fill(input, voucher);
            WebUI.delay(1);
            String btnApply = """
                    [data-an-la='coupon:apply'],
                    [data-an-la="promo code:apply"]
                    """;
            WebElement btn = WebUI.findElement(btnApply);
            btn.click();
            WebUI.delay(2);
            WebUI.waitForNotDisplayed("mat-spinner", 60);
        } catch (Exception e) {
            throw new Exception("Unable to apply voucher on cart page", e);
        }
    }

    static void removeVoucher() throws Exception {
        try {
            logger.info("Removing applied voucher code");
            String btnRemove = """
                    [data-an-la="promo code:delete"]
                    """;
            WebElement elm = WebUI.findElements(btnRemove)
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .findFirst()
                    .orElse(null);
            elm.click();
            WebUI.waitForNotDisplayed(".voucher-applied-code", 15);
        } catch (Exception e) {
            throw new Exception("Unable to remove voucher code" + e);
        }
    }

    static void removeAllVoucher() throws Exception {
        try {
            logger.info("Removing all applied voucher code");
            String btnRemoves = """
                    [data-an-la="promo code:delete"]
                    """;
            List<WebElement> elms = WebUI.findElements(btnRemoves)
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .toList();
            for (WebElement elm : elms) {
                elm.click();
                WebUI.delay(2);
            }
        } catch (Exception e) {
            throw new Exception("Unable to remove all voucher code" + e);
        }

    }

    static String getVoucher() {
        return "";
    }

    static int getItemQty(int line) {
        String to = ".cart-item-list:nth-child(%s) input.input-qty".formatted(line);
        WebElement input = WebUI.waitElement(to, 3);
        String value = WebUI.driver.executeScript("return arguments[0].value", input).toString();
        return Integer.parseInt(value);
    }

    static void increaseQty(int line, int count) throws Exception {
        try {
            String to = ".cart-item-list:nth-child(%s) .btn-qty-plus".formatted(line);
            WebElement btn = WebUI.waitElement(to, 3);
            WebUI.scrollToCenter(btn);
            WebUI.delay(1);
            for (int i = 0; i < count; i++) {
                WebUI.click(to);
                WebUI.delay(2);
            }
        } catch (Exception e) {
            throw new Exception("Unable to increase cart item quantity", e);
        }
    }

    static void decreaseQty(int line, int count) throws Exception {
        try {
            String to = ".cart-item-list:nth-child(%s) .decrement_focus".formatted(line);
            WebElement btn = WebUI.waitElement(to, 3);
            WebUI.scrollToCenter(btn);
            WebUI.delay(1);
            for (int i = 0; i < count; i++) {
                WebUI.click(to);
                WebUI.delay(2);
            }
        } catch (Exception e) {
            throw new Exception("Unable to decrease cart item quantity", e);
        }
    }

    static void changeItemQty(int line, int count) throws Exception {
        try {
            String to = ".cart-item-list:nth-child(%s) input.input-qty".formatted(line);
            WebElement input = WebUI.waitElement(to, 3);
            WebUI.scrollToCenter(input);
            WebUI.delay(1);
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
            input.sendKeys(Integer.toString(count) + Keys.ESCAPE);
        } catch (Exception e) {
            throw new Exception("Unable to change cart item quantity", e);
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
        Stream<Map<String, Object>> prods = API.searchProducts(endpoint).stream().filter(Util.inCategories(CATEGORIES));
        List<String> codes = prods.filter(API.CAN_ADD_TO_CART).map(v -> v.get("code").toString()).toList();
        for (int i = 0; i < codes.size(); i += 5) {
            int end = Math.min(i + 5, codes.size());
            List<Map<String, Object>> products = API.getProductByCodes(endpoint, codes.subList(i, end));
            List<Map<String, Object>> variants = Util.getVariants(products);
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
        }
        throw new Exception("Unable to find product to add to cart");
    }

    private static void removeConfirmYes() throws Exception {
        try {
            String to = """
                    app-cart-item-remove-modal [data-an-la='Yes'i],
                    app-cart-item-remove-modal [data-an-la='remove-item'],
                    app-cart-item-remove-modal [data-an-tr='cart-product-remove'],
                    app-remove-service-modal [data-an-tr='cart-product-remove'],
                    #removeEntryConfirmationModal button.js-remove-entry,
                    .modal-remove-service .trade-in-remove-main,
                    .cart-item-remove [data-an-la="remove-item"]""";
            WebElement btn = WebUI.waitElement(to, 3);
            if (btn != null) {
                btn.click();
                WebUI.delay(1);
            }
        } catch (Exception e) {
            throw new Exception("Unable to click remove confirm yes", e);
        }
    }

    static void removeItemByIndex(int line) throws Exception {
        try {
            String to = ".cart-item-list:nth-child(%s) .cart-item__remove--btn".formatted(line);
            WebElement btn = WebUI.waitElement(to, 3);
            WebUI.scrollToCenter(btn);
            WebUI.delay(1);
            btn.click();
            removeConfirmYes();
        } catch (Exception e) {
            throw new Exception("Unable to remove cart item", e);
        }
    }

    static void removeItemBySKU(String sku) throws Exception {
        try {
            String to = "[data-modelcode='%s'] .cart-item__remove--btn".formatted(sku);
            WebElement btn = WebUI.waitElement(to, 3);
            WebUI.scrollToCenter(btn);
            WebUI.delay(1);
            btn.click();
            removeConfirmYes();
        } catch (Exception e) {
            throw new Exception("Unable to remove cart item", e);
        }
    }

    static void removeService(String service) throws Exception {
        try {
            String to = "[data-modelname*='%s'] button[data-an-la='remove item']".formatted(service);
            WebElement btn = WebUI.waitElement(to, 3);
            WebUI.scrollToCenter(btn);
            WebUI.delay(1);
            btn.click();
            removeConfirmYes();
            logger.info("Service %s removed successfully".formatted(service));
        } catch (Exception e) {
            throw new Exception("Unable to remove service %s".formatted(service), e);
        }
    }

    static void removeServiceByIndex(String service, int Line) throws Exception {
        try {
            String to = ".cart-item-list:nth-child(%s) [data-modelname*='%s'] button[data-an-la='remove item']"
                    .formatted(Line, service);
            WebElement btn = WebUI.waitElement(to, 3);
            WebUI.scrollToCenter(btn);
            WebUI.delay(1);
            btn.click();
            removeConfirmYes();
            logger.info("Service %s line %s removed successfully".formatted(service, Line));
        } catch (Exception e) {
            throw new Exception("Unable to remove service %s line %s".formatted(service, Line), e);
        }
    }

    static String getCartAlert() {
        WebElement alert = WebUI.findElement(".cart-alert");
        return alert != null ? alert.getText() : null;
    }

    static void navigateTo(Context c, boolean reloadIfReadyOnCart) throws Exception {
        if (WebUI.isOnSiteCart(c)) {
            if (reloadIfReadyOnCart) {
                WebUI.driver.navigate().refresh();
            }
        } else {
            WebUI.openBrowser(c, c.getCartUrl());
        }
        WebUI.waitElement("cx-cart-details", 15);
    }

    static void guestCheckout(Context c) throws Exception {
        try {
            if (WebUI.isOneOfSites("IQ_AR", "IQ_KU")) {
                Cart.selectCityInCart();
            }
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
                    WebUI.delay(3);
                    WebElement popup = WebUI.findElement("[data-an-la$='continue to guest checkout']");
                    if (popup != null) {
                        popup.click();
                    }
                }
                if (url.contains("contactless/login")) {
                    WebUI.click("[data-an-tr='account-login'][data-an-la='guest']");
                }
                return url.contains("/checkout/one");
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
        try {
            logger.info("Continue to checkout as register");
            String to = "[data-an-la='samsung account']";
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
            String email = c.sso.get("email");
            String password = c.sso.get("mk");
            Object result = WebUI.wait(60).withMessage("navigate to splash page").until(driver -> {
                String alert = getCartAlert();
                if (alert != null) {
                    return new Exception(alert);
                }
                String url = driver.getCurrentUrl();
                if (url.contains("/guestlogin")) {
                    WebElement btn = WebUI.findElement(to);
                    btn.click();
                }
                try {
                    SSO.signInByEmail(email, password);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return url.contains("/checkout/one");
            });
            if (result instanceof Exception) {
                throw (Exception) result;
            }
            logger.info("Navigated to checkout page");
        } catch (Exception e) {
            throw new Exception("Unable to checkout as register", e);
        }
    }

    static void selecCountryInCart(Context c) throws Exception {
        try {
            String to = """
                .btn.cancel-btn.ng-binding,
                .button.pill-btn.pill-btn--white.reset.col""";
            WebElement elmCancel = WebUI.waitElement(to, 3);
            if (elmCancel == null) {
                return;
            }
            Map<String, String> countryMap = Map.of(
                    "kw", "Kuwait",
                    "kw_ar", "الكويت",
                    "om", "Oman",
                    "om_ar", "سلطنة عمان",
                    "bh", "Bahrain",
                    "bh_ar", "البحرين",
                    "qa", "Qatar",
                    "qa_ar", "دولة قطر"
            );
            String countryCode = c.site.toString().toLowerCase();
            String buttonText = countryMap.getOrDefault(countryCode, null);
            WebUI.wait(5).withMessage("Select country").until(d -> {
                String currentUrl = WebUI.driver.getCurrentUrl();
                if (currentUrl.contains("/cart")) {
                    WebElement btnConfirm = WebUI.driver.findElement(By.cssSelector("button.country-selector-button"));
                    WebElement checkbox = WebUI.driver.findElement(By.cssSelector(".country-selector-modal .mat-mdc-checkbox"));
                    if (buttonText != null) {
                        String xpathSelectCountry = String.format("//li[contains(@class,'country-item')]/descendant::span[contains(text(), '%s')]", buttonText);
                        WebElement selectCountry = WebUI.driver.findElement(By.xpath(xpathSelectCountry));
                        if (selectCountry != null) {
                            selectCountry.click();
                            WebUI.delay(1);
                        }
                    }
                    checkbox.click();
                    WebUI.delay(1);
                    btnConfirm.click();
                    logger.info("Country %s selected".formatted(buttonText != null ? buttonText : countryCode));
                    return true;
                }
                return currentUrl.contains(countryCode);
            });
        } catch (Exception e) {
            throw new Exception("Unable to select Country in Cart", e);
        }
    }

    static void selectCityInCart() throws Exception {
        try {
            String dropDownCity = "#mat-select-serverApp0";
            if (WebUI.waitElement(dropDownCity, 3) != null) {
                WebUI.click(dropDownCity);
            }
            String cityItem = ".mdc-list-item";
            List<WebElement> listCity = WebUI.findElements(cityItem);
            if (listCity.size() > 0) {
                for (WebElement city : listCity) {
                    city.click();
                    WebUI.delay(1);
                    String btnCheckout = "button.sticky-cta-enabled";
                    WebElement btnCheckoutElm = WebUI.waitElement(btnCheckout, 3);
                    if (btnCheckoutElm.isEnabled()) {
                        break;
                    } else {
                        WebUI.click(dropDownCity);
                    }
                }
            } else {
                throw new Exception("No city available to select");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to select city in Cart", e);
        }
    }
}
