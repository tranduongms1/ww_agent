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
                    c.siteUid = tokens.remove(0).toLowerCase();
                    logger.info("Change current store to %s".formatted(c.siteUid));
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
                leading = Tokens.removeLeading(tokens, "to", "home", "shop", "cart", "page");
                if (leading.containsAll(List.of("shop", "home"))) {
                    WebUI.openBrowser(c, c.getShopUrl());
                } else if (leading.contains("home")) {
                    WebUI.openBrowser(c, c.getHomeUrl());
                } else if (leading.contains("cart")) {
                    WebUI.openBrowser(c, c.getCartUrl());
                } else {
                    WebUI.openBrowser(c, tokens.get(0));
                }
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
                    GNB.hoverHumanIcon();
                }
                logger.info("[PASS] %s".formatted(req));
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

            case "add": {
                if (tokens.get(0).equalsIgnoreCase("trade-in")) {
                    if (WebUI.getUrl().contains("/cart")) {
                        Cart.addTradeIn(c);
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
                                    "e-warranty", "ewarranty", "warranty",
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
                            if (Tokens.containsAny(leading, "e-warranty", "ewarranty", "warranty")) {
                                item.addedServices.add("EWarranty");
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
                            }
                            mustReload = true;
                        }
                    }
                    Cart.navigateTo(c, mustReload);
                    WebUI.mustCloseAllPopup(c);
                }
                break;
            }

            case "ensure": {
                leading = Tokens.removeLeading(tokens, "cart", "page", "not", "empty");
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
                if (tokens.contains("delivery")) {
                    String type = null;
                    Object option = null;
                    int consignment = 0;
                    while (!tokens.isEmpty()) {
                        leading = Tokens.removeLeading(tokens, "delivery", "option", "mode", "time", "slot", "service",
                                "on", "for", "first", "1st", "second", "2nd", "third", "3rd", "fourth", "4th",
                                "consignment", "line", "and");
                        if (Tokens.containsAny(leading, "option", "mode")) {
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
                leading = Tokens.removeLeading(tokens, "to", "checkout", "payment", "page",
                        "as", "guest", "register", "user");
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

            case "pay", "payment":
                Tokens.removeLeading(tokens, "with", "the");
                String methodName = String.join(" ", tokens);
                if (!WebUI.getUrl().contains("CHECKOUT_STEP_PAYMENT")) {
                    Checkout.waitForNavigateTo();
                    c.mustCheckoutProcess().untilPayment();
                    Checkout.process(c);
                }
                Payment.expandPaymentMethod(methodName);
                Payment.process(c);
                break;

            case "capture": {
                if (c.checkoutProcess != null) {
                    Checkout.process(c);
                }
                String url = Util.captureFullPage();
                String fileId = c.client.uploadFile(c.post.getChannelId(), url);
                Post post = new Post();
                post.setChannelId(c.post.getChannelId());
                post.setMessage(req);
                post.setFileIds(List.of(fileId));
                post.setRootId(c.post.getId());
                c.client.createPost(post);
                break;
            }

            case "verify": {
                if (c.checkoutProcess != null) {
                    Checkout.process(c);
                }
                String url = Util.captureFullPage();
                String fileId = c.client.uploadFile(c.post.getChannelId(), url);
                Post post = new Post();
                post.setChannelId(c.post.getChannelId());
                post.setMessage("✅ " + req);
                post.setFileIds(List.of(fileId));
                post.setRootId(c.post.getId());
                post.setType("custom_ai_verify");
                post.getProps().put("currentUrl", WebUI.getUrl());
                post.getProps().put("logMessages", c.getAllLogs());
                c.client.createPost(post);
                break;
            }

            case "close": {
                if (tokens.get(0).equalsIgnoreCase("browser")) {
                    WebUI.closeBrower(c);
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
            }
        }
    }
}
