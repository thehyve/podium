/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { doInOrder } from './util';
import { Director } from '../protractor-stories/director';
import { browser, by, protractor } from 'protractor';
import { AdminConsole } from '../protractor-stories/admin-console';
import { isUndefined } from 'util';
import { Promise } from 'es6-promise';
import { Persona } from '../personas/templates';
import { Organisation } from '../data/templates';

let { defineSupportCode } = require('cucumber');


defineSupportCode(({ Given, When, Then }) => {
    let delay = process.env.BROWSERSTACK_DELAY ? process.env.BROWSERSTACK_DELAY : 0;


    When(/^(.*) adds user '(.*)' with role '(.*)'$/, function (personaName: string, targetNames: string, roleNames: string) {
        let director = this.director as Director;
        let personaNameList = targetNames.split(", ");
        let roleNameList = roleNames.split(", ");

        return director.clickOn("permissionsTab").then(() => {
            return doInOrder(personaNameList, (userNames) => {
                return browser.waitForAngular().then(() => {
                    return addRole(director, userNames, director.getData("menuRoleMapping")[roleNameList.pop()]);
                });
            });
        });
    });

    Then(/^'(.*)' has the role '(.*)' in the organisation '(.*)'$/, function (personaNames: string, roleNames: string, orgShortName: string): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;
        let personaNameList = personaNames.split(", ");
        let roleNameList = roleNames.split(", ");

        return adminConsole.getOrgUUID(orgShortName).then((uuid) => {
            return doInOrder(personaNameList, (userNames) => {
                return checkRole(director, adminConsole, userNames, director.getData("menuRoleMapping")[roleNameList.pop()], uuid);
            });
        });
    });

    When(/^he indicates that '(.*)' can be requested from '(.*)'$/, function (types, orgShortName): Promise<any> {
        let director = this.director as Director;
        let org = director.getData(orgShortName);
        org['requestTypes'] = types.split(", ");
        this.scenarioData = director.getData(orgShortName);

        let checkBoxes = [];
        org['requestTypes'].forEach((type) => {
            checkBoxes.push(director.clickOn(type))
        });

        return Promise.all(checkBoxes).then(() => {
            return director.clickOn("save")
        });
    });

    Then(/^the organisation's data has changed$/, function (): Promise<any> {
        let adminConsole = this.adminConsole as AdminConsole;

        return adminConsole.checkOrganisation(this.scenarioData, (expected: Organisation, realData) => {
            if (isUndefined(realData)) {
                return false
            }
            return realData.activated == expected["activated"] &&
                realData.name == expected["name"] &&
                realData.shortName == expected["shortName"] &&
                JSON.stringify(realData.requestTypes.sort()) == JSON.stringify(this.scenarioData.requestTypes.sort())
        })

    });
});

function checkRole(director: Director, adminConsole: AdminConsole, user: string, role: string, uuid: string): Promise<any> {
    return adminConsole.checkUser(director.getPersona(user), (expected: Persona, realData) => {
        return expected["login"] == realData.login &&
            expected["email"] == realData.email &&
            realData.organisationAuthorities[uuid + ""].includes(role);
    })
}

function addRole(director: Director, targetName: string, role: string): Promise<any> {
    return director.enterText("userSelection", director.getPersona(targetName)["firstName"], protractor.Key.ENTER).then(() => {
        return director.clickOnElement(
                director.getLocator("userSelection")
                .element(by.xpath('../..'))
                .$(`.user-authority-${role.toLowerCase()}`))
            .then(() => {
                return browser.sleep(500).then(() => {
                    return director.clickOn("add")
                })
            })
    })
}
