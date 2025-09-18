package wisewires.agent;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
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

    public HashMap<String, Object> testData;
    public TradeInProcess tradeInProcess;
    public SCPProcess scpProcess;
    public EWProcess ewProcess;
    public SIMProcess simProcess;
    public PFProcess pfProcess;
    public CheckoutProcess checkoutProcess;
    public GalaxyClubProcess galaxyClubProcess;
    public DASubscriptionProcess daSubscriptionProcess;
    public PaymentProcess paymentProcess;

    private boolean cookieReady = false;
    private Map<String, Boolean> aemReady = new HashMap<>();
    private Map<String, Boolean> popupClosed = new HashMap<>();
    private Map<String, Profile> profiles = new HashMap<>();

    public Context() {
        this.siteUid = "";
        this.env = "stg2";
        this.sso = new HashMap<>(Map.of("email", "test4.buivan@gmail.com", "mk", "Heocon12"));
        this.testData = new HashMap<>();
    }

    public String envKey() {
        return "%s_%s_%s".formatted(site.toUpperCase(),
                siteUid.isEmpty() ? "estore" : siteUid, env.isEmpty() ? "prod" : env);
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

    public void unsetPopupClosed() {
        popupClosed.put(envKey(), false);
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
        return "https://p6-pre-qa2.samsung.com/%s".formatted(getSiteUid());
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
        return siteUid.isEmpty() ? site.toLowerCase() : siteUid;
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

    public String getTariffEndpoint() throws Exception {
        Map<String, String> endpoints = getProfile().getTariffEndpoints();
        return endpoints.get(env);
    }

    public TradeInProcess mustTradeInProcess() throws Exception {
        if (this.tradeInProcess == null) {
            Map<String, String> data = getProfile().getTradeInData();
            this.tradeInProcess = new TradeInProcess(data);
        }
        return this.tradeInProcess;
    }

    public SCPProcess mustSCPProcess() {
        if (this.scpProcess == null) {
            this.scpProcess = new SCPProcess();
        }
        return this.scpProcess;
    }

    public EWProcess mustEWProcess() {
        if (this.ewProcess == null) {
            this.ewProcess = new EWProcess();
        }
        return this.ewProcess;
    }

    public SIMProcess mustSIMProcess() {
        if (this.simProcess == null) {
            this.simProcess = new SIMProcess();
        }
        return this.simProcess;
    }

    public GalaxyClubProcess mustGalaxyClubProcess() {
        if (this.galaxyClubProcess == null) {
            this.galaxyClubProcess = new GalaxyClubProcess();
        }
        return this.galaxyClubProcess;
    }

    public DASubscriptionProcess mustDASubscriptionProcess() {
        if (this.daSubscriptionProcess == null) {
            this.daSubscriptionProcess = new DASubscriptionProcess();
        }
        return this.daSubscriptionProcess;
    }

    public void usePostalCode(String code) throws Exception {
        Map<String, String> data = getProfile().getCustomerAddress();
        data.put("postalCode", code);
        switch (site.toUpperCase()) {
            case "AU":
                data.put("adminLevel1", "__ANY__");
                data.put("adminLevel2", "__ANY__");
                break;
        }
    }

    public CheckoutProcess mustCheckoutProcess() {
        if (this.checkoutProcess == null) {
            this.checkoutProcess = new CheckoutProcess();
        }
        return this.checkoutProcess;
    }

    public void reset() {
        ssoSignedIn = false;

        scpProcess = null;
        ewProcess = null;
        pfProcess = null;
        simProcess = null;
        checkoutProcess = null;

        cookieReady = false;
        aemReady = new HashMap<>();
        popupClosed = new HashMap<>();
    }
}
