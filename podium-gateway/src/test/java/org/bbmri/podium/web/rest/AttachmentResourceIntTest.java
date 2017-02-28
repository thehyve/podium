package org.bbmri.podium.web.rest;

import org.bbmri.podium.PodiumGatewayApp;

import org.bbmri.podium.domain.Attachment;
import org.bbmri.podium.repository.AttachmentRepository;
import org.bbmri.podium.service.AttachmentService;
import org.bbmri.podium.repository.search.AttachmentSearchRepository;

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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.bbmri.podium.domain.enumeration.AttachmentType;
/**
 * Test class for the AttachmentResource REST controller.
 *
 * @see AttachmentResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PodiumGatewayApp.class)
public class AttachmentResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_FILENAME = "AAAAAAAAAA";
    private static final String UPDATED_FILENAME = "BBBBBBBBBB";

    private static final AttachmentType DEFAULT_TYPE = AttachmentType.MTA;
    private static final AttachmentType UPDATED_TYPE = AttachmentType.DTA;

    private static final LocalDate DEFAULT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_MIME_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_MIME_TYPE = "BBBBBBBBBB";

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private AttachmentSearchRepository attachmentSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private EntityManager em;

    private MockMvc restAttachmentMockMvc;

    private Attachment attachment;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        AttachmentResource attachmentResource = new AttachmentResource(attachmentService);
        this.restAttachmentMockMvc = MockMvcBuilders.standaloneSetup(attachmentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Attachment createEntity(EntityManager em) {
        Attachment attachment = new Attachment()
                .name(DEFAULT_NAME)
                .description(DEFAULT_DESCRIPTION)
                .filename(DEFAULT_FILENAME)
                .type(DEFAULT_TYPE)
                .date(DEFAULT_DATE)
                .mimeType(DEFAULT_MIME_TYPE);
        UUID uploader = UUID.randomUUID();
        attachment.setUploader(uploader);
        return attachment;
    }

    @Before
    public void initTest() {
        attachmentSearchRepository.deleteAll();
        attachment = createEntity(em);
    }

    @Test
    @Transactional
    public void searchAttachment() throws Exception {
        // Initialize the database
        attachmentService.save(attachment);

        // Search the attachment
        restAttachmentMockMvc.perform(get("/api/_search/attachments?query=id:" + attachment.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(attachment.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].filename").value(hasItem(DEFAULT_FILENAME.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].mimeType").value(hasItem(DEFAULT_MIME_TYPE.toString())));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Attachment.class);
    }
}
