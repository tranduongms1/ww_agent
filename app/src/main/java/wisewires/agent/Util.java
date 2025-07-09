package wisewires.agent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

public abstract class Util {
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
}
