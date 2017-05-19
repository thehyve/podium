/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import {AdminConsole} from "../protractor-stories/admin-console";
import PersonaDictionary = require("../personas/persona-dictionary")
import initDataDictionary = require("../data/data-dictionary")
let {defineSupportCode} = require('cucumber');

defineSupportCode(function({After, Before}) {

    function setupUsers(adminConsole: AdminConsole, personas: string[]) {
        let createUserCalls = [];

        personas.forEach(function (value) {
            createUserCalls.push(adminConsole.createUser(PersonaDictionary[value]));
        });
        return Promise.all(createUserCalls);
    }

    function setupOrganizations(adminConsole: AdminConsole, organizations: string[]) {
        let DataDictionary = initDataDictionary;
        let createOrganizationsCalls = [];

        organizations.forEach(function (value) {
            createOrganizationsCalls.push(adminConsole.createOrganization(DataDictionary[value]));
        });
        return Promise.all(createOrganizationsCalls);
    }

    function setupRequests(adminConsole: AdminConsole, requests: string[]) {
        let DataDictionary = initDataDictionary;

        requests.forEach(function (value) {
            adminConsole.createRequest(DataDictionary[value]);

        })
    }

    function getPersonaList(personas: string[]) {
        let personaList = [];
        personas.forEach(function (personaName) {
            let name = personaName;
            personaList.push(PersonaDictionary[name])
        });
        return personaList;
    }

    function prepareAuthorityBatches(personas: string[]) {
        let personalist = getPersonaList(personas);
        let authorityBatches = {};

        personalist.forEach(function (persona) {
            persona.properties["authority"].forEach(function (authority) {
                (authorityBatches[authority.orgShortName] = authorityBatches[authority.orgShortName] || {});
                (authorityBatches[authority.orgShortName][authority.role] = authorityBatches[authority.orgShortName][authority.role] || []).push(persona.properties["login"])
            })
        });
        return authorityBatches;
    }

    function setupRoles(adminConsole: AdminConsole, personas: string[]) {
        let batches = prepareAuthorityBatches(personas);
        let assignRoleCalls = [];


        for (let orgShortName in batches) {
            for (let role in batches[orgShortName]) {
                let users = batches[orgShortName][role];
                assignRoleCalls.push(adminConsole.assignRole(orgShortName, role, users))
            }
        }
        return Promise.all(assignRoleCalls);
    }

    Before({tags: "@default"}, function (scenario) {
        let adminConsole = new AdminConsole();
        let userList = ["BBMRI_Admin", "Dave", "Linda"];
        let organizations = ["VarnameBank", 'SomeBank', 'XBank'];

        return adminConsole.cleanDB().then(function () {
            return Promise.all([
                setupUsers(adminConsole, userList),
                setupOrganizations(adminConsole, organizations)
            ]).then(function () {
                return setupRoles(adminConsole, userList)
            })
        });
    });
});
