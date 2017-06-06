/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
let { defineSupportCode } = require('cucumber');
import { Promise } from 'es6-promise';
import { $$ } from 'protractor';
import { Director } from '../protractor-stories/director';
import { AdminConsole } from '../protractor-stories/admin-console';
import { login, doInOrder, promiseTrue, checkTextElement } from './util';
import { isUndefined } from 'util';


defineSupportCode(({ Given, When, Then }) => {

    Then(/^the overview contains the organisation's '(.*)' for the organisations '(.*)'$/, function (fieldString, organisationString): Promise<any> {
        let director = this.director as Director;
        let fields = fieldString.split(", ");
        let organisations = organisationString.split(", ");

        let organisationsList = director.getListOfData(organisations);

        return Promise.resolve($$('.test-' + fields[0]).count()).then((count) => {
            return promiseTrue(count == organisationsList.length, "expected " + organisationsList.length + " organisations but found " + count);
        }).then(() => {
            return doInOrder(fields, (field) => {
                return $$('.test-' + field).each((element, index) => {
                    return element.getText().then((text) => {
                        return Promise.resolve(promiseTrue(text == organisationsList[index].properties[field], field + ": " + text + " did not equal " + organisationsList[index].properties[field]));
                    })
                });
            });
        });
    });

    Then(/^organisations are displayed in the following order: '(.*)'$/, function (organisationString): Promise<any> {
        let director = this.director as Director;
        let organisations = organisationString.split(", ");
        let organisationsList = director.getListOfData(organisations);

        let fields = ["shortName"];

        return Promise.resolve($$('.test-' + fields[0]).count()).then((count) => {
            return promiseTrue(count == organisationsList.length, "expected " + organisationsList.length + " organisations but found " + count);
        }).then(() => {
            return doInOrder(fields, (field) => {
                return $$('.test-' + field).each((element, index) => {
                    return element.getText().then((text) => {
                        return Promise.resolve(promiseTrue(text == organisationsList[index].properties[field], field + ": " + text + " did not equal " + organisationsList[index].properties[field]));
                    })
                });
            });
        });
    });

    Given(/^(.*) goes to the '(.*)' page for the organisation '(.*)'$/, function (personaName, pageName, orgShortName): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let persona = director.getPersona(personaName);

        return login(director, persona).then(function () {
            return adminConsole.getOrgUUID(orgShortName).then(function (sufix) {
                return director.goToPage(pageName, sufix as string);
            })
        });
    });

    Then(/^the organisation details page contains '(.*)'s data$/, function (organisationShortName): Promise<any> {
        let director = this.director as Director;
        let organisation = director.getData(organisationShortName);
        let page = director.getCurrentPage();

        let promisses = [
            checkTextElement(page.elements['shortName'].locator, organisation.properties['shortName']),
            checkTextElement(page.elements['name'].locator, organisation.properties['name'])
        ];

        return Promise.all(promisses)
    });

    When(/^(.*) creates the organisation '(.*)'$/, function (personaName, organisationShortName): Promise<any> {
        let director = this.director as Director;
        let organisation = director.getData(organisationShortName);
        this.scenarioData = organisation; //store it for the next step

        return Promise.all([
            director.enterText('shortName', organisation.properties['shortName']),
            director.enterText('name', organisation.properties['name'])
        ]).then(function () {
            return director.clickOn('submitButton');
        })
    });

    Then(/^'(.*)' organisation exists$/, function (organisationShortName): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let organisation = director.getData(organisationShortName);

        return adminConsole.checkorganisation(organisation, checkOrg);
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


