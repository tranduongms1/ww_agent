package wisewires.agent;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class WebUI {
    static ChromeDriver driver;

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
        String url = "https://stg2.shop.samsung.com/getcookie.html";
        if (driver == null) {
            openBrowser(url);
        } else {
            driver.executeScript("window.open(arguments[0])", url);
            Util.delay(2);
            switchToWindow(1);
            waitElement(".hubble-error-page h3", 10);
            driver.close();
            switchToWindow(0);
        }
        c.cookieReady = true;
    }

    static void mustGetCookie(Context c) {
        if (!c.cookieReady) {
            getCookie(c);
        }
    }

    static void mustAEMReady(Context c) {
        if (c.aemReady)
            return;
    }

    static void click(String selector) {
        wait(5).until(d -> {
            findElement(selector).click();
            return true;
        });
    }

    static WebElement findElement(String selector) {
        return driver.findElements(By.cssSelector(selector))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
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
}
