package wisewires.agent;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerInfo {
    static Logger logger = LoggerFactory.getLogger(CustomerInfo.class);

    static public Map<String, String> FIELD_NAME = Map.ofEntries(
            Map.entry("email", "email"),
            Map.entry("companyEmail", "email"),

            Map.entry("Title", "title"),
            Map.entry("Titre", "title"),
            Map.entry("Anrede", "title"),

            Map.entry("firstName", "firstName"),
            Map.entry("purchaserFirstName", "firstName"),

            Map.entry("lastName", "lastName"),
            Map.entry("purchaserLastName", "lastName"),

            Map.entry("fullName", "fullName"),

            Map.entry("Préfixe", "phoneCode"),
            Map.entry("Code postal", "phoneCode"),
            Map.entry("Syntymäaika", "phoneCode"),
            Map.entry("Præfiks", "phoneCode"),
            Map.entry("Prefix", "phoneCode"),
            Map.entry("Prefiks", "phoneCode"),
            Map.entry("Landcode", "phoneCode"),
            Map.entry("電話區號", "phoneCode"),
            Map.entry("Area Code", "phoneCode"),
            Map.entry("Prefijo", "phoneCode"),
            Map.entry("Prefisso", "phoneCode"),
            Map.entry("Indicativo", "phoneCode"),
            Map.entry("Fødselsdato", "phoneCode"),
            Map.entry("Ländervorwahl", "phoneCode"),
            Map.entry("Numer kierunkowy", "phoneCode"),
            Map.entry("Κωδικός κλήσης χώρας", "phoneCode"),
            Map.entry("Tel. předvolba", "phoneCode"),
            Map.entry("Tel. predvoľba", "phoneCode"),

            Map.entry("phone", "phoneNumber"),
            Map.entry("telephone", "phoneNumber"),
            Map.entry("purchaserNumber", "phoneNumber"),
            Map.entry("phone2", "phoneNumber"),
            Map.entry("phoneNumber2", "phoneNumber"),

            Map.entry("Tipo de documento", "documentType"),
            Map.entry("Tipo de Documento", "documentType"),
            Map.entry("NIF/NIE/Pasaporte", "documentType"),
            Map.entry("ขอใบกำกับภาษีในนาม", "documentType"),

            Map.entry("companyName", "companyName"),
            Map.entry("companyNameOpt", "companyName"),
            Map.entry("companyName2", "companyName"),
            Map.entry("companyNameInstitutional", "companyName"),

            Map.entry("companyId", "companyId"),

            Map.entry("Company Type", "companyType"),
            Map.entry("Company Size", "companySize"),

            Map.entry("companyPhoneNumber", "companyPhoneNumber"),

            Map.entry("vatNumber", "vatNumber"),
            Map.entry("vatNumber2", "vatNumber"),
            Map.entry("companyTaxNumber", "vatNumber"),

            Map.entry("personalTaxNumber", "personalTaxNumber"),
            Map.entry("fiscalCode", "fiscalCode"),
            Map.entry("taxBranch", "taxBranch"),

            Map.entry("commercializeTxt", "commercializeTxt"),

            Map.entry("Tag", "dayOfBirth"),
            Map.entry("Dato", "dayOfBirth"),
            Map.entry("Dag", "dayOfBirth"),
            Map.entry("Päivä", "dayOfBirth"),
            Map.entry("Jour", "dayOfBirth"),
            Map.entry("Día", "dayOfBirth"),
            Map.entry("Dia", "dayOfBirth"),
            Map.entry("Monat", "monthOfBirth"),
            Map.entry("Måned", "monthOfBirth"),
            Map.entry("Månad", "monthOfBirth"),
            Map.entry("Kuukausi", "monthOfBirth"),
            Map.entry("Maand", "monthOfBirth"),
            Map.entry("Mois", "monthOfBirth"),
            Map.entry("Mes", "monthOfBirth"),
            Map.entry("Mês", "monthOfBirth"),

            Map.entry("Jahr", "yearOfBirth"),
            Map.entry("År", "yearOfBirth"),
            Map.entry("Vuosi", "yearOfBirth"),
            Map.entry("Jaar", "yearOfBirth"),
            Map.entry("Année", "yearOfBirth"),
            Map.entry("Año", "yearOfBirth"),
            Map.entry("Ano", "yearOfBirth"),

            Map.entry("customerDOB", "dateOfBirth"),

            Map.entry("Country", "country"),
            Map.entry("Pays", "country"),
            Map.entry("Land", "country"),
            Map.entry("País", "country"),
            Map.entry("Paese", "country"),
            Map.entry("Země", "country"),

            Map.entry("tvLicenseNumber", "tvLicenseNumber"),

            Map.entry("TV License Validation Type", "tvLicenseType"));

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

    static String getFieldName(String nameOrLabel) {
        if (FIELD_NAME.containsKey(nameOrLabel)) {
            return FIELD_NAME.get(nameOrLabel);
        }
        return "";
    }

    static void enterField(String name, String value) throws Exception {
        enterField(logger, name, value);
    }

    static void enterField(Logger logger, String name, String value) throws Exception {
        List<WebElement> fields = Form.getFields("app-customer-info-v2 form", false);
        for (WebElement field : fields) {
            String nameOrLabel = Form.getNameOrLabel(field);
            if (nameOrLabel.equals(name) || getFieldName(nameOrLabel).equals(name)) {
                WebUI.scrollToCenter(field);
                field.clear();
                field.sendKeys(value);
                return;
            }
        }
        throw new Exception("Unable to locale field name '%s'".formatted(name));
    }

    static void fillField(Logger logger, WebElement field, String nameOrLabel, Map<String, String> data)
            throws Exception {
        WebUI.scrollToCenter(field);
        Thread.sleep(500);
        String name = FIELD_NAME.get(nameOrLabel);
        switch (name) {
            case "email":
                field.clear();
                field.sendKeys(data.get("email") + Keys.ENTER);
                break;

            case "title":
                Form.select(field, data.get("title"));
                break;

            case "firstName":
                field.clear();
                field.sendKeys(data.get("firstName"));
                break;

            case "lastName":
                field.clear();
                field.sendKeys(data.get("lastName"));
                break;

            case "fullName":
                field.clear();
                field.sendKeys(data.get("fullName"));
                break;

            case "phoneCode":
                logger.info("Filling '%s (phoneCode)'\n".formatted(nameOrLabel));
                if (data.get("phoneCode") != null) {
                    Form.select(field, data.get("phoneCode"));
                }
                break;

            case "phoneNumber":
                field.clear();
                field.sendKeys(data.get("phoneNumber"));
                break;

            case "documentType":
                logger.info("Filling '%s (documentType)'\n".formatted(nameOrLabel));
                Form.select(field, data.get("documentType"));
                break;

            case "companyName":
                field.clear();
                field.sendKeys(data.get("companyName"));
                break;

            case "vatNumber":
                field.clear();
                field.sendKeys(data.get("vatNumber") + Keys.ENTER);
                break;

            case "personalTaxNumber":
                field.clear();
                field.sendKeys(data.get("personalTaxNumber") + Keys.ENTER);
                Thread.sleep(1000);
                if (data.get("personalTaxNumber").equals("10339116396")) {
                    WebUI.waitElement("[name='companyName']", 5);
                }
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

            case "dayOfBirth":
                Form.select(field, data.get("dateOfBirth_Day"));
                break;

            case "monthOfBirth":
                Form.select(field, data.get("dateOfBirth_Month"));
                break;

            case "yearOfBirth":
                Form.select(field, data.get("dateOfBirth_Year"));
                break;

            case "dateOfBirth":
                field.clear();
                field.sendKeys(data.get("customerDOB") + Keys.ENTER);
                break;

            case "country":
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
