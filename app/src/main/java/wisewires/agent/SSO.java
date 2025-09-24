package wisewires.agent;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SSO {
    static Logger logger = LoggerFactory.getLogger(SSO.class);

    static void signInByEmail(Context c) throws Exception {
        signInByEmail(c.sso.get("email"), c.sso.get("mk"));
        c.ssoSignedIn = true;
    }

    static void signInByEmail(String email, String password) throws Exception {
        try {
            WebUI.waitElement("input#iptLgnPlnID, input#account", 10);
            WebUI.fill("input#iptLgnPlnID, input#account", email);
            WebUI.delay(3);

            WebUI.click("button#signInButton, button.MuiButton-containedPrimary");

            WebUI.waitElement("input#iptLgnPlnPD, input#password", 30);
            WebUI.fill("input#iptLgnPlnPD, input#password", password);

            WebUI.click("button#signInButton, button.MuiButton-containedPrimary");

            WebUI.wait(60, 2).until(driver -> {
                String url = driver.getCurrentUrl();
                if (url.contains("/stay-signed-in")) {
                    WebUI.click(".MuiButton-colorPrimary");
                    return false;
                }
                if (url.contains("/iam/sign-in/status/terms")) {
                    WebElement elm = WebUI.waitElement("span:has([id='all'])", 10);
                    WebUI.scrollToCenter(elm);
                    WebUI.delay(1);
                    elm.click();
                    WebUI.delay(1);
                    WebUI.click(".MuiButton-containedPrimary");
                }
                if (url.contains("/iam/sign-in/status/change-password")) {
                    WebElement elm = WebUI.findElement("button.MuiButton-containedSecondary");
                    if (elm != null) {
                        elm.click();
                    }
                }
                if (url.contains("/termsUpdate")) {
                    String to = """
                            input:not(:checked) + label[for='iptTncAll'],
                            input:not(:checked) + label[for='iptTncTC'],
                            input:not(:checked) + label[for='iptTncST'],
                            input:not(:checked) + label[for='iptTncOptAll']""";
                    ;
                    while (WebUI.waitElement(to, 1) != null) {
                        WebUI.click(to);
                    }
                    WebUI.click("button#terms");
                    WebUI.waitForNotPresent("button#terms", 30);
                }
                if (url.contains("/changePasswordCycle")) {
                    WebUI.click(".one-cancel.one-button");
                }
                return !driver.getCurrentUrl().contains("account.samsung.com");
            });
            logger.info("SSO sign-in success");
        } catch (Exception e) {
            throw new Exception("Error during SSO sign-in by email", e);
        }
    }

    static void mustSignedIn(Context c, String from) throws Exception {
        if (!c.ssoSignedIn) {
            switch (from) {
                case "aem":
                    Login.fromAEM(c);
                    break;
                case "shop":
                    Login.fromShop(c);
                    break;
                case "cart":
                    Login.fromCart(c);
                    break;
            }
        }
    }

    static void mustSignedOut(Context c) throws Exception {
        if (c.ssoSignedIn) {
            String lastUrl = WebUI.getUrl();
            String js = "return Boolean(window.samsung instanceof Object && window.samsung.account && window.samsung.account.signOut)";
            boolean ok = (boolean) WebUI.driver.executeScript(js);
            if (!ok) {
                WebUI.driver.get(c.getCartUrl());
                WebUI.wait(10).until(d -> WebUI.driver.executeScript(js));
            }
            WebUI.driver.executeAsyncScript("window.samsung.account.signOut().finally(arguments[0])");
            if (ok) {
                WebUI.driver.navigate().refresh();
            } else {
                WebUI.driver.get(lastUrl);
            }
            c.ssoSignedIn = false;
        }
    }
}
