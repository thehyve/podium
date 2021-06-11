package nl.thehyve.podium.common.service;

import nl.thehyve.podium.common.config.PodiumProperties;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.util.HtmlUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.internet.MimeMessage;
import javax.validation.constraints.NotNull;
import java.nio.charset.*;
import java.util.Locale;

/**
 * Service for sending e-mails.
 * <p>
 * We use the @Async annotation to send e-mails asynchronously.
 * </p>
 */
public abstract class AbstractMailService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    protected PodiumProperties podiumProperties;

    @Autowired
    protected MessageSource messageSource;

    @Autowired
    protected SpringTemplateEngine templateEngine;

    protected static final String USER = "user";

    protected static final String BASE_URL = "baseUrl";

    protected static final String SUPPORT_EMAIL = "supportEmail";

    protected static final String SIGNATURE = "signature";

    protected static final String DEFAULT_LANG_KEY = "en";

    private void setDefaultVariables(Context context) {
        PodiumProperties.Mail mailProperties = podiumProperties.getMail();
        context.setVariable(BASE_URL, mailProperties.getBaseUrl());
        context.setVariable(SUPPORT_EMAIL, mailProperties.getSupportEmail());
        context.setVariable(SIGNATURE, mailProperties.getSignature());
    }

    protected Context getDefaultContext() {
        Locale locale = Locale.forLanguageTag(DEFAULT_LANG_KEY);
        Context context = new Context(locale);
        setDefaultVariables(context);
        return context;
    }

    private Locale getLocaleForUser(@NotNull UserRepresentation user) {
        String langKey = user.getLangKey() == null ? DEFAULT_LANG_KEY : user.getLangKey();
        return Locale.forLanguageTag(langKey);
    }

    protected Context getDefaultContextForUser(UserRepresentation user) {
        Locale locale = getLocaleForUser(user);
        Context context = new Context(locale);
        setDefaultVariables(context);
        context.setVariable(USER, user);
        return context;
    }

    protected String getMessage(UserRepresentation user, String messageKey, Object... parameters) {
        Locale locale = getLocaleForUser(user);
        return messageSource.getMessage(messageKey, parameters, locale);
    }

    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        log.debug("Send e-mail[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
            isMultipart, isHtml, to, subject, content);

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(podiumProperties.getMail().getFrom());
            message.setSubject(HtmlUtils.htmlUnescape(subject));
            message.setText(content, isHtml);
            javaMailSender.send(mimeMessage);
            log.debug("Sent e-mail to User '{}'", to);
        } catch (Exception e) {
            log.warn("E-mail could not be sent to user '{}'", to, e);
        }
    }

}
