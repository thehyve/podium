/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.service.dto.DeliveryProcessRepresentation;
import nl.thehyve.podium.common.service.dto.DeliveryReferenceRepresentation;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.common.test.Action;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static nl.thehyve.podium.common.test.Action.format;
import static nl.thehyve.podium.common.test.Action.newAction;
import static nl.thehyve.podium.web.rest.RequestDataHelper.setRequestData;

/**
 * Integration test for the access policy on delivery actions.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
public class DeliveryAccessPolicyIntTest extends AbstractGatewayAccessPolicyIntTest {

    private Map<UUID, RequestRepresentation> deliveryRequests = new HashMap<>();
    private Map<UUID, DeliveryProcessRepresentation> delivery1 = new HashMap<>();
    private Map<UUID, DeliveryProcessRepresentation> delivery2 = new HashMap<>();
    private Map<UUID, DeliveryProcessRepresentation> delivery3 = new HashMap<>();
    private Map<UUID, RequestRepresentation> approvedRequests = new HashMap<>();

    Collection<RequestRepresentation> createRequests() throws Exception {
        List<RequestRepresentation> requests = new ArrayList<>();
        for(AuthenticatedUser user: allUsers) {
            UUID userUuid = user == null ? null : user.getUserUuid();
            RequestRepresentation deliveryRequest = createApprovedRequest();
            deliveryRequest = createDeliveryProcesses(coordinatorOrganisationA, deliveryRequest);
            deliveryRequests.put(userUuid, deliveryRequest);
            List<DeliveryProcessRepresentation> deliveries = getDeliveryProcesses(coordinatorOrganisationA, deliveryRequest);
            Assert.assertEquals(3, deliveries.size());
            delivery1.put(userUuid, deliveries.get(0));
            DeliveryReferenceRepresentation delivery2Reference = new DeliveryReferenceRepresentation();
            delivery2Reference.setReference("https://example.com/deliveries/12345789");
            delivery2.put(userUuid, releaseDelivery(coordinatorOrganisationA, deliveryRequest, deliveries.get(1), delivery2Reference));
            delivery3.put(userUuid, deliveries.get(2));
            RequestRepresentation approvedRequest = createApprovedRequest();
            approvedRequests.put(userUuid, approvedRequest);
            requests.add(deliveryRequest);
            requests.add(approvedRequest);
        }
        return requests;
    }


    private List<Action> actions = new ArrayList<>();

    private void createActions() {
        // GET /requests/{requestUuid}/deliveries
        actions.add(newAction()
            .setUrls(allUsers.stream()
                .map(user -> user == null ? null : user.getUuid())
                .collect(Collectors.toMap(Function.identity(),
                    userUuid -> format(REQUEST_ROUTE, "/%s/deliveries",
                        deliveryRequests.get(userUuid).getUuid())
                    )))
            .allow(researcher, coordinatorOrganisationA, coordinatorOrganisationAandB));
        // POST /api/requests/{requestUuid}/deliveries/{deliveryProcessUuid}/cancel
        actions.add(newAction()
            .setUrls(allUsers.stream()
                .map(user -> user == null ? null : user.getUuid())
                .collect(Collectors.toMap(Function.identity(),
                    userUuid -> format(REQUEST_ROUTE, "/%s/deliveries/%s/cancel",
                        deliveryRequests.get(userUuid).getUuid(),
                        delivery1.get(userUuid).getUuid())
                    )))
            .setMethod(HttpMethod.POST)
            .body(new MessageRepresentation())
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB));

        // GET /api/requests/{requestUuid}/deliveries/{deliveryProcessUuid}/received
        actions.add(newAction()
            .setUrls(allUsers.stream()
                .map(user -> user == null ? null : user.getUuid())
                .collect(Collectors.toMap(Function.identity(),
                    userUuid -> format(REQUEST_ROUTE, "/%s/deliveries/%s/received",
                        deliveryRequests.get(userUuid).getUuid(),
                        delivery2.get(userUuid).getUuid())
                )))
            .allow(researcher, coordinatorOrganisationA, coordinatorOrganisationAandB));
        // POST /api/requests/{requestUuid}/deliveries/{deliveryProcessUuid}/release
        actions.add(newAction()
            .setUrls(allUsers.stream()
                .map(user -> user == null ? null : user.getUuid())
                .collect(Collectors.toMap(Function.identity(),
                    userUuid -> format(REQUEST_ROUTE, "/%s/deliveries/%s/release",
                        deliveryRequests.get(userUuid).getUuid(),
                        delivery3.get(userUuid).getUuid())
                )))
            .setMethod(HttpMethod.POST)
            .body(new MessageRepresentation())
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB));
        // GET /api/requests/{requestUuid}/startDelivery
        actions.add(newAction()
            .setUrls(allUsers.stream()
                .map(user -> user == null ? null : user.getUuid())
                .collect(Collectors.toMap(Function.identity(),
                    userUuid -> format(REQUEST_ROUTE, "/%s/startDelivery",
                        approvedRequests.get(userUuid).getUuid())
                )))
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB));

    }

    @Test
    public void testAccessPolicy() throws Exception {
        setupData();
        createActions();
        runAll(actions, allUsers);
    }

}
