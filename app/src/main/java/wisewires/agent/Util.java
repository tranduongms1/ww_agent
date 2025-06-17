package wisewires.agent;

public abstract class Util {
    public static void delay(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException ignore) {
        }
    }
}
