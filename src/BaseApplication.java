import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * acm
 */
public abstract class BaseApplication implements Application {

    private ExecutorService pool;

    public BaseApplication(int numberOfThreads) {
        this.pool = Executors.newFixedThreadPool(numberOfThreads);
    }

    @Override
    public void runAction(final Message message) {
        pool.execute(() -> {
            switch (message.getMessageEvent()) {
                case CONNECT:
                    Logger.debug("CONNECT");
                    break;
                case ACCEPT:
                    Logger.debug("ACCEPT");
                    break;
                case EMPTY_READ:
                    Logger.debug("EMPTY_READ");
                    break;
                case READ:
                    Logger.debug("READ");
                    run(message);
                    break;
                case WRITE:
                    Logger.debug("WRITE");
                    break;
                case CLOSE:
                    Logger.debug("CLOSE");
                    break;
                case ERROR:
                    Logger.debug("ERROR");
                    break;
                default:
                    Logger.debug("no such event found: " + message.getMessageEvent());
                    break;
            }
        });
        /*
        switch (message.getMessageEvent()) {
            case CONNECT:
                Logger.debug("CONNECT");
                break;
            case ACCEPT:
                Logger.debug("ACCEPT");
                break;
            case EMPTY_READ:
                Logger.debug("EMPTY_READ");
                break;
            case READ:
                Logger.debug("READ");
                run(message);
                break;
            case WRITE:
                Logger.debug("WRITE");
                break;
            case CLOSE:
                Logger.debug("CLOSE");
                break;
            case ERROR:
                Logger.debug("ERROR");
                break;
            default:
                Logger.debug("no such event found: " + message.getMessageEvent());
                break;
        }
        */
    }

    private void run(Message message) {
        Request request = createRequest(message.getData());
        /*
        // single threaded
            try {
                Action action = request.getActionClass().newInstance();
                Response response = (Response) request.getMethod().invoke(action, request);
                message.setData(response.getBytes());
                message.setNumberOfBytes(response.getBytes().length);
            } catch (Exception e) {
                e.printStackTrace();
            }
            message.getHandler().send(message);
         */
        // multi threaded
        pool.execute(() -> {
            try {
                Action action = request.getActionClass().newInstance();
                Response response = (Response) request.getMethod().invoke(action, request);
                message.setData(response.getBytes());
                message.setNumberOfBytes(response.getBytes().length);
                message.getHandler().send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    protected abstract Request createRequest(byte[] data);

}
