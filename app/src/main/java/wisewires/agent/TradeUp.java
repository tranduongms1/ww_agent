package wisewires.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TradeUp {
    static Logger logger = LoggerFactory.getLogger(TradeIn.class);

    static String MODAL_LOCATOR = """
            .vd-trade-in-popup,
            [class^='trade-up-steps']""";

    static String FIELD_LOCATORS = """
            input#postal-code,
            [formcontrolname='postCodeControl'],
            .sdf-comp-model-menu,
            .sdf-comp-brand-menu,
            mat-expansion-panel,
            .modal__container,
            .trade-up-device-selection-container,
            mat-form-field:has([formcontrolname])""";

    static String NEXT_LOCATOR = """
            [an-la='trade-up:select device:next'],
            [data-an-la='trade-up:select device:next'],
            [an-la='trade-up:check device condition:next'],
            [data-an-la='trade-up:check device condition:next'],
            [an-la*='apply trade up'],
            [data-an-la*='apply trade up'],
            [data-an-la='tradeup2termswrapper:add to cart'],
            [data-an-la='trade-up:trade-up guide:next']""";

    static String getStepName(WebElement modal) throws Exception {
        WebElement elm = WebUI.waitElement(modal, By.cssSelector("""
                .step-three-card,
                    .vd-trade-in-popup__close,
                    .modal__close"""), 5);
        if (elm != null) {
            String attr = WebUI.getDomAttribute(elm, "an-la", "data-an-la", "class");
            return attr.contains("step-three-card") ? "apply discount" : attr.split(":")[1];
        }
        return "unknow";
    }

    static String getFieldName(WebElement elm) {
        String name = WebUI.getDomAttribute(elm, "formcontrolname", "id", "class");
        if (name.contains("model"))
            return "model";
        if (name.contains("brand"))
            return "brand";
        if (name.contains("mat-expansion-panel")) {
            return WebUI.driver.executeScript(
                    "return arguments[0].parentNode.parentNode.querySelector('.trade-up__dropdown-header .device_category').innerText",
                    elm).toString();
        }
        if (name.contains("mat-mdc-form-field")) {
            return elm.findElement(By.cssSelector("[formcontrolname]")).getDomAttribute("formcontrolname");
        }
        return name;
    }

    static void select(WebElement elm, String option) throws Exception {
        String className = WebUI.getDomAttribute(elm, "class");
        if (!className.contains("open") && !className.contains("mat-focused")) {
            elm.click();
            Thread.sleep(200);
        }
        WebElement opt = WebUI.findElement(elm, By.xpath(".//li | //*[@class='tradeup-text']"));
        opt.click();
    }

    static void selectDeviceConditions(WebElement modal) throws Exception {
        try {
            List<List<WebElement>> questions = new ArrayList<>();
            List<WebElement> elms = WebUI.waitElements(modal, By.cssSelector("""
                    .vd-trade-in-popup__summary-accept-list,
                    .condition-radio"""), 5);
            for (WebElement elm : elms) {
                if (elm.findElements(By.cssSelector("input:checked")).isEmpty()) {
                    By by = By.cssSelector(":has(> [type='radio'])");
                    questions.add(elm.findElements(by));
                }
            }
            TradeUpProcess.selectBestDeviceConditions(questions);
        } catch (Exception e) {
            throw new Exception("Unable to select device conditions", e);
        }
    }

    static void acceptTermsAndConditions(WebElement modal) throws Exception {
        List<WebElement> elms = WebUI.findElements(modal, By.cssSelector("""
                .vd-trade-in-popup__agree .checkbox-v2,
                .mdc-checkbox:has(+ label .trade-up__tc)
                """));
        for (WebElement elm : elms) {
            if (elm.findElements(By.cssSelector("input:checked")).isEmpty()) {
                if (elm.getDomAttribute("class").contains("checkbox-v2")) {
                    WebUI.scrollIntoView(elm);
                } else {
                    WebUI.scrollToCenter(elm);
                }
                WebUI.delay(1);
                WebUI.click(elm, 10, 10);
                logger.info("Term and conditions accepted");
            }
        }
    }

    static boolean nextStep(WebElement modal) throws Exception {
        WebElement btn = WebUI.findElement(NEXT_LOCATOR);
        if (btn == null) {
            throw new Exception("Modal keep loading");
        }
        WebUI.wait(3).withMessage("Next button enabled").until(d -> btn.isEnabled());
        btn.click();
        logger.info("Next button clicked");
        WebUI.waitForDisappear(btn, 10);
        if (WebUI.isSite("UK")) {
            WebUI.delay(2);
        }
        if (WebUI.waitElement(MODAL_LOCATOR, 3) == null) {
            return true;
        }
        WebUI.waitElement(NEXT_LOCATOR, 10);
        logger.info("Next step loaded");
        return false;
    }

    static void process(Context c) throws Exception {
        Map<String, String> data = c.getProfile().getTradeUpData();
        Exception error = null;
        int errorCount = 0;
        while (errorCount < 3) {
            try {
                WebElement modal = WebUI.findElement(MODAL_LOCATOR);
                String currentStep = getStepName(modal);
                switch (currentStep) {
                    case "select device":
                        List<WebElement> elms = modal.findElements(By.cssSelector(FIELD_LOCATORS));
                        if (elms.isEmpty()) {
                            List<WebElement> radios = modal.findElements(By.cssSelector("mat-radio-group li.service__product"));
                            for (WebElement radio : radios) {
                                WebUI.scrollToCenter(radio);
                                WebUI.click(radio);
                                WebUI.delay(1);
                            }
                        } else {
                            for (WebElement elm : elms) {
                                WebUI.scrollToCenter(elm);
                                String name = getFieldName(elm);
                                switch (name) {
                                    case "postal-code", "postCodeControl":
                                        elm.clear();
                                        WebUI.delay(1);
                                        elm.sendKeys(data.get("postalCode"));
                                        elm.sendKeys(Keys.ENTER);
                                        WebUI.waitElement(".sdf-comp-postal-code-input-panel.success", 5);
                                        break;

                                    case
                                            "model",
                                            "modelFormControl",
                                            "Select Size",
                                            "Selecteer maat",
                                            "Wähle die Grösse",
                                            "Choisis une taille",
                                            "¿De que tamaño es?",
                                            "Tyyppi/malli",
                                            "Type/model",
                                            "เลือกขนาด/ประเภทสินค้า",
                                            "Model Size",
                                            "حجم الموديل",
                                            "Өлшемді таңдаңыз",
                                            "Type / Model",
                                            "選擇尺寸",
                                            "Выберите размер",
                                            "Wähle dein Altgeräte-Modell",
                                            "Product",
                                            "Produit":
                                        select(elm, data.get("model"));
                                        break;

                                    case
                                            "brand",
                                            "brandFormControl",
                                            "Select Brand",
                                            "Selecteer merk",
                                            "Wähle die Marke",
                                            "Choisis la marque",
                                            "¿De qué marca es?",
                                            "Merkki",
                                            "Mærke",
                                            "เลือกยี่ห้อ",
                                            "Brand",
                                            "العلامة التجارية",
                                            "Өндірушіні таңдаңыз",
                                            "選擇品牌",
                                            "Выберите производителя",
                                            "Wähle deine Altgeräte-Marke",
                                            "Marque":
                                        select(elm, data.get("brand"));
                                        break;
                                }
                            }
                        }
                        break;

                    case "check device condition":
                        selectDeviceConditions(modal);
                        break;

                    case "apply discount":
                        acceptTermsAndConditions(modal);
                        break;
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
        logger.info("Trade-up process done");
    }
}
