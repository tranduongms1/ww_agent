package wisewires.agent;

import java.util.List;
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
        c.selectedDeliveryType.set("Any");
        return false;
    }

    public static boolean defaultSelectDeliveryTypeAtLine(Context c, int line) {
        c.selectedDeliveryTypes.accumulateAndGet(List.of("Any"), Util.appendFunction);
        return false;
    }

    public static boolean defaultSelectDeliveryOption(Context c, int line) {
        c.selectedDeliveryOptions.accumulateAndGet(List.of("Any"), Util.appendFunction);
        return false;
    }

    public static boolean defaultSelectDeliverySlot(Context c, int line) {
        c.selectedDeliverySlots.accumulateAndGet(List.of("Any"), Util.appendFunction);
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
}
