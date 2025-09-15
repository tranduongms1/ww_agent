package wisewires.agent;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerInfo {
    static Logger logger = LoggerFactory.getLogger(CustomerInfo.class);

    public static void autoFill(Map<String, String> data, boolean requiredOnly) throws Exception {
        try {
            if (data.get("dateOfBirth") != null) {
                Form.parseFormattedDate(data, "dateOfBirth");
            }
            String to = "app-customer-info-v2 form";
            List<WebElement> fields = Form.getFields(to, true);
            for (WebElement field : fields) {
                String nameOrLabel = Form.getNameOrLabel(field);
                if (!Form.checkEnable(logger, field, nameOrLabel)) {
                    continue;
                }
                fillField(logger, field, nameOrLabel, data);
            }
            logger.info("Customer info form filled");
        } catch (Exception e) {
            throw new Exception("Unable to fill customer info", e);
        }
    }

    static void fillField(Logger logger, WebElement field, String nameOrLabel, Map<String, String> data)
            throws Exception {
        WebUI.scrollToCenter(field);
        Thread.sleep(500);
        switch (nameOrLabel) {
            case
                    "email",
                    "companyEmail":
                field.clear();
                field.sendKeys(data.get("email") + Keys.ENTER);
                break;

            case
                    "Title",
                    "Titre",
                    "Anrede":
                Form.select(field, data.get("title"));
                break;

            case
                    "firstName",
                    "purchaserFirstName":
                field.clear();
                field.sendKeys(data.get("firstName"));
                break;

            case
                    "lastName",
                    "purchaserLastName":
                field.clear();
                field.sendKeys(data.get("lastName"));
                break;

            case "fullName":
                field.clear();
                field.sendKeys(data.get("fullName"));
                break;

            case
                    "Préfixe",
                    "Code postal",
                    "Syntymäaika",
                    "Præfiks",
                    "Prefix",
                    "Prefiks",
                    "Landcode",
                    "電話區號",
                    "Area Code",
                    "Prefijo",
                    "Prefisso",
                    "Indicativo",
                    "Fødselsdato",
                    "Ländervorwahl",
                    "Numer kierunkowy",
                    "Κωδικός κλήσης χώρας",
                    "Tel. předvolba",
                    "Tel. predvoľba":
                logger.info("Filling '%s (phoneCode)'\n".formatted(nameOrLabel));
                if (data.get("phoneCode") != null) {
                    Form.select(field, data.get("phoneCode"));
                }
                break;
            case
                    "phone",
                    "telephone",
                    "purchaserNumber",
                    "phone2",
                    "phoneNumber2":
                field.clear();
                field.sendKeys(data.get("phoneNumber"));
                break;

            case
                    "Tipo de documento",
                    "Tipo de Documento",
                    "NIF/NIE/Pasaporte",
                    "ขอใบกำกับภาษีในนาม":
                logger.info("Filling '%s (documentType)'\n".formatted(nameOrLabel));
                Form.select(field, data.get("documentType"));
                break;

            case
                    "companyName",
                    "companyNameOpt",
                    "companyName2",
                    "companyNameInstitutional":
                field.clear();
                field.sendKeys(data.get("companyName"));
                break;

            case
                    "vatNumber",
                    "vatNumber2",
                    "companyTaxNumber":
                field.clear();
                field.sendKeys(data.get("vatNumber") + Keys.ENTER);
                break;

            case "personalTaxNumber":
                field.clear();
                field.sendKeys(data.get("personalTaxNumber") + Keys.ENTER);
                break;

            case "companyId":
                field.clear();
                field.sendKeys(data.get("companyId"));
                break;

            case "taxBranch":
                field.clear();
                Form.select(field, data.get("taxBranch"));
                break;

            case "companyPhoneNumber":
                field.clear();
                field.sendKeys(data.get("companyPhoneNumber"));
                break;

            case "commercializeTxt":
                field.clear();
                field.sendKeys(data.get("commercializeTxt"));
                break;

            case
                    "Tag",
                    "Dato",
                    "Dag",
                    "Päivä",
                    "Jour",
                    "Día",
                    "Dia":
                Form.select(field, data.get("dateOfBirth_Day"));
                break;

            case
                    "Monat",
                    "Måned",
                    "Månad",
                    "Kuukausi",
                    "Maand",
                    "Mois",
                    "Mes",
                    "Mês":
                Form.select(field, data.get("dateOfBirth_Month"));
                break;

            case
                    "Jahr",
                    "År",
                    "Vuosi",
                    "Jaar",
                    "Année",
                    "Año",
                    "Ano":
                Form.select(field, data.get("dateOfBirth_Year"));
                break;

            case "customerDOB":
                field.clear();
                field.sendKeys(data.get("customerDOB") + Keys.ENTER);
                break;

            case
                    "Country",
                    "Pays",
                    "Land",
                    "País",
                    "Paese",
                    "Země":
                if (data.get("country") != null) {
                    Form.select(field, data.get("country"));
                }
                break;

            case "tvLicenseNumber":
                field.clear();
                field.sendKeys(data.get("tvLicenseNumber"));
                break;

            case "TV License Validation Type":
                Form.select(field, data.get("tvLicenseType"));
                break;
            default:
                if (Form.isRequired(field)) {
                    throw new Exception("Field '%s' is not handled".formatted(nameOrLabel));
                } else {
                    logger.warn("Field '%s' is not handled\n".formatted(nameOrLabel));
                    return;
                }
        }
        logger.info("Field '%s' is filled".formatted(nameOrLabel));
    }
}
