/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { Organisation, Request } from './templates';

function initDataDictionary() {
    let dataDictionary: { [key: string]: any } = Object.create(null);
    let dataObjects = [
        new Organisation('SomeBank', {
            "shortName": "SomeBank",
            "name": "International bank",
            "activated": true,
            "requestTypes": []
        }),
        new Organisation("VarnameBank", {
            "shortName": "VarnameBank",
            "name": "International variable name bank",
            "activated": true,
            "requestTypes": []
        }),
        new Organisation("XBank", {
            "shortName": "XBank",
            "name": "Bank",
            "activated": true,
            "requestTypes": []
        }),
        new Organisation("DataBank", {
            "shortName": "DataBank",
            "name": "Data-Bank",
            "activated": true,
            "requestTypes": ["Data"]
        }),
        new Organisation("ImageBank", {
            "shortName": "ImageBank",
            "name": "Image-Bank",
            "activated": true,
            "requestTypes": ["Images"]
        }),
        new Organisation("BioBank", {
            "shortName": "BioBank",
            "name": "Bio-Bank",
            "activated": true,
            "requestTypes": ["Material"]
        }),
        new Organisation("MultiBank", {
            "shortName": "MultiBank",
            "name": "Multi-Bank",
            "activated": true,
            "requestTypes": ["Material", "Images", "Data"]
        }),
        new Organisation("NewOrg", {
            "shortName": "NewOrg",
            "name": "New Organization",
            "activated": false,
            "requestTypes": []
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
            "requestTypes": ["Material", "Images", "Data"]
        }),
        {
            "dataID": "menuRoleMapping",
            "Organisation administrator": "ROLE_ORGANISATION_ADMIN",
            "Organisation coordinator": "ROLE_ORGANISATION_COORDINATOR",
            "Reviewer": "ROLE_REVIEWER",
        }
    ];

    dataObjects.forEach(function (data) {
        dataDictionary[data.dataID] = data;
    });

    return dataDictionary;
}

export = initDataDictionary;
