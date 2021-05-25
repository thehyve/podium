/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.config;

import org.elasticsearch.client.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.repository.config.*;

@Configuration
@EnableElasticsearchRepositories("nl.thehyve.podium.repository.search")
public class ElasticSearchConfiguration {
    @Bean
    public ElasticsearchOperations elasticsearchTemplate(RestHighLevelClient client) {
        return new ElasticsearchRestTemplate(client);
    }
}
