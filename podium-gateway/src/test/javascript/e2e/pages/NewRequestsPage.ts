/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import {browser, $} from "protractor";
import {Interactable} from "../protractor-stories/director";
import ProfileForm = require("./modules/ProfileForm")


/*
 * Must follow the Page interface
 * pages hold all stateless information on a page.
 */
class NewRequestsPage {
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
        this.name = "new requests";
        this.url = "#/requests/new";
        this.elements = {
            "new draft": {locator: $('button[type=button]')},
            "title": {locator: $('input[name=title]')},
            "background": {locator: $('textarea[name=background]')},
            "research question": {locator: $('textarea[name=researchQuestion]')},
            "hypothesis": {locator: $('textarea[name=hypothesis]')},
            "methods": {locator: $('textarea[name=methods]')},
            "related request number": {locator: $('input[name=relatedRequestNumber]')},
            "piName": {locator: $('input[name=piName]')},
            "piEmail": {locator: $('input[name=piEmail]')},
            "piFunction": {locator: $('input[name=piFunction]')},
            "piAffiliation": {locator: $('input[name=piAffiliation]')},
            "searchQuery": {locator: $('textarea[name=searchQuery]')},
            "type Data": {locator: $('input[value=Data]')},
            "type Images": {locator: $('input[value=Images]')},
            "type Material": {locator: $('input[value=Material]')},
            "clear": {locator: $('#clear-draft')},
            "save": {locator: $('#save-draft')},
            "submit": {locator: $('#submit-request')},
        }
    }
}

export = NewRequestsPage;
