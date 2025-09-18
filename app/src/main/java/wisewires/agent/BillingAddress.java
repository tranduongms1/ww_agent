package wisewires.agent;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BillingAddress {
    static Logger logger = LoggerFactory.getLogger(BillingAddress.class);

    public static void autoFill(Map<String, String> data, boolean requiredOnly) throws Exception {
        try {
            String to = "app-billing-address-v2";
            for (WebElement field : Form.getFields(to, requiredOnly)) {
                String nameOrLabel = Form.getNameOrLabel(field);
                if (!Form.checkEnable(logger, field, nameOrLabel)) {
                    continue;
                }
                CustomerAddress.fillField(logger, field, nameOrLabel, data);
            }
            logger.info("Billing address form filled");
        } catch (Exception e) {
            throw new Exception("Unable to fill billing address", e);
        }
    }

    static void enterField(String name, String value) throws Exception {
        List<WebElement> fields = Form.getFields("app-billing-address-v2", false);
        for (WebElement field : fields) {
            String nameOrLabel = Form.getNameOrLabel(field);
            if (nameOrLabel.equals(name) || CustomerAddress.getFieldName(nameOrLabel).equals(name)) {
                WebUI.scrollToCenter(field);
                field.clear();
                field.sendKeys(value);
                return;
            }
        }
        throw new Exception("Unable to locale field name '%s'".formatted(name));
    }

    public static void checkSameAsShipping() throws Exception {
        try {
            if (WebUI.isOneOfSites("th", "it")) {
                WebUI.delay(5);
                String taxInvoice = ".mdc-checkbox:has([name='isUserWantTaxInvoice']), .mdc-checkbox:has([name='vatCode'])";
                WebElement elmTaxInvoice = WebUI.findElement(taxInvoice);
                WebUI.scrollToCenter(elmTaxInvoice);
                WebUI.delay(2);
                Form.check(elmTaxInvoice);
                WebUI.delay(2);
            }
            String to = ".mdc-checkbox:has([name='sameAsShipping'])";
            if (!WebUI.isOneOfSites("pe", "jp")) {
                WebElement elm = WebUI.findElement(to);
                Form.check(elm);
            }
            logger.info("Checked billing address same as shipping");
        } catch (Exception e) {
            throw new Exception("Unable to check billing address same as shipping address", e);
        }
    }

    public static void uncheckSameAsShipping() throws Exception {
        try {
            if (WebUI.isOneOfSites("th", "it")) {
                WebUI.delay(5);
                String taxInvoice = ".mdc-checkbox:has([name='isUserWantTaxInvoice']), .mdc-checkbox:has([name='vatCode'])";
                WebElement elmTaxInvoice = WebUI.findElement(taxInvoice);
                WebUI.scrollToCenter(elmTaxInvoice);
                WebUI.delay(1);
                Form.check(elmTaxInvoice);
                WebUI.delay(2);
            }
            String to = ".mdc-checkbox:has([name='sameAsShipping'])";
            WebElement elm = WebUI.waitElement(to, 5);
            WebUI.scrollToCenter(elm);
            WebUI.delay(1);
            Form.uncheck(elm);
            logger.info("Un-checked billing address same as shipping");
        } catch (Exception e) {
            throw new Exception("Unable to un-check billing address same as shipping address");
        }
    }

    static void checkSaveAddress() throws Exception {
        try {
            String to = "app-billing-address-v2 mat-checkbox:has(input[name='saveInAddressBook']) label";
            WebElement elm = WebUI.findElement(to);
            WebUI.scrollToCenter(elm);
            Form.check(elm);
            logger.info("checked save Billing address");
        } catch (Exception e) {
            throw new Exception("Unable to check save billing address", e);
        }
    }
}
