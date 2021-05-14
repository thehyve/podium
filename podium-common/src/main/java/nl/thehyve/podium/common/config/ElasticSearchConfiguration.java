/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
// import org.elasticsearch.client.Client;
// import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
// import org.springframework.data.elasticsearch.core.EntityMapper;
// import org.springframework.data.mapping.model.MappingException;
// import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ElasticSearchConfiguration {

    // @Bean
    // public ElasticsearchTemplate elasticsearchTemplate(Client client, Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
    //     return new ElasticsearchTemplate(client, new CustomEntityMapper(jackson2ObjectMapperBuilder.createXmlMapper(false).build()));
    // }

    // public class CustomEntityMapper implements EntityMapper {

    //     private ObjectMapper objectMapper;

    //     public CustomEntityMapper(ObjectMapper objectMapper) {
    //         this.objectMapper = objectMapper;
    //         objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    //         objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    //     }

    //     @Override
    //     public String mapToString(Object object) {
    //         try {
    //             return objectMapper.writeValueAsString(object);
    //         } catch (IOException e) {
    //             throw new MappingException(e.getMessage(), e);
    //         }
    //     }

    //     @Override
    //     public <T> T mapToObject(String source, Class<T> clazz) {
    //         try {
    //         return objectMapper.readValue(source, clazz);
    //         } catch (IOException e) {
    //             throw new MappingException(e.getMessage(), e);
    //         }
    //     }
    // }
}
