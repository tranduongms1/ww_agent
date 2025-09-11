package wisewires.agent;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GalaxyClubProcess {
    static Logger logger = LoggerFactory.getLogger(GalaxyClubProcess.class);

    WebElementSelector selectOption;

    GalaxyClubProcess() {
        selectOption = GalaxyClubProcess::selectFirstOption;
    }

    static void selectFirstOption(List<WebElement> elms) throws Exception {
        WebElement opt = elms.get(1);
        WebUI.scrollToCenterAndClick(opt, 200);
        logger.info("1st Galaxy Club option '%s' selected".formatted(opt.getText()));
    }
}
