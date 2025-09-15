package wisewires.agent;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GNB {
    private static Logger logger = LoggerFactory.getLogger(GNB.class);

    static void hoverHumanIcon() throws Exception {
        try {
            String to = """
                    app-profile-header-menu .nav-action-button,
                    .after-login button[an-la="user name"],
                    .before-login [aria-label="Manage Account"]
                    """;
            WebElement elm = WebUI.waitElement(to, 10);
            Actions builder = new Actions(WebUI.driver);
            builder.moveToElement(elm).perform();
        } catch (Exception e) {
            throw new Exception("Unable to hover on GNB Human icon", e);
        }
    }

    static void navigateToSignIn() throws Exception {
        try {
            hoverHumanIcon();
            WebUI.delay(1);
            WebUI.click("a[an-la='sign in sign up'], :nth-child(1 of .sso-url.login-signup)");
            logger.info("Login link clicked");
            WebUI.wait(10).withMessage("navigated to SSO").until(d -> {
                String url = d.getCurrentUrl();
                return url.startsWith("https://account.samsung.com/") ||
                        url.startsWith("https://shop-support.samsung.com.cn/");
            });
            logger.info("Navigated to SSO login page");
        } catch (Exception e) {
            throw new Exception("Unable to navigate to SSO login from GNB", e);
        }
    }
}
