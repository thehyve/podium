/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.enumeration.*;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.common.service.dto.OrganisationRepresentation;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.repository.ReviewFeedbackRepository;
import nl.thehyve.podium.repository.ReviewRoundRepository;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.stream.Collectors;

import static nl.thehyve.podium.web.rest.RequestDataHelper.setRequestData;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the RequestResource REST controller.
 *
 * @see RequestResource
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
public class RequestResourceIntTest extends AbstractRequestDataIntTest {

    @Autowired
    private ReviewRoundRepository reviewRoundRepository;

    @Autowired
    private ReviewFeedbackRepository reviewFeedbackRepository;

    @Test
    public void createDraft() throws Exception {
        long databaseSizeBeforeCreate = requestRepository
                .findAllByRequesterAndStatus(mockRequesterUuid, RequestStatus.Draft, null).getTotalElements();

        RequestRepresentation request = newDraft(requester);

        Assert.assertNotNull(request.getUuid());

        long databaseSizeAfterCreate = requestRepository
                .findAllByRequesterAndStatus(mockRequesterUuid, RequestStatus.Draft, null).getTotalElements();
        Assert.assertEquals(databaseSizeBeforeCreate + 1, databaseSizeAfterCreate);
    }

    @Test
    public void fetchDrafts() throws Exception {
        RequestRepresentation request1 = newDraft(requester);
        RequestRepresentation request2 = newDraft(requester);
        Set<UUID> requestUuids = new TreeSet<>(Arrays.asList(request1.getUuid(), request2.getUuid()));

        List<RequestRepresentation> requests = fetchRequests(requester, "/drafts");
        Assert.assertEquals(requestUuids.size(), requests.size());
        Set<UUID> resultUuids = new TreeSet<>();
        for(RequestRepresentation req: requests) {
            resultUuids.add(req.getUuid());
        }
        Assert.assertEquals(requestUuids, resultUuids);
    }

    @Test
    public void deleteDraft() throws Exception {
        RequestRepresentation request = newDraft(requester);

        mockMvc.perform(
            getRequest(HttpMethod.DELETE,
                REQUESTS_ROUTE + "/drafts/" + request.getUuid().toString(),
                null,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
        });

        Request req = requestRepository.findOneByUuid(request.getUuid());
        Assert.assertNull(req);
    }

    @Test
    public void submitDraft() throws Exception {
        initMocks();
        // Setup a submitted draft
        getSubmittedDraft(requester, organisationUuid1);

        Thread.sleep(2000);

        verify(this.mailService, times(1)).sendSubmissionNotificationToCoordinators(any(), any(), nonEmptyUserRepresentationList());
        verify(this.mailService, times(1)).sendSubmissionNotificationToRequester(any(), nonEmptyRequestList());
        verify(this.auditService, times(1)).publishEvent(any());

        // Fetch requests with status 'Validation'
        List<RequestRepresentation> requests = fetchRequests(requester, "/status/Validation/requester");
        Assert.assertEquals(1, requests.size());
        for(RequestRepresentation req: requests) {
            Assert.assertEquals(OverviewStatus.Validation, req.getStatus());
            long historicEventCount = requestRepository.countHistoricEventsByRequestUuid(req.getUuid());
            Assert.assertEquals(1, historicEventCount);
        }
    }

    @Test
    public void requestsContainRelatedRequestData() throws Exception {
        initMocks();
        initFetchTests();

        // Fetch requests with status 'Validation'
        List<RequestRepresentation> requests = fetchRequests(requester, "/status/Validation/requester");
        List<RequestRepresentation> twoOrganisationsRequests = requests.stream()
            .filter(req -> req.getRequestDetail().getTitle().equals(TEST_TWO_ORGANISATIONS_TITLE))
            .collect(Collectors.toList());

        Assert.assertEquals(2, twoOrganisationsRequests.size());
        // Test if each of the two requests contains a link to the other.
        int i = 0;
        for(RequestRepresentation req: twoOrganisationsRequests) {
            int j = i == 0 ? 1 : 0;
            RequestRepresentation other = twoOrganisationsRequests.get(j);
            Assert.assertThat(other.getOrganisations(), hasSize(1));
            OrganisationRepresentation otherOrganisation = other.getOrganisations().get(0);

            RequestRepresentation fullRequest = fetchRequest(requester, "/" + req.getUuid().toString());

            Assert.assertThat(fullRequest.getRelatedRequests(), hasSize(1));
            Assert.assertThat(fullRequest.getRelatedRequests(), hasItem(
                allOf(
                    hasProperty("uuid", equalTo(other.getUuid())),
                    hasProperty("requestDetail",
                        hasProperty("requestType", equalTo(other.getRequestDetail().getRequestType()))),
                    hasProperty("organisations", hasItem(
                        allOf(
                            hasProperty("uuid", equalTo(otherOrganisation.getUuid())),
                            hasProperty("name", equalTo(otherOrganisation.getName()))
                        )
                    ))
                )
            ));
            i++;
        }
    }

    @Test
    public void fetchRequesterRequests() throws Exception {
        initMocks();
        initFetchTests();

        // Fetch requests with status 'Validation'
        List<RequestRepresentation> requests = fetchRequests(requester, "/status/Validation/requester");
        Assert.assertEquals(3, requests.size());
        for(RequestRepresentation req: requests) {
            Assert.assertEquals(OverviewStatus.Validation, req.getStatus());
        }
    }

    @Test
    public void fetchRequesterRequestCounts() throws Exception {
        initMocks();
        initFetchTests();

        List<RequestRepresentation> requests = fetchAllForRole(requester, AuthorityConstants.RESEARCHER);
        Assert.assertEquals(3, requests.size());

        RequestRepresentation organisation1Request = requests.stream().filter(req ->
            req.getOrganisations().stream().anyMatch(org -> org.getUuid().equals(organisationUuid1))
        ).findAny().get();
        validateRequest(organisation1Request, coordinator1);

        // Fetch request counts
        Map<OverviewStatus, Long> counts = fetchCounts(requester, "/counts/requester");
        Assert.assertEquals(3, counts.get(OverviewStatus.All).longValue());
        Assert.assertEquals(1, counts.get(OverviewStatus.Review).longValue());
        Assert.assertEquals(2, counts.get(OverviewStatus.Validation).longValue());
    }

    @Test
    public void fetchCoordinatorRequests() throws Exception {
        initMocks();
        initFetchTests();

        // Fetch requests with status 'Validation' for coordinator 1: should return 1 request
        List<RequestRepresentation> requests1 = fetchRequests(coordinator1, "/status/Validation/coordinator");
        Assert.assertEquals(1, requests1.size());
        for(RequestRepresentation req: requests1) {
            Assert.assertEquals(OverviewStatus.Validation, req.getStatus());
        }

        // Fetch requests with status 'Validation' for coordinator 2: should return 3 requests
        List<RequestRepresentation> requests2 = fetchRequests(coordinator2, "/status/Validation/coordinator");
        Assert.assertEquals(3, requests2.size());
        for(RequestRepresentation req: requests2) {
            Assert.assertEquals(OverviewStatus.Validation, req.getStatus());
        }

        // Fetch requests with status 'Validation' for coordinator 2, organisation 2: should return 2 requests
        List<RequestRepresentation> requests3 = fetchRequests(coordinator2,
                "/status/Validation/organisation/" + organisationUuid2.toString() + "/coordinator");
        Assert.assertEquals(2, requests3.size());
        for(RequestRepresentation req: requests3) {
            Assert.assertEquals(OverviewStatus.Validation, req.getStatus());
        }
    }

    @Test
    public void fetchCoordinatorAndReviewerRequestCounts() throws Exception {
        initMocks();
        initFetchTests();

        List<RequestRepresentation> requests = fetchAllForRole(requester, AuthorityConstants.RESEARCHER);
        Assert.assertEquals(3, requests.size());

        requests.stream().filter(req ->
            req.getOrganisations().stream().anyMatch(org -> org.getUuid().equals(organisationUuid2))
        ).forEach(request -> {
            try {
                validateRequest(request, coordinator2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Fetch request counts for coordinator
        Map<OverviewStatus, Long> counts = fetchCounts(coordinator2, "/counts/coordinator");
        Assert.assertEquals(3, counts.get(OverviewStatus.All).longValue());
        Assert.assertEquals(2, counts.get(OverviewStatus.Review).longValue());
        Assert.assertEquals(1, counts.get(OverviewStatus.Validation).longValue());

        // Fetch request counts for reviewer
        Map<OverviewStatus, Long> reviewerCounts = fetchCounts(reviewer2, "/counts/reviewer");
        Assert.assertEquals(2, reviewerCounts.get(OverviewStatus.All).longValue());
        Assert.assertEquals(2, reviewerCounts.get(OverviewStatus.Review).longValue());
    }

    @Test
    public void fetchReviewerRequests() throws Exception {
        initMocks();
        initFetchTests();

        List<RequestRepresentation> coordinatorRequests = fetchAllForCoordinator(coordinator1);
        Assert.assertThat(coordinatorRequests.size(), greaterThan(0));

        UUID requestUuid = coordinatorRequests.get(0).getUuid();

        // Submit for review
        performProcessAction(coordinator1, ACTION_VALIDATE, requestUuid, HttpMethod.GET, null);

        // Fetch requests with status 'Review' for reviewer 1: should return 1 request
        List<RequestRepresentation> requests = fetchRequests(reviewer1, "/reviewer");
        Assert.assertEquals(1, requests.size());
        for(RequestRepresentation req: requests) {
            Assert.assertEquals(requestUuid, req.getUuid());
            Assert.assertEquals(OverviewStatus.Review, req.getStatus());
        }
    }

    @Test
    public void submitInvalidDraftRejected() throws Exception {
        RequestRepresentation request = newDraft(requester);

        setRequestData(request);

        List<OrganisationRepresentation> organisations = new ArrayList<>();
        OrganisationRepresentation organisation = new OrganisationRepresentation();
        organisation.setUuid(organisationUuid1);
        organisations.add(organisation);
        request.setOrganisations(organisations);

        request.getRequestDetail().setTitle(""); // invalid, should be rejected.

        request = updateDraft(requester, request);
        Assert.assertEquals(1, request.getOrganisations().size());
        Assert.assertEquals("", request.getRequestDetail().getTitle());

        // Submit the draft. One request should have been generated (and is returned).
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/drafts/" + request.getUuid().toString() + "/submit",
                null,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andDo(result -> {
            log.info("Submitted result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
        });
    }

}
