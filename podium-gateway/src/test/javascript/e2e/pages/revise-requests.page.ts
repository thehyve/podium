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
class ReviseRequestsPage {
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
        this.name = "revise requests";
        this.url = "#/requests/detail/";
        this.elements = {
            "new draft": { locator: $('button[type=button]') },
            "title": { locator: $('input[name=title]') },
            "background": { locator: $('textarea[name=background]') },
            "research question": { locator: $('textarea[name=researchQuestion]') },
            "hypothesis": { locator: $('textarea[name=hypothesis]') },
            "methods": { locator: $('textarea[name=methods]') },
            "related request number": { locator: $('input[name=relatedRequestNumber]') },
            "piName": { locator: $('input[name=piName]') },
            "piEmail": { locator: $('input[name=piEmail]') },
            "piFunction": { locator: $('input[name=piFunction]') },
            "piAffiliation": { locator: $('input[name=piAffiliation]') },
            "searchQuery": { locator: $('textarea[name=searchQuery]') },
            "Data": { locator: $('input[value=Data]') },
            "Images": { locator: $('input[value=Images]') },
            "Material": { locator: $('input[value=Material]') },
            "organisations": { locator: $('#organisations') },
            "cancel": { locator: $('#cancel-btn') },
            "save": { locator: $('#save-request-btn') },
            "submit": { locator: $('#submit-request-btn') },
        }
    }
}

export = ReviseRequestsPage;
