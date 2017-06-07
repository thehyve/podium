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
import { Promise } from 'es6-promise';
import { Persona } from '../personas/templates';
import initPersonaDictionary = require("../personas/persona-dictionary")
import initDataDictionary = require("../data/data-dictionary")
let { defineSupportCode } = require('cucumber');

defineSupportCode(function ({ After, Before }) {

    function setupUsers(adminConsole: AdminConsole, personas: string[]) {
        let personaDictionary = initPersonaDictionary();
        let createUserCalls = [];

        personas.forEach(function (value) {
            createUserCalls.push(adminConsole.createUser(personaDictionary[value]));
        });
        return Promise.all(createUserCalls);
    }

    function setupOrganisations(adminConsole: AdminConsole, organisations: string[]) {
        let DataDictionary = initDataDictionary();
        let personaDictionary = initPersonaDictionary();
        let createOrganisationsCalls = [];

        organisations.forEach(function (value) {
            createOrganisationsCalls.push(adminConsole.createOrganization(
                personaDictionary['BBMRI_Admin'],
                DataDictionary[value]));
        });
        return Promise.all(createOrganisationsCalls);
    }

    function setupRequests(adminConsole: AdminConsole, requests: string[]) {
        let DataDictionary = initDataDictionary();

        requests.forEach(function (value) {
            adminConsole.createRequest(DataDictionary[value]);

        })
    }

    function getPersonaList(personas: string[]) {
        let personaDictionary = initPersonaDictionary();
        let personaList = [];
        personas.forEach(function (personaName) {
            let name = personaName;
            personaList.push(personaDictionary[name])
        });
        return personaList;
    }

    function prepareAuthorityBatches(personas: string[]) {
        let personalist = getPersonaList(personas);
        let authorityBatches = {};

        personalist.forEach(function (persona: Persona) {
            persona["authority"].forEach(function (authority) {
                (authorityBatches[authority.orgShortName] = authorityBatches[authority.orgShortName] || {});
                (authorityBatches[authority.orgShortName][authority.role] = authorityBatches[authority.orgShortName][authority.role] || []).push(persona["login"])
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
        let adminConsole = this.adminConsole as AdminConsole;
        let userList = ["BBMRI_Admin", "Dave", "Linda", "VarnameBank_Admin", "blank user"];
        let organisations = ["VarnameBank", 'SomeBank', 'XBank'];

        return adminConsole.cleanDB().then(function () {
            return setupUsers(adminConsole, userList).then(function () {
                setupOrganisations(adminConsole, organisations).then(function () {
                    return setupRoles(adminConsole, userList)
                })
            })
        });
    });

    Before({ tags: "@request" }, function (scenario): Promise<any> {
        let adminConsole = this.adminConsole as AdminConsole;
        let organisations = ["DataBank", 'ImageBank', 'BioBank', 'MultiBank'];

        return setupOrganisations(adminConsole, organisations)
    });
});
