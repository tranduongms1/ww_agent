package wisewires.agent;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PF {
    private static Logger logger = LoggerFactory.getLogger(PF.class);

    public static WebElement findProductCard(Context c, String productName) throws Exception {
        try {
            String xpath = "//div[contains(@class,'js-pfv2-product-card')][.//text()[normalize-space(.)='%s']]".formatted(productName);
            WebUI.findElement("body").sendKeys(Keys.PAGE_DOWN);
            WebElement card = WebUI.wait(30).until(d -> {
                WebElement elm = WebUI.driver.findElement(By.xpath(xpath));
                if (elm != null) {
                    return elm;
                }
                WebElement moreBtn = WebUI.findElement(".pd19-product-finder__view-more-btn");
                if (moreBtn != null) {
                    WebUI.scrollToCenter(moreBtn);
                    WebUI.delay(1);
                    moreBtn.click();
                }
                return null;
            });
            if (card == null) {
                throw new Exception("Product card not found");
            }
            WebUI.scrollToCenter(card);
            WebUI.delay(1);
            logger.info("Found product card for: " + productName);
            c.pfProcess.productCard = card;
            return card;
        } catch (Exception e) {
            c.pfProcess = null;
            throw new Exception("Unable to find product card for: " + productName, e);
        }
    }

    public static void selectColor(Context c, String color) throws Exception {
        try {
            WebElement card = c.pfProcess.productCard;
            String to = "[an-la*='color:%s'i]".formatted(color);
            WebElement elm = card.findElement(By.cssSelector(to));
            WebUI.scrollToCenterAndClick(elm, 500);
            WebUI.delay(1);
            logger.info("Selected color: " + color);
        } catch (Exception e) {
            c.pfProcess = null;
            throw new Exception("Unable to select color: " + color, e);
        }
    }

    public static void selectStorage(Context c, String storage) throws Exception {
        try {
            WebElement card = c.pfProcess.productCard;
            String to = "[an-la*='mobile memory:%s'i]".formatted(storage);
            WebElement elm = card.findElement(By.cssSelector(to));
            WebUI.scrollToCenterAndClick(elm, 500);
            WebUI.delay(1);
            logger.info("Selected storage: " + storage);
        } catch (Exception e) {
            c.pfProcess = null;
            throw new Exception("Unable to select storage: " + storage, e);
        }
    }
    public static void selectSize(Context c, String size) throws Exception {
        try {
            WebElement card = c.pfProcess.productCard;
            String to = "[an-la*='size:%s'i]".formatted(size);
            WebElement elm = card.findElement(By.cssSelector(to));
            WebUI.scrollToCenterAndClick(elm, 500);
            WebUI.delay(1);
            logger.info("Selected size: " + size);
        } catch (Exception e) {
            c.pfProcess = null;
            throw new Exception("Unable to select size: " + size, e);
        }
    }

    public static void clickBuyNow(Context c) throws Exception {
        try {
            WebElement card = c.pfProcess.productCard;
            WebElement buyNowBtn = card.findElement(By.cssSelector("[an-la='pf product card:buy now']"));
            WebUI.scrollToCenterAndClick(buyNowBtn, 500);
            WebUI.delay(1);
            logger.info("Clicked Buy Now button");
            WebUI.waitForStaleness(buyNowBtn, 5);
            c.pfProcess = null;
        } catch (Exception e) {
            c.pfProcess = null;
            throw new Exception("Unable to click Buy Now button", e);
        }
    }

    public static void clickAddToCart(Context c) throws Exception {
        try {
            WebElement card = c.pfProcess.productCard;
            WebElement addToCartBtn = card.findElement(By.cssSelector("[an-la='pf product card:add to cart']"));
            WebUI.scrollToCenterAndClick(addToCartBtn, 500);
            WebUI.delay(1);
            logger.info("Clicked add to cart button");
            WebUI.waitForStaleness(addToCartBtn, 5);
            c.pfProcess = null;
        } catch (Exception e) {
            c.pfProcess = null;
            throw new Exception("Unable to click add to cart button", e);
        }
    }
}
