/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { Persona } from '../personas/templates';
import { Promise } from 'es6-promise';
import { Director } from '../protractor-stories/director';
import { AdminConsole } from '../protractor-stories/admin-console';
import { $ } from 'protractor';
import { copyData, doInOrder, login, promiseTrue } from './util';

let { defineSupportCode } = require('cucumber');

defineSupportCode(function ({ setDefaultTimeout }) {
    setDefaultTimeout(30 * 1000);
});

defineSupportCode(({ Given, When, Then }) => {

    Given(/^(.*) goes to the '(.*)' page$/, function (personaName, pageName): Promise<any> {
        let director = this.director as Director;
        let persona: Persona = director.getPersona(personaName);

        if (['sign in', 'registration'].indexOf(pageName) < 0) {
            return login(director, persona).then(() => {
                return director.goToPage(pageName)
            });
        } else {
            return director.goToPage(pageName)
        }
    });

    When(/^(.*) attempts to login$/, function (personaName): Promise<any> {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);

        return Promise.all([
            director.enterText('usernameInput', persona['login']),
            director.enterText('passwordInput', persona['password'])
        ]).then(() => {
            return director.clickOn('submitButton')
        })
    });

    Then(/^(.*) is on the '(.*)' page$/, function (personaName, pageName): Promise<any> {
        let director = this.director as Director;
        return director.at(pageName);
    });

    When(/^(.*) edits the details '(.*)'$/, function (personaName, fieldString): Promise<any> {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);
        this.scenarioData = copyData(persona) //copy for future steps
        let fields: string[] = fieldString.split(', ');

        let promises = fields.map((field) => {
            this.scenarioData[field] = this.scenarioData[field] + 'edited';
            return director.enterText(field, this.scenarioData[field])
        });

        return Promise.all(promises).then(() => {
            return director.clickOn('submitButton')
        })
    });

    Then(/^the new details are saved$/, function (): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let fieldsToCheck = ["login", "firstName", "lastName", "email", "telephone", "institute", "department",
            "jobTitle", "specialism", "emailVerified", "adminVerified", "accountLocked"]


        return adminConsole.getUser(this.scenarioData).then((user) => {
            return Promise.all(fieldsToCheck.map((fieldname) => {
                return promiseTrue(user[fieldname] == this.scenarioData[fieldname], fieldname + ' did not match user:\n' + JSON.stringify(user) + '\n Persona: \n' + JSON.stringify(this.scenarioData));
            }))
        });
    });

    Then(/^the fields '(.*)' are not editable$/, function (fieldString): Promise<any> {
        let director = this.director as Director;
        let fields = fieldString.split(', ');

        return doInOrder(fields, (field) => {
            return director.getElement(field).locator.getTagName().then((tagname) => {
                return promiseTrue(tagname == 'div', field + " is editable");
            })
        })
    });


    When(/^(.*) registers for a new account$/, function (personaName): Promise<any> {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);

        let inputValues: { [key: string]: any } = persona;
        let fieldValueMapping: { [key: string]: string } = {
            "username": "login",
            "firstName": "firstName",
            "lastName": "lastName",
            "email": "email",
            "telephone": "telephone",
            "institute": "institute",
            "department": "department",
            "jobTitle": "jobTitle",
            "specialism": "specialism",
            "Other specialism": "Other specialism",
            "password": "password",
            "confirmPassword": "password",
        };

        let promises = [];

        for (let key in fieldValueMapping) {
            if (key == 'specialism') {
                $('option[value=' + inputValues[fieldValueMapping[key]] + ']').click();
                if (inputValues[fieldValueMapping[key]] == 'Other') {
                    promises.push(director.enterText("Other specialism", inputValues[fieldValueMapping["Other specialism"]]))
                }
            } else {
                promises.push(director.enterText(key, inputValues[fieldValueMapping[key]]))
            }

        }

        return Promise.all(promises).then(function () {
            return director.clickOn('submitButton')
        })
    });

    Then(/^an account is created$/, function (): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        return adminConsole.checkUser(director.getPersona("he"), checkNewUser);
    });

    When(/^(.*) attempts to login incorrectly '(\d+)' times$/, function (personaName, attempts): Promise<any> {
        let director = this.director as Director;
        let persona: Persona = director.getPersona(personaName);

        return Promise.all([
            director.enterText('usernameInput', persona['login']),
            director.enterText('passwordInput', 'wongPassword')
        ]).then(function () {
            return doInOrder(Array(attempts), (item) => {
                return director.clickOn('submitButton')
            })
        })
    });

    Then(/^(.*) is locked out$/, function (personaName): Promise<any> {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        return director.at('sign in').then(function () {
            return adminConsole.checkUser(director.getPersona(personaName), checkLocked);
        })
    });

    When(/^(.*) forgets to fill a field in the registration form$/, function (personaName): Promise<any> {
        let director = this.director as Director;
        let persona: Persona = director.getPersona(personaName);

        let inputValues: { [key: string]: any } = persona;
        let fieldValueMapping: { [key: string]: string } = {
            "username": "login",
            "firstName": "firstName",
            "lastName": "lastName",
            "email": "email",
            "telephone": "telephone",
            "institute": "institute",
            "department": "department",
            "jobTitle": "jobTitle",
            "specialism": "specialism",
            "Other specialism": "Other specialism",
            "password": "password",
            "confirmPassword": "password",
        };

        delete fieldValueMapping['jobTitle'];

        let promises = [];

        for (let key in fieldValueMapping) {
            if (key == 'specialism') {
                $('option[value=' + inputValues[fieldValueMapping[key]] + ']').click();
                if (inputValues[fieldValueMapping[key]] == 'Other') {
                    promises.push(director.enterText("Other specialism", inputValues[fieldValueMapping["Other specialism"]]))
                }
            } else {
                promises.push(director.enterText(key, inputValues[fieldValueMapping[key]]))
            }

        }

        return Promise.all(promises);
    });

    Then(/^(.*) is not registered$/, function (personaName): Promise<any> {
        let director = this.director as Director;

        return director.getElement('submitButton').locator.isEnabled().then((enabled) => {
            return promiseTrue(!enabled, 'submitButton was enabled');
        });
    });
});

function checkNonExistend(expected, realData) {
    return realData.message == "error.404";
}

function checkNewUser(expected: Persona, realData) {
    return expected["login"] == realData.login &&
        expected["email"] == realData.email &&
        false == realData.emailVerified &&
        false == realData.adminVerified
}

function checkLocked(expected: Persona, realData) {
    return expected["login"] == realData.login &&
        expected["email"] == realData.email &&
        true == realData.accountLocked
}
