package wisewires.agent;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TradeUp {
    static Logger logger = LoggerFactory.getLogger(TradeIn.class);

    static String MODAL_LOCATOR = """
            .vd-trade-in-popup""";

    static String FIELD_LOCATORS = """
            input#postal-code,
            [formcontrolname='postCodeControl'],
            .sdf-comp-model-menu,
            .sdf-comp-brand-menu""";

    static String NEXT_LOCATOR = """
            [an-la='trade-up:select device:next'],
            [an-la='trade-up:check device condition:next'],
            [an-la*='apply trade up']""";

    static String getStepName(WebElement modal) throws Exception {
        WebElement closeBtn = WebUI.waitElement(modal, By.cssSelector("""
                .vd-trade-in-popup__close"""), 5);
        if (closeBtn != null) {
            return WebUI.getDomAttribute(closeBtn, "an-la").split(":")[1];
        }
        return "unknow";
    }

    static String getFieldName(WebElement elm) {
        String name = WebUI.getDomAttribute(elm, "formcontrolname", "id", "class");
        if (name.contains("model"))
            return "model";
        if (name.contains("brand"))
            return "brand";
        return name;
    }

    static void select(WebElement elm, String option) throws Exception {
        if (!WebUI.getDomAttribute(elm, "class").contains("open")) {
            elm.click();
            Thread.sleep(200);
        }
        WebElement opt = WebUI.findElement(elm, "li");
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
                .vd-trade-in-popup__agree .checkbox-v2"""));
        for (WebElement elm : elms) {
            if (elm.findElements(By.cssSelector("input:checked")).isEmpty()) {
                WebUI.scrollIntoView(elm);
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
        if (WebUI.findElement(MODAL_LOCATOR) == null) {
            return true;
        }
        WebUI.waitElement(NEXT_LOCATOR, 10);
        logger.info("Next step loaded");
        return false;
    }

    static void process(Context c) throws Exception {
        Exception error = null;
        int errorCount = 0;
        while (errorCount < 3) {
            try {
                WebElement modal = WebUI.findElement(MODAL_LOCATOR);
                String currentStep = getStepName(modal);
                switch (currentStep) {
                    case "select device":
                        List<WebElement> elms = modal.findElements(By.cssSelector(FIELD_LOCATORS));
                        for (WebElement elm : elms) {
                            WebUI.scrollToCenter(elm);
                            String name = getFieldName(elm);
                            switch (name) {
                                case "postal-code", "postCodeControl":
                                    elm.clear();
                                    WebUI.delay(1);
                                    elm.sendKeys("5000");
                                    elm.sendKeys(Keys.ENTER);
                                    WebUI.waitElement(".sdf-comp-postal-code-input-panel.success", 5);
                                    break;

                                case "model":
                                    select(elm, name);
                                    break;

                                case "brand":
                                    select(elm, name);
                                    break;
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
