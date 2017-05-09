/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import {$$} from "protractor";
import {Director} from "../protractor-stories/director";
import {AdminConsole} from "../protractor-stories/admin-console";
import {login} from "./util";
import {isUndefined} from "util";


export = function () {
    this.setDefaultTimeout(30 * 1000); //max time before callback

    this.Then(/^the overview contains the organization's '(.*)' for the organizations '(.*)'$/, function (fieldString, organizationString, callback) {
            let director = this.director as Director;

            let checksFinished = 0;
            let fields = JSON.parse(fieldString);
            let organizations = JSON.parse(organizationString);

            let organizationsList = director.getListOfData(organizations);

            fields.forEach(function (field) {
                $$('.test-' + field).each(function (element, index) {
                    return checkField(element, index, field, organizationsList, callback);
                }).then(function () {
                    checksFinished++;
                    if (checksFinished == fields.length) {
                        callback()
                    }
                }, callback)
            })
        }
    );

    this.Then(/^organizations are displayed in the following order: '(.*)'$/, function (organizationString, callback) {
        let director = this.director as Director;
        let organizations = JSON.parse(organizationString);
        let organizationsList = director.getListOfData(organizations);

        let field = "shortName";

        $$('.test-' + field).count().then(function (count) {
            if (count == organizations.length){
                $$('.test-' + field).each(function (element, index) {
                    return checkField(element, index, field, organizationsList, callback);
                }).then(callback, callback)
            } else {
                callback("there are "+count+" elements for field: " + '.' + field + " expected: " + organizations.length)
            }
        });


    });

    this.Given(/^(.*) goes to the organization details page for '(.*)'$/, function (personaName, organizationName, callback) {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let persona = director.getPersona(personaName);



        login(director, persona).then(function () {
            adminConsole.getOrgUUID(organizationName).then(function (sufix) {
                director.goToPage('organization details', sufix as string).then(callback, callback);
            })
        }, callback);
    });

    this.Then(/^the organization details page contains '(.*)'s data$/, function (organizationShortName, callback) {
        let director = this.director as Director;
        let organization = director.getData(organizationShortName);
        let page = director.getCurrentPage();

        let promisses = [
            checkTextElement(page.elements['shortName'].locator, organization.properties['shortName'], callback),
            checkTextElement(page.elements['name'].locator, organization.properties['name'], callback)
        ];

        Promise.all(promisses).then(function () {
            callback()
        }, callback)
    });

    this.When(/^(.*) creates the organization '(.*)'$/, function (personaName, organizationShortName, callback) {
        let director = this.director as Director;
        let organization = director.getData(organizationShortName);
        this.scenarioData = organization; //store it for the next step

        Promise.all([
            director.enterText('shortName', organization.properties['shortName']),
            director.enterText('name', organization.properties['name'])
        ]).then(function () {
            return director.clickOn('submitButton').then(callback, callback);
        })
    });

    this.Then(/^'(.*)' organization exists$/, function (organizationShortName, callback) {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let organization = director.getData(organizationShortName);

        adminConsole.checkOrganization(organization, checkOrg).then(callback,callback);
    });
}

function checkOrg(expected, realData) {
    if (isUndefined(realData)) {
        return false
    }
    return realData.activated == expected.properties.activated &&
        realData.name == expected.properties.name &&
        realData.shortName == expected.properties.shortName

}

function checkField(element, index, field, organizations, callback) {
    return checkTextElement(element, organizations[index].properties[field], callback);
}

function checkTextElement(element, expectedText, callback) {

    return element.getText().then(function (text) {
        if (text != expectedText) {
            callback(text + " is not equal to " + expectedText);
        }
    })
}
