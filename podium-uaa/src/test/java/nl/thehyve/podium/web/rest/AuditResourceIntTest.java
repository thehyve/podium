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
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.event.EventType;
import nl.thehyve.podium.common.service.dto.AuditEventRepresentation;
import nl.thehyve.podium.config.audit.AuditEventConverter;
import nl.thehyve.podium.domain.PersistentAuditEvent;
import nl.thehyve.podium.repository.CustomAuditEventRepository;
import nl.thehyve.podium.repository.PersistenceAuditEventRepository;
import nl.thehyve.podium.service.AuditEventService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the AuditResource REST controller.
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PodiumUaaApp.class)
@Transactional
public class AuditResourceIntTest {

    private static final String SAMPLE_PRINCIPAL = "SAMPLE_PRINCIPAL";
    private static final EventType SAMPLE_TYPE = EventType.Status_Change;
    private static final Instant SAMPLE_INSTANT = Instant.now();
    private static final Date SAMPLE_TIMESTAMP = Date.from(SAMPLE_INSTANT);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Logger log = LoggerFactory.getLogger(AccountResourceIntTest.class);

    @Autowired
    private CustomAuditEventRepository customAuditEventRepository;

    @Autowired
    private PersistenceAuditEventRepository auditEventRepository;

    @Autowired
    private AuditEventConverter auditEventConverter;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private FormattingConversionService formattingConversionService;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private PersistentAuditEvent auditEvent;

    private MockMvc restAuditMockMvc;

    private TypeReference<List<AuditEventRepresentation>> listTypeReference =
        new TypeReference<List<AuditEventRepresentation>>(){};

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        AuditEventService auditEventService =
                new AuditEventService(customAuditEventRepository, auditEventRepository, auditEventConverter);
        AuditResource auditResource = new AuditResource(auditEventService);
        this.restAuditMockMvc = MockMvcBuilders.standaloneSetup(auditResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setConversionService(formattingConversionService)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        auditEventRepository.deleteAll();
        auditEvent = new PersistentAuditEvent();
        auditEvent.setEventType(SAMPLE_TYPE);
        auditEvent.setPrincipal(SAMPLE_PRINCIPAL);
        auditEvent.setEventDate(SAMPLE_TIMESTAMP);
    }

    @Test
    public void getAllAudits() throws Exception {
        // Initialize the database
        auditEventRepository.save(auditEvent);

        // Get all the audits
        restAuditMockMvc.perform(get("/management/audits"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andDo(result -> {
                    log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                    List<AuditEventRepresentation> events =
                        mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
                    Assert.assertEquals(1, events.size());
                    for(AuditEventRepresentation event: events) {
                        log.info(" - {}", event);
                    }
                })
                .andExpect(jsonPath("$.[*].principal").value(hasItem(SAMPLE_PRINCIPAL)));
    }

    @Test
    public void getAudit() throws Exception {
        // Initialize the database
        auditEventRepository.save(auditEvent);

        // Get the audit
        restAuditMockMvc.perform(get("/management/audits/{id}", auditEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.principal").value(SAMPLE_PRINCIPAL));
    }

    @Test
    public void getAuditsByDate() throws Exception {
        // Initialize the database
        auditEventRepository.save(auditEvent);

        // Generate dates for selecting audits by date, making sure the period will contain the audit
        String fromDate  = FORMATTER.format(LocalDateTime.ofInstant(SAMPLE_INSTANT, ZoneId.systemDefault()).minusDays(1));
        String toDate = FORMATTER.format(LocalDateTime.ofInstant(SAMPLE_INSTANT, ZoneId.systemDefault()).plusDays(1));
        // Get the audit
        restAuditMockMvc.perform(get("/management/audits?fromDate="+fromDate+"&toDate="+toDate))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andDo(result -> {
                log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                List<AuditEventRepresentation> events =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
                Assert.assertEquals(1, events.size());
                for(AuditEventRepresentation event: events) {
                    log.info(" - {}", event);
                }
            })
            .andExpect(jsonPath("$.[*].principal").value(hasItem(SAMPLE_PRINCIPAL)));
    }

    @Test
    public void getNonExistingAuditsByDate() throws Exception {
        // Initialize the database
        auditEventRepository.save(auditEvent);

        // Generate dates for selecting audits by date, making sure the period will not contain the sample audit
        String fromDate  = FORMATTER.format(LocalDateTime.ofInstant(SAMPLE_INSTANT, ZoneId.systemDefault()).minusDays(2));
        String toDate = FORMATTER.format(LocalDateTime.ofInstant(SAMPLE_INSTANT, ZoneId.systemDefault()).minusDays(1));

        // Query audits but expect no results
        restAuditMockMvc.perform(get("/management/audits?fromDate=" + fromDate + "&toDate=" + toDate))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(header().string("X-Total-Count", "0"));
    }

    @Test
    public void getNonExistingAudit() throws Exception {
        // Get the audit
        restAuditMockMvc.perform(get("/management/audits/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

}
