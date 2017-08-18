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
import { $, $$, ElementArrayFinder } from 'protractor';
import { Director } from '../protractor-stories/director';
import { checkInputElement, checkTextElement, countIs, doInOrder, login, promiseTrue } from './util';
import { ORGANISATION_AUTHORITIES_MAP } from '../../../../main/webapp/app/shared/authority/authority.constants';
import { AdminConsole } from '../protractor-stories/admin-console';

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

    Given(/^(.*) goes to the user details page for '(.*)'$/, function (personaName, targetUserName): Promise<any> {
        let director = this.director as Director;
        let suffix = director.getPersona(targetUserName)["login"] + '/edit)';
        let persona: Persona = director.getPersona(personaName);
        this.scenarioData = director.getPersona(targetUserName);

        return login(director, persona).then(() => {
            return director.goToPage('user details', suffix);
        });
    });

    Then(/^the user details page contains '(.*)'s data$/, function (personaName) {
        let director = this.director as Director;
        let persona: Persona = director.getPersona(personaName);
        let page = director.getCurrentPage();

        let textInputs = ["login", "firstName", "lastName", "email", "telephone", "institute", "department", "jobTitle", "specialism"];

        let promisses = textInputs.map((fieldname) => {
            return checkInputElement(director.getLocator(fieldname), persona[fieldname]);
        });


        let authorities = persona['authority'].map((authority) => {
            return authority['role'];
        });

        promisses = [countIs(<ElementArrayFinder>director.getLocator('authority'), authorities.length).then(() => {
            return director.getLocator('authority').each((element, index) => {
                return checkTextElement(element, ORGANISATION_AUTHORITIES_MAP[authorities[index]].name)
            })
        })];

        return Promise.all(promisses);
    });

    When(/^(.*) deletes '(.*)'s account$/, function (personaName, targetUserName) {
        let director = this.director as Director;
        this.scenarioData = director.getPersona(targetUserName);

        return $('.test-row-' + this.scenarioData['login']).$('.test-delete-btn').click().then(() => {
            return $('.test-delete-btn-modal').click();
        })
    });

    Then(/^(.*) account is removed$/, function (personaName) {
        let adminConsole = this.adminConsole as AdminConsole;

        return adminConsole.getUsers().then((users: {}[]) => {
            let filterd = users.filter((user) => {
                return user['login'] == this.scenarioData['login']
            })
            return promiseTrue(filterd.length == 0, 'there was a user found with login: ' + this.scenarioData['login'])
        })
    });
});
