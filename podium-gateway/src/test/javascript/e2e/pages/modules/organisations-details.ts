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
        "detailsTab": { locator: $('.nav-tabs').$$('.nav-link').get(0) },
        "permissionsTab": { locator: $('.nav-tabs').$$('.nav-link').get(1) },
        "userSelection": { locator: $('.user-select-input.ng-valid') },
        "authoritySelection": { locator: $('.field_authority_user') },
        "add": { locator: $('.test-add') },
        "name": { locator: $('#field_name') },
        "shortName": { locator: $('#field_shortName') },
        "Data": { locator: $('input[value=Data]') },
        "Images": { locator: $('input[value=Images]') },
        "Material": { locator: $('input[value=Material]') },
        "activeSwitch": { locator: $('ui-switch[name=organisation_active]') },
        "cancel": { locator: $('.btn-default') },
        "save": { locator: $('button[type=submit]') },
    };

    return elements;
}

export = initLocators;
