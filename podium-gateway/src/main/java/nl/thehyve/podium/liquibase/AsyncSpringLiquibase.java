/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.liquibase;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.StopWatch;

/**
 * Specific liquibase.integration.spring.SpringLiquibase that will update the database asynchronously.
 * <p>
 *     By default, this asynchronous version only works when using the "dev" profile.<p>
 *     The standard liquibase.integration.spring.SpringLiquibase starts Liquibase in the current thread:
 *     <ul>
 *         <li>This is needed if you want to do some database requests at startup</li>
 *         <li>This ensure that the database is ready when the application starts</li>
 *     </ul>
 *     But as this is a rather slow process, we use this asynchronous version to speed up our start-up time:
 *     <ul>
 *         <li>On a recent MacBook Pro, start-up time is down from 14 seconds to 8 seconds</li>
 *         <li>In production, this can help your application run on platforms like Heroku, where it must start/restart very quickly</li>
 *     </ul>
 */
public class AsyncSpringLiquibase extends SpringLiquibase {
    private final Logger logger = LoggerFactory.getLogger(AsyncSpringLiquibase.class);
    private final TaskExecutor taskExecutor;
    private final Environment env;

    public AsyncSpringLiquibase(@Qualifier("taskExecutor") TaskExecutor taskExecutor, Environment env) {
        this.taskExecutor = taskExecutor;
        this.env = env;
    }

    public void afterPropertiesSet() throws LiquibaseException {
        if(!this.env.acceptsProfiles(new String[]{"no-liquibase"})) {
            if(this.env.acceptsProfiles(new String[]{"dev", "heroku"})) {
                this.taskExecutor.execute(() -> {
                    try {
                        this.logger.warn("Starting Liquibase asynchronously, your database might not be ready at startup!");
                        this.initDb();
                    } catch (LiquibaseException var2) {
                        this.logger.error("Liquibase could not start correctly, your database is NOT ready: {}", var2.getMessage(), var2);
                    }

                });
            } else {
                this.logger.debug("Starting Liquibase synchronously");
                this.initDb();
            }
        } else {
            this.logger.debug("Liquibase is disabled");
        }

    }

    protected void initDb() throws LiquibaseException {
        StopWatch watch = new StopWatch();
        watch.start();
        super.afterPropertiesSet();
        watch.stop();
        this.logger.debug("Started Liquibase in {} ms", Long.valueOf(watch.getTotalTimeMillis()));
    }
}
