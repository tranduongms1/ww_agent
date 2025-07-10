package wisewires.agent;

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
                if (!Form.checkEnable(field, nameOrLabel)) {
                    continue;
                }
                switch (nameOrLabel) {
                    case "email":
                    case
                            "Country",
                            "Pays",
                            "Land",
                            "País",
                            "Paese",
                            "Země":
                    case
                            "firstName",
                            "purchaserFirstName",
                            "lastName",
                            "purchaserLastName",
                            "fullName":
                    case
                            "Préfixe",
                            "Code postal",
                            "Syntymäaika",
                            "Præfiks",
                            "Prefix",
                            "Prefiks",
                            "Ländervorwahl",
                            "Landcode",
                            "電話區號",
                            "Area Code",
                            "Prefijo",
                            "Prefisso",
                            "Indicativo",
                            "Fødselsdato",
                            "Numer kierunkowy",
                            "Κωδικός κλήσης χώρας",
                            "Tel. předvolba",
                            "Tel. predvoľba":
                    case
                            "phone",
                            "telephone",
                            "purchaserNumber",
                            "phone2",
                            "phoneNumber2":
                    case
                            "Tipo de documento",
                            "NIF/NIE/Pasaporte",
                            "ขอใบกำกับภาษีในนาม":
                    case
                            "companyName",
                            "companyNameOpt",
                            "companyName2":
                    case
                            "vatNumber",
                            "vatNumber2",
                            "companyTaxNumber":
                    case "companyId":
                    case "commercializeTxt":
                    case
                            "Title",
                            "Titre",
                            "Anrede":
                        CustomerInfo.fillField(field, nameOrLabel, data);
                        break;

                    case "fiscalCode":
                        field.clear();
                        field.sendKeys(data.get("fiscalCode"));
                        break;

                    default:
                        CustomerAddress.fillField(field, nameOrLabel, data);
                }
            }
            logger.info("Billing address form filled");
        } catch (Exception e) {
            throw new Exception("Unable to fill billing address", e);
        }
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
                WebUI.delay(2);
                Form.check(elmTaxInvoice);
                WebUI.delay(2);
            }
            String to = ".mdc-checkbox:has([name='sameAsShipping'])";
            WebElement elm = WebUI.waitElement(to, 5);
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
