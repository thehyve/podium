/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { browser, $, $$ } from 'protractor';
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
        this.name = "User Details";
        this.url = "#/bbmri/user-management/(popup:detail/";
        this.elements = {
            "login": { locator: $('.test-login-modal') },
            "firstName": { locator: $('.test-firstName-modal') },
            "lastName": { locator: $('.test-lastName-modal') },
            "email": { locator: $('.test-email-modal') },
            "telephone": { locator: $('.test-telephone-modal') },
            "institute": { locator: $('.test-institute-modal') },
            "department": { locator: $('.test-department-modal') },
            "jobTitle": { locator: $('.test-jobTitle-modal') },
            "specialism": { locator: $('.test-specialism-modal') },
            "activated": { locator: $('.test-activated') },
            "authority": { locator: $$('.test-authority-modal') },
            "adminVerified": { locator: $('#adminVerified-modal') },
            "submitButton": { locator: $('.test-save-btn') },
        }
    }
}

export = UserDetails;
