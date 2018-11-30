/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.OrganisationRepresentation;
import nl.thehyve.podium.common.test.AbstractAuthorisedUserIntTest;
import nl.thehyve.podium.common.test.web.rest.TestUtil;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.repository.OrganisationRepository;
import nl.thehyve.podium.repository.search.OrganisationSearchRepository;
import nl.thehyve.podium.search.SearchOrganisation;
import nl.thehyve.podium.service.OrganisationService;
import nl.thehyve.podium.service.TestService;
import nl.thehyve.podium.web.rest.dto.ManagedUserRepresentation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the OrganisationResource REST controller.
 *
 * @see OrganisationServer
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PodiumUaaApp.class)
public class OrganisationResourceIntTest extends AbstractAuthorisedUserIntTest {

    private Logger log = LoggerFactory.getLogger(OrganisationResourceIntTest.class);

    private static final String DEFAULT_NAME = "A";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_SHORT_NAME = "A";
    private static final String UPDATED_SHORT_NAME = "BBBBBBBBBB";

    private static final boolean DEFAULT_ACTIVATED = false;
    private static final boolean UPDATED_ACTIVATED = true;

    private static final boolean DEFAULT_DELETED = false;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private OrganisationSearchRepository organisationSearchRepository;

    @Autowired
    private TestService testService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    protected MockMvc getMockMvc() {
        return mockMvc;
    }

    private TypeReference<List<OrganisationRepresentation>> organisationListTypeReference =
        new TypeReference<List<OrganisationRepresentation>>(){};

    private TypeReference<List<ManagedUserRepresentation>> userListTypeReference =
        new TypeReference<List<ManagedUserRepresentation>>(){};

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    private Organisation organisationA;
    private Organisation organisationB;

    private OrganisationRepresentation organisationRepresentation;

    private OrganisationRepresentation createOrganisationDTO() {
        OrganisationRepresentation organisation = new OrganisationRepresentation();
        organisation.setName("ABC");
        organisation.setShortName("AB");
        Set<RequestType> requestTypes = new HashSet<>();
        requestTypes.add(RequestType.Data);
        organisation.setRequestTypes(requestTypes);
        organisation.setActivated(DEFAULT_ACTIVATED);
        return organisation;
    }

    private void createOrganisations() {
        organisationA = testService.createOrganisation(DEFAULT_NAME);
        organisationB = testService.createOrganisation("B");
        organisationRepresentation = createOrganisationDTO();
    }

    private AuthenticatedUser podiumAdmin;
    private AuthenticatedUser bbmriAdmin;
    private AuthenticatedUser adminOrganisationA;
    private AuthenticatedUser adminOrganisationB;
    private AuthenticatedUser adminOrganisationAandB;
    private AuthenticatedUser researcher;
    private AuthenticatedUser anonymous;

    private void createUsers() throws UserAccountException {
        podiumAdmin = testService.createUser("podiumAdmin", AuthorityConstants.PODIUM_ADMIN);
        bbmriAdmin = testService.createUser("bbmriAdmin", AuthorityConstants.BBMRI_ADMIN);
        adminOrganisationA = testService.createUser("adminOrganisationA", AuthorityConstants.ORGANISATION_ADMIN, organisationA);
        adminOrganisationB = testService.createUser("adminOrganisationB", AuthorityConstants.ORGANISATION_ADMIN, organisationB);
        adminOrganisationAandB = testService.createUser("adminOrganisationAandB", AuthorityConstants.ORGANISATION_ADMIN, organisationA, organisationB);
        researcher = testService.createUser("researcher", AuthorityConstants.RESEARCHER);
        anonymous = null;
    }

    private void setupData() throws UserAccountException {
        createOrganisations();
        createUsers();
    }

    @Test
    @Transactional
    public void createOrganisation() throws Exception {
        setupData();

        log.info("Create organisation");
        int databaseSizeBeforeCreate = organisationRepository.findAll().size();
        log.info("Database size: {}", databaseSizeBeforeCreate);

        // Create the Organisation
        mockMvc.perform(post("/api/organisations")
            .with(token(bbmriAdmin))
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(organisationRepresentation)))
            .andExpect(status().isCreated());

        // Validate the Organisation in the database
        List<Organisation> organisationList = organisationRepository.findAll();
        assertThat(organisationList).hasSize(databaseSizeBeforeCreate + 1);
        Organisation testOrganisation = organisationList.get(organisationList.size() - 1);
        assertThat(testOrganisation.getName()).isEqualTo("ABC");
        assertThat(testOrganisation.getShortName()).isEqualTo("AB");
        assertThat(testOrganisation.isActivated()).isEqualTo(DEFAULT_ACTIVATED);
        assertThat(testOrganisation.isDeleted()).isEqualTo(DEFAULT_DELETED);

        log.info("testOrganisation: {}", testOrganisation);

        // Validate the Organisation in Elasticsearch
        SearchOrganisation organisationEs = organisationSearchRepository.findOne(testOrganisation.getId());

        log.info("organisationEs: {}", organisationEs);

        assertThat(organisationEs).isEqualToIgnoringGivenFields(testOrganisation, "uuid");
        assertThat(organisationEs.getUuid()).isEqualTo(testOrganisation.getUuid().toString());
    }

    @Test
    @Transactional
    public void createOrganisationWithExistingId() throws Exception {
        setupData();

        int databaseSizeBeforeCreate = organisationRepository.findAll().size();

        // Create the Organisation with an existing ID
        Organisation existingOrganisation = new Organisation();
        existingOrganisation.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        mockMvc.perform(post("/api/organisations")
            .with(token(bbmriAdmin))
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingOrganisation)))
            .andExpect(status().isBadRequest());

        // Validate length of entities in the database didnt change
        List<Organisation> organisationList = organisationRepository.findAll();
        assertThat(organisationList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        setupData();

        int databaseSizeBeforeTest = organisationRepository.findAll().size();
        // set the field null
        organisationRepresentation.setName(null);

        // Create the Organisation, which fails.

        mockMvc.perform(post("/api/organisations")
            .with(token(bbmriAdmin))
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(organisationRepresentation)))
            .andExpect(status().isBadRequest());

        List<Organisation> organisationList = organisationRepository.findAll();
        assertThat(organisationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkShortNameIsRequired() throws Exception {
        setupData();

        int databaseSizeBeforeTest = organisationRepository.findAll().size();
        // set the field null
        organisationRepresentation.setShortName(null);

        // Create the Organisation, which fails.

        mockMvc.perform(post("/api/organisations")
            .with(token(bbmriAdmin))
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(organisationRepresentation)))
            .andExpect(status().isBadRequest());

        List<Organisation> organisationList = organisationRepository.findAll();
        assertThat(organisationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllOrganisations() throws Exception {
        setupData();

        // Get all the organisationList
        mockMvc.perform(get("/api/organisations?sort=id,desc")
            .with(token(bbmriAdmin)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(organisationA.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME)));
    }

    @Test
    @Transactional
    public void getAdminAOrganisations() throws Exception {
        setupData();

        // Get all the organisations for the admin of A
        mockMvc.perform(get("/api/organisations/admin")
            .with(token(adminOrganisationA)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andDo(result -> {
                List<OrganisationRepresentation> organisations =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), organisationListTypeReference);
                Assert.assertEquals(1, organisations.size());
            })
            .andExpect(jsonPath("$.[*].id").value(hasItem(organisationA.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME)));
    }

    @Test
    @Transactional
    public void getAdminAOrganisationUsers() throws Exception {
        setupData();

        // Get all the organisation users for the admin of A
        mockMvc.perform(get("/api/users/organisations")
            .with(token(adminOrganisationA)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andDo(result -> {
                log.warn("OUTPUT: {}", result.getResponse().getContentAsString());
                List<ManagedUserRepresentation> users =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), userListTypeReference);
                Assert.assertEquals(2, users.size());
            })
            .andExpect(jsonPath("$.[*].login").value(hasItem("test_adminorganisationa")))
            .andExpect(jsonPath("$.[*].login").value(hasItem("test_adminorganisationaandb")));

        // Get all the organisation users for the admin of A and B
        mockMvc.perform(get("/api/users/organisations")
            .with(token(adminOrganisationAandB)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andDo(result -> {
                List<ManagedUserRepresentation> users =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), userListTypeReference);
                Assert.assertEquals(3, users.size());
            })
            .andExpect(jsonPath("$.[*].login").value(hasItem("test_adminorganisationa")))
            .andExpect(jsonPath("$.[*].login").value(hasItem("test_adminorganisationb")))
            .andExpect(jsonPath("$.[*].login").value(hasItem("test_adminorganisationaandb")));

        // Get all the users of organisation A for the admin of A and B
        mockMvc.perform(get("/api/users/organisations/{uuid}", organisationA.getUuid())
            .with(token(adminOrganisationAandB)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andDo(result -> {
                List<ManagedUserRepresentation> users =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), userListTypeReference);
                Assert.assertEquals(2, users.size());
            })
            .andExpect(jsonPath("$.[*].login").value(hasItem("test_adminorganisationa")))
            .andExpect(jsonPath("$.[*].login").value(hasItem("test_adminorganisationaandb")));
    }

    @Test
    @Transactional
    public void getAdminAandBOrganisations() throws Exception {
        setupData();

        // Get all the organisations for the admin of A and B
        mockMvc.perform(get("/api/organisations/admin")
            .with(token(adminOrganisationAandB)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andDo(result -> {
                List<OrganisationRepresentation> organisations =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), organisationListTypeReference);
                Assert.assertEquals(2, organisations.size());
            })
            .andExpect(jsonPath("$.[*].id").value(hasItem(organisationA.getId().intValue())))
            .andExpect(jsonPath("$.[*].id").value(hasItem(organisationB.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME)));
    }

    @Test
    @Transactional
    public void getOrganisation() throws Exception {
        setupData();

        // Get the organisation
        mockMvc.perform(get("/api/organisations/uuid/{uuid}", organisationA.getUuid())
            .with(token(bbmriAdmin)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(organisationA.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.shortName").value(DEFAULT_SHORT_NAME));
    }

    @Test
    @Transactional
    public void getNonExistingOrganisation() throws Exception {
        setupData();

        // Get the organisation
        mockMvc.perform(get("/api/organisations/uuid/{uuid}", UUID.randomUUID())
            .with(token(bbmriAdmin)))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateOrganisation() throws Exception {
        setupData();

        int databaseSizeBeforeUpdate = organisationRepository.findAll().size();

        // Update the organisation
        organisationRepresentation.setId(organisationA.getId());
        organisationRepresentation.setName(UPDATED_NAME);
        organisationRepresentation.setShortName(UPDATED_SHORT_NAME);

        mockMvc.perform(put("/api/organisations")
            .with(token(bbmriAdmin))
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(organisationRepresentation)))
            .andExpect(status().isOk());

        // Validate the Organisation in the database
        List<Organisation> organisationList = organisationRepository.findAll();
        assertThat(organisationList).hasSize(databaseSizeBeforeUpdate);
        Organisation testOrganisation = organisationRepository.findByUuidAndDeletedFalse(organisationA.getUuid());
        assertThat(testOrganisation.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testOrganisation.getShortName()).isEqualTo(UPDATED_SHORT_NAME);

        // Validate the Organisation in Elasticsearch
        SearchOrganisation organisationEs = organisationSearchRepository.findOne(testOrganisation.getId());
        assertThat(organisationEs).isEqualToIgnoringGivenFields(testOrganisation, "uuid");
        assertThat(organisationEs.getUuid()).isEqualTo(testOrganisation.getUuid().toString());
    }

    @Test
    @Transactional
    public void updateNonExistingOrganisation() throws Exception {
        setupData();

        int databaseSizeBeforeUpdate = organisationRepository.findAll().size();

        // If the entity doesn't have an ID a 404 NOT FOUND will be thrown
        mockMvc.perform(put("/api/organisations")
            .with(token(bbmriAdmin))
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(organisationRepresentation)))
            .andExpect(status().isNotFound());

        // Validate the Organisation in the database
        List<Organisation> organisationList = organisationRepository.findAll();
        assertThat(organisationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void setOrganisationActivation() throws  Exception {
        setupData();

        int databaseSizeBeforeUpdate = organisationRepository.findAll().size();

        // Update the organisation
        Organisation updatedOrganisation = organisationRepository.findOne(organisationA.getId());

        mockMvc.perform(
            put("/api/organisations/{uuid}/activation?value={activate}", organisationA.getUuid(),
                UPDATED_ACTIVATED)
            .with(token(bbmriAdmin))
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedOrganisation)))
            .andExpect(status().isOk());

        // Validate the Organisation in the database
        List<Organisation> organisationList = organisationRepository.findAll();
        assertThat(organisationList).hasSize(databaseSizeBeforeUpdate);
        Organisation testOrganisation = organisationRepository.findByUuidAndDeletedFalse(organisationA.getUuid());
        assertThat(testOrganisation.isActivated()).isEqualTo(UPDATED_ACTIVATED);

        // Validate the Organisation in Elasticsearch
        SearchOrganisation organisationEs = organisationSearchRepository.findOne(testOrganisation.getId());
        assertThat(organisationEs).isEqualToIgnoringGivenFields(testOrganisation, "uuid");
        assertThat(organisationEs.getUuid()).isEqualTo(testOrganisation.getUuid().toString());
    }

    @Test
    @Transactional
    public void deleteOrganisation() throws Exception {
        setupData();

        int databaseSizeBeforeDelete = organisationService.count().intValue();

        // Get the organisation
        mockMvc.perform(delete("/api/organisations/{uuid}", organisationA.getUuid())
            .with(token(bbmriAdmin))
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean organisationExistsInEs = organisationSearchRepository.exists(organisationA.getId());
        assertThat(organisationExistsInEs).isFalse();

        // Validate the database is empty
        int databaseSize = organisationService.count().intValue();
        assertThat(databaseSize).isEqualTo(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchOrganisation() throws Exception {
        setupData();

        // Search the organisation
        mockMvc.perform(get("/api/_search/organisations?query=id:" + organisationA.getId())
            .with(token(bbmriAdmin)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(organisationA.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME)));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Organisation.class);
    }
}
