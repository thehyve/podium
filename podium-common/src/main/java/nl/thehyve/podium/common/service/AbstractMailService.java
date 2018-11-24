package nl.thehyve.podium.common.service;

import nl.thehyve.podium.common.config.PodiumProperties;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import org.apache.commons.lang3.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.util.HtmlUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.mail.internet.MimeMessage;
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

    protected static final String DEFAULT_LANG_KEY = "en";

    protected Context getDefaultContextForUser(UserRepresentation user) {
        Locale locale = Locale.forLanguageTag(user.getLangKey() == null ? DEFAULT_LANG_KEY : user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, podiumProperties.getMail().getBaseUrl());
        return context;
    }

    protected String getMessage(UserRepresentation user, String messageKey, Object... parameters) {
        Locale locale = Locale.forLanguageTag(user.getLangKey() == null ? DEFAULT_LANG_KEY : user.getLangKey());
        return messageSource.getMessage(messageKey, parameters, locale);
    }

    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        log.debug("Send e-mail[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
            isMultipart, isHtml, to, subject, content);

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, CharEncoding.UTF_8);
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
