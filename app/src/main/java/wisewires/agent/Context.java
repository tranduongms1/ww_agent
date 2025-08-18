package wisewires.agent;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class Context {
    public Client client;
    public Post post;

    public String site;
    public String siteUid;
    public String env;
    public Map<String, String> sso;
    public boolean ssoSignedIn = false;

    public SCPProcess scpProcess;
    public CheckoutProcess checkoutProcess;

    private boolean cookieReady = false;
    private Map<String, Boolean> aemReady = new HashMap<>();
    private Map<String, Boolean> popupClosed = new HashMap<>();
    private Map<String, Profile> profiles = new HashMap<>();
    private List<String> logMessages = new ArrayList<>();

    public Context() {
        this.siteUid = "";
        this.env = "stg2";
        this.sso = new HashMap<>(Map.of("email", "test4.buivan@gmail.com", "mk", "Heocon12"));
    }

    public String envKey() {
        return "%s_%s_%s".formatted(site.toUpperCase(), siteUid != "" ? siteUid : "estore", env != "" ? env : "prod");
    }

    public boolean isCookieReady() {
        return cookieReady;
    }

    public void setCookieReady() {
        cookieReady = true;
    }

    public boolean isAEMReady() {
        Boolean value = aemReady.get(envKey());
        return value != null ? value : false;
    }

    public void setAEMReady() {
        aemReady.put(envKey(), true);
    }

    public boolean isPopupClosed() {
        Boolean value = popupClosed.get(envKey());
        return value != null ? value : false;
    }

    public void setPopupClosed() {
        popupClosed.put(envKey(), true);
    }

    public Profile getProfile() throws Exception {
        if (!profiles.containsKey(site)) {
            InputStream inputStream = JsonReader.class.getResourceAsStream("/data/" + site + ".json");
            InputStreamReader reader = new InputStreamReader(inputStream);
            if (inputStream == null) {
                throw new Exception("Data file for %s not found!".formatted(site));
            }
            profiles.put(site, new Gson().fromJson(reader, Profile.class));
        }
        return profiles.get(site);
    }

    public String getCookieUrl() {
        return "https://%s.shop.samsung.com/getcookie.html".formatted(env);
    }

    public String getHomeUrl() {
        return "https://p6-pre-qa2.samsung.com/%s".formatted(siteUid);
    }

    public String getShopUrl() throws Exception {
        String tail = "";
        if (!siteUid.isEmpty()) {
            tail = getProfile().getStores().get(siteUid);
        }
        if (env.equalsIgnoreCase("prod")) {
            return "https://shop.samsung.com/%s%s".formatted(site.toLowerCase(), tail);
        }
        return "https://%s.shop.samsung.com/%s%s".formatted(env, site.toLowerCase(), tail);
    }

    public String getCartUrl() throws Exception {
        return getShopUrl() + "/cart";
    }

    public String getSiteUid() {
        return siteUid != "" ? siteUid : site.toLowerCase();
    }

    public String getPointingUrl() throws Exception {
        Map<String, String> urls = getProfile().getPointingUrls();
        return urls.get(env);
    }

    public String getAPIEndpoint() throws Exception {
        Map<String, String> apiEndpoints = getProfile().getApiEndpoints();
        return apiEndpoints.get(env) + getSiteUid();
    }

    public String getExchangeEndpoint() throws Exception {
        Map<String, String> endpoints = getProfile().getExchangeEndpoints();
        return endpoints.get(env);
    }

    public SCPProcess mustSCPProcess() {
        if (this.scpProcess == null) {
            this.scpProcess = new SCPProcess();
        }
        return this.scpProcess;
    }

    public CheckoutProcess mustCheckoutProcess() {
        if (this.checkoutProcess == null) {
            this.checkoutProcess = new CheckoutProcess();
        }
        return this.checkoutProcess;
    }

    public void log(String message) {
        logMessages.add(message);
    }

    public List<String> getAllLogs() {
        return logMessages;
    }

    public void clearLogs() {
        logMessages.clear();
    }
}
