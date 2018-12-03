/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.common.service.AbstractMailService;
import nl.thehyve.podium.common.service.dto.DeliveryProcessRepresentation;
import nl.thehyve.podium.common.service.dto.OrganisationRepresentation;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MailService extends AbstractMailService {

    private final Logger log = LoggerFactory.getLogger(MailService.class);

    void prepareSignature(Context context) {
        templateEngine.process("signature", context);
    }

    /**
     * Send a notification email to the coordinators of an organisation that a request has been
     * submitted to their organisation.
     *
     * @param request the request that has been submitted.
     * @param organisation the organisation that is was submitted to
     * @param coordinators the list of organisation coordinators.
     */
    @Async
    public void sendSubmissionNotificationToCoordinators(
        RequestRepresentation request, OrganisationRepresentation organisation, List<UserRepresentation> coordinators
    ) {
        log.info("Notifying coordinators: request = {}, organisation = {}, #coordinators = {}",
            request.getUuid(), organisation.getShortName(), coordinators == null ? null : coordinators.size());
        if (coordinators == null) {
            return;
        }
        for (UserRepresentation user: coordinators) {
            log.debug("Sending request submitted e-mail to '{}'", user.getEmail());
            Context context = getDefaultContextForUser(user);
            prepareSignature(context);
            context.setVariable("request", request);
            context.setVariable("organisation", organisation);
            String content = templateEngine.process("organisationRequestSubmitted", context);
            String subject = getMessage(user, "email.organisationRequestSubmitted.title", request.generateStringId());
            sendEmail(user.getEmail(), subject, content, false, true);
        }
    }

    /**
     * Send a notification email to the requester that their request has been
     * submitted.
     *
     * @param requester the requester details
     * @param organisationRequests the list of generated requests
     */
    @Async
    public void sendSubmissionNotificationToRequester(
        UserRepresentation requester, List<RequestRepresentation> organisationRequests) {
        log.info("Notifying requester: requester = {}, #requests = {}",
            requester.getLogin(), organisationRequests == null ? null : organisationRequests.size());
        if (organisationRequests == null) {
            return;
        }
        log.debug("Sending request submitted e-mail to '{}'", requester.getEmail());
        Context context = getDefaultContextForUser(requester);
        prepareSignature(context);
        context.setVariable("requests", organisationRequests);
        String requestList = organisationRequests.stream().map(RequestRepresentation::generateStringId).collect(Collectors.joining(", "));
        context.setVariable("requestList", requestList);
        String content = templateEngine.process("requesterRequestSubmitted", context);
        String subject = getMessage(requester, "email.requesterRequestSubmitted.title", requestList);
        sendEmail(requester.getEmail(), subject, content, false, true);
    }

    /**
     * Send a notification email to the requester informing that their request has been rejected.
     *
     * @param requester the requester details
     * @param requestRepresentation the request regarding this notification
     */
    @Async
    public void sendRejectionNotificationToRequester(
        UserRepresentation requester, RequestRepresentation requestRepresentation
    ) {
        log.info("Notifying requester: requester = {}, request = {}", requester.getLogin(), requestRepresentation.getUuid());
        log.debug("Sending request rejection e-mail to requester '{}'", requester.getEmail());
        Context context = getDefaultContextForUser(requester);
        prepareSignature(context);
        context.setVariable("request", requestRepresentation);
        String content = templateEngine.process("requesterRequestRejected", context);
        String subject = getMessage(requester, "email.requesterRequestRejected.title",
                requestRepresentation.generateStringId());
        sendEmail(requester.getEmail(), subject, content, false, true);
    }

    /**
     * Send a notification email to the organisation informing them about a submitted request revision
     *
     * @param request the request that has been submitted.
     * @param organisation the organisation that is was submitted to
     * @param coordinators the list of organisation coordinators.
     */
    @Async
    public void sendRequestRevisionSubmissionNotificationToCoordinators(
        RequestRepresentation request, OrganisationRepresentation organisation, List<UserRepresentation> coordinators
    ) {
        log.info("Notifying coordinators: request = {}, organisation = {}, #coordinators = {}",
            request.getUuid(), organisation.getShortName(), coordinators == null ? null : coordinators.size());
        if (coordinators == null) {
            return;
        }
        for (UserRepresentation user: coordinators) {
            log.debug("Sending request revision e-mail to '{}'", user.getEmail());
            Context context = getDefaultContextForUser(user);
            prepareSignature(context);
            context.setVariable("request", request);
            context.setVariable("organisation", organisation);
            String content = templateEngine.process("organisationRequestRevisionSubmitted", context);
            String subject = getMessage(user, "email.organisationRequestRevisionSubmitted.title",
                    request.generateStringId());
            sendEmail(user.getEmail(), subject, content, false, true);
        }
    }

    @Async
    public void sendRequestReviewNotificationToReviewers(
        RequestRepresentation request, OrganisationRepresentation organisation, List<UserRepresentation> reviewers
    ) {
        log.info("Notifying organisation reviewers: request = {}, organisation = {}, #reviewers = {}",
            request.getUuid(), organisation.getShortName(), reviewers == null ? null : reviewers.size());
        if (reviewers == null) {
            return;
        }
        for (UserRepresentation user : reviewers) {
            log.debug("Sending review request e-mail to '{}'", user.getEmail());
            Context context = getDefaultContextForUser(user);
            prepareSignature(context);
            context.setVariable("request", request);
            context.setVariable("organisation", organisation);
            String content = templateEngine.process("reviewerRequestReview", context);
            String subject = getMessage(user, "email.reviewerRequestReview.title", request.generateStringId());
            sendEmail(user.getEmail(), subject, content, false, true);
        }
    }

    @Async
    public void sendRequestReviewedNotificationToCoordinators(
        RequestRepresentation request, OrganisationRepresentation organisation,
        List<UserRepresentation> coordinators, UserRepresentation reviewer
    ) {
        log.info("Notifying coordinators of request reviewed: request = {}, organisation = {}, #coordinators = {}",
            request.getUuid(), organisation.getShortName(), coordinators == null ? null : coordinators.size());
        if (coordinators == null) {
            return;
        }
        for (UserRepresentation user : coordinators) {
            log.debug("Sending request reviewed e-mail to '{}'", user.getEmail());
            Context context = getDefaultContextForUser(user);
            prepareSignature(context);
            context.setVariable("request", request);
            context.setVariable("reviewer", reviewer);
            String content = templateEngine.process("organisationRequestReviewed", context);
            String subject = getMessage(user, "email.organisationRequestReviewed.title",
                    request.generateStringId());
            sendEmail(user.getEmail(), subject, content, false, true);
        }
    }

    /**
     * Send a notification email to the requester that their request has been
     * approved.
     *
     * @param requester the requester details
     * @param request the request
     */
    @Async
    public void sendRequestApprovalNotificationToRequester(
        UserRepresentation requester, RequestRepresentation request
    ) {
        log.info("Notifying requester: requester = {}, request = {}", requester.getLogin(), request.getUuid());
        log.debug("Sending request approved e-mail to '{}'", requester.getEmail());
        Context context = getDefaultContextForUser(requester);
        prepareSignature(context);
        context.setVariable("request", request);
        String content = templateEngine.process("requesterRequestApproved", context);
        String subject = getMessage(requester, "email.requesterRequestApproved.title",
                request.generateStringId());
        sendEmail(requester.getEmail(), subject, content, false, true);
    }

    /**
     * Send a notification email to the requester that their request requires one or more
     * revisions.
     *
     * @param requester the requester details
     * @param request the request
     */
    @Async
    public void sendRequestRevisionNotificationToRequester(
        UserRepresentation requester, RequestRepresentation request
    ) {
        log.info("Notifying requester: requester = {}, request = {}", requester.getLogin(), request.getUuid());
        log.debug("Sending request revision e-mail to '{}'", requester.getEmail());
        Context context = getDefaultContextForUser(requester);
        prepareSignature(context);
        context.setVariable("request", request);
        String content = templateEngine.process("requesterRequestRevision", context);
        String subject = getMessage(requester, "email.requesterRequestRevision.title",
                request.generateStringId());
        sendEmail(requester.getEmail(), subject, content, false, true);
    }

    /**
     * Send a notification email to the requester that their delivery has been released.
     *
     * @param requester the requester details
     * @param request the request
     * @param deliveryProcess the delivery process.
     */
    @Async
    public void sendDeliveryReleasedNotificationToRequester(
        UserRepresentation requester, RequestRepresentation request, DeliveryProcessRepresentation deliveryProcess) {
        log.info("Notifying requester: requester = {}, delivery = {}", requester.getLogin(), deliveryProcess.getUuid());
        Context context = getDefaultContextForUser(requester);
        prepareSignature(context);
        context.setVariable("request", request);
        context.setVariable("deliveryProcess", deliveryProcess);
        String content = templateEngine.process("requesterDeliveryReleased", context);
        String subject = getMessage(requester, "email.requesterDeliveryReleased.title",
                request.generateStringId(),
                deliveryProcess.getType().name(),
                request.getOrganisations().get(0).getName());
        sendEmail(requester.getEmail(), subject, content, false, true);
    }

    /**
     * Send a notification email to the organisation coordinators that the delivery has been received
     * by the requester.
     *
     * @param request the request
     * @param deliveryProcess the delivery process.
     * @param organisation the organisation that is was submitted to
     * @param coordinators the list of organisation coordinators.
     */
    @Async
    public void sendDeliveryReceivedNotificationToCoordinators(
        RequestRepresentation request, DeliveryProcessRepresentation deliveryProcess,
        OrganisationRepresentation organisation, List<UserRepresentation> coordinators) {
        log.info("Notifying coordinators: delivery = {}, organisation = {}, #coordinators = {}",
            deliveryProcess.getUuid(), organisation.getShortName(), coordinators == null ? null : coordinators.size());
        if (coordinators == null) {
            return;
        }
        for (UserRepresentation user: coordinators) {
            log.debug("Sending delivery received e-mail to '{}'", user.getEmail());
            Context context = getDefaultContextForUser(user);
            prepareSignature(context);
            context.setVariable("request", request);
            context.setVariable("deliveryProcess", deliveryProcess);
            context.setVariable("organisation", organisation);
            String content = templateEngine.process("organisationDeliveryReceived", context);
            String subject = getMessage(user, "email.organisationDeliveryReceived.title",
                    request.generateStringId(),
                    deliveryProcess.getType().name());
            sendEmail(user.getEmail(), subject, content, false, true);
        }
    }

    /**
     * Send a notification email to the requester that their request has been closed.
     *
     * @param requester the requester details
     * @param request the request
     */
    @Async
    public void sendRequestClosedNotificationToRequester(UserRepresentation requester, RequestRepresentation request) {
        log.info("Notifying requester: requester = {}, request = {}", requester.getLogin(), request.getUuid());
        Context context = getDefaultContextForUser(requester);
        prepareSignature(context);
        context.setVariable("request", request);
        String content = templateEngine.process("requesterRequestClosed", context);
        String subject = getMessage(requester, "email.requesterRequestClosed.title",
                request.generateStringId(),
                request.getOrganisations().get(0).getName());
        sendEmail(requester.getEmail(), subject, content, false, true);
    }

    /**
     * Send a notification email to the requester that the delivery has been cancelled.
     *
     * @param request the request.
     * @param deliveryProcess the delivery process.
     * @param user the requester.
     */
    @Async
    public void sendDeliveryCancelledNotificationToRequester(RequestRepresentation request, DeliveryProcessRepresentation deliveryProcess, UserRepresentation user) {
        log.info("Notifying requester: delivery = {}, requester = {}",
            deliveryProcess.getUuid(), user.getLogin());
        log.debug("Sending delivery received e-mail to '{}'", user.getEmail());
        Context context = getDefaultContextForUser(user);
        prepareSignature(context);
        context.setVariable("request", request);
        context.setVariable("deliveryProcess", deliveryProcess);
        String content = templateEngine.process("requesterDeliveryCancelled", context);
        String subject = getMessage(user, "email.requesterDeliveryCancelled.title",
                request.generateStringId(), deliveryProcess.getType().name(),
                request.getOrganisations().get(0).getName());
        sendEmail(user.getEmail(), subject, content, false, true);
    }

}
