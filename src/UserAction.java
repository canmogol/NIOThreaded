/**
 * acm
 */
public class UserAction implements Action {
    public Response doLoginExec(Request request) {
        Logger.debug("UserAction doLoginExec");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        return new Response("Hi there user!".getBytes());
    }
}
