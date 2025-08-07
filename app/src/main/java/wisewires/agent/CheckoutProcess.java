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

    List<String> formLocators;

    static String ADDRESS_DETAILS_EDIT = """
            .address-details .actions__edit,
            .checkout-contact-info-header .actions__edit""";

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

    public static boolean defaultPreFillForm(Context c, String formID, WebElement form) {
        return true;
    }

    public static boolean defaultSelectDeliveryType(Context c, WebElement elm) {
        c.checkoutProcess.selectedDeliveryType.set("Any");
        return false;
    }

    public static boolean defaultSelectDeliveryTypeAtLine(Context c, int line, WebElement elm) {
        c.checkoutProcess.selectedDeliveryTypes.accumulateAndGet(List.of("Any"), Util.appendFunction);
        return false;
    }

    public static boolean defaultSelectDeliveryOption(Context c, int line, WebElement elm) {
        c.checkoutProcess.selectedDeliveryOptions.accumulateAndGet(List.of("Any"), Util.appendFunction);
        return false;
    }

    private static void onSelectedDeliveryService(Context c, int line, WebElement elm) {
        String value = elm.getAttribute("id").replaceFirst("group\\d+", "");
        c.checkoutProcess.selectedDeliveryServices.accumulateAndGet(List.of(value), Util.appendFunction);
        logger.info("Delivery service '%s' selected for consigment %d".formatted(value, line));
    }

    public static boolean defaultSelectDeliveryService(Context c, int line, WebElement elm) {
        List<WebElement> opts = elm.findElements(By.cssSelector("mat-radio-button"));
        for (WebElement opt : opts) {
            if (opt.getAttribute("class").contains("checked")) {
                onSelectedDeliveryService(c, line, opt);
                return false;
            }
        }
        WebElement opt = opts.get(0);
        WebUI.scrollToCenter(opt);
        opt.click();
        onSelectedDeliveryService(c, line, opt);
        return false;
    }

    public static boolean defaultSelectDeliverySlot(Context c, int line, WebElement elm) {
        c.checkoutProcess.selectedDeliverySlots.accumulateAndGet(List.of("Any"), Util.appendFunction);
        return false;
    }

    public static boolean defaultUntil(Context c) {
        return WebUI.getUrl().contains("CHECKOUT_STEP_PAYMENT");
    }

    CheckoutProcess() {
        this.selectDeliveryTypeFunc = CheckoutProcess::defaultSelectDeliveryType;
        this.selectDeliveryTypeAtLineFunc = CheckoutProcess::defaultSelectDeliveryTypeAtLine;
        this.selectDeliveryOptionFunc = CheckoutProcess::defaultSelectDeliveryOption;
        this.selectDeliveryServiceFunc = CheckoutProcess::defaultSelectDeliveryService;
        this.selectDeliverySlotFunc = CheckoutProcess::defaultSelectDeliverySlot;
    }

    public void preProcess() {
        if (this.preFillFormFuncs.isEmpty()) {
            this.preFillFormFuncs.add(CheckoutProcess::defaultPreFillForm);
        }
        if (this.untilFuncs.isEmpty()) {
            this.untilFuncs.add(CheckoutProcess::defaultUntil);
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

    private boolean ensureNotPassedForm(String selector, String backSelector) {
        WebElement backElm = WebUI.findElement(backSelector);
        if (backElm != null && WebUI.findElement(selector) == null) {
            WebUI.scrollToCenter(backElm);
            WebUI.delay(1);
            backElm.click();
            WebUI.waitForStaleness(backElm, 5);
            return true;
        }
        return false;
    }

    public CheckoutProcess untilSeen(String locator) {
        this.preFillFormFuncs.add((c, formID, form) -> {
            return WebUI.findElement(locator) == null;
        });
        this.untilFuncs.add((Context c) -> {
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
        this.preFillFormFuncs.add((Context c, String id, WebElement form) -> {
            if (id.equals(formId)) {
                logger.info("Processed until form '%s'".formatted(formId));
                seen.set(true);
            }
            return !seen.get();
        });
        this.untilFuncs.add((Context c) -> {
            return seen.get();
        });
        return this;
    }

    public CheckoutProcess untilPayment() {
        this.preFillFormFuncs.add(CheckoutProcess::defaultPreFillForm);
        this.untilFuncs.add(CheckoutProcess::defaultUntil);
        return this;
    }

    public void reset() {
        this.preFillFormFuncs.clear();
        this.untilFuncs.clear();
        this.selectedDeliveryType = new AtomicReference<>();
        this.seenDeliveryTypes = 0;
        this.selectedDeliveryTypes = new AtomicReference<List<String>>(new ArrayList<>());
        this.seenDeliveryLists = 0;
        this.selectedDeliveryOptions = new AtomicReference<List<String>>(new ArrayList<>());
        this.seenDeliverySlots = 0;
        this.selectedDeliverySlots = new AtomicReference<List<String>>(new ArrayList<>());
    }

    public CheckoutProcess onCustomerAddress(OnFormAction action) {
        AtomicBoolean done = new AtomicBoolean(false);
        this.preFillFormFuncs.add((Context c, String id, WebElement form) -> {
            if (id.equals("app-customer-address-v2")) {
                action.apply(c, form);
                done.set(true);
            }
            return !done.get();
        });
        this.untilFuncs.add((Context c) -> {
            if (!done.get()) {
                boolean passed = ensureNotPassedForm("app-customer-address-v2", ADDRESS_DETAILS_EDIT);
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
        this.preFillFormFuncs.add((Context c, String id, WebElement form) -> {
            if (id.equals("app-billing-address-v2")) {
                action.apply(c, form);
                done.set(true);
            }
            return !done.get();
        });
        this.untilFuncs.add((Context c) -> {
            if (!done.get()) {
                boolean passed = ensureNotPassedForm("app-billing-address-v2", ADDRESS_DETAILS_EDIT);
                if (passed) {
                    c.checkoutProcess.formLocators.remove("app-customer-address-v2");
                }
            }
            return done.get();
        });
        return this;
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

    public CheckoutProcess selectDifferentBillingAddress() {
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

    public CheckoutProcess selectIndividualOrder() {
        AtomicBoolean done = new AtomicBoolean(false);
        this.preFillFormFuncs.add((Context c, String id, WebElement form) -> {
            if (id.equals("app-customer-info-v2")) {
                WebElement radio = WebUI.findElement(form, "mat-radio-button:has([value='PERSONAL_ORDER'])");
                Form.check(radio);
                logger.info("Selected individual order");
                done.set(true);
            }
            return !done.get();
        });
        this.untilFuncs.add((Context c) -> {
            return done.get();
        });
        return this;
    }

    public CheckoutProcess selectCompanyOrder() {
        AtomicBoolean done = new AtomicBoolean(false);
        this.preFillFormFuncs.add((Context c, String id, WebElement form) -> {
            if (id.equals("app-customer-info-v2")) {
                WebElement radio = WebUI.findElement(form, "mat-radio-button:has([value='COMPANY_ORDER'])");
                Form.check(radio);
                logger.info("Selected company order");
                done.set(true);
            }
            return !done.get();
        });
        this.untilFuncs.add((Context c) -> {
            return done.get();
        });
        return this;
    }
}
