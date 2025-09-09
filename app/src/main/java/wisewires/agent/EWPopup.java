package wisewires.agent;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

public abstract class EWPopup {
    static Logger logger = LoggerFactory.getLogger(EWPopup.class);

    static String MODAL_LOCATOR = """
            .extended-warranty-popup-vd,
            .modal__container""";

    static void acceptTermAndConditions() throws Exception {
        try {
            String to = """
                    .tandc-item__img,
                    input[name='vd-checkbox'],
                    .mdc-checkbox:has([type='checkbox'])""";

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

    static private void selectOption(Predicate<WebElement> predicate) throws Exception {
        WebElement modal = WebUI.findElement(MODAL_LOCATOR);
        String to = """
                .pd-select-option__item""";
        WebElement elm = modal.findElements(By.cssSelector(to))
                .stream()
                .filter(predicate)
                .findFirst()
                .get();
        WebUI.scrollToCenter(elm);
        Thread.sleep(500);
        elm.click();
    }

    static void selectOptionByTitle(String title) throws Exception {
        try {
            selectOption(opt -> {
                String to = """
                        .pd-option-selector__main-text""";
                return opt.findElement(By.cssSelector(to)).getText().equalsIgnoreCase(title);
            });
        } catch (Exception e) {
            throw new Exception("Unable to select option with title '%s'".formatted(title), e);
        }
    }

    static void selectFirstOption(WebElementSelector selector) throws Exception {
        try {
            String to = """
                    .extended-warranty-popup-vd .pd-select-option__item,
                    .modal__container .mat-mdc-radio-button""";
            List<WebElement> elms = WebUI.waitElements(to, 5);
            selector.apply(elms);
        } catch (Exception e) {
            throw new Exception("Unable to select E-Warranty option", e);
        }
    }

    static void clickConfirm() throws Exception {
        try {
            String to = """
                    a[an-la='extended warranty:confirm'],
                    button[data-an-la='extended warranty:add to cart']""";
            WebElement elm = WebUI.findElement(to);
            elm.click();
            logger.info("Confirm button clicked");
        } catch (Exception e) {
            throw new Exception("Unable to click Confirm button", e);
        }
    }

    static void waitForOpen(int timeOut) {
        WebUI.wait(timeOut, 1).withMessage("Waiting for E-Warranty popup to open").until(driver -> {
            return WebUI.findElement(MODAL_LOCATOR) != null;
        });
    }

    static void waitForClose(int timeOut) {
        WebUI.wait(timeOut, 1).withMessage("Waiting for E-Warranty popup to close").until(driver -> {
            return WebUI.findElement(MODAL_LOCATOR) == null;
        });
    }
}
