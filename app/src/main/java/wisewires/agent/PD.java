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
            TradeUp.process(c);
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
}
