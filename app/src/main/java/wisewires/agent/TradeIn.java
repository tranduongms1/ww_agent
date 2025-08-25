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
            app-step-one-au .pill-btn--blue.view-more,
            [an-la='trade-in:step2:next'],
            [an-la='trade-in:apply discount:apply trade in'],
            [data-an-la='trade-in:apply discount:add to cart'],
            [data-an-la='trade-in:select device:apply discount'],
            [data-an-la='trade-in:device1:apply discount:add to cart']""";

    static String getStepName(WebElement modal) throws Exception {
        WebElement closeBtn = WebUI.waitElement(modal, By.cssSelector("""
                .trade-in-popup__close,
                .trade-in-popup-v3__close,
                .modal__close"""), 5);
        if (closeBtn != null) {
            return WebUI.getDomAttribute(closeBtn, "an-la", "data-an-la").split(":")[1];
        }
        return "unknow";
    }

    static boolean select(WebElement elm, String option) throws Exception {
        String className = elm.getAttribute("class");
        if (className.contains("trade-in-select")) {
            if (!className.contains("is-opened")) {
                String selected = elm.findElement(By.cssSelector(".select-txt")).getText().trim();
                if (selected.equalsIgnoreCase(option.trim())) {
                    return false;
                }
                WebUI.scrollToCenter(elm);
                elm.click();
                WebUI.delay(1);
            }
            String css = "[an-la='%s'i], [value='%s'i]".replaceAll("%s", option);
            WebElement opt = elm.findElement(By.cssSelector(css));
            WebUI.scrollToCenter(opt);
            opt.click();
            return true;
        }
        if (className.contains("mat-expansion-panel")) {
            if (!className.contains("mat-expanded")) {
                String selected = WebUI.driver.executeScript("""
                        const e = arguments[0].querySelector(`
                            .mat-expansion-panel-header-title,
                            .trade-in__search
                        `);
                        return e.value || e.innerText.trim()""", elm).toString();
                if (selected.equalsIgnoreCase(option.trim())) {
                    return false;
                }
                WebUI.scrollToCenter(elm);
                elm.click();
                WebUI.delay(1);
            }
            String xpath = ".//span[%s='%s']".formatted(Util.XPATH_TEXT_LOWER, option.toLowerCase());
            WebElement opt = elm.findElement(By.xpath(xpath));
            WebUI.scrollToCenter(opt);
            opt.click();
            return true;
        }
        throw new Exception("Select is not handled");
    }

    static void selectCategory(WebElement elm, String category) throws Exception {
        try {
            WebElement opt = (WebElement) WebUI.driver.executeScript("""
                    var opts = arguments[0].querySelectorAll('input, [data-an-la]');
                    for (let opt of opts) {
                        if (opt.value && opt.value.toLowerCase() == arguments[1]) return opt.parentElement;
                        let anla = opt.getAttribute('data-an-la');
                        if (anla && anla.toLowerCase().endsWith(arguments[1])) return opt;
                        let label = opt.querySelector('.box__label');
                        if (label && label.innerText.toLowerCase() == arguments[1]) return opt;
                    }
                    return null""", elm, category.toLowerCase());
            boolean checked = WebUI.getDomAttribute(opt, "class").contains("checked") ||
                    !opt.findElements(By.cssSelector(":checked")).isEmpty();
            if (!checked) {
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

    static String getSelectType(WebElement elm) throws Exception {
        String className = elm.getAttribute("class");
        if (className.contains("category"))
            return "category";

        if (className.contains("trade-in-select"))
            return elm.findElement(By.cssSelector("ul")).getAttribute("id");

        if (className.contains("mat-mdc-radio-group")) {
            String id = WebUI.getDomAttribute(elm, "id");
            return id.isEmpty() ? "category" : id; // AU site
        }

        if (className.contains("mat-expansion-panel")) {
            return WebUI.driver.executeScript(
                    "return arguments[0].parentNode.parentNode.querySelector('.trade-in__dropdown-header-text').innerText",
                    elm).toString();
        }

        throw new Exception("Unknow device select type");
    }

    static void selectDevice(WebElement modal, Map<String, String> data) throws Exception {
        List<WebElement> elms = modal.findElements(By.cssSelector("""
                .trade-in-popup__category-device,
                .trade-in-popup-v3__tradeIn-category,
                .trade-in-select,
                mat-radio-group,
                mat-expansion-panel"""));
        for (WebElement elm : elms) {
            if (!elm.isDisplayed()) {
                continue;
            }
            String selectType = getSelectType(elm);
            switch (selectType) {
                case "category":
                    selectCategory(elm, data.get("category"));
                    break;

                case
                        "brand",
                        "brandName",
                        "manufacturer",
                        "Brand",
                        "Manufacturer",
                        "Indtast producent:",
                        "Marque",
                        "Tuotemerkki",
                        "Hersteller",
                        "Značka",
                        "Fabricant":
                    selectBrand(elm, data.get("brand"));
                    break;

                case
                        "model",
                        "modelName",
                        "stockModel",
                        "Device",
                        "Model",
                        "Indtast model:",
                        "Modèle",
                        "Συσκευή",
                        "Laite",
                        "Modell",
                        "Typ zařízení":
                    selectModel(elm, data.get("model"));
                    break;

                case
                        "storage",
                        "capacity",
                        "memory",
                        "Storage",
                        "Capacité ou carte graphique",
                        "Taille de l'espace de stockage",
                        "Speichergrösse":
                    selectStorage(elm, data.get("storage"));
                    break;

                case
                        "color",
                        "Color":
                    selectColor(elm, data.get("color"));
                    break;
            }
        }
    }

    static void selectDeviceConditions(WebElement modal) throws Exception {
        try {
            List<List<WebElement>> questions = new ArrayList<>();
            List<WebElement> elms = WebUI.waitElements(modal, By.cssSelector("""
                    .trade-in-popup__condition-list-item,
                    .trade-in-popup__summary-accept-list,
                    .trade-in-popup-v3__condition-list-item,
                    .trade-in-popup-v3__summary-accept-list,
                    .condition-radio"""), 5);
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
                .trade-in-popup-v3__terms .checkbox-v2,
                .terms-and-conditions-container,
                .tnc-container,
                .trade-in__tnc .mdc-checkbox"""));
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
            WebElement input = WebUI.waitElement(modal, By.cssSelector("""
                    .trade-in-popup__imei-form input,
                    .trade-in-popup-v3__imei-form input,
                    .trade-in-summary__imei-input input,
                    .trade-in-modal [formcontrolname='imei']"""), 5);
            WebUI.scrollToCenter(input);
            input.clear();
            WebUI.delay(1);
            input.sendKeys(imei + Keys.ENTER);
            WebUI.delay(2);
            logger.info("Device IMEI '%s' entered".formatted(imei));
            Optional<WebElement> loading = modal
                    .findElements(By.cssSelector(".circular-progress, mat-spinner"))
                    .stream().findAny();
            if (loading.isPresent()) {
                WebUI.delay(2);
                WebUI.waitForDisappear(loading.get(), 10);
                logger.info("Device IMEI checking done");
            }
        } catch (Exception e) {
            throw new Exception("Unable to enter device IMEI '%s'".formatted(imei));
        }
    }

    static boolean nextStep(WebElement modal) throws Exception {
        WebElement btn = WebUI.findElement(NEXT_LOCATOR);
        if (btn == null && WebUI.findElement(".circular-progress") != null) {
            throw new Exception("Modal keep loading");
        }
        WebUI.wait(3).withMessage("Next button enabled").until(d -> btn.isEnabled());
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
        while (errorCount < 5) {
            try {
                WebUI.waitForNotDisplayed(".circular-progress", 5);
                WebElement modal = WebUI.findElement(MODAL_LOCATOR);
                String currentStep = getStepName(modal);
                try {
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
                            if (List.of("CA", "DK", "FI", "GR", "CZ").contains(c.site)) {
                                enterIMEI(modal, data.get("imei"));
                            }
                            acceptTermsAndConditions(modal);
                            break;
                    }
                } catch (StaleElementReferenceException e) {
                    WebUI.delay(1);
                    continue;
                }
                boolean modalClosed = nextStep(modal);
                if (modalClosed) {
                    logger.info("Popup closed");
                    error = null;
                    break;
                }
                errorCount = 0;
            } catch (Exception e) {
                error = e;
                errorCount++;
                WebUI.delay(2);
            }
        }
        if (error != null) {
            throw error;
        }
        logger.info("Trade-in process done");
    }
}
