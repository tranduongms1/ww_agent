package wisewires.agent;

public abstract class Login {
    static void fromAEM(Context c) throws Exception {
        fromAEMGNB(c);
    }

    static void fromAEMGNB(Context c) throws Exception {
        WebUI.openBrowser(c, c.getHomeUrl());
        WebUI.mustCloseAllPopup(c);
        GNB.navigateToSignIn();
        SSO.signInByEmail(c);
    }

    static void fromShop(Context c) throws Exception {
        fromShopGNB(c);
    }

    static void fromShopGNB(Context c) throws Exception {
        if (WebUI.driver == null || !WebUI.getUrl().contains("shop.samsung.com")) {
            WebUI.openBrowser(c, c.getShopUrl());
        }
        WebUI.mustCloseAllPopup(c);
        GNB.navigateToSignIn();
        SSO.signInByEmail(c);
    }

    static void fromCart(Context c) throws Exception {
        fromCartGNB(c);
    }

    static void fromCartGNB(Context c) throws Exception {
        if (!WebUI.isOnSiteCart(c)) {
            Cart.navigateTo(c, false);
        }
        fromShopGNB(c);
    }

    static void fromEmptyCart(Context c) throws Exception {
        boolean readyEmpty = Cart.mustEmpty(c);
        Cart.navigateTo(c, !readyEmpty);
        WebUI.mustCloseAllPopup(c);
        WebUI.click("[data-an-la='empty cart:sign in']");
        SSO.signInByEmail(c);
    }

    static void fromSplash(Context c) throws Exception {
        boolean readyNotEmpty = Cart.mustNotEmpty(c);
        Cart.navigateTo(c, !readyNotEmpty);
        WebUI.mustCloseAllPopup(c);
        Cart.clickCheckoutButton();
        WebUI.wait(30).until(driver -> {
            String url = driver.getCurrentUrl();
            if (url.contains("/guestlogin/")) {
                WebUI.click(".button[data-an-la='samsung account']");
                WebUI.delay(2);
            }
            return url.contains("account.samsung.com");
        });
        SSO.signInByEmail(c);
    }
}
