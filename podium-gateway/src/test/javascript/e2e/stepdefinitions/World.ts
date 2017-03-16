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
import PageDictionary = require("../pages/PageDictionary")
import PersonaDictionary = require("../personas/PersonaDictionary")

/*
 *
 * The world class is recreated at the start of a cucumber scenario.
 * It is given as the 'this' context to a step
 * Here it is used to create the director class at the start of a scenario so it can be accessed with this.director in every step
 */
class World {
    public director;
    public scenarioData;

    constructor() {
        this.director = new Director(__dirname + '/..', PageDictionary, PersonaDictionary);
    }
}

export = function () {
    this.World = World;
};
