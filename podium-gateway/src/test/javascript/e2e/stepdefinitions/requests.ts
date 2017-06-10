/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { Director } from '../protractor-stories/director';
import { AdminConsole } from '../protractor-stories/admin-console';
import { isUndefined } from 'util';
import { Promise } from 'es6-promise';
import { doInOrder, promiseTrue } from './util';
import { Organisation, Request } from '../data/templates';
let { defineSupportCode } = require('cucumber');


defineSupportCode(({ Given, When, Then }) => {

    When(/^(.*) creates a new draft filling data for '(.*)'$/, function (personaName, requestName): Promise<any> {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);
        let request: Request = director.getData(requestName);
        this.scenarioData = request; //store for next step
        let page = director.getCurrentPage();

        return director.clickOn("new draft").then(() => {
            return doInOrder(["title", "background", "research question", "hypothesis", "methods", "related request number", "piName",
                "piEmail", "piFunction", "piAffiliation", "searchQuery"], (key) => {
                return director.enterText(key, request[key]);
            }).then(() => {
                return doInOrder(request["requestTypes"], (type) => {
                    return director.clickOn(type);
                }).then(() => {
                    return director.clickOn("save")
                })
            })
        });

    });

    When(/^(.*) selects request types '(.*)'$/, function (personaName, requestTypes) {
        let director = this.director as Director;
        let types = requestTypes.split(", ");

        return doInOrder(types, (type) => {
            return director.clickOn(type);
        })
    });

    Then(/^the organisations '(.*)' can be selected$/, function (organisationNames) {
        let director = this.director as Director;
        let orgList = organisationNames.split(", ");
        let orgNames: string[] = [];

        director.getListOfData(orgList).forEach((org: Organisation) => {
            orgNames.push(org["name"]);
        });

        return Promise.resolve(director.getElement("organisations").locator.$$('option').count()).then((count) => {
            return promiseTrue(count == orgNames.length, "expected " + orgNames.length + " organisations but found " + count);
        }).then(() => {
            return director.getElement("organisations").locator.$$('option').each((element) => {
                return element.getText().then((text) => {
                    return promiseTrue(orgNames.some((item) => {
                        return text.trim() == item;
                    }), "\"" + text + "\"" + "Should not be selectable only: " + orgNames)
                })
            })
        })
    });

    Then(/^the draft is saved$/, function (callback) {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let persona = director.getPersona('he');

        adminConsole.checkDraft(this.scenarioData, checkDraft, persona).then(callback, callback);

    });
});


function checkDraft(expected: Request, realData) {
    if (isUndefined(realData)) {
        return false
    }
    return true
}
