/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.test.Action;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Integration test for the access policy on actions on users
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = PodiumUaaApp.class)
public class UserAccessPolicyIntTest extends AbstractUaaAccessPolicyIntTest {

    private List<Action> actions = new ArrayList<>();

    private void createActions() {
        // Users

        // TODO: GET  /_search/users
        // TODO: GET  /_suggest/users
        // TODO: GET  /users
        // TODO: POST  /users
        // TODO: PUT  /users
        // TODO: GET  /users/organisations
        // TODO: GET  /users/organisations/{uuid}
        // TODO: GET  /users/uuid/{uuid}
        // TODO: PUT  /users/uuid/{uuid}/unlock
        // TODO: DELETE  /api/users/{login}
        // TODO: GET  /users/{login}
    }

    @Test
    @Transactional
    public void testAccessPolicy() throws Exception {
        setupData();
        createActions();
        runAll(actions, allUsers);
    }

}
