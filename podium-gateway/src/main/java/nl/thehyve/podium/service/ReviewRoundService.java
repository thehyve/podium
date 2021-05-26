/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.service;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.exceptions.ServiceNotAvailable;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.domain.ReviewFeedback;
import nl.thehyve.podium.domain.ReviewRound;
import nl.thehyve.podium.repository.ReviewFeedbackRepository;
import nl.thehyve.podium.repository.ReviewRoundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ReviewRoundService {

    private final Logger log = LoggerFactory.getLogger(ReviewRoundService.class);

    @Autowired
    private OrganisationClientService organisationClientService;

    @Autowired
    private ReviewRoundRepository reviewRoundRepository;

    @Autowired
    private ReviewFeedbackRepository reviewFeedbackRepository;

    @Autowired
    private SecurityService securityService;

    /**
     * Create a new review round for the given request.
     * Each reviewer that is assigned in all the organisations belonging to the request will be assigned a
     * review feedback, such that they can give their advice.
     *
     * All the reviewers are retrieved from the podiumUAA service.
     *
     * @param request The request to create the reviewround for.
     * @return ReviewRound with all the assigned reviewers.
     */
    @Timed
    public ReviewRound createReviewRoundForRequest(Request request) {
        log.debug("Creating review round for request {}", request.getUuid());

        // Create new review round
        ReviewRound reviewRound = new ReviewRound();
        reviewRound.setRequestDetail(request.getRequestDetail());

        // Set initiator
        AuthenticatedUser user = securityService.getCurrentUser();
        reviewRound.setInitiatedBy(user.getUuid());

        List<UserRepresentation> reviewers = new ArrayList<>();
        List<ReviewFeedback> reviewFeedbacks = new ArrayList<>();
        reviewRound.setReviewFeedback(reviewFeedbacks);

        // Retrieve all reviewers for the organisation
        for(UUID organisationUuid: request.getOrganisations()) {

            try {
                // Fetch the organisation object and filter the organisation supported request types.
                List<UserRepresentation> users
                    = organisationClientService.findUsersByRole(organisationUuid, AuthorityConstants.REVIEWER);
                reviewers.addAll(users);

            } catch (Exception e) {
                log.error("Error fetching organisation reviewers", e);
                throw new ServiceNotAvailable("Could not fetch organisation reviewers", e);
            }
        }

        // Create review feedback for every reviewer available.
        for (UserRepresentation reviewer: reviewers) {
            log.debug("Adding reviewer {} to reviewRound for request {}", reviewer.getUuid(), request.getUuid());
            ReviewFeedback feedback = new ReviewFeedback();
            feedback.setReviewer(reviewer.getUuid());

            // Set date of initialization. Update when feedback is given.
            feedback.setDate(ZonedDateTime.now());

            reviewFeedbackRepository.save(feedback);

            // Add the feedback to the round.
            reviewRound.getReviewFeedback().add(feedback);
        }

        reviewRoundRepository.save(reviewRound);

        return reviewRound;
    }

    public Request finalizeReviewRoundForRequest(Request request) {
        log.debug("Finalizing review round for request {}", request.getUuid());

        // Get last review round
        List<ReviewRound> reviewRounds = request.getReviewRounds();

        if (!reviewRounds.isEmpty()) {
            ReviewRound reviewRound = reviewRounds.get(reviewRounds.size() - 1);

            // Finalize an open review round.
            if (reviewRound != null && reviewRound.getEndDate() == null) {
                reviewRound.setEndDate(LocalDateTime.now());
                reviewRoundRepository.save(reviewRound);
            }
        }

        return request;
    }


}
