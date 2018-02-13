/*
 *
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

interface Iorganisation {
    name: string;
    shortName: string;
    activated: boolean;
    requestTypes: string[];
}

export class Organisation {
    constructor(public dataID: string, orgData: Iorganisation) {
        Object.assign(this, orgData)
    }
}

interface Irequest {
    title: string;
    background: string;
    researchQuestion: string;
    hypothesis: string;
    methods: string;
    relatedRequestNumber: string;
    name: string;
    email: string;
    jobTitle: string;
    affiliation: string;
    searchQuery: string;
    requestType: string[];
    organisations: string[];
    combinedRequest?: boolean;
    status?: string;
    requesterDataId?: string; //default user that creates this request
    files?: string[]
}

export class Request {
    constructor(public dataID: string, requestData: Irequest) {
        Object.assign(this, requestData)
    }
}

interface Ifile {
    path: string;
}

export class FileData {
    constructor(public dataID: string, requestData: Ifile) {
        Object.assign(this, requestData)
    }
}
