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
            "name": "Bank", //difference is for overview sorting cases
            "activated": true,
            "requestTypes": []
        }),
        new Organisation("DataBank", {
            "shortName": "DataBank",
            "name": "Data-bank",
            "activated": true,
            "requestTypes": ["Data"]
        }),
        new Organisation("ImageBank", {
            "shortName": "ImageBank",
            "name": "Image-bank",
            "activated": true,
            "requestTypes": ["Images"]
        }),
        new Organisation("BioBank", {
            "shortName": "BioBank",
            "name": "Bio-bank",
            "activated": true,
            "requestTypes": ["Material"]
        }),
        new Organisation("MultiBank", {
            "shortName": "MultiBank",
            "name": "Multi-bank",
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
            "requestTypes": ["Material", "Images", "Data"],
            "organisations": ["BioBank", "MultiBank"],
            "combinedRequest": null
        }),
        new Request("Request01-a", {
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
            "requestTypes": ["Material"],
            "organisations": ["BioBank"],
            "combinedRequest": null,
            "status": 'In validation',
        }),
        new Request("Request01-b", {
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
            "requestTypes": ["Material", "Images", "Data"],
            "organisations": ["MultiBank"],
            "combinedRequest": null,
            "status": 'In validation',
        }),
        new Request("Request02", {
            "title": "Request02",
            "background": "background02",
            "research question": "research question02",
            "hypothesis": "hypothesis02",
            "methods": "methods02",
            "related request number": "01",
            "piName": "piName02",
            "piEmail": "piEmail02@localhost",
            "piFunction": "piFunction02",
            "piAffiliation": "piAffiliation02",
            "searchQuery": "searchQuery02",
            "requestTypes": ["Data"],
            "organisations": ["DataBank"],
            "combinedRequest": null,
            "status": 'In validation',
            "requesterName": 'Linda New bee',
        }),
        new Request("Draft01", {
            "title": "Draft01",
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
            "requestTypes": ["Material", "Images", "Data"],
            "organisations": ["BioBank", "MultiBank"],
            "combinedRequest": null,
            "status": 'Draft',
        }),
        new Request("Draft02", {
            "title": "Draft02",
            "background": "background02",
            "research question": "research question02",
            "hypothesis": "hypothesis02",
            "methods": "methods02",
            "related request number": "01",
            "piName": "piName02",
            "piEmail": "piEmail02@localhost",
            "piFunction": "piFunction02",
            "piAffiliation": "piAffiliation02",
            "searchQuery": "searchQuery02",
            "requestTypes": ["Data"],
            "organisations": ["DataBank"],
            "combinedRequest": null,
            "status": 'Draft',
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
