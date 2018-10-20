/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
import { FileData, Organisation, Request } from './templates';

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
            "researchQuestion": "research question01",
            "hypothesis": "hypothesis01",
            "methods": "methods01",
            "relatedRequestNumber": "",
            "name": "name01",
            "email": "email01@localhost",
            "jobTitle": "jobTitle01",
            "affiliation": "affiliation01",
            "searchQuery": "searchQuery01",
            "requestType": ["Material", "Images", "Data"],
            "organisations": ["BioBank", "MultiBank"],
            "combinedRequest": false
        }),
        new Request("Request01-a", {
            "title": "Request01",
            "background": "background01",
            "researchQuestion": "research question01",
            "hypothesis": "hypothesis01",
            "methods": "methods01",
            "relatedRequestNumber": "-",
            "name": "name01",
            "email": "email01@localhost",
            "jobTitle": "jobTitle01",
            "affiliation": "affiliation01",
            "searchQuery": "searchQuery01",
            "requestType": ["Material"],
            "organisations": ["BioBank"],
            "combinedRequest": false,
            "status": 'In validation',
            "requesterDataId": 'Linda',
        }),
        new Request("Request01-b", {
            "title": "Request01",
            "background": "background01",
            "researchQuestion": "research question01",
            "hypothesis": "hypothesis01",
            "methods": "methods01",
            "relatedRequestNumber": "-",
            "name": "name01",
            "email": "email01@localhost",
            "jobTitle": "jobTitle01",
            "affiliation": "affiliation01",
            "searchQuery": "searchQuery01",
            "requestType": ["Material", "Images", "Data"],
            "organisations": ["MultiBank"],
            "combinedRequest": false,
            "status": 'In validation',
            "requesterDataId": 'Linda',
        }),
        new Request("Request02", {
            "title": "Request02",
            "background": "background02",
            "researchQuestion": "research question02",
            "hypothesis": "hypothesis02",
            "methods": "methods02",
            "relatedRequestNumber": "01",
            "name": "name02",
            "email": "email02@localhost",
            "jobTitle": "jobTitle02",
            "affiliation": "affiliation02",
            "searchQuery": "searchQuery02",
            "requestType": ["Data"],
            "organisations": ["DataBank"],
            "combinedRequest": false,
            "status": 'In validation',
            "requesterDataId": 'Linda',
        }),
        new Request("Request03", {
            "title": "Request03",
            "background": "background03",
            "researchQuestion": "research question03",
            "hypothesis": "hypothesis03",
            "methods": "methods03",
            "relatedRequestNumber": "02",
            "name": "name03",
            "email": "email03@localhost",
            "jobTitle": "jobTitle03",
            "affiliation": "affiliation03",
            "searchQuery": "searchQuery03",
            "requestType": ["Material", "Images", "Data"],
            "organisations": ["MultiBank"],
            "combinedRequest": false,
            "status": 'In validation',
            "requesterDataId": 'Linda',
        }),
        new Request("Draft01", {
            "title": "Draft01",
            "background": "background01",
            "researchQuestion": "research question01",
            "hypothesis": "hypothesis01",
            "methods": "methods01",
            "relatedRequestNumber": "-",
            "name": "name01",
            "email": "email01@localhost",
            "jobTitle": "jobTitle01",
            "affiliation": "affiliation01",
            "searchQuery": "searchQuery01",
            "requestType": ["Material", "Images", "Data"],
            "organisations": ["BioBank", "MultiBank"],
            "combinedRequest": false,
            "status": 'Draft',
        }),
        new Request("Draft02", {
            "title": "Draft02",
            "background": "background02",
            "researchQuestion": "research question02",
            "hypothesis": "hypothesis02",
            "methods": "methods02",
            "relatedRequestNumber": "01",
            "name": "name02",
            "email": "email02@localhost",
            "jobTitle": "jobTitle02",
            "affiliation": "affiliation02",
            "searchQuery": "searchQuery02",
            "requestType": ["Data"],
            "organisations": ["DataBank"],
            "combinedRequest": false,
            "status": 'Draft',
        }),
        new Request("Draft03", {
            "title": "Draft03",
            "background": "background Draft03",
            "researchQuestion": "research question Draft03",
            "hypothesis": "hypothesis Draft03",
            "methods": "methods Draft03",
            "relatedRequestNumber": "Draft01",
            "name": "name Draft03",
            "email": "emailDraft03@localhost",
            "jobTitle": "jobTitle Draft03",
            "affiliation": "affiliation Draft03",
            "searchQuery": "searchQuery Draft03",
            "requestType": ["Data"],
            "organisations": ["DataBank", "MultiBank"],
            "combinedRequest": false,
            "status": 'Draft',
        }),
        new Request("DraftWithFile", {
            "title": "DraftWithFile",
            "background": "background DraftWithFile",
            "researchQuestion": "research question DraftWithFile",
            "hypothesis": "hypothesis DraftWithFile",
            "methods": "methods DraftWithFile",
            "relatedRequestNumber": "Draft01",
            "name": "name DraftWithFile",
            "email": "emailDraftWithFile@localhost",
            "jobTitle": "jobTitle DraftWithFile",
            "affiliation": "affiliation DraftWithFile",
            "searchQuery": "searchQuery DraftWithFile",
            "requestType": ["Data"],
            "organisations": ["DataBank", "MultiBank"],
            "combinedRequest": false,
            "status": 'Draft',
            "files": ["exampleFile"],
        }),
        new FileData("exampleFile", { path: "example.txt" }),
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
