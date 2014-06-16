import java.util.Date;

/**
 * acm
 */
public class Logger {
    public static void debug(String log) {
        System.out.println("[" + Thread.currentThread().getId() + "] " + log);
    }
}
