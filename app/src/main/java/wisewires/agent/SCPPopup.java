package wisewires.agent;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SCPPopup {
    static Logger logger = LoggerFactory.getLogger(SCPPopup.class);

    static String MODAL_LOCATOR = """
            .hubble-care-popup,
            .hubble-care-popup-new,
            .smc-modal,
            app-samsung-care-v2.modal,
            .js-added-service-modal-SMC,
            .added-service-modal.show,
            app-samsung-care""";

    static void acceptTermAndConditions() throws Exception {
        try {
            String to = """
                    .hubble-care-popup__check-list .checkbox-radio,
                    .hubble-care-popup-new__check-list .checkbox-radio,
                    .smc-modal .tandc__item,
                    .js-added-services-container .added-services-terms .checkbox-square,
                    app-samsung-care-v2 mat-checkbox div[class="mdc-checkbox"],
                    .modal__checkbox-wrapper .mdc-checkbox""";
            List<WebElement> elms = WebUI.waitElements(to, 2);
            for (WebElement elm : elms) {
                WebUI.scrollIntoView(elm);
                WebUI.delay(1);
                WebUI.click(elm, 12, 12);
            }
            logger.info("Samsung Care+ terms and conditions checked");
        } catch (Exception e) {
            throw new Exception("Unable to select Term and Conditions", e);
        }
    }

    static void selectType(WebElementSelector selector) throws Exception {
        try {
            String to = ":has(> [name='smc-types'])";
            List<WebElement> elms = WebUI.waitElements(to, 5);
            selector.apply(elms);
        } catch (Exception e) {
            throw new Exception("Unable to select SC+ type", e);
        }
    }

    static void selectDuration(WebElementSelector selector) throws Exception {
        try {
            String to = ":has(> [name='smc-durations'])";
            List<WebElement> elms = WebUI.waitElements(to, 5);
            selector.apply(elms);
        } catch (Exception e) {
            throw new Exception("Unable to select SC+ duration", e);
        }
    }

    static void selectOption(WebElementSelector selector) throws Exception {
        try {
            String to = ".smc-option";
            List<WebElement> elms = WebUI.waitElements(to, 5);
            selector.apply(elms);
        } catch (Exception e) {
            throw new Exception("Unable to select SC+ option", e);
        }
    }

    static void selectItemByText(String text) throws Exception {
        try {
            WebUI.wait(5, 1).withMessage("Select SC+ item by text: " + text).until(driver -> {
                String to;
                if (WebUI.isOneOfSites("FR", "TH")) {
                    to = "app-samsung-care-v2 mat-radio-group mat-radio-button span";
                } else {
                    to = """
                            app-samsung-care-v2 mat-radio-group mat-radio-button div.option-box__price,
                            .service-list-selector[ng-if*='displaySmcProduct'],
                            app-samsung-care mat-radio-group mat-radio-button""";
                }

                List<WebElement> elms;
                if (text.equals("standard")) {
                    elms = WebUI.findElements(to)
                            .stream()
                            .filter(WebElement::isDisplayed)
                            .filter(e -> !e.getText().toLowerCase().contains("/"))
                            .toList();
                } else {
                    elms = WebUI.findElements(to)
                            .stream()
                            .filter(WebElement::isDisplayed)
                            .filter(e -> e.getText().toLowerCase().contains(text.toLowerCase()))
                            .toList();
                }

                if (!elms.isEmpty()) {
                    WebElement elm = elms.get(0);
                    WebUI.scrollToCenter(elm);
                    WebUI.delay(1);
                    elm.click();
                    logger.info("Selected SC+ item: " + text);
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            throw new Exception("Unable to select SC+ Item", e);
        }
    }

    static void clickContinue() throws Exception {
        try {
            String to = ".smc-modal [an-la='samsung care:continue']";
            WebElement elm = WebUI.waitElement(to, 5);
            WebUI.scrollToCenter(elm);
            WebUI.delay(1);
            elm.click();
            logger.info("Clicked Continue in SC+ popup");
        } catch (Exception e) {
            throw new Exception("Unable to click Continue", e);
        }
    }

    static void clickConfirm() throws Exception {
        try {
            String to = """
                    button[an-la="samsung care:confirm"],
                    button[data-an-la="samsung care:add to cart"],
                    app-samsung-care-v2 .modal__footer button[type="submit"],
                    button[data-an-la="samsung care:confirm"],
                    a[an-la="samsung care:confirm"]""";
            WebElement elm = WebUI.findElement(to);
            WebUI.scrollToCenter(elm);
            WebUI.delay(1);
            elm.click();
            logger.info("Clicked Confirm in SC+ popup");
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
