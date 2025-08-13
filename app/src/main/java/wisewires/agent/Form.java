package wisewires.agent;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Form {
    static Logger logger = LoggerFactory.getLogger(Form.class);

    static String FIELD_SELECTOR = "input[name], input[formcontrolname], mat-select[formcontrolname], mat-form-field:has(mat-select:not([formcontrolname]), input.search__input)";

    static File getResourceFile(String path) {
        URL resource = Form.class.getResource(path);
        if (resource == null) {
            throw new IllegalArgumentException("File not found in resources");
        }
        return new File(resource.getFile());
    }

    static void parseFormattedDate(Map<String, String> data, String dateKey) {
        if (!data.containsKey(dateKey) || data.get(dateKey) == null || data.get(dateKey).trim().isEmpty()) {
            throw new IllegalArgumentException("Value for " + dateKey + " is empty");
        }
        List<Integer> values = Arrays.stream(data.get(dateKey).split("[\\s-/]"))
                .filter(str -> !str.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        if (values.size() < 3) {
            throw new IllegalArgumentException("Invalid date format: " + data.get(dateKey));
        }
        data.put(dateKey + "_Day", String.format("%02d", values.get(0)));
        data.put(dateKey + "_Month", String.format("%02d", values.get(1)));
        data.put(dateKey + "_Year", values.get(2).toString());
    }

    public static List<WebElement> getFields(String to, boolean requiredOnly) {
        WebElement elm = WebUI.waitElement(to, 5);
        assert elm != null : String.format("Form %s not displayed", to);
        return elm.findElements(By.cssSelector(FIELD_SELECTOR))
                .stream()
                .filter(e -> !requiredOnly || isRequired(e))
                .toList();
    }

    public static String getNameOrLabel(WebElement field) {
        return field.getDomAttribute("name") != null ? field.getDomAttribute("name")
                : field.getDomAttribute("formcontrolname") != null ? field.getDomAttribute("formcontrolname")
                        : field.findElement(By.cssSelector("mat-label")).getText();
    }

    public static boolean isRequired(WebElement field) {
        if (field.getDomAttribute("required") != null)
            return true;
        if (!field.findElements(By.cssSelector("input[required], .mat-mdc-form-field-required-marker")).isEmpty())
            return true;
        return false;
    }

    public static boolean checkEnable(WebElement field, String nameOrLabel) {
        return checkEnable(logger, field, nameOrLabel);
    }

    public static boolean checkEnable(Logger logger, WebElement field, String nameOrLabel) {
        if (!field.isDisplayed()) {
            logger.warn(String.format("Field %s is not displayed, skipping", nameOrLabel));
            return false;
        }
        if (isDisabled(field)) {
            logger.warn(String.format("Field %s is disabled, skipping", nameOrLabel));
            return false;
        }
        return true;
    }

    public static boolean isDisabled(WebElement field) {
        if (field.getDomAttribute("disabled") != null)
            return true;
        if (field.getDomAttribute("readonly") != null)
            return true;
        String className = field.getDomAttribute("class");
        if (className.contains("mat-form-field-disabled"))
            return true;
        return false;
    }

    static void select(String to, List<String> withLabels, String value) throws Exception {
        WebElement elm = WebUI.waitElements(to, 5)
                .stream()
                .filter(e -> {
                    if (e.getDomAttribute("name") != null || e.getDomAttribute("formcontrolname") != null)
                        return true;
                    if (e.getTagName().equals("mat-form-field")) {
                        if (withLabels == null)
                            return true;
                        String label = e.findElement(By.cssSelector("mat-label")).getText();
                        return withLabels.stream().anyMatch(l -> label.startsWith(l));
                    }
                    return false;
                })
                .findFirst()
                .orElse(null);
        select(elm, value);
    }

    public static void select(WebElement elm, String value) throws Exception {
        if (elm.getTagName().equals("mat-form-field")) {
            select(elm.findElement(By.cssSelector("mat-select")), value);
            return;
        } else if (elm.getTagName().equals("mat-select")) {
            WebUI.scrollToCenter(elm);
            Thread.sleep(500);
            if (!elm.getDomAttribute("aria-expanded").equals("true")) {
                elm.click();
                Thread.sleep(500);
            }
            String id = elm.getDomAttribute("id");
            String xpath = "//*[@id='%s-panel']//mat-option[.//*[normalize-space(text())='%s']]".formatted(id, value);
            WebElement opt = WebUI.waitElement(elm, By.xpath(xpath), 5);
            WebUI.scrollToCenter(opt);
            Thread.sleep(200);
            opt.click();
            return;
        } else if (elm.getTagName().equals("select")) {
            Select dropdown = new Select(elm);
            dropdown.selectByVisibleText(value);
            return;
        }
        throw new Exception("Unsupported web element");
    }

    static boolean isSearchField(WebElement elm) {
        return !elm.findElements(By.xpath("./ancestor::mat-form-field//*[@svgicon='icon-regular-search']")).isEmpty();
    }

    static boolean isChecked(WebElement elm) {
        return elm.isSelected() ||
                (elm.getDomAttribute("aria-selected") != null
                        && WebUI.getDomAttribute(elm, "aria-selected").equals("true"))
                ||
                WebUI.getDomAttribute(elm, "class").contains("--active") ||
                !elm.findElements(By.cssSelector("input:checked")).isEmpty();
    }

    static void check(WebElement elm) {
        WebUI.scrollToCenter(elm);
        WebUI.delay(1);
        if (isChecked(elm)) {
            logger.warn("Already checked");
        } else {
            elm.click();
            logger.info("Checked");
        }
    }

    static void uncheck(WebElement elm) {
        WebUI.scrollToCenter(elm);
        if (isChecked(elm)) {
            elm.click();
            logger.info("Un-checked");
        } else {
            logger.warn("Already un-checked");
        }
    }

    public static void setProcessed(WebElement elm) {
        WebUI.driver.executeScript("arguments[0].setAttribute('data-auto-processed', true)", elm);
    }

    public static boolean isProcessed(WebElement elm) {
        return elm.getDomAttribute("data-auto-processed") != null;
    }

    static void uploadFile(String selector, String resourcePath) throws Exception {
        try {
            WebElement elm = WebUI.driver.findElement(By.cssSelector(selector));
            uploadFile(elm, resourcePath);
        } catch (Exception e) {
            throw new Exception("Unable to upload file '%s' to element '%s'".formatted(resourcePath, selector), e);
        }
    }

    static void uploadFile(WebElement elm, String resourcePath) throws Exception {
        try {
            File file = getResourceFile(resourcePath);
            elm.sendKeys(file.getAbsolutePath());
        } catch (Exception e) {
            throw new Exception("Unable to upload file '%s' to element'".formatted(resourcePath), e);
        }
    }
}
