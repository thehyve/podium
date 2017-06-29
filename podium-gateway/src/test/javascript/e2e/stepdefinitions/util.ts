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

export function roleToRoute(persona: Persona, orgShortName: string): string {
    let role = persona['authority'].filter((value) => {
        return value["orgShortName"] == orgShortName
    })[0]['role'];

    if (role = 'ROLE_ORGANISATION_COORDINATOR') {
        role = 'coordinator'
    } else {
        role = 'requester'
    }

    return role;
}
