package wisewires.agent;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SIMProcess {
    static Logger logger = LoggerFactory.getLogger(SIMProcess.class);

    WebElementSelector selectProvider;
    WebElementSelector selectOption;

    SIMProcess() {
        selectProvider = SIMProcess::selectFirstProvider;
        selectOption = SIMProcess::selectFirstOption;
    }

    static void selectFirstProvider(List<WebElement> elms) throws Exception {
        WebElement opt = elms.get(0);
        WebUI.scrollToCenterAndClick(opt, 1000);
        logger.info("First SIM provider '%s' selected".formatted(opt.getText()));
    }

    static void selectFirstOption(List<WebElement> elms) throws Exception {
        WebElement opt = elms.get(0);
        WebUI.scrollToCenterAndClick(opt, 200);
        logger.info("First SIM option '%s' selected".formatted(opt.getText()));
    }
}
