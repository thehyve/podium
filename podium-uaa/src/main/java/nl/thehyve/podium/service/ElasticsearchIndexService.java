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
import nl.thehyve.podium.search.SearchUser;
import nl.thehyve.podium.service.mapper.OrganisationMapper;
import nl.thehyve.podium.service.mapper.UserMapper;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

@Service
public class ElasticsearchIndexService {

    private final Logger log = LoggerFactory.getLogger(ElasticsearchIndexService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserSearchRepository userSearchRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private OrganisationMapper organisationMapper;

    @Autowired
    private OrganisationSearchRepository organisationSearchRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public ElasticsearchIndexService() {

    }

    @Async
    @Timed
    public Future<String> reindexAll() {

        // Reindex Organisations to SearchOrganisations
        reindexForClass(
            Organisation.class, organisationRepository,
            SearchOrganisation.class, organisationSearchRepository,
            (List<Organisation> organisations) -> organisationMapper.organisationsToSearchOrganisations(organisations));

        // Reindex Users -> SearchUsers
        reindexForClass(
            User.class, userRepository,
            SearchUser.class, userSearchRepository,
            (List<User> users) -> userMapper.usersToSearchUsers(users));

        log.info("Elasticsearch: Successfully performed reindexing");
        return new AsyncResult<String>("Indexing finished");
    }

    /**
     * Service for reindexing an entity in Elasticsearch.
     *
     * @param entityClass             The java entity to that has to be indexed in elasticsearch
     * @param jpaRepository           Instance of a java entity jpa repository
     * @param searchEntityClass       The Elasticsearch document entity that will be used in the mapping of the java entity
     * @param elasticsearchRepository Instane of the elasticsearchrepository for this document entity
     * @param mapperFunction          The mapper function to apply for the transformation of the entity to the searchentity
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    private <T, ID extends Serializable, S> void reindexForClass(
        Class<T> entityClass, JpaRepository<T, ID> jpaRepository,
        Class<S> searchEntityClass, ElasticsearchRepository<S, ID> elasticsearchRepository,
        Function<List<T>, List<S>> mapperFunction
    ) {
        elasticsearchTemplate.deleteIndex(searchEntityClass);
        try {
            elasticsearchTemplate.createIndex(searchEntityClass);
        } catch (IndexAlreadyExistsException e) {
            // Do nothing. Index was already concurrently recreated by some other service.
        }

        elasticsearchTemplate.putMapping(searchEntityClass);
        if (jpaRepository.count() > 0) {
            try {
                // Fetch all entities using reflection
                Method m = jpaRepository.getClass().getMethod("findAll");
                List<T> entities = (List<T>) m.invoke(jpaRepository);

                List<S> searchEntities = mapperFunction.apply(entities);
                elasticsearchRepository.save(searchEntities);
            } catch (Exception e) {
                log.error("ELasticsearchIndexer error: {}", e);
            }
        }
        log.info("Elasticsearch: Indexed all rows for " + entityClass.getSimpleName());
    }

}
