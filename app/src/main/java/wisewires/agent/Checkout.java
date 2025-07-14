package wisewires.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Checkout {
    static Logger logger = LoggerFactory.getLogger(Checkout.class);

    static String DELIVERY_INFO_TABS_SELECTOR = "div.delivery-info-tabs:not(.invisible-tabs):not(:has(.invisible-tabs)):has([role='tab']:not(hidden))";
    static String DELIVERY_TAB_GROUP_CONTAINER_TOP = ".delivery-tab__group-container:has(.delivery-tab__group-container):has([role='tab']:not(hidden))";
    static String DELIVERY_TAB_GROUP_CONTAINER = ".delivery-tab__group-container:not(:has(.delivery-tab__group-container)):has([role='tab']:not(hidden))";

    static List<String> FORM_SELECTOR = List.of(
            "app-delivery-info-delivery-first",
            "app-customer-info-v2",
            "app-delivery-info-pickup-first",
            "app-customer-address-v2",
            "app-billing-address-v2",
            "app-checkout-identification",
            "app-checkout-step-samsung-care",
            "app-checkout-step-trade-in",
            "app-step-leasing-evollis",
            "app-checkout-step-sim",
            "app-checkout-invoice",
            "app-checkout-tnc",
            "app-checkout-step-delivery",
            DELIVERY_INFO_TABS_SELECTOR,
            DELIVERY_TAB_GROUP_CONTAINER_TOP,
            DELIVERY_TAB_GROUP_CONTAINER,
            "ul.delivery_list",
            "ul.slot_list",
            ".delivery-info__tnc",
            "app-delivery-info-additional-location-info",
            "app-pick-up",
            "app-pick-up-manual",
            "app-chronopost-pickup",
            "app-sf-express-pick-up",
            "app-dhl-packstation",
            "app-in-post-geowidget",
            "app-dpd-parcel-machine",
            "app-omniva-parcel-machine",
            "app-delivery-locker",
            "app-in-post-locker",
            "app-novaposhta-to-pickup-point",
            "app-packetery",
            "app-sameday-locker");

    static String NEXT_SELECTOR = """
            app-delivery-info-delivery-first button.search-delivery-options-btn,
            button[data-an-la='customer details:Continue to delivery options'i],
            app-checkout-step-contact-info button.customer-details--continue-btn,
            button.smc-step--continue-btn,
            button.trade-in--continue-btn,
            button[data-an-la="delivery option:next"i],
            button.sim-plan--continue-btn,
            button[data-an-la="sim:continue to payment"]""";

    static String getFormId(WebElement form) {
        String tagName = form.getTagName();
        if (List.of("ul", "div").contains(tagName)) {
            String className = WebUI.getDomAttribute(form, "class");
            if (className.contains("delivery-info-tabs")) {
                if (WebUI.isSite("uk")) {
                    return "delivery_type_line";
                } else {
                    return "delivery_type_top";
                }
            } else if (className.contains("delivery-tab__group-container")) {
                if (form.findElements(By.cssSelector(".delivery-tab__group-container")).isEmpty()) {
                    return "delivery_type_line";
                } else {
                    return "delivery_type_top";
                }
            } else if (className.contains("delivery_list")) {
                return "delivery_list";
            } else if (className.contains("slot_list")) {
                return "delivery_slot_list";
            } else if (className.contains("delivery-info__tnc")) {
                return "delivery_info_tnc";
            }
        }
        return tagName;
    }

    static Object fillForm(Context c, String formID, WebElement form, List<String> formSelectors) throws Exception {
        CheckoutProcess p = c.checkoutProcess;
        Profile profile = c.getProfile();
        switch (formID) {
            case "app-delivery-info-delivery-first": {
                String postalCode = profile.getCustomerAddress().get("postalCode");
                fillDeliveryFirstForm(postalCode);
                formSelectors.remove(formID);
                break;
            }

            case "app-customer-info-v2": {
                logger.info("Processing customer info form");
                CustomerInfo.autoFill(profile.getCustomerInfo(), true);
                formSelectors.remove(formID);
                break;
            }

            case "app-customer-address-v2": {
                logger.info("Processing customer address form");
                boolean requiredOnly = true;
                if (Arrays.asList("LT", "EE", "LV").contains(c.site.toUpperCase())) {
                    requiredOnly = false;
                }
                CustomerAddress.autoFill(profile.getCustomerAddress(), requiredOnly);
                formSelectors.remove(formID);
                if (!formSelectors.contains("app-delivery-info-pickup-first")) {
                    WebUI.waitElement(".shipment-stepper", 10);
                    return false; // No click continue button
                }
                break;
            }

            case "app-billing-address-v2": {
                logger.info("Processing billing address form");
                boolean requiredOnly = true;
                if (Arrays.asList("LT", "EE", "LV").contains(c.site.toUpperCase())) {
                    requiredOnly = false;
                }
                BillingAddress.autoFill(profile.getBillingAddress(), requiredOnly);
                formSelectors.remove(formID);
                break;
            }

            case "app-checkout-step-samsung-care": {
                logger.info("Processing SMC form");
                fillSMCForm(profile.getCustomerInfo(), true);
                checkSMCTermAndConditions();
                formSelectors.remove(formID);
                break;
            }

            case "app-checkout-step-trade-in": {
                logger.info("Processing trade-in form");
                WebUI.delay(2);
                fillTradeInForm(profile.getTradeInInfo(), true);
                formSelectors.remove(formID);
                break;
            }

            case "app-checkout-step-sim": {
                logger.info("Processing SIM form");
                fillSIMForm(profile.getSIMInfo());
                formSelectors.remove(formID);
                break;
            }

            case "app-checkout-identification": {
                logger.info("Processing customer identification form");
                fillIdentificationForm(profile.getCustomerInfo(), true);
                formSelectors.remove(formID);
                break;
            }

            case "app-checkout-invoice": {
                logger.info("Processing checkout invoice form");
                String to = "mat-radio-button:has(input[value='GENERAL_INVOICE']) .mdc-radio";
                WebElement elm = WebUI.waitElement(to, 10);
                if (elm != null) {
                    WebUI.scrollToCenter(elm);
                    WebUI.delay(1);
                    WebUI.click(elm);
                    formSelectors.remove(formID);
                }
                break;
            }

            case "app-checkout-tnc": {
                try {
                    WebUI.delay(1);
                    List<WebElement> cbxs = form.findElements(By.cssSelector(".mdc-checkbox"))
                            .stream().filter(WebElement::isDisplayed).toList();
                    for (WebElement cbx : cbxs) {
                        if (Form.isRequired(cbx)) {
                            WebUI.scrollToCenter(cbx);
                            WebUI.delay(1);
                            if (WebUI.isOneOfSites("jp")) {
                                WebUI.waitElement("a[href='jp-checkout-required-tnc-popup']", 5);
                                WebUI.click("a[href='jp-checkout-required-tnc-popup']");
                                WebUI.delay(1);
                                WebUI.click(".jp-checkout-required-tnc-popup .button");
                                WebUI.click("a[href='jp-checkout-required-privacy-popup']");
                                WebUI.click(".jp-checkout-required-privacy-popup .button");
                                WebUI.delay(1);
                            }
                            Form.check(cbx);
                        }
                    }
                } catch (Exception e) {
                    throw new Exception("Unable to check required term and conditions" + e);
                }
                formSelectors.remove(formID);
                break;
            }

            case "app-checkout-step-delivery": {
                WebUI.waitElement("""
                        app-delivery-info,
                        app-delivery-info-delivery-first,
                        app-delivery-info-pickup-first,
                        app-delivery-info-no-pickup-split""", 10);
                formSelectors.remove(formID);
                return false;
            }

            case "app-delivery-info-pickup-first": {
                logger.info("Wait for customer address form");
                WebUI.waitElement("app-customer-address-v2", 5);
                formSelectors.remove(formID);
                return false;
            }

            case "delivery_type_top": {
                if (p.selectDeliveryTypeFunc.apply(c, formID)) {
                    return true;
                }
                formSelectors.remove(DELIVERY_INFO_TABS_SELECTOR);
                formSelectors.remove(DELIVERY_TAB_GROUP_CONTAINER_TOP);
                return false;
            }

            case "delivery_type_line": {
                p.seenDeliveryTypes++;
                int index = p.selectedDeliveryTypes.get().size() + 1;
                if (p.seenDeliveryTypes < index)
                    break;
                if (p.selectDeliveryTypeAtLineFunc.apply(c, index)) {
                    return true;
                }
                return false;
            }

            case "delivery_list": {
                p.seenDeliveryLists++;
                int index = p.selectedDeliveryOptions.get().size() + 1;
                if (p.seenDeliveryLists < index)
                    break;
                WebUI.waitElement("ul.slot_list", 2);
                if (p.selectDeliveryOptionFunc.apply(c, index)) {
                    return true;
                }
                return false;
            }

            case "delivery_slot_list": {
                p.seenDeliverySlots++;
                int index = p.selectedDeliverySlots.get().size() + 1;
                if (p.seenDeliverySlots < index)
                    break;
                if (p.selectDeliverySlotFunc.apply(c, index)) {
                    return true;
                }
                return false;
            }

            case "delivery_info_tnc": {
                Delivery.acceptAllTermAndConditions();
                formSelectors.remove(".delivery-info__tnc");
                break;
            }

            case "app-delivery-info-additional-location-info": {
                WebUI.scrollToCenter(form);
                WebUI.delay(1);
                Delivery.fillAdditionalLocationInfoForm();
                WebUI.waitElement("ul.slot_list", 15);
                formSelectors.remove("app-delivery-info-additional-location-info");
                return false;
            }

            default:
                throw new Exception(String.format("Form %s is not handled", formID));
        }
        return null;
    }

    static void fillDeliveryFirstForm(String postalCode) throws Exception {
        try {
            String to = "app-checkout-step-delivery input[name='postcode']";
            WebUI.fill(to, postalCode);
            logger.info("Postal code filled");
        } catch (Exception e) {
            throw new Exception("Unable to fill delivery first form", e);
        }
    }

    static void fillSMCForm(Map<String, String> data, boolean requiredOnly) throws Exception {
        try {
            String to = "app-checkout-step-samsung-care";
            for (WebElement field : Form.getFields(to, requiredOnly)) {
                String nameOrLabel = Form.getNameOrLabel(field);
                switch (nameOrLabel) {
                    case "Giorno":
                        logger.info("Filling '%s'".formatted(nameOrLabel));
                        Form.select(field, data.get("dateOfBirth_Day"));
                        break;

                    case "Mese":
                        logger.info("Filling '%s'".formatted(nameOrLabel));
                        Form.select(field, data.get("dateOfBirth_Month"));
                        break;

                    case "Anno":
                        logger.info("Filling '%s'".formatted(nameOrLabel));
                        Form.select(field, data.get("dateOfBirth_Year"));
                        break;

                    case "serviceTermsAndCondition":
                        break;

                    case "ID Type":
                        logger.info("Filling '%s'".formatted(nameOrLabel));
                        Form.select(field, data.get("idType"));
                        break;

                    case "identification":
                        break;

                    case "saPassport":
                        break;

                    case "foreignPassport":
                        logger.info("Filling '%s'".formatted(nameOrLabel));
                        field.clear();
                        field.sendKeys(data.get("foreignPassport"));
                        break;

                    default:
                        if (Form.isRequired(field)) {
                            throw new Exception("Field '%s' is not handled".formatted(nameOrLabel));
                        } else {
                            logger.info("Field '%s' is not handled".formatted(nameOrLabel));
                            continue;
                        }
                }
                logger.info("Field '%s' is filled".formatted(nameOrLabel));
            }
        } catch (Exception e) {
            throw new Exception("Unable to fill SMC Form", e);
        }
    }

    static void checkSMCTermAndConditions() throws Exception {
        try {
            List<WebElement> cbxs = WebUI.findElements(".mdc-checkbox");
            for (WebElement cbx : cbxs) {
                if (Form.isRequired(cbx)) {
                    WebUI.scrollToCenter(cbx);
                    WebUI.delay(1);
                    Form.check(cbx);
                }
            }
        } catch (Exception e) {
            throw new Exception("Unable to check required SMC term and conditions" + e);
        }
    }

    static void fillTradeInForm(Map<String, String> data, boolean requiredOnly) throws Exception {
        try {
            String to = "app-checkout-step-trade-in";
            for (WebElement field : Form.getFields(to, requiredOnly)) {
                String nameOrLabel = Form.getNameOrLabel(field);
                switch (nameOrLabel) {
                    case "iban":
                        logger.info("Filling '%s'".formatted(nameOrLabel));
                        field.clear();
                        field.sendKeys(data.get("iban"));
                        break;

                    case "bankName":
                        logger.info("Filling '%s'".formatted(nameOrLabel));
                        field.clear();
                        field.sendKeys(data.get("bankName"));
                        break;

                    case "swiftCode":
                        logger.info("Filling '%s'".formatted(nameOrLabel));
                        field.clear();
                        field.sendKeys(data.get("swiftCode"));
                        break;

                    case "fiscalCode":
                        logger.info("Filling '%s'".formatted(nameOrLabel));
                        field.clear();
                        field.sendKeys(data.get("fiscalCode"));
                        break;

                    default:
                        if (Form.isRequired(field)) {
                            throw new Exception("Field '%s' is not handled".formatted(nameOrLabel));
                        } else {
                            logger.info("Field '%s' is not handled".formatted(nameOrLabel));
                            continue;
                        }
                }
                logger.info("Field '%s' is filled".formatted(nameOrLabel));
            }
        } catch (Exception e) {
            throw new Exception("Unable to fill Trade-in form", e);
        }
    }

    static void fillSIMForm(Map<String, String> data) throws Exception {
        try {
            WebElement startApplication = WebUI.findElement("button[data-an-la='checkout:sim:start application']");
            if (startApplication != null) {
                WebUI.scrollToCenter(startApplication);
                WebUI.delay(1);
                WebUI.click(startApplication);
                WebUI.delay(3);
            }
            String to = "app-checkout-step-sim";
            String upperLabel = "";
            for (WebElement field : Form.getFields(to, true)) {
                String nameOrLabel = Form.getNameOrLabel(field);
                switch (nameOrLabel) {
                    case "Ausweisart":
                        logger.info("Filling '%s'".formatted(nameOrLabel));
                        WebUI.scrollToCenter(field);
                        WebUI.delay(1);
                        Form.select(field, data.get("idType"));
                        break;
                    case "issued":
                        logger.info("Filling '%s'".formatted(nameOrLabel));
                        field.clear();
                        field.sendKeys(data.get("issued"));
                        break;
                    case "documentNumber":
                        logger.info("Filling '%s'".formatted(nameOrLabel));
                        field.clear();
                        field.sendKeys(data.get("documentNumber"));
                        break;
                    case "Tag":
                        upperLabel = field.findElement(By.xpath(
                                "./ancestor::dynamic-material-form-control/preceding-sibling::dynamic-material-form-control[1]"))
                                .getText();
                        switch (upperLabel) {
                            case "Ausweis gültig bis *":
                                Form.select(field, "01");
                                break;
                            case "Geburtsdatum *":
                                Form.select(field, "01");
                                break;
                        }
                        break;
                    case "Monat":
                        switch (upperLabel) {
                            case "Ausweis gültig bis *":
                                Form.select(field, "01");
                                break;
                            case "Geburtsdatum *":
                                Form.select(field, "01");
                                break;
                        }
                        break;
                    case "Jahr":
                        switch (upperLabel) {
                            case "Ausweis gültig bis *":
                                Form.select(field, "2030");
                                break;
                            case "Geburtsdatum *":
                                Form.select(field, "1984");
                                break;
                        }
                        break;
                    case "dob":
                        WebUI.scrollToCenter(field);
                        WebUI.delay(1);
                        field.clear();
                        field.sendKeys(data.get("dob"));
                        break;

                    case "Marital status":
                        WebUI.scrollToCenter(field);
                        WebUI.delay(1);
                        Form.select(field, data.get("maritalStatus"));
                        break;

                    case "Employment status":
                        WebUI.scrollToCenter(field);
                        WebUI.delay(1);
                        Form.select(field, data.get("employmentStatus"));
                        break;
                    case "occupation":
                        WebUI.scrollToCenter(field);
                        Thread.sleep(500);
                        field.clear();
                        field.sendKeys(data.get("occupation"));
                        break;
                    case "Employment Area":
                        Form.select(field, data.get("employmentArea"));
                        break;
                    case "employmentYears":
                        field.clear();
                        field.sendKeys("5");
                        break;
                    case "employmentMonths":
                        field.clear();
                        field.sendKeys("5");
                        break;
                    case "Residential status":
                        WebUI.scrollToCenter(field);
                        Thread.sleep(500);
                        Form.select(field, data.get("residentialStatus"));
                        break;
                    case "timeAtYear":
                        upperLabel = field.findElement(By.xpath(
                                "./ancestor::dynamic-material-form-control/preceding-sibling::dynamic-material-form-control[1]"))
                                .getText();
                        switch (upperLabel) {
                            case "Duration of stay at this address *":
                                field.clear();
                                field.sendKeys("5");
                                break;
                            case "How long have you had this account? *":
                                field.clear();
                                field.sendKeys("5");
                                break;
                        }
                        break;
                    case "timeAtMonth":
                        switch (upperLabel) {
                            case "Duration of stay at this address *":
                                field.clear();
                                field.sendKeys("5");
                                break;
                            case "How long have you had this account? *":
                                field.clear();
                                field.sendKeys("5");
                                break;
                        }
                        break;
                    case "accountNumber":
                        WebUI.scrollToCenter(field);
                        field.clear();
                        field.sendKeys(data.get("accountNumber"));
                        break;
                    case "sortCode":
                        field.clear();
                        field.sendKeys(data.get("sortCode"));
                        break;
                    case "Staatsangehörigkeit":
                        WebUI.scrollToCenter(field);
                        WebUI.delay(1);
                        Form.select(field, data.get("nationality"));
                        break;
                    case "iban":
                        field.clear();
                        field.sendKeys(data.get("iban"));
                        break;
                }
                logger.info("Field '%s' is filled".formatted(nameOrLabel));
            }
            String to1 = """
                    app-checkout-step-sim mat-checkbox:has(input[required]:not(:checked)) .mdc-checkbox,
                    app-checkout-step-sim mat-radio-button:has(input[value='NO']:not(:checked)""";
            for (WebElement cbx : WebUI.driver.findElements(By.cssSelector(to1))) {
                if (!cbx.isDisplayed())
                    continue;
                WebUI.scrollToCenter(cbx);
                WebUI.delay(1);
                WebUI.click(cbx);
            }
        } catch (Exception e) {
            throw new Exception("Unable to fill SIM Form", e);
        }
    }

    static void fillIdentificationForm(Map<String, String> data, boolean requiredOnly) throws Exception {
        try {
            String to = "app-checkout-identification";
            for (WebElement field : Form.getFields(to, requiredOnly)) {
                String nameOrLabel = Form.getNameOrLabel(field);
                if (!field.isDisplayed() || !field.isEnabled()) {
                    logger.info(String.format("field %s not displayed/enabled, skipping", nameOrLabel));
                    continue;
                }
                switch (nameOrLabel) {
                    case
                            "Tipo de documento",
                            "NIF/NIE/Pasaporte",
                            "ขอใบกำกับภาษีในนาม":
                        logger.info("Filling '%s (docType)'".formatted(nameOrLabel));
                        WebUI.scrollToCenter(field);
                        WebUI.delay(1);
                        Form.select(field, data.get("docType"));
                        break;

                    case "fiscalCode":
                        logger.info("Filling '%s (docType)'".formatted(nameOrLabel));
                        WebUI.scrollToCenter(field);
                        WebUI.delay(1);
                        field.clear();
                        field.sendKeys(data.get("fiscalCode"));
                        break;

                    default:
                        if (Form.isRequired(field)) {
                            throw new Exception("Field '%s' is not handled".formatted(nameOrLabel));
                        } else {
                            logger.info("Field '%s' is not handled", nameOrLabel);
                            continue;
                        }
                }
                logger.info("Field '%s' is filled".formatted(nameOrLabel));
            }
        } catch (Exception e) {
            throw new Exception("Unable to fill Identification Form", e);
        }
    }

    static boolean nextStep() throws Exception {
        WebElement nextButton = WebUI.findElement(NEXT_SELECTOR);
        if (nextButton != null) {
            WebUI.scrollToCenter(nextButton);
            WebUI.delay(1);
            WebUI.click(nextButton);
            logger.info("Next button is clicked");
            if (WebUI.isOneOfSites("dk", "fi", "no", "se")) {
                WebElement next2 = WebUI.findElement(".cms-elkjop-modal-fragment [data-an-la='delivery option:next'i]");
                if (next2 != null) {
                    WebUI.delay(1);
                    next2.click();
                    logger.info("2nd Next button for Pickup:Store is clicked");
                }
            }
            WebElement errorMsg = WebUI.findElement("mat-error.mat-mdc-form-field-error");
            if (errorMsg != null) {
                throw new Exception(errorMsg.getText() + "\n");
            }
            WebUI.waitForStaleness(nextButton, 15);
            logger.info("Navigated to next step");
            WebUI.delay(1);
            WebUI.wait(30).withMessage("skeleton disappear")
                    .until(d -> WebUI.findElement(".skeleton") == null);
            logger.info("Next step loaded success");
            return true;
        }
        return false;
    }

    static void process(Context c) throws Exception {
        CheckoutProcess p = c.checkoutProcess;
        List<String> formSelectors = new ArrayList<>(FORM_SELECTOR);
        Object result = null;
        for (int errorCount = 0; errorCount < 3;) {
            String url = WebUI.getUrl();
            if (url.contains("paymentNotAvailable")) {
                result = new Exception("Unable to go to payment: Payment not available");
                break;
            }
            if (p.untilFunc.test(c)) {
                result = true;
                break;
            }
            String currentStep = url.split("step=")[1];
            logger.info("Current checkout step: " + currentStep);
            result = WebUI.wait(90).withMessage("process step " + currentStep).until(driver -> {
                try {
                    p.seenDeliveryTypes = 0;
                    p.seenDeliveryLists = 0;
                    p.seenDeliverySlots = 0;
                    List<WebElement> forms = WebUI.findElements(String.join(",", formSelectors));
                    for (WebElement form : forms) {
                        String formID = getFormId(form);
                        if (!form.isDisplayed()) {
                            logger.info(String.format("Form %s not displayed, skipping", formID));
                            continue;
                        }
                        if (p.fillFormFunc.apply(c, formID, formSelectors)) {
                            return true;
                        }
                        final Object r = fillForm(c, formID, form, formSelectors);
                        if (r instanceof Boolean) {
                            return r;
                        }
                    }
                    return nextStep();
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                    WebUI.delay(2);
                    return e;
                }
            });
            errorCount = result instanceof Exception ? errorCount + 1 : 0;
        }
        c.checkoutProcess = null;
        if (result instanceof Exception) {
            throw (Exception) result;
        }
        logger.info("Checkout processed success");
    }

    static void waitForNavigateTo() throws Exception {
        String to = "app-checkout-one";
        try {
            WebUI.waitElement(to, 10);
            logger.info("Checkout page loaded success");
            WebUI.delay(2);
        } catch (Exception e) {
            throw new Exception("checkout main content not loaded");
        }
    }
}
