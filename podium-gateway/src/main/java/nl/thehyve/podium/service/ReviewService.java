package nl.thehyve.podium.service;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.enumeration.RequestOutcome;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.enumeration.ReviewProcessOutcome;
import nl.thehyve.podium.common.exceptions.AccessDenied;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.common.service.dto.ReviewFeedbackRepresentation;
import nl.thehyve.podium.common.service.dto.ReviewRoundRepresentation;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.domain.ReviewFeedback;
import nl.thehyve.podium.domain.ReviewRound;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.repository.ReviewFeedbackRepository;
import nl.thehyve.podium.repository.search.ReviewFeedbackSearchRepository;
import nl.thehyve.podium.service.mapper.RequestMapper;
import nl.thehyve.podium.service.mapper.ReviewFeedbackMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Implementation for managing request reviews.
 */
@Service
@Transactional
@Timed
public class ReviewService {

    private final Logger log = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestMapper requestMapper;

    @Autowired
    private ReviewFeedbackMapper reviewFeedbackMapper;

    @Autowired
    private ReviewFeedbackRepository reviewFeedbackRepository;

    @Autowired
    private ReviewFeedbackSearchRepository reviewFeedbackSearchRepository;

    @Autowired
    private RequestReviewProcessService requestReviewProcessService;

    @Autowired
    private ReviewRoundService reviewRoundService;

    @Autowired
    private StatusUpdateEventService statusUpdateEventService;

    @Autowired
    private EntityManager entityManager;

    /**
     * Validating the request by uuid. If successful, the request will change to review status 'Review'.
     *
     * @param user the current user, validating the request
     * @param uuid the uuid of the request
     * @return the updated request
     * @throws ActionNotAllowed if the request is not in status 'Review' with review status 'Validation'.
     */
    public RequestRepresentation validateRequest(AuthenticatedUser user, UUID uuid) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(uuid);
        RequestReviewStatus sourceReviewStatus = AccessCheckHelper.checkReviewStatus(request, RequestReviewStatus.Validation);
        AccessCheckHelper.checkOrganisationAccess(user, request.getOrganisations(), AuthorityConstants.ORGANISATION_COORDINATOR);

        log.debug("Submitting request for review: {}", uuid);
        requestReviewProcessService.submitForReview(user, request.getRequestReviewProcess());

        request = requestRepository.findOneByUuid(uuid);

        // Once successfully started initiate review round and review feedback processes.
        ReviewRound reviewRound = reviewRoundService.createReviewRoundForRequest(request);
        request.getReviewRounds().add(reviewRound);

        requestRepository.save(request);

        statusUpdateEventService.publishReviewStatusUpdate(user, sourceReviewStatus, request, null);
        return requestMapper.extendedRequestToRequestDTO(request);
    }

    public RequestRepresentation rejectRequest(
        AuthenticatedUser user, UUID uuid, MessageRepresentation message
    ) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(uuid);

        RequestStatus sourceStatus = AccessCheckHelper.checkStatus(request, RequestStatus.Review);
        RequestReviewStatus sourceReviewStatus = AccessCheckHelper.checkReviewStatus(request, RequestReviewStatus.Validation, RequestReviewStatus.Review);
        AccessCheckHelper.checkOrganisationAccess(user, request.getOrganisations(), AuthorityConstants.ORGANISATION_COORDINATOR);

        // Reject the request
        requestReviewProcessService.reject(user, request.getRequestReviewProcess());

        // Finalize a potentially available review round
        reviewRoundService.finalizeReviewRoundForRequest(request);

        request = requestRepository.findOneByUuid(uuid);
        statusUpdateEventService.publishReviewStatusUpdate(user, sourceReviewStatus, request, message);

        if (request.getRequestReviewProcess().getStatus() == RequestReviewStatus.Closed &&
            request.getRequestReviewProcess().getDecision() == ReviewProcessOutcome.Rejected) {
            request.setStatus(RequestStatus.Closed);
            request.setOutcome(RequestOutcome.Rejected);
            request = requestRepository.save(request);
            statusUpdateEventService.publishStatusUpdate(user, sourceStatus, request, null);
        }

        return requestMapper.extendedRequestToRequestDTO(request);
    }

    public RequestRepresentation approveRequest(AuthenticatedUser user, UUID uuid) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(uuid);

        RequestStatus sourceStatus = AccessCheckHelper.checkStatus(request, RequestStatus.Review);
        RequestReviewStatus sourceReviewStatus = AccessCheckHelper.checkReviewStatus(request, RequestReviewStatus.Review);
        AccessCheckHelper.checkOrganisationAccess(user, request.getOrganisations(), AuthorityConstants.ORGANISATION_COORDINATOR);

        // Approve the request
        requestReviewProcessService.approve(user, request.getRequestReviewProcess());

        request = requestRepository.findOneByUuid(uuid);
        statusUpdateEventService.publishReviewStatusUpdate(user, sourceReviewStatus, request, null);

        if (request.getRequestReviewProcess().getStatus() == RequestReviewStatus.Closed &&
            request.getRequestReviewProcess().getDecision() == ReviewProcessOutcome.Approved) {
            request.setStatus(RequestStatus.Approved);
            request = requestRepository.save(request);
            statusUpdateEventService.publishStatusUpdate(user, sourceStatus, request, null);
        }

        // Finalize a potentially available review round
        reviewRoundService.finalizeReviewRoundForRequest(request);

        return requestMapper.extendedRequestToRequestDTO(request);
    }

    public RequestRepresentation requestRevision(
        AuthenticatedUser user, UUID uuid, MessageRepresentation message
    ) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(uuid);

        RequestReviewStatus sourceReviewStatus = AccessCheckHelper.checkReviewStatus(request, RequestReviewStatus.Validation, RequestReviewStatus.Review);
        AccessCheckHelper.checkOrganisationAccess(user, request.getOrganisations(), AuthorityConstants.ORGANISATION_COORDINATOR);

        // Request revision by the requester
        requestReviewProcessService.requestRevision(user, request.getRequestReviewProcess());

        request = requestRepository.findOneByUuid(uuid);

        // Finalize a potentially available review round
        reviewRoundService.finalizeReviewRoundForRequest(request);

        statusUpdateEventService.publishReviewStatusUpdate(user, sourceReviewStatus, request, message);
        return requestMapper.extendedRequestToRequestDTO(request);
    }

    /**
     * Save the review feedback for the current review round in a request.
     *
     * @param user the currently logged in user.
     * @param requestUuid the uuid of the request the review belongs to.
     * @param feedbackBody the feedback supplied by the reviewer.
     *
     * @return ReviewFeedback the updated review feedback.
     * @throws AccessDenied if the currently logged in user is not the owner of the feedback.
     * @throws ActionNotAllowed when the request is not in status 'Review', the feedback is not part of the request, or
     * the feedback has already been saved before.
     */
    @Timed
    public RequestRepresentation saveReviewFeedback(
        AuthenticatedUser user,
        UUID requestUuid,
        ReviewFeedbackRepresentation feedbackBody
    ) throws ActionNotAllowed {
        log.debug("Saving review feedback for {}", feedbackBody.getUuid());

        Request request = requestRepository.findOneByUuid(requestUuid);
        AccessCheckHelper.checkStatus(request, RequestStatus.Review);

        final UUID feedbackUuid = feedbackBody.getUuid();

        if (request.getReviewRounds() == null || request.getReviewRounds().isEmpty()) {
            throw new RuntimeException("No review rounds found for request " + requestUuid.toString());
        }
        ReviewRound currentReviewRound = request.getReviewRounds().get(request.getReviewRounds().size() - 1);

        // Check whether the feedback is part of the request.
        if (currentReviewRound.getReviewFeedback().stream().noneMatch(reviewFeedback ->
            reviewFeedback.getUuid().equals(feedbackUuid))) {
            throw new ActionNotAllowed(
                String.format("Review feedback (%s) is not part of the current review round of request (%s)",
                    feedbackUuid,
                    request.getUuid())
            );
        }

        ReviewFeedback feedback = reviewFeedbackRepository.findOneByUuid(feedbackUuid);
        if (feedback == null) {
            throw new ResourceNotFound("Review feedback could not be found for " + feedbackUuid.toString());
        }
        // Check if the current user is the owner of the review feedback
        if (!user.getUuid().equals(feedback.getReviewer())) {
            log.error("Current user ({}) is not the owner ({}) of the review feedback ({}).",
                user.getUuid(), feedback.getReviewer(), feedback.getUuid()
            );
            throw new AccessDenied("Current user is not the review feedback assignee.");
        }
        // Check if the review feedback has not already been saved before
        if (feedback.getAdvice() != ReviewProcessOutcome.None) {
            throw new ActionNotAllowed(
                String.format("Review feedback (%s) has already been saved.", feedbackUuid)
            );
        }

        feedback = reviewFeedbackMapper.safeUpdateReviewFeedbackFromDTO(feedbackBody, feedback);
        feedback = reviewFeedbackRepository.save(feedback);
        reviewFeedbackSearchRepository.save(feedback);
        entityManager.flush();
        entityManager.refresh(request);
        return requestMapper.extendedRequestToRequestDTO(request);
    }

}
