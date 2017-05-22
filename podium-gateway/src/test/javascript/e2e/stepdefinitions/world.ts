/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { Director } from "../protractor-stories/director";
import { AdminConsole } from "../protractor-stories/admin-console";
let {defineSupportCode} = require('cucumber');
import initPages = require ("../pages/page-dictionary");
import PersonaDictionary = require("../personas/persona-dictionary")
import initDataDictionary = require("../data/data-dictionary")

/*
 *
 * The world class is recreated at the start of a cucumber scenario.
 * It is given as the 'this' context to steps and hooks
 * Here it is used to create the director class at the start of a scenario so it can be accessed with this.director in every step
 */
class World {
    public director;
    public adminConsole;
    public scenarioData;

    constructor() {
        let Pages = initPages();
        let DataDictionary = initDataDictionary;

        this.director = new Director(__dirname + '/..', Pages, PersonaDictionary, DataDictionary);
        this.adminConsole = new AdminConsole();
    }
}

function initWolrd() {
    return new World();
}

defineSupportCode(function ({setWorldConstructor}) {
    setWorldConstructor(initWolrd)
});
