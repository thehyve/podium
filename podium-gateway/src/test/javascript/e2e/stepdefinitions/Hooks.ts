/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import {AdminConsole} from "../protractor-stories/AdminConsole";
import PersonaDictionary = require("../personas/PersonaDictionary")
import initDataDictionary = require("../data/DataDictionary")

let Hooks = function () {

    function setupUsers(adminConsole: AdminConsole, personas: string[]) {
        adminConsole.cleanUsers();
        personas.forEach(function (value) {
            adminConsole.createUser(PersonaDictionary[value]);
        })
    }

    function setupOrganizations(adminConsole: AdminConsole, organizations: string[]) {
        adminConsole.cleanOrganizations();
        let DataDictionary = initDataDictionary();

        organizations.forEach(function (value) {
            adminConsole.createOrganization(DataDictionary[value]);
        })
    }

    function setupRequests(adminConsole: AdminConsole, requests: string[]) {
        adminConsole.cleanRequests();
        let DataDictionary = initDataDictionary();

        requests.forEach(function (value) {
            adminConsole.createRequest(DataDictionary[value]);

        })
    }

    this.Before({tags: ["@default"]}, function (scenario, callback) {
        let adminConsole = new AdminConsole();

        console.log(scenario);
        setupUsers(adminConsole, []);
        setupOrganizations(adminConsole, []);
        setupRequests(adminConsole, []);

    });
};

module.exports = Hooks;
