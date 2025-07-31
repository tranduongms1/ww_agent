package wisewires.agent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
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

public abstract class WebUI {
    private static Logger logger = LoggerFactory.getLogger(WebUI.class);

    static ChromeDriver driver;

    public static void delay(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException ignore) {
        }
    }

    static void openBrowser(Context c, String url) {
        if (url.contains("pre-qa")) {
            mustAEMReady(c);
        } else if (url.startsWith("https://stg")) {
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

    static void getPointing(Context c) {
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

    static void mustAEMReady(Context c) {
        if (!c.isAEMReady()) {
            mustGetCookie(c);
            getPointing(c);
            c.setAEMReady();
        }
    }

    static void closeAllPopup(Context c) {
        String to = "#truste-consent-button, #privacyBtn, [data-an-la='cookie bar:accept'], [an-ac='cookie bar:accept'], #preferenceCheckBtn, .ins-element-close-button";
        try {
            wait(6, 2).until(d -> {
                List<WebElement> elms = WebUI.findElements(to);
                if (elms.stream().anyMatch(WebElement::isDisplayed)) {
                    List<WebElement> terms = WebUI.findElements("#privacy-terms, #privacy-terms2");
                    driver.executeScript("for (const e of arguments[0]) e.click()", terms);
                    driver.executeScript("for (const e of arguments[0]) e.click()", elms);
                    String script = "a=document.createElement('style');a.innerHTML='iframe.fpw-view, #spr-live-chat-app {display:none !important}';document.head.appendChild(a)";
                    driver.executeScript(script);
                    return true;
                }
                return false;
            });
        } catch (Exception ignore) {
        }
        c.setPopupClosed();
    }

    static void mustCloseAllPopup(Context c) {
        if (!c.isPopupClosed()) {
            closeAllPopup(c);
        }
    }

    static void click(String selector) {
        wait(5).until(d -> {
            findElement(selector).click();
            return true;
        });
    }

    public static void click(WebElement elm) {
        wait(5).until(d -> {
            elm.click();
            return true;
        });
    }

    public static void click(WebElement elm, int x, int y) {
        Rectangle rect = elm.getRect();
        int xOffset = x - Math.floorDiv(rect.width, 2);
        int yOffset = y - Math.floorDiv(rect.height, 2);
        new Actions(WebUI.driver).moveToElement(elm, xOffset, yOffset).click().perform();
    }

    static WebElement findElement(String selector) {
        return driver.findElements(By.cssSelector(selector))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
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
        for (String attr : attrs) {
            String value = elm.getDomAttribute(attr);
            if (value != null) {
                return value;
            }
        }
        return "";
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

    public static void scrollToCenter(WebElement elm) {
        driver.executeScript("arguments[0].scrollIntoView({block:'center'})", elm);
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
            String msg = "element located by '%s'".formatted(selector);
            return wait(seconds).withMessage(msg).until(d -> findElement(selector));
        } catch (Exception e) {
            return null;
        }
    }

    public static WebElement waitElement(SearchContext root, By by, int seconds) {
        try {
            return wait(seconds)
                    .withMessage("element located by '%s'".formatted(by))
                    .until(d -> findElement(root, by));
        } catch (Exception e) {
            logger.warn("Element located by '%s' not found".formatted(by));
            return null;
        }
    }

    public static List<WebElement> waitElements(String selector, int seconds) {
        try {
            return wait(seconds).withMessage("elements located by '%s'".formatted(selector)).until(d -> {
                List<WebElement> elms = findElements(selector);
                return elms.isEmpty() ? null : elms;
            });
        } catch (Exception e) {
            logger.warn("Elements located by '%s' not found".formatted(selector));
            return new ArrayList<>();
        }
    }

    public static boolean waitForElementNotPresent(String selector, int seconds) {
        return wait(seconds)
                .withMessage("waiting for element '%s' to disappear".formatted(selector))
                .until(d -> d.findElements(By.cssSelector(selector)).isEmpty());
    }

    public static boolean waitForDisappear(WebElement elm, int seconds) {
        return wait(seconds).until(ExpectedConditions.or(
                ExpectedConditions.stalenessOf(elm),
                ExpectedConditions.invisibilityOf(elm)));
    }

    public static boolean waitForStaleness(WebElement elm, int seconds) {
        return wait(seconds).until(ExpectedConditions.stalenessOf(elm));
    }

    public static boolean waitForUrlContains(String fraction, int seconds) {
        return wait(seconds).until(ExpectedConditions.urlContains(fraction));
    }
}
