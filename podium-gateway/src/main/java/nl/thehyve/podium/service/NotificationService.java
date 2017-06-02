package nl.thehyve.podium.service;

import nl.thehyve.podium.common.exceptions.ServiceNotAvailable;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service responsible for sending notifications and fetching the required data for the notifications.
 */
@Service
@Transactional(readOnly = true)
public class NotificationService {

    private Logger log  = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private OrganisationClientService organisationClientService;

    @Autowired
    private UserClientService userClientService;

    @Autowired
    private MailService mailService;

    private RequestService requestService;

    /**
     * RequestService is injected by the service post construct.
     * @param requestService the request service.
     * @see RequestService
     */
    public void setRequestService(RequestService requestService) {
        this.requestService = requestService;
    }

    /**
     * Notify the requester about the submission of a new request.
     * @param user the requester
     * @param organisationRequests the list of organisation requests generated at request submission.
     */
    @Async
    public void submissionNotificationToRequester(AuthenticatedUser user, List<RequestRepresentation> organisationRequests) {
        // Fetch requester data through Feign.
        UserRepresentation requester = this.fetchUserThroughFeign(user.getUuid());
        mailService.sendSubmissionNotificationToRequester(requester, organisationRequests);
    }

    /**
     * Notify organisation coordinators about the submission of a new request.
     * @param requestUuid The uuid of the request
     */
    @Async
    public void submissionNotificationToCoordinators(UUID requestUuid) {
        RequestRepresentation request = requestService.findRequest(requestUuid);
        for(OrganisationDTO organisation: request.getOrganisations()) {
            // Fetch organisation coordinators through Feign.
            List<UserRepresentation> coordinators
                = this.fetchOrganisationUsersByRoleThroughFeign(organisation.getUuid(), AuthorityConstants.ORGANISATION_COORDINATOR);
            mailService.sendSubmissionNotificationToCoordinators(request, organisation, coordinators);
        }
    }

    /**
     * Notify requester about the approval or rejection of their request.
     * @param requestUuid The uuid of the request
     */
    @Async
    public void reviewProcessClosedNotificationToRequester(UUID requestUuid) {
        RequestRepresentation request = requestService.findRequest(requestUuid);
        // Fetch requester data through Feign.
        UserRepresentation requester = this.fetchUserThroughFeign(request.getRequester().getUuid());

        switch (request.getRequestReview().getDecision()) {
            case Approved:
                mailService.sendRequestApprovalNotificationToRequester(requester, request);
                break;
            case Rejected:
                mailService.sendRejectionNotificationToRequester(requester, request);
                break;
            default:
                log.error("Unexpected review process outcome for request {}: {}. No notification sent.",
                    requestUuid, request.getRequestReview().getDecision());
        }
    }

    /**
     * Notify organisation coordinators about the submission of a revised request.
     * @param requestUuid The uuid of the request
     */
    @Async
    public void revisionNotificationToCoordinators(UUID requestUuid) {
        RequestRepresentation request = requestService.findRequest(requestUuid);
        for(OrganisationDTO organisation: request.getOrganisations()) {
            // Fetch organisation coordinators through Feign.
            List<UserRepresentation> coordinators
                = this.fetchOrganisationUsersByRoleThroughFeign(organisation.getUuid(), AuthorityConstants.ORGANISATION_COORDINATOR);

            mailService.sendRequestRevisionSubmissionNotificationToCoordinators(request, organisation, coordinators);
        }
    }

    /**
     * Notify organisation reviewers about an available request to review.
     *
     * @param requestUuid The uuid of the request to be reviewed
     */
    @Async
    public void reviewNotificationToReviewers(UUID requestUuid) {
        RequestRepresentation request = requestService.findRequest(requestUuid);
        for(OrganisationDTO organisation: request.getOrganisations()) {
            // Fetch organisation reviewers through Feign.
            List<UserRepresentation> reviewers
                = this.fetchOrganisationUsersByRoleThroughFeign(organisation.getUuid(), AuthorityConstants.REVIEWER);

            mailService.sendRequestReviewNotificationToReviewers(request, organisation, reviewers);
        }
    }

    /**
     * Notify the requester that their request requires one or more revisions.
     * @param requestUuid The uuid of the request
     */
    @Async
    public void revisionNotificationToRequester(UUID requestUuid) {
        RequestRepresentation request = requestService.findRequest(requestUuid);
        // Fetch requester data through Feign.
        UserRepresentation requester = this.fetchUserThroughFeign(request.getRequester().getUuid());

        mailService.sendRequestRevisionNotificationToRequester(requester, request);
    }

    /**
     * Fetch a user representation by UUID through feign.
     *
     * @param userUuid the UUID of the user
     * @return UserRepresentation the representation object of the user
     */
    private UserRepresentation fetchUserThroughFeign(UUID userUuid) {
        try {
            return userClientService.findUserByUuid(userUuid);
        } catch (Exception e) {
            log.error("Error fetching requester details", e);
            throw new ServiceNotAvailable("Could not fetch requester details", e);
        }
    }

    /**
     * Fetch all users from an organisation with a specific role
     *
     * @param uuid The UUID of the organisation
     * @param authority The authority that the users are required to have
     * @return List of user representations
     */
    private List<UserRepresentation> fetchOrganisationUsersByRoleThroughFeign(UUID uuid, String authority) {
        // Fetch organisation users by role through Feign.
        List<UserRepresentation> users;
        try {
            users = organisationClientService.findUsersByRole(uuid,
                authority);
        } catch (Exception e) {
            log.error("Error fetching organisation users", e);
            throw new ServiceNotAvailable("Could not fetch organisation users", e);
        }

        return users;
    }

}
