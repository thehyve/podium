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
import {login} from "./util";
import {isUndefined} from "util";

export = function () {
    this.setDefaultTimeout(30 * 1000); //max time before callback

    this.Then(/^the overview contains the user's '(.*)' for the users '(.*)'$/, function (fieldString, userString, callback) {
            let director = this.director as Director;

            let checksFinished = 0;
            let fields = JSON.parse(fieldString);
            let users = JSON.parse(userString);

            let personas = director.getListOfPersonas(users);

            fields.forEach(function (field) {
                $$('.test-' + field).count().then(function (count) {
                    if (count == personas.length) {

                        $$('.test-' + field).each(function (element, index) {
                            return checkField(element, index, field, personas, callback);
                        }).then(function () {
                            checksFinished++;
                            if (checksFinished == fields.length) {
                                callback()
                            }
                        }, callback)
                    } else {
                        callback("there are " + count + " elements for fields: " + '.' + field + " expected: " + personas.length)
                    }
                })
            });
        }
    );

    function checkField(element, index, field, personas, callback) {
        if (isUndefined(personas[index])) {
            callback("there is no persona for the user at (null based) index " + index);
        }

        if (['emailVerified', 'adminVerified'].indexOf(field) > -1) {
            element = element.$('span');
        }

        switch (field) {
            case 'authority':{
                let userAutorities = [];

                //TODO: untested, should work
                personas[index].properties[field].forEach(function (Autority) {
                    userAutorities.push(Autority["role"]);
                });

                return element.$$('div').each(function (element) {
                    return element.$('span').getText().then(function (text) {
                        if (userAutorities.indexOf(text) < 0) {
                            callback("'" + text + "' is not equal to [" + personas[index].properties[field] + "]");
                        }
                    })

                });
            }
            case 'emailVerified': {
                let text = personas[index].properties[field] ? "Email verified" : "Email unverified";

                return checkTextElement(element, text, callback);
            }
            case 'adminVerified': {
                let text = personas[index].properties[field] ? "Account verified" : "Account unverified";

                return checkTextElement(element, text, callback);
            }
            default: {
                return checkTextElement(element, personas[index].properties[field], callback);
            }
        }
    }


    this.When(/^(.*) sorts by '(.*)'$/, function (personaName, sortingType, callback) {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);

        if (sortingType != 'Nothing') {
            director.clickOn('Header' + sortingType).then(callback, callback);
        } else {
            callback()
        }
    });

    this.Then(/^users are displayed in the following order: '(.*)'$/, function (userString, callback) {
        let director = this.director as Director;
        let users = JSON.parse(userString);
        let personas = director.getListOfPersonas(users);
        let field = "login";

        $$('.test-' + field).count().then(function (count) {
            if (count == personas.length) {
                $$('.test-' + field).each(function (element, index) {

                    return checkField(element, index, field, personas, callback);
                }).then(callback, callback);
            } else {
                callback("there are " + count + " elements for fields: " + '.' + field + " expected: " + personas.length)
            }
        })
    });

    this.Given(/^(.*) goes to the '(.*)' page for '(.*)'$/, function (personaName, pageName, targetUserName, callback) {
        let director = this.director as Director;
        let sufix = director.getPersona(targetUserName).properties["userName"];
        let persona = director.getPersona(personaName);

        login(director, persona).then(function () {
            director.goToPage(pageName, sufix).then(callback, callback);
        }, callback);
    });

    this.Then(/^the user details page contains '(.*)'s data$/, function (personaName, callback) {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);
        let page = director.getCurrentPage();

        let promisses = [
            checkTextElement(page.elements['login'].locator, persona.properties['userName'], callback),
            checkTextElement(page.elements['firstName'].locator, persona.properties['firstName'], callback),
            checkTextElement(page.elements['lastName'].locator, persona.properties['lastName'], callback),
        ];

        Promise.all(promisses).then(function () {
            callback()
        }, callback)
    });
}

function checkTextElement(element, expectedText, callback) {

    return element.getText().then(function (text) {
        if (text != expectedText) {
            callback(text + " is not equal to " + expectedText);
        }
    }, callback)
}
