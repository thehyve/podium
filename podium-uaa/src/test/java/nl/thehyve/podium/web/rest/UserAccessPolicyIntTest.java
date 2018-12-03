/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.common.test.Action;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.service.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static nl.thehyve.podium.common.test.Action.format;
import static nl.thehyve.podium.common.test.Action.newAction;

/**
 * Integration test for the access policy on actions on users
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = PodiumUaaApp.class)
public class UserAccessPolicyIntTest extends AbstractUaaAccessPolicyIntTest {

    @Autowired
    UserMapper userMapper;

    private List<Action> actions = new ArrayList<>();

    private User fetchUser;
    private Map<UUID, UserRepresentation> newUsers = new HashMap<>();
    private Map<UUID, String> deleteUserLogins = new HashMap<>();

    void prepareTestUsers() throws UserAccountException {
        fetchUser = testService.createUser("fetch", AuthorityConstants.RESEARCHER);
        int i = 1;
        for (AuthenticatedUser user: allUsers) {
            UUID userUuid = user == null ? null : user.getUuid();
            UserRepresentation newUser = new UserRepresentation();
            String id = Integer.toString(i);
            newUser.setLogin("new_user_" + id);
            newUser.setEmail("new_user+" + id + "@localhost");
            newUser.setFirstName("Test");
            newUser.setLastName("User");
            newUser.setTelephone("0123456789");
            newUser.setDepartment("Test department");
            newUser.setInstitute("Test institute");
            newUser.setSpecialism("Testing");
            newUser.setJobTitle("Tester");
            newUsers.put(userUuid, newUser);
            User deleteUser = testService.createUser(id, AuthorityConstants.RESEARCHER);
            deleteUserLogins.put(userUuid, deleteUser.getLogin());
            i++;
        }
    }

    /**
     * Creates a map from user UUID to a url with a URL with a identifying string specific for the user
     * The query string should have a '%s' format specifier where the string should be placed.
     */
    Map<UUID, String> getUrlsForUsers(String query, Map<UUID, String> objectMap) {
        return getUrlsForUsers(allUsers, USER_ROUTE, query, objectMap);
    }

    private void createActions() {
        // Users

        // TODO: GET  /_search/users
        // TODO: GET  /_suggest/users
        // GET  /users
        actions.add(newAction()
            .setUrl(USER_ROUTE)
            .allow(bbmriAdmin, podiumAdmin, adminOrganisationA, adminOrganisationAandB, adminOrganisationB)
        );
        // POST  /users
        actions.add(newAction()
            .setUrl(USER_ROUTE)
            .setMethod(HttpMethod.POST)
            .bodyMap(newUsers)
            .successStatus(HttpStatus.CREATED)
            .allow(bbmriAdmin, podiumAdmin));
        // PUT  /users
        UserRepresentation accountInfo = userMapper.userToUserDTO(researcher);
        accountInfo.setDepartment("Updated test department");
        accountInfo.setInstitute("Updated test institute");
        accountInfo.setSpecialism("Update testing");
        accountInfo.setJobTitle("Updated tester");
        accountInfo.setTelephone("1234567890");
        actions.add(newAction()
            .setUrl(USER_ROUTE)
            .setMethod(HttpMethod.PUT)
            .body(accountInfo)
            .allow(bbmriAdmin, podiumAdmin));
        // GET  /users/organisations
        actions.add(newAction()
            .setUrl(USER_ROUTE + "/organisations")
            .allow(adminOrganisationA, adminOrganisationAandB, adminOrganisationB)
        );
        // GET  /users/organisations/{uuid}
        actions.add(newAction()
            .setUrl(format(USER_ROUTE, "/organisations/%s", organisationA.getUuid()))
            .allow(adminOrganisationA, adminOrganisationAandB)
        );
        actions.add(newAction()
            .setUrl(format(USER_ROUTE, "/organisations/%s", organisationB.getUuid()))
            .allow(adminOrganisationAandB, adminOrganisationB)
        );
        // GET  /users/uuid/{uuid}
        actions.add(newAction()
                .setUrl(format(USER_ROUTE, "/uuid/%s", researcher.getUuid()))
                .allow(bbmriAdmin, podiumAdmin, adminOrganisationA, adminOrganisationAandB, adminOrganisationB));
        // PUT  /users/uuid/{uuid}/unlock
        actions.add(newAction()
            .setUrl(format(USER_ROUTE, "/uuid/%s/unlock", researcher.getUuid()))
            .setMethod(HttpMethod.PUT)
            .allow(bbmriAdmin, podiumAdmin));
        // DELETE  /api/users/{login}
        actions.add(newAction()
            .setUrls(getUrlsForUsers("/%s", deleteUserLogins))
            .setMethod(HttpMethod.DELETE)
            .allow(bbmriAdmin, podiumAdmin));
        // GET  /users/{login}
        actions.add(newAction()
            .setUrl(USER_ROUTE + "/" + fetchUser.getLogin())
            .allow(bbmriAdmin, podiumAdmin)
        );
    }

    @Test
    @Transactional
    public void testAccessPolicy() throws Exception {
        setupData();
        prepareTestUsers();
        createActions();
        runAll(actions, allUsers);
    }

}
