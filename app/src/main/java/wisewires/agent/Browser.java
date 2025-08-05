package wisewires.agent;

import java.util.ArrayList;
import java.util.List;
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
                } else if (tokens.contains("env")) {
                    c.env = tokens.remove(0).toLowerCase();
                    logger.info("Change current env to %s".formatted(c.env));
                }
                break;
            }

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
                if (tokens.contains("cart")) {
                    boolean mustReload = false;
                    class Item {
                        String urlOrSKU;
                        List<String> addedServices = new ArrayList<>();

                        @Override
                        public String toString() {
                            return urlOrSKU + " with " + addedServices;
                        }
                    }
                    List<Item> items = new ArrayList<>();
                    while (!tokens.isEmpty()) {
                        Tokens.removeLeading(tokens, "product", "and", "+", "from", "bc", "pd", "page", "to", "cart");
                        if (!tokens.isEmpty()) {
                            Item item = new Item();
                            item.urlOrSKU = tokens.remove(0);
                            leading = Tokens.removeLeading(tokens, "with", "trade-in", "trade-up", "sc+", "and", "+");
                            if (Tokens.containsAny(leading, "trade-in")) {
                                item.addedServices.add("TradeIn");
                            }
                            if (Tokens.containsAny(leading, "trade-up")) {
                                item.addedServices.add("TradeUp");
                            }
                            if (Tokens.containsAny(leading, "sc+")) {
                                item.addedServices.add("SC+");
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
                            API.addToCart(c.getAPIEndpoint(), item.urlOrSKU);
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
                Profile profile = c.getProfile();
                TokenSingleMatch match = Tokens.getFormName(tokens);
                boolean all = Tokens.containsAny(match.leading, "all");
                if (c.checkoutProcess != null) {
                    Checkout.process(c);
                }
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
                    c.mustCheckoutProcess().selectDifferentBillingAddress();
                    break;
                }
                leading = Tokens.removeLeading(tokens, "new");
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

            case "verify": {
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
        }
    }
}
