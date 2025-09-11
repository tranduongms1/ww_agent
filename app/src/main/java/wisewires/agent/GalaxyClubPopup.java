package wisewires.agent;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GalaxyClubPopup {
    static Logger logger = LoggerFactory.getLogger(GalaxyClubPopup.class);

    static String MODAL_LOCATOR = """
            app-samsung-galaxy-club.modal,
            .bc-galaxy-club-popup__step--show""";

    static void selectOption(WebElementSelector selector) throws Exception {
        try {
            String to = """
                    .smc-option,
                    .bc-galaxy-club-popup__option-radio-form""";
            List<WebElement> elms = WebUI.waitElements(to, 5);
            selector.apply(elms);
        } catch (Exception e) {
            throw new Exception("Unable to select Galaxy Club option", e);
        }
    }

    static void clickContinue() throws Exception {
        try {
            String to = """
                    app-samsung-galaxy-club .modal__footer button[type='submit'],
                    .bc-galaxy-club-popup__btn-wrap [an-la='samsung galaxy club:continue']""";
            WebElement elm = WebUI.waitElement(to, 5);
            WebUI.scrollToCenter(elm);
            WebUI.delay(1);
            elm.click();
            logger.info("Clicked Continue in Galaxy Club popup");
        } catch (Exception e) {
            throw new Exception("Unable to click Continue", e);
        }
    }

    static void clickConfirm() throws Exception {
        try {
            String to = """
                    button[data-an-la="samsung galaxy club:add to cart"],
                    button[an-la="samsung galaxy club:confirm"]""";
            WebElement elm = WebUI.findElement(to);
            WebUI.scrollToCenter(elm);
            WebUI.delay(1);
            elm.click();
            logger.info("Clicked Confirm in Galaxy Club popup");
        } catch (Exception e) {
            throw new Exception("Unable to click Confirm", e);
        }
    }

    static void waitForOpen(int timeOut) {
        WebUI.wait(timeOut, 1).withMessage("SC+ popup to open").until(driver -> {
            return WebUI.findElement(MODAL_LOCATOR) != null;
        });
    }

    static void waitForClose(int timeOut) {
        WebUI.wait(timeOut, 1).withMessage("SC+ popup to close").until(driver -> {
            return WebUI.findElement(MODAL_LOCATOR) == null;
        });
    }

}
