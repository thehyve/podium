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

import com.fasterxml.jackson.databind.*;
import lombok.*;
import org.elasticsearch.client.*;
import org.springframework.context.annotation.*;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.mapping.*;
import org.springframework.http.converter.json.*;

import java.io.*;
import java.util.*;

@Configuration
public class ElasticSearchConfiguration {

    @Bean
    public ElasticsearchOperations elasticsearchTemplate(RestHighLevelClient client, Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
        return new ElasticsearchRestTemplate(client, new CustomEntityMapper(jackson2ObjectMapperBuilder.createXmlMapper(false).build()));
    }

    public static class CustomEntityMapper implements EntityMapper {

        private final ObjectMapper objectMapper;

        public CustomEntityMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        }

        @Override
        public String mapToString(Object object) {
            try {
                return objectMapper.writeValueAsString(object);
            } catch (IOException e) {
                throw new MappingException(e.getMessage(), e);
            }
        }

        @Override
        public <T> T mapToObject(String source, Class<T> clazz) {
            try {
                return objectMapper.readValue(source, clazz);
            } catch (IOException e) {
                throw new MappingException(e.getMessage(), e);
            }
        }

        @SneakyThrows
        @Override
        public Map<String, Object> mapObject(Object o) {
            return objectMapper.readValue(mapToString(objectMapper), Map.class);
        }

        @SneakyThrows
        @Override
        public <T> T readObject(Map<String, Object> map, Class<T> aClass) {
            return mapToObject(objectMapper.writeValueAsString(map), aClass);
        }
    }
}
