/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import {Director, Persona} from "../protractor-stories/director";
import {AdminConsole} from "../protractor-stories/AdminConsole";
import {$} from "protractor";

export = function () {
    this.setDefaultTimeout(30 * 1000); //max time before callback

    this.Given(/^(.*) goes to the '(.*)' page$/, function (personaName, pageName, callback) {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);

        if (['sign in', 'registration'].indexOf(pageName) < 0) {
            login(director, persona).then(function () {
                director.goToPage(pageName).then(callback, callback)
            }, callback);
        } else {
            director.goToPage(pageName).then(callback, callback)
        }
    });

    this.When(/^(.*) attempts to login$/, function (personaName, callback) {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);

        Promise.all([
            director.enterText('usernameInput', persona.properties['userName']),
            director.enterText('passwordInput', persona.properties['password'])
        ]).then(function () {
            director.clickOn('submitButton').then(callback, callback)
        }, callback)
    });

    this.Then(/^(.*) is on the '(.*)' page$/, function (personaName, pageName, callback) {
        let director = this.director as Director;
        director.at(pageName).then(callback, callback);
    });

    this.When(/^(.*) edits the details:$/, function (personaName, fieldValueString, callback) {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);
        let fieldValuePairs: {[key: string]: string} = JSON.parse(fieldValueString.trim());
        this.scenarioData = fieldValuePairs;
        let promises = [];

        for (let key in fieldValuePairs) {
            promises.push(director.enterText(key, fieldValuePairs[key]))
        }

        Promise.all(promises).then(function () {
            director.clickOn('submitButton').then(callback, callback)
        }, callback)

    });

    this.Then(/^the new details are saved$/, function (callback) {
        let director = this.director as Director;
        Promise.resolve(director.getElement('SuccessMessage').locator.getText()).then(function (text) {
            if (text == 'Settings saved!') {
                callback()
            } else {
                callback('data was not saved successfully')
            }
        })
    });

    this.Then(/^the following fields are not editable:$/, function (fieldString, callback) {
        let director = this.director as Director;
        let fields = JSON.parse(fieldString.trim());

        let promises = [];

        for (let index in fields) {
            promises.push(director.getElement(fields[index]).locator.getTagName().then(function (tagname) {
                checkWithCallback(tagname, 'div', callback);
            }))
        }

        Promise.all(promises).then(function () { //ignore return value if all succeeded
            callback();
        }, callback);
    });


    this.When(/^(.*) registers for a new account$/, function (personaName, callback) {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);

        let inputValues: {[key: string]: string} = persona.properties;
        let fieldValueMapping: {[key: string]: string} = {
            "username": "userName",
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

        Promise.all(promises).then(function () {
            director.clickOn('submitButton').then(callback, callback)
        }, callback)

    });

    this.Then(/^an account is created$/, function (callback) {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        director.at("completed").then(function () {
            adminConsole.checkUser(director.getPersona("he"), checkNewUser, callback);
        })
    });

    this.When(/^(.*) attempts to login incorrectly '(\d+)' times$/, function (personaName, attempts, callback) {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);
        let adminConsole = this.adminConsole as AdminConsole;
        adminConsole.unlockUser(persona);

        Promise.all([
            director.enterText('usernameInput', persona.properties['userName']),
            director.enterText('passwordInput', 'wongPassword')
        ]).then(function () {
            for (let i = 0; i < attempts; i++){
                director.clickOn('submitButton').then(callback);
            }
        })
    });

    this.Then(/^(.*) is locked out$/, function (personaName, callback) {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        director.at('sign in').then(function () {
            adminConsole.checkUser(director.getPersona(personaName), checkLocked, callback);
        }, callback)
    });

    this.When(/^(.*) forgets to fill a field in the registration form$/, function (personaName, callback) {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);

        let inputValues: {[key: string]: string} = persona.properties;
        let fieldValueMapping: {[key: string]: string} = {
            "username": "userName",
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

        Promise.all(promises).then(function () {
            director.clickOn('submitButton').then(callback, callback)
        }, callback)
    });

    this.Then(/^(.*) is not registered$/, function (personaName, callback) {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        director.at('registration').then(function () {
            adminConsole.checkUser(director.getPersona(personaName), checkLocked, callback);
        }, callback)
    });

    //using a custom function instead of expect because expect cannot trigger the callback only for failures
    function checkWithCallback(result, expected, callback) {
        if (result != expected) {
            callback(result + " does not equal " + expected);
        }
    }

    //TODO: hack for login, should be replaced with rest call if possible
    function login(director: Director, persona: Persona) {
        director.goToPage('sign in');
        return Promise.all([
            director.enterText('usernameInput', persona.properties['userName']),
            director.enterText('passwordInput', persona.properties['password'])
        ]).then(function () {
            return director.clickOn('submitButton').then(function () {
                return director.waitForPage('Dashboard');
            });
        })
    }
};

function checkNewUser(expected, realData) {
    return expected.properties.userName == realData.login &&
        expected.properties.email == realData.email &&
        false == realData.emailVerified &&
        false == realData.adminVerified
}

function checkLocked(expected, realData) {
    return expected.properties.userName == realData.login &&
        expected.properties.email == realData.email &&
        true == realData.accountLocked
}
