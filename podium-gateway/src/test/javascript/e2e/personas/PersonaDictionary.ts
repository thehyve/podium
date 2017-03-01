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
import AdminPersona = require("./AdminPersona")
/*
 * pages must be added to this dictionary for the director class to find them.
 */
let PersonaDictionary: {[key: string]: Persona} = Object.create(null);

PersonaDictionary['AdminPersona'] = AdminPersona;


export = PersonaDictionary;
