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
import {login} from "./Util";

export = function () {
    this.setDefaultTimeout(30 * 1000); //max time before callback

    this.Then(/^the overview contains the user's '(.*)' for the users '(.*)'$/, function (fieldString, userString, callback) {
            let director = this.director as Director;

            let checksFinished = 0;
            let fields = JSON.parse(fieldString);
            let users = JSON.parse(userString);

            let personas = director.getListOfPersonas(users);

            fields.forEach(function (field) {
                $$('.' + field).each(function (element, index) {
                    return checkField(element, index, field, personas, callback);
                }).then(function () {
                    checksFinished++;
                    if (checksFinished == fields.length) {
                        callback()
                    }
                }, callback)
            })
        }
    );

    function checkField(element, index, field, personas, callback) {
        if ('login' == field) { //TODO: unify the use of login and userName
            field = 'userName'
        }

        if (['emailVerified', 'adminVerified'].indexOf(field) > -1) {
            element = element.$('span');
        }

        if ('authority' == field) {
            return element.$$('div').each(function (element) {
                return element.$('span').getText().then(function (text) {
                    if (personas[index].properties[field].indexOf(text) < 0) {
                        callback("'" + text + "' is not equal to [" + personas[index].properties[field] + "]");
                    }
                })
            })
        } else {
            return checkTextElement(element, personas[index].properties[field], callback);
        }
    }

    function checkTextElement(element, expectedText, callback) {

        return element.getText().then(function (text) {
            if (text != expectedText) {
                callback(text + " is not equal to " + expectedText);
            }
        })
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


        $$('.' + field).each(function (element, index) {
            return checkField(element, index, field, personas, callback);
        }).then(callback, callback)

    });

    this.Given(/^(.*) goes to the '(.*)' page for '(.*)'$/, function (personaName, pageName, targetUserName, callback) {
        let director = this.director as Director;
        let sufix = director.getPersona(targetUserName).properties["userName"];
        let persona = director.getPersona(personaName);

        login(director, persona).then(function () {
            director.goToPage(pageName, sufix).then(callback, callback);
        }, callback);
    });

}
