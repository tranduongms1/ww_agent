package wisewires.agent;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SCPProcess {
    static Logger logger = LoggerFactory.getLogger(SCPProcess.class);

    WebElementSelector selectType;
    WebElementSelector selectPaymentTerm;
    WebElementSelector selectDuration;
    WebElementSelector selectOption;

    int cartLine;
    String cartSKU;

    SCPProcess() {
        selectType = SCPProcess::selectFirstType;
        selectPaymentTerm = SCPProcess::selectFirstPaymentTerm;
        selectDuration = SCPProcess::selectFirstDuration;
        selectOption = SCPProcess::selectFirstOption;
    }

    static void selectFirstType(List<WebElement> elms) throws Exception {
        WebElement opt = elms.get(0);
        WebUI.scrollToCenterAndClick(opt, 1000);
        logger.info("First SC+ type '%s' selected".formatted(opt.getText()));
    }

    static void selectFirstPaymentTerm(List<WebElement> elms) throws Exception {
        WebElement opt = elms.get(0);
        WebUI.scrollToCenterAndClick(opt, 1000);
        logger.info("First SC+ payment term '%s' selected".formatted(opt.getText()));
    }

    static void selectFirstDuration(List<WebElement> elms) throws Exception {
        WebElement opt = elms.get(0);
        WebUI.scrollToCenterAndClick(opt, 200);
        logger.info("First SC+ duration '%s' selected".formatted(opt.getText()));
    }

    static void selectFirstOption(List<WebElement> elms) throws Exception {
        WebElement opt = elms.get(0);
        WebUI.scrollToCenterAndClick(opt, 200);
        logger.info("First SC+ option '%s' selected".formatted(opt.getText()));
    }
}
