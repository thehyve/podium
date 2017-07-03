/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { browser, $ } from 'protractor';
import { Interactable } from '../protractor-stories/director';
import ProfileForm = require("./modules/profile-form")


/*
 * Must follow the Page interface
 * pages hold all stateless information on a page.
 */
class UserDetails {
    public name: string;
    public url: string;
    public elements: { [name: string]: Interactable };

    public at() {
        let that = this;
        return browser.getCurrentUrl().then(function (currentUrl) {
            return (browser.baseUrl + that.url) == currentUrl;
        })
    }

    constructor() {
        this.name = "organisation edit";
        this.url = "#/organisation/configuration/edit/";
        this.elements = {
            "details tab": { locator: $('.nav-tabs').$$('.nav-link').get(0) },
            "permissions tab": { locator: $('.nav-tabs').$$('.nav-link').get(1) },
            "user selection": { locator: $('.user-select-input.ng-valid') },
            "authority selection": { locator: $('.field_authority_user') },
            "add": { locator: $('.test-add') },
            "name input": { locator: $('#field_name') },
            "short name input": { locator: $('#field_shortName') },
            "Data": { locator: $('input[value=Data]') },
            "Images": { locator: $('input[value=Images]') },
            "Material": { locator: $('input[value=Material]') },
            "active switch": { locator: $('ui-switch[name=organisation_active]') },
            "cancel": { locator: $('.btn-default') },
            "save": { locator: $('button[type=submit]') },
        }
    }
}

export = UserDetails;
