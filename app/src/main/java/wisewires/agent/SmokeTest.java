package wisewires.agent;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SmokeTest {
    private static Logger logger = LoggerFactory.getLogger(SmokeTest.class);

    static void run(Context c, String req) throws Exception {
        List<String> tokens = Tokens.tokenize(req);
        String arg = tokens.remove(0);
        List<String> leading;
        switch (arg.toLowerCase()) {
            case "on": {
                if (tokens.contains("site")) {
                    c.site = tokens.remove(0).toUpperCase();
                    logger.info("Change current site to %s".formatted(c.site));
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
                if (tokens.contains("cart")) {
                    boolean mustReload = false;
                    List<String> items = new ArrayList<>();
                    while (!tokens.isEmpty()) {
                        Tokens.removeLeading(tokens, "product", "and", "+", "from", "bc", "pd", "page", "to", "cart");
                        if (!tokens.isEmpty()) {
                            items.add(tokens.remove(0));
                        }
                    }
                    for (String item : items) {
                        logger.info("Add to cart: " + item);
                        if (item.startsWith("https://")) {
                            Cart.addProduct(c, item);
                            mustReload = false;
                        } else {
                            Cart.mustCartId(c);
                            API.addToCart(c.getAPIEndpoint(), item);
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
                    boolean mustReload = true;
                    if (leading.contains("not")) {
                        mustReload = Cart.mustNotEmpty(c);
                    } else {
                        mustReload = Cart.mustEmpty(c);
                    }
                    Cart.navigateTo(c, mustReload);
                    WebUI.mustCloseAllPopup(c);
                }
            }

            case "fill": {
                Profile profile = c.getProfile();
                TokenSingleMatch match = Tokens.getFormName(tokens);
                boolean all = Tokens.containsAny(match.leading, "all");
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

            case "process": {
                leading = Tokens.removeLeading(tokens, "until");
                if (Tokens.containsAny(leading, "until")) {
                    Tokens.removeLeading(tokens, "seen", "meet");
                    TokenSingleMatch match = Tokens.getFormName(tokens);
                    switch (match.value) {
                        case "customer info":
                            Checkout.waitForNavigateTo();
                            c.mustCheckoutProcess().untilSeenForm("app-customer-info-v2");
                            Checkout.process(c);
                            break;
                        case "customer address":
                            Checkout.waitForNavigateTo();
                            c.mustCheckoutProcess().untilSeenForm("app-customer-address-v2");
                            Checkout.process(c);
                            break;
                        case "billing address":
                            Checkout.waitForNavigateTo();
                            c.mustCheckoutProcess().untilSeenForm("app-billing-address-v2");
                            Checkout.process(c);
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
                    } else {
                        Cart.guestCheckout(c);
                    }
                }
                if (leading.contains("payment")) {
                    Checkout.waitForNavigateTo();
                    c.mustCheckoutProcess();
                    Checkout.process(c);
                }
                break;
            }
        }
    }
}
