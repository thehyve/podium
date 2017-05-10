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

export function login(director: Director, persona: Persona) {
    director.goToPage('sign in');
    return Promise.all([
        director.enterText('usernameInput', persona.properties['login']),
        director.enterText('passwordInput', persona.properties['password'])
    ]).then(function () {
        return director.clickOn('submitButton').then(function () {
            return director.waitForPage('Dashboard');
        });
    })
}


