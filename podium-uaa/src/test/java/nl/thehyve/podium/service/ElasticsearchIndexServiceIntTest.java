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
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.repository.UserRepository;
import nl.thehyve.podium.repository.search.OrganisationSearchRepository;
import nl.thehyve.podium.repository.search.UserSearchRepository;
import nl.thehyve.podium.service.util.RandomUtil;
import nl.thehyve.podium.web.rest.vm.ManagedUserVM;
import org.elasticsearch.client.Client;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for the ElasticsearchService.
 *
 * @see ElasticsearchIndexService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PodiumUaaApp.class)
@Transactional
public class ElasticsearchIndexServiceIntTest {

    Logger log = LoggerFactory.getLogger(ElasticsearchIndexServiceIntTest.class);

    @Autowired
    private ElasticsearchIndexService elasticsearchIndexService;

    @Autowired
    private UserSearchRepository userSearchRepository;

    @Autowired
    private OrganisationSearchRepository organisationSearchRepository;

    @Test
    public void assertThatEntitiesAreMapped() throws Exception {
        Long beforeIndexSearchOrganisations = organisationSearchRepository.count();
        Long beforeIndexSearchUsers = userSearchRepository.count();

        assertThat(beforeIndexSearchOrganisations).isEqualTo(0);
        assertThat(beforeIndexSearchUsers).isEqualTo(0);

        // Wait for the future to complete
        Future future = elasticsearchIndexService.reindexAll();
        future.get();

        Long afterIndexSearchOrganisations = organisationSearchRepository.count();
        Long afterIndexSearchUsers = userSearchRepository.count();

        assertThat(afterIndexSearchOrganisations).isEqualTo(1);
        assertThat(afterIndexSearchUsers).isEqualTo(7);

    }

}
