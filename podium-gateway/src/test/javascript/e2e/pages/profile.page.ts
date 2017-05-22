/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { $ } from "protractor";
import { Interactable } from "../protractor-stories/director";
import ProfileForm = require("./modules/profile-form")

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
            ...ProfileForm(),
            "institute": {locator: $('.form-control-static')}, //not future-proof
            "SuccessMessage": {locator: $('.alert-success').$('strong')},
        }
    }
}

export = ProfilePage;
