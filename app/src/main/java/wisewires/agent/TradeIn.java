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

    static String IMEI_LOCATOR = """
            .trade-in-popup__imei-form input,
            .trade-in-popup-v3__imei-form input,
            .trade-in-summary__imei-input input,
            .trade-in-modal [formcontrolname='imei'],
            [formcontrolname='imeiFormControl']""";

    static String NEXT_LOCATOR = """
            [an-la='trade-in:trade-in guide:next'],
            [data-an-la='trade-in:trade-in guide:next'],
            [data-an-la='trade-in:trade-in guide:start'],
            [an-la='trade in:select brand:next'],
            [an-la='trade-in:select device:next'],
            .trade-in-popup__step--show [an-la='trade in:select device:next'],
            [data-an-la='trade-in:select device:next'],
            [an-la='trade-in:check device condition:next'],
            [data-an-la='trade-in:check device condition:next'],
            [an-la='trade-in:enter imei:next'],
            [data-an-la='trade-in:enter imei:next'],
            app-step-one-au .pill-btn--blue.view-more,
            [an-la='trade-in:step2:next'],
            [an-la='trade-in:apply discount:apply trade in'],
            [an-la='trade in:apply discount:confirm'],
            [data-an-la='trade-in:apply discount:add to cart'],
            [data-an-la='trade-in:select device:apply discount'],
            [data-an-la='trade-in:device1:apply discount:add to cart'],
            app-step-four .modal__footer [type='submit'],
            .trade-in-popup-v3__imei-wrap .trade-in-popup-v3__btn-continue,
            [data-an-la="trade-in:check device condition:continue"],
            [data-an-la="trade-in:apply discount:add to cart"]""";

    static String getStepName(WebElement modal) throws Exception {
        String to = """
                .hubble-tradein-popup__device-choose-wrap,
                .trade-in-popup__close,
                .trade-in-popup-v3__close,
                .modal__close""";
        if (WebUI.isSite("AT")) {
            to = ".trade-in-popup-v3__btn-continue, .trade-in-popup-v3__btn-apply, .modal__close";
        }
        WebElement elm = WebUI.waitElement(modal, By.cssSelector(to), 5);
        if (elm != null) {
            String attr = WebUI.getDomAttribute(elm, "an-la", "data-an-la", "class");
            return attr.contains("device-choose-wrap") ? "select device" : attr.split(":")[1];
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
        if (className.contains("mat-mdc-form-field")) {
            if (!className.contains("mat-focused")) {
                WebElement selected = WebUI.findElement(elm, By.cssSelector(".mat-mdc-select-value"));
                if (selected != null && selected.getText().trim().equalsIgnoreCase(option.trim())) {
                    return false;
                }
                WebUI.scrollToCenter(elm);
                elm.click();
                WebUI.delay(1);
            }
            String xpath = "//*[@class='tradein-text' and normalize-space(%s)='%s']".formatted(Util.XPATH_TEXT_LOWER,
                    option.toLowerCase());
            WebElement opt = elm.findElement(By.xpath(xpath));
            WebUI.scrollToCenter(opt);
            opt.click();
            WebUI.delay(1);
            return true;
        }
        if (className.contains("manual-search_para")) {
            WebElement selectElm = elm.findElement(By.cssSelector("mat-select"));
            if (!selectElm.getAttribute("aria-expanded").equals("true")) {
                if (selectElm.getText().trim().equalsIgnoreCase(option.trim())) {
                    return false;
                }
                WebUI.scrollToCenter(elm);
                elm.click();
                WebUI.delay(1);
            }
            String xpath = "//*[@class='mdc-list-item__primary-text' and normalize-space(%s)='%s']/..".formatted(
                    Util.XPATH_TEXT_LOWER,
                    option.toLowerCase());
            WebElement opt = elm.findElement(By.xpath(xpath));
            WebUI.scrollToCenter(opt);
            opt.click();
            WebUI.delay(1);
            return true;
        }
        throw new Exception("Select is not handled");
    }

    static void enterPostalCode(String postalCode) throws Exception {
        try {
            logger.info("Entering trade in postalCode");
            String to = """
                    [formcontrolname="postCodeControl"],
                    input#tradeInZipCode
                    """;
            WebElement elm = WebUI.findElement(to);
            elm.clear();
            elm.sendKeys(postalCode);
            WebUI.delay(1);
            String to1 = """
                    button.post-code-form__btn
                    """;
            WebElement btn = WebUI.findElement(to1);
            if (btn != null) {
                btn.click();
            }
        } catch (Exception e) {
            throw new Exception("Unable to enter trade in postal code", e);
        }
    }

    static void selectCategory(WebElement elm, String category) throws Exception {
        try {
            if (WebUI.getDomAttribute(elm, "class").contains("manual-search_para")) {
                if (select(elm, category)) {
                    logger.info("Device category '%s' selected".formatted(category));
                }
                return;
            }
            WebElement opt = (WebElement) WebUI.driver.executeScript("""
                    var opts = arguments[0].querySelectorAll('input, mat-radio-button, [data-an-la], [an-la]');
                    for (let opt of opts) {
                        if (opt.value && opt.value.toLowerCase() == arguments[1]) return opt.parentElement;
                        let anla = opt.getAttribute('data-an-la') || opt.getAttribute('an-la');
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

        if (className.contains("mat-mdc-form-field")) {
            return elm.findElement(By.cssSelector("[formcontrolname]")).getDomAttribute("formcontrolname");
        }

        if (className.contains("mat-expansion-panel")) {
            return WebUI.driver.executeScript(
                    "return arguments[0].parentNode.parentNode.querySelector('.trade-in__dropdown-header-text').innerText",
                    elm).toString();
        }
        if (className.contains("manual-search_para")) {
            return elm.findElement(By.cssSelector(".manual-search_list")).getText().trim();
        }
        throw new Exception("Unknow device select type");
    }

    static void selectDevice(WebElement modal, Map<String, String> data) throws Exception {
        List<WebElement> elms = modal.findElements(By.cssSelector("""
                 .trade-in-popup__category-device,
                 .trade-in-popup-v3__tradeIn-category,
                 .trade-in-select,
                 mat-radio-group,
                 mat-expansion-panel,
                 mat-form-field:has([formcontrolname]),
                 .manual-search_para,
                 .hubble-tradein-popup__device-choose
                """));
        for (WebElement elm : elms) {
            if (!elm.isDisplayed()) {
                continue;
            }
            String selectType = getSelectType(elm);
            switch (selectType) {
                case
                        "category",
                        "Избери вид на устройството",
                        "Termékkategória":
                    selectCategory(elm, data.get("category"));
                    break;

                case
                        "brand",
                        "brandName",
                        "manufacturer",
                        "brandFormControl",
                        "Brand",
                        "Manufacturer",
                        "Indtast producent:",
                        "Marque",
                        "Tuotemerkki",
                        "Hersteller",
                        "Značka",
                        "Fabricant",
                        "¿Qué marca es?",
                        "Избери производител на устройството",
                        "ブランド名",
                        "Merk",
                        "Marke",
                        "แบรนด์",
                        "選擇品牌",
                        "Nhà sản xuất",
                        "العلامة التجارية",
                        "Gyártó",
                        "Seleziona la marca",
                        "Le fabricant",
                        "Angi produsent:",
                        "Ange tillverkare:":
                    selectBrand(elm, data.get("brand"));
                    break;

                case
                        "model",
                        "Select Model",
                        "modelName",
                        "stockModel",
                        "modelFormControl",
                        "Device",
                        "device",
                        "Model",
                        "Indtast model:",
                        "Modèle",
                        "Συσκευή",
                        "Laite",
                        "Modell",
                        "Modelo",
                        "Typ zařízení",
                        "Избери модел на устройството",
                        "モデル名",
                        "Fashion Model",
                        "Le modèle",
                        "รุ่น",
                        "選擇型號",
                        "Thiết bị",
                        "الجهاز",
                        "Készülék",
                        "Model perangkat",
                        "Seleziona il modello del tuo dispositivo usato o inizia a digitare",
                        "productFormControl",
                        "Skriv inn modell",
                        "Ange modell:":
                    selectModel(elm, data.get("model"));
                    break;

                case
                        "storage",
                        "capacity",
                        "Capacidad",
                        "memory",
                        "storageFormControl",
                        "Storage",
                        "Capacité ou carte graphique",
                        "Taille de l'espace de stockage",
                        "Speichergrösse",
                        "ストレージ",
                        "Geheugen",
                        "ความจุ",
                        "سعة التخزين",
                        "Kapasitas",
                        "Une capacité de stockage":
                    selectStorage(elm, data.get("storage"));
                    break;

                case
                        "color",
                        "Color",
                        "La couleur":
                    selectColor(elm, data.get("color"));
                    break;
            }
        }
    }

    static void selectDeviceConditions(Context c, WebElement modal) throws Exception {
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
            TradeInProcess.selectBestDeviceConditions(c, questions);
        } catch (Exception e) {
            throw new Exception("Unable to select device conditions", e);
        }
    }

    static void acceptTermsAndConditions(WebElement modal) throws Exception {
        List<WebElement> elms = WebUI.findElements(modal, By.cssSelector("""
                .trade-in-popup__confirm-terms .checkbox-radio,
                .trade-in-popup__confirm-terms .checkbox-v2,
                .trade-in-popup-v3__terms .checkbox-v2,
                .terms-and-conditions-container,
                .tnc-container .mdc-checkbox,
                .trade-in__tnc .mdc-checkbox,
                li:has(.trade-in__tc) mat-checkbox"""));
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
            WebElement input = WebUI.waitElement(modal, By.cssSelector(IMEI_LOCATOR), 5);
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
            Optional<WebElement> manualSelectDevicebtn = modal
                    .findElements(By.cssSelector("""
                            .trade-in-popup__imei-result-guide a,
                            a.manual-cta
                            """))
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .findAny();
            manualSelectDevicebtn.ifPresent(el -> {
                WebUI.scrollToCenter(el);
                WebUI.delay(1);
                el.click();
                WebUI.delay(1);
                logger.info("Click manual select device");
            });
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
        if (WebUI.isSite("UK")) {
            WebUI.delay(2);
        }
        if (WebUI.findElement(MODAL_LOCATOR) == null) {
            return true;
        }
        WebUI.waitElement(NEXT_LOCATOR, 20);
        logger.info("Next step loaded");
        return false;
    }

    static void process(Context c) throws Exception {
        String to = """
                .trade-in-popup-v3__imei-form-wrap,
                .post-code-form
                """;
        Map<String, String> data = c.tradeInProcess.data;
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
                            if (WebUI.isOneOfSites("JP")) {
                                WebUI.click("label[for='addconditionCheck0-1']");
                            } else if (WebUI.isOneOfSites("IT")) {
                                WebUI.click("label[for='commoninstantcashback1'], div.trade-in-types__galaxy-button");
                                WebUI.delay(2);
                            }
                            break;

                        case "select brand":
                            WebUI.click(".trade-in-popup__brand-item");
                            break;

                        case "select device":
                            if (WebUI.findElement(".trade-in-popup__model-list") != null) {
                                WebUI.click(".trade-in-popup__model-item");
                                break;
                            }
                            if (WebUI.findElement(to) != null) {
                                enterPostalCode(c.getProfile().getTradeInData().get("tradeinPostalCode"));
                            }
                            selectDevice(modal, data);
                            break;

                        case "check device condition":
                            selectDeviceConditions(c, modal);
                            acceptTermsAndConditions(modal);
                            break;

                        case "enter imei":
                            enterIMEI(modal, data.get("imei"));
                            acceptTermsAndConditions(modal);
                            break;

                        case "apply discount":
                            if (WebUI.waitElement(IMEI_LOCATOR, 2) != null) {
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
