/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.common.event.EventType;
import nl.thehyve.podium.domain.PersistentAuditEvent;
import nl.thehyve.podium.repository.PersistenceAuditEventRepository;
import nl.thehyve.podium.service.AuditEventService;
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the {@link InternalAuditServer}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PodiumUaaApp.class)
public class InternalAuditServerIntTest {

    @Autowired
    private AuditEventService auditEventService;

    @Autowired
    private PersistenceAuditEventRepository persistenceAuditEventRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc mockMvc;

    private AuditEvent event;

    private Logger log = LoggerFactory.getLogger(getClass());

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        InternalAuditServer internalAuditServer = new InternalAuditServer(auditEventService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(internalAuditServer)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    public static AuditEvent createEvent() {
        //Instant instant = Instant.parse("2015-08-04T10:11:30");
        Date date = new Date(); //Date.from(instant);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sourceStatus", RequestReviewStatus.Review);
        data.put("targetStatus", RequestReviewStatus.Revision);
        data.put("requestUuid", UUID.randomUUID());
        return new AuditEvent(
            date,
            "mockUser",
            EventType.Authentication_Success.toString(),
            data
        );
    }

    @Before
    public void initTest() {
        persistenceAuditEventRepository.deleteAll();
        event = createEvent();
    }

    @Test
    @Transactional
    public void addEvent() throws Exception {
        log.info("Add event");
        int databaseSizeBeforeCreate = persistenceAuditEventRepository.findAll().size();
        log.info("Database size: {}", databaseSizeBeforeCreate);

        // Add the event
        mockMvc.perform(post("/internal/audit/events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(event)))
            .andExpect(status().isCreated());

        // Validate the event in the database
        List<PersistentAuditEvent> eventList = persistenceAuditEventRepository.findAll();
        assertThat(eventList).hasSize(databaseSizeBeforeCreate + 1);
    }


}
