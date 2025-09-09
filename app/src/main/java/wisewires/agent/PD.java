package wisewires.agent;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PD {
    private static Logger logger = LoggerFactory.getLogger(PD.class);

    static void addTradeIn(Context c) throws Exception {
        try {
            String to = ".pd-option-selector:has([data-pvitype='tv'][an-la='trade-in:yes'i])";
            WebElement elm = WebUI.waitElement(to, 10);
            WebUI.scrollToCenter(elm);
            WebUI.delay(1);
            WebUI.click(to);
            logger.info("Trade-in option 'Yes' clicked");
            WebUI.waitElement(TradeUp.MODAL_LOCATOR, 10);
            logger.info("Trade-in modal opened");
            TradeIn.process(c);
            logger.info("Trade-in added success on PD page");
        } catch (Exception e) {
            throw new Exception("Unable to add trade-in on TV PD page");
        }
    }

    static void addTradeUp(Context c) throws Exception {
        try {
            String to = ".pd-option-selector:has([an-la='trade-up:yes'i])";
            WebElement elm = WebUI.waitElement(to, 10);
            WebUI.scrollToCenter(elm);
            WebUI.delay(1);
            WebUI.click(to);
            logger.info("Trade-up option 'Yes' clicked");
            WebUI.waitElement(TradeUp.MODAL_LOCATOR, 10);
            logger.info("Trade-up modal opened");
            TradeUp.process(c);
            logger.info("Trade-up added success on PD page");
        } catch (Exception e) {
            throw new Exception("Unable to add trade-up on PD page");
        }
    }

    static void addSCPlus(Context c) throws Exception {
        try {
            String to = """
                    .hubble-product__options-list-wrap:not([style*='hidden']) .js-smc,
                    .wearable-option.option-care li:not(.depth-two) button:not([an-la*='none']),
                    .smc-list .insurance__item--yes,
                    .pd-select-option__item>.pd-option-selector:has([an-la='samsung care:yes'])""";
            WebElement elm = WebUI.findElement(to);
            if (elm != null) {
                WebUI.scrollToCenter(elm);
                WebUI.delay(1);
                elm.click();
            }

            // Handle additional payment terms
            String toPayment = "div[class='pd-select-option__payment'] .pd-option-selector div:has([an-la='samsung care:yes'])";
            WebElement elmPayment = WebUI.findElement(toPayment);

            if (elmPayment != null) {
                WebUI.scrollToCenter(elmPayment);
                WebUI.delay(1);
                elmPayment.click();
            }

            // Handle SC+ Popup
            if (elm.getAttribute("class").contains("smc-item")) {
                SCPPopup.selectType(SCPProcess::selectFirstType);
                SCPPopup.selectDuration(SCPProcess::selectFirstDuration);
                SCPPopup.clickContinue();
            }
            SCPPopup.acceptTermAndConditions();
            SCPPopup.clickConfirm();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    static void addEWarranty(Context c) throws Exception {
        EWProcess p = c.ewProcess;
        try {
            String to = ".pd-option-selector:has([an-la='extended warranty:yes'i])";
            WebElement elm = WebUI.waitElement(to, 10);
            WebUI.scrollToCenter(elm);
            WebUI.delay(1);
            WebUI.click(to);
            logger.info("E-Warranty option 'Yes' clicked");
            EWPopup.waitForOpen(10);
            //Handle E-Warranty Popup
            EWPopup.selectFirstOption(p.selectOption);
            EWPopup.acceptTermAndConditions();
            EWPopup.clickConfirm();
            EWPopup.waitForClose(10);
            logger.info("E-Warranty added success on PD page");
        } catch (Exception e) {
            throw new Exception("Unable to add e-warranty on PD page");
        }
    }

    static void continueToCart() {
        WebUI.wait(30, 1).withMessage("added to cart").until(driver -> {
            String buyTo = """
                    [an-la='secondary Nav:add to cart'],
                    [an-la='secondary Nav:pre order'],
                    [an-la='pd buying tool:add to cart'],
                    .cost-box__cta a[an-ac='addToCart']""";
            if (WebUI.findElement(buyTo) != null) {
                WebElement elm = WebUI.findElement(buyTo);
                WebUI.scrollToCenter(elm);
                WebUI.click(elm);
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
            if (WebUI.findElement(continueTo) != null) {
                WebUI.click(continueTo);
                return false;
            }

            return driver.getCurrentUrl().contains("/cart");
        });
    }
}
