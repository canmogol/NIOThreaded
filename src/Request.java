import java.lang.reflect.Method;
import java.util.Map;

/**
 * acm
 */
public class Request {
    private String requestUrl;
    private Map<String, String> parameters;
    private Map<String, String> headers;
    private Session session;
    private Class<? extends Action> actionClass;
    private Method method;

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Class<? extends Action> getActionClass() {
        return actionClass;
    }

    public void setActionClass(Class<? extends Action> actionClass) {
        this.actionClass = actionClass;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
