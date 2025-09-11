package wisewires.agent;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeInProcess {
    static Logger logger = LoggerFactory.getLogger(TradeInProcess.class);

    public Map<String, String> data;

    TradeInProcess(Map<String, String> data) {
        this.data = data;
    }

    static void selectBestDeviceConditions(Context c, List<List<WebElement>> questions) throws Exception {
        for (List<WebElement> options : questions) {
            WebElement opt = options.get(0);
            if (WebUI.isOneOfSites("CZ", "HK", "HK_EN", "NZ", "TR")) {
                opt = Lists.last(options);
            }
            if (WebUI.isOneOfSites("JP") && !WebUI.isOnSiteCart(c)) {
                opt = Lists.last(options);
            }
            WebUI.scrollToCenter(opt);
            opt.click();
            logger.info("Device condition option '%s' checked".formatted(opt.getText()));
        }
    }
}
