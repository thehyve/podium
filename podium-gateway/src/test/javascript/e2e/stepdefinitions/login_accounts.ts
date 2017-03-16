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
import {browser} from "protractor";

export = function () {
    this.setDefaultTimeout(60 * 1000); //max time before callback

    this.Given(/^(.*) goes to the '(.*)' page$/, function (personaName, pageName, callback) {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);

        if (pageName != 'sign in') {
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
            if (text == 'Settings saved!'){
                callback()
            } else {
                callback('data was not saved successfully')
            }
        })
    });

    this.Then(/^the following fields are not editable:$/, function (fieldString, callback) {

    });

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
