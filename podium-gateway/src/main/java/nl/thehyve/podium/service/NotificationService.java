package nl.thehyve.podium.service;

import nl.thehyve.podium.common.exceptions.ServiceNotAvailable;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.service.representation.RequestRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service responsible for sending notifications and fetching the required data for the notifications.
 */
@Service
public class NotificationService {

    private Logger log  = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private OrganisationClientService organisationClientService;

    @Autowired
    private UserClientService userClientService;

    @Autowired
    private MailService mailService;

    /**
     * Notify the requester about the submission of a new request.
     * @param user the requester
     * @param organisationRequests the list of organisation requests generated at request submission.
     */
    @Async
    public void submissionNotificationToRequester(AuthenticatedUser user, List<Request> organisationRequests) {
        // Fetch requester data through Feign.
        UserRepresentation requester = this.fetchUserThroughFeign(user.getUuid());

        Map<UUID, OrganisationDTO> organisations = new HashMap<>();
        try {
            for (Request request: organisationRequests) {
                for (UUID organisationUuid : request.getOrganisations()) {
                    if (!organisations.containsKey(organisationUuid)) {
                        organisations.put(organisationUuid, organisationClientService.findOrganisationByUuid(organisationUuid));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching organisation details", e);
            throw new ServiceNotAvailable("Could not fetch organisation details", e);
        }
        mailService.sendSubmissionNotificationToRequester(requester, organisationRequests, organisations);
    }

    /**
     * Notify organisation coordinators about the submission of a new request.
     * @param organisation the organisation DTO object
     * @param organisationRequest the submitted request object
     */
    @Async
    public void submissionNotificationToCoordinators(OrganisationDTO organisation, Request organisationRequest) {
        // Fetch organisation and organisation coordinators through Feign.
        List<UserRepresentation> coordinators
            = this.fetchOrganisationUsersByRoleThroughFeign(organisation.getUuid(), AuthorityConstants.ORGANISATION_COORDINATOR);

        mailService.sendSubmissionNotificationToCoordinators(organisationRequest, organisation, coordinators);
    }

    /**
     * Notify requester about the rejection of their request.
     * @param user the authenticated user
     * @param requestRepresentation the request object
     */
    public void rejectionNotificationToRequester(AuthenticatedUser user, RequestRepresentation requestRepresentation) {
        // Fetch requester data through Feign.
        UserRepresentation requester = this.fetchUserThroughFeign(user.getUuid());

        mailService.sendRejectionNotificationToRequester(requester, requestRepresentation);
    }

    /**
     * Notify organisation coordinators about the submission of a revised request.
     * @param organisation the organisation DTO object
     * @param organisationRequest the submitted request object
     */
    @Async
    public void revisionNotificationToCoordinators(OrganisationDTO organisation, Request organisationRequest) {
        // Fetch organisation coordinators through Feign.
        List<UserRepresentation> coordinators
            = this.fetchOrganisationUsersByRoleThroughFeign(organisation.getUuid(), AuthorityConstants.ORGANISATION_COORDINATOR);

        mailService.sendRequestRevisionNotificationToCoordinators(organisationRequest, organisation, coordinators);
    }

    /**
     * Notify organisation reviewers about an available request to review.
     *
     * @param organisation The organisation representation
     * @param reviewRequest The request to be reviewed
     */
    @Async
    public void reviewNotificationToReviewers(OrganisationDTO organisation, Request reviewRequest) {
        // Fetch organisation reviewers through Feign.
        List<UserRepresentation> reviewers
            = this.fetchOrganisationUsersByRoleThroughFeign(organisation.getUuid(), AuthorityConstants.REVIEWER);

        mailService.sendRequestReviewNotificationToReviewers(reviewRequest, organisation, reviewers);
    }

    /**
     * Notify the requester about the approval of a their request.
     * @param user the requester
     * @param organisationRequests the list of organisation requests generated at request submission.
     */
    @Async
    public void approvalNotificationToRequester(AuthenticatedUser user, RequestRepresentation requestRepresentation) {
        // Fetch requester data through Feign.
        UserRepresentation requester = this.fetchUserThroughFeign(user.getUuid());

        mailService.sendRequestApprovalNotificationToRequester(requester, requestRepresentation);
    }


    private UserRepresentation fetchUserThroughFeign(UUID userUuid) {
        try {
            return userClientService.findUserByUuid(userUuid);
        } catch (Exception e) {
            log.error("Error fetching requester details", e);
            throw new ServiceNotAvailable("Could not fetch requester details", e);
        }
    }

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
