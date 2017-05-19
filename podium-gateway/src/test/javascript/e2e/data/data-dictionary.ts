/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

import {Organisation, Request, File} from "./templates"
import {normalize} from 'path'

let dataDictionary: {[key: string]: any} = Object.create(null);
let dataObjects = [
    new Organisation('SomeBank', {
        "shortName": "SomeBank",
        "name": "International bank",
        "activated": true
    }),
    new Organisation("VarnameBank", {
        "shortName": "VarnameBank",
        "name": "International variable name bank",
        "uuid": "12dd08b3-eb8b-476e-a0b3-716cb6b5df7a",
        "activated": true
    }),
    new Organisation("XBank", {
        "shortName": "XBank",
        "name": "Bank",
        "activated": true
    }),
    new Organisation("NewOrg", {
        "shortName": "NewOrg",
        "name": "New Organization",
        "activated": false
    }),
    new Request("Request01", {
        "title": "Request01",
        "background": "background01",
        "research question": "research question01",
        "hypothesis": "hypothesis01",
        "methods": "methods01",
        "related request number": "",
        "piName": "piName01",
        "piEmail": "piEmail01@localhost",
        "piFunction": "piFunction01",
        "piAffiliation": "piAffiliation01",
        "searchQuery": "searchQuery01",
        "type Data": true,
        "type Images": true,
        "type Material": true,
    }),
    new File("example", normalize(__dirname + "/example")),
];

dataObjects.forEach(function (persona) {
    dataDictionary[persona.name] = persona;
});

export = dataDictionary;
