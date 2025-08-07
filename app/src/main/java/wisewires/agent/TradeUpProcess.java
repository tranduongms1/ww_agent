package wisewires.agent;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeUpProcess {
    static Logger logger = LoggerFactory.getLogger(TradeUpProcess.class);

    static void selectBestDeviceConditions(List<List<WebElement>> questions) {
        for (List<WebElement> options : questions) {
            WebElement opt = options.get(0);
            WebUI.scrollToCenter(opt);
            opt.click();
            logger.info("Device condition option '%s' selected".formatted(opt.getText()));
        }
    }
}
