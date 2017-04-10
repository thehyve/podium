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
class OrganisationsManagementPage {
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
        this.name = "organisation management";
        this.url = "#/organisation";
        this.elements = {
            "HeaderShortName": {locator: $('th[jhisortby=shortName]')},
            "HeaderName": {locator: $('th[jhisortby=name]')},
        }
    }
}

export = OrganisationsManagementPage;
