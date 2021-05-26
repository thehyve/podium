/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.repository.search.UserSearchRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for the ElasticsearchService.
 *
 * @see ElasticsearchIndexService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PodiumUaaApp.class)
@DirtiesContext
public class ElasticsearchIndexServiceIntTest {

    Logger log = LoggerFactory.getLogger(ElasticsearchIndexServiceIntTest.class);

    @Autowired
    private ElasticsearchIndexService elasticsearchIndexService;

    @Autowired
    private UserSearchRepository userSearchRepository;

    @Autowired
    private TestService testService;

    @Test
    public void assertThatEntitiesAreMapped() throws Exception {
        Long beforeIndexSearchUsers = userSearchRepository.count();

        assertThat(beforeIndexSearchUsers).isEqualTo(0);

        Organisation organisation = testService.createOrganisation("Test organisation");
        testService.createUser("User1", AuthorityConstants.RESEARCHER);
        testService.createUser("Reviewer2", AuthorityConstants.REVIEWER, organisation);

        // Wait for the future to complete
        Future future = elasticsearchIndexService.reindexAll();
        future.get();

        Long afterIndexSearchUsers = userSearchRepository.count();

        assertThat(afterIndexSearchUsers).isEqualTo(2);
    }
}
