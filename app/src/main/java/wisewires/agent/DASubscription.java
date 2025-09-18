package wisewires.agent;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DASubscription {
    static Logger logger = LoggerFactory.getLogger(DASubscription.class);

    static String MODAL_LOCATOR = "app-subscription-modal.modal";

    static void selectOption(WebElementSelector selector) throws Exception {
        try {
            String to = "mat-select";
            List<WebElement> elms = WebUI.waitElements(to, 5);
            WebUI.click(to);
            selector.apply(elms);
        } catch (Exception e) {
            throw new Exception("Unable to select DA Subscription option", e);
        }
    }

    static void acceptTermAndConditions() throws Exception {
        try {
            String to = "app-subscription-modal div[class='mdc-checkbox']";
            List<WebElement> elms = WebUI.waitElements(to, 2);
            for (WebElement elm : elms) {
                WebUI.scrollIntoView(elm);
                WebUI.delay(1);
                WebUI.click(elm);
            }
            logger.info("DA Subscription terms and conditions checked");
        } catch (Exception e) {
            throw new Exception("Unable to check Term and Conditions", e);
        }
    }

    static void clickContinue() throws Exception {
        try {
            String to = "app-subscription-modal .modal__footer button[type='submit']";
            WebElement elm = WebUI.waitElement(to, 5);
            WebUI.scrollToCenter(elm);
            WebUI.delay(1);
            elm.click();
            logger.info("Clicked Continue in DA Subscription popup");
        } catch (Exception e) {
            throw new Exception("Unable to click Continue", e);
        }
    }

    static void waitForOpen(int timeOut) {
        WebUI.wait(timeOut, 1).withMessage("DA Subscription popup to open").until(driver -> {
            return WebUI.findElement(MODAL_LOCATOR) != null;
        });
    }

    static void waitForClose(int timeOut) {
        WebUI.wait(timeOut, 1).withMessage("DA Subscription popup to close").until(driver -> {
            return WebUI.findElement(MODAL_LOCATOR) == null;
        });
    }

}
