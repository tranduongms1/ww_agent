package wisewires.agent;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfileSetting {
    static Logger logger = LoggerFactory.getLogger(ProfileSetting.class);

    public static void switchToShipping() throws Exception {
        WebUI.click("[data-an-la='address:shipping']");
        logger.info("Switched to Shipping tab");
    }

    public static void switchToBilling() throws Exception {
        WebUI.waitElement("[data-an-la='address:billing']", 5);
        WebUI.click("[data-an-la='address:billing']");
        logger.info("Switched to Billing tab");
    }

    public static void addShippingAddress(Map<String, String> data) throws Exception {
        WebUI.click("button[data-an-la='address:add']");
        WebUI.waitElement("form.address-form", 5);
        WebUI.delay(3);
        CustomerAddress.autoFill(data, true);
        WebUI.click("[data-an-la='address:add new address:save']");
        WebUI.waitForNotDisplayed("form.address-form", 5);
        ProfileSetting.loadAllAddresses();
        logger.info("New Shipping Address added sucessfully");
    }

    public static void addBillingAddress(Map<String, String> data) throws Exception {
        WebUI.click("button[data-an-la='address:add']");
        WebUI.waitElement("form.address-form", 5);
        WebUI.delay(3);
        CustomerAddress.autoFill(data, true);
        WebUI.click("[data-an-la='address:add new address:save']");
        WebUI.waitForNotDisplayed("form.address-form", 5);
        ProfileSetting.loadAllAddresses();
        logger.info("New Shipping Address added sucessfully");
    }

    public static void editShippingAddress(Map<String, String> data, int index) throws Exception {
        WebUI.waitElement("button.card__edit", 5);

        List<WebElement> editButtons = WebUI.driver.findElements(
                By.cssSelector("li.address__card button[data-an-la='address:edit']"));

        if (editButtons.isEmpty() || index >= editButtons.size()) {
            throw new NoSuchElementException("Edit button at index " + index + " not found");
        }

        editButtons.get(index).click();
        WebUI.waitElement("form.address-form", 5);
        WebUI.delay(2);
        CustomerAddress.autoFill(data, true);

        String to = "[data-an-la='address:edit a shipping address:save']";
        WebElement elm = WebUI.waitElement(to, 10);
        WebUI.scrollToCenter(elm);
        WebUI.click(to);
        WebUI.waitForNotDisplayed("form.address-form", 5);
        ProfileSetting.loadAllAddresses();
        logger.info("Shipping address edited successfully at index " + index);
    }

    public static void editBillingAddress(Map<String, String> data, int index) throws Exception {
        WebUI.waitElement("button.card__edit", 5);

        List<WebElement> editButtons = WebUI.driver.findElements(
                By.cssSelector("li.address__card button[data-an-la='address:edit']"));

        if (editButtons.isEmpty() || index >= editButtons.size()) {
            throw new NoSuchElementException("Edit button at index " + index + " not found");
        }

        editButtons.get(index).click();
        WebUI.waitElement("form.address-form", 5);
        WebUI.delay(2);
        CustomerAddress.autoFill(data, true);

        String to = "[data-an-la='address:edit a billing address:save']";
        WebElement elm = WebUI.waitElement(to, 10);
        WebUI.scrollToCenter(elm);
        WebUI.click(to);
        WebUI.waitForNotDisplayed("form.address-form", 5);
        ProfileSetting.loadAllAddresses();
        logger.info("Shipping address edited successfully at index " + index);
    }

    public static void removeShippingAddress(int index) throws Exception {
        WebUI.waitElement("button[data-an-la='address:delete']", 5);
        List<WebElement> removeButtons = WebUI.driver.findElements(
                By.cssSelector("button[data-an-la='address:delete']"));

        if (removeButtons.isEmpty() || index >= removeButtons.size()) {
            throw new NoSuchElementException("Remove button at index " + index + "not found");
        }
        removeButtons.get(index).click();
        WebUI.delay(5);
        WebUI.click("button[data-an-la='delete address:delete']");
        ProfileSetting.loadAllAddresses();
        logger.info("Shipping Address is removed sucessfully at index " + index);

    }

    public static void removeBillingAddress(int index) throws Exception {
        WebUI.waitElement("button[data-an-la='address:delete']", 5);
        List<WebElement> removeButtons = WebUI.driver.findElements(
                By.cssSelector("button[data-an-la='address:delete']"));

        if (removeButtons.isEmpty() || index >= removeButtons.size()) {
            throw new NoSuchElementException("Remove button at index " + index + "not found");
        }
        removeButtons.get(index).click();
        WebUI.delay(5);
        WebUI.click("button[data-an-la='delete address:delete']");
        ProfileSetting.loadAllAddresses();
        logger.info("Billing Address is removed sucessfully at index " + index);
    }

    public static void loadAllAddresses() {
        By viewMoreBtn = By.cssSelector("button[data-an-la='address:view more']");
        WebDriverWait wait = new WebDriverWait(WebUI.driver, Duration.ofSeconds(30));
        wait.ignoring(Exception.class)
                .until(driver -> {
                    List<WebElement> elements = driver.findElements(viewMoreBtn);
                    if (elements.isEmpty()) {
                        return true;
                    }
                    WebElement elm = elements.get(0);
                    if (!elm.isDisplayed()) {
                        return true;
                    }
                    WebUI.scrollToCenter(elm);
                    elm.click();
                    return false;
                });
    }

}
