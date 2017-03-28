/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import {Director} from "../protractor-stories/director";
import {AdminConsole} from "../protractor-stories/AdminConsole";
import {isUndefined} from "util";

export = function () {
    this.setDefaultTimeout(30 * 1000); //max time before callback

    this.When(/^(.*) creates a new draft filling data for '(.*)'$/, function (personaName, requestName, callback) {
        let director = this.director as Director;
        let persona = director.getPersona(personaName);
        let request = director.getData(requestName);
        this.scenarioData = request; //store for next step
        let page = director.getCurrentPage();
        let promisses = [];

        director.clickOn("new draft").then(function () {
            ["title", "background", "research question", "hypothesis", "methods", "related request number", "piName",
                "piEmail", "piFunction", "piAffiliation", "searchQuery"].forEach(function (key) {
                promisses.push(director.enterText(key, request.properties[key]));
            });

            ["type Data", "type Images", "type Material"].forEach(function (key) {
                if (request.properties[key]) {
                    promisses.push(director.clickOn(key));
                }
            });

            Promise.all(promisses).then(function () {
                director.clickOn("save").then(callback,callback)
            })
        })

    });

    this.Then(/^the draft is saved$/, function (callback) {
        let director = this.director as Director;
        let adminConsole = this.adminConsole as AdminConsole;

        let persona = director.getPersona('he');

        adminConsole.checkDraft(this.scenarioData, checkDraft, callback, persona);

    });
}

function checkDraft(expected, realData) {
    if (isUndefined(realData)) {
        return false
    }
    return true
}
