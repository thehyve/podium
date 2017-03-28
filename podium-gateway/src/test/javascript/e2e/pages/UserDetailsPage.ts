/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import {browser, $, $$, by, element} from "protractor";
import {Interactable} from "../protractor-stories/director";
import ProfileForm = require("./modules/ProfileForm")


/*
 * Must follow the Page interface
 * pages hold all stateless information on a page.
 */
class UserDetails {
    public name: string;
    public url: string;
    public elements: {[name: string]: Interactable};

    public at() {
        let that = this;
        return browser.getCurrentUrl().then(function (currentUrl) {
            return (browser.baseUrl + that.url) == currentUrl;
        })
    }

    constructor() {
        this.name = "User Details";
        this.url = "#/user-management/";
        this.elements = {
            "login": {locator: $('.test_login')},
            "firstName": {locator: $('.test_firstName')},
            "lastName": {locator: $('.test_lastName')},
            "email": {locator: $('.test_email')},
            "telephone": {locator: $('.test_telephone')},
            "institute": {locator: $('.test_institute')},
            "department": {locator: $('.test_department')},
            "jobTitle": {locator: $('.test_jobTitle')},
            "specialism": {locator: $('.test_specialism')},
            "activated": {locator: $('.test_activated')},
            "authority": {locator: $('.test_authority')},
        }
    }
}

export = UserDetails;