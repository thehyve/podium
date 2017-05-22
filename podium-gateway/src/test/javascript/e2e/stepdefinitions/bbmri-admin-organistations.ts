/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
let {defineSupportCode} = require('cucumber');
import { Promise } from "es6-promise";
import { $$ } from "protractor";
import { Director } from "../protractor-stories/director";
import { AdminConsole } from "../protractor-stories/admin-console";
import { login, doInOrder, promiseTrue, checkTextElement } from "./util";
import { isUndefined } from "util";


defineSupportCode(({Given, When, Then}) => {

    Then(/^the overview contains the organization's '(.*)' for the organizations '(.*)'$/, function (fieldString, organizationString): Promise<any> {
        let director = this.director as Director;
        let fields = JSON.parse(fieldString);
        let organizations = JSON.parse(organizationString);

        let organizationsList = director.getListOfData(organizations);

        return Promise.resolve($$('.test-' + fields[0]).count()).then((count) => {
            return promiseTrue(count == organizationsList.length, "expected " + organizationsList.length + " organisations but found " + count);
        }).then(() => {
            return doInOrder(fields, (field) => {
                return $$('.test-' + field).each((element, index) => {
                    return element.getText().then((text) => {
                        return Promise.resolve(promiseTrue(text == organizationsList[index].properties[field], field + ": " + text + " did not equal " + organizationsList[index].properties[field]));
                    })
                });
            });
        });
    });

    Then(/^organizations are displayed in the following order: '(.*)'$/, function (organizationString): Promise<any> {
        let director = this.director as Director;
        let organizations = JSON.parse(organizationString);
        let organizationsList = director.getListOfData(organizations);

        let fields = ["shortName"];

        return Promise.resolve($$('.test-' + fields[0]).count()).then((count) => {
            return promiseTrue(count == organizationsList.length, "expected " + organizationsList.length + " organisations but found " + count);
        }).then(() => {
            return doInOrder(fields, (field) => {
                return $$('.test-' + field).each((element, index) => {
                    return element.getText().then((text) => {
                        return Promise.resolve(promiseTrue(text == organizationsList[index].properties[field], field + ": " + text + " did not equal " + organizationsList[index].properties[field]));
                    })
                });
            });
        });
    });

    Given(/^(.*) goes to the organization details page for '(.*)'$/, function (personaName, organizationName): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let persona = director.getPersona(personaName);


        return login(director, persona).then(() => {
            return adminConsole.getOrgUUID(organizationName).then((suffix) => {
                return director.goToPage('organization details', suffix as string);
            })
        });
    });

    Then(/^the organization details page contains '(.*)'s data$/, function (organizationShortName): Promise<any> {
        let director = this.director as Director;
        let organization = director.getData(organizationShortName);
        let page = director.getCurrentPage();

        let promisses = [
            checkTextElement(page.elements['shortName'].locator, organization.properties['shortName']),
            checkTextElement(page.elements['name'].locator, organization.properties['name'])
        ];

        return Promise.all(promisses)
    });

    When(/^(.*) creates the organization '(.*)'$/, function (personaName, organizationShortName): Promise<any> {
        let director = this.director as Director;
        let organization = director.getData(organizationShortName);
        this.scenarioData = organization; //store it for the next step

        return Promise.all([
            director.enterText('shortName', organization.properties['shortName']),
            director.enterText('name', organization.properties['name'])
        ]).then(function () {
            return director.clickOn('submitButton');
        })
    });

    Then(/^'(.*)' organization exists$/, function (organizationShortName): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let organization = director.getData(organizationShortName);

        return adminConsole.checkOrganization(organization, checkOrg);
    });
});

function checkOrg(expected, realData) {
    if (isUndefined(realData)) {
        return false
    }
    return realData.activated == expected.properties.activated &&
        realData.name == expected.properties.name &&
        realData.shortName == expected.properties.shortName

}


