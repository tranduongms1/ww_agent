package wisewires.agent;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EWProcess {
    static Logger logger = LoggerFactory.getLogger(EWProcess.class);

    WebElementSelector selectOption;

    EWProcess() {
        selectOption = EWProcess::selectFirstOption;
    }

    static void selectFirstOption(List<WebElement> elms) throws Exception {
        WebElement opt = elms.get(0);
        WebUI.scrollToCenterAndClick(opt, 200);
        logger.info("First E-Warranty option '%s' selected".formatted(opt.getText()));
    }
}
