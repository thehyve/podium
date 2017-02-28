package org.bbmri.podium.web.rest;

import org.bbmri.podium.PodiumGatewayApp;

import org.bbmri.podium.domain.Request;
import org.bbmri.podium.repository.RequestRepository;
import org.bbmri.podium.service.RequestService;
import org.bbmri.podium.repository.search.RequestSearchRepository;
import org.bbmri.podium.service.dto.RequestDTO;
import org.bbmri.podium.service.mapper.RequestMapper;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.bbmri.podium.domain.enumeration.RequestStatus;
/**
 * Test class for the RequestResource REST controller.
 *
 * @see RequestResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PodiumGatewayApp.class)
public class RequestResourceIntTest {

    private static final RequestStatus DEFAULT_STATUS = RequestStatus.DRAFT;
    private static final RequestStatus UPDATED_STATUS = RequestStatus.FIRST_CHECK;

    private static final UUID REQUESTER_UUID = UUID.randomUUID();

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestMapper requestMapper;

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestSearchRepository requestSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private EntityManager em;

    private MockMvc restRequestMockMvc;

    private Request request;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        RequestResource requestResource = new RequestResource(requestService);
        this.restRequestMockMvc = MockMvcBuilders.standaloneSetup(requestResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Request createEntity(EntityManager em) {
        Request request = new Request()
                .status(DEFAULT_STATUS);
        request.setRequester(REQUESTER_UUID);

        return request;
    }

    @Before
    public void initTest() {
        requestSearchRepository.deleteAll();
        request = createEntity(em);
    }

    @Test
    @Transactional
    public void initializeRequest() throws Exception {
        int databaseSizeBeforeCreate = requestRepository
                .findAllByRequesterAndStatus(REQUESTER_UUID, DEFAULT_STATUS).size();

        restRequestMockMvc.perform(get("/api/requests/initialize/"+REQUESTER_UUID))
            .andExpect(status().isCreated());

        int databaseSizeAfterCreate = requestRepository
                .findAllByRequesterAndStatus(REQUESTER_UUID, DEFAULT_STATUS).size();
        assertThat(databaseSizeAfterCreate).isEqualTo(databaseSizeBeforeCreate + 1);
    }

    @Test
    @Transactional
    public void searchRequest() throws Exception {
        // Initialize the database
        requestRepository.saveAndFlush(request);
        requestSearchRepository.save(request);

        // Search the request
        restRequestMockMvc.perform(get("/api/_search/requests?query=id:" + request.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(request.getId().intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Request.class);
    }
}
