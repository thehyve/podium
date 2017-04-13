package nl.thehyve.podium.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.stereotype.Component;

import javax.enterprise.context.RequestScoped;

@Component("requestAuth2ClientContext")
@RequestScoped
public class RequestAuth2ClientContext extends DefaultOAuth2ClientContext {

    private final Logger log = LoggerFactory.getLogger(RequestAuth2ClientContext.class);

    public RequestAuth2ClientContext() {
        log.info("Creating new RequestAuth2ClientContext.");
    }

}
