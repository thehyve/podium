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
import { browser } from 'protractor';
import initPersonaDictionary = require("../personas/persona-dictionary");
import initDataDictionary = require("../data/data-dictionary");

let { defineSupportCode } = require('cucumber');

defineSupportCode(function ({ After, Before }) {
    let personaDictionary = initPersonaDictionary();
    let dataDictionary = initDataDictionary();

    function setupUsers(adminConsole: AdminConsole, personas: string[]) {
        let createUserCalls = [];

        personas.forEach(function (value) {
            createUserCalls.push(adminConsole.createUser(personaDictionary[value]));
        });
        return Promise.all(createUserCalls);
    }

    function setupOrganisations(adminConsole: AdminConsole, organisations: string[]) {
        let createOrganisationsCalls = [];

        organisations.forEach(function (value) {
            createOrganisationsCalls.push(adminConsole.createOrganisation(
                personaDictionary['BBMRI_Admin'],
                dataDictionary[value]));
        });
        return Promise.all(createOrganisationsCalls);
    }

    function setupRequests(adminConsole: AdminConsole, requests: string[]) {
        let createRequestCalls = [];

        requests.forEach(function (value) {
            createRequestCalls.push(adminConsole.createRequest(personaDictionary['Linda'], dataDictionary[value]));
        });
        return Promise.all(createRequestCalls);
    }

    function setupDrafts(adminConsole: AdminConsole, drafts: string[]) {
        let createDraftCalls = [];

        drafts.forEach(function (value) {
            createDraftCalls.push(adminConsole.createDraft(personaDictionary['Linda'], dataDictionary[value]));
        });
        return Promise.all(createDraftCalls);
    }

    function getPersonaList(personas: string[]) {
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

    Before(function (scenario): Promise<any> {
        return browser.get('/').then((): Promise<any> => {
            return Promise.all([
                browser.executeScript('localStorage.clear();'),
                browser.executeScript('sessionStorage.clear();')
            ])
        })
    });

    Before({ tags: "@default" }, function (scenario): Promise<any> {
        let adminConsole = this.adminConsole as AdminConsole;
        let userList = ['BBMRI_Admin', 'Dave', 'Linda', 'VarnameBank_Admin', 'blank user'];
        let organisations = ['VarnameBank', 'SomeBank', 'XBank'];

        return adminConsole.cleanDB().then(function () {
            return setupUsers(adminConsole, userList).then(function () {
                return setupOrganisations(adminConsole, organisations).then(function () {
                    return setupRoles(adminConsole, userList)
                })
            })
        });
    });

    Before({ tags: "@request" }, function (scenario): Promise<any> {
        let adminConsole = this.adminConsole as AdminConsole;
        let userList = ['Request_Coordinator', 'Request_Reviewer', 'Databank_Coordinator'];
        let organisations = ['DataBank', 'ImageBank', 'BioBank', 'MultiBank'];
        let requests = ['Request01', 'Request02', 'Request03'];
        let drafts = ['Draft01', 'Draft02'];

        return setupUsers(adminConsole, userList).then(function () {
            return setupOrganisations(adminConsole, organisations).then(function () {
                return setupRoles(adminConsole, userList).then(function () {
                    return Promise.all([
                        setupRequests(adminConsole, requests),
                        setupDrafts(adminConsole, drafts)
                    ])
                })
            })
        })
    });
});
