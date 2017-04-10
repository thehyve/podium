/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import {Persona} from "../protractor-stories/director";
import Rob = require("./Rob")
import Simone = require("./Simone")
import Dave = require("./Dave")
import Linda = require("./Linda")
import System = require("./System")
import Admin = require("./Admin")
import BBMRI_Admin = require("./BBMRI_Admin")


/*
 * pages must be added to this dictionary for the director class to find them.
 */
let PersonaDictionary: {[key: string]: Persona} = Object.create(null);

PersonaDictionary['Rob'] = Rob;
PersonaDictionary['Simone'] = Simone;
PersonaDictionary['Dave'] = Dave;
PersonaDictionary['Linda'] = Linda;
PersonaDictionary['System'] = System;
PersonaDictionary['Admin'] = Admin;
PersonaDictionary['BBMRI_Admin'] = BBMRI_Admin;

export = PersonaDictionary;
