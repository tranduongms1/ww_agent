package wisewires.agent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
interface PreFillFormFunction {
    boolean apply(Context c, String formID, WebElement form) throws Exception;
}

@FunctionalInterface
interface SelectDeliveryTypeFunction {
    boolean apply(Context c, WebElement elm);
}

@FunctionalInterface
interface SelectDeliveryTypeAtLineFunction {
    boolean apply(Context c, int line, WebElement elm);
}

@FunctionalInterface
interface SelectDeliveryOptionFunction {
    boolean apply(Context c, int line, WebElement elm);
}

@FunctionalInterface
interface SelectDeliveryServiceFunction {
    boolean apply(Context c, int line, WebElement elm);
}

@FunctionalInterface
interface SelectDeliverySlotFunction {
    boolean apply(Context c, int line, WebElement elm);
}

@FunctionalInterface
interface OnFormAction {
    void apply(Context c, WebElement form) throws Exception;
}

public class CheckoutProcess {
    static Logger logger = LoggerFactory.getLogger(CheckoutProcess.class);

    public List<String> formLocators;

    public AtomicReference<String> selectedDeliveryType = new AtomicReference<>();

    public int seenDeliveryTypes = 0;
    public AtomicReference<List<String>> selectedDeliveryTypes = new AtomicReference<>(new ArrayList<>());

    public int seenDeliveryServices = 0;
    public AtomicReference<List<String>> selectedDeliveryServices = new AtomicReference<>(new ArrayList<>());

    public int seenDeliveryLists = 0;
    public AtomicReference<List<String>> selectedDeliveryOptions = new AtomicReference<>(new ArrayList<>());

    public int seenDeliverySlots = 0;
    public AtomicReference<List<String>> selectedDeliverySlots = new AtomicReference<>(new ArrayList<>());

    public List<PreFillFormFunction> preFillFormFuncs = new ArrayList<>();
    public SelectDeliveryTypeFunction selectDeliveryTypeFunc;
    public SelectDeliveryTypeAtLineFunction selectDeliveryTypeAtLineFunc;
    public SelectDeliveryOptionFunction selectDeliveryOptionFunc;
    public SelectDeliveryServiceFunction selectDeliveryServiceFunc;
    public SelectDeliverySlotFunction selectDeliverySlotFunc;
    public List<Predicate<Context>> untilFuncs = new ArrayList<>();

    private static boolean defaultPreFillForm(Context c, String formID, WebElement form) {
        return true;
    }

    private static boolean defaultSelectDeliveryType(Context c, WebElement elm) {
        c.checkoutProcess.selectedDeliveryType.set("Any");
        return false;
    }

    private static boolean defaultSelectDeliveryTypeAtLine(Context c, int line, WebElement elm) {
        c.checkoutProcess.selectedDeliveryTypes.get().add("Any");
        return false;
    }

    private static String getOptionId(WebElement elm) {
        return elm.getAttribute("id").replaceFirst("group\\d+", "");
    }

    private static void onSelectedDeliveryOption(Context c, int line, WebElement elm) {
        String value = getOptionId(elm.findElement(By.cssSelector("input")));
        c.checkoutProcess.selectedDeliveryOptions.get().add(value);
        logger.info("Delivery option '%s' selected for consignment %d".formatted(value, line));
    }

    private static boolean defaultSelectDeliveryOption(Context c, int line, WebElement elm) {
        List<WebElement> opts = elm.findElements(By.cssSelector("li label:has(input)"));
        for (WebElement opt : opts) {
            if (opt.getAttribute("class").contains("selected-mode")) {
                onSelectedDeliveryOption(c, line, opt);
                return false;
            }
        }
        WebElement opt = opts.get(0);
        WebUI.scrollToCenter(opt);
        WebUI.delay(1);
        opt.click();
        onSelectedDeliveryOption(c, line, opt);
        return false;
    }

    private static void onSelectedDeliveryService(Context c, int line, WebElement elm) {
        String value = getOptionId(elm);
        c.checkoutProcess.selectedDeliveryServices.get().add(value);
        logger.info("Delivery service '%s' selected for consignment %d".formatted(value, line));
    }

    private static boolean defaultSelectDeliveryService(Context c, int line, WebElement elm) {
        List<WebElement> opts = WebUI.waitElements(elm, By.cssSelector("mat-radio-button, mat-checkbox"), 10);
        for (WebElement opt : opts) {
            if (opt.getAttribute("class").contains("checked")) {
                onSelectedDeliveryService(c, line, opt);
                return false;
            }
        }
        c.checkoutProcess.selectedDeliveryServices.get().add(null);
        return false;
    }

    private static void onSelectedDeliverySlot(Context c, int line, WebElement elm) {
        String value = elm.findElement(By.cssSelector(".delivery-dates-date")).getText().replaceAll("\\n", " ");
        c.checkoutProcess.selectedDeliverySlots.get().add(value);
        logger.info("Delivery slot '%s' selected for consignment %d".formatted(value, line));
    }

    private static boolean defaultSelectDeliverySlot(Context c, int line, WebElement elm) {
        List<WebElement> opts = WebUI.waitElements(elm, By.cssSelector(".delivery-dates"), 10);
        for (WebElement opt : opts) {
            if (opt.getAttribute("class").contains("selected-date")) {
                onSelectedDeliverySlot(c, line, opt);
                return false;
            }
        }
        WebElement opt = opts.get(0);
        WebUI.scrollToCenter(opt);
        WebUI.delay(1);
        opt.click();
        onSelectedDeliverySlot(c, line, opt);
        return false;
    }

    private static boolean defaultUntil(Context c) {
        return WebUI.getUrl().contains("CHECKOUT_STEP_PAYMENT");
    }

    public void preProcess() {
        if (selectDeliveryTypeFunc == null) {
            selectDeliveryTypeFunc = CheckoutProcess::defaultSelectDeliveryType;
        }
        if (selectDeliveryTypeAtLineFunc == null) {
            selectDeliveryTypeAtLineFunc = CheckoutProcess::defaultSelectDeliveryTypeAtLine;
        }
        if (selectDeliveryOptionFunc == null) {
            selectDeliveryOptionFunc = CheckoutProcess::defaultSelectDeliveryOption;
        }
        if (selectDeliveryServiceFunc == null) {
            selectDeliveryServiceFunc = CheckoutProcess::defaultSelectDeliveryService;
        }
        if (selectDeliverySlotFunc == null) {
            selectDeliverySlotFunc = CheckoutProcess::defaultSelectDeliverySlot;
        }
        if (preFillFormFuncs.isEmpty()) {
            preFillFormFuncs.add(CheckoutProcess::defaultPreFillForm);
        }
        if (untilFuncs.isEmpty()) {
            untilFuncs.add(CheckoutProcess::defaultUntil);
        }
    }

    public boolean preFillForm(Context c, String formID, WebElement form) throws Exception {
        Iterator<PreFillFormFunction> iterator = preFillFormFuncs.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().apply(c, formID, form)) {
                iterator.remove();
            }
        }
        return !preFillFormFuncs.isEmpty();
    }

    public boolean isDone(Context c) {
        Iterator<Predicate<Context>> iterator = untilFuncs.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().test(c)) {
                iterator.remove();
            }
        }
        return untilFuncs.isEmpty();
    }

    public void reset() {
        preFillFormFuncs.clear();
        untilFuncs.clear();
        selectedDeliveryType = new AtomicReference<>();
        seenDeliveryTypes = 0;
        selectedDeliveryTypes = new AtomicReference<List<String>>(new ArrayList<>());
        seenDeliveryLists = 0;
        selectedDeliveryOptions = new AtomicReference<List<String>>(new ArrayList<>());
        seenDeliverySlots = 0;
        selectedDeliverySlots = new AtomicReference<List<String>>(new ArrayList<>());
    }

    private boolean ensureNotPassedForm(String selector, String backSelector) {
        WebElement backElm = WebUI.findElement(backSelector);
        if (backElm != null && WebUI.findElement(selector) == null) {
            WebUI.scrollToCenter(backElm);
            WebUI.delay(1);
            backElm = WebUI.click(backSelector);
            WebUI.waitForStaleness(backElm, 5);
            WebUI.delay(3);
            WebUI.waitForNotDisplayed(".skeleton", 10);
            return true;
        }
        return false;
    }

    public CheckoutProcess untilSeen(String locator) {
        preFillFormFuncs.add((c, formID, form) -> {
            return WebUI.findElement(locator) == null;
        });
        untilFuncs.add((Context c) -> {
            boolean seen = WebUI.findElement(locator) != null;
            if (seen) {
                logger.info("Element '%s' is now displayed".formatted(locator));
            }
            return seen;
        });
        return this;
    }

    public CheckoutProcess untilForm(String formId) {
        AtomicBoolean seen = new AtomicBoolean(false);
        preFillFormFuncs.add((Context c, String id, WebElement form) -> {
            if (id.equals(formId)) {
                logger.info("Processed until form '%s'".formatted(formId));
                seen.set(true);
            }
            return !seen.get();
        });
        untilFuncs.add(c -> seen.get());
        return this;
    }

    public CheckoutProcess untilPayment() {
        preFillFormFuncs.add(CheckoutProcess::defaultPreFillForm);
        untilFuncs.add(CheckoutProcess::defaultUntil);
        return this;
    }

    public CheckoutProcess onCustomerInfo(OnFormAction action) {
        AtomicBoolean done = new AtomicBoolean(false);
        preFillFormFuncs.add((Context c, String id, WebElement form) -> {
            if (id.equals("app-customer-info-v2")) {
                action.apply(c, form);
                done.set(true);
            }
            return !done.get();
        });
        untilFuncs.add((Context c) -> {
            if (!done.get()) {
                ensureNotPassedForm("app-customer-info-v2", Checkout.CUSTOMER_INFO_EDIT);
            }
            return done.get();
        });
        return this;
    }

    public CheckoutProcess onCustomerAddress(OnFormAction action) {
        AtomicBoolean done = new AtomicBoolean(false);
        preFillFormFuncs.add((Context c, String id, WebElement form) -> {
            if (id.equals("app-customer-address-v2")) {
                action.apply(c, form);
                done.set(true);
            }
            return !done.get();
        });
        untilFuncs.add((Context c) -> {
            if (!done.get()) {
                boolean passed = ensureNotPassedForm("app-customer-address-v2", Checkout.ADDRESS_DETAILS_EDIT);
                if (passed) {
                    c.checkoutProcess.formLocators.remove("app-customer-info-v2");
                }
            }
            return done.get();
        });
        return this;
    }

    public CheckoutProcess onBillingAddress(OnFormAction action) {
        AtomicBoolean done = new AtomicBoolean(false);
        preFillFormFuncs.add((Context c, String id, WebElement form) -> {
            if (id.equals("app-billing-address-v2")) {
                action.apply(c, form);
                done.set(true);
            }
            return !done.get();
        });
        untilFuncs.add((Context c) -> {
            if (!done.get()) {
                boolean passed = ensureNotPassedForm("app-billing-address-v2", Checkout.ADDRESS_DETAILS_EDIT);
                if (passed) {
                    c.checkoutProcess.formLocators.remove("app-customer-address-v2");
                }
            }
            return done.get();
        });
        return this;
    }

    public CheckoutProcess selectIndividualOrder() {
        return onCustomerInfo((c, form) -> {
            WebElement radio = WebUI.findElement(form, "mat-radio-button:has([value='PERSONAL_ORDER'])");
            Form.check(radio);
            logger.info("Selected individual order");
        });
    }

    public CheckoutProcess selectCompanyOrder() {
        return onCustomerInfo((c, form) -> {
            WebElement radio = WebUI.findElement(form, "mat-radio-button:has([value='COMPANY_ORDER'])");
            Form.check(radio);
            logger.info("Selected company order");
        });
    }

    public CheckoutProcess selectOrderOption(String option) {
        return onCustomerInfo((c, form) -> {
            String xpath = "//mat-radio-button//span[normalize-space()='%s']".formatted(option);
            WebElement radio = WebUI.driver.findElement(By.xpath(xpath));
            Form.check(radio);
            logger.info("Selected order option '%s'".formatted(option));
        });
    }

    public CheckoutProcess selectNewCustomerAddress() {
        return onCustomerAddress((c, form) -> {
            WebElement rb = WebUI.findElement(form, "mat-radio-button:has([value='NEW_ADDRESS'])");
            Form.check(rb);
            logger.info("Checked 'New customer address'");
        });
    }

    public CheckoutProcess checkSaveCustomerAddress() {
        return onCustomerAddress((c, form) -> {
            WebElement rb = WebUI.findElement(form, "mat-checkbox:has([name='saveInAddressBook'])");
            Form.check(rb);
            logger.info("Checked 'Save customer address'");
        });
    }

    public CheckoutProcess checkSameAsShippingAddress() {
        return onBillingAddress((c, form) -> {
            BillingAddress.checkSameAsShipping();
        });
    }

    public CheckoutProcess uncheckSameAsShippingAddress() {
        return onBillingAddress((c, form) -> {
            BillingAddress.uncheckSameAsShipping();
        });
    }

    public CheckoutProcess selectNewBillingAddress() {
        return onBillingAddress((c, form) -> {
            WebElement rb = WebUI.findElement(form, "mat-radio-button:has([value='NEW_ADDRESS'])");
            Form.check(rb);
            logger.info("Checked 'New billing address'");
        });
    }

    public CheckoutProcess checkSaveBillingAddress() {
        return onBillingAddress((c, form) -> {
            WebElement rb = WebUI.findElement(form, "mat-checkbox:has([name='saveInAddressBook'])");
            Form.check(rb);
            logger.info("Checked 'Save billing address'");
        });
    }

    public CheckoutProcess selectSavedCustomerAddress(int index) {
        return onCustomerAddress((c, form) -> {
            try {
                String to = "app-customer-address-v2 app-address-list-v2 mat-radio-group > div mat-radio-button";
                List<WebElement> elms = WebUI.findElements(to).stream()
                        .filter(WebElement::isDisplayed)
                        .toList();
                WebElement elm = elms.get(index);
                WebUI.scrollToCenter(elm);
                elm.click();
            } catch (Exception e) {
                throw new Exception("Unable to select saved customer address", e);
            }
        });
    }

    public CheckoutProcess selectSavedBillingAddress(int index) {
        return onBillingAddress((c, form) -> {
            try {
                String to = "app-billing-address-v2 app-address-list-v2 mat-radio-group > div mat-radio-button";
                List<WebElement> elms = WebUI.findElements(to).stream()
                        .filter(WebElement::isDisplayed)
                        .toList();
                WebElement elm = elms.get(index);
                WebUI.scrollToCenter(elm);
                WebUI.delay(1);
                elm.click();
            } catch (Exception e) {
                throw new Exception("Unable to select saved billing address", e);
            }
        });
    }

    private static WebElement findDeliveryOptionByTitleOrCode(WebElement elm, String option) {
        return (WebElement) WebUI.driver.executeScript("""
                let s = arguments[1];
                return Array.from(arguments[0].querySelectorAll('li')).find(opt => {
                    let e = opt.querySelector('.delivery-mode-name-text');
                    if (e && e.innerText.split('\\n')[0].trim().replace(/\\u2060/g, '').toLowerCase() == s) return true;
                    e = opt.querySelector('input');
                    if (e.id.replace(/group\\d+/, '').toLowerCase() == s) return true;
                    return false;
                })""", elm, option.toLowerCase());
    }

    public CheckoutProcess selectDeliveryOption(int consignment, String option) {
        AtomicBoolean done = new AtomicBoolean(false);
        SelectDeliveryOptionFunction func = selectDeliveryOptionFunc;
        selectDeliveryOptionFunc = (c, line, elm) -> {
            if (line == consignment) {
                WebElement item = findDeliveryOptionByTitleOrCode(elm, option);
                WebUI.scrollToCenter(item);
                WebUI.delay(1);
                item.click();
                onSelectedDeliveryOption(c, line, item);
                done.set(true);
                return false;
            }
            if (func != null) {
                return func.apply(c, line, elm);
            } else {
                return CheckoutProcess.defaultSelectDeliveryOption(c, line, elm);
            }
        };
        preFillFormFuncs.add((c, formID, form) -> !done.get());
        untilFuncs.add(c -> done.get());
        return this;
    }

    private static WebElement findDeliveryServiceByCode(WebElement elm, String service) {
        return (WebElement) WebUI.driver.executeScript("""
                let s = arguments[1];
                return Array.from(arguments[0].querySelectorAll('li')).find(opt => {
                    let e = opt.querySelector('mat-radio-button, mat-checkbox');
                    if (e.id.replace(/group\\d+/, '').toLowerCase() == s) return true;
                    return false;
                })""", elm, service.toLowerCase());
    }

    public CheckoutProcess selectDeliveryService(int consignment, String service) {
        AtomicBoolean done = new AtomicBoolean(false);
        SelectDeliveryServiceFunction func = selectDeliveryServiceFunc;
        selectDeliveryServiceFunc = (c, line, elm) -> {
            if (line == consignment) {
                WebElement item = findDeliveryServiceByCode(elm, service);
                WebUI.scrollToCenter(item);
                WebUI.delay(1);
                item.click();
                onSelectedDeliveryService(c, line, elm);
                done.set(true);
                return false;
            }
            if (func != null) {
                return func.apply(c, line, elm);
            } else {
                return CheckoutProcess.defaultSelectDeliveryService(c, line, elm);
            }
        };
        preFillFormFuncs.add((c, formID, form) -> !done.get());
        untilFuncs.add(c -> done.get());
        return this;
    }

    public CheckoutProcess selectDeliverySlot(int consignment, int index) {
        AtomicBoolean done = new AtomicBoolean(false);
        SelectDeliverySlotFunction func = selectDeliverySlotFunc;
        selectDeliverySlotFunc = (c, line, elm) -> {
            if (line == consignment) {
                WebElement item = elm.findElements(By.cssSelector(".delivery-dates")).get(index - 1);
                WebUI.scrollToCenter(item);
                WebUI.delay(1);
                item.click();
                onSelectedDeliverySlot(c, line, elm);
                done.set(true);
                return false;
            }
            if (func != null) {
                return func.apply(c, line, elm);
            } else {
                return CheckoutProcess.defaultSelectDeliverySlot(c, line, elm);
            }
        };
        preFillFormFuncs.add((c, formID, form) -> !done.get());
        untilFuncs.add(c -> done.get());
        return this;
    }

    public void selectDelivery(String type, int consignment, Object option) {
        switch (type) {
            case "option", "mode":
                selectDeliveryOption(consignment, (String) option);
                break;
            case "slot":
                selectDeliverySlot(consignment, (int) option);
                break;
            case "service":
                selectDeliveryService(consignment, (String) option);
                break;
        }
    }
}
