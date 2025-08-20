package wisewires.agent;

public abstract class Logout {
    static void fromAEM(Context c) throws Exception {
        fromAEMGNB(c);
    }

    static void fromAEMGNB(Context c) throws Exception {
    }

    static void fromShop(Context c) throws Exception {
        fromShopGNB(c);
    }

    static void fromShopGNB(Context c) throws Exception {
        if (!WebUI.getUrl().contains("shop.samsung.com")) {
            WebUI.driver.get(c.getShopUrl());
        }
        GNB.hoverHumanIcon();
        WebUI.click("a[data-an-la='logout']");
        c.ssoSignedIn = false;
        WebUI.delay(2);
        GNB.hoverHumanIcon();
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
}
