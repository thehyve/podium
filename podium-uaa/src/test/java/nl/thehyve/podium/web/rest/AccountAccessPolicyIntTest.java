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
 * Integration test for the access policy on actions on the user account
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = PodiumUaaApp.class)
public class AccountAccessPolicyIntTest extends AbstractUaaAccessPolicyIntTest {

    private List<Action> actions = new ArrayList<>();

    private void createActions() {
        // Account

        // TODO: GET  /account
        // TODO: POST  /account
        // TODO: POST  /account/change_password
        // TODO: POST  /account/reset_password/finish
        // TODO: POST  /account/reset_password/init
        // TODO: GET  /authenticate
        // TODO: POST  /api/register
        // TODO: GET  /reverify
        // TODO: GET  /verify

    }

    @Test
    @Transactional
    public void testAccessPolicy() throws Exception {
        setupData();
        createActions();
        runAll(actions, allUsers);
    }

}
