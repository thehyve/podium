/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.common.service.AbstractMailService;
import nl.thehyve.podium.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.Collection;
import java.util.Locale;

/**
 * Service for sending e-mails.
 * <p>
 * We use the @Async annotation to send e-mails asynchronously.
 * </p>
 */
@Service
public class MailService extends AbstractMailService {

    private final Logger log = LoggerFactory.getLogger(MailService.class);

    @Async
    public void sendVerificationEmail(User user) {
        log.debug("Sending verification e-mail to '{}'", user.getEmail());
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, podiumProperties.getMail().getBaseUrl());
        String content = templateEngine.process("verificationEmail", context);
        String subject = messageSource.getMessage("email.verification.title", null, locale);
        sendEmail(user.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendCreationEmail(User user) {
        log.debug("Sending creation e-mail to '{}'", user.getEmail());
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, podiumProperties.getMail().getBaseUrl());
        String content = templateEngine.process("creationEmail", context);
        String subject = messageSource.getMessage("email.verification.title", null, locale);
        sendEmail(user.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendUserRegisteredEmail(Collection<User> administrators, User registeredUser) {
        log.debug("Notify BBRMI administrators of registered user: '{}'", registeredUser.getEmail());
        for (User user: administrators) {
            Locale locale = Locale.forLanguageTag(user.getLangKey());
            Context context = new Context(locale);
            context.setVariable(USER, user);
            context.setVariable("registeredUser", registeredUser);
            context.setVariable(BASE_URL, podiumProperties.getMail().getBaseUrl());
            String content = templateEngine.process("userRegistered", context);
            String subject = messageSource.getMessage("email.userRegistered.title", null, locale);
            sendEmail(user.getEmail(), subject, content, false, true);
        }
    }

    @Async
    public void sendPasswordResetMail(User user) {
        log.debug("Sending password reset e-mail to '{}'", user.getEmail());
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, podiumProperties.getMail().getBaseUrl());
        String content = templateEngine.process("passwordResetEmail", context);
        String subject = messageSource.getMessage("email.reset.title", null, locale);
        sendEmail(user.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendPasswordResetMailNoUser(String email) {
        log.debug("Sending no user password reset e-mail to '{}'", email);
        // Send email in english
        Locale locale = Locale.forLanguageTag("en");
        Context context = new Context(locale);
        context.setVariable(BASE_URL, podiumProperties.getMail().getBaseUrl());
        String content = templateEngine.process("passwordResetEmailNoUser", context);
        String subject = messageSource.getMessage("email.reset.noUser.title", null, locale);
        sendEmail(email, subject, content, false, true);
    }

    @Async
    public void sendAccountLockedMail(User user) {
        log.debug("Sending account locked e-mail to '{}'", user.getEmail());
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, podiumProperties.getMail().getBaseUrl());
        String content = templateEngine.process("accountLockedEmail", context);
        String subject = messageSource.getMessage("email.accountLocked.title", null, locale);
        sendEmail(user.getEmail(), subject, content, false, true);
    }

    public void sendAccountAlreadyExists(User user) {
        log.debug("Sending account already exists e-mail to '{}'", user.getEmail());
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, podiumProperties.getMail().getBaseUrl());
        String content = templateEngine.process("accountAlreadyExistsEmail", context);
        String subject = messageSource.getMessage("email.accountAlreadyExists.title", null, locale);
        sendEmail(user.getEmail(), subject, content, false, true);
    }

}
