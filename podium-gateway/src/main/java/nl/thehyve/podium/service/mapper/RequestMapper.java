/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.domain.ReviewRound;
import nl.thehyve.podium.service.util.*;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Mapper for the entity Request and its DTO RequestDTO.
 */
@Mapper(componentModel = "spring", uses = {
    RequestDetailMapper.class,
    RequestReviewProcessMapper.class,
    UserMapperHelper.class,
    OrganisationMapperHelper.class,
    PodiumEventMapper.class,
    ReviewRoundMapper.class
})
public abstract class RequestMapper {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private RequestDetailMapper requestDetailMapper;

    @Autowired
    private OrganisationMapperHelper organisationMapperHelper;

    @Autowired
    private UserMapperHelper userMapperHelper;

    @Autowired
    private ReviewRoundMapper reviewRoundMapper;

    @Autowired
    private PodiumEventMapper podiumEventMapper;

    /**
     * Copy only request uuid, request type and organisation uuid and name.
     *
     * @param request the source request.
     * @return the representation.
     */
    @MinimalMapper
    public RequestRepresentation minimalRequestToRequestDTO(Request request) {
        if (request == null) {
            return null;
        }
        RequestRepresentation requestRepresentation = new RequestRepresentation();
        requestRepresentation.setUuid(request.getUuid());
        requestRepresentation.setRequestDetail(requestDetailMapper.mapRequestTypeOnly(request.getRequestDetail()));
        requestRepresentation.setOrganisations(organisationMapperHelper.uuidsToExtendedOrganisationDTOs(request.getOrganisations()));
        return requestRepresentation;
    }

    @MinimalMapper
    @IterableMapping(qualifiedBy = MinimalMapper.class)
    public abstract Set<RequestRepresentation> minimalRequestsToRequestDTOs(Set<Request> requests);

    /**
     * Copy only the information needed for the request overviews.
     *
     * @param request the source request.
     * @return the representation.
     */
    @OverviewMapper
    public RequestRepresentation overviewRequestToRequestDTO(Request request) {
        if (request == null) {
            return null;
        }
        RequestRepresentation requestRepresentation = new RequestRepresentation();
        requestRepresentation.setId(request.getId());
        requestRepresentation.setUuid(request.getUuid());
        requestRepresentation.setRequestDetail(requestDetailMapper.requestDetailToRequestDetailRepresentation(request.getRequestDetail()));
        requestRepresentation.setStatus(request.getOverviewStatus());
        requestRepresentation.setCreatedDate(request.getCreatedDate());
        requestRepresentation.setOrganisations(organisationMapperHelper.uuidsToExtendedOrganisationDTOs(request.getOrganisations()));
        if (securityService.isCurrentUserInAnyOrganisationRole(
            request.getOrganisations(), Collections.singleton(AuthorityConstants.ORGANISATION_COORDINATOR))) {
            requestRepresentation.setRequester(userMapperHelper.uuidToRemoteUserRepresentation(request.getRequester()));
        } else {
            requestRepresentation.setRequester(userMapperHelper.uuidToUserRepresentation(request.getRequester()));
        }
        return requestRepresentation;
    }

    @OverviewMapper
    @IterableMapping(qualifiedBy = OverviewMapper.class)
    public abstract List<RequestRepresentation> overviewRequestsToRequestDTOs(List<Request> requests);

    private ReviewRound getLatestReviewRound(Request request) {
        if (request.getReviewRounds() == null || request.getReviewRounds().isEmpty()) {
            return null;
        }
        return request.getReviewRounds().get(request.getReviewRounds().size() - 1);
    }

    @RoleAwareDetail
    public RequestRepresentation detailsRequestToRequestDTO(Request request) {
        if (request == null) {
            return null;
        }
        RequestRepresentation requestRepresentation = overviewRequestToRequestDTO(request);
        // Copy requester information
        if (requestRepresentation.getRequester() == null) {
            requestRepresentation.setRequester(userMapperHelper.uuidToRemoteUserRepresentation(request.getRequester()));
        }
        // Copy revision detail if requester
        boolean isRequester = request.getRequester().equals(securityService.getCurrentUserUuid());
        if (isRequester) {
            requestRepresentation.setRevisionDetail(requestDetailMapper.requestDetailToRequestDetailRepresentation(
                request.getRevisionDetail()
            ));
        }
        boolean isCoordinator = securityService.isCurrentUserInAnyOrganisationRole(request.getOrganisations(), AuthorityConstants.ORGANISATION_COORDINATOR);
        boolean isReviewer = securityService.isCurrentUserInAnyOrganisationRole(request.getOrganisations(), AuthorityConstants.REVIEWER);
        // Copy review information of latest review round for coordinators and reviewers
        ReviewRound latestReviewRound = getLatestReviewRound(request);
        if (latestReviewRound != null) {
            if (isCoordinator) {
                requestRepresentation.setReviewRound(reviewRoundMapper.reviewRoundToReviewRoundRepresentation(latestReviewRound));
            } else if (isReviewer) {
                requestRepresentation.setReviewRound(reviewRoundMapper.reviewerReviewRoundToReviewRoundRepresentation(latestReviewRound));
            }
        }
        // Copy latest event if requester or coordinator
        if (isRequester || isCoordinator) {
            requestRepresentation.setLatestEvent(podiumEventMapper.lastPodiumEventToPodiumEventRepresentation(request.getHistoricEvents()));

        }
        // Copy related requests
        requestRepresentation.setRelatedRequests(this.minimalRequestsToRequestDTOs(request.getRelatedRequests()));
        return requestRepresentation;
    }

    @RoleAwareDetail
    @IterableMapping(qualifiedBy = RoleAwareDetail.class)
    public abstract List<RequestRepresentation> detailsRequestsToRequestDTOs(List<Request> requests);

    @Mappings({
        @Mapping(source = "requestDetail", target = "requestDetail", qualifiedByName = "clone"),
        @Mapping(source = "revisionDetail", target = "revisionDetail", qualifiedByName = "clone"),
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "uuid", ignore = true),
        @Mapping(target = "historicEvents", ignore = true),
        @Mapping(target = "relatedRequests", ignore = true)
    })
    public abstract Request clone(Request request);

}
