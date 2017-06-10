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
    "research question": string;
    hypothesis: string;
    methods: string;
    "related request number": string;
    piName: string;
    piEmail: string;
    piFunction: string;
    piAffiliation: string;
    searchQuery: string;
    requestTypes: string[];
}

export class Request {
    constructor(public dataID: string, requestData: Irequest) {
        Object.assign(this, requestData)
    }
}

export class File {
    constructor(public dataID: string, public path: string) {
    }
}
