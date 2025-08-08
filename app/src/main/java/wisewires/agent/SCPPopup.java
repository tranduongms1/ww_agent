package wisewires.agent;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SCPPopup {
    static Logger logger = LoggerFactory.getLogger(SCPPopup.class);

    static void checkTermAndCondition() throws Exception {
        try {
            WebUI.wait(5, 1).withMessage("Check SC+ terms and conditions").until(driver -> {
                List<WebElement> elms;
                Actions action = new Actions(driver);

                String altBox = """
                        .tandc-item__img,
                        .checkbox-radio__label""";
                String defBox = """
                        .hubble-care-popup-new__check-list .checkbox-radio input,
                        .smc-modal .tandc__item,
                        .js-added-services-container .added-services-terms .checkbox-square,
                        app-samsung-care-v2 mat-checkbox div[class="mdc-checkbox"]""";

                if (WebUI.findElement(altBox) != null) {
                    elms = WebUI.findElements(altBox);
                } else if (WebUI.findElement(defBox) != null) {
                    elms = WebUI.findElements(defBox);
                } else {
                    elms = null;
                    return true;
                }
                for (WebElement elm : elms) {
                    WebUI.scrollToCenter(elm);
                    WebUI.delay(1);
                    action.moveToElement(elm).click().perform();
                    logger.info("Checked SC+ terms and conditions");
                }
                return true;
            });
        } catch (Exception e) {
            throw new Exception("Unable to select Term and Condition", e);
        }
    }

    static void selectFirstType() throws Exception {
        try {
            String to = ".smc-modal :has(> [name='smc-types'])";
            WebElement elm = WebUI.findElement(to);
            if (elm != null) {
                elm.click();
                logger.info("Selected first SC+ type");
            }
        } catch (Exception e) {
            throw new Exception("Unable to select first SC+ type", e);
        }

    }

    static void selectFirstDuration() throws Exception {
        try {
            String to = ".smc-modal :has(> [name='smc-durations'])";
            WebElement elm = WebUI.findElement(to);
            if (elm != null) {
                elm.click();
                logger.info("Selected first SC+ duration");
            }
        } catch (Exception e) {
            throw new Exception("Unable to select first SC+ duration", e);
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
            if (elm != null) {
                WebUI.scrollToCenter(elm);
                WebUI.delay(1);
                elm.click();
                logger.info("Clicked Continue in SC+ popup");
            }
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
            if (elm != null) {
                WebUI.scrollToCenter(elm);
                WebUI.delay(1);
                elm.click();
                logger.info("Clicked Confirm in SC+ popup");
            }
        } catch (Exception e) {
            throw new Exception("Unable to click Confirm", e);
        }
    }

    static void waitForClose(int timeOut) {
        WebUI.wait(timeOut, 1).withMessage("Waiting for SC+ popup to close").until(driver -> {
            String to = """
                    .hubble-care-popup-new,
                    .smc-modal,
                    app-samsung-care-v2.modal,
                    .js-added-service-modal-SMC,
                    .added-service-modal.show,
                    app-samsung-care""";
            List<WebElement> elms = WebUI.findElements(to);
            if (elms.isEmpty()) {
                return true;
            }
            return false;
        });
    }

    static void waitForOpen(int timeOut) {
        WebUI.wait(timeOut, 1).withMessage("Waiting for SC+ popup to open").until(driver -> {
            String to = """
                    .hubble-care-popup-new,
                    .smc-modal,
                    app-samsung-care-v2.modal,
                    .js-added-service-modal-SMC,
                    .added-service-modal.show,
                    app-samsung-care""";
            List<WebElement> elms = WebUI.findElements(to);
            return !elms.isEmpty();
        });
    }
}
