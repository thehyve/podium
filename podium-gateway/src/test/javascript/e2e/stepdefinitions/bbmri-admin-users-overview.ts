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
import { $$ } from 'protractor';
import { Director } from '../protractor-stories/director';
import { login, promiseTrue, doInOrder, checkTextElement } from './util';
let { defineSupportCode } = require('cucumber');

defineSupportCode(({ Given, When, Then }) => {

    Then(/^the overview contains the user's '(.*)' for the users '(.*)'$/, function (fieldString, userString): Promise<any> {
        let director = this.director as Director;

        let fields = fieldString.split(", ");
        let users = userString.split(", ");

        let personas: Persona[] = director.getListOfPersonas(users);

        return Promise.resolve($$('.test-' + fields[0]).count()).then((count) => {
            return promiseTrue(count == personas.length, "expected " + personas.length + " fields for " + fields[0] + " but found " + count);
        }).then(() => {
            return doInOrder(fields, (field) => {
                return $$('.test-' + field).each((element, index) => {
                    return checkField(element, field, personas[index]);
                });
            });
        });
    });

    function checkField(element, field, persona: Persona): Promise<any> {
        if (['emailVerified', 'adminVerified'].indexOf(field) > -1) {
            element = element.$('span');
        }

        switch (field) {
            case 'emailVerified': {
                let text = persona[field] ? "Email verified" : "Email unverified";

                return checkTextElement(element, text);
            }
            case 'adminVerified': {
                let text = persona[field] ? "Account verified" : "Account unverified";

                return checkTextElement(element, text);
            }
            default: {
                return checkTextElement(element, persona[field]);
            }
        }
    }


    When(/^(.*) sorts by '(.*)'$/, function (personaName, sortingType): Promise<any> {
        let director = this.director as Director;

        if (sortingType != 'Nothing') {
            return director.clickOn('Header' + sortingType)
        }
    });

    Then(/^users are displayed in the following order: '(.*)'$/, function (userString): Promise<any> {
        let director = this.director as Director;
        let users = userString.split(", ");
        let personas: Persona[] = director.getListOfPersonas(users);
        let fields = ["login"];

        return Promise.resolve($$('.test-' + fields[0]).count()).then((count) => {
            return promiseTrue(count == personas.length, "expected " + personas.length + " fields for " + fields[0] + " but found " + count);
        }).then(() => {
            return doInOrder(fields, (field) => {
                return $$('.test-' + field).each((element, index) => {
                    return checkField(element, field, personas[index]);
                });
            });
        });
    });

    Given(/^(.*) goes to the '(.*)' page for '(.*)'$/, function (personaName, pageName, targetUserName): Promise<any> {
        let director = this.director as Director;
        let suffix = director.getPersona(targetUserName)["login"];
        let persona: Persona = director.getPersona(personaName);

        return login(director, persona).then(() => {
            return director.goToPage(pageName, suffix);
        });
    });

    Then(/^the user details page contains '(.*)'s data$/, function (personaName) {
        let director = this.director as Director;
        let persona: Persona = director.getPersona(personaName);
        let page = director.getCurrentPage();

        let promisses = [
            checkTextElement(page.elements['login'].locator, persona['login']),
            checkTextElement(page.elements['firstName'].locator, persona['firstName']),
            checkTextElement(page.elements['lastName'].locator, persona['lastName']),
        ];

        return Promise.all(promisses);
    });
});
