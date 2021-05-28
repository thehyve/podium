/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.config;

import nl.thehyve.podium.common.config.*;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.*;
import org.slf4j.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.liquibase.*;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({LiquibaseConfiguration.class, LiquibaseAutoConfiguration.class})
public class ProcessEngineConfiguration implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {
    private final Logger log = LoggerFactory.getLogger(ProcessEngineConfiguration.class);
    @Override
    public void configure(SpringProcessEngineConfiguration config) {
        log.info("Configuring Process Engine Configuration ...");
        config.setDatabaseSchemaUpdate("false");
    }
}
