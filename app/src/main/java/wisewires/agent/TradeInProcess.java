package wisewires.agent;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeInProcess {
    static Logger logger = LoggerFactory.getLogger(TradeInProcess.class);

    static void selectBestDeviceConditions(List<List<WebElement>> questions) {
        for (List<WebElement> question : questions) {
            WebElement opt = question.get(0);
            if (WebUI.isOneOfSites("CZ", "HK", "HK_EN", "NZ", "TR")) {
                opt = question.get(question.size() - 1); // Last option
            }
            WebUI.scrollToCenter(opt);
            opt.click();
            logger.info("Device condition option '%s' checked".formatted(opt.getText()));
        }
    }
}
