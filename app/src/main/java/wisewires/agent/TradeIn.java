package wisewires.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TradeIn {
    static Logger logger = LoggerFactory.getLogger(TradeIn.class);

    static String MODAL_LOCATOR = """
            .trade-in-popup,
            .trade-in-popup-v3,
            [class^='trade-in-steps'],
            .trade-in-container,
            .modal-dialog.tradein-cx-flow""";

    static String NEXT_LOCATOR = """
            [an-la='trade-in:trade-in guide:next'],
            [data-an-la='trade-in:trade-in guide:next'],
            [data-an-la='trade-in:trade-in guide:start'],
            [an-la='trade-in:select device:next'],
            [data-an-la='trade-in:select device:next'],
            [an-la='trade-in:check device condition:next'],
            [data-an-la='trade-in:check device condition:next'],
            [an-la='trade-in:enter imei:next'],
            [data-an-la='trade-in:enter imei:next'],
            [an-la='trade-in:step2:next'],
            [an-la='trade-in:apply discount:apply trade in'],
            [data-an-la='trade-in:apply discount:add to cart'],
            [data-an-la='trade-in:select device:apply discount'],
            [data-an-la='trade-in:device1:apply discount:add to cart']""";

    static String getStepName(WebElement modal) throws Exception {
        WebElement closeBtn = modal.findElement(By.cssSelector("""
                .trade-in-popup__close,
                .trade-in-popup-v3__close,
                .modal__close"""));
        if (closeBtn != null) {
            return WebUI.getDomAttribute(closeBtn, "an-la", "data-an-la").split(":")[1];
        }
        return "unknow";
    }

    static boolean select(WebElement elm, String option) throws Exception {
        WebUI.scrollToCenter(elm);
        WebUI.delay(1);
        String className = elm.getAttribute("class");
        if (className.contains("trade-in-select")) {
            if (!className.contains("is-opened")) {
                String selected = elm.findElement(By.cssSelector(".select-txt")).getText().trim();
                if (selected.equalsIgnoreCase(option.trim())) {
                    return false;
                }
                elm.click();
                WebUI.delay(1);
            }
            String css = "[an-la='%s'i], [value='%s'i]".replaceAll("%s", option);
            WebElement opt = elm.findElement(By.cssSelector(css));
            WebUI.scrollToCenter(opt);
            opt.click();
        }
        return true;
    }

    static void selectCategory(WebElement elm, String category) throws Exception {
        try {
            String css = ":has(> [value*='%s'i])".replaceAll("%s", category);
            WebElement opt = elm.findElement(By.cssSelector(css));
            if (opt.findElements(By.cssSelector(":checked")).isEmpty()) {
                WebUI.scrollToCenter(opt);
                opt.click();
                logger.info("Device category '%s' selected".formatted(category));
            }
        } catch (Exception e) {
            throw new Exception("Unable to select device category '%s'".formatted(category));
        }
    }

    static void selectBrand(WebElement elm, String brand) throws Exception {
        try {
            if (select(elm, brand)) {
                logger.info("Device brand '%s' selected".formatted(brand));
            }
        } catch (Exception e) {
            throw new Exception("Unable to select device brand '%s'".formatted(brand));
        }
    }

    static void selectModel(WebElement elm, String model) throws Exception {
        try {
            if (select(elm, model)) {
                logger.info("Device model '%s' selected".formatted(model));
            }
        } catch (Exception e) {
            throw new Exception("Unable to select device model '%s'".formatted(model));
        }
    }

    static void selectStorage(WebElement elm, String storage) throws Exception {
        try {
            if (select(elm, storage)) {
                logger.info("Device storage '%s' selected".formatted(storage));
            }
        } catch (Exception e) {
            throw new Exception("Unable to select device storage '%s'".formatted(storage));
        }
    }

    static void selectColor(WebElement elm, String color) throws Exception {
        try {
            if (select(elm, color)) {
                logger.info("Device color '%s' selected".formatted(color));
            }
        } catch (Exception e) {
            throw new Exception("Unable to select device color '%s'".formatted(color));
        }
    }

    static void selectDevice(WebElement modal, Map<String, String> data) throws Exception {
        List<WebElement> elms = modal.findElements(By.cssSelector("""
                .trade-in-popup__category-device,
                .trade-in-popup-v3__tradeIn-category,
                .trade-in-select"""));
        for (WebElement elm : elms) {
            if (!elm.isDisplayed()) {
                continue;
            }
            String className = WebUI.getDomAttribute(elm, "class");
            if (className.contains("category")) {
                selectCategory(elm, data.get("category"));
            } else if (className.contains("trade-in-select")) {
                String id = elm.findElement(By.cssSelector("ul")).getAttribute("id");
                switch (id) {
                    case "brand", "brandName", "manufacturer":
                        selectBrand(elm, data.get("brand"));
                        break;

                    case "model", "modelName":
                        selectModel(elm, data.get("model"));
                        break;

                    case "storage", "capacity", "memory":
                        selectStorage(elm, data.get("storage"));
                        break;

                    case "color":
                        selectColor(elm, data.get("color"));
                        break;
                }
            }
        }
    }

    static void selectDeviceConditions(WebElement modal) throws Exception {
        try {
            List<List<WebElement>> questions = new ArrayList<>();
            List<WebElement> elms = WebUI.findElements(modal, By.cssSelector("""
                    .trade-in-popup__condition-list-item,
                    .trade-in-popup__summary-accept-list,
                    .trade-in-popup-v3__condition-list-item,
                    .trade-in-popup-v3__summary-accept-list"""));
            for (WebElement elm : elms) {
                if (elm.findElements(By.cssSelector("input:checked")).isEmpty()) {
                    By by = By.cssSelector(":has(> [type='radio'])");
                    questions.add(elm.findElements(by));
                }
            }
            TradeInProcess.selectBestDeviceConditions(questions);
        } catch (Exception e) {
            throw new Exception("Unable to select device conditions", e);
        }
    }

    static void acceptTermsAndConditions(WebElement modal) throws Exception {
        List<WebElement> elms = WebUI.findElements(modal, By.cssSelector("""
                .trade-in-popup__confirm-terms .checkbox-radio,
                .trade-in-popup-v3__terms .checkbox-v2"""));
        for (WebElement elm : elms) {
            if (elm.findElements(By.cssSelector("input:checked")).isEmpty()) {
                WebUI.scrollToCenter(elm);
                WebUI.delay(1);
                WebUI.click(elm, 10, 10);
                logger.info("Term and conditions accepted");
            }
        }
    }

    static void enterIMEI(WebElement modal, String imei) throws Exception {
        try {
            WebElement input = WebUI.findElement(modal, By.cssSelector("""
                    .trade-in-popup__imei-form input,
                    .trade-in-popup-v3__imei-form input"""));
            WebUI.scrollToCenter(input);
            input.clear();
            input.sendKeys(imei + Keys.ENTER);
            WebUI.delay(1);
            logger.info("Device IMEI '%s' entered".formatted(imei));
            Optional<WebElement> loading = modal.findElements(By.cssSelector(".circular-progress")).stream().findAny();
            if (loading.isPresent()) {
                WebUI.delay(2);
                WebUI.waitForDisappear(loading.get(), 10);
                logger.info("Device IMEI checking done");
            }
        } catch (Exception e) {
            throw new Exception("Unable to enter device IMEI '%s'".formatted(imei));
        }
    }

    static boolean nextStep() throws Exception {
        WebElement btn = WebUI.findElement(NEXT_LOCATOR);
        if (!btn.isEnabled()) {
            throw new Exception("TradeIn: Next button is not enabled");
        }
        btn.click();
        logger.info("Next button clicked");
        WebUI.waitForDisappear(btn, 10);
        if (WebUI.findElement(MODAL_LOCATOR) == null) {
            return true;
        }
        WebUI.waitElement(NEXT_LOCATOR, 20);
        logger.info("Next step loaded");
        return false;
    }

    static void process(Context c) throws Exception {
        Map<String, String> data = c.getProfile().getTradeInData();
        Exception error = null;
        int errorCount = 0;
        while (errorCount < 3) {
            try {
                WebElement modal = WebUI.findElement(MODAL_LOCATOR);
                String currentStep = getStepName(modal);
                switch (currentStep) {
                    case "trade-in guide":
                        break;

                    case "select device":
                        selectDevice(modal, data);
                        break;

                    case "check device condition":
                        selectDeviceConditions(modal);
                        acceptTermsAndConditions(modal);
                        break;

                    case "enter imei":
                        enterIMEI(modal, data.get("imei"));
                        acceptTermsAndConditions(modal);
                        break;

                    case "apply discount":
                        if (List.of("CA").contains(c.site)) {
                            enterIMEI(modal, data.get("imei"));
                        }
                        acceptTermsAndConditions(modal);
                        break;
                }
                boolean modalClosed = nextStep();
                if (modalClosed) {
                    logger.info("Popup closed");
                    error = null;
                    break;
                }
            } catch (StaleElementReferenceException ignore) {
            } catch (Exception e) {
                error = e;
                errorCount++;
            }
        }
        if (error != null) {
            throw error;
        }
        logger.info("Trade-in process done");
    }
}
