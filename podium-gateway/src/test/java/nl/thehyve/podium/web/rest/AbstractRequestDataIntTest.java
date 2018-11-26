package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.SerialisedUser;
import nl.thehyve.podium.common.service.dto.*;
import nl.thehyve.podium.service.AuditService;
import nl.thehyve.podium.service.MailService;
import nl.thehyve.podium.service.OrganisationClientService;
import nl.thehyve.podium.service.UserClientService;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static nl.thehyve.podium.web.rest.RequestDataHelper.setRequestData;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

public abstract class AbstractRequestDataIntTest extends AbstractGatewayIntTest {

    @MockBean
    OrganisationClientService organisationService;

    @MockBean
    UserClientService userClientService;

    @MockBean
    MailService mailService;

    @MockBean
    AuditService auditService;

    AuthenticatedUser requester;
    AuthenticatedUser coordinator1;
    AuthenticatedUser coordinator2;
    AuthenticatedUser reviewer1;
    AuthenticatedUser reviewer2;

    final UUID organisationUuid1 = UUID.randomUUID();
    final UUID organisationUuid2 = UUID.randomUUID();
    final UUID coordinatorUuid1 = UUID.randomUUID();
    final UUID coordinatorUuid2 = UUID.randomUUID();
    final UUID reviewerUuid1 = UUID.randomUUID();
    final UUID reviewerUuid2 = UUID.randomUUID();

    final String TEST_TWO_ORGANISATIONS_TITLE = "Test request to two organisations";

    static final String mockRequesterUsername = "requester";
    static UUID mockRequesterUuid = UUID.randomUUID();

    @Before
    public void setup() {
        log.info("Clearing database before test ...");
        testService.clearDatabase();

        MockitoAnnotations.initMocks(this);

        // Mock authentication for the requester
        {
            requester = new SerialisedUser(
                mockRequesterUuid, mockRequesterUsername, requesterAuthorities, null);
        }

        // Coordinator 1 is coordinator of mock organisation 1
        {
            coordinator1 = new SerialisedUser(
                coordinatorUuid1, "coordinator1", requesterAuthorities,
                createOrganisationRole(organisationUuid1, AuthorityConstants.ORGANISATION_COORDINATOR));
        }

        // Coordinator 2 is coordinator of both mock organisations
        {
            Map<UUID, Collection<String>> roles = new HashMap<>();
            roles.put(organisationUuid1, Sets.newSet(AuthorityConstants.ORGANISATION_COORDINATOR));
            roles.put(organisationUuid2, Sets.newSet(AuthorityConstants.ORGANISATION_COORDINATOR));
            coordinator2 = new SerialisedUser(
                coordinatorUuid2, "coordinator2", requesterAuthorities, roles);
        }

        // Reviewer 1 is reviewer of mock organisation 1
        {
            reviewer1 = new SerialisedUser(
                reviewerUuid1, "reviewer1", requesterAuthorities,
                createOrganisationRole(organisationUuid1, AuthorityConstants.REVIEWER));
        }

        // Reviewer 2 is reviewer of both mock organisations
        {
            Map<UUID, Collection<String>> roles = new HashMap<>();
            roles.put(organisationUuid1, Sets.newSet(AuthorityConstants.REVIEWER));
            roles.put(organisationUuid2, Sets.newSet(AuthorityConstants.REVIEWER));
            reviewer2 = new SerialisedUser(
                reviewerUuid2, "reviewer2", requesterAuthorities,
                roles);
        }

        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @After
    public void clearDatabase() {
        log.info("Clearing database after test ...");
        testService.clearDatabase();
    }

    void initMocks() {
        // Mock organisation service for fetching organisation info through Feign
        OrganisationRepresentation organisation1 = createOrganisation(1, organisationUuid1);
        organisations.put(organisationUuid1, organisation1);
        OrganisationRepresentation organisation2 = createOrganisation(2, organisationUuid2);
        organisations.put(organisationUuid2, organisation2);
        for(Map.Entry<UUID, OrganisationRepresentation> entry: organisations.entrySet()) {
            given(this.organisationService.findOrganisationByUuid(eq(entry.getKey())))
                .willReturn(entry.getValue());
            given(this.organisationService.findOrganisationByUuidCached(eq(entry.getKey())))
                .willReturn(entry.getValue());
        }

        // Mock organisations service for fetching coordinators
        UserRepresentation coordinatorRepresentation1 = createCoordinator(1, coordinatorUuid1);
        users.put(coordinatorUuid1, coordinatorRepresentation1);
        UserRepresentation coordinatorRepresentation2 = createCoordinator(2, coordinatorUuid2);
        users.put(coordinatorUuid2, coordinatorRepresentation2);
        List<UserRepresentation> coordinators1 = new ArrayList<>();
        coordinators1.add(coordinatorRepresentation1);
        coordinators1.add(coordinatorRepresentation2);
        given(this.organisationService.findUsersByRole(eq(organisationUuid1), eq(AuthorityConstants.ORGANISATION_COORDINATOR)))
            .willReturn(coordinators1);
        List<UserRepresentation> coordinators2 = new ArrayList<>();
        coordinators1.add(coordinatorRepresentation2);
        given(this.organisationService.findUsersByRole(eq(organisationUuid2), eq(AuthorityConstants.ORGANISATION_COORDINATOR)))
            .willReturn(coordinators2);

        // Mock organisations service for fetching reviewers
        UserRepresentation reviewerRepresentation1 = createReviewer(1, reviewerUuid1);
        users.put(reviewerUuid1, reviewerRepresentation1);
        UserRepresentation reviewerRepresentation2 = createReviewer(1, reviewerUuid2);
        users.put(reviewerUuid2, reviewerRepresentation2);
        List<UserRepresentation> reviewers1 = new ArrayList<>();
        reviewers1.add(reviewerRepresentation1);
        reviewers1.add(reviewerRepresentation2);
        given(this.organisationService.findUsersByRole(eq(organisationUuid1), eq(AuthorityConstants.REVIEWER)))
            .willReturn(reviewers1);
        List<UserRepresentation> reviewers2 = new ArrayList<>();
        reviewers2.add(reviewerRepresentation2);
        given(this.organisationService.findUsersByRole(eq(organisationUuid2), eq(AuthorityConstants.REVIEWER)))
            .willReturn(reviewers2);

        // Mock Feign client for fetching user information
        UserRepresentation requesterRepresentation = createRequester(mockRequesterUuid, mockRequesterUsername);
        users.put(mockRequesterUuid, requesterRepresentation);

        for(Map.Entry<UUID, UserRepresentation> entry: users.entrySet()) {
            given(this.userClientService.findUserByUuid(eq(entry.getKey())))
                .willReturn(entry.getValue());
            given(this.userClientService.findUserByUuidCached(eq(entry.getKey())))
                .willReturn(entry.getValue());
        }

        // Mock notification call
        doNothing().when(this.mailService).sendSubmissionNotificationToCoordinators(any(), any(), anyListOf(UserRepresentation.class));
        doNothing().when(this.mailService).sendRequestClosedNotificationToRequester(any(), any());
        doNothing().when(this.mailService).sendDeliveryReleasedNotificationToRequester(any(), any(), any());
        doNothing().when(this.mailService).sendDeliveryReceivedNotificationToCoordinators(any(), any(), any(), anyListOf(UserRepresentation.class));
        doNothing().when(this.mailService).sendDeliveryCancelledNotificationToRequester(any(), any(), any(UserRepresentation.class));

        // Mock audit service calls
        doNothing().when(this.auditService).publishEvent(any());
    }

    void initFetchTests() throws Exception {
        // Initialize draft with two organisations
        RequestRepresentation requestTwoOrganisation = newDraft(requester);
        setRequestData(requestTwoOrganisation);
        requestTwoOrganisation.getRequestDetail().setTitle(TEST_TWO_ORGANISATIONS_TITLE);

        submitDraftToOrganisations(requester, requestTwoOrganisation, Arrays.asList(organisationUuid1, organisationUuid2));

        // Initialize draft with one organisation
        RequestRepresentation requestOneOrganisation = newDraft(requester);
        setRequestData(requestOneOrganisation);

        submitDraftToOrganisations(requester, requestOneOrganisation, Arrays.asList(organisationUuid2));
    }

}
