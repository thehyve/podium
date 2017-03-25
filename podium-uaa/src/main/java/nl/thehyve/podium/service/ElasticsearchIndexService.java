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

/**
 * Created by bernd on 25/03/2017.
 */
import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.repository.OrganisationRepository;
import nl.thehyve.podium.repository.UserRepository;
import nl.thehyve.podium.repository.search.OrganisationSearchRepository;
import nl.thehyve.podium.repository.search.UserSearchRepository;
import nl.thehyve.podium.search.SearchOrganisation;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

@Service
public class ElasticsearchIndexService {

    private final Logger log = LoggerFactory.getLogger(ElasticsearchIndexService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSearchRepository userSearchRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private OrganisationSearchRepository organisationSearchRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public ElasticsearchIndexService() {

    }

    @Async
    @Timed
    public void reindexAll() {
            reindexForClass(Organisation.class, organisationRepository, organisationSearchRepository);

            reindexForClass(User.class, userRepository, userSearchRepository);

            log.info("Elasticsearch: Successfully performed reindexing");
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    private <T, ID extends Serializable> void reindexForClass(Class<T> entityClass, JpaRepository<T, ID> jpaRepository,
                                                              ElasticsearchRepository<T, ID> elasticsearchRepository) {
        elasticsearchTemplate.deleteIndex(entityClass);
        try {
            elasticsearchTemplate.createIndex(entityClass);
        } catch (IndexAlreadyExistsException e) {
            // Do nothing. Index was already concurrently recreated by some other service.
        }
        elasticsearchTemplate.putMapping(entityClass);
        if (jpaRepository.count() > 0) {
            try {
                elasticsearchRepository.save(jpaRepository.findAll());
                // Method m = jpaRepository.getClass().getMethod("findAllWithEagerRelationships");
                // elasticsearchRepository.save((List<T>) m.invoke(jpaRepository));
            } catch (Exception e) {
                elasticsearchRepository.save(jpaRepository.findAll());
            }
        }
        log.info("Elasticsearch: Indexed all rows for " + entityClass.getSimpleName());
    }

}
