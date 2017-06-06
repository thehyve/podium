/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { AdminConsole } from '../protractor-stories/admin-console';
import { browser } from 'protractor';
import { Promise } from 'es6-promise';
import PersonaDictionary = require("../personas/persona-dictionary")
import initDataDictionary = require("../data/data-dictionary")
let { defineSupportCode } = require('cucumber');

defineSupportCode(function ({ After, Before }) {

    function setupUsers(adminConsole: AdminConsole, personas: string[]) {
        let createUserCalls = [];

        personas.forEach(function (value) {
            createUserCalls.push(adminConsole.createUser(PersonaDictionary[value]));
        });
        return Promise.all(createUserCalls);
    }

    function setuporganisations(adminConsole: AdminConsole, organisations: string[]) {
        let DataDictionary = initDataDictionary;
        let createorganisationsCalls = [];

        organisations.forEach(function (value) {
            createorganisationsCalls.push(adminConsole.createorganisation(DataDictionary[value]));
        });
        return Promise.all(createorganisationsCalls);
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

    Before({ tags: "@default" }, function (scenario): Promise<any> {
        let adminConsole = new AdminConsole();
        let userList = ["BBMRI_Admin", "Dave", "Linda", "VarnameBank_Admin", "blank user"];
        let organisations = ["VarnameBank", 'SomeBank', 'XBank'];

        return adminConsole.cleanDB().then(function () {
            return Promise.all([
                setupUsers(adminConsole, userList),
                setuporganisations(adminConsole, organisations)
            ]).then(function () {
                return setupRoles(adminConsole, userList)
            })
        });
    });

    Before(function (scenario): Promise<any> {
        return browser.sleep(2000);
    });
});
