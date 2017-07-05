/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { Director, Persona } from '../protractor-stories/director';
import { Promise } from 'es6-promise';
import {
    UserGroupAuthority,
    OrganisationAuthorityOptions
} from '../../../../main/webapp/app/shared/authority/authority.constants';
import { ElementArrayFinder } from 'protractor';

export function login(director: Director, persona: Persona) {
    director.goToPage('sign in');
    return Promise.all([
        director.enterText('usernameInput', persona['login']),
        director.enterText('passwordInput', persona['password'])
    ]).then(function () {
        return director.clickOn('submitButton').then(function () {
            return director.waitForPage('Dashboard');
        });
    })
}

export function doInOrder<T>(parameterArray: Array<T>, method: (T) => Promise<any>): Promise<any> {
    if (parameterArray.length > 0) {
        return method(parameterArray.pop()).then(() => {
            return doInOrder(parameterArray, method);
        })
    }
}

export function promiseTrue(checkResult: boolean, message: string): Promise<any> {
    return new Promise(function (resolve, reject) {
        if (checkResult) {
            resolve();
        }
        else {
            reject("promiseTrue failure: " + message);
        }
    });
}

export function checkTextElement(element, expectedText): Promise<any> {
    return element.getText().then(function (text) {
        return promiseTrue(text == expectedText, text + " is not equal to " + expectedText);
    })
}

export function checkInputElement(element, expectedText): Promise<any> {
    return element.getAttribute('value').then(function (text) {
        return promiseTrue(text == expectedText, text + " is not equal to " + expectedText);
    })
}

export function roleToRoute(persona: Persona, orgShortName: string): string {
    let roles = persona['authority'].filter((value) => {
        return value["orgShortName"] == orgShortName
    });

    let role;
    if (roles.length > 0) {
        role = roles[0]['role'];
    }

    switch (role) {
        case OrganisationAuthorityOptions.ROLE_ORGANISATION_COORDINATOR: {
            return UserGroupAuthority.Coordinator.toString();
        }
        case OrganisationAuthorityOptions.ROLE_REVIEWER: {
            return UserGroupAuthority.Reviewer.toString();
        }
        default: {
            return UserGroupAuthority.Requester.toString();
        }
    }
}

export function copyData(data) {
    return JSON.parse(JSON.stringify(data));
}

export function countIs(elementArray: ElementArrayFinder, expectedCount: number): Promise<any> {
    return elementArray.count().then((count) => {
        return promiseTrue(count == expectedCount, "expected: " + expectedCount + " elements but found: " + count);
    })
}
