package wisewires.agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomerAddress {
    static Logger logger = LoggerFactory.getLogger(CustomerAddress.class);

    static public Map<String, String> FIELD_NAME = Map.ofEntries(
            Map.entry("Country", "country"),
            Map.entry("Pays", "country"),
            Map.entry("Land", "country"),
            Map.entry("País", "country"),
            Map.entry("Paese", "country"),
            Map.entry("Šalis", "country"),
            Map.entry("Valsts", "country"),
            Map.entry("Riik", "country"),
            Map.entry("Kraj", "country"),
            Map.entry("Krajina", "country"),
            Map.entry("Χώρα", "country"),
            Map.entry("Země", "country"),

            Map.entry("searchAddress", "searchAddress"),
            Map.entry("postalCode", "postalCode"),
            Map.entry("Postcode", "postalCode"),

            Map.entry("nameOnRinger", "nameOnRinger"),
            Map.entry("floor", "floor"),
            Map.entry("apartment", "apartment"),
            Map.entry("buildingNumber", "buildingNumber"),
            Map.entry("streetNumber", "streetNumber"),
            Map.entry("landmark", "landmark"),

            Map.entry("line1", "line1"),

            Map.entry("Nombre Calle", "line2Type"),
            Map.entry("line2", "line2"),
            Map.entry("neighborhood", "neighborhood"),
            Map.entry("line2_b", "line2_b"),
            Map.entry("line2_c", "line2_c"),
            Map.entry("line2_d", "line2_d"),

            Map.entry("Tipo de propiedad", "tipoDePropiedad"),
            Map.entry("Departamento", "departamento"),
            Map.entry("fiscalCode", "fiscalCode"),

            Map.entry("regionIso", "adminLevel1"),
            Map.entry("State", "adminLevel1"),
            Map.entry("County", "adminLevel1"),
            Map.entry("Región", "adminLevel1"),
            Map.entry("Región / Provincia", "adminLevel1"),
            Map.entry("State/Territory", "adminLevel1"),
            Map.entry("Region", "adminLevel1"),
            Map.entry("Province", "adminLevel1"),
            Map.entry("Provincia", "adminLevel1"),
            Map.entry("Provinsi", "adminLevel1"),
            Map.entry("Région", "adminLevel1"),
            Map.entry("State / Region", "adminLevel1"),
            Map.entry("المنطقة/ الولاية", "adminLevel1"),
            Map.entry("Etat / Province", "adminLevel1"),
            Map.entry("Region/Province", "adminLevel1"),
            Map.entry("จังหวัด", "adminLevel1"),
            Map.entry("Tỉnh/Thành phố", "adminLevel1"),
            Map.entry("المنطقة/المحافظة", "adminLevel1"),
            Map.entry("Region/City", "adminLevel1"),
            Map.entry("المنطقة/الحي", "adminLevel1"),
            Map.entry("Νομός", "adminLevel1"),
            Map.entry("Județ", "adminLevel1"),
            Map.entry("Окръг", "adminLevel1"),
            Map.entry("Облыс", "adminLevel1"),
            Map.entry("Область", "adminLevel1"),
            Map.entry("Νόμος", "adminLevel1"),

            Map.entry("Suburb", "adminLevel2"),
            Map.entry("suburb", "adminLevel2"),
            Map.entry("Comuna", "adminLevel2"),
            Map.entry("Kota", "adminLevel2"),
            Map.entry("City", "adminLevel2"),
            Map.entry("城市", "adminLevel2"),
            Map.entry("縣市", "adminLevel2"),
            Map.entry("地區", "adminLevel2"),
            Map.entry("Ville", "adminLevel2"),
            Map.entry("עיר", "adminLevel2"),
            Map.entry("المحافظة", "adminLevel2"),
            Map.entry("پارێزگا", "adminLevel2"),
            Map.entry("المدينة", "adminLevel2"),
            Map.entry("مدينة", "adminLevel2"),
            Map.entry("İl", "adminLevel2"),
            Map.entry("Governorate", "adminLevel2"),
            Map.entry("Ciudad", "adminLevel2"),
            Map.entry("المحافظه", "adminLevel2"),
            Map.entry("יישוב", "adminLevel2"),
            Map.entry("town", "adminLevel2"),
            Map.entry("Πόλη", "adminLevel2"),
            Map.entry("Localitate", "adminLevel2"),
            Map.entry("Град", "adminLevel2"),
            Map.entry("ГРАД", "adminLevel2"),
            Map.entry("Қала", "adminLevel2"),
            Map.entry("Город", "adminLevel2"),

            Map.entry("Kecamatan", "adminLevel3"),
            Map.entry("區域", "adminLevel3"),
            Map.entry("Distrito", "adminLevel3"),
            Map.entry("District", "adminLevel3"),
            Map.entry("district", "adminLevel3"),
            Map.entry("เขต/อำเภอ", "adminLevel3"),
            Map.entry("鄉鎮市區", "adminLevel3"),
            Map.entry("Area", "adminLevel3"),
            Map.entry("المنطقة", "adminLevel3"),
            Map.entry("ناوچە", "adminLevel3"),
            Map.entry("District / Area", "adminLevel3"),
            Map.entry("المنطقة/المنطقة", "adminLevel3"),
            Map.entry("İlçe", "adminLevel3"),
            Map.entry("Colonia", "adminLevel3"),
            Map.entry("Huyện", "adminLevel3"),
            Map.entry("Област", "adminLevel3"),

            Map.entry("Kelurahan", "adminLevel4"),
            Map.entry("แขวง/ตำบล", "adminLevel4"),
            Map.entry("Barangay", "adminLevel4"),
            Map.entry("רחוב", "adminLevel4"),
            Map.entry("Mahalle", "adminLevel4"),
            Map.entry("Phường", "adminLevel4"));

    public static void autoFill(Map<String, String> data, boolean requiredOnly) throws Exception {
        try {
            String to = "app-customer-address-v2, form.address-form";
            for (WebElement field : Form.getFields(to, requiredOnly)) {
                String nameOrLabel = Form.getNameOrLabel(field);
                if (!Form.checkEnable(logger, field, nameOrLabel)) {
                    continue;
                }
                fillField(logger, field, nameOrLabel, data);
            }
            logger.info("Customer address form filled");
        } catch (Exception e) {
            throw new Exception("Unable to fill customer address", e);
        }
    }

    static String getFieldName(String nameOrLabel) {
        if (FIELD_NAME.containsKey(nameOrLabel)) {
            return FIELD_NAME.get(nameOrLabel);
        }
        if (CustomerInfo.FIELD_NAME.containsKey(nameOrLabel)) {
            return CustomerInfo.FIELD_NAME.get(nameOrLabel);
        }
        return "";
    }

    static void enterField(String name, String value) throws Exception {
        enterField(logger, name, value);
    }

    static void enterField(Logger logger, String name, String value) throws Exception {
        List<WebElement> fields = Form.getFields("app-customer-address-v2, form.address-form", false);
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
        String name = FIELD_NAME.get(nameOrLabel);
        switch (name) {
            case "country":
                if (data.get(name) != null) {
                    Form.select(field, data.get(name));
                }
                break;

            case "searchAddress", "postalCode":
                if (nameOrLabel.equals("Postcode")) {
                    Form.select(field, data.get("postalCode"));
                    break;
                }
                field.clear();
                field.sendKeys(data.get(name));
                if (!WebUI.isOneOfSites("ch", "ch_fr")) {
                    field.sendKeys(Keys.TAB);
                    Thread.sleep(500);
                }
                if (Form.isSearchField(field)) {
                    if (WebUI.isOneOfSites("ch", "ch_fr", "fr", "jp")) {
                        WebUI.delay(3);
                    } else {
                        selectFirstSearchResult(field);
                    }
                } else {
                    WebUI.delay(1);
                }
                break;

            case
                    "nameOnRinger",
                    "floor",
                    "buildingNumber",
                    "apartment",
                    "streetNumber",
                    "landmark":
                field.clear();
                field.sendKeys(data.get(name));
                break;

            case "line1":
                field.clear();
                field.sendKeys(data.get(name));
                if (!WebUI.isOneOfSites("ch", "ch_fr")) {
                    field.sendKeys(Keys.TAB);
                }
                if (Form.isSearchField(field)) {
                    if (WebUI.isOneOfSites("ch", "ch_fr")) {
                        WebUI.delay(1);
                    } else {
                        selectFirstSearchResult(field);
                    }
                }
                break;

            case "line2Type":
                Form.select(field, data.get(name));
                break;

            case
                    "line2",
                    "neighborhood",
                    "line2_b",
                    "line2_c",
                    "line2_d":
                field.clear();
                field.sendKeys(data.get(name));
                if (!WebUI.isOneOfSites("ch", "ch_fr")) {
                    field.sendKeys(Keys.TAB);
                }
                if (Form.isSearchField(field)) {
                    if (WebUI.isOneOfSites("ch", "ch_fr")) {
                        WebUI.delay(1);
                    } else {
                        selectFirstSearchResult(field);
                    }
                }
                break;

            case "departamento":
                WebUI.scrollToCenter(field);
                Form.select(field, data.get(name));
                WebUI.delay(2);
                break;

            case "tipoDePropiedad":
                WebUI.scrollToCenter(field);
                Form.select(field, data.get(name));
                break;

            case "adminLevel1":
                if (field.getTagName().equals("input")) {
                    field.clear();
                    field.sendKeys(data.get(name));
                } else if (Form.isSearchField(field)) {
                    selectFirstSearchResult(field);
                } else {
                    Form.select(field, data.get(name));
                }
                WebUI.delay(2);
                break;

            case "adminLevel2":
                if (field.getTagName().equals("input")) {
                    field.clear();
                    field.sendKeys(data.get(name));
                    if (Form.isSearchField(field)) {
                        if (WebUI.isOneOfSites("ch", "ch_fr")) {
                            WebUI.delay(1);
                        } else {
                            selectFirstSearchResult(field);
                        }
                    }
                } else {
                    Form.select(field, data.get(name));
                }
                WebUI.delay(2);
                break;

            case "adminLevel3":
                if (WebUI.isOneOfSites("IQ_AR", "IQ_KU")) {
                    WebUI.selectFirstOpt(field);
                    break;
                }
                if (field.getTagName().equals("input")) {
                    field.clear();
                    field.sendKeys(data.get(name));
                } else {
                    Form.select(field, data.get(name));
                }
                WebUI.delay(2);
                break;

            case "adminLevel4":
                if (field.getTagName().equals("input")) {
                    field.clear();
                    field.sendKeys(data.get(name));
                } else {
                    Form.select(field, data.get(name));
                }
                break;
            case "fiscalCode":
            field.clear();
            field.sendKeys(data.get(name));
            break;

            default:
                if (Form.isRequired(field)) {
                    throw new Exception("Field '%s' is not handled".formatted(nameOrLabel));
                } else {
                    logger.warn("Field '%s' is not handled\n".formatted(nameOrLabel));
                    return;
                }
        }
        logger.info("Field '%s' (%s) is filled".formatted(nameOrLabel, name));
    }

    static void selectFirstSearchResult(WebElement elm) {
        WebUI.scrollToCenter(elm);
        WebUI.delay(1);
        String selector = "./ancestor::dynamic-material-form-control//*[contains(@class,'dropWindow')]//mat-option";
        WebUI.wait(5).until(d -> {
            WebElement opt = WebUI.findElement(elm, By.xpath(selector));
            if (opt != null) {
                WebUI.scrollToCenter(opt);
                WebUI.delay(1);
                opt.click();
                return true;
            }
            return false;
        });
        WebUI.delay(2);
    }

    static void checkSaveAddress() throws Exception {
        try {
            WebUI.delay(2);
            String to = "app-customer-address-v2 mat-checkbox:has(input[name='saveInAddressBook']) label";
            WebElement elm = WebUI.findElement(to);
            WebUI.scrollToCenter(elm);
            Form.check(elm);
            logger.info("checked save Shipping address");
        } catch (Exception e) {
            throw new Exception("Unable to check save shipping address", e);
        }
    }

    static void selectB2BOption() throws Exception {
        try {
            String checkbox = """
                    .mdc-checkbox:has(input[name='company']:not(:checked)),
                    .mdc-checkbox:has(input[name='buyingCompany']:not(:checked)),
                    input[name="companyTaxNumber"],
                    [data-an-la="checkout:customer details:search cta"]
                    """;
            ;
            Map<String, String> data = new HashMap<>(Map.ofEntries(
                    Map.entry("companyTaxNumber", "39216892")));

            List<WebElement> elms = WebUI.driver.findElements(By.cssSelector(checkbox));
            for (WebElement elm : elms) {
                String name = WebUI.getDomAttribute(elm, "name");
                if (data.containsKey(name)) {
                    elm.clear();
                    elm.sendKeys(data.get(name));
                } else {
                    WebUI.scrollToCenter(elm);
                    WebUI.delay(1);
                    elm.click();
                }

            }
        } catch (Exception e) {
            throw new Exception("Unable to select B2C option", e);
        }
    }

    static void selectB2COption() throws Exception {
        try {
            String to = ".mdc-checkbox:has(input[name='buyingPerson']:not(:checked))";
            WebElement B2Bcheckbox = WebUI.findElement(to);
            WebUI.scrollToCenter(B2Bcheckbox);
            WebUI.delay(1);
            B2Bcheckbox.click();
        } catch (Exception e) {
            throw new Exception("Unable to select B2B option", e);
        }
    }

    public static void selectVATReversal() throws Exception {
        try {
            WebElement chk = WebUI.findElement(".mdc-checkbox:has(input[name='buyingCompany']:not(:checked))");
            WebUI.scrollToCenter(chk);
            WebUI.delay(1);
            chk.click();
            Map<String, String> data = new HashMap<>(Map.ofEntries(
                    Map.entry("vatNumber", "LV40103148504")));
            if (WebUI.isOneOfSites("EE")) {
                data.put("vatNumber", "EE101206172");
            } else if (WebUI.isOneOfSites("LT")) {
                data.put("vatNumber", "LT100006806712");
            }
            List<WebElement> elms = WebUI.driver.findElements(By.cssSelector(" input[name='vatNumber']"));
            for (WebElement elm : elms) {
                String name = WebUI.getDomAttribute(elm, "name");
                if (data.containsKey(name)) {
                    elm.clear();
                    elm.sendKeys(data.get(name));
                    elm.sendKeys(Keys.ENTER.toString());
                }
            }
        } catch (Exception e) {
            throw new Exception("Unable to select VAT reversal option", e);
        }
    }

    public static void selectNonVATReversal() throws Exception {
        try {
            WebElement chk1 = WebUI.findElement(".mdc-checkbox:has(input[name='buyingCompany']:not(:checked))");
            WebUI.scrollToCenter(chk1);
            WebUI.delay(1);
            chk1.click();
            WebElement chk2 = WebUI.findElement(".mdc-checkbox:has(input[name='vatPayer']:checked)");
            chk2.click();
            String to = """
                    input[name="companyName"],
                    input[name="landmark"],
                    input[name="companyRegNumber"]
                    """;
            Map<String, String> data = new HashMap<>(Map.ofEntries(
                    Map.entry("companyName", "WW"),
                    Map.entry("landmark", "HANOI"),
                    Map.entry("companyRegNumber", "123456789")));
            if (WebUI.isOneOfSites("EE")) {
                data.put("companyRegNumber", "12345678");
            }
            List<WebElement> elms = WebUI.findElements(to);
            for (WebElement elm : elms) {
                String name = WebUI.getDomAttribute(elm, "name");
                if (data.containsKey(name)) {
                    elm.clear();
                    elm.sendKeys(data.get(name));
                }
            }
        } catch (Exception e) {
            throw new Exception("Unable to select Non VAT reversal option", e);
        }
    }
}
