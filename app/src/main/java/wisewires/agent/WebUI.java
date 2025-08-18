package wisewires.agent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
interface WebElementSelector {
    void apply(List<WebElement> elms) throws Exception;
}

public abstract class WebUI {
    private static Logger logger = LoggerFactory.getLogger(WebUI.class);

    static String DISPLAY_NONE = "a=document.createElement('style');a.innerHTML='iframe.fpw-view, #spr-live-chat-app, #fw-player {display:none !important}';document.head.appendChild(a)";

    static ChromeDriver driver;

    public static void delay(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException ignore) {
        }
    }

    static void openBrowser(Context c, String url) throws Exception {
        if (url.contains("pre-qa")) {
            mustAEMReady(c);
        } else if (url.startsWith("https://stg") || url.startsWith("https://v2")) {
            mustGetCookie(c);
        }
        openBrowser(url);
        logger.info("Opened: " + url);
    }

    static void openBrowser(String url) {
        if (driver == null) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized", "--incognito",
                    "--user-agent=D2CEST-AUTO-70a4cf16 Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36.D2CEST-AUTO-70a4cf16");
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-geolocation");
            options.addArguments("--use-fake-ui-for-media-stream");
            driver = new ChromeDriver(options);
        }
        driver.get(url);
    }

    static void getCookie(Context c) {
        if (driver == null) {
            openBrowser(c.getCookieUrl());
        } else {
            driver.executeScript("window.open(arguments[0])", c.getCookieUrl());
            delay(2);
            switchToWindow(1);
            waitElement(".hubble-error-page h3", 10);
            driver.close();
            switchToWindow(0);
        }
        c.setCookieReady();
    }

    static void mustGetCookie(Context c) {
        if (!c.isCookieReady()) {
            getCookie(c);
        }
    }

    static void getPointing(Context c) throws Exception {
        String url = c.getPointingUrl();
        driver.get(url);
        delay(1);
        wait(30).withMessage("AEM login").until(d -> {
            WebElement elm = findElement("#login.coral-Form");
            if (elm != null) {
                fill("input[name='j_username']", "qauser01");
                fill("input[name='j_password']", "samsungqa");
                click("#submit-button");
            }
            return !driver.getCurrentUrl().contains("login.html");
        });
        driver.get(url);
        delay(1);
    }

    static void mustAEMReady(Context c) throws Exception {
        if (!c.isAEMReady()) {
            mustGetCookie(c);
            getPointing(c);
            c.setAEMReady();
        }
    }

    static void closeAllPopup(Context c) {
        String to = "#truste-consent-button, #privacyBtn, [data-an-la='cookie bar:accept'], [an-ac='cookie bar:accept'], #preferenceCheckBtn, .ins-element-close-button";
        WebElement body = WebUI.findElement("body");
        try {
            wait(6, 2).until(d -> {
                List<WebElement> elms = WebUI.findElements(to);
                if (!elms.isEmpty()) {
                    List<WebElement> terms = WebUI.findElements("#privacy-terms, #privacy-terms2");
                    driver.executeScript("for (const e of arguments[0]) e.click()", terms);
                    driver.executeScript("for (const e of arguments[0]) e.click()", elms);
                    return true;
                }
                return false;
            });
        } catch (Exception ignore) {
        }
        WebUI.delay(2);
        if (ExpectedConditions.stalenessOf(body).apply(driver)) {
            logger.info("Page reloaded, waiting for new ready state");
            waitForPageLoad(10);
        }
        driver.executeScript(DISPLAY_NONE);
        c.setPopupClosed();
    }

    static void mustCloseAllPopup(Context c) {
        if (!c.isPopupClosed()) {
            closeAllPopup(c);
        }
    }

    static void closeBrower(Context c) throws Exception {
        if (driver != null && c.checkoutProcess != null) {
            Checkout.waitForNavigateTo();
            Checkout.process(c);
        }
        if (driver != null) {
            try {
                for (String handle : driver.getWindowHandles()) {
                    driver.switchTo().window(handle);
                    driver.close();
                }
            } catch (Exception ignore) {
            }
        }
        driver = null;
        c = new Context();
    }

    static WebElement click(String selector) {
        return wait(5).until(d -> {
            WebElement elm = findElement(selector);
            elm.click();
            return elm;
        });
    }

    public static void click(WebElement elm) {
        wait(5).until(d -> {
            elm.click();
            return true;
        });
    }

    @SuppressWarnings("unchecked")
    public static void click(WebElement elm, int dx, int dy) {
        Map<String, Number> rect = (Map<String, Number>) WebUI.driver
                .executeScript("return arguments[0].getBoundingClientRect()", elm);
        int top = rect.get("top").intValue();
        int left = rect.get("left").intValue();
        new Actions(WebUI.driver).moveToLocation(left + dx, top + dy).click().perform();
    }

    static WebElement findElement(String selector) {
        List<WebElement> elms = driver.findElements(By.cssSelector(selector));
        Collections.reverse(elms);
        return elms
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
    }

    public static WebElement findElement(SearchContext root, String cssSelector) {
        return findElement(root, By.cssSelector(cssSelector));
    }

    public static WebElement findElement(SearchContext root, By by) {
        return root.findElements(by)
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
    }

    static List<WebElement> findElements(String selector) {
        return driver.findElements(By.cssSelector(selector))
                .stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }

    public static List<WebElement> findElements(SearchContext root, By by) {
        return root.findElements(by)
                .stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }

    public static void fill(String selector, String value) {
        wait(5).until(d -> {
            WebElement elm = findElement(selector);
            elm.clear();
            elm.sendKeys(value);
            return true;
        });
    }

    public static String getDomAttribute(WebElement elm, String... attrs) {
        return WebUI.driver.executeScript("""
                for (const attr of arguments[1]) {
                    let value = arguments[0].getAttribute(attr);
                    if (value) return value;
                }
                return '';
                """, elm, attrs).toString();
    }

    public static String getUrl() {
        return driver.getCurrentUrl();
    }

    static void hover(String selector) throws Exception {
        try {
            WebElement elm = waitElement(selector, 5);
            wait(5).until(ExpectedConditions.elementToBeClickable(elm));
            Actions builder = new Actions(driver);
            builder.moveToElement(elm).perform();
        } catch (Exception e) {
            throw new Exception("Unable to hover on %s".formatted(selector), e);
        }
    }

    public static boolean isSite(String site) {
        return driver.getCurrentUrl().contains("/" + site.toLowerCase());
    }

    public static boolean isOneOfSites(String... sites) {
        String url = driver.getCurrentUrl();
        for (String site : sites) {
            if (url.contains("/" + site.toLowerCase() + "/"))
                return true;
        }
        return false;
    }

    public static void scrollIntoView(WebElement elm) {
        driver.executeScript("arguments[0].scrollIntoView()", elm);
    }

    public static void scrollToCenter(WebElement elm) {
        driver.executeScript("arguments[0].scrollIntoView({block:'center'})", elm);
    }

    public static void scrollToCenterAndClick(WebElement elm, int milisecond) throws Exception {
        WebUI.scrollToCenter(elm);
        Thread.sleep(milisecond);
        elm.click();
    }

    static void switchToWindow(int index) {
        String[] handles = driver.getWindowHandles().toArray(new String[0]);
        if (handles.length > index) {
            driver.switchTo().window(handles[index]);
        }
    }

    static FluentWait<WebDriver> wait(int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds))
                .ignoring(Exception.class);
    }

    static FluentWait<WebDriver> wait(int seconds, int delay) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds), Duration.ofSeconds(delay))
                .ignoring(Exception.class);
    }

    static WebElement waitElement(String selector, int seconds) {
        try {
            return wait(seconds).until(d -> findElement(selector));
        } catch (Exception e) {
            return null;
        }
    }

    public static WebElement waitElement(SearchContext root, By by, int seconds) {
        try {
            return wait(seconds).until(d -> findElement(root, by));
        } catch (Exception e) {
            return null;
        }
    }

    public static List<WebElement> waitElements(String selector, int seconds) {
        try {
            return wait(seconds).until(d -> {
                List<WebElement> elms = findElements(selector);
                return elms.isEmpty() ? null : elms;
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static List<WebElement> waitElements(SearchContext root, By by, int seconds) {
        try {
            return wait(seconds).until(d -> {
                List<WebElement> elms = findElements(root, by);
                return elms.isEmpty() ? null : elms;
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static boolean waitForNotPresent(String selector, int seconds) {
        return wait(seconds)
                .withMessage("waiting for element '%s' to disappear".formatted(selector))
                .until(d -> d.findElements(By.cssSelector(selector)).isEmpty());
    }

    public static boolean waitForNotDisplayed(String selector, int seconds) {
        return wait(seconds)
                .withMessage("waiting for element '%s' not displayed".formatted(selector))
                .until(d -> findElement(selector) == null);
    }

    public static boolean waitForDisappear(WebElement elm, int seconds) {
        return wait(seconds).until(ExpectedConditions.or(
                ExpectedConditions.stalenessOf(elm),
                ExpectedConditions.invisibilityOf(elm)));
    }

    public static void waitForPageLoad(int seconds) {
        try {
            wait(seconds, 1).until(d -> {
                String state = WebUI.driver.executeScript("return document.readyState").toString();
                return state.equals("complete");
            });
        } catch (Exception ignore) {
        }
        logger.info("Page loaded");
    }

    public static boolean waitForStaleness(WebElement elm, int seconds) {
        return wait(seconds).until(ExpectedConditions.stalenessOf(elm));
    }

    public static boolean waitForUrlContains(String fraction, int seconds) {
        return wait(seconds).until(ExpectedConditions.urlContains(fraction));
    }
}
