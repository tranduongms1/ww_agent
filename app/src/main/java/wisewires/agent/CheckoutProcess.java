package wisewires.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
interface PreFillFormFunction {
    boolean apply(Context c, String formID, WebElement form);
}

@FunctionalInterface
interface SelectDeliveryTypeFunction {
    boolean apply(Context c, String type);
}

@FunctionalInterface
interface SelectDeliveryTypeAtLineFunction {
    boolean apply(Context c, int line);
}

@FunctionalInterface
interface SelectDeliveryOptionFunction {
    boolean apply(Context c, int line);
}

@FunctionalInterface
interface SelectDeliverySlotFunction {
    boolean apply(Context c, int line);
}

public class CheckoutProcess {
    static Logger logger = LoggerFactory.getLogger(CheckoutProcess.class);

    List<String> formLocators;

    public AtomicReference<String> selectedDeliveryType = new AtomicReference<>();

    public int seenDeliveryTypes = 0;
    public AtomicReference<List<String>> selectedDeliveryTypes = new AtomicReference<List<String>>(new ArrayList<>());

    public int seenDeliveryLists = 0;
    public AtomicReference<List<String>> selectedDeliveryOptions = new AtomicReference<List<String>>(new ArrayList<>());

    public int seenDeliverySlots = 0;
    public AtomicReference<List<String>> selectedDeliverySlots = new AtomicReference<List<String>>(new ArrayList<>());

    public PreFillFormFunction preFillFormFunc;
    public SelectDeliveryTypeFunction selectDeliveryTypeFunc;
    public SelectDeliveryTypeAtLineFunction selectDeliveryTypeAtLineFunc;
    public SelectDeliveryOptionFunction selectDeliveryOptionFunc;
    public SelectDeliverySlotFunction selectDeliverySlotFunc;
    public Predicate<Context> untilFunc;

    public static boolean defaultPreFillForm(Context c, String formID, WebElement form) {
        return true;
    }

    public static boolean defaultSelectDeliveryType(Context c, String type) {
        c.checkoutProcess.selectedDeliveryType.set("Any");
        return false;
    }

    public static boolean defaultSelectDeliveryTypeAtLine(Context c, int line) {
        c.checkoutProcess.selectedDeliveryTypes.accumulateAndGet(List.of("Any"), Util.appendFunction);
        return false;
    }

    public static boolean defaultSelectDeliveryOption(Context c, int line) {
        c.checkoutProcess.selectedDeliveryOptions.accumulateAndGet(List.of("Any"), Util.appendFunction);
        return false;
    }

    public static boolean defaultSelectDeliverySlot(Context c, int line) {
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
    }

    public CheckoutProcess untilSeen(String locator) {
        Predicate<Context> untilFunc = this.untilFunc;
        this.untilFunc = (Context c) -> {
            if (untilFunc != null && !untilFunc.test(c)) {
                return false;
            }
            boolean seen = WebUI.findElement(locator) != null;
            if (seen) {
                logger.info("Element '%s' is now displayed".formatted(locator));
            }
            return seen;
        };
        return this;
    }

    public CheckoutProcess untilForm(String formId) {
        AtomicBoolean seen = new AtomicBoolean(false);
        PreFillFormFunction preFillFormFunc = this.preFillFormFunc;
        this.preFillFormFunc = (Context c, String id, WebElement form) -> {
            if (preFillFormFunc != null && preFillFormFunc.apply(c, id, form)) {
                return true;
            }
            if (id.equals(formId)) {
                logger.info("Processed until form '%s'".formatted(formId));
                seen.set(true);
            }
            return !seen.get();
        };
        Predicate<Context> untilFunc = this.untilFunc;
        this.untilFunc = (Context c) -> {
            if (untilFunc != null && !untilFunc.test(c)) {
                return false;
            }
            return seen.get();
        };
        return this;
    }

    public void ensureNotNull() {
        if (this.preFillFormFunc == null) {
            this.preFillFormFunc = CheckoutProcess::defaultPreFillForm;
        }
        if (this.untilFunc == null) {
            this.untilFunc = CheckoutProcess::defaultUntil;
        }
    }

    public void reset() {
        this.preFillFormFunc = null;
        this.untilFunc = null;
        this.selectedDeliveryType = new AtomicReference<>();
        this.seenDeliveryTypes = 0;
        this.selectedDeliveryTypes = new AtomicReference<List<String>>(new ArrayList<>());
        this.seenDeliveryLists = 0;
        this.selectedDeliveryOptions = new AtomicReference<List<String>>(new ArrayList<>());
        this.seenDeliverySlots = 0;
        this.selectedDeliverySlots = new AtomicReference<List<String>>(new ArrayList<>());
    }
}
