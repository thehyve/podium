/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.common.test.Action;
import nl.thehyve.podium.service.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static nl.thehyve.podium.common.test.Action.newAction;

/**
 * Integration test for the access policy on actions on the user account
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = PodiumUaaApp.class)
public class AccountAccessPolicyIntTest extends AbstractUaaAccessPolicyIntTest {

    @Autowired
    private UserMapper userMapper;

    private List<Action> actions = new ArrayList<>();

    private void createActions() {
        // Account

        // GET  /account
        actions.add(newAction()
            .setUrl(ACCOUNT_ROUTE)
            .allow(getAllExceptAnonymous())
        );
        // POST  /account
        UserRepresentation accountInfo = userMapper.userToUserDTO(researcher);
        accountInfo.setDepartment("Test department");
        accountInfo.setInstitute("Test institute");
        accountInfo.setSpecialism("Testing");
        accountInfo.setJobTitle("Tester");
        accountInfo.setTelephone("0123456789");
        actions.add(newAction()
            .setUrl(ACCOUNT_ROUTE)
            .setMethod(HttpMethod.POST)
            .body(accountInfo)
            .allow(getAllExceptAnonymous()));
        // FIXME: POST  /account/change_password
        // Fails because of HttpMediaTypeNotAcceptableException
        // actions.add(newAction()
        //    .setUrl(ACCOUNT_ROUTE + "/change_password")
        //    .setMethod(HttpMethod.POST)
        //    .accept(MediaType.TEXT_PLAIN)
        //    .body("new password")
        //    .allow(getAllExceptAnonymous()));
    }

    @Test
    @Transactional
    public void testAccessPolicy() throws Exception {
        setupData();
        createActions();
        runAll(actions, allUsers);
    }

}
