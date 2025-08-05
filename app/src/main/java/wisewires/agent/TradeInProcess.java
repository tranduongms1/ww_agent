package wisewires.agent;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeInProcess {
    static Logger logger = LoggerFactory.getLogger(TradeInProcess.class);

    static void selectBestDeviceConditions(List<List<WebElement>> questions) {
        for (List<WebElement> options : questions) {
            WebElement opt = options.get(0);
            if (WebUI.isOneOfSites("CZ", "HK", "HK_EN", "NZ", "TR")) {
                opt = Lists.last(options);
            }
            WebUI.scrollToCenter(opt);
            opt.click();
            logger.info("Device condition option '%s' checked".formatted(opt.getText()));
        }
    }
}
