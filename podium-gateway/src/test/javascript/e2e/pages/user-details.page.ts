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
import ProfileForm = require("./modules/profile-form")


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
            "login": {locator: $('.test-login')},
            "firstName": {locator: $('.test-firstName')},
            "lastName": {locator: $('.test-lastName')},
            "email": {locator: $('.test-email')},
            "telephone": {locator: $('.test-telephone')},
            "institute": {locator: $('.test-institute')},
            "department": {locator: $('.test-department')},
            "jobTitle": {locator: $('.test-jobTitle')},
            "specialism": {locator: $('.test-specialism')},
            "activated": {locator: $('.test-activated')},
            "authority": {locator: $('.test-authority')},
        }
    }
}

export = UserDetails;
