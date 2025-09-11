package wisewires.agent;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class SIMPopup {
    static Logger logger = LoggerFactory.getLogger(SIMPopup.class);

    static String MODAL_LOCATOR = """
            .tariff-popup__contents""";

    static void acceptTermAndConditions() throws Exception {
        try {
            String to = """
                    .tariff-popup__checkbox-label""";

            List<WebElement> elms = WebUI.waitElements(to, 5);
            for (WebElement elm : elms) {
                WebUI.scrollToCenter(elm);
                Thread.sleep(200);
                WebUI.click(elm, 10, 10);
                logger.info("E-Warranty terms and conditions accepted");
            }
        } catch (Exception e) {
            throw new Exception("Unable to check Term and Conditions", e);
        }
    }

    static void selectProvider(WebElementSelector selector) throws Exception {
        try {
            String to = """
                    .tariff-popup__tab-item""";
            List<WebElement> elms = WebUI.waitElements(to, 5);
            selector.apply(elms);
        } catch (Exception e) {
            throw new Exception("Unable to select SIM provider", e);
        }
    }

    static void selectOption(WebElementSelector selector) throws Exception {
        try {
            String to = """
                    .tariff-popup__radio""";
            List<WebElement> elms = WebUI.waitElements(to, 5);
            selector.apply(elms);
        } catch (Exception e) {
            throw new Exception("Unable to select E-Warranty option", e);
        }
    }

    static void clickContinue() throws Exception {
        try {
            String to = """
                    .tariff-popup__btn-next""";
            WebElement elm = WebUI.findElement(to);
            elm.click();
            logger.info("Continue button clicked");
        } catch (Exception e) {
            throw new Exception("Unable to click Continue button", e);
        }
    }

    static void clickConfirm() throws Exception {
        try {
            String to = """
                    .tariff-popup__btn-submit""";
            WebElement elm = WebUI.findElement(to);
            elm.click();
            logger.info("Confirm button clicked");
        } catch (Exception e) {
            throw new Exception("Unable to click Confirm button", e);
        }
    }

    static void waitForOpen(int timeOut) {
        WebUI.wait(timeOut, 1).withMessage("Waiting for SIM popup to open").until(driver -> {
            return WebUI.findElement(MODAL_LOCATOR) != null;
        });
    }

    static void waitForClose(int timeOut) {
        WebUI.wait(timeOut, 1).withMessage("Waiting for SIM popup to close").until(driver -> {
            return WebUI.findElement(MODAL_LOCATOR) == null;
        });
    }
}
