import java.util.HashMap;
import java.util.Map;

/**
 * acm
 */
public class HttpApplication extends BaseApplication {

    public HttpApplication(int numberOfThreads) {
        super(numberOfThreads);
    }

    protected Request createRequest(byte[] data) {
        String requestUrl = "/myApp/user/login?username=acm&password=123";
        Map<String, String> parameters = new HashMap<String, String>() {{
            put("username", "acm");
            put("password", "123");
        }};
        Map<String, String> headers = new HashMap<String, String>() {{
            put("method", "GET");
            put("x-some-thing", "000");
        }};
        Session session = new Session();

        Request request = new Request();
        try {
            request.setHeaders(headers);
            request.setParameters(parameters);
            request.setRequestUrl(requestUrl);
            request.setSession(session);
            request.setActionClass(UserAction.class);
            request.setMethod(UserAction.class.getMethod("doLoginExec", Request.class));
            return request;
        } catch (Exception e) {
            e.printStackTrace();
            return request;
        }
    }

}
