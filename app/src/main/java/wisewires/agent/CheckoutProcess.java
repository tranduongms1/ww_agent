package wisewires.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
interface FillFormFunction {
    boolean apply(Context c, String formID, List<String> formSelectors);
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

    public AtomicReference<String> selectedDeliveryType = new AtomicReference<>();

    public int seenDeliveryTypes = 0;
    public AtomicReference<List<String>> selectedDeliveryTypes = new AtomicReference<List<String>>(new ArrayList<>());

    public int seenDeliveryLists = 0;
    public AtomicReference<List<String>> selectedDeliveryOptions = new AtomicReference<List<String>>(new ArrayList<>());

    public int seenDeliverySlots = 0;
    public AtomicReference<List<String>> selectedDeliverySlots = new AtomicReference<List<String>>(new ArrayList<>());

    public FillFormFunction fillFormFunc;
    public SelectDeliveryTypeFunction selectDeliveryTypeFunc;
    public SelectDeliveryTypeAtLineFunction selectDeliveryTypeAtLineFunc;
    public SelectDeliveryOptionFunction selectDeliveryOptionFunc;
    public SelectDeliverySlotFunction selectDeliverySlotFunc;
    public Predicate<Context> untilFunc;

    public static boolean noopFillForm(Context c, String formID, List<String> formSelectors) {
        return false;
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
        this.fillFormFunc = CheckoutProcess::noopFillForm;
        this.selectDeliveryTypeFunc = CheckoutProcess::defaultSelectDeliveryType;
        this.selectDeliveryTypeAtLineFunc = CheckoutProcess::defaultSelectDeliveryTypeAtLine;
        this.selectDeliveryOptionFunc = CheckoutProcess::defaultSelectDeliveryOption;
        this.untilFunc = CheckoutProcess::defaultUntil;
    }

    public void untilSeenForm(String formId) {
        AtomicBoolean seen = new AtomicBoolean(false);
        this.fillFormFunc = (Context c, String id, List<String> formSelectors) -> {
            if (id.equals(formId)) {
                seen.set(true);
                logger.info("Form '%s' is now displayed".formatted(formId));
                return true;
            }
            return false;
        };
        this.untilFunc = (Context c) -> {
            if (seen.get()) {
                return true;
            }
            if (WebUI.getUrl().contains("CHECKOUT_STEP_PAYMENT")) {
                logger.error("Unable to process until seen form '%s'".formatted(formId));
                return true;
            }
            return false;
        };
    }

    public void reset() {
        this.selectedDeliveryType = new AtomicReference<>();
        this.seenDeliveryTypes = 0;
        this.selectedDeliveryTypes = new AtomicReference<List<String>>(new ArrayList<>());
        this.seenDeliveryLists = 0;
        this.selectedDeliveryOptions = new AtomicReference<List<String>>(new ArrayList<>());
        this.seenDeliverySlots = 0;
        this.selectedDeliverySlots = new AtomicReference<List<String>>(new ArrayList<>());
    }
}
