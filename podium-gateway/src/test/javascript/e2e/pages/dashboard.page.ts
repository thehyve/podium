/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { browser, $ } from "protractor";
import { Interactable } from "../protractor-stories/director";

class DashboardPage {
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
        this.name = "Dashboard";
        this.url = "#/dashboard";
        this.elements = {
            "submitButton": {locator: $('span[ng-reflect-inner-h-t-m-l]')},
        }
    }
}

export = DashboardPage;
