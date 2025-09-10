package wisewires.agent;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BC {
    private static Logger logger = LoggerFactory.getLogger(BC.class);

    static boolean isTradeInOptionSelectable() {
        String to = ".s-option-trade a[an-la='trade-in:yes'i], .wearable-option.trade-in button[an-la='trade-in:yes'i]";
        WebElement elm = WebUI.findElement(to);
        if (elm == null || WebUI.getDomAttribute(elm, "class").contains("disabled")
                || elm.getCssValue("pointer-events").equals("none")
                || WebUI.getDomAttribute(elm, "aria-disabled").equals("true")) {
            return false;
        }
        return true;
    }

    static void addTradeIn(Context c) throws Exception {
        try {
            String to = """
                    .s-option-trade a[an-la='trade-in:yes'i],
                    .wearable-option.trade-in button[an-la='trade-in:yes'i]""";
            WebElement elm = WebUI.waitElement(to, 10);
            WebUI.scrollToCenter(elm);
            WebUI.delay(1);
            WebUI.click(to);
            logger.info("Trade-in option 'Yes' clicked");
            WebUI.waitElement(TradeIn.MODAL_LOCATOR, 10);
            logger.info("Trade-in popup opened");
            TradeIn.process(c);
            logger.info("Trade-in added success on BC page");
        } catch (Exception e) {
            throw new Exception("Unable to add trade-in on BC page");
        }
    }

    static void addSCPlus(Context c) throws Exception {
        SCPProcess p = c.scpProcess;
        try {
            List<WebElement> opts = WebUI.waitElements("""
                    .hubble-product__options-list-wrap:not([style*="hidden"]) .js-smc,
                    .wearable-option.option-care li:not(.depth-two) button:not([an-la*='none']),
                    .smc-list .insurance__item--yes""", 10);
            boolean onlyYes = opts.get(0).getDomAttribute("class").contains("smc-item");

            if (!onlyYes) {
                p.selectType.apply(opts);
                // Handle additional payment terms
                List<WebElement> terms = WebUI.findElements("""
                        .hubble-product__options-payment .s-option-box[aria-disabled="false"],
                        .wearable-option.option-care li.depth-two[aria-disabled="false"],
                        .hubble-product__options-payment .s-option-box,
                        .wearable-option.option-care li.depth-two""");
                if (!terms.isEmpty()) {
                    p.selectPaymentTerm.apply(terms);
                }
            }

            // Handle SC+ Popup
            SCPPopup.waitForOpen(10);
            logger.info("Samsung Care+ popup opened");
            if (onlyYes) {
                SCPPopup.selectType(p.selectType);
                SCPPopup.selectDuration(p.selectDuration);
                SCPPopup.clickContinue();
            }
            SCPPopup.acceptTermAndConditions();
            SCPPopup.clickConfirm();
            SCPPopup.waitForClose(10);
            logger.info("Popup closed, Samsung Care+ added successfully");
        } catch (Exception e) {
            throw new Exception("Unable to add SC+ on BC page", e);
        }
    }

    static void addSIM(Context c) throws Exception {
        SIMProcess p = c.simProcess;
        try {
            String to = "[an-la='tariff:apply']";
            WebElement elm = WebUI.waitElement(to, 10);
            WebUI.scrollToCenter(elm);
            WebUI.delay(1);
            WebUI.click(to);
            logger.info("Add SIM button clicked");
            SIMPopup.waitForOpen(10);
            logger.info("Add SIM popup opened");
            SIMPopup.selectProvider(p.selectProvider);
            SIMPopup.selectOption(p.selectOption);
            SIMPopup.clickContinue();
            WebUI.delay(3);
            SIMPopup.acceptTermAndConditions();
            SIMPopup.clickConfirm();
            SIMPopup.waitForClose(10);
            logger.info("Popup closed, SIM added successfully");
        } catch (Exception e) {
            throw new Exception("Unable to add SIM on BC page", e);
        }
    }

    static boolean shouldSelectSCP() {
        String to = """
                .hubble-product__options-list-wrap:not([style*="hidden"]) .js-smc,
                .wearable-option.option-care li:not(.depth-two) button:not([an-la*='none']),
                .smc-list .insurance__item--yes""";
        List<WebElement> elms = WebUI.findElements(to);
        if (elms.isEmpty()) {
            return false;
        }
        return !elms.stream().anyMatch(e -> WebUI.getDomAttribute(e, "class").contains("is-checked"));
    }

    static void continueToCart() {
        if (isTradeInOptionSelectable()) {
            String to = ".s-option-trade a[an-la='trade-in:no'i], .wearable-option.trade-in button[an-la='trade-in:no'i]";
            WebElement elm = WebUI.findElement(to);
            WebUI.scrollToCenter(elm);
            WebUI.delay(1);
            WebUI.click(to);
        }
        if (shouldSelectSCP()) {
            String to = """
                    .hubble-product__options-list-wrap:not([style*="hidden"]) .js-smc-none,
                    .hubble-product__options-list-wrap:not([style*="hidden"]) #carenone,
                    .wearable-option.option-care button[an-la="samsung care:none"],
                    .smc-list .insurance__item--no""";
            WebElement elm = WebUI.findElement(to);
            WebUI.scrollToCenter(elm);
            WebUI.delay(1);
            WebUI.click(to);
        }
        WebUI.wait(30, 1).withMessage("added to cart").until(driver -> {
            String url = driver.getCurrentUrl();
            if (url.contains("/cart") || url.contains("/checkout")) {
                return true;
            }

            String buyTo = """
                    [an-la='sticky bar:continue'],
                    [an-la='sticky bar:add to cart'],
                    [an-la='sticky bar:pre order'],
                    [an-la='sticky bar:buy now'],
                    [an-la='sticky bar:buy with subscription'],
                    [an-la='secondary:buy with subscription'],
                    [an-la='top sticky bar:add to cart'],
                    [an-la='top sticky bar:continue']""";
            if (WebUI.findElement(buyTo) != null) {
                WebUI.click(buyTo);
                return false;
            }

            String skipTo = "[an-la='evoucher:no addition:skip'], [an-la='evoucher:below evoucher:back']";
            if (WebUI.isSite("FR")) {
                skipTo = "[id='giftContinue']";
            } else if (WebUI.isOneOfSites("HK", "HK_EN")) {
                skipTo = "[an-la='evoucher:below evoucher:back'], [an-la='evoucher:no addition:redeem']";
            }
            if (WebUI.findElement(skipTo) != null) {
                WebUI.click(skipTo);
                return false;
            }

            String continueTo = """
                    [an-la='add-on:continue'],
                    [an-la='add-on:go to cart'],
                    [an-la='free gift:continue'],
                    #giftContinue,
                    [an-la='evoucher:continue'],
                    [an-la='evoucher:go to cart'],
                    [id='nextBtn'],
                    [id='primaryInfoGoCartAddOn']""";
            WebUI.click(continueTo);
            return false;
        });
    }
}
