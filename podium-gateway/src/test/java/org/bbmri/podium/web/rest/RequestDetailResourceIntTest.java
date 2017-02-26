package org.bbmri.podium.web.rest;

import org.bbmri.podium.PodiumGatewayApp;

import org.bbmri.podium.domain.RequestDetail;
import org.bbmri.podium.repository.RequestDetailRepository;
import org.bbmri.podium.service.RequestDetailService;
import org.bbmri.podium.repository.search.RequestdetailSearchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.bbmri.podium.domain.enumeration.RequestType;
/**
 * Test class for the RequestdetailResource REST controller.
 *
 * @see RequestdetailResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PodiumGatewayApp.class)
public class RequestDetailResourceIntTest {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_BACKGROUND = "AAAAAAAAAA";
    private static final String UPDATED_BACKGROUND = "BBBBBBBBBB";

    private static final String DEFAULT_RESEARCH_QUESTION = "AAAAAAAAAA";
    private static final String UPDATED_RESEARCH_QUESTION = "BBBBBBBBBB";

    private static final String DEFAULT_HYPOTHESIS = "AAAAAAAAAA";
    private static final String UPDATED_HYPOTHESIS = "BBBBBBBBBB";

    private static final String DEFAULT_METHODS = "AAAAAAAAAA";
    private static final String UPDATED_METHODS = "BBBBBBBBBB";

    private static final String DEFAULT_RELATED_REQUEST_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_RELATED_REQUEST_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_SEARCH_QUERY = "AAAAAAAAAA";
    private static final String UPDATED_SEARCH_QUERY = "BBBBBBBBBB";

    private static final RequestType DEFAULT_REQUEST_TYPE = RequestType.DATA;
    private static final RequestType UPDATED_REQUEST_TYPE = RequestType.IMAGES;

    private static final Boolean DEFAULT_COMBINED_REQUEST = false;
    private static final Boolean UPDATED_COMBINED_REQUEST = true;

    @Autowired
    private RequestDetailRepository requestDetailRepository;

    @Autowired
    private RequestDetailService requestDetailService;

    @Autowired
    private RequestdetailSearchRepository requestdetailSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private EntityManager em;

    private MockMvc restRequestdetailMockMvc;

    private RequestDetail requestDetail;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        RequestdetailResource requestdetailResource = new RequestdetailResource(requestDetailService);
        this.restRequestdetailMockMvc = MockMvcBuilders.standaloneSetup(requestdetailResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RequestDetail createEntity(EntityManager em) {
        RequestDetail requestDetail = new RequestDetail()
                .title(DEFAULT_TITLE)
                .background(DEFAULT_BACKGROUND)
                .researchQuestion(DEFAULT_RESEARCH_QUESTION)
                .hypothesis(DEFAULT_HYPOTHESIS)
                .methods(DEFAULT_METHODS)
                .relatedRequestNumber(DEFAULT_RELATED_REQUEST_NUMBER)
                .searchQuery(DEFAULT_SEARCH_QUERY)
                .requestType(DEFAULT_REQUEST_TYPE)
                .combinedRequest(DEFAULT_COMBINED_REQUEST);
        return requestDetail;
    }

    @Before
    public void initTest() {
        requestdetailSearchRepository.deleteAll();
        requestDetail = createEntity(em);
    }

    @Test
    @Transactional
    public void createRequestdetail() throws Exception {
        int databaseSizeBeforeCreate = requestDetailRepository.findAll().size();

        // Create the RequestDetail

        restRequestdetailMockMvc.perform(post("/api/requestdetails")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestDetail)))
            .andExpect(status().isCreated());

        // Validate the RequestDetail in the database
        List<RequestDetail> requestDetailList = requestDetailRepository.findAll();
        assertThat(requestDetailList).hasSize(databaseSizeBeforeCreate + 1);
        RequestDetail testRequestDetail = requestDetailList.get(requestDetailList.size() - 1);
        assertThat(testRequestDetail.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testRequestDetail.getBackground()).isEqualTo(DEFAULT_BACKGROUND);
        assertThat(testRequestDetail.getResearchQuestion()).isEqualTo(DEFAULT_RESEARCH_QUESTION);
        assertThat(testRequestDetail.getHypothesis()).isEqualTo(DEFAULT_HYPOTHESIS);
        assertThat(testRequestDetail.getMethods()).isEqualTo(DEFAULT_METHODS);
        assertThat(testRequestDetail.getRelatedRequestNumber()).isEqualTo(DEFAULT_RELATED_REQUEST_NUMBER);
        assertThat(testRequestDetail.getSearchQuery()).isEqualTo(DEFAULT_SEARCH_QUERY);
        assertThat(testRequestDetail.getRequestType()).isEqualTo(DEFAULT_REQUEST_TYPE);
        assertThat(testRequestDetail.isCombinedRequest()).isEqualTo(DEFAULT_COMBINED_REQUEST);

        // Validate the RequestDetail in Elasticsearch
        RequestDetail requestDetailEs = requestdetailSearchRepository.findOne(testRequestDetail.getId());
        assertThat(requestDetailEs).isEqualToComparingFieldByField(testRequestDetail);
    }

    @Test
    @Transactional
    public void createRequestdetailWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = requestDetailRepository.findAll().size();

        // Create the RequestDetail with an existing ID
        RequestDetail existingRequestDetail = new RequestDetail();
        existingRequestDetail.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restRequestdetailMockMvc.perform(post("/api/requestdetails")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingRequestDetail)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<RequestDetail> requestDetailList = requestDetailRepository.findAll();
        assertThat(requestDetailList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = requestDetailRepository.findAll().size();
        // set the field null
        requestDetail.setTitle(null);

        // Create the RequestDetail, which fails.

        restRequestdetailMockMvc.perform(post("/api/requestdetails")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestDetail)))
            .andExpect(status().isBadRequest());

        List<RequestDetail> requestDetailList = requestDetailRepository.findAll();
        assertThat(requestDetailList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkBackgroundIsRequired() throws Exception {
        int databaseSizeBeforeTest = requestDetailRepository.findAll().size();
        // set the field null
        requestDetail.setBackground(null);

        // Create the RequestDetail, which fails.

        restRequestdetailMockMvc.perform(post("/api/requestdetails")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestDetail)))
            .andExpect(status().isBadRequest());

        List<RequestDetail> requestDetailList = requestDetailRepository.findAll();
        assertThat(requestDetailList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkResearchQuestionIsRequired() throws Exception {
        int databaseSizeBeforeTest = requestDetailRepository.findAll().size();
        // set the field null
        requestDetail.setResearchQuestion(null);

        // Create the RequestDetail, which fails.

        restRequestdetailMockMvc.perform(post("/api/requestdetails")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestDetail)))
            .andExpect(status().isBadRequest());

        List<RequestDetail> requestDetailList = requestDetailRepository.findAll();
        assertThat(requestDetailList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkHypothesisIsRequired() throws Exception {
        int databaseSizeBeforeTest = requestDetailRepository.findAll().size();
        // set the field null
        requestDetail.setHypothesis(null);

        // Create the RequestDetail, which fails.

        restRequestdetailMockMvc.perform(post("/api/requestdetails")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestDetail)))
            .andExpect(status().isBadRequest());

        List<RequestDetail> requestDetailList = requestDetailRepository.findAll();
        assertThat(requestDetailList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkMethodsIsRequired() throws Exception {
        int databaseSizeBeforeTest = requestDetailRepository.findAll().size();
        // set the field null
        requestDetail.setMethods(null);

        // Create the RequestDetail, which fails.

        restRequestdetailMockMvc.perform(post("/api/requestdetails")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestDetail)))
            .andExpect(status().isBadRequest());

        List<RequestDetail> requestDetailList = requestDetailRepository.findAll();
        assertThat(requestDetailList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSearchQueryIsRequired() throws Exception {
        int databaseSizeBeforeTest = requestDetailRepository.findAll().size();
        // set the field null
        requestDetail.setSearchQuery(null);

        // Create the RequestDetail, which fails.

        restRequestdetailMockMvc.perform(post("/api/requestdetails")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestDetail)))
            .andExpect(status().isBadRequest());

        List<RequestDetail> requestDetailList = requestDetailRepository.findAll();
        assertThat(requestDetailList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRequestTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = requestDetailRepository.findAll().size();
        // set the field null
        requestDetail.setRequestType(null);

        // Create the RequestDetail, which fails.

        restRequestdetailMockMvc.perform(post("/api/requestdetails")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestDetail)))
            .andExpect(status().isBadRequest());

        List<RequestDetail> requestDetailList = requestDetailRepository.findAll();
        assertThat(requestDetailList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllRequestdetails() throws Exception {
        // Initialize the database
        requestDetailRepository.saveAndFlush(requestDetail);

        // Get all the requestdetailList
        restRequestdetailMockMvc.perform(get("/api/requestdetails?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(requestDetail.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].background").value(hasItem(DEFAULT_BACKGROUND.toString())))
            .andExpect(jsonPath("$.[*].researchQuestion").value(hasItem(DEFAULT_RESEARCH_QUESTION.toString())))
            .andExpect(jsonPath("$.[*].hypothesis").value(hasItem(DEFAULT_HYPOTHESIS.toString())))
            .andExpect(jsonPath("$.[*].methods").value(hasItem(DEFAULT_METHODS.toString())))
            .andExpect(jsonPath("$.[*].relatedRequestNumber").value(hasItem(DEFAULT_RELATED_REQUEST_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].searchQuery").value(hasItem(DEFAULT_SEARCH_QUERY.toString())))
            .andExpect(jsonPath("$.[*].requestType").value(hasItem(DEFAULT_REQUEST_TYPE.toString())))
            .andExpect(jsonPath("$.[*].combinedRequest").value(hasItem(DEFAULT_COMBINED_REQUEST.booleanValue())));
    }

    @Test
    @Transactional
    public void getRequestdetail() throws Exception {
        // Initialize the database
        requestDetailRepository.saveAndFlush(requestDetail);

        // Get the requestDetail
        restRequestdetailMockMvc.perform(get("/api/requestdetails/{id}", requestDetail.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(requestDetail.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.background").value(DEFAULT_BACKGROUND.toString()))
            .andExpect(jsonPath("$.researchQuestion").value(DEFAULT_RESEARCH_QUESTION.toString()))
            .andExpect(jsonPath("$.hypothesis").value(DEFAULT_HYPOTHESIS.toString()))
            .andExpect(jsonPath("$.methods").value(DEFAULT_METHODS.toString()))
            .andExpect(jsonPath("$.relatedRequestNumber").value(DEFAULT_RELATED_REQUEST_NUMBER.toString()))
            .andExpect(jsonPath("$.searchQuery").value(DEFAULT_SEARCH_QUERY.toString()))
            .andExpect(jsonPath("$.requestType").value(DEFAULT_REQUEST_TYPE.toString()))
            .andExpect(jsonPath("$.combinedRequest").value(DEFAULT_COMBINED_REQUEST.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingRequestdetail() throws Exception {
        // Get the requestDetail
        restRequestdetailMockMvc.perform(get("/api/requestdetails/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateRequestdetail() throws Exception {
        // Initialize the database
        requestDetailService.save(requestDetail);

        int databaseSizeBeforeUpdate = requestDetailRepository.findAll().size();

        // Update the requestDetail
        RequestDetail updatedRequestDetail = requestDetailRepository.findOne(requestDetail.getId());
        updatedRequestDetail
                .title(UPDATED_TITLE)
                .background(UPDATED_BACKGROUND)
                .researchQuestion(UPDATED_RESEARCH_QUESTION)
                .hypothesis(UPDATED_HYPOTHESIS)
                .methods(UPDATED_METHODS)
                .relatedRequestNumber(UPDATED_RELATED_REQUEST_NUMBER)
                .searchQuery(UPDATED_SEARCH_QUERY)
                .requestType(UPDATED_REQUEST_TYPE)
                .combinedRequest(UPDATED_COMBINED_REQUEST);

        restRequestdetailMockMvc.perform(put("/api/requestdetails")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedRequestDetail)))
            .andExpect(status().isOk());

        // Validate the RequestDetail in the database
        List<RequestDetail> requestDetailList = requestDetailRepository.findAll();
        assertThat(requestDetailList).hasSize(databaseSizeBeforeUpdate);
        RequestDetail testRequestDetail = requestDetailList.get(requestDetailList.size() - 1);
        assertThat(testRequestDetail.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testRequestDetail.getBackground()).isEqualTo(UPDATED_BACKGROUND);
        assertThat(testRequestDetail.getResearchQuestion()).isEqualTo(UPDATED_RESEARCH_QUESTION);
        assertThat(testRequestDetail.getHypothesis()).isEqualTo(UPDATED_HYPOTHESIS);
        assertThat(testRequestDetail.getMethods()).isEqualTo(UPDATED_METHODS);
        assertThat(testRequestDetail.getRelatedRequestNumber()).isEqualTo(UPDATED_RELATED_REQUEST_NUMBER);
        assertThat(testRequestDetail.getSearchQuery()).isEqualTo(UPDATED_SEARCH_QUERY);
        assertThat(testRequestDetail.getRequestType()).isEqualTo(UPDATED_REQUEST_TYPE);
        assertThat(testRequestDetail.isCombinedRequest()).isEqualTo(UPDATED_COMBINED_REQUEST);

        // Validate the RequestDetail in Elasticsearch
        RequestDetail requestDetailEs = requestdetailSearchRepository.findOne(testRequestDetail.getId());
        assertThat(requestDetailEs).isEqualToComparingFieldByField(testRequestDetail);
    }

    @Test
    @Transactional
    public void updateNonExistingRequestdetail() throws Exception {
        int databaseSizeBeforeUpdate = requestDetailRepository.findAll().size();

        // Create the RequestDetail

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restRequestdetailMockMvc.perform(put("/api/requestdetails")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(requestDetail)))
            .andExpect(status().isCreated());

        // Validate the RequestDetail in the database
        List<RequestDetail> requestDetailList = requestDetailRepository.findAll();
        assertThat(requestDetailList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteRequestdetail() throws Exception {
        // Initialize the database
        requestDetailService.save(requestDetail);

        int databaseSizeBeforeDelete = requestDetailRepository.findAll().size();

        // Get the requestDetail
        restRequestdetailMockMvc.perform(delete("/api/requestdetails/{id}", requestDetail.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean requestdetailExistsInEs = requestdetailSearchRepository.exists(requestDetail.getId());
        assertThat(requestdetailExistsInEs).isFalse();

        // Validate the database is empty
        List<RequestDetail> requestDetailList = requestDetailRepository.findAll();
        assertThat(requestDetailList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchRequestdetail() throws Exception {
        // Initialize the database
        requestDetailService.save(requestDetail);

        // Search the requestDetail
        restRequestdetailMockMvc.perform(get("/api/_search/requestdetails?query=id:" + requestDetail.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(requestDetail.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].background").value(hasItem(DEFAULT_BACKGROUND.toString())))
            .andExpect(jsonPath("$.[*].researchQuestion").value(hasItem(DEFAULT_RESEARCH_QUESTION.toString())))
            .andExpect(jsonPath("$.[*].hypothesis").value(hasItem(DEFAULT_HYPOTHESIS.toString())))
            .andExpect(jsonPath("$.[*].methods").value(hasItem(DEFAULT_METHODS.toString())))
            .andExpect(jsonPath("$.[*].relatedRequestNumber").value(hasItem(DEFAULT_RELATED_REQUEST_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].searchQuery").value(hasItem(DEFAULT_SEARCH_QUERY.toString())))
            .andExpect(jsonPath("$.[*].requestType").value(hasItem(DEFAULT_REQUEST_TYPE.toString())))
            .andExpect(jsonPath("$.[*].combinedRequest").value(hasItem(DEFAULT_COMBINED_REQUEST.booleanValue())));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(RequestDetail.class);
    }
}
