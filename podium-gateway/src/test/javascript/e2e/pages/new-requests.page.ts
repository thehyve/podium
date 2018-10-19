/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { browser } from 'protractor';
import { Interactable } from '../protractor-stories/director';
import requestEditForm = require("./modules/request-edit-form");


/*
 * Must follow the Page interface
 * pages hold all stateless information on a page.
 */
class NewRequestsPage {
    public name: string;
    public url: string;
    public elements: { [name: string]: Interactable };

    public at() {
        let that = this;
        return new Promise<boolean>((resolve) =>
            browser.getCurrentUrl().then(function (currentUrl) {
                resolve((browser.baseUrl + that.url) == currentUrl);
            })
        );
    }

    constructor() {
        this.name = "new requests";
        this.url = "#/requests/new";
        this.elements = {
            ...requestEditForm()
        }
    }
}

export = NewRequestsPage;
