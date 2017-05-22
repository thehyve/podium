/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { $ } from 'protractor';
import { Interactable } from '../../protractor-stories/director';


function initLocators() {
    let elements: { [name: string]: Interactable };

    elements = {
        "firstName": { locator: $('input[name=firstName]') },
        "lastName": { locator: $('input[name=lastName]') },
        "email": { locator: $('input[name=email]') },
        "telephone": { locator: $('input[name=telephone]') },
        "department": { locator: $('input[name=department]') },
        "jobTitle": { locator: $('input[name=jobTitle]') },
        "specialism": { locator: $('select[name=specialism]') },
        "Other specialism": { locator: $('input[placeholder="Other specialism"]') },
        "submitButton": { locator: $('button[type=submit]') },
    }

    return elements;
}

export = initLocators;
