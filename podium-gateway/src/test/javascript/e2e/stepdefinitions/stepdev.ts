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
import {Director} from "../protractor-stories/director";
import {Promise} from "es6-promise";
import SigninPage = require("../pages/SigninPage")
import PageDictionary = require("../pages/PageDictionary")
import PersonaDictionary = require("../personas/PersonaDictionary")
import {AdminConsole} from "../protractor-stories/AdminConsole";
import {isUndefined} from "util";


export = function () {

    this.Given(/^Test$/, function (callback) {
        let adminConsole = this.adminConsole;
        let org = this.director.getData('NewOrg');

        adminConsole.checkOrganization(org, check, callback);
    });
}

function check(expected, realData){
    if (isUndefined(realData)){
        return false
    }
    return realData.activated == expected.properties.activated;
}
