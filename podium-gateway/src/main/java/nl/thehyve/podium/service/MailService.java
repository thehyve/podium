/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.config.PodiumProperties;
import nl.thehyve.podium.domain.Request;
import org.apache.commons.lang3.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Locale;

@Service
public class MailService {

    private final Logger log = LoggerFactory.getLogger(MailService.class);

    private static final String USER = "user";

    private static final String BASE_URL = "baseUrl";

    @Autowired
    private PodiumProperties podiumProperties;

    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SpringTemplateEngine templateEngine;

    /**
     * Sends email with provided parameters.
     * @param to the adressee
     * @param subject subject line.
     * @param content the message body.
     * @param isMultipart whether to sent a multipart message or not.
     * @param isHtml whether the contents is HTML.
     */
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

    /**
     * Notify the coordinators of an organisation that a request has been
     * submitted to their organisation.
     * @param organisationRequest the request that has been submitted.
     * @param coordinators the list of organisation coordinators.
     */
    @Async
    public void notifyCoordinators(Request organisationRequest, OrganisationDTO organisation,
                                   List<UserRepresentation> coordinators) {
        log.info("Notifying coordinators: request = {}, organisation = {}, #coordinators = {}",
            organisationRequest, organisation, coordinators == null ? null : coordinators.size());
        log.info("Mail sender: {} ({})", this.javaMailSender, this.javaMailSender.toString());
        for (UserRepresentation user: coordinators) {
            log.debug("Sending request submitted e-mail to '{}'", user.getEmail());
            Locale locale = Locale.forLanguageTag(user.getLangKey());
            Context context = new Context(locale);
            context.setVariable(USER, user);
            context.setVariable(BASE_URL, podiumProperties.getMail().getBaseUrl());
            context.setVariable("request", organisationRequest);
            context.setVariable("organisation", organisation);
            String content = templateEngine.process("requestSubmitted", context);
            String subject = messageSource.getMessage("email.requestSubmitted.title", null, locale);
            sendEmail(user.getEmail(), subject, content, false, true);
        }
    }
}
