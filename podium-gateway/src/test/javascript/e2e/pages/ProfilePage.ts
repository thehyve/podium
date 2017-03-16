/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import {$} from "protractor";
import {Interactable} from "../protractor-stories/director";
/*
 * Must follow the Page interface
 * pages hold all stateless information on a page.
 */
class ProfilePage {
    public name: string;
    public url: string;
    public elements: {[name: string]: Interactable};

    constructor() {
        this.name = "profile";
        this.url = "#/settings";
        this.elements = {
            "firstName": {locator: $('input[name=firstName]')},
            "lastName": {locator: $('input[name=lastName]')},
            "email": {locator: $('input[name=email]')},
            "telephone": {locator: $('input[name=telephone]')},
            "institute": {locator: $('.form-control-static')}, //not future-proof
            "department": {locator: $('input[name=department]')},
            "jobTitle": {locator: $('input[name=jobTitle]')},
            "specialism": {locator: $('input[name=specialism]')},
            "submitButton": {locator: $('button[type=submit]')},
            "SuccessMessage": {locator: $('.alert-success').$('strong')},
            // "Other specialism": {locator: $('button[name=jobTitle]')},
        }
    }
}

export = ProfilePage;
