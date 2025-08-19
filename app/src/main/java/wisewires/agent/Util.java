package wisewires.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v138.page.Page;

public abstract class Util {
    static String XPATH_TEXT_LOWER = "translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')";

    public static boolean isValidIPv4(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.getHostAddress().equals(ip) && ip.contains(".");
        } catch (UnknownHostException ex) {
            return false;
        }
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean isPurchasable(Map<String, Object> variant) {
        Map<String, Object> stock = (Map<String, Object>) variant.get("stock");
        if (stock.get("stockLevel") == null)
            return false;
        long stockLevel = (long) stock.get("stockLevel");
        return stockLevel > 0;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getCartEntry(Map<String, Object> cartInfo, String sku) {
        List<Map<String, Object>> entries = (List<Map<String, Object>>) cartInfo.get("entries");
        for (Map<String, Object> entry : entries) {
            Map<String, Object> product = (Map<String, Object>) entry.get("product");
            if (product.get("code").toString().equalsIgnoreCase(sku)) {
                return entry;
            }
        }
        return null;
    }

    public static long getCartEntryNumber(Map<String, Object> cartInfo, String sku) {
        Map<String, Object> entry = getCartEntry(cartInfo, sku);
        return entry != null ? (long) entry.get("entryNumber") : -1;
    }

    public static String captureFullPage() throws IOException {
        DevTools devTools = WebUI.driver.getDevTools();
        devTools.createSession();
        String base64Image = devTools.send(Page.captureScreenshot(
                Optional.of(Page.CaptureScreenshotFormat.JPEG),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(true),
                Optional.empty()));
        devTools.close();
        String tempDir = System.getProperty("java.io.tmpdir");
        String filePath = tempDir + File.separator + UUID.randomUUID() + ".jpg";
        FileOutputStream out = new FileOutputStream(filePath);
        out.write(Base64.getDecoder().decode(base64Image));
        out.close();
        return filePath;
    }

    public static void captureImageAndCreatePost(Context c, Post post) throws Exception {
        String path = Util.captureFullPage();
        String fileId = c.client.uploadFile(post.getChannelId(), path);
        post.setFileIds(List.of(fileId));
        c.client.createPost(post);
    }

    public static boolean isPDPage() {
        String pdSelector = "div[class*='pdp-header']";
        WebElement pdPage = WebUI.findElement(pdSelector);
        if (pdPage != null) {
            return true;
        }
        return false;
    }

    public static <K, V> Map<K, V> combined(Map<K, V> m1, Map<K, V> m2) {
        Map<K, V> combined = new HashMap<>();
        combined.putAll(m1);
        combined.putAll(m2);
        return combined;
    }
}
