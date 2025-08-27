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

    public static void autoFill(Map<String, String> data, boolean requiredOnly) throws Exception {
        try {
            String to = "app-customer-address-v2, form.address-form";
            for (WebElement field : Form.getFields(to, requiredOnly)) {
                String nameOrLabel = Form.getNameOrLabel(field);
                if (!Form.checkEnable(logger, field, nameOrLabel)) {
                    continue;
                }
                switch (nameOrLabel) {
                    case
                            "firstName",
                            "lastName",
                            "email":
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
                    case "phone":
                    case
                            "companyName",
                            "companyNameOpt":
                    case "vatNumber":
                        CustomerInfo.fillField(logger, field, nameOrLabel, data);
                        break;

                    default:
                        fillField(logger, field, nameOrLabel, data);
                }
            }
            logger.info("Customer address form filled");
        } catch (Exception e) {
            throw new Exception("Unable to fill customer address", e);
        }
    }

    static void fillField(Logger logger, WebElement field, String nameOrLabel, Map<String, String> data)
            throws Exception {
        WebUI.scrollToCenter(field);
        switch (nameOrLabel) {
            case
                    "Country",
                    "Pays",
                    "Land",
                    "País",
                    "Paese",
                    "Šalis",
                    "Valsts",
                    "Riik",
                    "Kraj",
                    "Krajina",
                    "Χώρα",
                    "Země":
                if (data.get("country") != null) {
                    Form.select(field, data.get("country"));
                }
                break;

            case "searchAddress", "postalCode":
                field.clear();
                field.sendKeys(data.get(nameOrLabel));
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

            case "Postcode":
                Form.select(field, data.get("postalCode"));
                break;

            case
                    "apartment",
                    "streetNumber",
                    "landmark":
                field.clear();
                field.sendKeys(data.get(nameOrLabel));
                break;

            case "line1":
                field.clear();
                field.sendKeys(data.get(nameOrLabel));
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

            case "Nombre Calle":
                Form.select(field, data.get("line2Type"));
                break;

            case
                    "line2",
                    "neighborhood",
                    "line2_b",
                    "line2_c",
                    "line2_d":
                field.clear();
                field.sendKeys(data.get(nameOrLabel));
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

            case
                    "Departamento":
                WebUI.scrollToCenter(field);
                Form.select(field, data.get("departamento"));
                WebUI.delay(2);
                break;

            case "Tipo de propiedad":
                WebUI.scrollToCenter(field);
                Form.select(field, data.get("tipoDePropiedad"));
                break;

            case
                    "regionIso",
                    "State",
                    "County",
                    "Región",
                    "Región / Provincia",
                    "State/Territory",
                    "Region",
                    "Province",
                    "Provincia",
                    "Provinsi",
                    "Région",
                    "State / Region",
                    "المنطقة/ الولاية",
                    "Etat / Province",
                    "Region/Province",
                    "จังหวัด",
                    "Tỉnh/Thành phố",
                    "المنطقة/المحافظة",
                    "Region/City",
                    "المنطقة/الحي",
                    "Νομός",
                    "Județ",
                    "Окръг",
                    "Облыс",
                    "Область",
                    "Νόμος":
                if (field.getTagName().equals("input")) {
                    field.clear();
                    field.sendKeys(data.get("adminLevel1"));
                } else if (Form.isSearchField(field)) {
                    selectFirstSearchResult(field);
                } else {
                    Form.select(field, data.get("adminLevel1"));
                }
                WebUI.delay(2);
                break;

            case
                    "Suburb",
                    "suburb",
                    "Comuna",
                    "Kota",
                    "City",
                    "城市",
                    "縣市",
                    "地區",
                    "Ville",
                    "עיר",
                    "المحافظة",
                    "پارێزگا",
                    "المدينة",
                    "مدينة",
                    "İl",
                    "Governorate",
                    "Ciudad",
                    "المحافظه",
                    "יישוב",
                    "town",
                    "Πόλη",
                    "Localitate",
                    "Град",
                    "ГРАД",
                    "Қала",
                    "Город":
                if (field.getTagName().equals("input")) {
                    field.clear();
                    field.sendKeys(data.get("adminLevel2"));
                    if (Form.isSearchField(field)) {
                        if (WebUI.isOneOfSites("ch", "ch_fr")) {
                            WebUI.delay(1);
                        } else {
                            selectFirstSearchResult(field);
                        }
                    }
                } else {
                    Form.select(field, data.get("adminLevel2"));
                }
                WebUI.delay(2);
                break;

            case
                    "Kecamatan",
                    "區域",
                    "Distrito",
                    "District",
                    "district",
                    "เขต/อำเภอ",
                    "鄉鎮市區",
                    "Area",
                    "المنطقة",
                    "ناوچە",
                    "District / Area",
                    "المنطقة/المنطقة",
                    "İlçe",
                    "Colonia",
                    "Huyện",
                    "Област":
                if (WebUI.isOneOfSites("IQ_AR", "IQ_KU")) {
                    WebUI.selectFirstOpt(field);
                    break;
                }
                if (field.getTagName().equals("input")) {
                    field.clear();
                    field.sendKeys(data.get("adminLevel3"));
                } else {
                    Form.select(field, data.get("adminLevel3"));
                }
                WebUI.delay(2);
                break;

            case
                    "Kelurahan",
                    "แขวง/ตำบล",
                    "Barangay",
                    "רחוב",
                    "Mahalle",
                    "Phường":
                if (field.getTagName().equals("input")) {
                    field.clear();
                    field.sendKeys(data.get("adminLevel4"));
                } else {
                    Form.select(field, data.get("adminLevel4"));
                }
                break;

            case
                    "phone":
                field.clear();
                field.sendKeys(data.get("phoneNumber"));
                break;
            case "nameOnRinger":
                field.clear();
                field.sendKeys(data.get("nameOnRinger"));
                break;
            case "floor":
                field.clear();
                field.sendKeys(data.get("floor"));
                break;
            case "buildingNumber":
                field.clear();
                field.sendKeys(data.get("buildingNumber"));
                break;
            case "Company Type":
                WebUI.scrollToCenter(field);
                Form.select(field, data.get("companyType"));
                break;
            case "Company Size":
                WebUI.scrollToCenter(field);
                Form.select(field, data.get("companySize"));
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
