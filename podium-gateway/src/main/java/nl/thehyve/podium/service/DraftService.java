package nl.thehyve.podium.service;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Sets;
import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.exceptions.ServiceNotAvailable;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.domain.PrincipalInvestigator;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.domain.RequestDetail;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.service.mapper.RequestDetailMapper;
import nl.thehyve.podium.service.mapper.RequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service Implementation for managing request drafts.
 */
@Service
@Transactional
@Timed
public class DraftService {

    private final Logger log = LoggerFactory.getLogger(DraftService.class);

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestMapper requestMapper;

    @Autowired
    private RequestDetailMapper requestDetailMapper;

    @Autowired
    private RequestReviewProcessService requestReviewProcessService;

    @Autowired
    private OrganisationClientService organisationClientService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private StatusUpdateEventService statusUpdateEventService;

    /**
     * Create a new draft request.
     *
     * @param user the current user (the requester).
     * @return saved request representation
     */
    public RequestRepresentation createDraft(IdentifiableUser user) {
        Request request = new Request();
        request.setStatus(RequestStatus.Draft);
        request.setRequester(user.getUserUuid());
        RequestDetail requestDetail = new RequestDetail();
        requestDetail.setPrincipalInvestigator(new PrincipalInvestigator());
        request.setRequestDetail(requestDetail);
        requestRepository.save(request);
        return requestMapper.requestToRequestDTO(request);
    }

    /**
     * Updates the draft request with the properties in the body.
     * The request to update is fetched based on the uuid in the body.
     *
     * @param user the current user
     * @param body the updated properties.
     * @return the updated draft request
     * @throws ActionNotAllowed if the request is not in status 'Draft'.
     */
    public RequestRepresentation updateDraft(IdentifiableUser user, RequestRepresentation body) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(body.getUuid());

        AccessCheckHelper.checkRequester(user, request);
        AccessCheckHelper.checkStatus(request, RequestStatus.Draft);

        request = requestMapper.updateRequestDTOToRequest(body, request);
        requestRepository.save(request);
        return requestMapper.requestToRequestDTO(request);
    }

    /**
     * Updates the request revision with the properties in the body.
     * The request to update is fetched based on the uuid in the body.
     * Only allowed when a request has the review status Revision.
     *
     * @param user the current user
     * @param body the updated properties.
     * @return the updated request
     * @throws ActionNotAllowed if the request is not in review status 'Revision'.
     */
    public RequestRepresentation updateRevision(IdentifiableUser user, RequestRepresentation body) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(body.getUuid());

        AccessCheckHelper.checkRequester(user, request);
        AccessCheckHelper.checkReviewStatus(request, RequestReviewStatus.Revision);

        requestDetailMapper.processingRequestDetailDtoToRequestDetail(body.getRevisionDetail(), request.getRevisionDetail());

        request = requestRepository.save(request);
        return requestMapper.extendedRequestToRequestDTO(request);
    }

    /**
     * Submit the draft request by uuid.
     * Generates requests for the organisations specified in the draft.
     *
     * @param user the current user, submitting the request
     * @param uuid the uuid of the draft request
     * @return the list of generated requests to organisations.
     * @throws ActionNotAllowed if the request is not in status 'Draft'.
     */
    public List<RequestRepresentation> submitDraft(AuthenticatedUser user, UUID uuid) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(uuid);

        log.debug("Access and status checks...");
        AccessCheckHelper.checkRequester(user, request);
        RequestStatus sourceStatus = AccessCheckHelper.checkStatus(request, RequestStatus.Draft);

        RequestRepresentation requestData = requestMapper.requestToRequestDTO(request);

        log.debug("Validating request data.");
        RequestService.validateRequest(requestData);

        log.debug("Submitting request : {}", uuid);
        List<Request> organisationRequests = new ArrayList<>();
        // TODO: Aggregate mails for multiple organisations per user.

        for (UUID organisationUuid: request.getOrganisations()) {
            Request organisationRequest = requestMapper.clone(request);

            // Create organisation revision details
            RequestDetail revisionDetail = requestDetailMapper.clone(request.getRequestDetail());
            organisationRequest.setRevisionDetail(revisionDetail);

            try {
                Set<RequestType> selectedRequestTypes = organisationRequest.getRequestDetail().getRequestType();

                // Fetch the organisation object and filter the organisation supported request types.
                OrganisationDTO organisationDTO = organisationClientService.findOrganisationByUuid(organisationUuid);
                Set<RequestType> organisationRequestTypes = organisationDTO.getRequestTypes();

                Set<RequestType> organisationSupportedRequestTypes
                    = Sets.intersection(selectedRequestTypes, organisationRequestTypes);

                // Set the by the organisation supported request types for this request.
                organisationRequest.getRequestDetail().setRequestType(organisationSupportedRequestTypes);
                organisationRequest.getRevisionDetail().setRequestType(organisationSupportedRequestTypes);
            } catch (Exception e) {
                log.error("Error fetching organisation", e);
                throw new ServiceNotAvailable("Could not fetch organisation", e);
            }

            organisationRequest.setOrganisations(
                new HashSet<>(Collections.singleton(organisationUuid)));
            organisationRequest.setStatus(RequestStatus.Review);
            organisationRequest.setRequestReviewProcess(
                requestReviewProcessService.start(user));
            organisationRequest = requestRepository.save(organisationRequest);

            statusUpdateEventService.publishStatusUpdate(user, sourceStatus, organisationRequest, null);

            organisationRequests.add(organisationRequest);
            log.debug("Created new submitted request for organisation {}.", organisationUuid);
        }

        List<RequestRepresentation> result = requestMapper.extendedRequestsToRequestDTOs(organisationRequests);
        notificationService.submissionNotificationToRequester(user, result);

        log.debug("Deleting draft request.");
        requestService.deleteRequest(request.getId());
        return result;
    }

}
