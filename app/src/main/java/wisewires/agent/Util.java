package wisewires.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v138.page.Page;

public abstract class Util {
    static String XPATH_TEXT_LOWER = "translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')";

    static String NO_CAPTURE_LOCATOR = """
            .pd-g-product-finder-ux2 .pdd34-product-carousel,
            .pd-g-product-finder-ux2 .pdd28-reasons-to-buy,
            [position='TokoPaymentBanner'],
            [position='RecommendationSection'],
            footer""";

    public static boolean isHTTP(String ipOrDomain) {
        if (ipOrDomain.contains(":") || ipOrDomain.contains("wisewires.com.vn"))
            return true;
        try {
            InetAddress address = InetAddress.getByName(ipOrDomain);
            return address.getHostAddress().equals(ipOrDomain) && ipOrDomain.contains(".");
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

    public static Predicate<Map<String, Object>> inCategories(List<String> categories) {
        return prod -> categories.contains(prod.get("pviType").toString().toLowerCase());
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getVariants(List<Map<String, Object>> products) {
        List<Map<String, Object>> variants = new ArrayList<>();
        for (Map<String, Object> product : products) {
            for (Map<String, Object> variant : (List<Map<String, Object>>) product.get("variantOptions")) {
                variants.add(variant);
            }
        }
        return variants;
    }

    @SuppressWarnings("unchecked")
    public static boolean isPurchasable(Map<String, Object> variant) {
        if (Boolean.TRUE.equals(variant.get("purchasable")))
            return true;
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

    public static String captureToVerify(Context c) throws IOException {
        try {
            boolean fullPage = true;
            if (isPDPage() || isPFPage())
                fullPage = false;
            WebUI.driver.executeScript("""
                    for (const e of document.querySelectorAll(arguments[0])) {
                        e.setAttribute('org-style-display', e.style.display);
                        e.style.display = 'none';
                    }""", NO_CAPTURE_LOCATOR);
            WebUI.delay(1);
            DevTools devTools = WebUI.driver.getDevTools();
            devTools.createSession();
            String base64Image = devTools.send(Page.captureScreenshot(
                    Optional.of(Page.CaptureScreenshotFormat.JPEG),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(fullPage),
                    Optional.empty()));
            devTools.close();
            String tempDir = System.getProperty("java.io.tmpdir");
            String filePath = tempDir + File.separator + UUID.randomUUID() + ".jpg";
            FileOutputStream out = new FileOutputStream(filePath);
            out.write(Base64.getDecoder().decode(base64Image));
            out.close();
            return filePath;
        } finally {
            WebUI.driver.executeScript("""
                    for (const e of document.querySelectorAll(arguments[0])) {
                        e.style.display = e.getAttribute('org-style-display');
                    }""", NO_CAPTURE_LOCATOR);
        }
    }

    public static void captureImageAndCreatePost(Context c, Post post) throws Exception {
        String path = Util.captureFullPage();
        String fileId = c.client.uploadFile(post.getChannelId(), path);
        post.setFileIds(List.of(fileId));
        c.client.createPost(post);
    }

    public static boolean isPDPage() {
        return WebUI.findElement("div[class*='pdp-header']") != null;
    }

    public static boolean isPFPage() {
        return WebUI.findElement(".pd-g-product-finder-ux2") != null;
    }

    public static <K, V> Map<K, V> combined(Map<K, V> m1, Map<K, V> m2) {
        Map<K, V> combined = new HashMap<>();
        combined.putAll(m1);
        combined.putAll(m2);
        return combined;
    }
}
