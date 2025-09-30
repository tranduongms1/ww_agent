package wisewires.agent;

import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Splash {
    static Logger logger = LoggerFactory.getLogger(Cart.class);

    static void inputEmail(Context c) throws Exception {
        try {
            String email = c.getProfile().getCustomerInfo().get("email");
            String url = WebUI.getUrl();
            if (url.contains("/guestlogin/")) {
                WebUI.fill("[formcontrolname='guestEmail']", email + Keys.ESCAPE);
                logger.info("Email %s used to checkout".formatted(email));
            } else {
                throw new Exception("Not on splash page");
            }
        } catch (Exception e) {
            throw new Exception("Unable to fill email", e);
        }
    }
}
