import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ApplicationHandler implements MessageHandler, Runnable {

    private final List<Message> queue = new LinkedList<>();
    private boolean running = true;
    private Map<String, HttpApplication> urlAppMap = new HashMap<>();

    public void send(Message message) {
        byte[] dataCopy = new byte[message.getNumberOfBytes()];
        System.arraycopy(message.getData(), 0, dataCopy, 0, message.getNumberOfBytes());
        message.setData(dataCopy);
        synchronized (queue) {
            queue.add(message);
            queue.notify();
        }
    }

    public void stop() {
        running = false;
        synchronized (queue) {
            queue.notify();
        }
    }

    public void run() {
        Message message;

        while (running) {
            // Wait for data to become available
            synchronized (queue) {
                while (queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                message = queue.remove(0);
            }
            runApplication(message);
        }
    }

    public void runApplication(Message message) {
        String appUrl = findAppUrl(message.getData());
        if (!urlAppMap.containsKey(appUrl)) {
            Logger.debug("CREATING APP");
            urlAppMap.put(appUrl, new HttpApplication(10));
        }
        urlAppMap.get(appUrl).runAction(message);
    }

    private String findAppUrl(byte[] data) {
        return "/myApp";
    }
}
