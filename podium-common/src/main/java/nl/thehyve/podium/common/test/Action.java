package nl.thehyve.podium.common.test;

import nl.thehyve.podium.common.security.AuthenticatedUser;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.*;

/**
 * Class that encodes a test action.
 */
public class Action {

    /**
     * The method to perform.
     */
    public HttpMethod method = HttpMethod.GET;
    /**
     * The url to perform the action at.
     */
    public String url = null;
    /**
     * Map from user UUID to url, for when a unique url is required per user,
     * e.g., when a side effect of an action makes the url unfit for use in another test.
     */
    public Map<UUID, String> urls = null;
    /**
     * The query parameters.
     */
    public Map<String, String> parameters = new HashMap<>();
    /**
     * The request body.
     */
    public Object body;
    /**
     * The users that are supposed to be allowed to execute the action.
     */
    public Collection<AuthenticatedUser> allowedUsers = new LinkedHashSet<>();
    /**
     * The expected status if any of these users performs the action.
     */
    public HttpStatus expectedStatus;
    /**
     * The success status if any of the allowed users performs the action.
     */
    public HttpStatus successStatus;

    public Action setMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public Action setUrl(String url) {
        this.url = url;
        return this;
    }

    public Action setUrls(Map<UUID, String> urls) {
        this.urls = urls;
        return this;
    }

    public Action set(String param, Object value) {
        this.parameters.put(param, value.toString());
        return this;
    }

    public Action body(Object body) {
        this.body = body;
        return this;
    }

    public Action allow(AuthenticatedUser ... users) {
        this.allowedUsers.addAll(Arrays.asList(users));
        return this;
    }

    public Action expect(HttpStatus status) {
        this.expectedStatus = status;
        return this;
    }

    public Action successStatus(HttpStatus status) {
        this.successStatus = status;
        return this;
    }

    public static Action newAction() {
        return new Action();
    }

    public static String format(String url, String format, Object ... args) {
        return url + String.format(format, args);
    }

}
