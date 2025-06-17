package wisewires.agent;

import java.util.Map;

public class Context {
    public Client client;
    public Map<String, String> sso;

    public String site;
    public String stg;

    public boolean cookieReady = false;
    public boolean aemReady = false;
    public boolean ssoSignedIn = false;
}
