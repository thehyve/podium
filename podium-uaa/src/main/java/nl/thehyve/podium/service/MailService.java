/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.common.service.AbstractMailService;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
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

    void prepareSignature(Context context) {
        templateEngine.process("signature", context);
    }

    @Async
    public void sendVerificationEmail(UserRepresentation user, String activationKey) {
        log.debug("Sending verification e-mail to '{}'", user.getEmail());
        Context context = getDefaultContextForUser(user);
        prepareSignature(context);
        context.setVariable("activationKey", activationKey);
        String content = templateEngine.process("verificationEmail", context);
        String subject = getMessage(user, "email.verification.title");
        sendEmail(user.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendAccountVerifiedEmail(UserRepresentation user) {
        log.debug("Sending account verified e-mail to '{}'", user.getEmail());
        Context context = getDefaultContextForUser(user);
        prepareSignature(context);
        String content = templateEngine.process("accountVerifiedEmail", context);
        String subject = getMessage(user, "email.accountVerified.title");
        sendEmail(user.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendCreationEmail(UserRepresentation user, String resetKey) {
        log.debug("Sending creation e-mail to '{}'", user.getEmail());
        Context context = getDefaultContextForUser(user);
        prepareSignature(context);
        context.setVariable("resetKey", resetKey);
        String content = templateEngine.process("creationEmail", context);
        String subject = getMessage(user, "email.creation.title");
        sendEmail(user.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendUserRegisteredEmail(Collection<? extends UserRepresentation> administrators, UserRepresentation registeredUser) {
        log.debug("Notify BBRMI administrators of registered user: '{}'", registeredUser.getEmail());
        for (UserRepresentation user: administrators) {
            Context context = getDefaultContextForUser(user);
            prepareSignature(context);
            context.setVariable("registeredUser", registeredUser);
            String content = templateEngine.process("userRegistered", context);
            String subject = getMessage(user, "email.userRegistered.title");
            sendEmail(user.getEmail(), subject, content, false, true);
        }
    }

    @Async
    public void sendPasswordResetMail(UserRepresentation user, String resetKey) {
        log.debug("Sending password reset e-mail to '{}'", user.getEmail());
        Context context = getDefaultContextForUser(user);
        prepareSignature(context);
        context.setVariable("resetKey", resetKey);
        String content = templateEngine.process("passwordResetEmail", context);
        String subject = getMessage(user, "email.reset.title");
        sendEmail(user.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendPasswordResetMailNoUser(String email) {
        log.debug("Sending no user password reset e-mail to '{}'", email);
        // Send email in default language
        Locale locale = Locale.forLanguageTag(DEFAULT_LANG_KEY);
        Context context = new Context(locale);
        prepareSignature(context);
        context.setVariable(BASE_URL, podiumProperties.getMail().getBaseUrl());
        String content = templateEngine.process("passwordResetEmailNoUser", context);
        String subject = messageSource.getMessage("email.reset.noUser.title", null, locale);
        sendEmail(email, subject, content, false, true);
    }

    @Async
    public void sendAccountLockedMail(UserRepresentation user) {
        log.debug("Sending account locked e-mail to '{}'", user.getEmail());
        Context context = getDefaultContextForUser(user);
        prepareSignature(context);
        String content = templateEngine.process("accountLockedEmail", context);
        String subject = getMessage(user, "email.accountLocked.title");
        sendEmail(user.getEmail(), subject, content, false, true);
    }

    public void sendAccountAlreadyExists(UserRepresentation user) {
        log.debug("Sending account already exists e-mail to '{}'", user.getEmail());
        Context context = getDefaultContextForUser(user);
        prepareSignature(context);
        String content = templateEngine.process("accountAlreadyExistsEmail", context);
        String subject = getMessage(user, "email.accountAlreadyExists.title");
        sendEmail(user.getEmail(), subject, content, false, true);
    }

}
