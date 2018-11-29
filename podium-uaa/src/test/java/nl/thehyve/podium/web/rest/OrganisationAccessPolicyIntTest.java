/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.IdentifiableOrganisation;
import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.service.dto.OrganisationRepresentation;
import nl.thehyve.podium.common.test.Action;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.service.mapper.OrganisationMapper;
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
 * Integration test for the access policy on actions on organisations
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = PodiumUaaApp.class)
public class OrganisationAccessPolicyIntTest extends AbstractUaaAccessPolicyIntTest {

    @Autowired
    OrganisationMapper organisationMapper;

    private static OrganisationRepresentation createTestOrganisationRepresentation() {
        OrganisationRepresentation newOrganisation = new OrganisationRepresentation();
        newOrganisation.setName("New organisation");
        newOrganisation.setShortName("New");
        Set<RequestType> types = new HashSet<>();
        types.add(RequestType.Material);
        newOrganisation.setRequestTypes(types);
        return newOrganisation;
    }

    private OrganisationRepresentation organisationBRepresentation;
    private Map<UUID, Organisation> deleteOrganisations = new HashMap<>();

    private void createTestOrganisations() {
        organisationBRepresentation = organisationMapper.organisationToOrganisationDTO(organisationB);
        for (IdentifiableUser user: allUsers) {
            UUID userUuid = user == null ? null : user.getUserUuid();
            Organisation deleteOrganisation = testService.createOrganisation("Organisation " + userUuid);
            deleteOrganisations.put(userUuid, deleteOrganisation);
        }
    }

    /**
     * Creates a map from user UUID to a url with a URL with a organisation UUID specific for the user
     * The query string should have a '%s' format specifier where the UUID should be placed.
     */
    Map<UUID, String> getUrlsForUsers(String query, Map<UUID, ? extends IdentifiableOrganisation> objectMap) {
        return getUrlsForUsers(allUsers, ORGANISATION_ROUTE, query, objectMap);
    }

    private List<Action> actions = new ArrayList<>();

    private void createActions() {
        // Organisations

        // GET  /_search/organisations
        // GET  /organisations
        actions.add(newAction()
            .setUrl(ORGANISATION_ROUTE)
            .allow(bbmriAdmin));
        // POST /organisations
        actions.add(newAction()
            .setUrl(ORGANISATION_ROUTE)
            .setMethod(HttpMethod.POST)
            .body(createTestOrganisationRepresentation())
            .successStatus(HttpStatus.CREATED)
            .allow(bbmriAdmin));
        // PUT  /organisations
        organisationBRepresentation.setShortName("Edited");
        actions.add(newAction()
            .setUrl(ORGANISATION_ROUTE)
            .setMethod(HttpMethod.PUT)
            .body(organisationBRepresentation)
            .allow(bbmriAdmin, adminOrganisationB, adminOrganisationAandB)
        );
        // GET  /organisations/admin
        actions.add(newAction()
            .setUrl(ORGANISATION_ROUTE + "/admin")
            .allow(bbmriAdmin, adminOrganisationA, adminOrganisationAandB, adminOrganisationB));
        // GET  /organisations/all
        actions.add(newAction()
            .setUrl(ORGANISATION_ROUTE + "/all")
            .allow(bbmriAdmin));
        // GET  /organisations/available
        actions.add(newAction()
            .setUrl(ORGANISATION_ROUTE + "/available")
            .allow(getAllExceptAnonymous()));
        // GET  /organisations/uuid/{uuid}
        actions.add(newAction()
            .setUrl(ORGANISATION_ROUTE + "/uuid/" + organisationA.getUuid().toString())
            .allow(getAllExceptAnonymous()));
        // GET  /organisations/{id}
        actions.add(newAction()
            .setUrl(ORGANISATION_ROUTE + "/" + organisationA.getId().toString())
            .allow(bbmriAdmin));
        // DELETE  /organisations/{uuid}
        actions.add(newAction()
            .setUrls(getUrlsForUsers("/%s", deleteOrganisations))
            .setMethod(HttpMethod.DELETE)
            .allow(bbmriAdmin));
        // PUT /organisations/{uuid}/activation
        actions.add(newAction()
            .setUrl(format(ORGANISATION_ROUTE,  "/%s/activation?value=true", organisationB.getUuid()))
            .setMethod(HttpMethod.PUT)
            .allow(bbmriAdmin)
        );
    }

    @Test
    @Transactional
    public void testAccessPolicy() throws Exception {
        setupData();
        createTestOrganisations();
        createActions();
        runAll(actions, allUsers);
    }

}
