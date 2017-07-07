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
class RequestDetails {
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
        this.name = "Request Details";
        this.url = "#/requests/detail/";
        this.elements = {
            "title": { locator: $('.test-title') },
            "requestTypes": { locator: $('.test-requestTypes') },
            "combinedRequest": { locator: $('.test-combinedRequest') },
            "organisations": { locator: $('.test-organisations') },
            "searchQuery": { locator: $('.test-searchQuery') },
            "background": { locator: $('.test-background') },
            "researchQuestion": { locator: $('.test-researchQuestion') },
            "hypothesis": { locator: $('.test-hypothesis') },
            "methods": { locator: $('.test-methods') },
            "relatedRequestNumber": { locator: $('.test-relatedRequestNumber') },
            "piName": { locator: $('.test-piName') },
            "piEmail": { locator: $('.test-piEmail') },
            "piFunction": { locator: $('.test-piFunction') },
            "piAffiliation": { locator: $('.test-piAffiliation') },
            "validationCheck": { locator: $('#validationCheck') },
            "validate": { locator: $('#validate-request-btn') },
            "approve": { locator: $('#approve-request-btn') },
            "reject": { locator: $('.test-reject-request-btn') },
            "revision": { locator: $('.test-request-revision-btn') },
            "close": { locator: $('.test-close-request-btn') },
            "startDelivery": { locator: $('.test-request-start-delivery-btn') },
            "messageSummary": { locator: $('#messageSummary') },
            "messageDescription": { locator: $('#messageDescription') },
            "cancel": { locator: $('.test-cancel-btn') },
            "submit": { locator: $('.test-submit-btn') },
            "deliveryAction": { locator: $('.test-delivery-action-btn') },
            "reference": { locator: $('#reference') },
            "deliveryrowImages": { locator: $('.test-deliveryrow-Images') },
            "deliveryrowMaterial": { locator: $('.test-deliveryrow-Material') },
            "deliveryrowData": { locator: $('.test-deliveryrow-Data') },
            "finalize": { locator: $('.test-request-finalize-request-btn') },
            "finalizeSubmit": { locator: $('.btn-success') },
        }
    }
}

export = RequestDetails;
