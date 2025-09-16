package wisewires.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Browser {
    private static Logger logger = LoggerFactory.getLogger(Browser.class);

    static void run(Context c, String req) throws Exception {
        List<String> tokens = Tokens.tokenize(req);
        String arg = tokens.remove(0);
        List<String> leading;
        switch (arg.toLowerCase()) {
            case "on": {
                if (tokens.contains("site")) {
                    c.site = tokens.remove(0).toUpperCase();
                    logger.info("Change current site to %s".formatted(c.site));
                } else if (tokens.contains("store")) {
                    if (tokens.contains("estore")) {
                        c.siteUid = "";
                    } else {
                        c.siteUid = tokens.remove(0).toLowerCase();
                    }
                    logger.info("Change current store to %s".formatted(c.siteUid.isEmpty() ? "estore" : c.siteUid));
                } else if (tokens.contains("env")) {
                    c.env = tokens.remove(0).toLowerCase();
                    logger.info("Change current env to %s".formatted(c.env));
                }
                break;
            }

            case "set", "use": {
                if (Tokens.containsAny(tokens, "sso", "checkout")) {
                    while (!tokens.isEmpty()) {
                        leading = Tokens.removeLeading(tokens, "sso", "checkout", "email", "and", "password");
                        if (leading.contains("email")) {
                            c.sso.put("email", tokens.remove(0));
                            c.getProfile().getCustomerInfo().put("email", c.sso.get("email"));
                            logger.info("Change checkout email to %s".formatted(c.sso.get("email")));
                        }
                        if (leading.contains("password")) {
                            c.sso.put("mk", tokens.remove(0));
                            logger.info("Change checkout password to %s".formatted(c.sso.get("mk")));
                        }
                    }
                }
                break;
            }

            case "delay":
                WebUI.delay(Integer.parseInt(tokens.get(0)));
                break;

            case "open", "go", "navigate": {
                leading = Tokens.removeLeading(tokens, "to", "home", "shop", "cart", "pf", "pd", "bc", "page");
                if (leading.containsAll(List.of("shop", "home"))) {
                    WebUI.openBrowser(c, c.getShopUrl());
                } else if (leading.contains("home")) {
                    WebUI.openBrowser(c, c.getHomeUrl());
                } else if (leading.contains("cart")) {
                    WebUI.openBrowser(c, c.getCartUrl());
                } else {
                    WebUI.openBrowser(c, tokens.get(0));
                }
                Cart.selecCountryInCart(c);
                WebUI.mustCloseAllPopup(c);
                break;
            }

            case "login": {
                SSO.mustSignedOut(c);
                Tokens.removeLeading(tokens, "from", "on");
                leading = Tokens.removeLeading(tokens, "aem", "shop", "empty", "cart", "gnb", "splash", "page");
                if (leading.contains("aem")) {
                    Login.fromAEMGNB(c);
                } else if (leading.contains("shop")) {
                    if (leading.contains("gnb")) {
                        Login.fromShopGNB(c);
                    } else {
                        Login.fromShop(c);
                    }
                } else if (leading.contains("cart")) {
                    if (leading.contains("gnb")) {
                        Login.fromCartGNB(c);
                    } else if (leading.contains("empty")) {
                        Login.fromEmptyCart(c);
                    } else {
                        Login.fromCart(c);
                    }
                } else if (leading.contains("splash")) {
                    Login.fromSplash(c);
                } else {
                    SSO.signInByEmail(c);
                }
                if (!WebUI.getUrl().contains("/checkout/one")) {
                    if (WebUI.isOneOfSites("TH")) {
                        WebUI.closeAllPopup(c);
                    }
                    WebUI.waitForPageLoad(30);
                    GNB.hoverHumanIcon();
                }
                logger.info("[PASS] %s".formatted(req));
                break;
            }

            case "hover": {
                Tokens.removeLeading(tokens, "to");
                leading = Tokens.removeLeading(tokens, "human", "icon");
                if (leading.contains("human")) {
                    GNB.hoverHumanIcon();
                }
                break;
            }

            case "logout": {
                leading = Tokens.removeLeading(tokens, "from", "on", "aem", "shop", "cart", "gnb", "page");
                if (leading.contains("aem")) {
                    SSO.mustSignedIn(c, "aem");
                    Logout.fromAEM(c);
                } else if (leading.contains("shop")) {
                    SSO.mustSignedIn(c, "shop");
                    if (leading.contains("gnb")) {
                        Logout.fromShopGNB(c);
                    } else {
                        Logout.fromShop(c);
                    }
                } else if (leading.contains("cart")) {
                    SSO.mustSignedIn(c, "cart");
                    if (leading.contains("gnb")) {
                        Logout.fromCartGNB(c);
                    } else {
                        Logout.fromCart(c);
                    }
                } else {
                    SSO.mustSignedOut(c);
                }
                break;
            }

            case "find": {
                Tokens.removeLeading(tokens, "product");
                String productName = String.join(" ", tokens);
                c.pfProcess = new PFProcess();
                PF.findProductCard(c, productName);
                break;
            }

            case "add": {
                leading = Tokens.removeLeading(tokens, "first", "second", "third");
                if (tokens.get(0).equalsIgnoreCase("trade-in")) {
                    c.mustTradeInProcess();
                    if (WebUI.getUrl().contains("/cart")) {
                        if (leading.contains("second")) {
                            c.tradeInProcess.data = c.getProfile().getTradeInData2();
                        } else if (leading.contains("third")) {
                            c.tradeInProcess.data = c.getProfile().getTradeInData3();
                        }
                        Cart.addTradeIn(c);
                    } else if (Util.isPDPage()) {
                        PD.addTradeIn(c);
                    } else {
                        BC.addTradeIn(c);
                    }
                    break;
                }
                if (tokens.get(0).equalsIgnoreCase("trade-up")) {
                    if (WebUI.getUrl().contains("/cart")) {
                        Cart.addTradeUp(c);
                    } else {
                        PD.addTradeUp(c);
                    }
                    break;
                }
                if (tokens.get(0).equalsIgnoreCase("sc+")) {
                    c.mustSCPProcess();
                    if (WebUI.getUrl().contains("/cart")) {
                        Cart.addSCPlus(c);
                    } else {
                        BC.addSCPlus(c);
                    }
                    break;
                }
                if (tokens.get(0).equalsIgnoreCase("galaxy-club")) {
                    c.mustGalaxyClubProcess();
                    if (WebUI.getUrl().contains("/cart")) {
                        Cart.addGalaxyClub(c);
                        ;
                    } else {
                        BC.addGalaxyClub(c);
                    }
                    break;
                }
                if (tokens.get(0).equalsIgnoreCase("e-warranty")) {
                    c.mustEWProcess();
                    if (WebUI.getUrl().contains("/cart")) {
                        Cart.addEWarranty(c);
                    } else {
                        PD.addEWarranty(c);
                    }
                }
                if (tokens.get(0).equalsIgnoreCase("sim")) {
                    c.mustSIMProcess();
                    BC.addSIM(c);
                    break;
                }
                if (tokens.contains("cart")) {
                    boolean mustReload = false;
                    class Item {
                        String urlOrSKU;
                        List<String> addedServices = new ArrayList<>();

                        @Override
                        public String toString() {
                            return urlOrSKU + " with added services " + addedServices;
                        }
                    }
                    List<Item> items = new ArrayList<>();
                    while (!tokens.isEmpty()) {
                        Tokens.removeLeading(tokens, "product", "and", "+", "from", "bc", "pd", "page", "to", "cart");
                        if (!tokens.isEmpty()) {
                            Item item = new Item();
                            item.urlOrSKU = tokens.remove(0);
                            leading = Tokens.removeLeading(tokens, "with",
                                    "trade-in", "tradein", "trade-up", "tradeup",
                                    "sc+", "smc", "std", "standard", "sub", "subscription",
                                    "sim", "e-warranty", "ewarranty", "warranty",
                                    "galaxy-club", "galaxy club", "galaxyclub",
                                    "and", "+");
                            if (Tokens.containsAny(leading, "trade-in", "tradein")) {
                                item.addedServices.add("TradeIn");
                            }
                            if (Tokens.containsAny(leading, "trade-up", "tradeup")) {
                                item.addedServices.add("TradeUp");
                            }
                            if (Tokens.containsAny(leading, "sc+", "smc")) {
                                if (Tokens.containsAny(leading, "sub", "subscription")) {
                                    item.addedServices.add("SUB-SC+");
                                } else if (Tokens.containsAny(leading, "std", "standard")) {
                                    item.addedServices.add("STD-SC+");
                                } else {
                                    item.addedServices.add("SC+");
                                }
                            }
                            if (Tokens.containsAny(leading, "sim")) {
                                item.addedServices.add("SIM");
                            }
                            if (Tokens.containsAny(leading, "e-warranty", "ewarranty", "warranty")) {
                                item.addedServices.add("Warranty");
                            }
                            if (Tokens.containsAny(leading, "galaxy-club", "galaxy club", "galaxyclub")) {
                                item.addedServices.add("GalaxyClub");
                            }
                            items.add(item);
                        }
                    }
                    for (Item item : items) {
                        logger.info("Add to cart: " + item);
                        if (item.urlOrSKU.startsWith("https://")) {
                            Cart.addProduct(c, item.urlOrSKU, item.addedServices);
                            mustReload = false;
                        } else {
                            Cart.mustCartId(c);
                            String sku = item.urlOrSKU;
                            API.addToCart(c.getAPIEndpoint(), sku);
                            if (!item.addedServices.isEmpty()) {
                                Thread.sleep(2000);
                                Map<String, Object> cartInfo = API.getCurrentCartInfo(c.getAPIEndpoint());
                                long entryNumber = Util.getCartEntryNumber(cartInfo, sku);
                                if (item.addedServices.contains("TradeIn")) {
                                    Cart.addTradeInViaAPI(c, sku, entryNumber);
                                }
                                if (item.addedServices.contains("TradeUp")) {
                                    Cart.addTradeUpViaAPI(c, sku, entryNumber);
                                }
                                if (item.addedServices.contains("SUB-SC+")) {
                                    Map<String, Object> option = API.getSubSMCService(c, sku);
                                    if (option == null)
                                        throw new Exception("No Subcription SC+ avaiable for %s".formatted(sku));
                                    Cart.addSMCViaAPI(c, entryNumber, option);
                                }
                                if (item.addedServices.contains("STD-SC+")) {
                                    Map<String, Object> option = API.getStdSMCService(c, sku);
                                    if (option == null)
                                        throw new Exception("No Standard SC+ avaiable for %s".formatted(sku));
                                    Cart.addSMCViaAPI(c, entryNumber, option);
                                }
                                if (item.addedServices.contains("SC+")) {
                                    Map<String, Object> option = API.getSMCServices(c, sku)
                                            .stream().findFirst().orElse(null);
                                    if (option == null)
                                        throw new Exception("No SC+ avaiable for %s".formatted(sku));
                                    Cart.addSMCViaAPI(c, entryNumber, option);
                                }
                                if (item.addedServices.contains("SIM")) {
                                    List<Map<String, Object>> plans = API.getSIMPlans(c, sku);
                                    if (plans.isEmpty())
                                        throw new Exception("No SIM plan avaiable for %s".formatted(sku));
                                    Map<String, Object> plan = plans.get(0);
                                    Cart.addSIMViaAPI(c, sku, entryNumber, plan);
                                }
                                if (item.addedServices.contains("Warranty")) {
                                    List<Map<String, Object>> services = API.getWarrantyServices(c, sku);
                                    if (services.isEmpty())
                                        throw new Exception("No Warranty services avaiable for %s".formatted(sku));
                                    String code = (String) services.get(0).get("code");
                                    Cart.addWarrantyViaAPI(c, sku, entryNumber, code);
                                }
                            }
                            mustReload = true;
                        }
                    }
                    if (WebUI.isOneOfSites("AE", "AE_AR", "BH", "BH_AR", "OM", "OM_AR", "QA", "QA_AR")) {
                        Cart.selecCountryInCart(c);
                        WebUI.delay(2);
                    }
                    Cart.navigateTo(c, mustReload);
                    WebUI.mustCloseAllPopup(c);
                }
                break;
            }

            case "apply": {
                if (tokens.get(0).equalsIgnoreCase("voucher")) {
                    Tokens.removeLeading(tokens, "voucher", "code");
                    if (WebUI.getUrl().contains("/cart")) {
                        Cart.applyVoucher(tokens.get(0));
                    } else {
                        Checkout.applyVoucher(tokens.get(0));
                    }
                    break;
                }
                break;
            }
            case "ensure": {
                leading = Tokens.removeLeading(tokens, "cart", "page", "is", "not", "empty");
                if (Tokens.containsAll(leading, "cart", "empty")) {
                    boolean ready;
                    if (leading.contains("not")) {
                        ready = Cart.mustNotEmpty(c);
                    } else {
                        ready = Cart.mustEmpty(c);
                    }
                    Cart.navigateTo(c, !ready);
                    WebUI.mustCloseAllPopup(c);
                }
                break;
            }

            case "increase", "decrease": {
                int line = 1;
                leading = Tokens.removeLeading(tokens, "cart", "item", "quantity", "qty", "at", "line");
                if (leading.contains("line")) {
                    line = Integer.parseInt(tokens.remove(0));
                }
                leading = Tokens.removeLeading(tokens, "to", "by");
                if (leading.contains("to")) {
                    int target = Integer.parseInt(tokens.remove(0));
                    logger.info("%s cart item at line %d to %d".formatted(arg, line, target));
                    int initial = Cart.getItemQty(line);
                    if (arg.equalsIgnoreCase("increase")) {
                        Cart.increaseQty(line, target - initial);
                    } else {
                        Cart.decreaseQty(line, initial - target);
                    }
                } else if (Tokens.containsAny(leading, "by")) {
                    int count = Integer.parseInt(tokens.remove(0));
                    logger.info("%s cart item at line %d by %d".formatted(arg, line, count));
                    if (arg.equalsIgnoreCase("increase")) {
                        Cart.increaseQty(line, count);
                    } else {
                        Cart.decreaseQty(line, count);
                    }
                } else {
                    if (arg.equalsIgnoreCase("increase")) {
                        Cart.increaseQty(line, 1);
                    } else {
                        Cart.decreaseQty(line, 1);
                    }
                }
                break;
            }

            case "change": {
                int line = 1;
                leading = Tokens.removeLeading(tokens, "cart", "item", "quantity", "qty", "at", "line");
                if (leading.contains("line")) {
                    line = Integer.parseInt(tokens.remove(0));
                }
                leading = Tokens.removeLeading(tokens, "to");
                if (leading.contains("to")) {
                    int qty = Integer.parseInt(tokens.remove(0));
                    Cart.changeItemQty(line, qty);
                }
                break;
            }

            case "remove": {
                leading = Tokens.removeLeading(tokens, "product", "item", "by", "with", "sku",
                        "at", "line", "from", "cart", "for", "service");
                if (leading.contains("line")) {
                    int line = Integer.parseInt(tokens.remove(0));
                    Cart.removeItemByIndex(line);
                }
                if (leading.contains("sku")) {
                    String sku = tokens.remove(0).toUpperCase();
                    Cart.removeItemBySKU(sku);
                }
                if (leading.contains("service")) {
                    String service = tokens.remove(0).toUpperCase();
                    if (service.equalsIgnoreCase("SC+")) {
                        service = "SMC";
                    } else if (service.equalsIgnoreCase("E-WARRANTY")) {
                        service = "WARRANTY";
                    } else if (service.equalsIgnoreCase("TRADE-IN")) {
                        service = "TRADE-IN";
                    } else if (service.equalsIgnoreCase("TRADE-UP")) {
                        service = "TRADE-UP";
                    } else if (service.equalsIgnoreCase("SIM")) {
                        service = "SIM";
                    }
                    if (leading.contains("line")) {
                        int line = Integer.parseInt(tokens.remove(0));
                        Cart.removeServiceByIndex(service, line);
                    } else {
                        Cart.removeService(service);
                    }
                }
                if (tokens.size() >= 1 && tokens.get(0).equalsIgnoreCase("voucher")) {
                    Cart.removeVoucher();
                    Tokens.removeLeading(tokens, "voucher");
                    break;
                }
                if (tokens.size() >= 2
                        && tokens.get(0).equalsIgnoreCase("all")
                        && tokens.get(1).equalsIgnoreCase("voucher")) {
                    Cart.removeAllVoucher();
                    Tokens.removeLeading(tokens, "all", "voucher");
                    break;
                }
                break;
            }

            case "fill": {
                TokenSingleMatch match = Tokens.getFormName(tokens);
                boolean all = Tokens.containsAny(match.leading, "all");
                if (c.checkoutProcess != null) {
                    Checkout.process(c);
                }
                Profile profile = c.getProfile();
                switch (match.value) {
                    case "customer info":
                        CustomerInfo.autoFill(profile.getCustomerInfo(), !all);
                        break;
                    case "customer address":
                        CustomerAddress.autoFill(profile.getCustomerAddress(), !all);
                        break;
                    case "billing address":
                        BillingAddress.autoFill(profile.getBillingAddress(), !all);
                        break;
                }
                if (Tokens.containsAll(tokens, "and", "continue")
                        || Tokens.containsAll(tokens, "and", "next")) {
                    Checkout.nextStep();
                }
                break;
            }

            case "select": {
                if (c.pfProcess != null) {
                    while (!tokens.isEmpty()) {
                        String value = tokens.remove(0);
                        leading = Tokens.removeLeading(tokens, "color", "and", "storage");
                        if (leading.contains("color")) {
                            PF.selectColor(c, value);
                        } else if (leading.contains("storage")) {
                            PF.selectStorage(c, value);
                        } else {
                            break;
                        }
                    }
                    break;
                }
                if (Tokens.containsAll(tokens, "different", "billing", "address")) {
                    c.mustCheckoutProcess().uncheckSameAsShippingAddress();
                    break;
                }
                if (tokens.get(0).equalsIgnoreCase("new")) {
                    tokens.remove(0);
                    TokenSingleMatch match = Tokens.getFormName(tokens);
                    switch (match.value) {
                        case "customer address":
                            c.mustCheckoutProcess().selectNewCustomerAddress();
                            break;
                        case "billing address":
                            c.mustCheckoutProcess().selectNewBillingAddress();
                            break;
                    }
                    break;
                }
                if (Tokens.containsAny(tokens, "saved")) {
                    leading = Tokens.removeLeading(tokens,
                            "1st", "first", "2nd", "second", "3rd", "third",
                            "saved", "billing", "customer", "address");

                    int idx = -1;
                    if (Tokens.containsAny(leading, "1st", "first")) {
                        idx = 0;
                    } else if (Tokens.containsAny(leading, "2nd", "second")) {
                        idx = 1;
                    } else if (Tokens.containsAny(leading, "3rd", "third")) {
                        idx = 2;
                    }
                    if (Tokens.containsAll(leading, "billing")) {
                        if (idx >= 0) {
                            c.mustCheckoutProcess().selectSavedBillingAddress(idx);
                            break;
                        }
                    } else if (Tokens.containsAny(leading, "customer")) {
                        if (idx >= 0) {
                            c.mustCheckoutProcess().selectSavedCustomerAddress(idx);
                            break;
                        }
                    }
                }

                if (Tokens.containsAny(tokens, "order")) {
                    if (Tokens.containsAny(tokens, "individual", "personal")) {
                        c.mustCheckoutProcess().selectIndividualOrder();
                        break;
                    }
                    if (Tokens.containsAny(tokens, "company")) {
                        c.mustCheckoutProcess().selectCompanyOrder();
                        break;
                    }
                }
                if (Tokens.containsAny(tokens, "option")) {
                    Tokens.removeLeading(tokens, "option");
                    String option;
                    if (tokens.size() > 1) {
                        option = tokens.get(0) + " " + tokens.get(1);
                    } else {
                        option = tokens.get(0);
                    }
                    c.mustCheckoutProcess().selectOrderOption(option);
                    break;
                }
                if (tokens.contains("delivery")) {
                    String type = null;
                    Object option = null;
                    int consignment = 0;
                    while (!tokens.isEmpty()) {
                        leading = Tokens.removeLeading(tokens, "delivery", "type", "option", "mode",
                                "time", "slot", "service",
                                "on", "for", "first", "1st", "second", "2nd", "third", "3rd", "fourth", "4th",
                                "consignment", "line", "and");
                        if (Tokens.contains(leading, "type")) {
                            type = "type";
                            option = tokens.remove(0);
                            continue;
                        } else if (Tokens.containsAny(leading, "option", "mode")) {
                            type = "option";
                            option = tokens.remove(0);
                            continue;
                        } else if (Tokens.contains(leading, "slot")) {
                            type = "slot";
                            option = Integer.parseInt(tokens.remove(0));
                            continue;
                        } else if (Tokens.contains(leading, "service")) {
                            type = "service";
                            option = tokens.remove(0);
                            continue;
                        } else if (Tokens.containsAny(leading, "first", "1st")) {
                            consignment = 1;
                        } else if (Tokens.containsAny(leading, "second", "2nd")) {
                            consignment = 2;
                        } else if (Tokens.containsAny(leading, "third", "3rd")) {
                            consignment = 3;
                        } else if (Tokens.containsAny(leading, "fourth", "4th")) {
                            consignment = 4;
                        } else if (type != null && option != null && !tokens.isEmpty()) {
                            option = tokens.remove(0);
                            continue;
                        }
                        if (type != null && option != null) {
                            c.mustCheckoutProcess().selectDelivery(type, consignment > 0 ? consignment : 1, option);
                            option = null;
                            consignment = 0;
                        }
                    }
                    if (type != null && option != null) {
                        c.mustCheckoutProcess().selectDelivery(type, consignment > 0 ? consignment : 1, option);
                    }
                }
                break;
            }

            case "click": {
                if (c.pfProcess != null) {
                    if (Tokens.containsAll(tokens, "buy", "now")) {
                        PF.clickBuyNow(c);
                        WebUI.closeAllPopup(c);
                    }
                    if (Tokens.containsAll(tokens, "add", "to", "cart")) {
                        PF.clickAddToCart(c);
                        WebUI.closeAllPopup(c);
                    }
                }
                if (tokens.get(0).equalsIgnoreCase("edit")) {
                    tokens.remove(0);
                    TokenSingleMatch match = Tokens.getFormName(tokens);
                    switch (match.value) {
                        case "customer info":
                            Checkout.clickEditCustomerInfo();
                            break;
                        case "customer address":
                            Checkout.clickEditCustomerAddress();
                            break;
                        case "delivery":
                            Checkout.clickEditDeliveryInfo();
                            break;
                    }
                }
                Tokens.removeLeading(tokens, "from");
                leading = Tokens.removeLeading(tokens, "cart");
                if (Tokens.containsAll(tokens, "continue", "shopping")) {
                    Tokens.removeLeading(tokens, "continue", "shopping");
                    Cart.clickContinueShopping(c);
                }
                if (Tokens.containsAll(tokens, "sign", "in")) {
                    Tokens.removeLeading(tokens, "sign", "in");
                    Cart.clickSignInFromCart(c);
                }
                break;
            }

            case "process": {
                leading = Tokens.removeLeading(tokens, "until");
                if (Tokens.containsAny(leading, "until")) {
                    boolean seenOnly = !Tokens.removeLeading(tokens, "seen", "meet").isEmpty();
                    TokenSingleMatch match = Tokens.getFormName(tokens);
                    switch (match.value) {
                        case "customer info":
                            Checkout.waitForNavigateTo();
                            if (seenOnly) {
                                c.mustCheckoutProcess().untilSeen("app-customer-info-v2");
                            } else {
                                c.mustCheckoutProcess().untilForm("app-customer-info-v2");
                            }
                            break;
                        case "customer address":
                            Checkout.waitForNavigateTo();
                            if (seenOnly) {
                                c.mustCheckoutProcess().untilSeen("app-customer-address-v2");
                            } else {
                                c.mustCheckoutProcess().untilForm("app-customer-address-v2");
                            }
                            break;
                        case "billing address":
                            Checkout.waitForNavigateTo();
                            if (seenOnly) {
                                c.mustCheckoutProcess().untilSeen("app-billing-address-v2");
                            } else {
                                c.mustCheckoutProcess().untilForm("app-billing-address-v2");
                            }
                            break;
                        case "delivery":
                            c.mustCheckoutProcess().untilSeen("app-checkout-step-delivery");
                    }
                }
                break;
            }

            case "continue": {
                leading = Tokens.removeLeading(tokens, "to", "cart", "checkout", "payment", "page",
                        "as", "guest", "register", "user");
                if (leading.contains("cart")) {
                    if (Util.isPDPage()) {
                        PD.continueToCart();
                    } else {
                        BC.continueToCart();
                    }
                }
                if (leading.contains("checkout")) {
                    if (Tokens.containsAny(leading, "register", "user")) {
                        Cart.ssoCheckout(c);
                    } else if (Tokens.containsAny(leading, "guest")) {
                        if (c.ssoSignedIn) {
                            throw new Exception("Unable to checkout as guest, please logout first");
                        }
                        Cart.guestCheckout(c);
                    } else {
                        Cart.guestCheckout(c);
                    }
                }
                if (leading.contains("payment")) {
                    Checkout.waitForNavigateTo();
                    c.mustCheckoutProcess().untilPayment();
                    Checkout.process(c);
                }
                break;
            }

            case "pay", "payment": {
                Tokens.removeLeading(tokens, "with", "the");
                if (!WebUI.getUrl().contains("CHECKOUT_STEP_PAYMENT")) {
                    Checkout.waitForNavigateTo();
                    c.mustCheckoutProcess().untilPayment();
                    Checkout.process(c);
                }
                c.paymentProcess = new PaymentProcess();
                c.paymentProcess.methodName = String.join(" ", tokens);
                Payment.expandPaymentMethod(c.paymentProcess.methodName);
                Payment.process(c);
                String poNumber = Order.getPONumber();
                Post p = new Post(c.post.getChannelId(), "PO Number: " + poNumber);
                p.setRootId(c.post.getId());
                c.client.createPost(p);
                break;
            }

            case "get": {
                if (Tokens.containsAll(tokens, "po", "number")) {
                    String poNumber = Order.getPONumber();
                    Post p = new Post(c.post.getChannelId(), "PO Number: " + poNumber);
                    p.setRootId(c.post.getId());
                    c.client.createPost(p);
                }
                break;
            }

            case "capture": {
                if (c.checkoutProcess != null) {
                    Checkout.process(c);
                }
                Post p = new Post(c.post.getChannelId(), req);
                p.setRootId(c.post.getId());
                Util.captureImageAndCreatePost(c, p);
                break;
            }

            case "verify": {
                if (c.checkoutProcess != null) {
                    Checkout.process(c);
                }
                String message = "V" + req.trim().substring(1);
                Post p = new Post(c.post.getChannelId(), message);
                p.setRootId(c.post.getId());
                p.setType("custom_ai_verify");
                p.getProps().setActivateAI(true);
                p.getProps().setCurrentUrl(WebUI.getUrl());
                p.getProps().setTestData(c.testData);
                String path = Util.captureToVerify(c);
                String fileId = c.client.uploadFile(p.getChannelId(), path);
                p.setFileIds(List.of(fileId));
                c.client.createPost(p);
                break;
            }

            case "close": {
                if (tokens.get(0).equalsIgnoreCase("browser")) {
                    WebUI.closeBrower(c);
                    c.reset();
                }
                break;
            }

            case "check": {
                leading = Tokens.removeLeading(tokens, "save");
                TokenSingleMatch match = Tokens.getFormName(tokens);
                switch (match.value) {
                    case "customer address":
                        c.mustCheckoutProcess().checkSaveCustomerAddress();
                        break;
                    case "billing address":
                        c.mustCheckoutProcess().checkSaveBillingAddress();
                        break;
                }
                break;
            }
        }
    }
}
