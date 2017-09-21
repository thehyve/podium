package nl.thehyve.podium.service;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Sets;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.common.enumeration.OverviewStatus;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.exceptions.InvalidRequest;
import nl.thehyve.podium.common.exceptions.ServiceNotAvailable;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.service.dto.ExternalRequestRepresentation;
import nl.thehyve.podium.common.service.dto.OrganisationRepresentation;
import nl.thehyve.podium.common.service.dto.RequestDetailRepresentation;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.domain.PrincipalInvestigator;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.domain.RequestDetail;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.security.RequestAccessCheckHelper;
import nl.thehyve.podium.service.mapper.RequestDetailMapper;
import nl.thehyve.podium.service.mapper.RequestMapper;
import nl.thehyve.podium.service.util.OrganisationMapperHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private EntityManager entityManager;

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
        requestDetail.setCombinedRequest(false);
        requestDetail.setPrincipalInvestigator(new PrincipalInvestigator());
        request.setRequestDetail(requestDetail);
        requestRepository.save(request);
        return requestMapper.detailedRequestToRequestDTO(request);
    }

    /**
     * Create a new draft based on external request
     *
     * @param user                          the current user (the requester).
     * @param externalRequestRepresentation external request
     * @param missingOrganisationUUIDs      missing organisation UUIDs
     * @return saved request representation
     */
    public RequestRepresentation createDraftFromExternalRequest(IdentifiableUser user, ExternalRequestRepresentation
        externalRequestRepresentation, List<Map<String, String>> missingOrganisationUUIDs) {
        Request request = new Request();
        request.setStatus(RequestStatus.Draft);
        request.setRequester(user.getUserUuid());
        RequestDetail requestDetail = new RequestDetail();
        requestDetail.setCombinedRequest(false);
        requestDetail.setPrincipalInvestigator(new PrincipalInvestigator());
        requestDetail.setSearchQuery(externalRequestRepresentation.getHumanReadable());
        List<Map<String, String>> collections = externalRequestRepresentation.getCollections();

        // Get the String id's from the exteral request and turn them into a list of relevant organisations
        Set<UUID> organisationUUIDs = new HashSet<>();

        for (Map<String, String> collection : collections) {
            String biobankId = collection.get("biobankID");
            try {
                UUID biobankUUID = UUID.fromString(collection.get("biobankID"));
                log.debug("Checking for organization", biobankId);

                OrganisationRepresentation organisationRepresentation =
                    organisationClientService.findOrganisationByUuidCached(biobankUUID);

                if (organisationRepresentation.getActivated()) {
                    organisationUUIDs.add(organisationRepresentation.getUuid());
                } else {
                    missingOrganisationUUIDs.add(createMissingOrganisationError(biobankId, "Organisation for the " +
                        "given id: " + biobankId + " is inactive"));
                }
            } catch (IllegalArgumentException e) {
                missingOrganisationUUIDs.add(
                    createMissingOrganisationError(biobankId, e.getMessage()));
            } catch (HystrixRuntimeException e) {
                missingOrganisationUUIDs.add(
                    createMissingOrganisationError(biobankId, "Cannot find an organization for the given id: " +
                        biobankId)
                );
            }
        }

        request.setOrganisations(organisationUUIDs);
        Set<RequestType> allTypes = new HashSet<>(Arrays.asList(RequestType.Data, RequestType.Images,
            RequestType.Material));
        requestDetail.setRequestType(allTypes);
        request.setRequestDetail(requestDetail);
        request.setRequestDetail(requestDetail);
        requestRepository.save(request);
        return requestMapper.detailedRequestToRequestDTO(request);
    }

    /**
     * Create missing organisation error
     *
     * @param uuid         organisation uuid
     * @param errorMessage error message
     * @return error
     */
    private Map<String, String> createMissingOrganisationError(String uuid, String errorMessage) {
        Map<String, String> error = new HashMap<>();
        error.put("organisationId", uuid);
        error.put("errorMessage", errorMessage);
        log.debug("Organisation {0} is missing due to {1}", uuid, errorMessage);
        return error;
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

        RequestAccessCheckHelper.checkRequester(user, request);
        RequestAccessCheckHelper.checkStatus(request, RequestStatus.Draft);

        request.setOrganisations(new HashSet<>(OrganisationMapperHelper.organisationDTOsToUuids(body.getOrganisations())));
        requestDetailMapper.processingRequestDetailDtoToRequestDetail(body.getRequestDetail(), request.getRequestDetail());
        request.getRequestDetail().setRequestType(body.getRequestDetail().getRequestType());
        request.getRequestDetail().setCombinedRequest(body.getRequestDetail().getCombinedRequest());

        requestRepository.save(request);
        return requestMapper.detailedRequestToRequestDTO(request);
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

        RequestAccessCheckHelper.checkRequester(user, request);
        RequestAccessCheckHelper.checkReviewStatus(request, RequestReviewStatus.Revision);

        requestDetailMapper.processingRequestDetailDtoToRequestDetail(body.getRevisionDetail(), request.getRevisionDetail());

        request = requestRepository.save(request);
        return requestMapper.detailedRequestToRequestDTO(request);
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

        // Organisations should be selected during the process before organisation requests can be created.
        if (request.getOrganisations().isEmpty()) {
            throw new InvalidRequest("No organisations selected.");
        }

        log.debug("Access and status checks...");
        RequestAccessCheckHelper.checkRequester(user, request);
        RequestAccessCheckHelper.checkStatus(request, RequestStatus.Draft);
        OverviewStatus sourceStatus = request.getOverviewStatus();

        RequestDetailRepresentation requestData = requestDetailMapper.requestDetailToRequestDetailRepresentation(request.getRequestDetail());

        log.debug("Validating request data.");
        RequestService.validateRequest(requestData);

        log.debug("Submitting request : {}", uuid);

        final List<Request> organisationRequests = new ArrayList<>();

        for (UUID organisationUuid: request.getOrganisations()) {
            Request organisationRequest = requestMapper.clone(request);

            // Create organisation revision details
            RequestDetail revisionDetail = requestDetailMapper.clone(request.getRequestDetail());
            organisationRequest.setRevisionDetail(revisionDetail);

            try {
                Set<RequestType> selectedRequestTypes = organisationRequest.getRequestDetail().getRequestType();

                // Fetch the organisation object and filter the organisation supported request types.
                OrganisationRepresentation organisationRepresentation = organisationClientService.findOrganisationByUuid(organisationUuid);
                Set<RequestType> organisationRequestTypes = organisationRepresentation.getRequestTypes();

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

            organisationRequests.add(organisationRequest);

            log.debug("Created new submitted request for organisation {}.", organisationUuid);
        }
        entityManager.flush();

        for (Request organisationRequest: organisationRequests) {
            entityManager.refresh(organisationRequest);
        }

        // Setting links to related requests in every request
        for (Request organisationRequest: organisationRequests) {
            log.debug("Saving related requests for request {}", organisationRequest.getUuid());
            Set<Request> relatedRequests = new HashSet<>(organisationRequests.stream().filter(req ->
                req != organisationRequest
            ).collect(Collectors.toSet()));
            organisationRequest.setRelatedRequests(relatedRequests);
            entityManager.persist(organisationRequest);
        }
        entityManager.flush();
        log.debug("Done saving related requests.");

        // Publish status update event for every generated request
        for (Request organisationRequest: organisationRequests) {
            statusUpdateEventService.publishStatusUpdate(user, sourceStatus, organisationRequest, null);
        }

        List<RequestRepresentation> result = requestMapper.detailedRequestsToRequestDTOs(organisationRequests);
        notificationService.submissionNotificationToRequester(user, result);

        log.debug("Deleting draft request.");
        requestService.deleteRequest(request.getId());
        return result;
    }

}
