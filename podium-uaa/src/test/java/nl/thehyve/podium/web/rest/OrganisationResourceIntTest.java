/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.repository.OrganisationRepository;
import nl.thehyve.podium.repository.search.OrganisationSearchRepository;
import nl.thehyve.podium.search.SearchOrganisation;
import nl.thehyve.podium.service.OrganisationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the OrganisationResource REST controller.
 *
 * @see OrganisationServer
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PodiumUaaApp.class)
public class OrganisationResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_SHORT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SHORT_NAME = "BBBBBBBBBB";

    private static final boolean DEFAULT_ACTIVATED = false;
    private static final boolean UPDATED_ACTIVATED = true;

    private static final boolean DEFAULT_DELETED = false;
    private static final boolean UPDATED_DELETED = true;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private OrganisationSearchRepository organisationSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private EntityManager em;

    private MockMvc restOrganisationMockMvc;

    private Organisation organisation;

    private OrganisationDTO organisationDTO;

    Logger log = LoggerFactory.getLogger(getClass());

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        OrganisationServer organisationServer = new OrganisationServer();
        ReflectionTestUtils.setField(organisationServer, "organisationService", organisationService);
        this.restOrganisationMockMvc = MockMvcBuilders.standaloneSetup(organisationServer)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    public static Organisation createEntity(EntityManager em) {
        Organisation organisation = new Organisation()
            .name(DEFAULT_NAME)
            .shortName(DEFAULT_SHORT_NAME);

        organisation.setDeleted(DEFAULT_DELETED);
        organisation.setActivated(DEFAULT_ACTIVATED);

        return organisation;
    }

    /**
     * Create an entityDTO for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static OrganisationDTO createEntityDTO() {
        OrganisationDTO organisationDTO = new OrganisationDTO();
        organisationDTO.setName(DEFAULT_NAME);
        organisationDTO.setShortName(DEFAULT_SHORT_NAME);
        organisationDTO.setActivated(DEFAULT_ACTIVATED);

        return organisationDTO;
    }

    @Before
    public void initTest() {
        organisationSearchRepository.deleteAll();
        organisationDTO = createEntityDTO();
        organisation = createEntity(em);
    }

    @Test
    @Transactional
    public void createOrganisation() throws Exception {
        log.info("Create organisation");
        int databaseSizeBeforeCreate = organisationRepository.findAll().size();
        log.info("Database size: {}", databaseSizeBeforeCreate);

        // Create the Organisation
        restOrganisationMockMvc.perform(post("/api/organisations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(organisationDTO)))
            .andExpect(status().isCreated());

        // Validate the Organisation in the database
        List<Organisation> organisationList = organisationRepository.findAll();
        assertThat(organisationList).hasSize(databaseSizeBeforeCreate + 1);
        Organisation testOrganisation = organisationList.get(organisationList.size() - 1);
        assertThat(testOrganisation.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testOrganisation.getShortName()).isEqualTo(DEFAULT_SHORT_NAME);
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
        int databaseSizeBeforeCreate = organisationRepository.findAll().size();

        // Create the Organisation with an existing ID
        Organisation existingOrganisation = new Organisation();
        existingOrganisation.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restOrganisationMockMvc.perform(post("/api/organisations")
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
        int databaseSizeBeforeTest = organisationRepository.findAll().size();
        // set the field null
        organisationDTO.setName(null);

        // Create the Organisation, which fails.

        restOrganisationMockMvc.perform(post("/api/organisations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(organisationDTO)))
            .andExpect(status().isBadRequest());

        List<Organisation> organisationList = organisationRepository.findAll();
        assertThat(organisationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkShortNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = organisationRepository.findAll().size();
        // set the field null
        organisationDTO.setShortName(null);

        // Create the Organisation, which fails.

        restOrganisationMockMvc.perform(post("/api/organisations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(organisationDTO)))
            .andExpect(status().isBadRequest());

        List<Organisation> organisationList = organisationRepository.findAll();
        assertThat(organisationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllOrganisations() throws Exception {
        // Initialize the database
        organisationRepository.saveAndFlush(organisation);

        // Get all the organisationList
        restOrganisationMockMvc.perform(get("/api/organisations?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(organisation.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME.toString())));
    }

    @Test
    @Transactional
    public void getOrganisation() throws Exception {
        // Initialize the database
        organisationRepository.saveAndFlush(organisation);

        // Get the organisation
        restOrganisationMockMvc.perform(get("/api/organisations/{id}", organisation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(organisation.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.shortName").value(DEFAULT_SHORT_NAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingOrganisation() throws Exception {
        // Get the organisation
        restOrganisationMockMvc.perform(get("/api/organisations/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateOrganisation() throws Exception {
        // Initialize the database
        organisationService.save(organisation);

        int databaseSizeBeforeUpdate = organisationRepository.findAll().size();

        // Update the organisation
        organisationDTO.setId(organisation.getId());
        organisationDTO.setName(UPDATED_NAME);
        organisationDTO.setShortName(UPDATED_SHORT_NAME);

        restOrganisationMockMvc.perform(put("/api/organisations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(organisationDTO)))
            .andExpect(status().isOk());

        // Validate the Organisation in the database
        List<Organisation> organisationList = organisationRepository.findAll();
        assertThat(organisationList).hasSize(databaseSizeBeforeUpdate);
        Organisation testOrganisation = organisationList.get(organisationList.size() - 1);
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
        int databaseSizeBeforeUpdate = organisationRepository.findAll().size();

        // If the entity doesn't have an ID a 404 NOT FOUND will be thrown
        restOrganisationMockMvc.perform(put("/api/organisations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(organisationDTO)))
            .andExpect(status().isNotFound());

        // Validate the Organisation in the database
        List<Organisation> organisationList = organisationRepository.findAll();
        assertThat(organisationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void setOrganisationActivation() throws  Exception {
        // Initialize the database
        organisationService.save(organisation);

        int databaseSizeBeforeUpdate = organisationRepository.findAll().size();

        // Update the organisation
        Organisation updatedOrganisation = organisationRepository.findOne(organisation.getId());

        restOrganisationMockMvc.perform(
            put("/api/organisations/{id}/activation?value={activate}", organisation.getId(),
                UPDATED_ACTIVATED)
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedOrganisation)))
            .andExpect(status().isOk());

        // Validate the Organisation in the database
        List<Organisation> organisationList = organisationRepository.findAll();
        assertThat(organisationList).hasSize(databaseSizeBeforeUpdate);
        Organisation testOrganisation = organisationList.get(organisationList.size() - 1);
        assertThat(testOrganisation.isActivated()).isEqualTo(UPDATED_ACTIVATED);

        // Validate the Organisation in Elasticsearch
        SearchOrganisation organisationEs = organisationSearchRepository.findOne(testOrganisation.getId());
        assertThat(organisationEs).isEqualToIgnoringGivenFields(testOrganisation, "uuid");
        assertThat(organisationEs.getUuid()).isEqualTo(testOrganisation.getUuid().toString());
    }

    @Test
    @Transactional
    public void deleteOrganisation() throws Exception {
        // Initialize the database
        organisationService.save(organisation);

        int databaseSizeBeforeDelete = organisationService.count().intValue();

        // Get the organisation
        restOrganisationMockMvc.perform(delete("/api/organisations/{uuid}", organisation.getUuid())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean organisationExistsInEs = organisationSearchRepository.exists(organisation.getId());
        assertThat(organisationExistsInEs).isFalse();

        // Validate the database is empty
        int databaseSize = organisationService.count().intValue();
        assertThat(databaseSize).isEqualTo(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchOrganisation() throws Exception {
        // Initialize the database
        organisationService.save(organisation);

        // Search the organisation
        restOrganisationMockMvc.perform(get("/api/_search/organisations?query=id:" + organisation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(organisation.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME)));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Organisation.class);
    }
}
