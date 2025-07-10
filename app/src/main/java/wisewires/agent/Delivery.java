package wisewires.agent;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Delivery {
    static Logger logger = LoggerFactory.getLogger(Delivery.class);

    static void acceptAllTermAndConditions() {
        List<WebElement> cbxs = WebUI.findElements("mat-checkbox[id*=mat-mdc-checkbox]");
        for (WebElement cbx : cbxs) {
            WebUI.scrollToCenter(cbx);
            WebUI.delay(1);
            Form.check(cbx);
        }
    }

    public static void fillAdditionalLocationInfoForm() throws Exception {
        try {
            String to = "app-delivery-info-additional-location-info";
            for (WebElement field : Form.getFields(to, true)) {
                String nameOrLabel = Form.getNameOrLabel(field);
                switch (nameOrLabel) {
                    case "floor":
                        logger.info("Filling '%s'".formatted(nameOrLabel));
                        field.clear();
                        field.sendKeys("1");
                        break;

                    case "Type d'habitation":
                        logger.info("Filling '%s'".formatted(nameOrLabel));
                        Form.select(field, "Maison");
                        break;

                    case "Reprise d'appareil (DEEE)":
                        logger.info("Filling '%s'".formatted(nameOrLabel));
                        Form.select(field, "1");
                        break;
                }
                logger.info("Field '%s' is filled".formatted(nameOrLabel));
            }
        } catch (Exception e) {
            throw new Exception("Unable to fill additional location info form", e);
        }
    }
}
